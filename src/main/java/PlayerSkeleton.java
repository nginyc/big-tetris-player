public class PlayerSkeleton {

	private GameStateSearcher gameStateSearcher;
	private GameState gameState;
	private GameStateUtilityFunction utilityFunction;

	public PlayerSkeleton(double[] weights) {
		this.gameState = new GameState(State.ROWS - 1);
		this.utilityFunction = new GameStateUtilityFunction(weights);
		this.gameStateSearcher = new GameStateSearcher(utilityFunction);
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
		PlayerSkeleton p = new PlayerSkeleton(new double[] {
			-0.7600470062605138, -0.4521173978191585, -0.5744522175063087, -0.021265771358714873, 0.4340105392756133, -0.21542144905191385, -0.30213028362155675, -0.6454385901789101, -0.8601498956441487, -0.42247759897665393, 0.7705063090959259, -0.1347681386561799, 0.4169110305839228, 0.015883560257191016, -0.01313231451581028
		});

		while(!s.hasLost()) {
			int[] move = p.pickMove(s);
			if (move == null) {
				// System.out.println("No move will result in a next state.");
				break;
			}
			s.makeMove(move[State.ORIENT], move[State.SLOT]);
			// s.draw();
			// s.drawNext(0,0);
			// try {
			// 	Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// 	e.printStackTrace();
			// }
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
}
