import java.util.*;

import org.apache.commons.math3.*;
import org.apache.commons.math3.distribution.NormalDistribution;

public class GameStateUtilityLearner {

	private double[][] population;
	private double[] individualsFitness;
	private HashSet<Integer> individualsSelected;
	private ArrayList<Integer> individualsSelectedList;
	private double totalFitness = 0;

	private int noOfTriesPerIndividual;
	private int noOfGenerations;
	private int populationSize; 
	private double mutationProb;

	public static int WEIGHTS_COUNT = 4;

	public GameStateUtilityLearner(int noOfTriesPerIndividual, int noOfGenerations,
		int populationSize, double mutationProb) {
		this.noOfTriesPerIndividual = noOfTriesPerIndividual;
		this.noOfGenerations = noOfGenerations;
		this.populationSize = populationSize;
		this.mutationProb = mutationProb;

		this.population = new double[this.populationSize][WEIGHTS_COUNT];
		this.individualsFitness = new double[this.populationSize];
		this.individualsSelected = new HashSet<>(this.populationSize);
		this.individualsSelectedList = new ArrayList<>(this.populationSize);
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

	private void initPopulation(double[] initialWeights) {
		// Randomly initialize each individual based on initial weights
		for (int i = 0; i < this.populationSize; i ++) {
			for (int j = 0; j < WEIGHTS_COUNT; j ++) {
				double w = initialWeights[j];
				// Do gaussian mutation on every weight
				this.population[i][j] = this.doGaussianMutation(w, 0f, 1f);
			}
		}
	}
	
	private void doRouletteWheelSelection() {
		this.individualsSelected.clear();
		this.individualsSelectedList.clear();
		for (int i = 0; i < this.populationSize; i ++) {
			double fitness = this.individualsFitness[i];
			if (Math.random() < fitness / totalFitness) {
				this.individualsSelected.add(i);
				this.individualsSelectedList.add(i);
			} 
		}
	}

	private void evaluatePopulationFitness() {
		for (int i = 0; i < this.populationSize; i ++) {
			this.individualsFitness[i] = this.getFitness(this.population[i]);
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

				double p1Fitness = this.getFitness(p1);
				double p2Fitness = this.getFitness(p2);
				double ratio = p1Fitness / (p1Fitness + p2Fitness);
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
					this.population[i][j] = Math.random();
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
		return train(weights);
	}

	public double[] train(double[] initialWeights) {
		this.initPopulation(initialWeights);

		for (int g = 0; g < this.noOfGenerations; g ++) {
			this.evaluatePopulationFitness();
			this.doRouletteWheelSelection();
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
		int bestIndividualIndex = this.getBestIndividualIndex();
		double fitness = this.individualsFitness[bestIndividualIndex];
		double[] bestIndividual = this.population[bestIndividualIndex];
		System.out.println("Best phenotype of generation " + generation +
			" has fitness " + fitness + " with weights " + Arrays.toString(bestIndividual));
	}
}
