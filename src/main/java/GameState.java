import java.util.*;

public class GameState {
    public static final int COLS = 10;
    public static final int N_PIECES = 7;
    public static final int ORIENT = 0;
    public static final int SLOT = 1;

    //possible orientations for a given piece type
    public static int[] P_ORIENTS = { 1, 2, 4, 4, 4, 2, 2 };

    //Volume of each blocks
    public static int[] P_VOLUME = { 4, 4, 4, 4, 4, 4, 4 };

    //the next several arrays define the piece vocabulary in detail
    //width of the pieces [piece ID][orientation]
    public static int[][] P_WIDTH = { 
        { 2 }, 
        { 1, 4 }, 
        { 2, 3, 2, 3 },
        { 2, 3, 2, 3 }, 
        { 2, 3, 2, 3 }, 
        { 3, 2 },
        { 3, 2 }
     };

    //height of the pieces [piece ID][orientation]
    public static int[][] P_HEIGHT = { 
        { 2 }, 
        { 4, 1 }, 
        { 3, 2, 3, 2 }, 
        { 3, 2, 3, 2 }, 
        { 3, 2, 3, 2 }, 
        { 2, 3 },
        { 2, 3 } 
    };

    //?? of the pieces [piece ID][orientation][num empty space at this col from bottom to block]
    public static int[][][] P_BOTTOM = { 
        { 
            { 0, 0 } 
        }, 
        {  
            { 0 }, 
            { 0, 0, 0, 0 } 
        },
        { 
            { 0, 0 }, 
            { 0, 1, 1 }, 
            { 2, 0 }, 
            { 0, 0, 0 } 
        }, 
        { 
            { 0, 0 }, 
            { 0, 0, 0 }, 
            { 0, 2 }, 
            { 1, 1, 0 } 
        },
        { 
            { 0, 1 }, 
            { 1, 0, 1 }, 
            { 1, 0 }, 
            { 0, 0, 0 } 
        }, 
        { 
            { 0, 0, 1 }, 
            { 1, 0 } 
        }, 
        { 
            { 1, 0, 0 }, 
            { 0, 1 } 
        } 
    };

    //?? of the pieces [piece ID][orientation][num squares at this col from top to block]
    public static int[][][] P_TOP = { 
        { 
            { 2, 2 } 
        }, 
        { 
            { 4 }, 
            { 1, 1, 1, 1 } 
        },
        { 
            { 3, 1 }, 
            { 2, 2, 2 }, 
            { 3, 3 }, 
            { 1, 1, 2 } 
        }, 
        { 
            { 1, 3 }, 
            { 2, 1, 1 }, 
            { 3, 3 }, 
            { 2, 2, 2 } 
        },
        { 
            { 3, 2 }, 
            { 2, 2, 2 }, 
            { 2, 3 }, 
            { 1, 2, 1 } 
        },
        { 
            { 1, 2, 2 }, 
            { 3, 2 } 
        }, 
        { 
            { 2, 2, 1 }, 
            { 2, 3 } 
        } 
    };

