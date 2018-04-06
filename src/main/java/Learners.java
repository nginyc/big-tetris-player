import java.util.Arrays;

public class Learners {
    public static Evaluator evaluator = new Evaluator();

    public static IGameStateUtilityFunction toUtilityFunction(double[] weights, int[] weightIndices) {
        double[] evalWeights = new double[15];
        Arrays.fill(evalWeights, 0);
        for (int i = 0; i < weightIndices.length; i ++) {
            evalWeights[weightIndices[i]] = weights[i];
        }
        GameStateUtilityFunction utilityFunction = new GameStateUtilityFunction(evalWeights);
        return utilityFunction;
    }

    public static class LearnerPsoNWeights implements LearnerEvaluator.Learner {
        private int[] weightIndices;

        public LearnerPsoNWeights(int[] weightIndices) {
            this.weightIndices = weightIndices;
        }

		@Override
		public IGameStateUtilityFunction train(int rows) {
			ParticleSwarmLearner learner = new ParticleSwarmLearner(
                (weights) -> {
                    return evaluator.evaluate(10, rows, toUtilityFunction(weights, this.weightIndices));
                }, this.weightIndices.length, 10, 20, 0.8, 1.5, 1.5
            );
            
            double[] weights = learner.train();
            return toUtilityFunction(weights, this.weightIndices);
        }
        
        @Override
        public String toString() {
            return this.getClass() + " with weight indices " + Arrays.toString(this.weightIndices);
        }
    }

    public static class LearnerNWeights implements LearnerEvaluator.Learner {
        private int[] weightIndices;

        public LearnerNWeights(int[] weightIndices) {
            this.weightIndices = weightIndices;
        }

		@Override
		public IGameStateUtilityFunction train(int rows) {
			GeneticAlgorithmLearner learner = new GeneticAlgorithmLearner(
                (weights) -> {
                    return evaluator.evaluate(10, rows, toUtilityFunction(weights, this.weightIndices));
                }, this.weightIndices.length, 50, 100, 0.1, 0.8, 2, 0.1, 0, 0
            );
            
            double[] weights = learner.train();
            return toUtilityFunction(weights, this.weightIndices);
        }
        
        @Override
        public String toString() {
            return this.getClass() + " with weight indices " + Arrays.toString(this.weightIndices);
        }
    }

    public static class LearnerNWeightsHillClimb implements LearnerEvaluator.Learner {
        private int[] weightIndices;

        public LearnerNWeightsHillClimb(int[] weightIndices) {
            this.weightIndices = weightIndices;
        }

		@Override
		public IGameStateUtilityFunction train(int rows) {
			GeneticAlgorithmLearner learner = new GeneticAlgorithmLearner(
                (weights) -> {
                    return evaluator.evaluate(10, rows, toUtilityFunction(weights, this.weightIndices));
                }, this.weightIndices.length, 25, 100, 0.1, 0.8, 2, 0.1, 0.01, 10
            );
            
            double[] weights = learner.train();
            return toUtilityFunction(weights, this.weightIndices);
        }
        
        @Override
        public String toString() {
            return this.getClass() + " with weight indices " + Arrays.toString(this.weightIndices);
        }
    }

    public static class LearnerNWeightsHillClimbRisingMutation implements LearnerEvaluator.Learner {
        private int[] weightIndices;

        public LearnerNWeightsHillClimbRisingMutation(int[] weightIndices) {
            this.weightIndices = weightIndices;
        }

		@Override
		public IGameStateUtilityFunction train(int rows) {
			GeneticAlgorithmLearner learner = new GeneticAlgorithmLearner(
                (weights) -> {
                    return evaluator.evaluate(10, rows, toUtilityFunction(weights, this.weightIndices));
                }, this.weightIndices.length, 25, 100, 0.1, 0.8, 2, 0.5, 0.01, 10
            );
            
            double[] weights = learner.train();
            return toUtilityFunction(weights, this.weightIndices);
        }
        
        @Override
        public String toString() {
            return this.getClass() + " with weight indices " + Arrays.toString(this.weightIndices);
        }
    }


    public static class LearnerNWeightsHillClimbBiggerTournament implements LearnerEvaluator.Learner {
        private int[] weightIndices;

        public LearnerNWeightsHillClimbBiggerTournament(int[] weightIndices) {
            this.weightIndices = weightIndices;
        }

		@Override
		public IGameStateUtilityFunction train(int rows) {
			GeneticAlgorithmLearner learner = new GeneticAlgorithmLearner(
                (weights) -> {
                    return evaluator.evaluate(10, rows, toUtilityFunction(weights, this.weightIndices));
                }, this.weightIndices.length, 25, 100, 0.1, 0.8, 3, 0.1, 0.01, 10
            );
            
            double[] weights = learner.train();
            return toUtilityFunction(weights, this.weightIndices);
        }
        
        @Override
        public String toString() {
            return this.getClass() + " with weight indices " + Arrays.toString(this.weightIndices);
        }
    }
}