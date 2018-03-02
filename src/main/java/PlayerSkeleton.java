
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
		GameStateSearcher.BestMoveResult result = this.gameStateSearcher.searchNLevelsDFS(this.gameState, 1);
		this.gameState.makePlayerMove(result.move[0], result.move[1]);
		return result.move;
	}

	public static void main(String[] args) {
		State s = new State();
		// new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton(new double[] {
			-0.8793331272057922, 0.3101553510084032, -1, 0.08131740320451719, -0.26371297468266, -0.3641134817118301, 0.3132512371248528, -0.12697638610048667, -0.14414061689164392, -0.33749567608321795, 0.5510232612670767, 0.6468889991303903, -0.11670647424861746, 0.017096300316661317, 0.6207565558500359
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
			// 	Thread.sleep(10000);
			// } catch (InterruptedException e) {
			// 	e.printStackTrace();
			// }
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
}
