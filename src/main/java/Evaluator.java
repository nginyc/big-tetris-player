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

        double totalRowsCleared = 0;
        for (int j = 0; j < noOfTries; j ++) {
            try {
                Future<Integer> future = (Future<Integer>)futures[j];
                int rowsCleared = future.get();
                System.out.println("Rows cleared: " + rowsCleared);
                totalRowsCleared += rowsCleared;
			} catch (ExecutionException error) {
				throw new Error("Execution exception reached: " + error.getMessage());
			} catch (InterruptedException error) {
				throw new Error("Interrupted exception reached: " + error.getMessage());
            }
        }

        double averageRowsCleared = totalRowsCleared / noOfTries;
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
        long initialTime = System.currentTimeMillis();
        IGameStateUtilityFunction[] utilityFunctions = new IGameStateUtilityFunction[] {
            Learners.toUtilityFunction(
                new double[] { -0.3907454899138355, -0.7076471959178626, -0.9999999978876627, 0.34814812107690407, 0.005918216014713197, -0.506446236591784, 0.010013842808179697, 0.00000000304563703249 }, 
                new int[] { 0, 4, 5, 10, 12, 11, 2, 7 }
            )
        };

        Evaluator evaluator = new Evaluator();

        for (int i = 0; i < utilityFunctions.length; i ++) {
            IGameStateUtilityFunction utilityFunction = utilityFunctions[i];
            System.out.println("Testing utility function " + i + " " + utilityFunction.toString() + "...");
            double averageRowsCleared = evaluator.evaluate(utilityFunction);
            System.out.println("Candidate " + i + " clears average of " + averageRowsCleared + " rows over " + NO_OF_TRIES + " tries");
		}

        System.out.println("Time taken: " + (System.currentTimeMillis() - initialTime)  + "ms");
        System.exit(0);
	}
}