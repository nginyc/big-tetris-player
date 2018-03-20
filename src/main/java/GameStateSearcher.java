// Given a game state and a utility function, returns best move
public class GameStateSearcher {
  private IGameStateUtilityFunction utilityFunction;

	public GameStateSearcher(IGameStateUtilityFunction utilityFunction) {
    this.utilityFunction = utilityFunction;
  }

  public int[] search(GameState gameState) {
    int[][] legalMoves = gameState.getLegalPlayerMoves();

    int[] bestMove = null;
    double bestMoveUtil = -Double.MAX_VALUE;

    for (int i = 0; i < legalMoves.length; i ++) {
      int[] legalMove = legalMoves[i];
      // Calculate move util
      int orient = legalMove[GameState.ORIENT];
      int slot = legalMove[GameState.SLOT];
      GameState nextGameState = gameState.clone();
      nextGameState.makePlayerMove(orient, slot);
      double moveUtil = utilityFunction.get(nextGameState);
      if (bestMove == null || moveUtil >= bestMoveUtil) {
        bestMove = legalMove;
        bestMoveUtil = moveUtil;
      }
    }

    return bestMove;
  }
}
