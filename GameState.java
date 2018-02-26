
import java.util.*;
import java.util.stream.Collectors;

public class GameState {

    public static final int COLS = 10;
    public static final int ROWS = 20;

    // TODO: Refactor definition of piece vocab if necessary
    public static final int N_PIECES = 7;
    public static final int ORIENT = 0;
    public static final int SLOT = 1;

    //possible orientations for a given piece type
    public static int[] P_ORIENTS = {1, 2, 4, 4, 4, 2, 2};

    //the next several arrays define the piece vocabulary in detail
    //width of the pieces [piece ID][orientation]
    public static int[][] P_WIDTH = {
        {2},
        {1, 4},
        {2, 3, 2, 3},
        {2, 3, 2, 3},
        {2, 3, 2, 3},
        {3, 2},
        {3, 2}
    };
    //height of the pieces [piece ID][orientation]
    public static int[][] P_HEIGHT = {
        {2},
        {4, 1},
        {3, 2, 3, 2},
        {3, 2, 3, 2},
        {3, 2, 3, 2},
        {2, 3},
        {2, 3}
    };
    public static int[][][] P_BOTTOM = {
        {{0, 0}},
        {{0}, {0, 0, 0, 0}},
        {{0, 0}, {0, 1, 1}, {2, 0}, {0, 0, 0}},
        {{0, 0}, {0, 0, 0}, {0, 2}, {1, 1, 0}},
        {{0, 1}, {1, 0, 1}, {1, 0}, {0, 0, 0}},
        {{0, 0, 1}, {1, 0}},
        {{1, 0, 0}, {0, 1}}
    };

    public static int[][][] P_TOP = {
        {{2, 2}},
        {{4}, {1, 1, 1, 1}},
        {{3, 1}, {2, 2, 2}, {3, 3}, {1, 1, 2}},
        {{1, 3}, {2, 1, 1}, {3, 3}, {2, 2, 2}},
        {{3, 2}, {2, 2, 2}, {2, 3}, {1, 2, 1}},
        {{1, 2, 2}, {3, 2}},
        {{2, 2, 1}, {2, 3}}
    };

    // TODO: Refactor definition of legal moves if necessary
    //all legal moves - first index is piece type - then a list of 2-length arrays
    public static int[][][] LEGAL_MOVES = new int[N_PIECES][][];

    //initialize legalMoves
    {
        //for each piece type
        for (int i = 0; i < N_PIECES; i++) {
            //figure number of legal moves
            int n = 0;
            for (int j = 0; j < P_ORIENTS[i]; j++) {
                //number of locations in this orientation
                n += COLS + 1 - P_WIDTH[i][j];
            }
            //allocate space
            LEGAL_MOVES[i] = new int[n][2];
            //for each orientation
            n = 0;
            for (int j = 0; j < P_ORIENTS[i]; j++) {
                //for each slot
                for (int k = 0; k < COLS + 1 - P_WIDTH[i][j]; k++) {
                    LEGAL_MOVES[i][n][ORIENT] = j;
                    LEGAL_MOVES[i][n][SLOT] = k;
                    n++;
                }
            }
        }
    }

    // State variables
    private int[][] field; // As defined in `State`
    private int nextPiece; // As defined in `State`, -1 for not set
    private int lost; // 1 if lost, 0 otherwise  
    private int turn = 0; // Turn count
    private int rowsCleared = 0; // Rows cleared

    // Derived variables
    private int[] top = new int[COLS]; // Top filled row of each column

    public GameState(int[][] field, int nextPiece, int lost, int turn, int rowsCleared) {
        // Board field must match COLS and ROWS
        if (field.length != ROWS || field[0].length != COLS) {
            throw new IllegalArgumentException();
        }

        this.field = field;
        this.nextPiece = nextPiece;
        this.lost = lost;
        this.turn = turn;
        this.rowsCleared = rowsCleared;

        this.refreshTop();
    }

    private GameState(int[][] field, int nextPiece, int lost, int turn, int rowsCleared, int[] top) {
        this.field = field;
        this.nextPiece = nextPiece;
        this.lost = lost;
        this.turn = turn;
        this.rowsCleared = rowsCleared;
        this.top = top;
    }

    public GameState clone() {
        int[][] fieldClone = Arrays.stream(this.field).map(row -> Arrays.copyOf(row, row.length)).toArray(int[][]::new);
        int[] topClone = Arrays.stream(this.top).toArray();
        return new GameState(
                fieldClone, this.nextPiece, this.lost, this.turn, this.rowsCleared, topClone
        );
    }

    ///////////////////////// Heuristics ////////////////////////////
    public int getMaxTopHeight() {
        return Arrays.stream(top).max().orElse(0);
    }

