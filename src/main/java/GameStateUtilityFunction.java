public class GameStateUtilityFunction implements IGameStateUtilityFunction {

	private double[] weights;

	public GameStateUtilityFunction(double[] weights) {
		if (weights.length != 4) {
			throw new IllegalArgumentException();
		}

		this.weights = weights;
	}

	public double get(GameState gameState) {
		if (gameState.hasPlayerLost() == 1) {
			return -Double.MAX_VALUE;
		}

		return 
			-weights[0] * gameState.getColumnAggregateHeight() + 
			weights[1] * gameState.getRowsClearedInCurrentMove() + 
			-weights[2] * gameState.getHolesTotalVolume() +
			-weights[3] * gameState.getBumpiness(1);
	}
}
