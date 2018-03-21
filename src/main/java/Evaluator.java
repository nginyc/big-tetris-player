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
            new double[] { -0.6480731776141786, 0.3807425271110561, -0.7754776101157559, 0.0565896368226417, -0.030153599357583383, -0.17087308203302012, -0.2126650511234261, -0.4309887734369272, -0.23948529345182237, -0.04274779318872454, 0.5999737840835486, 0.18079736188170248, 0.1881795167166116, -0.33744708988239214, -0.5225643642291802 },
        };

        Evaluator evaluator = new Evaluator();

        for (int i = 0; i < candidates.length; i ++) {
            System.out.println("Testing candidate " + i + " " + Arrays.toString(candidates[i]) + "...");
            double averageRowsCleared = evaluator.evaluate(candidates[i]);
            System.out.println("Candidate " + i + " clears average of " + averageRowsCleared + " rows over " + NO_OF_TRIES + " tries");
		}
	}
}