    public int getRowsCleared() {
        return this.rowsCleared;
    }

    public int getHolesTotalVolume() {
        int holes = 0;
        for (int c = 0; c < COLS; c++) {
            int r = top[c];
            if (r == 0) {
                continue;
            }
            for (int i = r - 1; i >= 0; i--) {
                if (field[i][c] == 0) {
                    holes++;
                }
            }
        }
        return holes;
    }

    public int getBlockadesTotalVolume() {
        int potentialBlockades = 0;
        int blockades = 0;
        for (int c = 0; c < COLS; c++) {
            potentialBlockades = 0;
            int r = top[c];
            if (r == 0) {
                continue;
            }
            for (int i = r - 1; i >= 0; i--) {
                if (field[i][c] != 0) {
                    potentialBlockades++;
                } else {
                    blockades += potentialBlockades;
                    potentialBlockades = 0;
                }
            }
        }
        return blockades;
    }

    public int getBumpiness() {
        int bumpiness = 0;
        for (int c = 0; c < COLS - 1; c++) {
            bumpiness += Math.abs(top[c] - top[c + 1]);
        }
        return bumpiness;
    }

    /////////////////////////////////////////////////////////////////
    public int hasPlayerLost() {
        return this.lost;
    }

    public void makePlayerMove(int orient, int slot) {
        if (this.nextPiece == -1) {
            throw new IllegalStateException();
        }

        int nextPiece = this.nextPiece;
        int turn = ++this.turn;

        int bottom = -1;
        // row corresponding to bottom of piece after falling
        for (int c = 0; c < P_WIDTH[nextPiece][orient]; c++) {
            bottom = Math.max(bottom, this.top[slot + c] - P_BOTTOM[nextPiece][orient][c]);
        }

        // Check if game ended
        if (bottom + P_HEIGHT[nextPiece][orient] > ROWS) {
            this.lost = 1;
            return;
        }

        // Fill in the appropriate blocks
        // For each column in the piece 
        for (int i = 0; i < P_WIDTH[nextPiece][orient]; i++) {
            // From bottom to top of piece
            for (int h = bottom + P_BOTTOM[nextPiece][orient][i]; h < bottom + P_TOP[nextPiece][orient][i]; h++) {
                this.field[h][i + slot] = turn;
            }
        }

        // Adjust top
        for (int c = 0; c < P_WIDTH[nextPiece][orient]; c++) {
            this.top[slot + c] = bottom + P_TOP[nextPiece][orient][c];
        }

        // Check for full rows and clear them
        for (int r = bottom + P_HEIGHT[nextPiece][orient] - 1; r >= bottom; r--) {

            // Check all columns in the row
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (this.field[r][c] == 0) {
                    full = false;
                    break;
                }
            }

            // If the row was full - remove it and slide above stuff down
            if (full) {
                this.rowsCleared++;
                // For each column
                for (int c = 0; c < COLS; c++) {
                    // Slide down all bricks
                    for (int i = r; i < this.top[c]; i++) {
                        this.field[i][c] = (i + 1 < ROWS) ? this.field[i + 1][c] : 0;
                    }

                    // Lower top
                    this.top[c]--;
                }
            }
        }

        this.nextPiece = -1;
    }

    public void setNextPiece(int piece) {
        if (this.nextPiece != -1) {
            throw new IllegalStateException();
        }

        this.nextPiece = piece;
    }

    // On player's turnm, get legal moves for player as an array of {slot, orient} duples
    public int[][] getLegalPlayerMoves() {
        if (this.nextPiece == -1) {
            throw new IllegalStateException();
        }

        return LEGAL_MOVES[this.nextPiece];
    }

    @Override
    public String toString() {
        return String.format(
                "# State \n"
                + "## field \n"
                + "%s \n"
                + "## nextPiece: %d \n"
                + "## lost: %d \n"
                + "## turn: %d \n"
                + "## rowsCleared: %d \n"
                + "# Derived state \n"
                + "## top: %s \n\n",
                this.getPrettyPrintFieldString(this.field), this.nextPiece, this.lost, this.turn, this.rowsCleared,
                Arrays.toString(this.top)
        );
    }

    private String getPrettyPrintFieldString(int[][] field) {
        List<int[]> fieldClone = Arrays.stream(field).collect(Collectors.toList());
        Collections.reverse(fieldClone);
        return String.join("\n",
                fieldClone.stream().map(x -> Arrays.toString(x)).toArray(String[]::new));
    }

    private void refreshTop() {
        for (int c = 0; c < COLS; c++) {
            this.top[c] = 0;
            for (int r = ROWS - 1; r >= 0; r--) { // From top
                if (this.field[r][c] != 0) {
                    this.top[c] = r + 1;
                    break;
                }
            }
        }
    }
}
