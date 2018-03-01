
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

		GameStateSearcher.BestMoveResult result = this.gameStateSearcher.searchNLevelsDFS(gameState, 1);
		
		return result.move;
	}

	public static void main(String[] args) {
		State s = new State();
		// new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton(new double[] {
			0.3558897587671985, 0.5578360670485542, 0.15301938433107315, 0.7744246767898073, 0.3632932494322964, 0.13466182090419002, 0.09534435426024071, 0.1322198150632026, 0.2773706633320931, 0.06468844936537607, 0.8266387046568953, 0.831341087462889, 0.870554870899324, 0.5414074175725934
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
