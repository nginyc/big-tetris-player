import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class SimulatedAnnealingLearner {

    private double[] currentIndividual;
    private double bestEnergy;
    private double[] bestIndividual;
    private double temp;

    private ExecutorService executor;
    public static int MAX_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    // Learning hyperparameters
    private Function<double[], Double> fitnessFunction;
    private int weightsCount;
    private int noOfTriesPerIndividual;
    private double initialTemp;
    private double coolingRate;

    public SimulatedAnnealingLearner(Function<double[], Double> fitnessFunction,
                                     int weightsCount, int noOfTriesPerIndividual, double initialTemp, double coolingRate) {
        this.fitnessFunction = fitnessFunction;
        this.weightsCount = weightsCount;
        this.noOfTriesPerIndividual = noOfTriesPerIndividual;
        this.initialTemp = initialTemp;
        this.temp = initialTemp;
        this.coolingRate = coolingRate;

        this.executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
    }

    private double getEnergy(double[] weights) {
            Object futureFitness = this.executor.submit(() -> {
                double fitness = this.fitnessFunction.apply(weights);
                return fitness;
            });
        try {
            Future<Double> future = (Future<Double>)futureFitness;
            double fitness = future.get();
            return fitness;
        } catch (ExecutionException error) {
            throw new Error("Execution exception reached: " + error.getMessage());
        } catch (InterruptedException error) {
            throw new Error("Interrupted exception reached: " + error.getMessage());
        }
    }

    private double doGaussianMutation(double x, double min, double max) {
        NormalDistribution dist = new NormalDistribution(x, (max - min) / 100);
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
            System.out.println("currEnergy is: " + currentEnergy);
            System.out.println("neighbourEnergy is: " + neighbourEnergy);
            System.out.println("acceptanceProb is: " + acceptanceProb);

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