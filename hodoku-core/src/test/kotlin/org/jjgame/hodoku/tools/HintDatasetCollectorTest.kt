package org.jjgame.hodoku.tools

import kotlinx.serialization.json.Json
import org.jjgame.hodoku.GeneratedPuzzle
import org.jjgame.hodoku.HodokuDifficultyRater
import solver.SudokuSolver
import sudoku.Sudoku2
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HintDatasetCollectorTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun json_encoding_serializes_all_fields() {
        val record = HintDatasetRecord(
            sourcePuzzle = ".".repeat(81),
            puzzleBeforeStep = ".".repeat(81),
            solutionValue = 7,
            puzzleAfterStep = "7" + ".".repeat(80),
            sourceDifficulty = "HARD",
            solutionCategory = "SINGLES",
            solutionType = "HIDDEN_SINGLE",
            action = HintDatasetRecord.Action.SET,
            targetCell = 12,
            targetValue = 7,
            eliminations = listOf(HintDatasetRecord.Elimination(index = 5, value = 3)),
            candidateCountBefore = 157,
            stepText = "Hidden Single: r2c1=7",
        )

        val encoded = json.encodeToString(record)

        // round-trip: decoding produces an equal object
        val decoded = json.decodeFromString<HintDatasetRecord>(encoded)
        assertEquals(record, decoded)

        // key fields present in JSON output
        assertTrue(encoded.contains("\"solutionValue\":7"))
        assertTrue(encoded.contains("\"solutionType\":\"HIDDEN_SINGLE\""))
        assertTrue(encoded.contains("\"sourceDifficulty\":\"HARD\""))
        assertTrue(encoded.contains("\"action\":\"SET\""))
    }

    @Test
    fun json_encoding_handles_special_characters_in_step_text() {
        val record = HintDatasetRecord(
            sourcePuzzle = ".".repeat(81),
            puzzleBeforeStep = ".".repeat(81),
            solutionValue = null,
            puzzleAfterStep = ".".repeat(81),
            sourceDifficulty = "HARD",
            solutionCategory = "SINGLES",
            solutionType = "HIDDEN_SINGLE",
            action = HintDatasetRecord.Action.ELIMINATION,
            targetCell = null,
            targetValue = null,
            eliminations = emptyList(),
            candidateCountBefore = 100,
            stepText = "He said \"hint\"\nnext line",
        )

        val encoded = json.encodeToString(record)
        val decoded = json.decodeFromString<HintDatasetRecord>(encoded)

        // kotlinx.serialization handles escaping; round-trip preserves the original value
        assertEquals(record.stepText, decoded.stepText)
    }

    @Test
    fun collector_writes_one_sample_for_requested_type() {
        val clues = toClues(
            "53..7...." +
                "6..195..." +
                ".98....6." +
                "8...6...3" +
                "4..8.3..1" +
                "7...2...6" +
                ".6....28." +
                "...419..5" +
                "....8..79",
        )
        val rating = HodokuDifficultyRater.rate(clues)
        val generatedPuzzle = GeneratedPuzzle(
            clues = clues.copyOf(),
            solution = IntArray(81),
            rating = rating,
        )

        val firstHintType = SudokuSolver()
            .getHint(Sudoku2().apply { setSudoku(toDotGrid(clues), true) }, false)
            ?.type
            ?: error("Expected at least one hint for known valid puzzle")

        val outputDir = Files.createTempDirectory("hint-dataset-test")
        try {
            val collector = HintDatasetCollector { _, _ -> generatedPuzzle }
            val summary = collector.collect(
                HintDatasetConfig(
                    outputDirectory = outputDir,
                    targetTypes = setOf(firstHintType),
                    samplesPerType = 1,
                    generationDifficulty = rating.difficulty,
                    maxPuzzles = 1,
                    maxAttemptsPerPuzzle = 1,
                ),
            )

            assertEquals(1, summary.samplesByType[firstHintType])

            val files = Files.walk(outputDir).use { stream ->
                stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".jsonl") }
                    .toList()
            }
            assertEquals(1, files.size)

            val lines = Files.readAllLines(files.first())
            assertEquals(1, lines.size)
            assertTrue(lines.first().contains("\"solutionType\":\"${firstHintType.name}\""))
        } finally {
            Files.walk(outputDir)
                .sorted(Comparator.reverseOrder())
                .forEach(Files::deleteIfExists)
        }
    }

    private fun toClues(grid: String): IntArray {
        require(grid.length == 81) { "grid must be 81 chars long" }
        return IntArray(81) { idx ->
            val ch = grid[idx]
            if (ch == '.') 0 else (ch - '0')
        }
    }
}
