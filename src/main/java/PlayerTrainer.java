public class PlayerTrainer {
	public PlayerTrainer() {
	}

	public static void main(String[] args) {
		// TODO: Try other selection & mutation methods 
		// TODO: Try PSO
		while (true) {
			LearnerEvaluator.Learner learner = new Learners.LearnerNWeightsSeeded(
				new int[] { 0, 4, 5, 10, 12, 11, 2, 7 },
				new double[][] {
					new double[] { -0.3907454916830745, -0.811413075782089, -1.0, 0.3531714849383103, 0.0059182152605956875, -0.5170695909853636, 0, 0 },
					new double[] { -0.5492628540570825, -1.0, -0.9999921480184876, 0.4833128587233925, 0.00987285705521266, -0.7972235992021071, 0, 0 },
					new double[] { -0.18058087213646176, -1.0, -0.9859493187639575, 0.15454976336975096, 0.009062094933315022, -0.4302022682880107, -0.2921079786301265, 0.008874001359701303 },
					new double[] { -0.3907454899138355, -0.7076471959178626, -0.9999999978876627, 0.34814812107690407, 0.005918216014713197, -0.506446236591784, 0.010013842808179697, 0.00000000304563703249 }
				}
			);
			IGameStateUtilityFunction utilityFunction = learner.train(20);
			Evaluator evaluator = new Evaluator();
			double rowsCleared = evaluator.evaluate(utilityFunction);
			System.out.println("Rows cleared: " + rowsCleared);
		}
		
	}
}