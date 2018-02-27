public class GameStateUtilityFunction implements IGameStateUtilityFunction {

	private double[] weights;

	public GameStateUtilityFunction(double[] weights) {
		if (weights.length != 8) {
			throw new IllegalArgumentException();
		}

		this.weights = weights;
	}

	private double getWeight(int i) {
		return (this.weights[i] - 0.5) * 2;
	}

	public double get(GameState gameState) {
		if (gameState.hasPlayerLost() == 1) {
			return -Double.MAX_VALUE;
		}
		
		return (
			this.getWeight(0) * gameState.getColumnAggregateHeight() + 
			this.getWeight(1) * gameState.getRowsClearedInCurrentMove() + 
			this.getWeight(2) * gameState.getHolesTotalVolume() +
			this.getWeight(3) * gameState.getBumpiness(1) +
			this.getWeight(4) * gameState.getWells(2) +
			this.getWeight(5) * gameState.getLandingHeight() +
			this.getWeight(6) * gameState.getColTransitions() +
			this.getWeight(7) * gameState.getRowTransitions()
		);
	}
}
