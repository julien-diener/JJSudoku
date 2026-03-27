/*
 * Copyright (C) 2019-20  PseudoFish
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

import generator.BackgroundGeneratorThread;
import generator.GeneratorPattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author hobiwan
 */

/*
 * Important Note: the serializer requires a very specific naming convention for
 * the setters and getters to function properly; otherwise, it will not read/write
 * the proper values. Say we have: boolean singleClickMode, you must create a
 * getter with the 'is' prefix as such: public boolean isSingleClickMode();
 * Likewise with the setter, you must have a 'set' prefix.
 */

public final class Options {

	private static final ProgressComparator progressComparator = new ProgressComparator();
	
	// Difficulty levels
	public static final DifficultyLevel[] DEFAULT_DIFFICULTY_LEVELS = {
			new DifficultyLevel(DifficultyType.INCOMPLETE, 0, "Incomplete"),
			new DifficultyLevel(DifficultyType.EASY, 800, "Easy"),
			new DifficultyLevel(DifficultyType.MEDIUM, 1000, "Medium"),
			new DifficultyLevel(DifficultyType.HARD, 1600, "Hard"),
			new DifficultyLevel(DifficultyType.UNFAIR, 1800, "Unfair"),
			new DifficultyLevel(DifficultyType.EXTREME, Integer.MAX_VALUE, "Extreme") };

