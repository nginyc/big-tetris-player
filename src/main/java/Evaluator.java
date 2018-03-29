import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Evaluator {

    public static int NO_OF_TRIES = 100;
    public static int ROWS = State.ROWS - 1;
	public static int MAX_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private ExecutorService executor;

	public Evaluator() {
		this.executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
    }

    public double evaluate(double[] weights) {
        IGameStateUtilityFunction utilityFunction = new GameStateUtilityFunction(weights);
        return this.evaluate(utilityFunction);
    }

    public double evaluate(IGameStateUtilityFunction utilityFunction) {
        return this.evaluate(NO_OF_TRIES, ROWS, utilityFunction);
    }

    public double evaluate(int noOfTries, final int rows, IGameStateUtilityFunction utilityFunction) {
        Object[] futures = new Object[noOfTries];

        for (int j = 0; j < noOfTries; j ++) {
            futures[j] = this.executor.submit(() -> {
                return this.getRowsCleared(rows, utilityFunction);
            });
        }

        int totalRowsCleared = 0;
        for (int j = 0; j < noOfTries; j ++) {
            try {
                Future<Integer> future = (Future<Integer>)futures[j];
                int rowsCleared = future.get();
                totalRowsCleared += rowsCleared;
			} catch (ExecutionException error) {
				throw new Error("Execution exception reached: " + error.getMessage());
			} catch (InterruptedException error) {
				throw new Error("Interrupted exception reached: " + error.getMessage());
            }
        }

        double averageRowsCleared = (double)totalRowsCleared / noOfTries;
        return averageRowsCleared;
    }

    private int getRowsCleared(int rows, IGameStateUtilityFunction utilityFunction) {
        final GameStateSearcher gameStateSearcher = new GameStateSearcher(rows, utilityFunction);
        GameState gameState = new GameState(rows);
        while(gameState.hasPlayerLost() == 0) {
            // Randomly get a piece
            int nextPiece = gameState.getRandomNextPiece();
            gameState.setNextPiece(nextPiece);
            int[] move = gameStateSearcher.search(gameState);
            gameState.makePlayerMove(move[GameState.ORIENT], move[GameState.SLOT]);
        }
        return gameState.getRowsCleared(); 
    }

	public static void main(String[] args) {
        double[][] candidates = new double[][] {
            new double[] { -0.7827327260183714, -0.11334336727622543, -0.04677414857202162, 0.053388888547202154, -0.46329670624437014, -0.05007488959693253, -0.08437679573888653, -0.7023611620823788, -0.45828420372251777, 0.026138258237249294, 0.05012811601766011, 0.2928197003358416, 0.09266091906516735, 0.1976357986587708, -0.04490329796781195 },
            new double[] { -0.10535323935941053, 0.0012378990514839156, -0.11976135881490828, 0.0009199426483574079, -0.08823390729195249, -1, -0.0034571639142464347, -0.07006317338464395, -0.0389316396907097, -0.006601781575429543, 0.08778061719042551, 0.07347776609114921, 0.006118235517837187, -0.0027428340856810664, -0.0042475024771130515 },
        };

        Evaluator evaluator = new Evaluator();

        for (int i = 0; i < candidates.length; i ++) {
            System.out.println("Testing candidate " + i + " " + Arrays.toString(candidates[i]) + "...");
            double averageRowsCleared = evaluator.evaluate(candidates[i]);
            System.out.println("Candidate " + i + " clears average of " + averageRowsCleared + " rows over " + NO_OF_TRIES + " tries");
		}
	}
}