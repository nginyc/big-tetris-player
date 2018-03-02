import java.util.Arrays;

public class PlayerTrainer {

	public static int CANDIDATE_COUNT = 100;
	public static int CANDIDATE_TEST_NO_OF_TRIES = 100;

	public PlayerTrainer() {
	}

	public static void main(String[] args) {

		double[][] candidates = new double[CANDIDATE_COUNT][];

		GameStateUtilityLearner learner = new GameStateUtilityLearner(
			10, 25, 20, 0.1, 0.8, 0.5
		);
		for (int i = 0; i < CANDIDATE_COUNT; i ++) {
			candidates[i] = learner.train();
			System.out.println("Candidate " + i + ": " + Arrays.toString(candidates[i]));
		}

		// Calculate n-game average rows cleared of candidates, then pick the best performing one
		double[] bestCandidate = null;
		double bestCandidateRowsCleared = 0;
		for (int i = 0; i < CANDIDATE_COUNT; i ++) {
			System.out.println("Testing candidate " + i + ": " + Arrays.toString(candidates[i]));
			
			GameStateUtilityFunction utilityFunction = new GameStateUtilityFunction(candidates[i]);
			GameStateSearcher gameStateSearcher = new GameStateSearcher(utilityFunction);
	
			int rowsCleared = 0;
			for (int j = 0; j < CANDIDATE_TEST_NO_OF_TRIES; j ++) {
				GameState gameState = new GameState();
				while(gameState.hasPlayerLost() == 0) {
					// Randomly get a piece
					int nextPiece = gameState.getRandomNextPiece();
					gameState.setNextPiece(nextPiece);
					GameStateSearcher.BestMoveResult result = gameStateSearcher.searchNLevelsDFS(gameState, 1);
					int[] move = result.move;
					gameState.makePlayerMove(move[GameState.ORIENT], move[GameState.SLOT]);
				}
				rowsCleared += gameState.getRowsCleared(); 
			}

			double averageRowsCleared = (double)rowsCleared / CANDIDATE_TEST_NO_OF_TRIES;
			
			System.out.println("Candidate " + i + " clears average of " + averageRowsCleared + " rows over " + CANDIDATE_TEST_NO_OF_TRIES + " tries");

			if (averageRowsCleared > bestCandidateRowsCleared) {
				bestCandidate = candidates[i];
				bestCandidateRowsCleared = averageRowsCleared;
			}
		}

		System.out.println("Best candidate weights: " + Arrays.toString(bestCandidate));
	}
}