	private DifficultyLevel[] difficultyLevels = null;
	// Order and configuration of solver steps
	// IMPORTANT: New solver steps must be added at the end of the array.
	// The position is determined by "index".
	public static final StepConfig[] DEFAULT_SOLVER_STEPS = {
			new StepConfig(Integer.MAX_VALUE - 1, SolutionType.INCOMPLETE, DifficultyType.INCOMPLETE.ordinal(),
					SolutionCategory.LAST_RESORT, 0, 0, false, false, Integer.MAX_VALUE - 1, false, false),
			new StepConfig(Integer.MAX_VALUE, SolutionType.GIVE_UP, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.LAST_RESORT, 20000, 0, true, false, Integer.MAX_VALUE, true, false),
			new StepConfig(100, SolutionType.FULL_HOUSE, DifficultyType.EASY.ordinal(), SolutionCategory.SINGLES, 4, 0,
					true, true, 100, true, false),
			new StepConfig(200, SolutionType.NAKED_SINGLE, DifficultyType.EASY.ordinal(), SolutionCategory.SINGLES, 4,
					0, true, true, 200, true, false),
			new StepConfig(300, SolutionType.HIDDEN_SINGLE, DifficultyType.EASY.ordinal(), SolutionCategory.SINGLES, 14,
					0, true, true, 300, true, false),
			new StepConfig(1000, SolutionType.LOCKED_PAIR, DifficultyType.MEDIUM.ordinal(),
					SolutionCategory.INTERSECTIONS, 40, 0, true, true, 1000, true, false),
			new StepConfig(1100, SolutionType.LOCKED_TRIPLE, DifficultyType.MEDIUM.ordinal(),
					SolutionCategory.INTERSECTIONS, 60, 0, true, true, 1100, true, false),
			// new StepConfig(1200, SolutionType.LOCKED_CANDIDATES,
			// DifficultyType.MEDIUM.ordinal(), SolutionCategory.INTERSECTIONS, 50, 0, true,
			// true, 1200, true, false),
			new StepConfig(1200, SolutionType.LOCKED_CANDIDATES_1, DifficultyType.MEDIUM.ordinal(),
					SolutionCategory.INTERSECTIONS, 50, 0, true, true, 1200, true, false),
			new StepConfig(1300, SolutionType.NAKED_PAIR, DifficultyType.MEDIUM.ordinal(), SolutionCategory.SUBSETS, 60,
					0, true, true, 1300, true, false),
			new StepConfig(1400, SolutionType.NAKED_TRIPLE, DifficultyType.MEDIUM.ordinal(), SolutionCategory.SUBSETS,
					80, 0, true, true, 1400, true, false),
			new StepConfig(1500, SolutionType.HIDDEN_PAIR, DifficultyType.MEDIUM.ordinal(), SolutionCategory.SUBSETS,
					70, 0, true, true, 1500, true, false),
			new StepConfig(1600, SolutionType.HIDDEN_TRIPLE, DifficultyType.MEDIUM.ordinal(), SolutionCategory.SUBSETS,
					100, 0, true, true, 1600, true, false),
			new StepConfig(2000, SolutionType.NAKED_QUADRUPLE, DifficultyType.HARD.ordinal(), SolutionCategory.SUBSETS,
					120, 0, true, true, 2000, true, false),
			new StepConfig(2100, SolutionType.HIDDEN_QUADRUPLE, DifficultyType.HARD.ordinal(), SolutionCategory.SUBSETS,
					150, 0, true, true, 2100, true, false),
			new StepConfig(2200, SolutionType.X_WING, DifficultyType.HARD.ordinal(), SolutionCategory.BASIC_FISH, 140,
					0, true, false, 2200, false, false),
			new StepConfig(2300, SolutionType.SWORDFISH, DifficultyType.HARD.ordinal(), SolutionCategory.BASIC_FISH,
					150, 0, true, false, 2300, false, false),
			new StepConfig(2400, SolutionType.JELLYFISH, DifficultyType.HARD.ordinal(), SolutionCategory.BASIC_FISH,
					160, 0, true, false, 2400, false, false),
			new StepConfig(2500, SolutionType.SQUIRMBAG, DifficultyType.UNFAIR.ordinal(), SolutionCategory.BASIC_FISH,
					470, 0, false, false, 2500, false, false),
			new StepConfig(2600, SolutionType.WHALE, DifficultyType.UNFAIR.ordinal(), SolutionCategory.BASIC_FISH, 470,
					0, false, false, 2600, false, false),
			new StepConfig(2700, SolutionType.LEVIATHAN, DifficultyType.UNFAIR.ordinal(), SolutionCategory.BASIC_FISH,
					470, 0, false, false, 2700, false, false),
			new StepConfig(2800, SolutionType.REMOTE_PAIR, DifficultyType.HARD.ordinal(),
					SolutionCategory.CHAINS_AND_LOOPS, 110, 0, true, true, 2800, false, false),
			new StepConfig(2900, SolutionType.BUG_PLUS_1, DifficultyType.HARD.ordinal(), SolutionCategory.UNIQUENESS,
					100, 0, true, true, 2900, false, false),
			new StepConfig(3000, SolutionType.SKYSCRAPER, DifficultyType.HARD.ordinal(),
					SolutionCategory.SINGLE_DIGIT_PATTERNS, 130, 0, true, true, 3000, false, false),
			new StepConfig(3200, SolutionType.W_WING, DifficultyType.HARD.ordinal(), SolutionCategory.WINGS, 150, 0,
					true, true, 3200, false, false),
			new StepConfig(3100, SolutionType.TWO_STRING_KITE, DifficultyType.HARD.ordinal(),
					SolutionCategory.SINGLE_DIGIT_PATTERNS, 150, 0, true, true, 3100, false, false),
			new StepConfig(3300, SolutionType.XY_WING, DifficultyType.HARD.ordinal(), SolutionCategory.WINGS, 160, 0,
					true, true, 3300, false, false),
			new StepConfig(3400, SolutionType.XYZ_WING, DifficultyType.HARD.ordinal(), SolutionCategory.WINGS, 180, 0,
					true, true, 3400, false, false),
			new StepConfig(3500, SolutionType.UNIQUENESS_1, DifficultyType.HARD.ordinal(), SolutionCategory.UNIQUENESS,
					100, 0, true, true, 3500, false, false),
			new StepConfig(3600, SolutionType.UNIQUENESS_2, DifficultyType.HARD.ordinal(), SolutionCategory.UNIQUENESS,
					100, 0, true, true, 3600, false, false),
			new StepConfig(3700, SolutionType.UNIQUENESS_3, DifficultyType.HARD.ordinal(), SolutionCategory.UNIQUENESS,
					100, 0, true, true, 3700, false, false),
			new StepConfig(3800, SolutionType.UNIQUENESS_4, DifficultyType.HARD.ordinal(), SolutionCategory.UNIQUENESS,
					100, 0, true, true, 3800, false, false),
			new StepConfig(3900, SolutionType.UNIQUENESS_5, DifficultyType.HARD.ordinal(), SolutionCategory.UNIQUENESS,
					100, 0, true, true, 3900, false, false),
			new StepConfig(4000, SolutionType.UNIQUENESS_6, DifficultyType.HARD.ordinal(), SolutionCategory.UNIQUENESS,
					100, 0, true, true, 4000, false, false),
			new StepConfig(4100, SolutionType.FINNED_X_WING, DifficultyType.HARD.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 130, 0, true, false, 4100, false, false),
			new StepConfig(4200, SolutionType.SASHIMI_X_WING, DifficultyType.HARD.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 150, 0, true, false, 4200, false, false),
			new StepConfig(4300, SolutionType.FINNED_SWORDFISH, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 200, 0, true, false, 4300, false, false),
			new StepConfig(4400, SolutionType.SASHIMI_SWORDFISH, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 240, 0, true, false, 4400, false, false),
			new StepConfig(4500, SolutionType.FINNED_JELLYFISH, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 250, 0, true, false, 4500, false, false),
			new StepConfig(4600, SolutionType.SASHIMI_JELLYFISH, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 260, 0, true, false, 4600, false, false),
			new StepConfig(4700, SolutionType.FINNED_SQUIRMBAG, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 470, 0, false, false, 4700, false, false),
			new StepConfig(4800, SolutionType.SASHIMI_SQUIRMBAG, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 470, 0, false, false, 4800, false, false),
			new StepConfig(4900, SolutionType.FINNED_WHALE, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 470, 0, false, false, 4900, false, false),
			new StepConfig(5000, SolutionType.SASHIMI_WHALE, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 470, 0, false, false, 5000, false, false),
			new StepConfig(5100, SolutionType.FINNED_LEVIATHAN, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 470, 0, false, false, 5100, false, false),
			new StepConfig(5200, SolutionType.SASHIMI_LEVIATHAN, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_BASIC_FISH, 470, 0, false, false, 5200, false, false),
			new StepConfig(5300, SolutionType.SUE_DE_COQ, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.MISCELLANEOUS, 250, 0, true, true, 5300, false, false),
			new StepConfig(5400, SolutionType.X_CHAIN, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.CHAINS_AND_LOOPS, 260, 0, true, true, 5400, false, false),
			new StepConfig(5500, SolutionType.XY_CHAIN, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.CHAINS_AND_LOOPS, 260, 0, true, true, 5500, false, false),
			new StepConfig(5600, SolutionType.NICE_LOOP, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.CHAINS_AND_LOOPS, 280, 0, true, true, 5600, false, false),
			new StepConfig(5700, SolutionType.ALS_XZ, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.ALMOST_LOCKED_SETS, 300, 0, true, true, 5700, false, false),
			new StepConfig(5800, SolutionType.ALS_XY_WING, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.ALMOST_LOCKED_SETS, 320, 0, true, true, 5800, false, false),
			new StepConfig(5900, SolutionType.ALS_XY_CHAIN, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.ALMOST_LOCKED_SETS, 340, 0, true, true, 5900, false, false),
			new StepConfig(6000, SolutionType.DEATH_BLOSSOM, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.ALMOST_LOCKED_SETS, 360, 0, false, true, 6000, false, false),
			new StepConfig(6100, SolutionType.FRANKEN_X_WING, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FRANKEN_FISH, 300, 0, true, false, 6100, false, false),
			new StepConfig(6200, SolutionType.FRANKEN_SWORDFISH, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FRANKEN_FISH, 350, 0, true, false, 6200, false, false),
			new StepConfig(6300, SolutionType.FRANKEN_JELLYFISH, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FRANKEN_FISH, 370, 0, false, false, 6300, false, false),
			new StepConfig(6400, SolutionType.FRANKEN_SQUIRMBAG, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FRANKEN_FISH, 470, 0, false, false, 6400, false, false),
			new StepConfig(6500, SolutionType.FRANKEN_WHALE, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FRANKEN_FISH, 470, 0, false, false, 6500, false, false),
			new StepConfig(6600, SolutionType.FRANKEN_LEVIATHAN, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FRANKEN_FISH, 470, 0, false, false, 6600, false, false),
			new StepConfig(6700, SolutionType.FINNED_FRANKEN_X_WING, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_FRANKEN_FISH, 390, 0, true, false, 6700, false, false),
			new StepConfig(6800, SolutionType.FINNED_FRANKEN_SWORDFISH, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_FRANKEN_FISH, 410, 0, true, false, 6800, false, false),
			new StepConfig(6900, SolutionType.FINNED_FRANKEN_JELLYFISH, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.FINNED_FRANKEN_FISH, 430, 0, false, false, 6900, false, false),
			new StepConfig(7000, SolutionType.FINNED_FRANKEN_SQUIRMBAG, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FINNED_FRANKEN_FISH, 470, 0, false, false, 7000, false, false),
			new StepConfig(7100, SolutionType.FINNED_FRANKEN_WHALE, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FINNED_FRANKEN_FISH, 470, 0, false, false, 7100, false, false),
			new StepConfig(7200, SolutionType.FINNED_FRANKEN_LEVIATHAN, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FINNED_FRANKEN_FISH, 470, 0, false, false, 7200, false, false),
			new StepConfig(7300, SolutionType.MUTANT_X_WING, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.MUTANT_FISH, 450, 0, false, false, 7300, false, false),
			new StepConfig(7400, SolutionType.MUTANT_SWORDFISH, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.MUTANT_FISH, 450, 0, false, false, 7400, false, false),
			new StepConfig(7500, SolutionType.MUTANT_JELLYFISH, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.MUTANT_FISH, 450, 0, false, false, 7500, false, false),
			new StepConfig(7600, SolutionType.MUTANT_SQUIRMBAG, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.MUTANT_FISH, 470, 0, false, false, 7600, false, false),
			new StepConfig(7700, SolutionType.MUTANT_WHALE, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.MUTANT_FISH, 470, 0, false, false, 7700, false, false),
			new StepConfig(7800, SolutionType.MUTANT_LEVIATHAN, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.MUTANT_FISH, 470, 0, false, false, 7800, false, false),
			new StepConfig(7900, SolutionType.FINNED_MUTANT_X_WING, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FINNED_MUTANT_FISH, 470, 0, false, false, 7900, false, false),
			new StepConfig(8000, SolutionType.FINNED_MUTANT_SWORDFISH, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FINNED_MUTANT_FISH, 470, 0, false, false, 8000, false, false),
			new StepConfig(8100, SolutionType.FINNED_MUTANT_JELLYFISH, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FINNED_MUTANT_FISH, 470, 0, false, false, 8100, false, false),
			new StepConfig(8200, SolutionType.FINNED_MUTANT_SQUIRMBAG, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FINNED_MUTANT_FISH, 470, 0, false, false, 8200, false, false),
			new StepConfig(8300, SolutionType.FINNED_MUTANT_WHALE, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FINNED_MUTANT_FISH, 470, 0, false, false, 8300, false, false),
			new StepConfig(8400, SolutionType.FINNED_MUTANT_LEVIATHAN, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.FINNED_MUTANT_FISH, 470, 0, false, false, 8400, false, false),
			new StepConfig(8700, SolutionType.TEMPLATE_SET, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.LAST_RESORT, 10000, 0, false, false, 8700, false, false),
			new StepConfig(8800, SolutionType.TEMPLATE_DEL, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.LAST_RESORT, 10000, 0, false, false, 8800, false, false),
			new StepConfig(8500, SolutionType.FORCING_CHAIN, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.LAST_RESORT, 500, 0, true, false, 8500, false, false),
			new StepConfig(8600, SolutionType.FORCING_NET, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.LAST_RESORT, 700, 0, true, false, 8600, false, false),
			new StepConfig(8900, SolutionType.BRUTE_FORCE, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.LAST_RESORT, 10000, 0, true, false, 8900, false, false),
			new StepConfig(5650, SolutionType.GROUPED_NICE_LOOP, DifficultyType.UNFAIR.ordinal(),
					SolutionCategory.CHAINS_AND_LOOPS, 300, 0, true, true, 5650, false, false),
			new StepConfig(3170, SolutionType.EMPTY_RECTANGLE, DifficultyType.HARD.ordinal(),
					SolutionCategory.SINGLE_DIGIT_PATTERNS, 120, 0, true, true, 3170, false, false),
			new StepConfig(4010, SolutionType.HIDDEN_RECTANGLE, DifficultyType.HARD.ordinal(),
					SolutionCategory.UNIQUENESS, 100, 0, true, true, 4010, false, false),
			new StepConfig(4020, SolutionType.AVOIDABLE_RECTANGLE_1, DifficultyType.HARD.ordinal(),
					SolutionCategory.UNIQUENESS, 100, 0, true, true, 4020, false, false),
			new StepConfig(4030, SolutionType.AVOIDABLE_RECTANGLE_2, DifficultyType.HARD.ordinal(),
					SolutionCategory.UNIQUENESS, 100, 0, true, true, 4030, false, false),
			new StepConfig(5330, SolutionType.SIMPLE_COLORS, DifficultyType.HARD.ordinal(), SolutionCategory.COLORING,
					150, 0, true, true, 5330, false, false),
			new StepConfig(5360, SolutionType.MULTI_COLORS, DifficultyType.HARD.ordinal(), SolutionCategory.COLORING,
					200, 0, true, true, 5360, false, false),
			new StepConfig(8450, SolutionType.KRAKEN_FISH, DifficultyType.EXTREME.ordinal(),
					SolutionCategory.LAST_RESORT, 500, 0, false, false, 8450, false, false),
			new StepConfig(3120, SolutionType.TURBOT_FISH, DifficultyType.HARD.ordinal(),
					SolutionCategory.SINGLE_DIGIT_PATTERNS, 120, 0, true, true, 3120, false, false),
			new StepConfig(1210, SolutionType.LOCKED_CANDIDATES_2, DifficultyType.MEDIUM.ordinal(),
					SolutionCategory.INTERSECTIONS, 50, 0, true, true, 1210, true, false) };
	// Unsorted steps including all user changes -> serialized to *.cfg
	private StepConfig[] orgSolverSteps = null;
	// Sorted copy used internally; must not be serialized to *.cfg
	public StepConfig[] solverSteps = null;
	// Sorted copy used for step-progress calculations; must not be serialized to *.cfg
	public StepConfig[] solverStepsProgress = null;
	// internal cache for background creation
	public static final int CACHE_SIZE = 10;
	private String[][] normalPuzzles = new String[5][CACHE_SIZE]; // 10 puzzles per DifficultyLevel
	private String[] learningPuzzles = new String[CACHE_SIZE]; // 10 puzzles for training
	private String[] practisingPuzzles = new String[CACHE_SIZE]; // 10 puzzles for practising
	private int practisingPuzzlesLevel = -1; // the DifficultyLevel, for which the practising puzzles have been created
	// ChainSolver
	public static final int RESTRICT_CHAIN_LENGTH = 20; // maximum X-/XY-chain length when restrictChainSize is enabled
	public static final int RESTRICT_NICE_LOOP_LENGTH = 10; // maximum Nice Loop length when restrictChainSize is enabled
	public static final boolean RESTRICT_CHAIN_SIZE = true; // limit chain length?
	private int restrictChainLength = RESTRICT_CHAIN_LENGTH;
	private int restrictNiceLoopLength = RESTRICT_NICE_LOOP_LENGTH;
	private boolean restrictChainSize = RESTRICT_CHAIN_SIZE;
	// TablingSolver
	public static final int MAX_TABLE_ENTRY_LENGTH = 1000;
//    public static final int MAX_TABLE_ENTRY_LENGTH = 400;
	public static final int ANZ_TABLE_LOOK_AHEAD = 4;
	public static final boolean ONLY_ONE_CHAIN_PER_STEP = true;
	public static final boolean ALLOW_ALS_IN_TABLING_CHAINS = false;
	public static final boolean ALL_STEPS_ALLOW_ALS_IN_TABLING_CHAINS = true;
	private int maxTableEntryLength = MAX_TABLE_ENTRY_LENGTH;
	private int anzTableLookAhead = ANZ_TABLE_LOOK_AHEAD;
	private boolean onlyOneChainPerStep = ONLY_ONE_CHAIN_PER_STEP;
	private boolean allowAlsInTablingChains = ALLOW_ALS_IN_TABLING_CHAINS;
	private boolean allStepsAllowAlsInTablingChains = ALL_STEPS_ALLOW_ALS_IN_TABLING_CHAINS;
	// AlsSolver
	public static final boolean ONLY_ONE_ALS_PER_STEP = true; // only one step in every ALS elimination
	public static final boolean ALLOW_ALS_OVERLAP = false; // allow ALS steps with overlap (runtime!)
	public static final boolean ALL_STEPS_ONLY_ONE_ALS_PER_STEP = true; // only one step in every ALS elimination
	public static final boolean ALL_STEPS_ALLOW_ALS_OVERLAP = true; // allow ALS steps with overlap (runtime!)
	private boolean onlyOneAlsPerStep = ONLY_ONE_ALS_PER_STEP;
	private boolean allowAlsOverlap = ALLOW_ALS_OVERLAP;
	private boolean allStepsOnlyOneAlsPerStep = ALL_STEPS_ONLY_ONE_ALS_PER_STEP;
	private boolean allStepsAllowAlsOverlap = ALL_STEPS_ALLOW_ALS_OVERLAP;
	// FishSolver
	public static final int MAX_FINS = 5; // maximum number of fins
	public static final int MAX_ENDO_FINS = 2; // maximum number of endo-fins
	public static final boolean CHECK_TEMPLATES = true; // use template check to exclude candidates from search
	public static final int KRAKEN_MAX_FISH_TYPE = 1; // 0: basic only, 1: basic+franken, 2: basic+franken+mutant
	public static final int KRAKEN_MAX_FISH_SIZE = 4; // number of units in base/cover sets
	public static final int MAX_KRAKEN_FINS = 2; // maximum number of fins in Kraken search
	public static final int MAX_KRAKEN_ENDO_FINS = 0; // maximum number of endo-fins in Kraken search
	public static final boolean ONLY_ONE_FISH_PER_STEP = true; // only the smallest fish for every elimination
	public static final int FISH_DISPLAY_MODE = 0; // 0: normal; 1: statistics numbers; 2: statistics cells
	private int maxFins = MAX_FINS;
	private int maxEndoFins = MAX_ENDO_FINS;
	private boolean checkTemplates = CHECK_TEMPLATES;
	private int krakenMaxFishType = KRAKEN_MAX_FISH_TYPE;
	private int krakenMaxFishSize = KRAKEN_MAX_FISH_SIZE;
	private int maxKrakenFins = MAX_KRAKEN_FINS;
	private int maxKrakenEndoFins = MAX_KRAKEN_ENDO_FINS;
	private boolean onlyOneFishPerStep = ONLY_ONE_FISH_PER_STEP;
	private int fishDisplayMode = FISH_DISPLAY_MODE;
	// Search all steps
	public static final boolean ALL_STEPS_SEARCH_FISH = true; // search fish in the "All Steps" panel
	public static final int ALL_STEPS_MAX_FISH_TYPE = 1; // 0: basic only, 1: basic+franken, 2: basic+franken+mutant
	public static final int ALL_STEPS_MIN_FISH_SIZE = 2; // number of units in base/cover sets
	public static final int ALL_STEPS_MAX_FISH_SIZE = 4; // number of units in base/cover sets
	public static final int ALL_STEPS_MAX_FINS = 5; // maximum number of fins
	public static final int ALL_STEPS_MAX_ENDO_FINS = 2; // maximum number of endo-fins
	public static final boolean ALL_STEPS_CHECK_TEMPLATES = true; // use template check to exclude candidates from search
	public static final int ALL_STEPS_MAX_KRAKEN_FISH_TYPE = 1; // 0: basic only, 1: basic+franken, 2: basic+franken+mutant
	public static final int ALL_STEPS_MIN_KRAKEN_FISH_SIZE = 2; // number of units in base/cover sets
	public static final int ALL_STEPS_MAX_KRAKEN_FISH_SIZE = 4; // number of units in base/cover sets
	public static final int ALL_STEPS_MAX_KRAKEN_FINS = 2; // maximum number of fins in Kraken search
	public static final int ALL_STEPS_MAX_KRAKEN_ENDO_FINS = 0; // maximum number of endo-fins in Kraken search
	public static final String ALL_STEPS_FISH_CANDIDATES = "111111111"; // 1 for every candidate that should be
																		// searched, 0 otherwise
	public static final String ALL_STEPS_KRAKEN_FISH_CANDIDATES = "111111111"; // see above
	public static final int ALL_STEPS_SORT_MODE = 4; // sort by StepType
	public static final int ALL_STEPS_ALS_CHAIN_LENGTH = 6; // maximum chain length in ALS-Chain search (all steps only)
	public static final boolean ALL_STEPS_ALS_CHAIN_FORWARD_ONLY = true;
	private boolean allStepsSearchFish = ALL_STEPS_SEARCH_FISH;
	private int allStepsMaxFishType = ALL_STEPS_MAX_FISH_TYPE;
	private int allStepsMinFishSize = ALL_STEPS_MIN_FISH_SIZE;
	private int allStepsMaxFishSize = ALL_STEPS_MAX_FISH_SIZE;
	private int allStepsMaxFins = ALL_STEPS_MAX_FINS;
	private int allStepsMaxEndoFins = ALL_STEPS_MAX_ENDO_FINS;
	private boolean allStepsCheckTemplates = ALL_STEPS_CHECK_TEMPLATES;
	private int allStepsKrakenMaxFishType = ALL_STEPS_MAX_KRAKEN_FISH_TYPE;
	private int allStepsKrakenMinFishSize = ALL_STEPS_MIN_KRAKEN_FISH_SIZE;
	private int allStepsKrakenMaxFishSize = ALL_STEPS_MAX_KRAKEN_FISH_SIZE;
	private int allStepsMaxKrakenFins = ALL_STEPS_MAX_KRAKEN_FINS;
	private int allStepsMaxKrakenEndoFins = ALL_STEPS_MAX_KRAKEN_ENDO_FINS;
	private String allStepsFishCandidates = ALL_STEPS_FISH_CANDIDATES;
	private String allStepsKrakenFishCandidates = ALL_STEPS_KRAKEN_FISH_CANDIDATES;
	private int allStepsSortMode = ALL_STEPS_SORT_MODE;
	private int allStepsAlsChainLength = ALL_STEPS_ALS_CHAIN_LENGTH;
	private boolean allStepsAlsChainForwardOnly = ALL_STEPS_ALS_CHAIN_FORWARD_ONLY;
	// SudokuPanel
	// Coloring Solver
	// Single Digit Pattern Solver
	public static final boolean ALLOW_ERS_WITH_ONLY_TWO_CANDIDATES = false; // as it says...
	private boolean allowErsWithOnlyTwoCandidates = ALLOW_ERS_WITH_ONLY_TWO_CANDIDATES;
	public static final boolean ALLOW_DUALS_AND_SIAMESE = false; // Dual 2-String-Kites, Dual Skyscrapers && Siamese
																	// Fish
	private boolean allowDualsAndSiamese = ALLOW_DUALS_AND_SIAMESE;
	// Uniqueness Solver
	public static final boolean ALLOW_UNIQUENESS_MISSING_CANDIDATES = true; // allow missing candidates in cells with
																			// additional candidates
	private boolean allowUniquenessMissingCandidates = ALLOW_UNIQUENESS_MISSING_CANDIDATES;
	// General options
	public static final boolean SHOW_CANDIDATES = true; // show all candidates
	public static final boolean SHOW_CANDIDATE_HIGHLIGHT = false;
	public static final boolean SHOW_WRONG_VALUES = true; // show invalid cell/candidate values (constraint violations)
	public static final boolean SHOW_DEVIATIONS = true; // show deviations from the correct solution
	public static final boolean SHOW_COLORKU = false; // use colors instead of numbers
	public static final boolean INVALID_CELLS = false; // show possible cells
	public static final boolean COLOR_CELLS = true; // color cells or candidates
	public static final boolean SAVE_WINDOW_LAYOUT = true; // save window layout at shutdown
	public static final boolean USE_SHIFT_FOR_REGION_SELECT = true; // use shift for selecting cells or toggling
																					// candidates
	public static final boolean DELETE_CURSOR_DISPLAY = false; // let the cursor disappear after a while
	public static final int DELETE_CURSOR_DISPLAY_LENGTH = 1000; // time in ms
	public static final boolean USE_OR_INSTEAD_OF_AND_FOR_FILTER = false; // used when filtering more than one candidate
	/** Draw filters on the candidates themselves, not on the whole cell */
	public static final boolean ONLY_SMALL_FILTERS = false;
	public static final boolean USE_DEFAULT_FONT_SIZE = true; // default size for all fonts in the GUI
	public static final int CUSTOM_FONT_SIZE = 12; // custom size for all fonts in the GUI
	public static final int DRAW_MODE = 1;
	// public static final int INITIAL_HEIGHT = 728; // used to store window layout
	// at shutdown
	public static final int INITIAL_HEIGHT = 844; // used to store window layout at shutdown
	// public static final int INITIAL_WIDTH = 540; // used to store window layout
	// at shutdown
	public static final int INITIAL_WIDTH = 643; // used to store window layout at shutdown
	public static final int INITIAL_VERT_DIVIDER_LOC = -1; // used to store window layout at shutdown
	// public static final int INITIAL_HORZ_DIVIDER_LOC = 524; // used to store
	// window layout at shutdown
	public static final int INITIAL_HORZ_DIVIDER_LOC = 627; // used to store window layout at shutdown
	public static final int INITIAL_DISP_MODE = 0; // 0 .. sudoku only, 1 .. summary, 2 .. solution, 3 .. all steps
	public static final int INITIAL_X_POS = -1; // used to store window layout at shutdown
	public static final int INITIAL_Y_POS = -1; // used to store window layout at shutdown
	public static final boolean INITIAL_SHOW_HINT_PANEL = true;
	public static final boolean INITIAL_SHOW_TOOLBAR = true;
	public static final int ACT_LEVEL = DEFAULT_DIFFICULTY_LEVELS[1].getOrdinal(); // default is EASY
	public static final boolean SHOW_SUDOKU_SOLVED = false;
	public static final boolean EDIT_MODE_AUTO_ADVANCE = false;
	public static final boolean SINGLE_CLICK_MODE = false;
	public static final boolean AUTO_HIGHLIGHTING = false;
	public static final boolean COLORS_VISIBLE = true;
	public static final boolean HIGHLIGHT_GIVENS = false;
	public static final int[] COLORING_COLORS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	public static final boolean COLOR_VALUES = false;
	public static final double VALUE_FONT_FACTOR = 1.0;
	public static final double CANDIDATE_FONT_FACTOR = 1.0;
	public static final double HINT_BACK_FACTOR = 1.0;
	public static final double BOX_LINE_FACTOR = 1.0;
	private boolean showCandidates = SHOW_CANDIDATES;
	private boolean showCandidateHighlight = SHOW_CANDIDATE_HIGHLIGHT;
	private boolean showWrongValues = SHOW_WRONG_VALUES;
	private boolean showDeviations = SHOW_DEVIATIONS;
	private boolean showColorKu = SHOW_COLORKU;
	/** Current state, set by {@link MainFrame}. */
	private boolean showColorKuAct = SHOW_COLORKU;
	private boolean invalidCells = INVALID_CELLS;
	private boolean colorCells = COLOR_CELLS;
	private boolean saveWindowLayout = SAVE_WINDOW_LAYOUT;
	private boolean useShiftForRegionSelect = USE_SHIFT_FOR_REGION_SELECT;
	private boolean deleteCursorDisplay = DELETE_CURSOR_DISPLAY;
	private int deleteCursorDisplayLength = DELETE_CURSOR_DISPLAY_LENGTH;
	private boolean useDefaultFontSize = USE_DEFAULT_FONT_SIZE;
	private int customFontSize = CUSTOM_FONT_SIZE;
	private boolean useOrInsteadOfAndForFilter = USE_OR_INSTEAD_OF_AND_FOR_FILTER;
	private boolean onlySmallFilters = ONLY_SMALL_FILTERS;
	private int drawMode = DRAW_MODE;
	private int initialHeight = INITIAL_HEIGHT;
	private int initialWidth = INITIAL_WIDTH;
	private int initialVertDividerLoc = INITIAL_VERT_DIVIDER_LOC;
	private int initialHorzDividerLoc = INITIAL_HORZ_DIVIDER_LOC;
	private int initialDisplayMode = INITIAL_DISP_MODE;
	private int initialXPos = INITIAL_X_POS;
	private int initialYPos = INITIAL_Y_POS;
	private boolean showHintPanel = INITIAL_SHOW_HINT_PANEL;
	private boolean showToolBar = INITIAL_SHOW_TOOLBAR;
	private int actLevel = ACT_LEVEL;
	private boolean showSudokuSolved = SHOW_SUDOKU_SOLVED;
	private boolean editModeAutoAdvance = EDIT_MODE_AUTO_ADVANCE;
	private boolean isSingleClickMode = SINGLE_CLICK_MODE;
	private boolean isAutoHighlighting = AUTO_HIGHLIGHTING;
	private boolean isColoringVisible = COLORS_VISIBLE;
	private boolean isHighlightingGivens = HIGHLIGHT_GIVENS;
	private int[] coloringColors = Arrays.copyOf(COLORING_COLORS, COLORING_COLORS.length);
	private boolean colorValues = COLOR_VALUES;
	// Clipboard
	public static final boolean USE_ZERO_INSTEAD_OF_DOT = false; // as the name says...
	private boolean useZeroInsteadOfDot = USE_ZERO_INSTEAD_OF_DOT;
//    private Color colorKuInvalidColor = COLORKU_INVALID_COLOR;
//    private Color colorKuDeviationColor = COLORKU_DEVIATION_COLOR;
	private double valueFontFactor = VALUE_FONT_FACTOR;
	private double candidateFontFactor = CANDIDATE_FONT_FACTOR;
	private double hintBackFactor = HINT_BACK_FACTOR;
	private double boxLineFactor = BOX_LINE_FACTOR;
	public static final String DEFAULT_FILE_DIR = System.getProperty("user.home");
	public static final String DEFAULT_IMAGE_DIR = System.getProperty("user.home");
	private String defaultFileDir = DEFAULT_FILE_DIR;
	private String defaultImageDir = DEFAULT_IMAGE_DIR;
	public static final String DEFAULT_LANGUAGE = "";
	private String language = DEFAULT_LANGUAGE;
	public static final String DEFAULT_LAF = "";
	private String laf = DEFAULT_LAF;
	// paint cursor only as small frame around cell
	public static final boolean ONLY_SMALL_CURSORS = true;
	public static final double CURSOR_FRAME_SIZE = 0.08;
	private boolean onlySmallCursors = ONLY_SMALL_CURSORS;
	private double cursorFrameSize = CURSOR_FRAME_SIZE;
	// game mode
	public static final GameMode GAME_MODE = GameMode.PLAYING;
	private GameMode gameMode = GAME_MODE;
	// show hint buttons in toolbar
	public static final boolean SHOW_HINT_BUTTONS_IN_TOOLBAR = false;
	private boolean showHintButtonsInToolbar = SHOW_HINT_BUTTONS_IN_TOOLBAR;
	// history of created puzzles and savepoints
	public static final int HISTORY_SIZE = 50;
	public static final boolean HISTORY_PREVIEW = true;
	private int historySize = HISTORY_SIZE;
	private boolean historyPreview = HISTORY_PREVIEW;
	private List<String> historyOfCreatedPuzzles = new ArrayList<String>(historySize);
	// BackdoorSearchDialog
	public static final boolean BDS_SEARCH_FOR_CELLS = true; // Search for possible backdoor cells (or combinations of
																// cells)
	public static final boolean BDS_SEARCH_FOR_CANDIDATES = false; // Search for possible backdoor candidates (or
																	// combinations of candidates)
	public static final int BDS_SEARCH_CANDIDATES_ANZ = 0; // only single candidates
	private boolean bdsSearchForCells = BDS_SEARCH_FOR_CELLS;
	private boolean bdsSearchForCandidates = BDS_SEARCH_FOR_CANDIDATES;
	private int bdsSearchCandidatesAnz = BDS_SEARCH_CANDIDATES_ANZ;
	// Generator Patterns: List is empty per default
	public static final int GENERATOR_PATTERN_INDEX = -1;
	private ArrayList<GeneratorPattern> generatorPatterns = new ArrayList<GeneratorPattern>();
	private int generatorPatternIndex = GENERATOR_PATTERN_INDEX;
	// Singleton
	public static Options instance = null;

