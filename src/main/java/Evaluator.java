import java.util.Arrays;

public class Evaluator {

    public static int NO_OF_TRIES = 1;

	public Evaluator() {
    }
    
    public double evaluate(double[] weights) {
        GameStateUtilityFunction utilityFunction = new GameStateUtilityFunction(weights);
        GameStateSearcher gameStateSearcher = new GameStateSearcher(utilityFunction);

        int rowsCleared = 0;
        for (int j = 0; j < NO_OF_TRIES; j ++) {
            State state = new State();
            GameState gameState = new GameState(State.ROWS);
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
            new double[] { -0.509030547581401, -0.6164657784915446, -0.8525819563636403, -0.3745322537668416, 0.1810698222670854, -0.5093756308917914, -0.09508589979762064, -0.8752873163316679, -0.5728075805042074, -0.49572994692471595, -0.17189057581690614, -0.6336212465849484, 0.7239962247970739, 0.6352966845893782, 0.25492722923034844 },
            new double[] { -0.5528063620775443, -0.6329763669790446, -1.0, -0.266664052806424, 0.21449147912678654, -0.5187947625476893, -0.04087496129041331, -0.7558447400087838, -0.6796475523688372, -0.49409691831159164, -0.17592484640362693, -0.6299124357664336, 1.0, 0.641557706962041, 0.25847578607080424 },
            new double[] { -0.8404818342671059, 0.7215086004349738, -0.19270837809567318, 0.004948446724797127, 0.08053947797738137, -0.19486956740171635, -0.49895485688944996, -0.3657337172780898, -0.26101377431193834, -0.07634281548350902, 0.9091451760843949, 0.30336014635697833, 0.3276157994981263, -0.488271619159437, 0.030193411369024706 },
            new double[] { -0.7600470062605138, -0.4521173978191585, -0.5744522175063087, -0.021265771358714873, 0.4340105392756133, -0.21542144905191385, -0.30213028362155675, -0.6454385901789101, -0.8601498956441487, -0.42247759897665393, 0.7705063090959259, -0.1347681386561799, 0.4169110305839228, 0.015883560257191016, -0.01313231451581028 },
        };

        Evaluator evaluator = new Evaluator();

        for (int i = 0; i < candidates.length; i ++) {
            System.out.println("Testing candidate " + i + " " + Arrays.toString(candidates[i]) + "...");
            double averageRowsCleared = evaluator.evaluate(candidates[i]);
            System.out.println("Candidate " + i + " clears average of " + averageRowsCleared + " rows over " + NO_OF_TRIES + " tries");
		}
	}
}