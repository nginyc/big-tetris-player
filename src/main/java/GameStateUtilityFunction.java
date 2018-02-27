public class GameStateUtilityFunction implements IGameStateUtilityFunction {

	private double[] weights;

	public GameStateUtilityFunction(double[] weights) {
		if (weights.length != 16) {
			throw new IllegalArgumentException();
		}

		this.weights = weights;
	}

	public double get(GameState gameState) {
		return 
			weights[0] * gameState.getRowsCleared() + 
			weights[1] * gameState.getMaxTopHeight() + 
			weights[2] * gameState.getHolesTotalVolume() +
			weights[3] * gameState.getBlockadesTotalVolume() +
			weights[4] * gameState.getBumpiness(1) +
			weights[5] * gameState.getWells(2) +
			weights[6] * gameState.getNumBlocksInField() +
			weights[7] * gameState.hasPlayerLost() +
			weights[8] * gameState.getMeanHeightDifference() +
			weights[9] * gameState.getLandingHeight() +
			weights[10] * gameState.getColTransitions() + //col transitions seem to be more damaging than row
            weights[11] * gameState.getRowTransitions() +
            weights[12] * gameState.getNumEdgesTouchingTheWall() +
            weights[13] * gameState.getNumEdgesTouchingTheFloor() +
            weights[14] * gameState.getNumEdgesTouchingCeiling() +
            weights[15] * gameState.getNumEdgesTouchingAnotherBlock();
	}
}
