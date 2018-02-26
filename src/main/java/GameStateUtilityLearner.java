import java.util.*;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;

public class GameStateUtilityLearner {

	public static int CUTOFF_STEADY_FITNESS_GENERATIONS = 100; 
	public int noOfTriesPerIndividual;
	public int populationSize; 
	public float mutationProb;

	public static int WEIGHTS_COUNT = 8;

	public GameStateUtilityLearner(int noOfTriesPerIndividual, int populationSize, float mutationProb) {
		this.noOfTriesPerIndividual = noOfTriesPerIndividual;
		this.populationSize = populationSize;
		this.mutationProb = mutationProb;
	}

	private double getFitness(Genotype<DoubleGene> gt) {
		double[] weights = this.genotypeToWeights(gt);

		if (weights.length != WEIGHTS_COUNT) {
			throw new IllegalStateException();
		}

		PlayerSkeleton p = new PlayerSkeleton(weights);
		
		double rowsCleared = 0;
		for (int i = 0; i < this.noOfTriesPerIndividual; i ++) {
			State s = new State();
			while(!s.hasLost()) {
				int[] move = p.pickMove(s);
				s.makeMove(move[0], move[1]);
			}
			rowsCleared += s.getRowsCleared(); 
		}

		double fitness = (double)rowsCleared / this.noOfTriesPerIndividual;
		return fitness;
	}

	public double[] train() {
		// Define an individual
		Factory<Genotype<DoubleGene>> gtf =
				Genotype.of(DoubleChromosome.of(-1d, 1d, WEIGHTS_COUNT));

		// Define engine that takes in the utility function
		Engine<DoubleGene, Double> engine = Engine
				.builder(this::getFitness, gtf)
				.alterers(
					new Mutator<>(mutationProb)
				)
				.populationSize(this.populationSize)
				.build();

		// Train!
		Genotype<DoubleGene> result = engine.stream()
				.limit(Limits.bySteadyFitness(CUTOFF_STEADY_FITNESS_GENERATIONS))
				.map(x -> {
					prettyPrintGeneration(x);
					return x;
				})
				.collect(EvolutionResult.toBestGenotype());

		double[] bestWeights = this.genotypeToWeights(result);

		return bestWeights;
	}       

	private void prettyPrintGeneration(EvolutionResult<DoubleGene, Double> result) {
		Phenotype<DoubleGene, Double> phenotype = result.getBestPhenotype();
		long generationCount = phenotype.getGeneration();
		double fitness = phenotype.getFitness();
		Genotype<DoubleGene> genotype = phenotype.getGenotype();
		double[] weights = this.genotypeToWeights(genotype);
		System.out.println("Best phenotype of generation " + generationCount +
			" has fitness " + fitness + " with weights " + Arrays.toString(weights));
	}
	
	private double[] genotypeToWeights(Genotype<DoubleGene> gt) {
		double[] weights = gt.stream().flatMap(x -> x.stream())
			.map(x -> x.doubleValue()).mapToDouble(x -> x).toArray();
		return weights;
	}
}
