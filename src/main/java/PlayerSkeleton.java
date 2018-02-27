
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
		int[] move = this.gameStateSearcher.getBestMove(gameState);
		return move;
	}

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton(new double[] {
			-0.3833217021335531, -0.2991216095851814, -0.9033648455277214, -0.06417739912016285, -0.07057842371336309, -0.09208476184731196, -0.018568446747041722, -0.30384126603893424, -0.2, -0.2, -0.4, -0.1, 0.01, 0.01, -1, 0.01
			// 100d/1000, -0.5d/1000, -100d/1000, -10d/1000, -10d/1000, -10d/1000, -1d/1000, -1000d/1000
			// 100d, -0.5d, -100d, -10d, -10d, -10d, -1d, -1000d,
			// 0.37073482618670894, -0.5052451673530609, -0.9856621036908355, -0.592334190301409, -0.11416158721924785, -0.27826905231459276, 0.04908111855531905, -1
			// 0.37073482618670894, -0.5052451673530609, -0.9856621036908355, -0.592334190301409, -0.11416158721924785, -0.27826905231459276, 0.04908111855531905, -0.06935114340499116
		});

		while(!s.hasLost()) {
			int[] move = p.pickMove(s);
			if (move == null) {
				// System.out.println("No move will result in a next state.");
				break;
			}
			s.makeMove(move[0], move[1]);
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
