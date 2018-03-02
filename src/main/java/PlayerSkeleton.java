import java.util.Arrays;

public class PlayerSkeleton {

	private GameStateSearcher gameStateSearcher;
	private GameState gameState;
	private GameStateUtilityFunction utilityFunction;

	public PlayerSkeleton(double[] weights) {
		this.gameState = new GameState();
		this.utilityFunction = new GameStateUtilityFunction(weights);
		this.gameStateSearcher = new GameStateSearcher(utilityFunction);
	}

	//implement this function to have a working system
	public int[] pickMove(State s) {
		this.gameState.setNextPiece(s.getNextPiece());
		// System.out.println("At game state:");
		// System.out.println(this.gameState + "\n");
		GameStateSearcher.BestMoveResult result = this.gameStateSearcher.searchNLevelsDFS(this.gameState, 1);
		// Actually make the best move
		this.gameState.makePlayerMove(result.move[0], result.move[1]);
		// System.out.println(String.format("Best move %s resulted in util %f.", Arrays.toString(result.move), result.moveUtil));
		return result.move;
	}

	public static void main(String[] args) {
		State s = new State();
		// new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton(new double[] {
			-0.8109383395184426, -0.4498623560469789, -0.4972411024934661, -0.021353334982763178, 0.47810032299068606, -0.23881966399830679, -0.25970076647022794, -0.5777192405714082, -0.8574067070271092, -0.44182663708670394, 0.763216765985224, -0.21294243905496052, 0.2963920141458013, -0.09731451379647636, -0.0707415185532011
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
