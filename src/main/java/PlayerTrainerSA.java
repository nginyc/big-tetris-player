import java.util.Arrays;

public class PlayerTrainerSA {

	public PlayerTrainerSA() {
	}

	public static void main(String[] args) {
		SimulatedAnnealingLearner learner = new SimulatedAnnealingLearner(
			20, 15, 10, 10000, 0.003
		);
        //int rows, int weightsCount, int noOfTriesPerIndividual, int initialTemp, int coolingRate)

        double[] weights = learner.train();
		System.out.println("Weights from SA " + Arrays.toString(weights));

		Evaluator evaluator = new Evaluator();
		double rowsCleared = evaluator.evaluate(weights);
		System.out.println("Rows cleared: " + rowsCleared);
	}
}