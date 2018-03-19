import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// Given a game state and a utility function, returns best move
public class GameStateSearcher {

  public static int MAX_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
  
	private Object[] legalMovesTaskFutures;
  private ExecutorService executor;
  
  private IGameStateUtilityFunction utilityFunction;

	public GameStateSearcher(IGameStateUtilityFunction utilityFunction) {
    this.utilityFunction = utilityFunction;
    this.legalMovesTaskFutures = new Object[GameState.COLS * 4]; 
  }

  // Search for best move 2-depth
  public int[] search(GameState gameState) {
		this.executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
    int[][] legalMoves = gameState.getLegalPlayerMoves();

    for (int i = 0; i < legalMoves.length; i ++) {
      final int[] legalMove = legalMoves[i];
      this.legalMovesTaskFutures[i] = this.executor.submit(() -> {
        // Calculate move util
        int orient = legalMove[GameState.ORIENT];
        int slot = legalMove[GameState.SLOT];
        GameState nextGameState = gameState.clone();
        nextGameState.makePlayerMove(orient, slot);
        double moveUtil = utilityFunction.get(nextGameState);
				return moveUtil;
			});
    }

    int[] bestMove = null;
    double bestMoveUtil = -Double.MAX_VALUE;
    for (int i = 0; i < legalMoves.length; i ++) {
      int[] legalMove = legalMoves[i];
      try {
				Future<Double> future = (Future<Double>)this.legalMovesTaskFutures[i];
				double moveUtil = future.get();
        if (moveUtil >= bestMoveUtil) {
          bestMove = legalMove;
          bestMoveUtil = moveUtil;
        }
			} catch (ExecutionException error) {
				throw new Error("Execution exception reached: " + error.getMessage());
			} catch (InterruptedException error) {
				throw new Error("Interrupted exception reached: " + error.getMessage());
			}
    }

    this.executor.shutdown();

    return bestMove;
  }
}
