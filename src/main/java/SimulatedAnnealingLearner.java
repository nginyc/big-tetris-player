import org.apache.commons.math3.distribution.NormalDistribution;

public class SimulatedAnnealingLearner {

    private double[] currentIndividual;
    private double bestEnergy;
    private double[] bestIndividual;
    private double temp;

    // Learning hyperparameters
    private int rows;
    private int weightsCount;
    private int noOfTriesPerIndividual;
    private double initialTemp;
    private double coolingRate;

    public SimulatedAnnealingLearner(int rows, int weightsCount, int noOfTriesPerIndividual, 
        double initialTemp, double coolingRate) {
        this.rows = rows;
        this.weightsCount = weightsCount;
        this.noOfTriesPerIndividual = noOfTriesPerIndividual;
        this.initialTemp = initialTemp;
        this.temp = initialTemp;
        this.coolingRate = coolingRate;
    }

    private double getEnergy(double[] weights) {
        if (weights.length != this.weightsCount) {
            throw new IllegalStateException();
        }
        GameStateUtilityFunction utilityFunction = new GameStateUtilityFunction(weights);
        GameStateSearcher gameStateSearcher = new GameStateSearcher(this.rows, utilityFunction);

        int rowsCleared = 0;
        for (int i = 0; i < this.noOfTriesPerIndividual; i++) {
            GameState gameState = new GameState(this.rows);
            while (gameState.hasPlayerLost() == 0) {
                // Randomly get a piece
                int nextPiece = gameState.getRandomNextPiece();
                gameState.setNextPiece(nextPiece);
                int[] move = gameStateSearcher.search(gameState);
                gameState.makePlayerMove(move[GameState.ORIENT], move[GameState.SLOT]);
            }
            rowsCleared += gameState.getRowsCleared();
        }

        double energy = (double) rowsCleared / this.noOfTriesPerIndividual;
        return energy;
    }

    private double doGaussianMutation(double x, double min, double max) {
        NormalDistribution dist = new NormalDistribution(x, (max - min) / 10);
        return Math.min(Math.max(-1, dist.sample()), 1);
    }

    private double[] generateNeighbour() {
        double[] neighbour = new double[currentIndividual.length];
        for(int i = 0; i < currentIndividual.length; i++) {
            neighbour[i] = doGaussianMutation(currentIndividual[i], 0, 1);
        }
        return neighbour;
    }

    private void initPopulation(double[] initialWeightsSet) {
        int i = 0;
        currentIndividual = new double[weightsCount];
        for (int w = 0; w < initialWeightsSet.length; w++) {
            currentIndividual[w] = initialWeightsSet[w];
            i++;
        }
        while (i < weightsCount) {
            currentIndividual[i] = (Math.random() - 0.5) * 2;
            i++;
        }
    }

    public static double acceptanceProbability(double energy, double newEnergy, double temperature) {
        // If the new solution is better, accept it
        if (newEnergy > energy) {
            return 1.0;
        }
        // If the new solution is worse, calculate an acceptance probability
        return Math.exp((newEnergy - energy) / temperature);
    }

    public double[] train() {
        return train(new double[] { });
    }

    public double[] train(double[] initialWeights) {
        initPopulation(initialWeights);
        double currentEnergy = getEnergy(currentIndividual);

        // Set as current best
        bestIndividual = currentIndividual;
        bestEnergy = currentEnergy;

        // while system has not cooled
        while(temp > 1) {
            // Create new neighbour weights
            double[] neighbour = generateNeighbour();
            // Get energy of neighbour
            double neighbourEnergy = getEnergy(neighbour);

            double acceptanceProb = acceptanceProbability(currentEnergy, neighbourEnergy, temp);
            //System.out.println("currEnergy is: " + currentEnergy);
            //System.out.println("neighboutEnergy is: " + neighbourEnergy);
            //System.out.println("acceptanceProb is: " + acceptanceProb);

            if (acceptanceProb >= 1.0) {
                // Just accept
                currentEnergy = neighbourEnergy;
                currentIndividual = neighbour;
            } else if (acceptanceProb > Math.random()) {
                currentEnergy = neighbourEnergy;
                currentIndividual = neighbour;
            }

            // Keep track of best weights
            if (currentEnergy > bestEnergy) {
                bestIndividual = currentIndividual;
                bestEnergy = currentEnergy;
            }

            // Cool the system
            temp *= 1 - coolingRate;
        }
        return bestIndividual;
    }
}