import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.apache.commons.math3.distribution.NormalDistribution;

public class GeneticAlgorithmLearner {

	private double[][] population;
	private double[] individualsFitness;
	private Object[] individualsTaskFutures;
	private TreeMap<Double, Integer> fitnessToIndividualIndex;
	private HashSet<Integer> individualsSelected; 
	private double[] bestIndividual;
	private double bestIndividualFitness;
	private int stallGenerations; // No. of generations where there was no new best individual
	private ExecutorService executor;

	// Temporary values
	private ArrayList<Integer> individualsUnselectedList; // For tournament selection
	private ArrayList<Integer> individualsSelectedList; // For tournament selection
	private double[] testIndividual; // For hillclimbing

	// Learning hyperparameters
	private int maxStallGenerations;
	private int populationSize; 
	private int weightsCount;
	private double mutationProb;
	private double mutationDecayRate;
	private double selectedFraction;
	private int tournamentSize;
	private double individualHillClimbRatio;
	private int maxIndividualHillClimbTries;
	private Function<double[], Double> fitnessFunction;

	public static int MAX_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

	public GeneticAlgorithmLearner(Function<double[], Double> fitnessFunction,
		int weightsCount, int maxStallGenerations, int populationSize, double mutationProb, 
		double selectedFraction, int tournamentSize, double mutationDecayRate,
		double individualHillClimbRatio, int maxIndividualHillClimbTries) {
		this.fitnessFunction = fitnessFunction;
		this.maxStallGenerations = maxStallGenerations;
		this.populationSize = populationSize;
		this.mutationProb = mutationProb;
		this.weightsCount = weightsCount;
		this.tournamentSize = tournamentSize;
		this.selectedFraction = selectedFraction;
		this.mutationDecayRate = mutationDecayRate;
		this.individualHillClimbRatio = individualHillClimbRatio;
		this.maxIndividualHillClimbTries = maxIndividualHillClimbTries;

		this.fitnessToIndividualIndex = new TreeMap<>(Collections.reverseOrder());
		this.population = new double[this.populationSize][this.weightsCount];
		this.individualsFitness = new double[this.populationSize];
		this.individualsSelected = new HashSet<>(this.populationSize);
		this.individualsSelectedList = new ArrayList<>(this.populationSize);
		this.individualsUnselectedList = new ArrayList<>(this.populationSize);
		this.individualsTaskFutures = new Object[this.populationSize];
		this.bestIndividual = new double[this.weightsCount];
		this.testIndividual = new double[this.weightsCount];
		this.executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
	}

	// standard deviation = (max - min) * sdRatio
	private double doGaussianMutation(double x, double min, double max, double sdRatio) {
		NormalDistribution dist = new NormalDistribution(x, (max - min) * sdRatio);
		return Math.min(Math.max(min, dist.sample()), max);
	}

	private void initPopulation(double[][] initialWeightsSet) {
		// Each initial weight gets to be an individual 
		// Fill the remainding slots with randomly initialized individuals
		int i = 0;
		for (int w = 0; w < initialWeightsSet.length; w ++) {
			for (int j = 0; j < this.weightsCount; j ++) {
				this.population[i][j] = initialWeightsSet[w][j];
			}
			i ++;
		}

		while (i < this.populationSize) {
			for (int j = 0; j < this.weightsCount; j ++) {
				this.population[i][j] = (Math.random() - 0.5) * 2;
			}
			i++;
		}

		// Void best individual
		Arrays.fill(this.bestIndividual, 0);
		this.bestIndividualFitness = -Double.MAX_VALUE;
	}
	
