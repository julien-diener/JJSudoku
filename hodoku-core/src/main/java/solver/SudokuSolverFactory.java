/*
 * Copyright (C) 2008-12  Bernhard Hobiger
 *
 * This file is part of HoDoKu.
 *
 * HoDoKu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HoDoKu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HoDoKu. If not, see <http://www.gnu.org/licenses/>.
 */

package solver;

/**
 * Minimal factory wrapper for non-UI/core usage.
 * 
 * @author hobiwan
 */
public class SudokuSolverFactory {
	/** The <b>defaultSolver</b> for use by the GUI. */
	private static final SudokuSolver defaultSolver = new SudokuSolver();

	/**
	 * This class is a utility class that cannot be instantiated.
	 */
	private SudokuSolverFactory() {
		/* class cannot be instantiated! */ }

	/**
	 * Get the {@link #defaultSolver}.
	 * 
	 * @return
	 */
	public static SudokuSolver getDefaultSolverInstance() {
		return defaultSolver;
	}

	/**
	 * Hand out an ununsed solver or create a new one if necessary.
	 * 
	 * @return
	 */
	public static SudokuSolver getInstance() {
		return new SudokuSolver();
	}

	/**
	 * Gives a solver back to the factory.
	 * 
	 * @param solver
	 */
	public static void giveBack(SudokuSolver solver) {
		// no-op in core mode
	}
}
