import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class ParticleSwarmLearner {

	private double[][] particles;
	private double[][] particlesVelocities;
	private double[][] particlesBests;
	private double[] particlesBestsFitness;
	private int bestParticlesBestIndex;
	private double[] particlesFitness;
	private ExecutorService executor;
	private Object[] particleTaskFutures;
	private int stallIterations;
	
	// Learning hyperparameters
	private Function<double[], Double> fitnessFunction;
	private int swarmSize;
	private int weightsCount;
	private double inertiaRatio;
	private double selfAdjustmentWeight;
	private double socialAdjustmentWeight;
	private int maxStallIterations;

	public static int MAX_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

	/**
	 * Weights count = size of vector
	 * Swarm size = no. of particles
	 * Max Stall Iterations = no. of iterations without a new best weight set before termination
	 */
	public ParticleSwarmLearner(Function<double[], Double> fitnessFunction, int weightsCount, 
		int swarmSize, int maxStallIterations, double inertiaRatio, double selfAdjustmentWeight,
		double socialAdjustmentWeight) {
		this.fitnessFunction = fitnessFunction;
		this.weightsCount = weightsCount;
		this.swarmSize = swarmSize;
		this.maxStallIterations = maxStallIterations;
		this.inertiaRatio = inertiaRatio;
		this.selfAdjustmentWeight = selfAdjustmentWeight;
		this.socialAdjustmentWeight = socialAdjustmentWeight;

		this.particles = new double[this.swarmSize][this.weightsCount];
		this.particlesVelocities = new double[this.swarmSize][this.weightsCount];
		this.particlesFitness = new double[this.swarmSize];
		this.particlesBests = new double[this.swarmSize][this.weightsCount];
		this.particlesBestsFitness = new double[this.swarmSize];
		Arrays.fill(this.particlesBestsFitness, -Double.MAX_VALUE); // Start with worst fitnesses
		this.bestParticlesBestIndex = 0;
		this.particleTaskFutures = new Object[this.swarmSize];
		this.stallIterations = 0;
		this.executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
	}

	private void initializeParticles(double[] initialParticles) {
        // Randomly initialize particles' velocities, where each velocity is taken from U(-1, 1)
        for (int i = 0; i < this.swarmSize; i ++) {
            double[] particle = this.particles[i];
            double[] particleVelocity = this.particlesVelocities[i];
            for (int w = 0; w < this.weightsCount; w ++) {
                if (initialParticles.length < 1) {
                    // Randomly initialize particles, where each weight is taken from U(-1, 1)
                    particle[w] = Math.random() * 2 - 1;
                } else {
                    // Don't random particle, use good weights already
                    particle[w] = initialParticles[w];
                }
                particleVelocity[w] = (Math.random() * 2 - 1) * 0.1;
            }
        }
	}
	private void initializeParticles() {
	    initializeParticles(new double[] {});
	}

	private void evaluateParticlesFitness() {
		for (int i = 0; i < this.swarmSize; i ++) {
			final double[] particle = this.particles[i];
			this.particleTaskFutures[i] = this.executor.submit(() -> {
				double fitness = this.fitnessFunction.apply(particle);
				return fitness;
			});
		}

		for (int i = 0; i < this.swarmSize; i ++) {
			try {
				Future<Double> future = (Future<Double>)this.particleTaskFutures[i];
				double fitness = future.get();
				this.particlesFitness[i] = fitness;
			} catch (ExecutionException error) {
				throw new Error("Execution exception reached: " + error.getMessage());
			} catch (InterruptedException error) {
				throw new Error("Interrupted exception reached: " + error.getMessage());
			}
		}
	}

	private void updateParticlesBests() {
		// For each particle, check and update against its personal and global best
		boolean hasNewBest = false;
		for (int i = 0; i < this.swarmSize; i ++) {
			double bestFitness = this.particlesBestsFitness[i];
			double currentFitness = this.particlesFitness[i];
			if (currentFitness >= bestFitness) {

				this.particlesBestsFitness[i] = currentFitness;
				for (int w = 0; w < this.weightsCount; w++) {
                    this.particlesBests[i][w] = this.particles[i][w];
				}

				double bestParticlesBestsFitness = this.particlesBestsFitness[bestParticlesBestIndex];
				if (currentFitness >= bestParticlesBestsFitness) {
					this.bestParticlesBestIndex = i;
					hasNewBest = true;
				}
			}
		}

		// Reset or increment stall iterations depending on whether there was a new global best
		if (hasNewBest) {
			this.stallIterations = 0;
		} else {
			this.stallIterations ++;
		}
	}

	private void moveParticles() {
		double[] bestParticlesBest = this.particlesBests[this.bestParticlesBestIndex];
		for (int i = 0; i < this.swarmSize; i ++) {
			double[] particle = this.particles[i];
			double[] particleVelocity = this.particlesVelocities[i];
			double[] particlesBest =  this.particlesBests[i];
			double rand1 = (Math.random()) * 0.5;
			double rand2 = (Math.random()) * 0.5;
			for (int w = 0; w < this.weightsCount; w ++) {
                this.particlesVelocities[i][w] = (this.inertiaRatio * this.particlesVelocities[i][w]) +
                        (this.selfAdjustmentWeight * rand1 * (particlesBest[w] - this.particles[i][w])) +
                        (this.socialAdjustmentWeight * rand2 * (bestParticlesBest[w] - this.particles[i][w]));
				if (Math.abs(this.particlesVelocities[i][w]) < 0.00001) {
                    // Velocity too low
                    this.particlesVelocities[i][w] = Math.signum(this.particlesVelocities[i][w]) * 0.00001;
                } else if (Math.abs(this.particlesVelocities[i][w]) > 0.1) {
				    // Velocity too high
                    this.particlesVelocities[i][w] = Math.signum(this.particlesVelocities[i][w]) * 0.1;
                }
                this.particles[i][w] = Math.max(-1, Math.min(this.particles[i][w] + this.particlesVelocities[i][w], 1));
			}
		}
	}

	public double[] train(double[] initialParticles) {
        int iteration = 0;
        this.initializeParticles(initialParticles);
        this.prettyPrintIteration(iteration);
        while (this.stallIterations < maxStallIterations) {
            this.evaluateParticlesFitness();
            this.updateParticlesBests();
            this.moveParticles();
            iteration ++;
            this.prettyPrintIteration(iteration);
            if (iteration == 20) {
                swarmSize *= 0.5;
            } else if (iteration == 40) {
                swarmSize *= 0.5;
            }
        }
        this.evaluateParticlesFitness();
        this.updateParticlesBests();
        double[] bestParticlesBest = this.particlesBests[this.bestParticlesBestIndex];
        prettyPrintBestParticle(iteration);
        return bestParticlesBest;
    }
	public double[] train() {
        return train(new double[]{});
	}

	private void prettyPrintBestParticle(int iteration) {
	    System.out.println();
		System.out.println("Iteration " + iteration + ": Best particle is " + this.getParticlePrint(this.bestParticlesBestIndex));
        System.out.println();
	}

	private String getParticlePrint(int particleIndex) {
		return "(Fitness=" + this.particlesFitness[particleIndex] + ")" +
			"(PersonalBestFitness=" + this.particlesBestsFitness[particleIndex] + ")" +
			((this.bestParticlesBestIndex == particleIndex) ? "(BEST)" : "") + 
			"(Weights=" + Arrays.toString(this.particles[particleIndex]) + ")" + 
			"(PersonalBestWeights=" + Arrays.toString(this.particlesBests[particleIndex]) + ")" + 
			"(Velocity=" + Arrays.toString(this.particlesVelocities[particleIndex]) + ")" +
			"(Index=" + particleIndex + ")";
	}

	private void prettyPrintIteration(int iteration) {
		System.out.println("Iteration " + iteration + ":");
		for (int i = 0; i < this.swarmSize; i ++)  {
			System.out.println("\t " + this.getParticlePrint(i));
		}
	}
}