    //all legal moves - first index is piece type - then a list of 2-length arrays
    public static int[][][] LEGAL_MOVES = null;
    static {
        // Initialize this only ONCE
        if (LEGAL_MOVES == null) {
            LEGAL_MOVES = new int[N_PIECES][][];
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
    }

    // State variables
    public int rows; // No. of rows
    private HashSet<Integer> field; // Whether field at index is occupied
    private int nextPiece = -1; // As defined in `State`, -1 for not set
    private int orient = -1; // Orientation of the nextPiece, -1 for not set
    private int lost = 0; // 1 if lost, 0 otherwise  
    private int turn = 0; // Turn count
    private int rowsCleared = 0; // Rows cleared in total

    // Derived variables
    private int[] top; // Column heights
    private int[] bottom; // Column heights from the bottom
    private int maxTop; // Max height of column
    private int columnAggregateHeight = 0; // Sum of all column heights
    private int columnAggregateBottomStackHeight = 0; // Sum of all floor stack heights
    private int rowsClearedInPrevMove = 0; // Rows cleared in prev move
    private int numBlocksInField = 0; // Number of filled squares in level
    private double landingHeightInPrevMove = 0; // Landing height in prev move
    private int numBlocksTouchingWall = 0;
    private int[] rowTransitions; // number of transitions from blocks to hole to block in each row
    private int[] colTransitions; // number of transitions from blocks to hole to block in each col

    public GameState(int rows) {
        this.rows = rows;
        this.top = new int[COLS];
        this.bottom = new int[COLS];
        this.colTransitions = new int[COLS];
        this.rowTransitions = new int[this.rows];
        this.field = new HashSet<>();
    }

    private GameState(int rows, HashSet<Integer> field, int nextPiece, int lost, int turn, int rowsCleared, int[] top,
        int[] bottom, int columnAggregateHeight, int columnAggregateBottomStackHeight, int rowsClearedInPrevMove, 
        int numBlocksInField, int maxTop, double landingHeightInPrevMove, int numBlocksTouchingWall, int[] rowTransitions, 
        int[] colTransitions) {
        this.rows = rows;
        this.field = field;
        this.nextPiece = nextPiece;
        this.lost = lost;
        this.turn = turn;
        this.rowsCleared = rowsCleared;
        this.top = top;
        this.bottom = bottom;
        this.columnAggregateHeight = columnAggregateHeight;
        this.columnAggregateBottomStackHeight = columnAggregateBottomStackHeight;
        this.rowsClearedInPrevMove = rowsClearedInPrevMove;
        this.numBlocksInField = numBlocksInField;
        this.maxTop = maxTop;
        this.landingHeightInPrevMove = landingHeightInPrevMove;
        this.numBlocksTouchingWall = numBlocksTouchingWall;
        this.rowTransitions = rowTransitions;
        this.colTransitions = colTransitions;
    }

    public GameState clone() {
        HashSet<Integer> fieldClone = new HashSet<>(this.field);
        int[] topClone = Arrays.stream(this.top).toArray();
        int[] bottomClone = Arrays.stream(this.bottom).toArray();
        int[] rowTransitionsClone = Arrays.stream(this.rowTransitions).toArray();
        int[] colTransitionsClone = Arrays.stream(this.colTransitions).toArray();
        return new GameState(
            this.rows, fieldClone, this.nextPiece, this.lost, 
            this.turn, this.rowsCleared, topClone, bottomClone,
            this.columnAggregateHeight, this.columnAggregateBottomStackHeight, this.rowsClearedInPrevMove, 
            this.numBlocksInField, this.maxTop, this.landingHeightInPrevMove,
            this.numBlocksTouchingWall, rowTransitionsClone, colTransitionsClone
        );
    }

    ///////////////////////// Heuristics ////////////////////////////

    public int getColumnAggregateHeight() {
        return this.columnAggregateHeight;
    }

    public int getMaxTopHeight() {
        return this.maxTop;
    }

    public int getRowsCleared() {
        return this.rowsCleared;
    }

    public int getRowsClearedInPrevMove() {
        return this.rowsClearedInPrevMove;
    }

    public int getNumBlocksInField() {
        return this.numBlocksInField;
    }

    // This heuristic penalizes the volume of the holes
    public int getHolesTotalVolume() {
        return this.columnAggregateHeight - this.numBlocksInField;
    }

    // imagine xx___xx, there will be 2 row transitions
    public int getRowTransitions() {
        return Arrays.stream(this.rowTransitions).sum();
    }

    public int getColTransitions() {
        return Arrays.stream(this.colTransitions).sum();
    }

    // This heuristic penalizes deepness of the holes
    public int getBlockadesTotalVolume() {
        return this.numBlocksInField - this.columnAggregateBottomStackHeight;
    }

    // This heuristic encourages smoothness of the "terrain" (TOP only)
    public int getBumpiness() {
        int bumpiness = 0;
        for (int c = 0; c < COLS - 1; c++) {
            bumpiness += Math.abs(top[c] - top[c + 1]);
        }
        return bumpiness;
    }

    // This heuristic discourages formation of wells
    // Wells is defined as a 1-block wide valley.
    public int getWells() {
        int wellScore = 0;
        int temp;
        if (top[0] < top[1]) {
            temp = top[0] - top[1];
            wellScore += temp * temp;
        }
        for (int c = 1; c < COLS - 1; c++) {
            if (top[c - 1] > top[c] && top[c + 1] > top[c]) {
                temp = Math.min(top[c - 1], top[c + 1]) - top[c];
                wellScore += temp * temp;
            }
        }
        if (top[COLS - 1] < top[COLS - 2]) {
            temp = top[COLS - 1] - top[COLS - 2];
            wellScore += temp * temp;
        }
        return wellScore;
    }

    // The height where the piece is put (= the height of the column + (the height of the piece / 2))
    public double getPrevLandingHeight() {
        return this.landingHeightInPrevMove;
    }
    
    public double getAverageHeightOfCols() {
        return (this.columnAggregateHeight / COLS);
    }

    public int getMeanHeightDifference() {
        // Average of the difference between the height of each col and the mean height of the state
        int meanHeightDifference = 0;
        double average = this.getAverageHeightOfCols();
        for (int c = 0; c < COLS; c++) {
            meanHeightDifference += Math.abs(average - this.top[c]);
        }
        meanHeightDifference = meanHeightDifference / COLS;
        return meanHeightDifference;
    }

    public int getNumEdgesTouchingCeiling() {
        int count = 0;
        for (int c = 0; c < COLS; c++) {
            if (this.top[c] == this.rows) {
                count++;
            }
        }
        return count;
    }

    public int getNumEdgesTouchingTheWall() {
        return this.numBlocksTouchingWall;
    }

    public int getNumEdgesTouchingTheFloor() {
        int count = 0;
        for (int c = 0; c < COLS; c++) {
            if (this.bottom[c] > 0) {
                count++;
            }
        }
        return count;
    }

    public int getField(int row, int col) {
        int fieldIndex = getFieldIndex(row, col);
        Integer cell = field.contains(fieldIndex) ? 1 : 0;
        return (cell == null) ? 0 : cell;
    }


    /////////////////////////////////////////////////////////////////

    public int hasPlayerLost() {
        return this.lost;
    }

    // returns moveUtil
    public void makePlayerMove(int orient, int slot) {
        if (this.nextPiece == -1) {
            throw new IllegalStateException();
        }

        int nextPiece = this.nextPiece;
        this.orient = orient;
        int turn = ++this.turn;
        this.numBlocksInField += P_VOLUME[this.nextPiece];

        int bottomPiece = -1;
        // row corresponding to bottom of piece after falling
        for (int c = 0; c < P_WIDTH[nextPiece][orient]; c++) {
            bottomPiece = Math.max(bottomPiece, this.top[slot + c] - P_BOTTOM[nextPiece][orient][c]);
        }

        this.landingHeightInPrevMove = bottomPiece + ((double)P_HEIGHT[nextPiece][orient] / 2);
        
        // Check if game ended
        if (bottomPiece + P_HEIGHT[nextPiece][orient] > this.rows) {
            this.lost = 1;
            this.nextPiece = -1;
            return;
        }
        
        // If the leftmost col of the current piece touches the wall
        if (slot == 0) {
            this.numBlocksTouchingWall += P_TOP[nextPiece][orient][0] - P_BOTTOM[nextPiece][orient][0];
        }

        // Fill in the appropriate blocks
        // For each column in the piece 
        for (int i = 0; i < P_WIDTH[nextPiece][orient]; i++) {
            
            int colBefore = i + slot - 1;
            int colAfter = i + slot + 1;
            
            // From bottom to top of piece
            for (int h = bottomPiece + P_BOTTOM[nextPiece][orient][i]; h < bottomPiece + P_TOP[nextPiece][orient][i]; h++) {
                this.setField(h, i + slot, turn);
                
                // Calculate col transitions
                int rowBefore = h - 1;
                int rowAfter = h + 1;

                int cellRowBefore = this.getField(rowBefore, i + slot);
                int cellRowAfter = this.getField(rowAfter, i + slot);

                if (rowBefore >= 0) {
                    if (cellRowBefore == 0) {
                        // Col before is empty
                        this.colTransitions[i + slot] += 1;
                    } else if (cellRowBefore > 0) {
                        // Col before is filled
                        this.colTransitions[i + slot] -= 1;
                    }
                }

                if (rowAfter < this.rows) {
                    if (cellRowAfter == 0) {
                        // Col after is empty
                        this.colTransitions[i + slot] += 1;
                    } else if (cellRowAfter > 0) {
                        // Col after is filled
                        this.colTransitions[i + slot] -= 1;
                    }
                }

                // Calculate row transitions
                int cellColBefore = this.getField(h, colBefore);
                int cellColAfter = this.getField(h, colAfter);

                if (colBefore >= 0) {
                    if (cellColBefore == 0) {
                        // Col before is empty
                        this.rowTransitions[h] += 1;
                    } else if (cellColBefore > 0) {
                        // Col before is filled
                        this.rowTransitions[h] -= 1;
                    }
                }

                if (colAfter < COLS) {
                    if (cellColAfter == 0) {
                        // Col after is empty
                        this.rowTransitions[h] += 1;
                    } else if (cellColAfter > 0) {
                        // Col after is filled
                        this.rowTransitions[h] -= 1;
                    }
                }
            }
            // If the rightmost col of the current piece touches the wall
            if (i + slot == COLS - 1) {
                this.numBlocksTouchingWall += P_TOP[nextPiece][orient][i] - P_BOTTOM[nextPiece][orient][i];
            }
        }

        for (int c = 0; c < P_WIDTH[nextPiece][orient]; c++) {
            // Adjust top
            int colIndex = slot + c;
            int newTop = bottomPiece + P_TOP[nextPiece][orient][c];
            this.columnAggregateHeight += newTop - this.top[colIndex];
            this.top[colIndex] = newTop;
            if(newTop > this.maxTop) {
                this.maxTop = newTop;
            }

            // Adjust bottom
            // Checks if the newly placed piece continues the stack from the bottom
            // If there exists a hole in that column, there is no change to bottom[col]
            if (this.bottom[colIndex] + 1 == bottomPiece + P_BOTTOM[nextPiece][orient][c]) {
                int stackHeightChange = P_TOP[nextPiece][orient][c] - P_BOTTOM[nextPiece][orient][c];
                this.columnAggregateBottomStackHeight += stackHeightChange;
                this.bottom[colIndex] += stackHeightChange;
            }
        }

        // Check for full rows and clear them
        for (int r = bottomPiece + P_HEIGHT[nextPiece][orient] - 1; r >= bottomPiece; r--) {
            // Check all columns in the row
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (this.getField(r, c) == 0) {
                    full = false;
                    break;
                }
            }

            // If the row was full - remove it and slide above stuff down
            if (full) {
                this.numBlocksInField -= COLS;
                this.rowsClearedInPrevMove++;
                this.maxTop--;
                this.numBlocksTouchingWall -= 2;

                // Move down row transitions
                for(int i = r; i < this.rows - 1; i++) {
                    this.rowTransitions[i] = this.rowTransitions[i+1];
                }

                // For each column
                for (int c = 0; c < COLS; c++) {
                    // Slide down all bricks
                    for (int i = r; i < this.top[c]; i++) {
                        this.setField(i, c, (i + 1 < this.rows) ? this.getField(i + 1, c) : 0);
                    }

                    // Update col transitions (Only when blocks above and below are empty then the colTransitions change)
                    if (c == 0 && this.getField(r, c + 1) == 0) {
                        this.colTransitions[c] -= 2;
                    } else if (c == COLS - 1 && this.getField(r, c - 1) == 0) {
                        this.colTransitions[c] -= 2;
                    } else if (this.getField(r, c - 1) == 0 && this.getField(r, c + 1) == 0) {
                        this.colTransitions[c] -= 2;
                    }

                    // Lower top
                    this.top[c]--;
                    this.columnAggregateHeight --;
                    while(this.top[c] >= 1 && this.getField(this.top[c] - 1, c) == 0)	{
                        this.top[c]--;
                        this.columnAggregateHeight --;
                    }

                    // Check bottom
                    // If the stack of blocks from the bottom has any blocks that is destroyed by the line clear
                    if (this.bottom[c] >= r) {
                        this.bottom[c]--;
                        this.columnAggregateBottomStackHeight--;
                    }
                }
            }
        }

        this.nextPiece = -1;
        this.rowsCleared += this.rowsClearedInPrevMove;
    }

