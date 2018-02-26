import java.util.*;

public class GameStateUtilityFunction implements IGameStateUtilityFunction {
	public float get(GameState gameState) {
		return (-1) * gameState.getMaxTopHeight() + (-1000) * gameState.hasPlayerLost();
	}
}
