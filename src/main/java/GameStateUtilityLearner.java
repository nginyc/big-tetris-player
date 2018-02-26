import java.util.*;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

public class GameStateUtilityLearner {

	public static int WEIGHTS_COUNT = 6;

	private double getFitness(Genotype<DoubleGene> gt) {
		double[] weights = this.genotypeToWeights(gt);

		if (weights.length != WEIGHTS_COUNT) {
			throw new IllegalStateException();
		}

		State s = new State();
		PlayerSkeleton p = new PlayerSkeleton(weights);
		while(!s.hasLost()) {
			int[] move = p.pickMove(s);
			s.makeMove(move[0], move[1]);
		}

		double fitness = s.getRowsCleared(); 

		System.out.println("Evaluated fitness of genotype " + Arrays.toString(weights) + " as " + fitness);
		
		return fitness;
	}

	public double[] train() {
		// Define an individual
		Factory<Genotype<DoubleGene>> gtf =
				Genotype.of(DoubleChromosome.of(-1000f, 1000f, WEIGHTS_COUNT));

		// Define engine that takes in the utility function
		Engine<DoubleGene, Double> engine = Engine
				.builder(this::getFitness, gtf)
				.build();

		// Train!
		Genotype<DoubleGene> result = engine.stream()
				.limit(10)
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
