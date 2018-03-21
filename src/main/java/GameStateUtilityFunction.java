public class GameStateUtilityFunction implements IGameStateUtilityFunction {

    private double[] weights;

	public GameStateUtilityFunction(double[] weights) {
		if (weights.length != 15) {
			throw new IllegalArgumentException();
		}

		this.weights = weights;
	}

	private double getWeight(int i) {
		return this.weights[i];
	}

	public double get(GameState gameState) {
		if (gameState.hasPlayerLost() == 1) {
			return -Double.MAX_VALUE;
        }

        int col = GameState.COLS;
        int row = gameState.rows;
        // Note: Many of the max values of the heuristics are probably overestimates, because they are the 
        // worst/best cases which are basically almost impossible to attain.
        
		return (
			this.getWeight(0) * (gameState.getColumnAggregateHeight() / col / row) +                    // 0 to col * row
			this.getWeight(1) * (gameState.getRowsClearedInPrevMove() / 4) +                            // 0 to 4
			this.getWeight(2) * (gameState.getHolesTotalVolume() / col / (row - 1)) +                   // 0 to col * (row - 1)
			this.getWeight(3) * (gameState.getMaxTopHeight() / row) +                                   // 0 to row
			this.getWeight(4) * (gameState.getBumpiness() / row / row / (col - 1)) +                    // 0 to row * row * (col - 1)
			this.getWeight(5) * (gameState.getWells() / row / row / (col - 1) * 2) +                    // 0 to row * row * ((col - 1) / 2)
			this.getWeight(6) * (gameState.getPrevLandingHeight() / row) +                              // 0 to row
			this.getWeight(7) * (gameState.getColTransitions() / col / row) +                           // 0 to row * col
			this.getWeight(8) * (gameState.getRowTransitions() / col / row) +                           // 0 to row * col
			this.getWeight(9) * (gameState.getBlockadesTotalVolume() / col / (row - 1)) +               // 0 to (row - 1) * col
			this.getWeight(10) * (gameState.getNumBlocksInField() / row / (col - 1)) +                  // 0 to row * (col - 1)
			this.getWeight(11) * (gameState.getMeanHeightDifference() / row / (col / 2) / (col / 2)) +  // 0 to row * (col / 2)^2
            this.getWeight(12) * (gameState.getNumEdgesTouchingTheWall() / row / 2) +                   // 0 to row * 2
            this.getWeight(13) * (gameState.getNumEdgesTouchingTheFloor() / col) +                      // 0 to col
            this.getWeight(14) * (gameState.getNumEdgesTouchingCeiling() / col)                         // 0 to col
		);
	}
}
