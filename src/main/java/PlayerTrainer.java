import java.util.Arrays;

public class PlayerTrainer {

	public PlayerTrainer() {
	}

	public static void main(String[] args) {
		GameStateUtilityLearner learner = new GameStateUtilityLearner();
		double[] weights = learner.train();
		System.out.println("Best weights: " + Arrays.toString(weights));
	}
}
