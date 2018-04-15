// Given a game state and a utility function, returns best move
public class GameStateSearcher {
  private IGameStateUtilityFunction utilityFunction;
  private GameState searchGameState;

	public GameStateSearcher(int rows, IGameStateUtilityFunction utilityFunction) {
    this.utilityFunction = utilityFunction;
    this.searchGameState = new GameState(rows);
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
      this.searchGameState = gameState.clone();
      // this.searchGameState.restore(gameState);
      this.searchGameState.makePlayerMove(orient, slot);
      double moveUtil = utilityFunction.get(this.searchGameState);
      if (bestMove == null || moveUtil >= bestMoveUtil) {
        bestMove = legalMove;
        bestMoveUtil = moveUtil;
      }
    }

    return bestMove;
  }
}
