import java.util.*;

public class StateNode {

    // StateNode properties
    private GameState currState;
    private ArrayList<StateNode> nextStates;
    private int[] prevMove, bestMove;
    private float numNextStates;
    private double utility;

    // Constructor
    StateNode(GameState state) {
        this.currState = state;
    }

    StateNode(GameState state, int[] move) {
        this.currState = state;
        this.prevMove = move;
    }

    // Mutators
    void findNextGameStates() {
        this.nextStates = new ArrayList<StateNode>();
        if(this.currState.getStateNextPiece() < 0) {
            // Next piece had not been set as this is a future state. We want to add future pieces permutations here.
            for(int i = 0; i < this.currState.N_PIECES; i++) {
                GameState nextPredictGameState = this.currState.clone();
                nextPredictGameState.setNextPiece(i);
                findPiecePermutationGameStates(nextPredictGameState);
            }
        } else {
            // Search the state space normally.
            findPiecePermutationGameStates(this.currState);
        }
    }

    void setUtility(double value) {
        this.utility = value;
    }

    void setBestMove(int[] value) {
        this.bestMove = value;
    }

    // Methods
    public void findPiecePermutationGameStates(GameState state) {
        int[][] legalMoves = state.getLegalPlayerMoves();

        for (int[] legalMove : legalMoves) {
            GameState nextGameState = state.clone();
            nextGameState.makePlayerMove(legalMove[GameState.ORIENT], legalMove[GameState.SLOT]);
            this.nextStates.add(new StateNode(nextGameState, legalMove));
        }
        this.numNextStates = this.nextStates.size();
    }

    // Accessors
    ArrayList<StateNode> getNextStates() {
        return this.nextStates;
    }

    GameState getGameState() {
        return this.currState;
    }

    float getNumNextStates() {
        return this.numNextStates;
    }

    int[] getPrevMove() {
        return this.prevMove;
    }

    int[] getBestMove() {
        return this.bestMove;
    }

    double getUtility() {
        return this.utility;
    }
}
