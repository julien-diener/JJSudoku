package org.jjgame.hodoku

import sudoku.Options
import sudoku.SolutionType

/**
 * Difficulty metadata derived from HoDoKu's DEFAULT_SOLVER_STEPS.
 *
 * - level: ordinal of DifficultyType (EASY=1, MEDIUM=2, HARD=3, ...)
 * - score: baseScore in StepConfig
 */
data class TechniqueComplexity(
    val level: Int,
    val score: Int,
)

object HodokuTechniqueComplexityCatalog {

    private val complexityByTypeName: Map<String, TechniqueComplexity> =
        Options.DEFAULT_SOLVER_STEPS
            .groupBy { it.type.name }
            .mapValues { (_, configs) ->
                val best = configs.minWith(compareBy({ it.level }, { it.baseScore }, { it.index }))
                TechniqueComplexity(level = best.level, score = best.baseScore)
            }

    /**
     * Returns complexity metadata for a SolutionType name (e.g. "X_WING"), or null if unknown.
     */
    fun findByTypeName(solutionType: String): TechniqueComplexity? {
        return complexityByTypeName[solutionType]
    }

    /**
     * Returns complexity metadata for a SolutionType value, or null if no default step config exists.
     */
    fun findByType(solutionType: SolutionType): TechniqueComplexity? {
        return complexityByTypeName[solutionType.name]
    }
}

