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
        double moveUtil = nextGameState.makePlayerMove(orient, slot, utilityFunction);
        //double moveUtil = utilityFunction.get(nextGameState);
        if (moveUtil >= bestMoveUtil && nextGameState.hasPlayerLost() != 1) {
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
        nextGameState.makePlayerMove(orient, slot, null);
        double moveUtil = 0;
        for(int i = 0; i < GameState.N_PIECES; i++) {
          GameState nextPredictGameState = nextGameState.clone();
          nextPredictGameState.setNextPiece(i);
          BestMoveResult result = searchNLevelsDFS(nextPredictGameState, searchDepth - 1);
          moveUtil += result.moveUtil / GameState.N_PIECES;
        }

        if (moveUtil >= bestMoveUtil && nextGameState.hasPlayerLost() != 1) {
          bestMove = legalMove;
          bestMoveUtil = moveUtil;
        }
      }
      return new BestMoveResult(bestMove, bestMoveUtil);
    }
  }

  // Single-level search
  public BestMoveResult getBestMove(GameState startGameState) {
    int[][] legalMoves = startGameState.getLegalPlayerMoves();
    // System.out.println(String.format("Evaluating best move for start game state:"));
    // System.out.println(startGameState);

    // For each possible move, evaluate the resultant game state and return move with highest utility
    int[] bestMove = null;
    double bestMoveUtil = -Double.MAX_VALUE;
    for (int[] legalMove : legalMoves) {
      int orient = legalMove[GameState.ORIENT];
      int slot = legalMove[GameState.SLOT];
      GameState gameState = startGameState.clone();
      double moveUtil = gameState.makePlayerMove(orient, slot, utilityFunction);
      //double moveUtil = utilityFunction.get(gameState);
      // System.out.println(String.format("Considering move %s resulting in util %f...", Arrays.toString(legalMove), moveUtil));
      // System.out.println(gameState);
      if (moveUtil >= bestMoveUtil && gameState.hasPlayerLost() != 1) {
        bestMove = legalMove;
        bestMoveUtil = moveUtil;
      }
    }

    // System.out.println(String.format("Best move is %s with util %f", Arrays.toString(bestMove), bestMoveUtil));
    // System.out.println();

    return new BestMoveResult(bestMove, bestMoveUtil);
  }
}