	/** Creates a new instance of Options */
	public Options() {
		difficultyLevels = copyDifficultyLevels(DEFAULT_DIFFICULTY_LEVELS);
		orgSolverSteps = copyStepConfigs(DEFAULT_SOLVER_STEPS, false, false, true);
		solverSteps = copyStepConfigs(DEFAULT_SOLVER_STEPS, false, false, false);
		solverStepsProgress = copyStepConfigs(DEFAULT_SOLVER_STEPS, false, false, false, true);
	}

	/**
	 * Adds a new sudoku to the creation history. The size of the history buffer is
	 * adjusted accordingly. New sudokus are always inserted at the start of the
	 * list and deleted from the end of the list, effectively turning the list in a
	 * queue (the performance overhead can be ignored here).
	 * 
	 * @param sudoku
	 */
	public void addSudokuToHistory(Sudoku2 sudoku) {
		if (sudoku.getLevel() == null) {
			// something went wrong, do not add it to the history
			return;
		}
		List<String> history = getHistoryOfCreatedPuzzles();
		while (history.size() > historySize - 1) {
			history.remove(history.size() - 1);
		}
		String str = sudoku.getSudoku(ClipboardMode.CLUES_ONLY) + "#" + sudoku.getLevel().getOrdinal() + "#"
				+ sudoku.getScore() + "#" + new Date().getTime();
		history.add(0, str);
	}