    public int getStateNextPiece() {
        return this.nextPiece;
    }

    public void setNextPiece(int piece) {
        if (this.nextPiece != -1) {
            throw new IllegalStateException();
        }

        this.nextPiece = piece;
        this.rowsClearedInPrevMove = 0;
    }

    // On player's turn, get legal moves for player as an array of {slot, orient} duples
    public int[][] getLegalPlayerMoves() {
        if (this.nextPiece == -1) {
            throw new IllegalStateException();
        }

        return LEGAL_MOVES[this.nextPiece];
    }

    public int getRandomNextPiece() {
		return (int)(Math.random()*N_PIECES);
	}
    
    // WARNING: Don't put this in hot loop due to its inefficiency! Use only for debugging
    @Override
    public String toString() {
        return String.format(
                "# State \n" + "## field \n" + "%s \n" + "## nextPiece: %d \n" + "## lost: %d \n" + "## turn: %d \n"
                        + "## rowsCleared: %d \n" + "# Derived state \n" + "## top: %s \n" 
                        + "## columnAggregateHeight: %s \n" + "## rowsClearedInPrevMove: %s \n"
                        + "## numBlocksInField: %s \n" + "## maxTop: %s \n" + "## landingHeightInPrevMove: %s"
                        + "## numBlocksTouchingWall: %s \n",
                this.getPrettyPrintFieldString(this.field), this.nextPiece, this.lost, this.turn, this.rowsCleared,
                Arrays.toString(this.top), this.columnAggregateHeight, this.rowsClearedInPrevMove, this.numBlocksInField,
                this.maxTop, this.landingHeightInPrevMove, this.numBlocksTouchingWall
        );
    }

    private String getPrettyPrintFieldString(HashSet<Integer> field) {
        int[][] fieldArray = new int[this.rows][COLS];
        for (int r = this.rows - 1; r >= 0; r --) {
            for (int c = 0; c < COLS; c ++) {
                fieldArray[r][c] = field.contains(this.getFieldIndex(this.rows - 1 - r, c)) ? 1 : 0;
            }
        }
        return String.join("\n", Arrays.stream(fieldArray).map(x -> Arrays.toString(x)).toArray(String[]::new));
    }

    private void setField(int row, int col, int value) {
        int fieldIndex = this.getFieldIndex(row, col);
        if (value == 0) {
            this.field.remove(fieldIndex);
        } else {
            this.field.add(fieldIndex);
        }
    }
    
    private int getFieldIndex(int row, int col) {
        return row * COLS + col;
    }
}
