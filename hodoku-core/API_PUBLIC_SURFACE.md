# hodoku-core public API surface (core features)

This document defines the current public API to treat as external contract for `hodoku-core`.

## Primary app-facing API

- `org.jjgame.hodoku.HodokuDifficultyRater.rate(IntArray)`
  - Input: 81 values in `[0..9]`, `0` for empty.
  - Output: `HodokuRating(solved, difficulty, score)`.

## Generation API

- `generator.SudokuGeneratorFactory`
  - `getDefaultGeneratorInstance()`
  - `getInstance()`
  - `giveBack(SudokuGenerator)`
- `generator.SudokuGenerator`
  - `generateSudoku(boolean symmetric)`
  - `generateSudoku(boolean symmetric, boolean[] pattern)`
  - `getNumberOfSolutions(Sudoku2 sudoku, int maxSolutionCount)`
  - `validSolution(Sudoku2 sudoku)`

## Solving/rating API

- `solver.SudokuSolver`
  - `setSudoku(Sudoku2)`
  - `solve()` / `solve(boolean withGui)` (core should use `withGui=false`)
  - `getHint(Sudoku2, boolean singlesOnly)`
  - `getScore()`
  - `getLevel()`
  - `getSteps()`

## Grid model API

- `sudoku.Sudoku2`
  - `setSudoku(String, boolean)`
  - `getSudoku(ClipboardMode)`
  - Cell/candidate mutation and validation helpers (`setCell`, `setCandidate`, `isSolved`, etc.)

## Compatibility (public but not primary)

These types remain public to preserve existing solver signatures but are not primary app-facing contracts:

- `sudoku.SolverProgressDialog`

