import java.util.*;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;

public class GameStateUtilityLearner {

	public static int CUTOFF_STEADY_FITNESS_GENERATIONS = 100; 
	private int noOfTriesPerIndividual;
	private int populationSize; 
	private float mutationProb;
	private int tournamentSampleSize;
	private float survivorsFraction;
	private float meanAltererProb;
	private float crossoverProb;

	public static int WEIGHTS_COUNT = 9;

	public GameStateUtilityLearner(int noOfTriesPerIndividual, int populationSize, 
		float mutationProb, int tournamentSampleSize,
		float survivorsFraction, float meanAltererProb, float crossoverProb) {
		this.noOfTriesPerIndividual = noOfTriesPerIndividual;
		this.populationSize = populationSize;
		this.mutationProb = mutationProb;
		this.tournamentSampleSize = tournamentSampleSize;
		this.survivorsFraction = survivorsFraction;
		this.meanAltererProb = meanAltererProb;
		this.crossoverProb = crossoverProb;
	}

	private double getFitness(Genotype<DoubleGene> gt) {
		double[] weights = this.genotypeToWeights(gt);

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
				int[] move = gameStateSearcher.getBestMove(gameState);
				gameState.makePlayerMove(move[GameState.ORIENT], move[GameState.SLOT]);
			}
			rowsCleared += gameState.getRowsCleared(); 
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
				.survivorsSelector(
					new TournamentSelector<>(this.tournamentSampleSize)
				)
				.survivorsFraction(this.survivorsFraction)
				.alterers(
					new UniformCrossover(this.crossoverProb),
					new MeanAlterer<>(this.meanAltererProb),
					new Mutator<>(this.mutationProb)
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