	public static Options getInstance() {
		if (instance == null) {
			instance = new Options();
		}
		return instance;
	}

	public DifficultyLevel[] copyDifficultyLevels(DifficultyLevel[] src) {
		DifficultyLevel[] dest = new DifficultyLevel[src.length];
		for (int i = 0; i < src.length; i++) {
			DifficultyLevel act = src[i];
			dest[i] = new DifficultyLevel(act.getType(), act.getMaxScore(), act.getName());
		}
		return dest;
	}

	public StepConfig[] copyStepConfigs(StepConfig[] src, boolean noLastTwo, boolean addLastTwo, boolean noSort) {
		return copyStepConfigs(src, noLastTwo, addLastTwo, noSort, false);
	}

	public StepConfig[] copyStepConfigs(StepConfig[] src, boolean noLastTwo, boolean addLastTwo, boolean noSort,
			boolean sortProgress) {
		// If noLastTwo or addLastTwo is set, src is usually already sorted, meaning
		// INCOMPLETE and GIVE_UP are at the end.
		// Exception: src == DEFAULT_SOLVER_STEPS (reset in ConfigSolverPanel).
		int length = src.length;
		if (noLastTwo) {
			length -= 2;
		}
		if (addLastTwo) {
			length += 2;
		}
		StepConfig[] dest = new StepConfig[length];
		// let's do it the hard way: When "reset" is pressed in ConfigSolverPanel,
		// everything is copied
		// from DEFAULT_SOLVER_STEPS, noLastTwo is set, addLastTwo is not set ->
		// INCOMPLETE and GIVE_UP
		// are the first two elements
		if (src == DEFAULT_SOLVER_STEPS && noLastTwo == true && addLastTwo == false && noSort == false) {
			for (int i = 0; i < length; i++) {
				StepConfig act = src[i + 2];
				dest[i] = new StepConfig(act.getIndex(), act.getType(), act.getLevel(), act.getCategory(),
						act.getBaseScore(), act.getAdminScore(), act.isEnabled(), act.isAllStepsEnabled(),
						act.getIndexProgress(), act.isEnabledProgress(), act.isEnabledTraining());
			}
		} else {
			for (int i = 0; i < (addLastTwo ? length - 2 : length); i++) {
				StepConfig act = src[i];
				dest[i] = new StepConfig(act.getIndex(), act.getType(), act.getLevel(), act.getCategory(),
						act.getBaseScore(), act.getAdminScore(), act.isEnabled(), act.isAllStepsEnabled(),
						act.getIndexProgress(), act.isEnabledProgress(), act.isEnabledTraining());
			}
		}
		if (addLastTwo) {
			StepConfig act = DEFAULT_SOLVER_STEPS[0];
			dest[dest.length - 2] = new StepConfig(act.getIndex(), act.getType(), act.getLevel(), act.getCategory(),
					act.getBaseScore(), act.getAdminScore(), act.isEnabled(), act.isAllStepsEnabled(),
					act.getIndexProgress(), act.isEnabledProgress(), act.isEnabledTraining());
			act = DEFAULT_SOLVER_STEPS[1];
			dest[dest.length - 1] = new StepConfig(act.getIndex(), act.getType(), act.getLevel(), act.getCategory(),
					act.getBaseScore(), act.getAdminScore(), act.isEnabled(), act.isAllStepsEnabled(),
					act.getIndexProgress(), act.isEnabledProgress(), act.isEnabledTraining());
		}
		if (!noSort) {
			if (sortProgress) {
				Arrays.sort(dest, progressComparator);
			} else {
				Arrays.sort(dest);
			}
		}
		return dest;
	}

