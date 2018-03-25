public class GameStateUtilityFunction implements IGameStateUtilityFunction {

    private double[] weights;

	public GameStateUtilityFunction(double[] weights) {
		if (weights.length != 15) {
			throw new IllegalArgumentException();
		}

		this.weights = weights;
	}

	private double normalize(double value, double min, double max) {
		return (value - min) / (max - min);
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
			this.weights[0] * (this.normalize(gameState.getColumnAggregateHeight(), 0, col * row)) +                    // 0 to col * row
			this.weights[1] * (this.normalize(gameState.getRowsClearedInPrevMove(), 0, 4)) +                            // 0 to 4
			this.weights[2] * (this.normalize(gameState.getHolesTotalVolume(), 0, col * (row - 1))) +                   // 0 to col * (row - 1)
			this.weights[3] * (this.normalize(gameState.getMaxTopHeight(), 0, row)) +                                   // 0 to row
			this.weights[4] * (this.normalize(gameState.getBumpiness(), 0, row * row * (col - 1))) +                    // 0 to row * row * (col - 1)
			this.weights[5] * (this.normalize(gameState.getWells(), 0, row * row * (col - 1) * 2)) +                    // 0 to row * row * ((col - 1) / 2)
			this.weights[6] * (this.normalize(gameState.getPrevLandingHeight(), 0, row)) +                              // 0 to row
			this.weights[7] * (this.normalize(gameState.getColTransitions(), 0, col * row)) +                           // 0 to row * col
			this.weights[8] * (this.normalize(gameState.getRowTransitions(), 0, col * row)) +                           // 0 to row * col
			this.weights[9] * (this.normalize(gameState.getBlockadesTotalVolume(), 0, col * (row - 1))) +               // 0 to (row - 1) * col
			this.weights[10] * (this.normalize(gameState.getNumBlocksInField(), 0, row * (col - 1))) +                  // 0 to row * (col - 1)
			this.weights[11] * (this.normalize(gameState.getMeanHeightDifference(), 0, (double)(row * col * col) / 4)) +  // 0 to row * (col / 2)^2
            this.weights[12] * (this.normalize(gameState.getNumEdgesTouchingTheWall(), 0, row * 2)) +                   // 0 to row * 2
            this.weights[13] * (this.normalize(gameState.getNumEdgesTouchingTheFloor(), 0, col)) +                      // 0 to col
            this.weights[14] * (this.normalize(gameState.getNumEdgesTouchingCeiling(), 0, col))                         // 0 to col
		);
	}
}
