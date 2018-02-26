import java.util.*;

public class GameStateUtilityFunction implements IGameStateUtilityFunction {
	public float get(GameState gameState) {
		return 
		(100f) * gameState.getRowsCleared() + 
		(-0.5f) * gameState.getMaxTopHeight() + 
		(-100f) * gameState.getHolesTotalVolume() +
		(-10f) * gameState.getBlockadesTotalVolume() +
		(-10f) * gameState.getBumpiness() +
		(-10f) * gameState.getWells() +
		//(8f) * gameState.leftColumnEmptyStrategy() +
		//(0f) * gameState.erodedPieceCells() +
		(-1000f) * gameState.hasPlayerLost();
	}
}
