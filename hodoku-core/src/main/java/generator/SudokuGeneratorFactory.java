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

package generator;

/**
 * Minimal factory wrapper for non-UI/core usage.
 * 
 * @author hobiwan
 */
public class SudokuGeneratorFactory {
	/** The <b>defaultGenerator</b> for use by the GUI. */
	private static final SudokuGenerator defaultGenerator = new SudokuGenerator();

	/**
	 * This class is a utility class that cannot be instantiated.
	 */
	private SudokuGeneratorFactory() {
		/* class cannot be instantiated! */ }

	/**
	 * Get the {@link #defaultGenerator}.
	 * 
	 * @return
	 */
	public static SudokuGenerator getDefaultGeneratorInstance() {
		return defaultGenerator;
	}

	/**
	 * Hand out an ununsed generator or create a new one if necessary.
	 * 
	 * @return
	 */
	public static SudokuGenerator getInstance() {
		return new SudokuGenerator();
	}

	/**
	 * Gives a generator back to the factory.
	 * 
	 * @param generator
	 */
	public static void giveBack(SudokuGenerator generator) {
		// no-op in core mode
	}
}
