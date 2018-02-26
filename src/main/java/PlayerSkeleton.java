
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
			-0.3208433615384241, -0.11081195413475875, -0.9605866492448987, -0.17979304088323622, -0.14854310222244482, -0.10959924803520749, -0.054347037107380425	
		});

		
		while(!s.hasLost()) {
			int[] move = p.pickMove(s);
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
