public class PlayerSkeleton {

	private GameStateSearcher gameStateSearcher;
	private GameState gameState;
	private IGameStateUtilityFunction utilityFunction;
	private static int ROWS = State.ROWS - 1;

	public PlayerSkeleton() {
		this.gameState = new GameState(State.ROWS - 1);
		this.utilityFunction = Learners.toUtilityFunction(
			new double[] { -0.18058087213646176, -1.0, -0.9859493187639575, 0.15454976336975096, 0.009062094933315022, -0.4302022682880107, -0.2921079786301265, 0.008874001359701303 }, 
			new int[] { 0, 4, 5, 10, 12, 11, 2, 7 }
		);
		this.gameStateSearcher = new GameStateSearcher(ROWS, utilityFunction);
	}

	//implement this function to have a working system
	public int[] pickMove(State s) {
		this.gameState.setNextPiece(s.getNextPiece());
		// System.out.println("At game state:");
		// System.out.println(this.gameState + "\n");
		int[] move = this.gameStateSearcher.search(this.gameState);
		// Actually make the best move
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
