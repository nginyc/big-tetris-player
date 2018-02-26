import java.util.*;

public class GameStateUtilityFunction implements IGameStateUtilityFunction {

	private double[] weights;

	public GameStateUtilityFunction(double[] weights) {
		if (weights.length != 6) {
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
		weights[4] * gameState.getBumpiness() + 
		weights[5] * gameState.hasPlayerLost();
	}
}