	private void doTournamentSelection() {
		this.individualsSelected.clear();
		this.individualsSelectedList.clear();

		// Re-populate unselected list with indices
		this.individualsUnselectedList.clear();
		for (int i = 0; i < this.populationSize; i ++) {
			this.individualsUnselectedList.add(i);
		}

		// Conduct tournaments to select survivors
		while (this.individualsSelected.size() < this.populationSize * selectedFraction) {
			// Iterate through sets of individuals in shuffled unselected individuals, holding tournaments
			Collections.shuffle(this.individualsUnselectedList);
			int tournamentsCount = (int)(this.individualsUnselectedList.size() / this.tournamentSize);
			if (tournamentsCount == 0) {
				throw new IllegalStateException("Unable to hold more tournaments due to large tournament size.");
			}
			for (int t = 0; t < tournamentsCount; t ++) {
				int bestListIndex = -1;
				double winnerFitness = -Double.MAX_VALUE;
				for (int i = 0; i < tournamentSize; i ++) {
					int index = t * this.tournamentSize + i;
					int candidateIndex = this.individualsUnselectedList.get(index);
					double candidateFitness = this.individualsFitness[candidateIndex];
					if (candidateFitness > winnerFitness) {
						winnerFitness = candidateFitness;
						bestListIndex = index;
					}
				}
				// Add winner to selected list
				int winnerIndex = this.individualsUnselectedList.get(bestListIndex);
				this.individualsSelected.add(winnerIndex);
				this.individualsSelectedList.add(winnerIndex);
				this.individualsUnselectedList.set(bestListIndex, -1); // -1 to indicate selected
			}
			// Remove all -1s (selected individuals)
			this.individualsUnselectedList.removeIf(x -> x == -1);
		}
	}

	private void evaluatePopulationFitness() {
		for (int i = 0; i < this.populationSize; i ++) {
			final double[] individual = this.population[i];
			this.individualsTaskFutures[i] = this.executor.submit(() -> {
				double fitness = this.fitnessFunction.apply(individual);
				return fitness;
			});
		}

		this.fitnessToIndividualIndex.clear();
		for (int i = 0; i < this.populationSize; i ++) {
			try {
				Future<Double> future = (Future<Double>)this.individualsTaskFutures[i];
				this.individualsFitness[i] = future.get();
				this.fitnessToIndividualIndex.put(this.individualsFitness[i], i);
			} catch (ExecutionException error) {
				throw new Error("Execution exception reached: " + error.getMessage());
			} catch (InterruptedException error) {
				throw new Error("Interrupted exception reached: " + error.getMessage());
			}
		}
	}

	private void doWeightedAverageCrossover() {
		int selectedCount = this.individualsSelectedList.size();
		for (int i = 0; i < this.populationSize; i ++) {
			// If individual has been eliminated
			if (!this.individualsSelected.contains(i)) {
				// Get 2 other random selected individual as parents
				int p1Index = this.individualsSelectedList.get((int)(Math.random() * selectedCount));
				int p2Index = this.individualsSelectedList.get((int)(Math.random() * selectedCount));
				double[] p1 = this.population[p1Index];
				double[] p2 = this.population[p2Index];

				double p1Fitness = this.individualsFitness[p1Index];
				double p2Fitness = this.individualsFitness[p2Index];
				double ratio = (p1Fitness == 0) ? 0 : p1Fitness / (p1Fitness + p2Fitness);
				for (int j = 0; j < this.weightsCount; j ++) {
					this.population[i][j] = ratio * p1[j] + (1 - ratio) * p2[j];
				} 
			}
		}
	}

	private void doRandomMutation(double[] individual, double sdRatio) {
		// Adjust a random weight of the invididual normally
		int j = (int)(Math.random() * this.weightsCount);
		individual[j] = this.doGaussianMutation(individual[j], -1, 1, sdRatio);
	}

	private void doRandomMutation(int generatioNo) {
		double sdRatio =  0.1 * Math.pow(this.mutationDecayRate, generatioNo);
		for (int i = 0; i < this.populationSize; i ++) {
			// If individual is a child, with a probability, mutate it
			if (!this.individualsSelected.contains(i)) {
				if (Math.random() < this.mutationProb) {
					double[] individual = this.population[i];
					doRandomMutation(individual, sdRatio);
				}
			}
		}
	}