	/**
	 * Apply all changes from solverSteps to orgSolverSteps.
	 * orgSolverSteps remains unsorted (used by the XmlWriter serializer).
	 */
	public void adjustOrgSolverSteps() {
		boolean somethingChanged = false;
		for (StepConfig step : solverSteps) {
			StepConfig orgStep = null;
			for (int i = 0; i < orgSolverSteps.length; i++) {
				if (orgSolverSteps[i].getType() == step.getType()) {
					orgStep = orgSolverSteps[i];
					break;
				}
			}
			if (orgStep == null) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "StepConfig not found!");
				continue;
			}
			if (step.getAdminScore() != orgStep.getAdminScore() || step.getBaseScore() != orgStep.getBaseScore()
					|| step.getCategory() != orgStep.getCategory() || step.isEnabled() != orgStep.isEnabled()
					|| step.getIndex() != orgStep.getIndex() || step.getLevel() != orgStep.getLevel()) {
				somethingChanged = true;
			}
			orgStep.setAdminScore(step.getAdminScore());
			orgStep.setBaseScore(step.getBaseScore());
			orgStep.setCategory(step.getCategory());
			orgStep.setEnabled(step.isEnabled());
			orgStep.setIndex(step.getIndex());
			orgStep.setLevel(step.getLevel());
			// values for allStepsEnabled, indexHeuristics, enabledHeuristics and
			// enableTraining are not set here, is done manually in the
			// corresponding config panel
		}
		if (somethingChanged) {
			BackgroundGeneratorThread.getInstance().resetAll();
		}
	}

	/**
	 * Resort the progressSteps (needed after options change)
	 */
	public void sortProgressSteps() {
		Arrays.sort(solverStepsProgress, progressComparator);
	}

	/**
	 * Since the locale is set AFTER the options have been read, the names of the
	 * difficulty levels are always in the default locale. They have to be adjusted
	 * after the correct locale has been set.
	 */

	/**
	 * Returns a String that contains a comma-separated list of all steps that are
	 * configured for training mode.
	 * 
	 * @param ellipsis
	 * @return
	 */
	public String getTrainingStepsString(boolean ellipsis) {
		return getTrainingStepsString(orgSolverSteps, ellipsis);
	}

	/**
	 * Returns a String that contains a comma-separated list of all steps that are
	 * configured for training mode.<br>
	 * If ellipsis is <code>true</code>, only one technique is shown. If more than
	 * one technique is selected, an ellipsis is appended to the first technique.
	 *
	 * @param stepArray
	 * @param ellipsis
	 * @return
	 */
	public String getTrainingStepsString(StepConfig[] stepArray, boolean ellipsis) {
		StringBuilder tmp = new StringBuilder();
		boolean first = true;
		for (StepConfig step : stepArray) {
			if (step.isEnabledTraining()) {
				if (first) {
					first = false;
				} else {
					if (ellipsis) {
						tmp.append("...");
						break;
					} else {
						tmp.append(", ");
					}
				}
				tmp.append(step.getType().getStepName());
			}
		}
		return tmp.toString();
	}


	/**
	 * @return the historyOfCreatedPuzzles
	 */
	public List<String> getHistoryOfCreatedPuzzles() {
		return historyOfCreatedPuzzles;
	}

	/**
	 * @return the fishDisplayMode
	 */
	public int getFishDisplayMode() {
		return fishDisplayMode;
	}

	/**
	 * @return the allowUniquenessMissingCandidates
	 */
	public boolean isAllowUniquenessMissingCandidates() {
		return allowUniquenessMissingCandidates;
	}

	/**
	 * @param allowUniquenessMissingCandidates the allowUniquenessMissingCandidates
	 *                                         to set
	 */
	public void setAllowUniquenessMissingCandidates(boolean allowUniquenessMissingCandidates) {
		this.allowUniquenessMissingCandidates = allowUniquenessMissingCandidates;
	}

	/**
	 * @return the normalPuzzles
	 */
	public String[][] getNormalPuzzles() {
		return normalPuzzles;
	}

	/**
	 * @return the learningPuzzles
	 */
	public String[] getLearningPuzzles() {
		return learningPuzzles;
	}

	/**
	 * @return the practisingPuzzles
	 */
	public String[] getPractisingPuzzles() {
		return practisingPuzzles;
	}

	/**
	 * @return the practisingPuzzlesLevel
	 */
	public int getPractisingPuzzlesLevel() {
		return practisingPuzzlesLevel;
	}

	/**
	 * @param practisingPuzzlesLevel the practisingPuzzlesLevel to set
	 */
	public void setPractisingPuzzlesLevel(int practisingPuzzlesLevel) {
		this.practisingPuzzlesLevel = practisingPuzzlesLevel;
	}

	/**
	 * @return the generatorPatterns
	 */
	public ArrayList<GeneratorPattern> getGeneratorPatterns() {
		return generatorPatterns;
	}

	/**
	 * @return the generatorPatternIndex
	 */
	public int getGeneratorPatternIndex() {
		return generatorPatternIndex;
	}

	/**
	 * @return the useOrInsteadOfAndForFilter
	 */
	public boolean isUseOrInsteadOfAndForFilter() {
		return useOrInsteadOfAndForFilter;
	}

	public int getActLevel() {
		return actLevel;
	}

	public int getAllStepsAlsChainLength() {
		return allStepsAlsChainLength;
	}

	public int[] getColoringColors() {
		return coloringColors;
	}

	public void setColoringColors(int[] coloringColors) {
		if (coloringColors == null || coloringColors.length == 0) {
			this.coloringColors = Arrays.copyOf(COLORING_COLORS, COLORING_COLORS.length);
			return;
		}
		this.coloringColors = Arrays.copyOf(coloringColors, coloringColors.length);
	}

	/**
	 * @return the allStepsAlsChainForwardOnly
	 */
	public boolean isAllStepsAlsChainForwardOnly() {
		return allStepsAlsChainForwardOnly;
	}

	private static class ProgressComparator implements Comparator<StepConfig> {

		@Override
		public int compare(StepConfig o1, StepConfig o2) {
			return o1.getIndexProgress() - o2.getIndexProgress();
		}
	}

	public DifficultyLevel nextDifficultyLevel(DifficultyLevel level) {
		int i = 0;
		for (i = 0; i < difficultyLevels.length; i++) {
			if (level == difficultyLevels[i]) {
				break;
			}
		}
		if (i >= difficultyLevels.length - 1) {
			return null;
		} else {
			return difficultyLevels[i + 1];
		}
	}

	/**
	 * Find a {@link DifficultyLevel} via its ordinal.
	 * 
	 * @param ordinal
	 * @return
	 */
	public DifficultyLevel getDifficultyLevel(int ordinal) {
		int i = 0;
		for (i = 0; i < difficultyLevels.length; i++) {
			if (ordinal == difficultyLevels[i].getOrdinal()) {
				break;
			}
		}
		if (i >= difficultyLevels.length) {
			return null;
		} else {
			return difficultyLevels[i];
		}
	}

	public StepConfig[] getOrgSolverSteps() {
		return orgSolverSteps;
	}

	public int getRestrictChainLength() {
		return restrictChainLength;
	}

	public int getRestrictNiceLoopLength() {
		return restrictNiceLoopLength;
	}


	public boolean isRestrictChainSize() {
		return restrictChainSize;
	}

	public int getMaxFins() {
		return maxFins;
	}

	public void setMaxFins(int maxFins) {
		this.maxFins = maxFins;
	}

	public int getMaxEndoFins() {
		return maxEndoFins;
	}

	public void setMaxEndoFins(int maxEndoFins) {
		this.maxEndoFins = maxEndoFins;
	}

	public boolean isCheckTemplates() {
		return checkTemplates;
	}

	public void setCheckTemplates(boolean checkTemplates) {
		this.checkTemplates = checkTemplates;
	}

	public DifficultyLevel[] getDifficultyLevels() {
		return difficultyLevels;
	}

	public int getMaxTableEntryLength() {
		return maxTableEntryLength;
	}

	public int getAnzTableLookAhead() {
		return anzTableLookAhead;
	}

	public boolean isUseZeroInsteadOfDot() {
		return useZeroInsteadOfDot;
	}

	public boolean isAllowErsWithOnlyTwoCandidates() {
		return allowErsWithOnlyTwoCandidates;
	}

	public void setAllowErsWithOnlyTwoCandidates(boolean allowErsWithOnlyTwoCandidates) {
		this.allowErsWithOnlyTwoCandidates = allowErsWithOnlyTwoCandidates;
	}

	public int getKrakenMaxFishType() {
		return krakenMaxFishType;
	}

	public int getMaxKrakenFins() {
		return maxKrakenFins;
	}

	public int getMaxKrakenEndoFins() {
		return maxKrakenEndoFins;
	}

	public int getKrakenMaxFishSize() {
		return krakenMaxFishSize;
	}

	public int getAllStepsMaxFins() {
		return allStepsMaxFins;
	}

	public int getAllStepsMaxEndoFins() {
		return allStepsMaxEndoFins;
	}

	public boolean isAllowDualsAndSiamese() {
		return allowDualsAndSiamese;
	}

	public void setAllowDualsAndSiamese(boolean allowDualsAndSiamese) {
		this.allowDualsAndSiamese = allowDualsAndSiamese;
	}

	public boolean isOnlyOneFishPerStep() {
		return onlyOneFishPerStep;
	}

	public void setOnlyOneFishPerStep(boolean onlyOneFishPerStep) {
		this.onlyOneFishPerStep = onlyOneFishPerStep;
	}

	public boolean isOnlyOneAlsPerStep() {
		return onlyOneAlsPerStep;
	}

	public void setOnlyOneAlsPerStep(boolean onlyOneAlsPerStep) {
		this.onlyOneAlsPerStep = onlyOneAlsPerStep;
	}

	public boolean isAllowAlsOverlap() {
		return allowAlsOverlap;
	}

	public void setAllowAlsOverlap(boolean allowAlsOverlap) {
		this.allowAlsOverlap = allowAlsOverlap;
	}

	public boolean isOnlyOneChainPerStep() {
		return onlyOneChainPerStep;
	}

	public void setOnlyOneChainPerStep(boolean onlyOneChainPerStep) {
		this.onlyOneChainPerStep = onlyOneChainPerStep;
	}

	public boolean isAllowAlsInTablingChains() {
		return allowAlsInTablingChains;
	}

	public void setAllowAlsInTablingChains(boolean allowAlsInTablingChains) {
		this.allowAlsInTablingChains = allowAlsInTablingChains;
	}
}
