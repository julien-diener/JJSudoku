package org.jjgame.hodoku.tools

import sudoku.DifficultyType
import sudoku.SolutionType
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * POC executable that mines hint examples and writes grouped JSONL files.
 *
 * Usage examples:
 *   ./gradlew :hodoku-core:probeHintDataset
 *   ./gradlew :hodoku-core:probeHintDataset --args="--perType=50 --maxPuzzles=1000"
 *   ./gradlew :hodoku-core:probeHintDataset --args="--types=ALL"
 *   ./gradlew :hodoku-core:probeHintDataset --args="--types=HIDDEN_SINGLE,LOCKED_CANDIDATES_1,X_WING"
 */

/**
 * Types that are never directly returned by the solver — they are aggregate aliases
 * (the solver returns the concrete sub-type instead) or internal control values.
 * Requesting them as targets would never yield any samples.
 */
private val NON_ACTIONABLE_TYPES = setOf(
    SolutionType.GIVE_UP,
    SolutionType.INCOMPLETE,
    SolutionType.BRUTE_FORCE,
    // aggregate aliases: solver returns sub-type (e.g. CONTINUOUS_NICE_LOOP, not NICE_LOOP)
    SolutionType.LOCKED_CANDIDATES,
    SolutionType.NICE_LOOP,
    SolutionType.GROUPED_NICE_LOOP,
    SolutionType.FORCING_CHAIN,
    SolutionType.FORCING_NET,
    SolutionType.KRAKEN_FISH,
    SolutionType.SIMPLE_COLORS,
    SolutionType.MULTI_COLORS,
)
object HintDatasetProbeMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val options = parseOptions(args)
        val collector = HintDatasetCollector()

        val summary = collector.collect(
            HintDatasetConfig(
                outputDirectory = options.outputDir,
                targetTypes = options.targetTypes,
                samplesPerType = options.perType,
                generationDifficulty = options.difficulty,
                maxPuzzles = options.maxPuzzles,
                maxAttemptsPerPuzzle = options.maxAttemptsPerPuzzle,
            ),
        )

        println()
        println("=== Hint dataset probe ===")
        println("Output directory  : ${summary.outputDirectory.absolutePathString()}")
        println("Puzzles processed : ${summary.puzzlesProcessed}")
        println("Samples per type  : ${options.perType}")
        println("Difficulty source : ${options.difficulty}")
        println("Requested types   : ${options.targetTypes.joinToString { it.name }}")
        println("--- Collected ---")
        options.targetTypes.forEach { type ->
            val count = summary.samplesByType[type] ?: 0
            println("  ${type.name.padEnd(28)} $count")
        }
        println("Complete          : ${summary.isComplete(options.targetTypes, options.perType)}")
        println()
    }

    private data class Options(
        val outputDir: Path,
        val perType: Int,
        val maxPuzzles: Int,
        val maxAttemptsPerPuzzle: Int,
        val difficulty: DifficultyType,
        val targetTypes: Set<SolutionType>,
    )

    private fun parseOptions(args: Array<String>): Options {
        val raw = args
            .mapNotNull { it.split('=', limit = 2).takeIf { parts -> parts.size == 2 } }
            .associate { (key, value) -> key.trim() to value.trim() }

        val outputDir = Path.of(raw["--outputDir"] ?: "build/hint-dataset")
        val perType = (raw["--perType"]?.toIntOrNull() ?: 15).coerceAtLeast(1)
        val maxPuzzles = (raw["--maxPuzzles"]?.toIntOrNull() ?: 600).coerceAtLeast(1)
        val maxAttemptsPerPuzzle = (raw["--maxAttempts"]?.toIntOrNull() ?: 100).coerceAtLeast(1)
        val difficulty = raw["--difficulty"]
            ?.let { DifficultyType.valueOf(it.uppercase()) }
            ?: DifficultyType.HARD
        val allActionableTypes = SolutionType.entries.toSet() - NON_ACTIONABLE_TYPES
        val defaultTypes = setOf(
            SolutionType.HIDDEN_SINGLE,
            SolutionType.NAKED_SINGLE,
            SolutionType.LOCKED_CANDIDATES_1,
            SolutionType.X_WING,
            SolutionType.SKYSCRAPER,
        )
        val targetTypes = when (val typesArg = raw["--types"]?.trim()?.uppercase()) {
            null -> defaultTypes
            "ALL" -> allActionableTypes
            else -> typesArg
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { SolutionType.valueOf(it) }
                .toSet()
                .ifEmpty { defaultTypes }
        }

        return Options(
            outputDir = outputDir,
            perType = perType,
            maxPuzzles = maxPuzzles,
            maxAttemptsPerPuzzle = maxAttemptsPerPuzzle,
            difficulty = difficulty,
            targetTypes = targetTypes,
        )
    }
}