	private void hillClimbBestIndividuals(int generatioNo) {
		double sdRatio = 0.05 * Math.pow(this.mutationDecayRate, generatioNo);
		// For each of the best k individuals of population
		int k = 0;
		Iterator<Entry<Double, Integer>> iterator = this.fitnessToIndividualIndex.entrySet().iterator();
		while (k < this.populationSize * this.individualHillClimbRatio) {
			Entry<Double, Integer> entry = iterator.next();
			double fitness = entry.getKey();
			int individualIndex = entry.getValue();
			double[] individual = this.population[individualIndex];
			// With a maximum tries before giving up, hill climb the individual
			int tries = 0;
			while (tries < this.maxIndividualHillClimbTries) {
				System.arraycopy(individual, 0, this.testIndividual, 0, this.weightsCount);
				this.doRandomMutation(this.testIndividual, sdRatio);
				double testFitness = this.fitnessFunction.apply(this.testIndividual);
				if (testFitness > fitness) {
					// Found neighbour that is fitter! Update individual 
					this.fitnessToIndividualIndex.remove(fitness);
					fitness = testFitness;
					this.individualsFitness[individualIndex] = fitness;
					this.fitnessToIndividualIndex.put(fitness, individualIndex);
					System.arraycopy(this.testIndividual, 0, individual, 0, this.weightsCount);
					System.out.println("Made individual fitter during hill-climbing: " + this.getIndividualPrint(individualIndex));
					tries = 0; // Reset tries counter
				} else {
					tries ++;
				}
			}  
			k ++;
		}
	}

	public double[] train() {
		return train(new double[][] { });
	}

	public double[] train(double[][] initialWeights) {
		this.initPopulation(initialWeights);
		this.evaluatePopulationFitness();	
		this.stallGenerations = 0;
		int generation = 0;
		this.prettyPrintBestIndividual(generation);
		// this.prettyPrintPopulation(generation);
		while (this.stallGenerations < this.maxStallGenerations) {
			this.doTournamentSelection();
			this.doWeightedAverageCrossover();
			this.doRandomMutation(generation);
			this.evaluatePopulationFitness();
			this.hillClimbBestIndividuals(generation);
			this.updateBestIndividual();
			generation ++;
			this.prettyPrintBestIndividual(generation);
			this.prettyPrintPopulation(generation);
		}

		return this.bestIndividual;
	}    
	
	private void updateBestIndividual() {
		int newBestIndividualIndex = this.fitnessToIndividualIndex.firstEntry().getValue();
		double newBestFitness = this.individualsFitness[newBestIndividualIndex];
		if (newBestFitness > this.bestIndividualFitness) {
			double[] newBestIndividual = this.population[newBestIndividualIndex];
			this.bestIndividualFitness = newBestFitness;
			for (int w = 0; w < this.weightsCount; w ++) {
				this.bestIndividual[w] = newBestIndividual[w];
			}
			this.stallGenerations = 0;
		} else {
			this.stallGenerations ++;
		}
	}
	
	private void prettyPrintBestIndividual(int generation) {
		int bestIndividualIndex = this.fitnessToIndividualIndex.firstEntry().getValue();
		System.out.println("Generation " + generation + ": Fittest individual is " + this.getIndividualPrint(bestIndividualIndex));
	}

	private String getIndividualPrint(int individualIndex) {
		double fitness = this.individualsFitness[individualIndex];
		return "(Fitness=" + fitness + ")" +
			((fitness == this.bestIndividualFitness) ? "(BEST)" : "") + 
			"(Weights=" + Arrays.toString(this.population[individualIndex]) + ")" + 
			"(Index=" + individualIndex + ")";
	}

	private void prettyPrintPopulation(int generation) {
		System.out.println("Generation " + generation + ":");
		for (int i = 0; i < this.populationSize; i ++)  {
			System.out.println("\t " + this.getIndividualPrint(i));
		}
	}
}
