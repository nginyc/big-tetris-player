import java.util.Arrays;

public class PlayerTrainer {

	public PlayerTrainer() {
	}

	public static void main(String[] args) {
		GameStateUtilityLearner learner = new GameStateUtilityLearner(
			10, 10, 100, 0.05
		);

		double[] weights = learner.train(new double[] { 0.4196536509730699, 0.48691656577614023, 0.4990965922288645, 0.13582579792757876 });
		System.out.println("Best utility func weights: " + Arrays.toString(weights));
	}
}