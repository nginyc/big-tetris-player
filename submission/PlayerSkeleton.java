
import java.util.*;
import java.util.function.DoubleSupplier;

public class PlayerSkeleton {
	private GameStateSearcher gameStateSearcher;
	private GameState gameState;
	private IGameStateUtilityFunction utilityFunction;
	private int ROWS = State.ROWS - 1;

	public PlayerSkeleton() {
		this.gameState = new GameState(ROWS);
		this.utilityFunction = toUtilityFunction(
			new double[] { -0.3907454899138355, -0.7076471959178626, -0.9999999978876627, 0.34814812107690407, 0.005918216014713197, -0.506446236591784, 0.010013842808179697, 0.00000000304563703249 }, 
			new int[] { 0, 4, 5, 10, 12, 11, 2, 7 }
		);
		this.gameStateSearcher = new GameStateSearcher(ROWS, utilityFunction);
	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		// Assumption: If `s`'s turn no. is 0, the game has restarted
		if (s.getTurnNumber() == 0) {
			this.gameState = new GameState(ROWS);
		}
		this.gameState.setNextPiece(s.getNextPiece());
		int[] move = this.gameStateSearcher.search(this.gameState);
		// Assumption: `s` will also make the returned move 
		this.gameState.makePlayerMove(move[0], move[1]);
		int legalMoveIndex = 0;
		for (int i = 0; i < legalMoves.length; i ++) {
			if (Arrays.equals(legalMoves[i], move)) {
				legalMoveIndex = i;
			}
		}
		return legalMoveIndex;
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}

	// Given a game state and a utility function, returns best move
	public static class GameStateSearcher {
		private IGameStateUtilityFunction utilityFunction;
		private GameState searchGameState;

		public GameStateSearcher(int rows, IGameStateUtilityFunction utilityFunction) {
			this.utilityFunction = utilityFunction;
			this.searchGameState = new GameState(rows);
		}

		public int[] search(GameState gameState) {
			int[][] legalMoves = gameState.getLegalPlayerMoves();

			int[] bestMove = null;
			double bestMoveUtil = -Double.MAX_VALUE;

			for (int i = 0; i < legalMoves.length; i ++) {
				int[] legalMove = legalMoves[i];
				// Calculate move util
				int orient = legalMove[GameState.ORIENT];
				int slot = legalMove[GameState.SLOT];
				this.searchGameState.restore(gameState);
				this.searchGameState.makePlayerMove(orient, slot);
				double moveUtil = utilityFunction.get(this.searchGameState);
				if (bestMove == null || moveUtil >= bestMoveUtil) {
					bestMove = legalMove;
					bestMoveUtil = moveUtil;
				}
			}

			return bestMove;
		}
	}

	public static interface IGameStateUtilityFunction {
		public double get(GameState gameState);
	}

	public static IGameStateUtilityFunction toUtilityFunction(double[] weights, int[] weightIndices) {
		double[] evalWeights = new double[15];
		Arrays.fill(evalWeights, 0);
		for (int i = 0; i < weightIndices.length; i ++) {
				evalWeights[weightIndices[i]] = weights[i];
		}
		GameStateUtilityFunction utilityFunction = new GameStateUtilityFunction(evalWeights);
		return utilityFunction;
	}

	public static class GameStateUtilityFunction implements IGameStateUtilityFunction {
    private double[] weights;

		public GameStateUtilityFunction(double[] weights) {
			if (weights.length != 15) {
				throw new IllegalArgumentException();
			}

			this.weights = weights;
		}

		private double normalize(double value, double min, double max) {
			return (value - min) / (max - min);
		}

		public double get(GameState gameState) {
			if (gameState.hasPlayerLost() == 1) {
				return -Double.MAX_VALUE;
			}

			int col = GameState.COLS;
			int row = gameState.rows;
				
			// Note: Many of the max values of the heuristics are probably overestimates, because they are the 
			// worst/best cases which are basically almost impossible to attain.
			// Features are wrapped in a function to allow for lazy evaluation (some weights might be passed as 0)
			DoubleSupplier[] features = new DoubleSupplier[] {
				() -> this.normalize(gameState.getColumnAggregateHeight(), 0, col * row),
				() -> this.normalize(gameState.getRowsClearedInPrevMove(), 0, 4),
				() -> this.normalize(gameState.getHolesTotalVolume(), 0, col * (row - 1)),
				() -> this.normalize(gameState.getMaxTopHeight(), 0, row),
				() -> this.normalize(gameState.getBumpiness(), 0, row * row * (col - 1)),
				() -> this.normalize(gameState.getWells(), 0, row * row * (col - 1) * 2),
				() -> this.normalize(gameState.getPrevLandingHeight(), 0, row),
				() -> this.normalize(gameState.getColTransitions(), 0, col * row),
				() -> this.normalize(gameState.getRowTransitions(), 0, col * row),
				() -> this.normalize(gameState.getBlockadesTotalVolume(), 0, col * (row - 1)),
				() -> this.normalize(gameState.getNumBlocksInField(), 0, row * (col - 1)),
				() -> this.normalize(gameState.getMeanHeightDifference(), 0, (double)(row * col * col) / 4),
				() -> this.normalize(gameState.getNumEdgesTouchingTheWall(), 0, row * 2),
				() -> this.normalize(gameState.getNumEdgesTouchingTheFloor(), 0, col),
				() -> this.normalize(gameState.getNumEdgesTouchingCeiling(), 0, col)
			};
				
			double utility = 0;
			for (int i = 0; i < 15; i ++) {
				if (this.weights[i] != 0) {
					utility += this.weights[i] * features[i].getAsDouble();
				}
			}
			return utility;
		}
	}

