import java.util.Arrays;

public class PlayerTrainer {

	public PlayerTrainer() {
	}

	public static void main(String[] args) {
		GameStateUtilityLearner learner = new GameStateUtilityLearner(100, 10);
		double[] weights = learner.train();
		System.out.println("Best weights: " + Arrays.toString(weights));
	}
}
 