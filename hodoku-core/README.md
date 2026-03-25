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
- `sudoku/SolutionPanel.java`
- `sudoku/SolverProgressDialog.java`
- `sudoku/SudokuPanel.java`
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

