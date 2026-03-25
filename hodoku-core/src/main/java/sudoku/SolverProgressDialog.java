package sudoku;

import solver.SudokuSolver;

/**
 * Minimal non-UI placeholder used by solver GUI entry points.
 */
public class SolverProgressDialog {
    private final Thread thread;
    private volatile boolean visible;
    private volatile boolean solved;

    public SolverProgressDialog(Object parent, boolean modal, SudokuSolver solver) {
        this.thread = new Thread(() -> solved = solver.solve(), "hodoku-core-solver");
        this.thread.start();
    }

    public Thread getThread() {
        return thread;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isSolved() {
        return solved;
    }

    public void initializeProgressState(int candidateCount) {
        // no-op in core mode
    }

    public void setProgressState(int unsolvedCells, int unsolvedCandidates) {
        // no-op in core mode
    }

    public void resetFishProgressBar(int max) {
        // no-op in core mode
    }

    public void updateFishProgressBar(int current) {
        // no-op in core mode
    }
}

