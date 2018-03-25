import java.util.Arrays;

public class Evaluator {

    public static int NO_OF_TRIES = 10;
    public static int ROWS = State.ROWS - 1;

	public Evaluator() {
    }
    
    public double evaluate(double[] weights) {
        GameStateUtilityFunction utilityFunction = new GameStateUtilityFunction(weights);
        GameStateSearcher gameStateSearcher = new GameStateSearcher(ROWS, utilityFunction);

        int rowsCleared = 0;
        for (int j = 0; j < NO_OF_TRIES; j ++) {
            State state = new State();
            GameState gameState = new GameState(ROWS);
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
            new double[] { -0.10535323935941053, 0.0012378990514839156, -0.11976135881490828, 0.0009199426483574079, -0.08823390729195249, -1, -0.0034571639142464347, -0.07006317338464395, -0.0389316396907097, -0.006601781575429543, 0.08778061719042551, 0.07347776609114921, 0.006118235517837187, -0.0027428340856810664, -0.0042475024771130515 },
        };

        Evaluator evaluator = new Evaluator();

        for (int i = 0; i < candidates.length; i ++) {
            System.out.println("Testing candidate " + i + " " + Arrays.toString(candidates[i]) + "...");
            double averageRowsCleared = evaluator.evaluate(candidates[i]);
            System.out.println("Candidate " + i + " clears average of " + averageRowsCleared + " rows over " + NO_OF_TRIES + " tries");
		}
	}
}