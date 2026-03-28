package org.jjgame.sudokuapp

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jjgame.hodoku.HodokuTechniqueComplexityCatalog

/**
 * One training hint record as loaded from assets/training/<CATEGORY>/<TYPE>.jsonl
 *
 * Only the fields needed for Phase 1 (SET actions) are used. The full schema is preserved
 * for future ELIMINATION training support.
 */
@Serializable
data class TrainingRecord(
    val sourcePuzzle: String,
    val puzzleBeforeStep: String,
    val solutionValue: Int?,
    val puzzleAfterStep: String,
    val sourceDifficulty: String,
    val solutionCategory: String,
    val solutionType: String,
    val action: String,
    val targetCell: Int?,
    val targetValue: Int?,
    val eliminations: List<Elimination>,
    val candidateCountBefore: Int,
    val stepText: String,
) {
    @Serializable
    data class Elimination(val index: Int, val value: Int)
}

/**
 * One item in the technique selection list.
 */
data class TrainingTechnique(
    /** Asset path, e.g. "training/BASIC_FISH/X_WING.jsonl" */
    val assetPath: String,
    /** Human-readable category, e.g. "BASIC FISH" */
    val category: String,
    /** Human-readable technique name, e.g. "X Wing" */
    val name: String,
    /** Number of available samples */
    val sampleCount: Int,
)

object TrainingRepository {

    private data class RankedTechnique(
        val technique: TrainingTechnique,
        val knownComplexity: Boolean,
        val level: Int,
        val score: Int,
    )

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Lists all available training techniques by scanning the assets/training folder.
     * Returns them sorted by HoDoKu complexity (difficulty level, then base score).
     */
    fun listTechniques(context: Context): List<TrainingTechnique> {
        val assetManager = context.assets
        val techniques = mutableListOf<RankedTechnique>()

        val categories = assetManager.list("training") ?: return emptyList()
        for (category in categories.sorted()) {
            val files = assetManager.list("training/$category") ?: continue
            for (file in files.sorted()) {
                if (!file.endsWith(".jsonl")) continue
                val typeName = file.removeSuffix(".jsonl")
                val assetPath = "training/$category/$file"
                val complexity = HodokuTechniqueComplexityCatalog.findByTypeName(typeName)

                // Count lines = number of samples
                val sampleCount = assetManager.open(assetPath).bufferedReader().use { reader ->
                    reader.lines().filter { it.isNotBlank() }.count().toInt()
                }

                techniques.add(
                    RankedTechnique(
                        technique = TrainingTechnique(
                        assetPath = assetPath,
                        category = category.replace('_', ' '),
                        name = typeName.replace('_', ' ').lowercase()
                            .replaceFirstChar { it.uppercase() },
                        sampleCount = sampleCount,
                        ),
                        knownComplexity = complexity != null,
                        level = complexity?.level ?: Int.MAX_VALUE,
                        score = complexity?.score ?: Int.MAX_VALUE,
                    ),
                )
            }
        }
        return techniques
            .sortedWith(
                compareBy<RankedTechnique> { if (it.knownComplexity) 0 else 1 }
                    .thenBy { it.level }
                    .thenBy { it.score }
                    .thenBy { it.technique.category }
                    .thenBy { it.technique.name },
            )
            .map { it.technique }
    }

    /**
     * Loads all records for the given technique asset path.
     * Filters to only SET-action records for Phase 1 training.
     */
    fun loadSetRecords(context: Context, assetPath: String): List<TrainingRecord> {
        return context.assets.open(assetPath).bufferedReader().use { reader ->
            reader.lineSequence()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    runCatching { json.decodeFromString<TrainingRecord>(line) }.getOrNull()
                }
                .filter { it.action == "SET" && it.targetCell != null && it.targetValue != null }
                .toList()
        }
    }

    /**
     * Loads all records for the given technique asset path (both SET and ELIMINATION).
     */
    fun loadAllRecords(context: Context, assetPath: String): List<TrainingRecord> {
        return context.assets.open(assetPath).bufferedReader().use { reader ->
            reader.lineSequence()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    runCatching { json.decodeFromString<TrainingRecord>(line) }.getOrNull()
                }
                .toList()
        }
    }
}

