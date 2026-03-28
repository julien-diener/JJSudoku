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

package sudoku;

/**
 *
 * @author hobiwan
 */
public final class StepConfig implements Cloneable, Comparable<StepConfig> {
	private final int index; // search order when solving
	private final SolutionType type; // which step
	private final DifficultyType difficultyType; // Index in Options.difficultyLevels
	private final SolutionCategory category; // which category (used for configuration)
	private final int baseScore; // score for every instance of step in solution
	private final int adminScore; // currently not used
	private final boolean enabled; // used in solution?
	private final boolean allStepsEnabled; // searched for when all steps are found?
	private final boolean enabledProgress; // enabled when rating the efficiency of steps
	private final boolean enabledTraining; // enabled for traing/practising mode

	/** Creates a new instance of StepConfig */
	public StepConfig(DifficultyType difficultyType, SolutionCategory category, SolutionType type, int index, int baseScore, int adminScore,
					  boolean enabled, boolean allStepsEnabled, boolean enabledProgress,
					  boolean enabledTraining) {
		this.difficultyType = difficultyType;
		this.category = category;
		this.type = type;
		this.index = index;
		this.baseScore = baseScore;
		this.adminScore = adminScore;
		this.enabled = enabled;
		this.allStepsEnabled = allStepsEnabled;
		this.enabledProgress = enabledProgress;
		this.enabledTraining = enabledTraining;
	}

	@Override
	public String toString() {
		return type.getStepName();
	}

	public SolutionType getType() {
		return type;
	}

	public DifficultyType getDifficultyType() {
		return difficultyType;
	}
	public int getLevel() {
		return difficultyType.ordinal();
	}

	public int getBaseScore() {
		return baseScore;
	}

	public int getAdminScore() {
		return adminScore;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public SolutionCategory getCategory() {
		return category;
	}

	public int getIndex() {
		return index;
	}


	public boolean isAllStepsEnabled() {
		return allStepsEnabled;
	}

	public boolean isEnabledProgress() {
		return enabledProgress;
	}

	/**
	 * @return the enabledTraining
	 */
	public boolean isEnabledTraining() {
		return enabledTraining;
	}

	@Override
	public int compareTo(StepConfig o) {
		return index - o.getIndex();
	}
}
