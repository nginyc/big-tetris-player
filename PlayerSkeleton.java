
public class PlayerSkeleton {

	private GameStateSearcher gameStateSearcher;
	private GameStateUtilityFunction utilityFunction;

	public PlayerSkeleton() {
		this.utilityFunction = new GameStateUtilityFunction();
		this.gameStateSearcher = new GameStateSearcher(utilityFunction);
	}

	//implement this function to have a working system
	public int[] pickMove(State s) {
		GameState gameState = new GameState(
			this.getBoardField(s), s.getNextPiece(), 
			s.lost ? 1 : 0, s.getTurnNumber()
		);
		int[] move = this.gameStateSearcher.getBestMove(gameState);
		return move;
	}

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			System.out.println("Picking next move...");
			int[] move = p.pickMove(s);
			s.makeMove(move[0], move[1]);
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(5000);
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
