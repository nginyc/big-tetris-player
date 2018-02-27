
public class PlayerSkeleton {

	private GameStateSearcher gameStateSearcher;
	private GameStateUtilityFunction utilityFunction;

	public PlayerSkeleton(double[] weights) {
		this.utilityFunction = new GameStateUtilityFunction(weights);
		this.gameStateSearcher = new GameStateSearcher(utilityFunction);
	}

	//implement this function to have a working system
	public int[] pickMove(State s) {
		GameState gameState = new GameState(
			this.getBoardField(s), s.getNextPiece(),
			s.lost ? 1 : 0, s.getTurnNumber(), s.getRowsCleared()
		);

		GameStateSearcher.BestMoveResult result = this.gameStateSearcher.searchNLevelsDFS(gameState, 2);
		
		return result.move;
	}

	public static void main(String[] args) {
		State s = new State();
		// new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton(new double[] {
			0.44434135395361885, 0.8117001403213164, 0.047147956300193195, 0.7396052257856431, 0.3493358550139455, 0.3792102040425659, 0.0762387008863219, 0.04941063287026368
		});

		while(!s.hasLost()) {
			int[] move = p.pickMove(s);
			if (move == null) {
				// System.out.println("No move will result in a next state.");
				break;
			}
			s.makeMove(move[0], move[1]);
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
	
	// Make board field compatible
	private int[][] getBoardField(State s) {
		int[][] field = s.getField();
		int[][] fieldNew = new int[GameState.ROWS][GameState.COLS];
		for (int r = 0; r < GameState.ROWS; r ++) {
			for (int c = 0; c < GameState.COLS; c ++) {
				fieldNew[r][c] = field[r][c];
			}
		}

		return fieldNew;
	} 
}
