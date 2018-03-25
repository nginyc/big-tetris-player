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

### Learned Utility Functions

#### Simple Genetic Algorithm

Configurations:

- 15 features used (in order): column aggregate height, rows cleared in move, total volume of holes, maximum top height, bumpiness, wells, landing height for move, column transitions, row transitions, total volume of blockades, blocks in field, mean height difference, edges touching the wall, edges touching the floor, edges touching the ceiling
- Tournament selection with tournment ratio of 0.5, selection fraction of 0.8
- Weighted crossover
- Gaussian mutation with per-weight mutation probability of 0.1
- Fitness function is the average rows cleared over 10 20-row games

Performance:

In one of the runs, found the following best weights at generation 15:

[-0.645997508898601, 0.4045944882880365, -0.860815244477992, 0.056902958678377524, -0.030407170091654185, -0.168514048818137, -0.22487312098130158, -0.3756294363492263, -0.21313614910277484, -0.007625462874487815, 0.5995176907694355, 0.17623189330652983, 0.199920798422004, -0.3989981025533119, -0.6743078938370471]

It is evaluated to clear 2647426 rows over 100 tries, with a peak of 11327331 rows in a game.

### Optimizations

#### No-Clone

Optimization: Avoid cloning of GameState for each move during the 1-layer game state search (no parallelization), reusing a single GameState construct over all searches instead.

Before: Able to clear 27752676 rows in 31m 26s (1886s) => 14715 rows / s
After: Able to clear 36316541 rows in 17min 50s (1070s) => 33940 rows / s
Speedup = 2.31x

Conditions: Intel(R) Core(TM) i7-7700HQ CPU @ 2.80GHz, 2801 Mhz, 4 Core(s), 8 Logical Processor(s)
