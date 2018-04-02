
public class LearnerEvaluator {

    public static int ROWS = 10;
    public static int NO_OF_TRIES = 100;
    public static int MAX_LEARNING_MS = 1000 * 60 * 60; // 60 min

    public interface Learner {
        IGameStateUtilityFunction train(int rows);
    }

	public LearnerEvaluator() {
    }
    
    public double evaluate(Learner learner) {
        int learningMs = 0;
        
        Evaluator evaluator = new Evaluator();

        double bestAvgRowsCleared = -Double.MAX_VALUE;
        while (learningMs <= MAX_LEARNING_MS) {
            System.out.println("Miliseconds spent learning: " + learningMs);
            long beforeMs = System.currentTimeMillis();
            IGameStateUtilityFunction utilityFunction = learner.train(ROWS);
            learningMs += (System.currentTimeMillis() - beforeMs);
            double rowsCleared = evaluator.evaluate(NO_OF_TRIES, ROWS, utilityFunction);
            System.out.println("Average rows cleared for " + ROWS + "-row game for this round of learning: " + rowsCleared);
            bestAvgRowsCleared = Math.max(bestAvgRowsCleared, rowsCleared);
        }

        return bestAvgRowsCleared;
    }

	public static void main(String[] args) {
        Learner[] testLearners = new Learner[] {
            new Learners.LearnerNWeightsHillClimb(new int[] { 0, 4, 5, 10, 12, 11, 2, 7 }),
        };

        LearnerEvaluator learnerEvaluator = new LearnerEvaluator();

        for (Learner learner : testLearners) {
            System.out.println("Testing " + learner + "...");
            double bestAvgRowsCleared = learnerEvaluator.evaluate(learner);
            System.out.println("Best average rows cleared for " + ROWS + "-row game: " + bestAvgRowsCleared);
        }
    }
}