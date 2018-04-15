public class PlayerSkeleton {

	private GameStateSearcher gameStateSearcher;
	private GameState gameState;
	private IGameStateUtilityFunction utilityFunction;
	private static int ROWS = State.ROWS - 1;

	public PlayerSkeleton() {
		this.gameState = new GameState(ROWS);
		this.utilityFunction = Learners.toUtilityFunction(
			new double[] { -0.3907454899138355, -0.7076471959178626, -0.9999999978876627, 0.34814812107690407, 0.005918216014713197, -0.506446236591784, 0.010013842808179697, 0.00000000304563703249 }, 
			new int[] { 0, 4, 5, 10, 12, 11, 2, 7 }
		);
		this.gameStateSearcher = new GameStateSearcher(ROWS, utilityFunction);
	}

	//implement this function to have a working system
	public int[] pickMove(State s) {
		// Assumption: If `s`'s turn no. is 0, the game has restarted
		if (s.getTurnNumber() == 0) {
			this.gameState = new GameState(ROWS);
		}
		this.gameState.setNextPiece(s.getNextPiece());
		int[] move = this.gameStateSearcher.search(this.gameState);
		// Assumption: `s` will also make the returned move 
		this.gameState.makePlayerMove(move[0], move[1]);
		return move;
	}

	public static void main(String[] args) {
		State s = new State();
		// new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();

		while(!s.hasLost()) {
			int[] move = p.pickMove(s);
			s.makeMove(move[State.ORIENT], move[State.SLOT]);
			// s.draw();
			// s.drawNext(0,0);
			// try {
			// 	Thread.sleep(10);
			// } catch (InterruptedException e) {
			// 	e.printStackTrace();
			// }
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
}
