import java.util.Arrays;
import java.util.function.Function;

public class PlayerTrainer {

	public PlayerTrainer() {
	}

	public static void main(String[] args) {
		// TODO: Analyse effectiveness of crossover
		// TODO: Analyse change in weights down generations
		// TODO: Try PSO
		// TODO: Try hybrid GA
		while (true) {
			trainGa((weights) -> {
				double rowsCleared = getAverageRowsCleared(20, 10, weights);
				return rowsCleared;
			}, 100, 50);	
		}	
	}

	public static double getAverageRowsCleared(int gameRows, int noOfTriesPerIndividual, double[] weights) {
		GameStateUtilityFunction utilityFunction = new GameStateUtilityFunction(weights);
		GameStateSearcher gameStateSearcher = new GameStateSearcher(gameRows, utilityFunction);

		int rowsCleared = 0;
		for (int i = 0; i < noOfTriesPerIndividual; i ++) {
			GameState gameState = new GameState(gameRows);
			while(gameState.hasPlayerLost() == 0) {
				// Randomly get a piece
				int nextPiece = gameState.getRandomNextPiece();
				gameState.setNextPiece(nextPiece);
				int[] move = gameStateSearcher.search(gameState);
				gameState.makePlayerMove(move[GameState.ORIENT], move[GameState.SLOT]);
			}
			rowsCleared += gameState.getRowsCleared(); 
		}

		double fitness = (double)rowsCleared / noOfTriesPerIndividual;
		return fitness;
	}

	private static void trainGa(Function<double[], Double> fitnessFunction, int noOfGenerations, int populationSize) {
		GeneticAlgorithmLearner learner = new GeneticAlgorithmLearner(
			fitnessFunction, 15, noOfGenerations, populationSize, 0.1, 0.8, 0.5, 0.998
		);
		double[] weights = learner.train();
		System.out.println("Weights found: " + Arrays.toString(weights));
		
		Evaluator evaluator = new Evaluator();
		double rowsCleared = evaluator.evaluate(weights);
		System.out.println("Rows cleared: " + rowsCleared);
	}

	private static void trainGaThenSa(int rows, Function<double[], Double> fitnessFunction) {
		GeneticAlgorithmLearner learner = new GeneticAlgorithmLearner(
			fitnessFunction, 15, 100, 100, 0.1, 0.8, 0.5, 0.999
		);
		double[] weights = learner.train();
		System.out.println("Weights found in GA: " + Arrays.toString(weights));

		SimulatedAnnealingLearner saLearner = new SimulatedAnnealingLearner(rows, 15, 10, 100000, 0.003);
		weights = saLearner.train(weights);
		System.out.println("Weights found after SA: " + Arrays.toString(weights));
		
		Evaluator evaluator = new Evaluator();
		double rowsCleared = evaluator.evaluate(weights);
		System.out.println("Rows cleared: " + rowsCleared);
	}
}