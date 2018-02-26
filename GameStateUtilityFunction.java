import java.util.*;

public class GameStateUtilityFunction implements IGameStateUtilityFunction {
	public float get(GameState gameState) {
		return 
		(100f) * gameState.getRowsCleared() + 
		(-0.5f) * gameState.getMaxTopHeight() + 
		(-1f) * gameState.getHolesTotalVolume() + 
		(-1f) * gameState.getBlockadesTotalVolume() + 
		(-1f) * gameState.getBumpiness() + 
		(-1f) * gameState.getWells() +
		//(8f) * gameState.leftColumnEmptyStrategy() +
		//(0f) * gameState.erodedPieceCells() +
		(-1000f) * gameState.hasPlayerLost();
	}
}
