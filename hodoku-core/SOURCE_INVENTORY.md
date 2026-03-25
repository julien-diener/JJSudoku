# HoDoKu source inventory

This document tracks exactly what was imported from HoDoKu and what was removed during the non-UI extraction and desktop-coupling cleanup.

## Summary

- Original HoDoKu Java files: **108**
- Kept in `hodoku-core`: **57**
- Removed from `hodoku-core`: **51**
- Added brand-new files: **0** (all retained paths come from original HoDoKu paths)

## Kept files with notable changes

Most kept files are byte-identical to upstream HoDoKu.
The following kept files were intentionally replaced with non-UI compatibility implementations:

| File | Change note |
|---|---|
| `sudoku/FindAllStepsProgressDialog.java` | Replaced with minimal no-op progress callback stub |
| `sudoku/GenerateSudokuProgressDialog.java` | Replaced with minimal no-op progress callback stub |
| `sudoku/SolutionPanel.java` | Replaced with minimal state-bridge stub for `GuiState` |
| `sudoku/SolverProgressDialog.java` | Replaced with non-Swing wrapper used by `SudokuSolver.solve(boolean)` |
| `sudoku/SudokuPanel.java` | Replaced with minimal state-bridge stub for `GuiState` |
| `sudoku/SudokuUtil.java` | Replaced with non-UI utility subset (`clearStepList*`, `combinations`, no-op look-and-feel) |
| `generator/BackgroundGenerator.java` | Removed AWT `EventQueue` usage; progress callbacks are now direct |
| `sudoku/DifficultyLevel.java` | Removed AWT `Color` fields; now stores only type/maxScore/name |
| `sudoku/GuiState.java` | Removed AWT `Color` types from coloring maps (uses integer color ids) |
| `sudoku/Options.java` | Removed desktop serialization/font/color dependencies; kept solver configuration |

## Full kept file list

```text
generator/BackgroundGenerator.java
generator/BackgroundGeneratorThread.java
generator/GeneratorPattern.java
generator/SudokuGenerator.java
generator/SudokuGeneratorFactory.java
solver/AbstractSolver.java
solver/Als.java
solver/AlsSolver.java
solver/BruteForceSolver.java
solver/ChainSolver.java
solver/ColoringSolver.java
solver/FishSolver.java
solver/GiveUpSolver.java
solver/GroupNode.java
solver/IncompleteSolver.java
solver/MiscellaneousSolver.java
solver/RestrictedCommon.java
solver/SimpleSolver.java
solver/SingleDigitPatternSolver.java
solver/SudokuSolver.java
solver/SudokuSolverFactory.java
solver/SudokuStepFinder.java
solver/TableEntry.java
solver/TablingSolver.java
solver/TemplateSolver.java
solver/UniquenessSolver.java
solver/WingSolver.java
sudoku/AlsInSolutionStep.java
sudoku/Candidate.java
sudoku/Chain.java
sudoku/ClipboardMode.java
sudoku/DifficultyLevel.java
sudoku/DifficultyType.java
sudoku/Entity.java
sudoku/FindAllStepsProgressDialog.java
sudoku/GameMode.java
sudoku/GenerateSudokuProgressDialog.java
sudoku/GuiState.java
sudoku/ListDragAndDropChange.java
sudoku/Options.java
sudoku/RegressionTester.java
sudoku/SolutionCategory.java
sudoku/SolutionPanel.java
sudoku/SolutionStep.java
sudoku/SolutionType.java
sudoku/SolverProgressDialog.java
sudoku/StepConfig.java
sudoku/Sudoku.java
sudoku/Sudoku2.java
sudoku/SudokuCell.java
sudoku/SudokuPanel.java
sudoku/SudokuSet.java
sudoku/SudokuSetBase.java
sudoku/SudokuSetShort.java
sudoku/SudokuSinglesQueue.java
sudoku/SudokuStatus.java
sudoku/SudokuUtil.java
```

## Full removed file list

```text
sudoku/AboutDialog.java
sudoku/AllStepsPanel.java
sudoku/BackdoorSearchDialog.java
sudoku/CellZoomPanel.java
sudoku/CheckNode.java
sudoku/CheckRenderer.java
sudoku/ConfigColorPanel.java
sudoku/ConfigColorkuPanel.java
sudoku/ConfigDialog.java
sudoku/ConfigFindAllStepsPanel.java
sudoku/ConfigGeneralPanel.java
sudoku/ConfigGeneratorPanel.java
sudoku/ConfigLevelFontPanel.java
sudoku/ConfigProgressPanel.java
sudoku/ConfigSolverPanel.java
sudoku/ConfigStepPanel.java
sudoku/ConfigTrainigPanel.java
sudoku/ConfigTrainingDialog.java
sudoku/ExtendedPrintDialog.java
sudoku/ExtendedPrintProgressDialog.java
sudoku/FileDrop.java
sudoku/FindAllSteps.java
sudoku/FishChooseCandidatesDialog.java
sudoku/GeneratorPatternPanel.java
sudoku/HistoryDialog.java
sudoku/KeyboardLayoutFrame.java
sudoku/ListDragAndDrop.java
sudoku/Main.java
sudoku/MainFrame.java
sudoku/MyBrowserLauncher.java
sudoku/MyFontChooser.java
sudoku/NumbersOnlyDocument.java
sudoku/PrintSolutionDialog.java
sudoku/ProgressChecker.java
sudoku/RestoreSavePointDialog.java
sudoku/RightClickMenu.java
sudoku/ColorKuImage.java
sudoku/SetGivensDialog.java
sudoku/SplitPanel.java
sudoku/StatusColorPanel.java
sudoku/SudokuConsoleFrame.java
sudoku/SummaryPanel.java
sudoku/RelativeLayout.java
sudoku/UIBorderedImagePanel.java
sudoku/UIColorPalette.java
sudoku/UIColorTools.java
sudoku/UIExportLine.java
sudoku/UIImportLine.java
sudoku/UIQuickBrowse.java
sudoku/UIToggleButton.java
sudoku/WriteAsPNGDialog.java
```

## Runtime resources kept for core

Only these HoDoKu bundles are still required by current core code paths:

```text
templates.dat
intl/SolutionStep.properties
intl/SolutionStep_de.properties
intl/SolutionType.properties
intl/SolutionType_de.properties
```

Other `intl/*.properties` files from desktop dialogs/panels were removed as unused by solver/generator/rating in `hodoku-core`.


