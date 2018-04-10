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
                    //	int weightsCount, int swarmSize, int maxStallIterations, double inertiaRatio, double selfAdjustmentWeight,
                    //		double socialAdjustmentWeight
                }, this.weightIndices.length, 200, 20, 0.5, 0.6, 0.9
            );
            
            double[] weights = learner.train(new double[] {-0.3907454899138355, -0.7076471959178626, -0.9999999978876627, 0.34814812107690407, 0.005918216014713197, -0.506446236591784, 0.010013842808179697, 0.00000000304563703249});
            //double[] weights = learner.train();
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


    public static class LearnerNWeightsSeeded implements LearnerEvaluator.Learner {
        private int[] weightIndices;
        private double[][] seedWeightSets;

        public LearnerNWeightsSeeded(int[] weightIndices, double[][] seedWeightSets) {
            this.weightIndices = weightIndices;
            this.seedWeightSets = seedWeightSets;
        }

        @Override
        public IGameStateUtilityFunction train(int rows) {
            GeneticAlgorithmLearner learner = new GeneticAlgorithmLearner(
                    (weights) -> {
                        return evaluator.evaluate(10, rows, toUtilityFunction(weights, this.weightIndices));
                    }, this.weightIndices.length, 50, 100, 0.1, 0.8, 2, 1, 0.01, 10
            );

            double[] weights = learner.train(this.seedWeightSets);
            return toUtilityFunction(weights, this.weightIndices);
        }
    }

    public static class LearnerNWeightsSimulatedAnnealing implements LearnerEvaluator.Learner {
        private int[] weightIndices;

        public LearnerNWeightsSimulatedAnnealing(int[] weightIndices) {
            this.weightIndices = weightIndices;
        }

        @Override
        public IGameStateUtilityFunction train(int rows) {
            SimulatedAnnealingLearner learner = new SimulatedAnnealingLearner(
                    (weights) -> {
                        return evaluator.evaluate(10, rows, toUtilityFunction(weights, this.weightIndices));
                    }, this.weightIndices.length, 10, 100000, 0.003
            );
            //fitness function, int weightsCount, int noOfTriesPerIndividual, int initialTemp, int coolingRate)

            double[] weights = learner.train(new double[] {-0.3907454899138355, -0.7076471959178626, -0.9999999978876627, 0.34814812107690407, 0.005918216014713197, -0.506446236591784, 0.010013842808179697, 0.00000000304563703249});
            //return toUtilityFunction(weights, this.weightIndices);
            return null;
        }

        @Override
        public String toString() {
            return this.getClass() + " with weight indices " + Arrays.toString(this.weightIndices);
        }
    }
}