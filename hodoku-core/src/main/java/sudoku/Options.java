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

import generator.GeneratorPattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static sudoku.DifficultyType.*;
import static sudoku.SolutionCategory.*;

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
			new DifficultyLevel(INCOMPLETE, 0, "Incomplete"),
			new DifficultyLevel(EASY, 800, "Easy"),
			new DifficultyLevel(MEDIUM, 1000, "Medium"),
			new DifficultyLevel(HARD, 1600, "Hard"),
			new DifficultyLevel(UNFAIR, 1800, "Unfair"),
			new DifficultyLevel(EXTREME, Integer.MAX_VALUE, "Extreme") };

	private DifficultyLevel[] difficultyLevels = null;
	// Order and configuration of solver steps
	// IMPORTANT: New solver steps must be added at the end of the array.
	// The position is determined by "index".
	public static final StepConfig[] DEFAULT_SOLVER_STEPS = {
			new StepConfig(EASY, SINGLES, SolutionType.FULL_HOUSE, 100, 4, 0, true, true, true, false),
			new StepConfig(EASY, SINGLES, SolutionType.NAKED_SINGLE, 200, 4, 0, true, true, true, false),
			new StepConfig(EASY, SINGLES, SolutionType.HIDDEN_SINGLE, 300, 14, 0, true, true, true, false),
			new StepConfig(MEDIUM, INTERSECTIONS, SolutionType.LOCKED_PAIR, 1000, 40, 0, true, true, true, false),
			new StepConfig(MEDIUM, INTERSECTIONS, SolutionType.LOCKED_TRIPLE, 1100, 60, 0, true, true, true, false),
			new StepConfig(MEDIUM, INTERSECTIONS, SolutionType.LOCKED_CANDIDATES_1, 1200, 50, 0, true, true, true, false),
			new StepConfig(MEDIUM, INTERSECTIONS, SolutionType.LOCKED_CANDIDATES_2, 1210, 50, 0, true, true, true, false),
			new StepConfig(MEDIUM, SUBSETS, SolutionType.NAKED_PAIR, 1300, 60, 0, true, true, true, false),
			new StepConfig(MEDIUM, SUBSETS, SolutionType.NAKED_TRIPLE, 1400, 80, 0, true, true, true, false),
			new StepConfig(MEDIUM, SUBSETS, SolutionType.HIDDEN_PAIR, 1500, 70, 0, true, true, true, false),
			new StepConfig(MEDIUM, SUBSETS, SolutionType.HIDDEN_TRIPLE, 1600, 100, 0, true, true, true, false),
			new StepConfig(HARD, SUBSETS, SolutionType.NAKED_QUADRUPLE, 2000, 120, 0, true, true, true, false),
			new StepConfig(HARD, SUBSETS, SolutionType.HIDDEN_QUADRUPLE, 2100, 150, 0, true, true, true, false),
			new StepConfig(HARD, BASIC_FISH, SolutionType.X_WING, 2200, 140, 0, true, false, false, false),
			new StepConfig(HARD, BASIC_FISH, SolutionType.SWORDFISH, 2300, 150, 0, true, false, false, false),
			new StepConfig(HARD, BASIC_FISH, SolutionType.JELLYFISH, 2400, 160, 0, true, false, false, false),
			new StepConfig(HARD, CHAINS_AND_LOOPS, SolutionType.REMOTE_PAIR, 2800, 110, 0, true, true, false, false),
			new StepConfig(HARD, UNIQUENESS, SolutionType.BUG_PLUS_1, 2900, 100, 0, true, true, false, false),
			new StepConfig(HARD, SINGLE_DIGIT_PATTERNS, SolutionType.SKYSCRAPER, 3000, 130, 0, true, true, false, false),
			new StepConfig(HARD, SINGLE_DIGIT_PATTERNS, SolutionType.TWO_STRING_KITE, 3100, 150, 0, true, true, false, false),
			new StepConfig(HARD, SINGLE_DIGIT_PATTERNS, SolutionType.TURBOT_FISH, 3120, 120, 0, true, true, false, false) ,
			new StepConfig(HARD, SINGLE_DIGIT_PATTERNS, SolutionType.EMPTY_RECTANGLE, 3170, 120, 0, true, true, false, false),
			new StepConfig(HARD, WINGS, SolutionType.W_WING, 3200, 150, 0, true, true, false, false),
			new StepConfig(HARD, WINGS, SolutionType.XY_WING, 3300, 160, 0, true, true, false, false),
			new StepConfig(HARD, WINGS, SolutionType.XYZ_WING, 3400, 180, 0, true, true, false, false),
			new StepConfig(HARD, UNIQUENESS, SolutionType.UNIQUENESS_1, 3500, 100, 0, true, true, false, false),
			new StepConfig(HARD, UNIQUENESS, SolutionType.UNIQUENESS_2, 3600, 100, 0, true, true, false, false),
			new StepConfig(HARD, UNIQUENESS, SolutionType.UNIQUENESS_3, 3700, 100, 0, true, true, false, false),
			new StepConfig(HARD, UNIQUENESS, SolutionType.UNIQUENESS_4, 3800, 100, 0, true, true, false, false),
			new StepConfig(HARD, UNIQUENESS, SolutionType.UNIQUENESS_5, 3900, 100, 0, true, true, false, false),
			new StepConfig(HARD, UNIQUENESS, SolutionType.UNIQUENESS_6, 4000, 100, 0, true, true, false, false),
			new StepConfig(HARD, UNIQUENESS, SolutionType.HIDDEN_RECTANGLE, 4010, 100, 0, true, true, false, false),
			new StepConfig(HARD, UNIQUENESS, SolutionType.AVOIDABLE_RECTANGLE_1, 4020, 100, 0, true, true, false, false),
			new StepConfig(HARD, UNIQUENESS, SolutionType.AVOIDABLE_RECTANGLE_2, 4030, 100, 0, true, true, false, false),
			new StepConfig(HARD, FINNED_BASIC_FISH, SolutionType.FINNED_X_WING, 4100, 130, 0, true, false, false, false),
			new StepConfig(HARD, FINNED_BASIC_FISH, SolutionType.SASHIMI_X_WING, 4200, 150, 0, true, false, false, false),
			new StepConfig(HARD, COLORING, SolutionType.SIMPLE_COLORS, 5330, 150, 0, true, true, false, false),
			new StepConfig(HARD, COLORING, SolutionType.MULTI_COLORS, 5360, 200, 0, true, true, false, false),
			new StepConfig(UNFAIR, FINNED_BASIC_FISH, SolutionType.FINNED_SWORDFISH, 4300, 200, 0, true, false, false, false),
			new StepConfig(UNFAIR, FINNED_BASIC_FISH, SolutionType.SASHIMI_SWORDFISH, 4400, 240, 0, true, false, false, false),
			new StepConfig(UNFAIR, FINNED_BASIC_FISH, SolutionType.FINNED_JELLYFISH, 4500, 250, 0, true, false, false, false),
			new StepConfig(UNFAIR, FINNED_BASIC_FISH, SolutionType.SASHIMI_JELLYFISH, 4600, 260, 0, true, false, false, false),
			new StepConfig(UNFAIR, FINNED_BASIC_FISH, SolutionType.FINNED_SQUIRMBAG, 4700, 470, 0, false, false, false, false),
			new StepConfig(UNFAIR, FINNED_BASIC_FISH, SolutionType.SASHIMI_SQUIRMBAG, 4800, 470, 0, false, false, false, false),
			new StepConfig(UNFAIR, FINNED_BASIC_FISH, SolutionType.FINNED_WHALE, 4900, 470, 0, false, false, false, false),
			new StepConfig(UNFAIR, FINNED_BASIC_FISH, SolutionType.SASHIMI_WHALE, 5000, 470, 0, false, false, false, false),
			new StepConfig(UNFAIR, FINNED_BASIC_FISH, SolutionType.FINNED_LEVIATHAN, 5100, 470, 0, false, false, false, false),
			new StepConfig(UNFAIR, FINNED_BASIC_FISH, SolutionType.SASHIMI_LEVIATHAN, 5200, 470, 0, false, false, false, false),
			new StepConfig(UNFAIR, BASIC_FISH, SolutionType.SQUIRMBAG, 2500, 470, 0, false, false, false, false),
			new StepConfig(UNFAIR, BASIC_FISH, SolutionType.WHALE, 2600, 470, 0, false, false, false, false),
			new StepConfig(UNFAIR, BASIC_FISH, SolutionType.LEVIATHAN, 2700, 470, 0, false, false, false, false),
			new StepConfig(UNFAIR, MISCELLANEOUS, SolutionType.SUE_DE_COQ, 5300, 250, 0, true, true, false, false),
			new StepConfig(UNFAIR, CHAINS_AND_LOOPS, SolutionType.X_CHAIN, 5400, 260, 0, true, true, false, false),
			new StepConfig(UNFAIR, CHAINS_AND_LOOPS, SolutionType.XY_CHAIN, 5500, 260, 0, true, true, false, false),
			new StepConfig(UNFAIR, CHAINS_AND_LOOPS, SolutionType.NICE_LOOP, 5600, 280, 0, true, true, false, false),
			new StepConfig(UNFAIR, CHAINS_AND_LOOPS, SolutionType.GROUPED_NICE_LOOP, 5650, 300, 0, true, true, false, false),
			new StepConfig(UNFAIR, ALMOST_LOCKED_SETS, SolutionType.ALS_XZ, 5700, 300, 0, true, true, false, false),
			new StepConfig(UNFAIR, ALMOST_LOCKED_SETS, SolutionType.ALS_XY_WING, 5800, 320, 0, true, true, false, false),
			new StepConfig(UNFAIR, ALMOST_LOCKED_SETS, SolutionType.ALS_XY_CHAIN, 5900, 340, 0, true, true, false, false),
			new StepConfig(UNFAIR, ALMOST_LOCKED_SETS, SolutionType.DEATH_BLOSSOM, 6000, 360, 0, false, true, false, false),
			new StepConfig(UNFAIR, FRANKEN_FISH, SolutionType.FRANKEN_X_WING, 6100, 300, 0, true, false, false, false),
			new StepConfig(UNFAIR, FRANKEN_FISH, SolutionType.FRANKEN_SWORDFISH, 6200, 350, 0, true, false, false, false),
			new StepConfig(UNFAIR, FRANKEN_FISH, SolutionType.FRANKEN_JELLYFISH, 6300, 370, 0, false, false, false, false),
			new StepConfig(EXTREME, FRANKEN_FISH, SolutionType.FRANKEN_SQUIRMBAG, 6400, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, FRANKEN_FISH, SolutionType.FRANKEN_WHALE, 6500, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, FRANKEN_FISH, SolutionType.FRANKEN_LEVIATHAN, 6600, 470, 0, false, false, false, false),
			new StepConfig(UNFAIR, FINNED_FRANKEN_FISH, SolutionType.FINNED_FRANKEN_X_WING, 6700, 390, 0, true, false, false, false),
			new StepConfig(UNFAIR, FINNED_FRANKEN_FISH, SolutionType.FINNED_FRANKEN_SWORDFISH, 6800, 410, 0, true, false, false, false),
			new StepConfig(UNFAIR, FINNED_FRANKEN_FISH, SolutionType.FINNED_FRANKEN_JELLYFISH, 6900, 430, 0, false, false, false, false),
			new StepConfig(EXTREME, FINNED_FRANKEN_FISH, SolutionType.FINNED_FRANKEN_SQUIRMBAG, 7000, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, FINNED_FRANKEN_FISH, SolutionType.FINNED_FRANKEN_WHALE, 7100, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, FINNED_FRANKEN_FISH, SolutionType.FINNED_FRANKEN_LEVIATHAN, 7200, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, MUTANT_FISH, SolutionType.MUTANT_X_WING, 7300, 450, 0, false, false, false, false),
			new StepConfig(EXTREME, MUTANT_FISH, SolutionType.MUTANT_SWORDFISH, 7400, 450, 0, false, false, false, false),
			new StepConfig(EXTREME, MUTANT_FISH, SolutionType.MUTANT_JELLYFISH, 7500, 450, 0, false, false, false, false),
			new StepConfig(EXTREME, MUTANT_FISH, SolutionType.MUTANT_SQUIRMBAG, 7600, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, MUTANT_FISH, SolutionType.MUTANT_WHALE, 7700, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, MUTANT_FISH, SolutionType.MUTANT_LEVIATHAN, 7800, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, FINNED_MUTANT_FISH, SolutionType.FINNED_MUTANT_X_WING, 7900, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, FINNED_MUTANT_FISH, SolutionType.FINNED_MUTANT_SWORDFISH, 8000, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, FINNED_MUTANT_FISH, SolutionType.FINNED_MUTANT_JELLYFISH, 8100, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, FINNED_MUTANT_FISH, SolutionType.FINNED_MUTANT_SQUIRMBAG, 8200, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, FINNED_MUTANT_FISH, SolutionType.FINNED_MUTANT_WHALE, 8300, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, FINNED_MUTANT_FISH, SolutionType.FINNED_MUTANT_LEVIATHAN, 8400, 470, 0, false, false, false, false),
			new StepConfig(EXTREME, LAST_RESORT, SolutionType.TEMPLATE_SET, 8700, 10000, 0, false, false, false, false),
			new StepConfig(EXTREME, LAST_RESORT, SolutionType.TEMPLATE_DEL, 8800, 10000, 0, false, false, false, false),
			new StepConfig(EXTREME, LAST_RESORT, SolutionType.FORCING_CHAIN, 8500, 500, 0, true, false, false, false),
			new StepConfig(EXTREME, LAST_RESORT, SolutionType.FORCING_NET, 8600, 700, 0, true, false, false, false),
			new StepConfig(EXTREME, LAST_RESORT, SolutionType.BRUTE_FORCE, 8900, 10000, 0, true, false, false, false),
			new StepConfig(EXTREME, LAST_RESORT, SolutionType.KRAKEN_FISH, 8450, 500, 0, false, false, false, false),
			new StepConfig(INCOMPLETE, LAST_RESORT, SolutionType.INCOMPLETE, Integer.MAX_VALUE - 1, 0, 0, false, false, false, false),
			new StepConfig(EXTREME, LAST_RESORT, SolutionType.GIVE_UP, Integer.MAX_VALUE, 20000, 0, true, false, true, false)
		};

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
	private int maxTableEntryLength = MAX_TABLE_ENTRY_LENGTH;
	private int anzTableLookAhead = ANZ_TABLE_LOOK_AHEAD;
	private boolean onlyOneChainPerStep = ONLY_ONE_CHAIN_PER_STEP;
	private boolean allowAlsInTablingChains = ALLOW_ALS_IN_TABLING_CHAINS;
	// AlsSolver
	public static final boolean ONLY_ONE_ALS_PER_STEP = true; // only one step in every ALS elimination
	public static final boolean ALLOW_ALS_OVERLAP = false; // allow ALS steps with overlap (runtime!)
	private boolean onlyOneAlsPerStep = ONLY_ONE_ALS_PER_STEP;
	private boolean allowAlsOverlap = ALLOW_ALS_OVERLAP;
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
	public static final int ALL_STEPS_MAX_FINS = 5; // maximum number of fins
	public static final int ALL_STEPS_MAX_ENDO_FINS = 2; // maximum number of endo-fins
	public static final int ALL_STEPS_ALS_CHAIN_LENGTH = 6; // maximum chain length in ALS-Chain search (all steps only)
	public static final boolean ALL_STEPS_ALS_CHAIN_FORWARD_ONLY = true;
	private int allStepsMaxFins = ALL_STEPS_MAX_FINS;
	private int allStepsMaxEndoFins = ALL_STEPS_MAX_ENDO_FINS;
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

	public static final boolean USE_OR_INSTEAD_OF_AND_FOR_FILTER = false; // used when filtering more than one candidate
	public static final int ACT_LEVEL = DEFAULT_DIFFICULTY_LEVELS[1].getOrdinal(); // default is EASY
	public static final int[] COLORING_COLORS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private boolean useOrInsteadOfAndForFilter = USE_OR_INSTEAD_OF_AND_FOR_FILTER;
	private int actLevel = ACT_LEVEL;
	private int[] coloringColors = Arrays.copyOf(COLORING_COLORS, COLORING_COLORS.length);
	// Clipboard
	public static final boolean USE_ZERO_INSTEAD_OF_DOT = false; // as the name says...
	private boolean useZeroInsteadOfDot = USE_ZERO_INSTEAD_OF_DOT;

	// Generator Patterns: List is empty per default
	public static final int GENERATOR_PATTERN_INDEX = -1;
	private ArrayList<GeneratorPattern> generatorPatterns = new ArrayList<GeneratorPattern>();
	private int generatorPatternIndex = GENERATOR_PATTERN_INDEX;
	// Singleton
	public static Options instance = null;

	/** Creates a new instance of Options */
	public Options() {
		difficultyLevels = copyDifficultyLevels(DEFAULT_DIFFICULTY_LEVELS);
		solverSteps = copyStepConfigs(false);
		solverStepsProgress = copyStepConfigs(true);
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

	public StepConfig[] copyStepConfigs(boolean sortProgress) {
		int length = DEFAULT_SOLVER_STEPS.length;
		StepConfig[] dest = new StepConfig[length];

		for (int i = 0; i < length; i++) {
			StepConfig act = DEFAULT_SOLVER_STEPS[i];
			dest[i] = new StepConfig(act.getDifficultyType(), act.getCategory(), act.getType(), act.getIndex(),
					act.getBaseScore(), act.getAdminScore(), act.isEnabled(), act.isAllStepsEnabled(),
					act.isEnabledProgress(), act.isEnabledTraining());
		}
		if (sortProgress) {
			Arrays.sort(dest, progressComparator);
		} else {
			Arrays.sort(dest);
		}
		return dest;
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

	/**
	 * @return the allStepsAlsChainForwardOnly
	 */
	public boolean isAllStepsAlsChainForwardOnly() {
		return allStepsAlsChainForwardOnly;
	}

	private static class ProgressComparator implements Comparator<StepConfig> {

		@Override
		public int compare(StepConfig o1, StepConfig o2) {
			return o1.getIndex() - o2.getIndex();
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
