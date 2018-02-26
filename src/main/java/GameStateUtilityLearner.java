import java.util.*;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

public class GameStateUtilityLearner {

	public int noOfGenerations;
	public int noOfTriesPerIndividual;

	public static int WEIGHTS_COUNT = 8;

	public GameStateUtilityLearner(int noOfGenerations, int noOfTriesPerIndividual) {
		this.noOfGenerations = noOfGenerations;
		this.noOfTriesPerIndividual = noOfTriesPerIndividual;
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
				.build();

		// Train!
		Genotype<DoubleGene> result = engine.stream()
				.limit(this.noOfGenerations)
				.collect(EvolutionResult.toBestGenotype());

		double[] bestWeights = this.genotypeToWeights(result);
		return bestWeights;
	}       
	
	private double[] genotypeToWeights(Genotype<DoubleGene> gt) {
		double[] weights = gt.stream().flatMap(x -> x.stream())
			.map(x -> x.doubleValue()).mapToDouble(x -> x).toArray();
		return weights;
	}
}
