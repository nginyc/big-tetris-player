import java.util.Arrays;

public class PlayerTrainer {

	public PlayerTrainer() {
	}

	public static void main(String[] args) {
		// TODO: GA stops on converge
		// TODO: Hill climbing on best performing indiv
		GameStateUtilityLearner learner = new GameStateUtilityLearner(
			20, 15, 10, 100, 100, 0.1, 0.8, 0.5
		);
		double[] weights = learner.train();
		System.out.println("Weights: " + Arrays.toString(weights));

		Evaluator evaluator = new Evaluator();
		double rowsCleared = evaluator.evaluate(weights);
		System.out.println("Rows cleared: " + rowsCleared);
	}
}