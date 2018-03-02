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
		
		return (
			this.getWeight(0) * gameState.getColumnAggregateHeight() + 
			this.getWeight(1) * gameState.getRowsClearedInPrevMove() + 
			this.getWeight(2) * gameState.getHolesTotalVolume() +
			this.getWeight(3) * gameState.getMaxTopHeight() +
			this.getWeight(4) * gameState.getBumpiness() +
			this.getWeight(5) * gameState.getWells() +
			this.getWeight(6) * gameState.getPrevLandingHeight() +
			this.getWeight(7) * gameState.getColTransitions() +
			this.getWeight(8) * gameState.getRowTransitions() +
			this.getWeight(9) * gameState.getBlockadesTotalVolume() +
			this.getWeight(10) * gameState.getNumBlocksInField() +
			this.getWeight(11) * gameState.getMeanHeightDifference() +
            this.getWeight(12) * gameState.getNumEdgesTouchingTheWall() +
            this.getWeight(13) * gameState.getNumEdgesTouchingTheFloor() +
            this.getWeight(14) * gameState.getNumEdgesTouchingCeiling()
		);
	}
}
