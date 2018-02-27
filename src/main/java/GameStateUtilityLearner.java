import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.distribution.NormalDistribution;

public class GameStateUtilityLearner {

	private double[][] population;
	private final double[] individualsFitness;
	private HashSet<Integer> individualsSelected;
	private ArrayList<Integer> individualsSelectedList;
	private Object[] individualsTaskFutures;
	private double totalFitness = 0;

	private int noOfTriesPerIndividual;
	private int noOfGenerations;
	private int populationSize; 
	private double mutationProb;
	private double selectedFraction;
	private double tournamentSampleRatio;
	private ExecutorService executor;

	public static int WEIGHTS_COUNT = 15;
	public static int MAX_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

	public GameStateUtilityLearner(int noOfTriesPerIndividual, int noOfGenerations,
		int populationSize, double mutationProb, double selectedFraction, double tournamentSampleRatio) {
		this.noOfTriesPerIndividual = noOfTriesPerIndividual;
		this.noOfGenerations = noOfGenerations;
		this.populationSize = populationSize;
		this.mutationProb = mutationProb;
		this.tournamentSampleRatio = tournamentSampleRatio;
		this.selectedFraction = selectedFraction;

		this.population = new double[this.populationSize][WEIGHTS_COUNT];
		this.individualsFitness = new double[this.populationSize];
		this.individualsSelected = new HashSet<>(this.populationSize);
		this.individualsSelectedList = new ArrayList<>(this.populationSize);
		this.individualsTaskFutures = new Object[this.populationSize];
	}

	private double getFitness(double[] weights) {
		if (weights.length != WEIGHTS_COUNT) {
			throw new IllegalStateException();
		}

		GameStateUtilityFunction utilityFunction = new GameStateUtilityFunction(weights);
		GameStateSearcher gameStateSearcher = new GameStateSearcher(utilityFunction);

		int rowsCleared = 0;
		for (int i = 0; i < this.noOfTriesPerIndividual; i ++) {
			GameState gameState = new GameState();
			while(gameState.hasPlayerLost() == 0) {
				// Randomly get a piece
				int nextPiece = gameState.getRandomNextPiece();
				gameState.setNextPiece(nextPiece);
				GameStateSearcher.BestMoveResult result = gameStateSearcher.searchNLevelsDFS(gameState, 1);
				int[] move = result.move;
				gameState.makePlayerMove(move[GameState.ORIENT], move[GameState.SLOT]);
			}
			rowsCleared += gameState.getRowsCleared(); 
		}

		double fitness = (double)rowsCleared / this.noOfTriesPerIndividual;
		return fitness;
	}

	private double doGaussianMutation(double x, double min, double max) {
		NormalDistribution dist = new NormalDistribution(x, (max - min) / 10);
		return Math.min(Math.max(0, dist.sample()), 1);
	}

	private void initPopulation(double[][] initialWeightsSet) {
		// In order, each initial weight gets to randomly influence an individual 
		int i = 0;
		while (i < this.populationSize) {
			double[] initialWeights = initialWeightsSet[i % initialWeightsSet.length];
			for (int j = 0; j < WEIGHTS_COUNT; j ++) {
				double w = initialWeights[j];
				// Do gaussian mutation on every weight
				this.population[i][j] = this.doGaussianMutation(w, 0f, 1f);
			}
			i++;
		}
	}
	
	private void doTournamentSelection() {
		this.individualsSelected.clear();
		this.individualsSelectedList.clear();

		while (this.individualsSelectedList.size() < this.populationSize * selectedFraction) {
			int winnerIndex = -1;
			double bestIndividualFitness = -Double.MAX_VALUE;
			
			// Randomly choose tournament sample and compare against best individual so far
			for (int i = 0; i < this.populationSize * selectedFraction * tournamentSampleRatio; i ++) {
				int candidateIndex = (int)(Math.random() * this.populationSize);
				double candidateFitness = this.individualsFitness[candidateIndex];
				if (candidateFitness > bestIndividualFitness) {
					bestIndividualFitness = candidateFitness;
					winnerIndex = candidateIndex;
				}
			}

			this.individualsSelected.add(winnerIndex);
			this.individualsSelectedList.add(winnerIndex);
		}
	}

	private void evaluatePopulationFitness() {
		for (int i = 0; i < this.populationSize; i ++) {
			final double[] individual = this.population[i];
			this.individualsTaskFutures[i] = executor.submit(() -> {
				return this.getFitness(individual);
			});
		}

		double min = Double.MAX_VALUE;
		for (int i = 0; i < this.populationSize; i ++) {
			try {
				Future<Double> future = (Future<Double>)this.individualsTaskFutures[i];
				this.individualsFitness[i] = future.get();
				if (this.individualsFitness[i] < min) {
					min = this.individualsFitness[i];
				}
			} catch (ExecutionException error) {
				throw new Error("Execution exception reached.");
			} catch (InterruptedException error) {
				throw new Error("Interrupted exception reached.");
			}
		}

		// Ensure that all fitness scores are positive
		this.totalFitness = 0;
		for (int i = 0; i < this.populationSize; i ++) {
			this.individualsFitness[i] = this.individualsFitness[i] + min;
			this.totalFitness += this.individualsFitness[i];
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
				for (int j = 0; j < WEIGHTS_COUNT; j ++) {
					this.population[i][j] = ratio * p1[j] + (1 - ratio) * p2[j];
				} 
			}
		}
	}

	private void doRandomMutation() {
		for (int i = 0; i < this.populationSize; i ++) {
			for (int j = 0; j < WEIGHTS_COUNT; j ++) {
				if (Math.random() < this.mutationProb) {
					this.population[i][j] = doGaussianMutation(this.population[i][j], 0, 1);
				}
			}  
		}
	}

	public double[] train() {
		// Randomly initialize weights
		double[] weights = new double[WEIGHTS_COUNT];
		for (int i = 0; i < WEIGHTS_COUNT; i ++) {
			weights[i] = Math.random();
		}
		return train(new double[][] { weights });
	}

	public double[] train(double[][] initialWeights) {
		executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);

		this.initPopulation(initialWeights);

		for (int g = 0; g < this.noOfGenerations; g ++) {
			this.evaluatePopulationFitness();
			this.doTournamentSelection();
			this.doWeightedAverageCrossover();
			this.doRandomMutation();
			prettyPrintGeneration(g);
		}

		double bestFitness = -Double.MAX_VALUE;
		double[] bestWeights = null;

		for (int i = 0; i < this.populationSize; i ++) {
			if (this.individualsFitness[i] > bestFitness) {
				bestWeights = this.population[i];
				bestFitness = this.individualsFitness[i];
			} 
		}

		executor.shutdown();

		return bestWeights;
	}    
	
	private int getBestIndividualIndex() {
		double bestFitness = -Double.MAX_VALUE;
		int index = -1;
		
		for (int i = 0; i < this.populationSize; i ++) {
			if (this.individualsFitness[i] > bestFitness) {
				index = i;
				bestFitness = this.individualsFitness[i];
			} 
		}
		
		return index;
	}

	private void prettyPrintGeneration(int generation) {
		int winnerIndex = this.getBestIndividualIndex();
		double fitness = this.individualsFitness[winnerIndex];
		double[] bestIndividual = this.population[winnerIndex];
		System.out.println("Best phenotype of generation " + generation +
			" has fitness " + fitness + " with weights " + Arrays.toString(bestIndividual));
	}
}
