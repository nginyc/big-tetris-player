import java.util.Arrays;

public class PlayerTrainer {

	public static int CANDIDATE_COUNT = 25;

	public PlayerTrainer() {
	}

	public static void main(String[] args) {

		double[][] candidates = new double[CANDIDATE_COUNT][];

		// Idea: focus on exploration first
		// Low computation time
		// Converge quickly to a local maxima
		GameStateUtilityLearner explorer = new GameStateUtilityLearner(
			3, 25, 20, 0.1, 0.8, 0.5
		);
		for (int i = 0; i < CANDIDATE_COUNT; i ++) {
			candidates[i] = explorer.train();
			System.out.println("Candidate " + i + ": " + Arrays.toString(candidates[i]));
		}

		System.out.println("Validating candidates...");

		// Idea: focus on exploitation
		// High computation time to thoroughly validate correctness 
		// More crossovers between multiple viable parents
		// Lower selection pressure to allow multiple viable parents
		GameStateUtilityLearner validator = new GameStateUtilityLearner(
			10, 100, 100, 0.05, 0.8, 0.1
		);

		double[] weights = validator.train(candidates);

		System.out.println("Best utility func weights: " + Arrays.toString(weights));
	}
}