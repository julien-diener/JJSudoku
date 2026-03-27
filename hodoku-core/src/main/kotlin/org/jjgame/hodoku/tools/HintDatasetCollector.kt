package org.jjgame.hodoku.tools

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jjgame.hodoku.GeneratedPuzzle
import org.jjgame.hodoku.GenerationResult
import org.jjgame.hodoku.HodokuPuzzleGenerator
import solver.SudokuSolver
import sudoku.DifficultyType
import sudoku.SolutionStep
import sudoku.SolutionType
import sudoku.Sudoku2
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

/** JSON encoder used for dataset JSONL output. */
private val json = Json { encodeDefaults = true }

/**
 * One serialized hint example for training datasets.
 *
 * Fields:
 * - sourcePuzzle: original puzzle with clues only (1-9 or .)
 * - puzzleBeforeStep: puzzle state immediately before this hint was found
 * - solutionValue: for SET actions, the digit (1-9) that should be placed
 * - puzzleAfterStep: puzzle state after applying this hint (one more cell solved)
 * - sourceDifficulty: HoDoKu rating of the full puzzle
 * - solutionCategory, solutionType: hint technique classification (stored as strings for forward compatibility)
 * - action: SET (place digit) or ELIMINATION (remove candidate)
 * - targetCell: cell index (0-80) affected by the hint
 * - targetValue: digit (1-9) involved (for SET: value to place; for ELIMINATION: value to eliminate)
 * - eliminations: list of (index, value) pairs to eliminate (for ELIMINATION actions)
 * - candidateCountBefore: number of candidate marks before applying hint
 * - stepText: human-readable description of the hint
 */
@Serializable
data class HintDatasetRecord(
    val sourcePuzzle: String,
    val puzzleBeforeStep: String,
    val solutionValue: Int?,
    val puzzleAfterStep: String,
    val sourceDifficulty: String,
    val solutionCategory: String, // todo: convert enum to kotlin to be serialisable
    val solutionType: String,     // same
    val action: Action,
    val targetCell: Int?,
    val targetValue: Int?,
    val eliminations: List<Elimination>,
    val candidateCountBefore: Int,
    val stepText: String,
) {
    @Serializable
    enum class Action { SET, ELIMINATION }

    @Serializable
    data class Elimination(val index: Int, val value: Int)
}

data class HintDatasetConfig(
    val outputDirectory: Path,
    val targetTypes: Set<SolutionType>,
    val samplesPerType: Int,
    val generationDifficulty: DifficultyType = DifficultyType.HARD,
    val maxPuzzles: Int = 2_000,
    val maxAttemptsPerPuzzle: Int = 100,
)

data class HintDatasetSummary(
    val puzzlesProcessed: Int,
    val samplesByType: Map<SolutionType, Int>,
    val outputDirectory: Path,
) {
    fun isComplete(targetTypes: Set<SolutionType>, samplesPerType: Int): Boolean =
        targetTypes.all { (samplesByType[it] ?: 0) >= samplesPerType }
}

/**
 * POC collector that mines solver steps and writes JSONL files grouped by category/type.
 */
