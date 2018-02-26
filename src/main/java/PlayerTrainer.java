import java.util.Arrays;

public class PlayerTrainer {

	public PlayerTrainer() {
	}

	public static void main(String[] args) {
		GameStateUtilityLearner learner = new GameStateUtilityLearner(
			10, 50, 0.5f
		);
		double[] weights = learner.train();
		System.out.println("Best utility func weights: " + Arrays.toString(weights));
	}
}
 
