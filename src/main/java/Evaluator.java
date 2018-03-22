import java.util.Arrays;

public class Evaluator {

    public static int NO_OF_TRIES = 100;

	public Evaluator() {
    }
    
    public double evaluate(double[] weights) {
        GameStateUtilityFunction utilityFunction = new GameStateUtilityFunction(weights);
        GameStateSearcher gameStateSearcher = new GameStateSearcher(utilityFunction);

        int rowsCleared = 0;
        for (int j = 0; j < NO_OF_TRIES; j ++) {
            State state = new State();
            GameState gameState = new GameState(State.ROWS - 1);
            while(!state.hasLost()) {
                // Randomly get a piece
                int nextPiece = state.getNextPiece();
                gameState.setNextPiece(nextPiece);
                int[] move = gameStateSearcher.search(gameState);
                state.makeMove(move[State.ORIENT], move[State.SLOT]);
                gameState.makePlayerMove(move[GameState.ORIENT], move[GameState.SLOT]);
            }
            
            System.out.println(state.getRowsCleared() + " rows cleared");
            rowsCleared += state.getRowsCleared(); 
        }

        double averageRowsCleared = (double)rowsCleared / NO_OF_TRIES;
        return averageRowsCleared;
    }

	public static void main(String[] args) {
        double[][] candidates = new double[][] {
            new double[] { -0.641196655437541, 0.09316246852599018, -1.0, -0.10966787782405332, 0.3366283480319635, -0.9755105377101284, -0.030234993598680794, -0.6935400314361607, -0.7578671550807475, -0.11586238445360375, 0.27158497232646495, 0.45848002866814774, 0.13763390483047486, -0.0011017814204401586, -0.624064623760171 },
        };

        Evaluator evaluator = new Evaluator();

        for (int i = 0; i < candidates.length; i ++) {
            System.out.println("Testing candidate " + i + " " + Arrays.toString(candidates[i]) + "...");
            double averageRowsCleared = evaluator.evaluate(candidates[i]);
            System.out.println("Candidate " + i + " clears average of " + averageRowsCleared + " rows over " + NO_OF_TRIES + " tries");
		}
	}
}