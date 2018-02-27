import java.util.*;

// Given a game state and a utility function, returns best move
public class GameStateSearcher {

  private IGameStateUtilityFunction utilityFunction;

	public GameStateSearcher(IGameStateUtilityFunction utilityFunction) {
    this.utilityFunction = utilityFunction;
  }

  public int[] getBestMove(GameState startGameState) {
    int[][] legalMoves = startGameState.getLegalPlayerMoves();
    // System.out.println(String.format("Evaluating best move for start game state:"));
    // System.out.println(startGameState);

    // For each possible move, evaluate the resultant game state and return move with highest utility
    int[] bestMove = null;
    double bestMoveUtil = -Float.MAX_VALUE;
    for (int[] legalMove : legalMoves) {
      int orient = legalMove[GameState.ORIENT];
      int slot = legalMove[GameState.SLOT];
      GameState gameState = startGameState.clone();
      gameState.makePlayerMove(orient, slot);
      double moveUtil = utilityFunction.get(gameState);
      // System.out.println(String.format("Considering move %s resulting in util %f...", Arrays.toString(legalMove), moveUtil));
      // System.out.println(gameState);
      if (moveUtil > bestMoveUtil && gameState.hasPlayerLost() != 1) {
        bestMove = legalMove;
        bestMoveUtil = moveUtil;
      }
    }

    // System.out.println(String.format("Best move is %s with util %f", Arrays.toString(bestMove), bestMoveUtil));
    // System.out.println();

    return bestMove;
  }
}
