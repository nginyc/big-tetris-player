import java.util.*;

public class GameStateUtilityFunction implements IGameStateUtilityFunction {
	public float get(GameState gameState) {
		return 
		(-0.5) * gameState.getMaxTopHeight() + 
		(-1) * gameState.getHolesTotalVolume() + 
		(-1) * gameState.getBlockadesTotalVolume() + 
		(-1) * gameState.getBumpiness() + 
		(-1000) * gameState.hasPlayerLost();
	}
}
