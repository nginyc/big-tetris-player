import java.util.function.DoubleSupplier;

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
		// Features are wrapped in a function to allow for lazy evaluation (some weights might be passed as 0)
		DoubleSupplier[] features = new DoubleSupplier[] {
			() -> this.normalize(gameState.getColumnAggregateHeight(), 0, col * row),
			() -> this.normalize(gameState.getRowsClearedInPrevMove(), 0, 4),
			() -> this.normalize(gameState.getHolesTotalVolume(), 0, col * (row - 1)),
			() -> this.normalize(gameState.getMaxTopHeight(), 0, row),
			() -> this.normalize(gameState.getBumpiness(), 0, row * row * (col - 1)),
			() -> this.normalize(gameState.getWells(), 0, row * row * (col - 1) * 2),
			() -> this.normalize(gameState.getPrevLandingHeight(), 0, row),
			() -> this.normalize(gameState.getColTransitions(), 0, col * row),
			() -> this.normalize(gameState.getRowTransitions(), 0, col * row),
			() -> this.normalize(gameState.getBlockadesTotalVolume(), 0, col * (row - 1)),
			() -> this.normalize(gameState.getNumBlocksInField(), 0, row * (col - 1)),
			() -> this.normalize(gameState.getMeanHeightDifference(), 0, (double)(row * col * col) / 4),
			() -> this.normalize(gameState.getNumEdgesTouchingTheWall(), 0, row * 2),
			() -> this.normalize(gameState.getNumEdgesTouchingTheFloor(), 0, col),
			() -> this.normalize(gameState.getNumEdgesTouchingCeiling(), 0, col)
		};
		
		double utility = 0;
		for (int i = 0; i < 15; i ++) {
			if (this.weights[i] != 0) {
				utility += this.weights[i] * features[i].getAsDouble();
			}
		}
        return utility;
	}
}