	public static class GameState {
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
		private int[][] field; // Whether field at index is occupied
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
				this.field = new int[this.rows][COLS];
		}

		private GameState(int rows, int[][] field, int nextPiece, int lost, int turn, int rowsCleared, 
				int[] top, int[] bottom, int columnAggregateHeight, int columnAggregateBottomStackHeight, int rowsClearedInPrevMove, 
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
				int[][] fieldClone = new int[this.rows][COLS];
				int[] topClone = new int[COLS];
				int[] bottomClone = new int[COLS];
				int[] rowTransitionsClone = new int[this.rows];
				int[] colTransitionsClone = new int[COLS];

				for (int c = 0; c < COLS; c ++) {
						topClone[c] = this.top[c];
						bottomClone[c] = this.bottom[c];
						colTransitionsClone[c] = this.colTransitions[c];
				}

				for (int r = 0; r < this.rows; r ++) {
						rowTransitionsClone[r] = this.rowTransitions[r];
				}

				for (int r = 0; r < this.rows; r ++) {
						for (int c = 0; c < COLS; c ++) {
								fieldClone[r][c] = this.field[r][c];
						}
				}

				return new GameState(
						this.rows, fieldClone, this.nextPiece, this.lost, 
						this.turn, this.rowsCleared, topClone, bottomClone,
						this.columnAggregateHeight, this.columnAggregateBottomStackHeight, this.rowsClearedInPrevMove, 
						this.numBlocksInField, this.maxTop, this.landingHeightInPrevMove,
						this.numBlocksTouchingWall, rowTransitionsClone, colTransitionsClone
				);
		}

		/**
		 * Restore from given game state (must be the same no. of rows)
		 */
		public void restore(GameState gameState) {
				if (this.rows != gameState.rows) {
						throw new IllegalArgumentException("Game state to restore should be the same no. of rows!");
				}

				for (int c = 0; c < COLS; c ++) {
						this.top[c] = gameState.top[c];
						this.bottom[c] = gameState.bottom[c];
						this.colTransitions[c] = gameState.colTransitions[c];
				}

				for (int r = 0; r < this.rows; r ++) {
						this.rowTransitions[r] = gameState.rowTransitions[r];
				}

				for (int r = 0; r < this.rows; r ++) {
						for (int c = 0; c < COLS; c ++) {
								this.field[r][c] = gameState.field[r][c];
						}
				}

				this.rows = gameState.rows;
				this.nextPiece = gameState.nextPiece;
				this.turn = gameState.turn;
				this.lost = gameState.lost;
				this.turn = gameState.turn;
				this.rowsCleared = gameState.rowsCleared;
				this.columnAggregateHeight = gameState.columnAggregateHeight;
				this.columnAggregateBottomStackHeight = gameState.columnAggregateBottomStackHeight;
				this.rowsClearedInPrevMove = gameState.rowsClearedInPrevMove;
				this.numBlocksInField = gameState.numBlocksInField;
				this.maxTop = gameState.maxTop;
				this.landingHeightInPrevMove = gameState.landingHeightInPrevMove;
				this.numBlocksTouchingWall = gameState.numBlocksTouchingWall;
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

		public double getMeanHeightDifference() {
				// Average of the difference between the height of each col and the mean height of the state
				double meanHeightDifference = 0;
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
				if (row < 0 || row >= this.rows || col < 0 || col >= COLS) {
						return 0;
				}

				return this.field[row][col];
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

		private String getPrettyPrintFieldString(int[][] field) {
				return String.join("\n", Arrays.stream(this.field).map(x -> Arrays.toString(x)).toArray(String[]::new));
		}

		private void setField(int row, int col, int value) {
				this.field[row][col] = value;
		}
	}
}
