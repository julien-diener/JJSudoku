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
	private int index; // search order when solving
	private SolutionType type; // which step
	private int level; // Index in Options.difficultyLevels
	private SolutionCategory category; // which category (used for configuration)
	private int baseScore; // score for every instance of step in solution
	private int adminScore; // currently not used
	private boolean enabled; // used in solution?
	private boolean allStepsEnabled; // searched for when all steps are found?
	private int indexProgress; // search order when rating the efficiency of steps
	private boolean enabledProgress; // enabled when rating the efficiency of steps
	private boolean enabledTraining; // enabled for traing/practising mode

	/** Creates a new instance of StepConfig */
	public StepConfig(int index, SolutionType type, int level, SolutionCategory category, int baseScore, int adminScore,
			boolean enabled, boolean allStepsEnabled, int indexProgress, boolean enabledProgress,
			boolean enabledTraining) {
		this.index = index;
		this.type = type;
		this.level = level;
		this.category = category;
		this.baseScore = baseScore;
		this.adminScore = adminScore;
		this.enabled = enabled;
		this.allStepsEnabled = allStepsEnabled;
		this.indexProgress = indexProgress;
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

	public static String getLevelName(DifficultyLevel level) {
		// return level.getName();
		return Options.getInstance().getDifficultyLevels()[level.getOrdinal()].getName();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getBaseScore() {
		return baseScore;
	}

	public void setBaseScore(int baseScore) {
		this.baseScore = baseScore;
	}

	public int getAdminScore() {
		return adminScore;
	}

	public void setAdminScore(int adminScore) {
		this.adminScore = adminScore;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public SolutionCategory getCategory() {
		return category;
	}

	public void setCategory(SolutionCategory category) {
		this.category = category;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isAllStepsEnabled() {
		return allStepsEnabled;
	}

	public int getIndexProgress() {
		return indexProgress;
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
