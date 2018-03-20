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
            while(gameState.hasPlayerLost() == 0) {
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
            new double[] { -0.678938546389057, 0.34584029998128385, -0.6507745907665137, 0.04495673139587936, -0.05088896346124317, -0.15501715808211858, -0.21428899853936528, -0.43126487860023166, -0.0066156198116587295, 0.03082954578656715, 0.5635769883273619, 0.18689883335437713, 0.272470781155557, -0.46836530219858297, -0.4933848863909443 },
        };

        Evaluator evaluator = new Evaluator();

        for (int i = 0; i < candidates.length; i ++) {
            System.out.println("Testing candidate " + i + " " + Arrays.toString(candidates[i]) + "...");
            double averageRowsCleared = evaluator.evaluate(candidates[i]);
            System.out.println("Candidate " + i + " clears average of " + averageRowsCleared + " rows over " + NO_OF_TRIES + " tries");
		}
	}
}