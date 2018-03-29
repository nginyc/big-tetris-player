public class PlayerTrainer {
	public PlayerTrainer() {
	}

	public static void main(String[] args) {
		// TODO: Try other selection & mutation methods 
		// TODO: Try PSO
		while (true) {
			LearnerEvaluator.Learner learner = new Learners.LearnerNWeightsHillClimb(new int[] { 0, 4, 5, 10, 12, 11 });
			IGameStateUtilityFunction utilityFunction = learner.train(20);
			Evaluator evaluator = new Evaluator();
			double rowsCleared = evaluator.evaluate(utilityFunction);
			System.out.println("Rows cleared: " + rowsCleared);
		}
		
	}
}