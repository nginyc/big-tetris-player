public class GameStateUtilityFunction implements IGameStateUtilityFunction {

	private double[] weights;

	public GameStateUtilityFunction(double[] weights) {
		if (weights.length != 7) {
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
			weights[5] * gameState.getWells() +
			weights[6] * gameState.getNumBlocksInField();
	}
}
