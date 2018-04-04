# Big Tetris Agent

## Scripts

### Running the Player (Runs `PlayerSkeleton.java`)

```sh
./gradlew run
```

### Training the Player (Runs `PlayerTrainer.java`)

```sh
./gradlew train
```

### Evaluating Utility Functions (Runs `Evaluator.java`)

```sh
./gradlew evaluate
```

### Evaluating Learners of Utility Functions (Runs `LearnerEvaluator.java`)

```sh
./gradlew evaluateLearner
```

## Results

### Learned Utility Functions

#### Simple Genetic Algorithm

Configurations:

- 15 features used (in order): column aggregate height, rows cleared in move, total volume of holes, maximum top height, bumpiness, wells, landing height for move, column transitions, row transitions, total volume of blockades, blocks in field, mean height difference, edges touching the wall, edges touching the floor, edges touching the ceiling
- Tournament selection with tournment ratio of 0.5, selection fraction of 0.8
- Weighted crossover
- Gaussian mutation with per-weight mutation probability of 0.1 for all individuals in a generation
- Fitness function is the average rows cleared over 10 20-row games

Performance:

In one of the runs, found the following best weights at generation 15:

[-0.645997508898601, 0.4045944882880365, -0.860815244477992, 0.056902958678377524, -0.030407170091654185, -0.168514048818137, -0.22487312098130158, -0.3756294363492263, -0.21313614910277484, -0.007625462874487815, 0.5995176907694355, 0.17623189330652983, 0.199920798422004, -0.3989981025533119, -0.6743078938370471]

It is evaluated to clear 2647426 rows over 100 tries, with a peak of 11327331 rows in a game.

#### Genetic Algorithm with Hill-Climbing

Configurations:

- 6 features used (in order), normalized: column aggregate height, bumpiness, wells, blocks in field, edges touching the wall, mean height difference
- Tournament selection with tournament size of 2, selection fraction of 0.8
- Weighted crossover
- Gaussian mutation with probability of 0.1 to mutate a single weight for only children in a generation
- 10-attempt hill-climbing on the best individual in a generation
- Fitness function is the average rows cleared over 10 20-row games

Performance:

In one of the runs, found the following best weights at generation 30:

[-0.5492628540570825, -1.0, -0.9999921480184876, 0.4833128587233925, 0.00987285705521266, -0.7972235992021071]

It is evaluated to clear 5459051.95 rows over 100 tries, with a peak of 25113599 rows in a game.

In one of the runs, found the following best weights at generation 30:

[-0.3907454916830745, -0.811413075782089, -1.0, 0.3531714849383103, 0.0059182152605956875, -0.5170695909853636]

It is evaluated to clear 15634965.25 rows over 100 tries, with a peak of 119448005 in a game.

#### Genetic Algorithm with Hill-Climbing (8 features)

Configurations:

- 8 features used (in order), normalized: column aggregate height, bumpiness, wells, blocks in field, edges touching the wall, mean height difference, holes, column transitions
- Tournament selection with tournament size of 2, selection fraction of 0.8
- Weighted crossover
- Gaussian mutation with probability of 0.1 to mutate a single weight for only children in a generation
- 10-attempt hill-climbing on the best individual in a generation
- Fitness function is the average rows cleared over 10 20-row games

Performance:

In one of the runs, found the following best weights at generation 102:

[-0.18058087213646176, -1.0, -0.9859493187639575, 0.15454976336975096, 0.009062094933315022, -0.4302022682880107, -0.2921079786301265, 0.008874001359701303]

It is evaluated to clear 9318571.49 rows over 100 tries, with a peak of 48228450 rows in a game.

### Speed Optimisations

We made certain optimisations in our code to speed up the search for, and evaluation of, a set of weights for the Tetris utility function to find the best one. So we don't need to wait forever to get our results.

#### Fast-Features

What we did: We reduced the time complexity of computing each feature by maintaining necessary derived-state variables within our custom abstraction of a Tetris game state `GameState`.

Before: // TODO

After: // TODO

Speedup: // TODO

Conditions: // TODO

#### Parallel-Simulation-Eval

What we did: We spawned multiple threads to simulate multiple Tetris games concurrently in the evaluation of a single set of weights.

Before: // TODO

After: // TODO

Speedup: // TODO

Conditions: // TODO

#### Parallel-Fitness-Eval

What we did: In our genetic algorithm, we spawned multiple threads to evaluate the fitness of multiple individuals (i.e. sets of weights) concurrently within a generation.

Before: // TODO

After: // TODO

Speedup: // TODO

Conditions: // TODO

#### No-Clone

What we did: We avoided cloning of `GameState` for each move during the 1-layer game state search (no parallelisation), instead reusing a single `GameState` construct over all searches instead.

Before: Able to clear 27752676 rows in 31m 26s (1886s) => 14715 rows / s

After: Able to clear 36316541 rows in 17min 50s (1070s) => 33940 rows / s

Speedup: 2.31x

Conditions: Intel(R) Core(TM) i7-7700HQ CPU @ 2.80GHz, 2801 Mhz, 4 Core(s), 8 Logical Processor(s)

### Search Optimisations

We made certain optimisations in our code in an attempt to increase the possibility of finding the optimal Tetris utility function.

#### Feature-Subset-Selection

What we did: From 15 features, we narrowed down to 8 important features using greedy forward selection of features. To heavily cut the computation time required for the evaluation of whether a set of features is more important than another, we used the number of rows cleared for a 10-row Tetris game as a proxy to that for a 20-row Tetris game. We first evaluated which single feature can produce a utility function that can clear the most rows over 10 tries of a 10-row Tetris game, then we successively added more features to the utility function greedily based on their improvement on the number of rows cleared.

Motivation: We started with 15 features identified from our research, but the resultant search space was too huge, leading to repeated runs of the genetic algorithm without any good output sets of weights.

Features that we included, in descending order of importance:

1) Column aggregate height
2) Bumpiness
3) No. of wells
4) No. of blocks in field
5) No. of edges touching wall
6) Mean height difference
7) No. of holes
8) No. of column transitions

#### Genetic-Hill-Climbing

What we did: In our genetic algorithm, we applied hill-climbing on the best-performing individual at the end of every generation (with reference to https://www.hindawi.com/journals/jam/2013/103591/).

Motivation: We found that the genetic algorithm was slow in converging to a locally optimal set of weights upon the discovery of a new "best" individual due to mutation and crossover. If we increased the selection pressure (i.e. the rate at which "weaker" individuals are eliminated in favour of "stronger" ones), it would increase the convergence to a local optimum at the heavy cost of exploration and discovery of the true global optimum. Such a hybridised genetic algorithm marries the strengths of these search algorithms.
