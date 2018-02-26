public class GameStateUtilityFunction implements IGameStateUtilityFunction {

	private double[] weights;

	public GameStateUtilityFunction(double[] weights) {
		if (weights.length != 8) {
			throw new IllegalArgumentException();
		}

		this.weights = weights;
	}

	public double get(GameState gameState) {
		if (gameState.hasPlayerLost() == 1) {
			return -Double.MAX_VALUE;
		}

		return 
			weights[0] * gameState.getRowsCleared() + 
			weights[1] * gameState.getMaxTopHeight() + 
			weights[2] * gameState.getHolesTotalVolume() +
			weights[3] * gameState.getBlockadesTotalVolume() +
			weights[4] * gameState.getBumpiness(1) +
			weights[5] * gameState.getWells(2) +
			weights[6] * gameState.getNumBlocksInField() +
			weights[7] * gameState.getMeanHeightDifference();
	}
}
