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
                // System.out.println("Rows cleared: " + rowsCleared);
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
        IGameStateUtilityFunction[] utilityFunctions = new IGameStateUtilityFunction[] {
            Learners.toUtilityFunction(
                new double[] { -0.5492628540570825, -1.0, -0.9999921480184876, 0.4833128587233925, 0.00987285705521266, -0.7972235992021071 }, 
                new int[] { 0, 4, 5, 10, 12, 11 }
            )
        };

        Evaluator evaluator = new Evaluator();

        for (int i = 0; i < utilityFunctions.length; i ++) {
            IGameStateUtilityFunction utilityFunction = utilityFunctions[i];
            System.out.println("Testing utility function " + i + " " + utilityFunction.toString() + "...");
            double averageRowsCleared = evaluator.evaluate(utilityFunction);
            System.out.println("Candidate " + i + " clears average of " + averageRowsCleared + " rows over " + NO_OF_TRIES + " tries");
		}
	}
}