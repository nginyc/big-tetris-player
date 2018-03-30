import java.util.*;

// Given a game state and a utility function, returns best move
public class GameStateSearcher {

  private IGameStateUtilityFunction utilityFunction;

	public GameStateSearcher(IGameStateUtilityFunction utilityFunction) {
    this.utilityFunction = utilityFunction;
  }

  public static class BestMoveResult {
    public int[] move;
    public double moveUtil; 
    private BestMoveResult(int[] move, double moveUtil) {
      this.move = move;
      this.moveUtil = moveUtil;
    }
  }

  public BestMoveResult searchNLevelsDFS(GameState gameState, int searchDepth) {
    // A state here is defined as a { board, nextPiece != -1 }
    if(searchDepth <= 1) {
      // Leaf node found. The util of this state = util of the board state after the best possible move
      int[][] legalMoves = gameState.getLegalPlayerMoves();
      int[] bestMove = null;
      double bestMoveUtil = -Double.MAX_VALUE;
      for (int[] legalMove : legalMoves) {
        int orient = legalMove[GameState.ORIENT];
        int slot = legalMove[GameState.SLOT];
        GameState nextGameState = gameState.clone();
        nextGameState.makePlayerMove(orient, slot);
        double moveUtil = utilityFunction.get(nextGameState);
        // System.out.println(String.format("Considering move %s resulting in util %f...", Arrays.toString(legalMove), moveUtil));
        // System.out.println(nextGameState + "\n");
        if (moveUtil >= bestMoveUtil) {
          bestMove = legalMove;
          bestMoveUtil = moveUtil;
        }
      }

      return new BestMoveResult(bestMove, bestMoveUtil);
    } else {

      int[][] legalMoves = gameState.getLegalPlayerMoves();
      int[] bestMove = null;
      
      double bestMoveUtil = -Double.MAX_VALUE;
      for (int[] legalMove : legalMoves) {
        int orient = legalMove[GameState.ORIENT];
        int slot = legalMove[GameState.SLOT];
        GameState nextGameState = gameState.clone();
        nextGameState.makePlayerMove(orient, slot);
        double moveUtil = 0;
        for(int i = 0; i < GameState.N_PIECES; i++) {
          GameState nextPredictGameState = nextGameState.clone();
          nextPredictGameState.setNextPiece(i);
          BestMoveResult result = searchNLevelsDFS(nextPredictGameState, searchDepth - 1);
          moveUtil += result.moveUtil / GameState.N_PIECES;
        }

        if (moveUtil >= bestMoveUtil) {
          bestMove = legalMove;
          bestMoveUtil = moveUtil;
        }
      }
      return new BestMoveResult(bestMove, bestMoveUtil);
    }
  }
}
