# Big Tetris Agent

Our project aims to create an utility-based Tetris agent whose goal is to maximize the number of lines cleared in a game of Tetris. Our agent uses a heuristic function that considers multiple features of the Tetris game state, and the best heuristic function was learned through Tetris game simulations with a Genetic Algorithm (GA). Our agent achieved a 100-game average of 26.7 million rows cleared with a peak of 124.6 million rows cleared in a single game. In the process, we have developed a Tetris agent learning approach that has been carefully optimised to produce better agent performance within a shorter amount of time.

Refer to our [final report](./Final_Report.pdf) for a more in-depth discussion of our project.

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


