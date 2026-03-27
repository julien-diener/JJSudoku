# hodoku-core

`hodoku-core` is an extraction of HoDoKu's Sudoku engine for reuse in modern projects and UIs.

## Why this module exists

HoDoKu is one of the strongest open-source Sudoku engines for:
- puzzle solving with many advanced techniques,
- puzzle rating by human-style difficulty,
- generation workflows tied to solver-based validation.

However, the original project is no longer actively maintained and ships as a desktop Swing application.
This module starts an extraction effort to keep the solver/generator logic reusable without the original UI.

## Current scope

This module currently contains:
- HoDoKu solver and generator packages (`solver`, `generator`),
- core Sudoku model/config classes from `sudoku`,
- runtime data/resources required by the original engine (`templates.dat`, `intl/*`),
- a Kotlin adapter (`org.jjgame.hodoku.HodokuDifficultyRater`) used by tests and future integration.

This module deliberately removes desktop UI entry points and Swing-heavy panels/dialogs.

## Public API contract

Current core-facing public entry points are documented in `hodoku-core/API_PUBLIC_SURFACE.md`.
Use this contract as the baseline for integration tests and future refactoring.

## What was removed

- Swing-based dialog/panel/frame classes from the original `sudoku` package,
- desktop launcher/console classes,
- UI glue that only made sense in the original HoDoKu application shell.

See `hodoku-core/SOURCE_INVENTORY.md` for complete kept/removed file lists.

## Compatibility notes

The extraction currently prioritizes keeping algorithm behavior while removing UI coupling.
Some original types are still referenced by algorithm classes, so a few files were replaced by minimal non-UI compatibility stubs:
- `sudoku/FindAllStepsProgressDialog.java`
- `sudoku/GenerateSudokuProgressDialog.java`
- `sudoku/SolverProgressDialog.java`
- `sudoku/SudokuUtil.java`

These stubs keep method signatures used by solver/generator code paths and are intentionally no-op for UI behavior.

## Provenance and license

- Upstream source: HoDoKu (Bernhard Hobiger), GPL-3.0-or-later
- This module is derived work from HoDoKu source files.
- Keep HoDoKu copyright and GPL notices in retained source files.

## Verification

The current extraction is validated by running module tests:

```bash
cd /path/to/JJSudoku
./gradlew :hodoku-core:test --no-daemon
```

## Optional probe tool

To sample natural generation difficulty distribution and timing (without test assertions):

```bash
cd /path/to/JJSudoku
./gradlew :hodoku-core:probeGenerationDistribution --args="100"
```

## Hint dataset POC

`hodoku-core` now includes a second probe that mines solver steps into a training-oriented dataset.

- output format: JSON Lines (`.jsonl`)
- grouping: `<outputDir>/<SolutionCategory>/<SolutionType>.jsonl`
- one line = one pre-step hint sample

Default run:

```bash
cd /path/to/JJSudoku
./gradlew :hodoku-core:probeHintDataset
```

Custom run example:

```bash
cd /path/to/JJSudoku
./gradlew :hodoku-core:probeHintDataset --args="--outputDir=build/hint-dataset --difficulty=HARD --perType=30 --maxPuzzles=1000 --types=HIDDEN_SINGLE,LOCKED_CANDIDATES_1,X_WING"
```

Current schema fields include:
- `sourcePuzzle`: original puzzle grid (1-9 or `.` for empty)
- `puzzleBeforeStep`: puzzle state immediately before the hint was applied
- `solutionValue`: for SET actions, the digit (1-9) that should be placed
- `puzzleAfterStep`: puzzle state after applying the hint
- `sourceDifficulty`, `solutionCategory`, `solutionType`
- `action`: `SET` (place digit) or `ELIMINATION` (remove candidate)
- `targetCell`: cell index (0-80) affected
- `targetValue`: digit (1-9) involved
- `eliminations`: list of `{index, value}` pairs to eliminate
- `candidateCountBefore`: candidate count before the hint
- `stepText`: human-readable hint description

