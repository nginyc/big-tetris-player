import java.util.Arrays;

public class PlayerTrainer {

	public PlayerTrainer() {
	}

	public static void main(String[] args) {
		while (true) {
			trainGaThenSa(16);	
		}	
	}

	private static void trainGa(int rows) {
		GameStateUtilityLearner learner = new GameStateUtilityLearner(
			rows, 15, 10, 100, 100, 0.1, 0.8, 0.5
		);
		double[] weights = learner.train();
		System.out.println("Weights found: " + Arrays.toString(weights));
		
		Evaluator evaluator = new Evaluator();
		double rowsCleared = evaluator.evaluate(weights);
		System.out.println("Rows cleared: " + rowsCleared);
	}

	private static void trainGaThenSa(int rows) {
		GameStateUtilityLearner learner = new GameStateUtilityLearner(
			rows, 15, 10, 100, 100, 0.1, 0.8, 0.5
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