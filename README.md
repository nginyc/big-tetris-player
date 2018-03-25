# Big Tetris Agent

## Running the Player

```sh
./gradlew run
```

## Training the Player

```sh
./gradlew train
```

## Results

### Optimizations

#### No-Clone Speedup

Conditions: Intel(R) Core(TM) i7-7700HQ CPU @ 2.80GHz, 2801 Mhz, 4 Core(s), 8 Logical Processor(s)

Optimization: Avoid cloning of GameState for each move during the 1-layer game state search (no parallelization), reusing a single GameState construct over all searches instead.

Before: Able to clear 27752676 rows in 31m 26s (1886s) => 14715 rows / s
After: Able to clear 36316541 rows in 17min 50s (1070s) => 33940 rows / s

Speedup = 2.31x