package sudoku;

import java.util.List;

/**
 * Utility helpers kept from HoDoKu API surface but stripped from UI dependencies.
 */
public final class SudokuUtil {
    private SudokuUtil() {
    }

    public static void clearStepList(List<SolutionStep> steps) {
        if (steps != null) {
            steps.clear();
        }
    }

    public static void clearStepListWithNullify(List<SolutionStep> steps) {
        clearStepList(steps);
    }

    public static long combinations(int n, int k) {
        if (k < 0 || n < 0 || k > n) {
            return 0;
        }
        if (k == 0 || k == n) {
            return 1;
        }
        int effectiveK = Math.min(k, n - k);
        long result = 1;
        for (int i = 1; i <= effectiveK; i++) {
            result = (result * (n - effectiveK + i)) / i;
        }
        return result;
    }

    public static void setLookAndFeel() {
        // no-op in core mode
    }
}

