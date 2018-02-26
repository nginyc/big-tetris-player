import java.util.Arrays;

public class PlayerTrainer {

	public PlayerTrainer() {
	}

	public static void main(String[] args) {
		GameStateUtilityLearner learner = new GameStateUtilityLearner(
			20, 100, 0.25f, 5, 0.75f, 0.05f
		);
		double[] weights = learner.train();
		System.out.println("Best utility func weights: " + Arrays.toString(weights));
	}
}
 