class HintDatasetCollector(
    private val generatedPuzzleProvider: (DifficultyType, Int) -> GeneratedPuzzle = { difficulty, maxAttempts ->
        when (val result = HodokuPuzzleGenerator.generate(difficulty, maxAttempts)) {
            is GenerationResult.Success -> result.puzzle
            is GenerationResult.BestEffort -> result.best
        }
    },
) {

    fun collect(config: HintDatasetConfig): HintDatasetSummary {
        require(config.samplesPerType >= 1) { "samplesPerType must be at least 1" }
        require(config.maxPuzzles >= 1) { "maxPuzzles must be at least 1" }
        require(config.maxAttemptsPerPuzzle >= 1) { "maxAttemptsPerPuzzle must be at least 1" }
        require(config.targetTypes.isNotEmpty()) { "targetTypes must not be empty" }

        if (config.outputDirectory.notExists()) {
            config.outputDirectory.createDirectories()
        }

        val counts = config.targetTypes.associateWith { 0 }.toMutableMap()
        val writers = mutableMapOf<Pair<String, SolutionType>, BufferedWriter>()
        var puzzlesProcessed = 0

        try {
            while (puzzlesProcessed < config.maxPuzzles && !isDone(counts, config.samplesPerType)) {
                puzzlesProcessed++
                val generated = generatedPuzzleProvider(config.generationDifficulty, config.maxAttemptsPerPuzzle)
                val sourcePuzzle = toDotGrid(generated.clues)
                val sudoku = Sudoku2().apply { setSudoku(sourcePuzzle, true) }
                val solver = SudokuSolver()

                while (true) {
                    val step = solver.getHint(sudoku, false) ?: break
                    if (step.type == SolutionType.GIVE_UP) break

                    val type = step.type
                    if (type in config.targetTypes && (counts[type] ?: 0) < config.samplesPerType) {
                        val record = buildRecord(
                            sourcePuzzle = sourcePuzzle,
                            sudoku = sudoku,
                            step = step,
                            sourceDifficulty = generated.rating.difficulty,
                        )
                        val key = record.solutionCategory to type
                        val writer = writers.getOrPut(key) {
                            val categoryDir = config.outputDirectory.resolve(record.solutionCategory)
                            Files.createDirectories(categoryDir)
                            Files.newBufferedWriter(categoryDir.resolve("${type.name}.jsonl"))
                        }
                        writer.write(json.encodeToString(record))
                        writer.newLine()
                        counts[type] = (counts[type] ?: 0) + 1
                    }

                    solver.doStep(sudoku, step)
                    if (isDone(counts, config.samplesPerType)) break
                }
            }
        } finally {
            writers.values.forEach { it.close() }
        }

        return HintDatasetSummary(
            puzzlesProcessed = puzzlesProcessed,
            samplesByType = counts.toMap(),
            outputDirectory = config.outputDirectory,
        )
    }

    internal fun buildRecord(
        sourcePuzzle: String,
        sudoku: Sudoku2,
        step: SolutionStep,
        sourceDifficulty: DifficultyType,
    ): HintDatasetRecord {
        val target = if (step.anzSet > 0 && step.indices.isNotEmpty() && step.values.isNotEmpty()) {
            step.indices.first() to step.values.first()
        } else {
            null
        }

        val eliminations = step.candidatesToDelete.map {
            HintDatasetRecord.Elimination(index = it.index, value = it.value)
        }
        val action = if (target != null) HintDatasetRecord.Action.SET else HintDatasetRecord.Action.ELIMINATION
        val category = step.type.stepConfig?.category?.name ?: "UNCATEGORIZED"

        // Capture puzzle state before applying the hint
        val beforeValues = sudoku.values.copyOf()

        // Apply the hint to get the "after" state
        val solver = SudokuSolver()
        solver.doStep(sudoku, step)
        val afterValues = sudoku.values.copyOf()

        // Restore original state for next iteration
        sudoku.setValues(beforeValues)

        return HintDatasetRecord(
            sourcePuzzle = sourcePuzzle,
            puzzleBeforeStep = toDotGrid(beforeValues),
            solutionValue = target?.second,
            puzzleAfterStep = toDotGrid(afterValues),
            sourceDifficulty = sourceDifficulty.name,
            solutionCategory = category,
            solutionType = step.type.name,
            action = action,
            targetCell = target?.first,
            targetValue = target?.second,
            eliminations = eliminations,
            candidateCountBefore = sudoku.unsolvedCandidatesAnz,
            stepText = step.toString(2),
        )
    }

    private fun isDone(counts: Map<SolutionType, Int>, samplesPerType: Int): Boolean =
        counts.values.all { it >= samplesPerType }
}

internal fun toDotGrid(values: IntArray): String = buildString(81) {
    for (value in values) {
        append(if (value == 0) '.' else ('0' + value))
    }
}
