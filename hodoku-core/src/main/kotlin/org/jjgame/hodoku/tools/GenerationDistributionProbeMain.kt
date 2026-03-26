package org.jjgame.hodoku.tools

import generator.SudokuGeneratorFactory
import org.jjgame.hodoku.GenerationResult
import org.jjgame.hodoku.HodokuDifficultyRater
import org.jjgame.hodoku.HodokuPuzzleGenerator
import sudoku.DifficultyType

/**
 * Developer probe tool (not a unit test).
 *
 * Run manually to understand what difficulty levels HoDoKu generation naturally produces,
 * and how fast generation + rating are before deciding a rejection-sampling budget.
 *
 * Usage:
 *   ./gradlew :hodoku-core:probeGenerationDistribution --args="100"
 */
object GenerationDistributionProbeMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val n = args.firstOrNull()?.toIntOrNull()?.coerceAtLeast(1) ?: 100
        val generator = SudokuGeneratorFactory.getInstance()
        val counts = mutableMapOf<DifficultyType, Int>()
        val genTimes = mutableListOf<Long>()
        val rateTimes = mutableListOf<Long>()

        try {
            repeat(n) {
                val t1 = System.currentTimeMillis()
                val puzzle = generator.generateSudoku(false)
                genTimes += System.currentTimeMillis() - t1

                val t2 = System.currentTimeMillis()
                val rating = HodokuDifficultyRater.rate(puzzle.values)
                rateTimes += System.currentTimeMillis() - t2

                counts[rating.difficulty] = (counts[rating.difficulty] ?: 0) + 1
            }
        } finally {
            SudokuGeneratorFactory.giveBack(generator)
        }

        val totalGen = genTimes.sum()
        val totalRate = rateTimes.sum()
        val avgGen = totalGen / n
        val avgRate = totalRate / n
        val maxGen = genTimes.max()
        val maxRate = rateTimes.max()

        println("\n=== Generation distribution probe ($n puzzles) ===")
        println("  generate : avg ${avgGen}ms  max ${maxGen}ms  total ${totalGen}ms")
        println("  rate     : avg ${avgRate}ms  max ${maxRate}ms  total ${totalRate}ms")
        println("  distribution (natural, no filtering):")
        DifficultyType.entries.forEach { level ->
            val count = counts[level] ?: 0
            val pct = count * 100 / n
            val bar = "#".repeat(pct / 2)
            println("    %-10s  %3d%%  %s".format(level, pct, bar))
        }
        println()
        println("  Implication for rejection-sampling budget per difficulty:")
        DifficultyType.entries.filter { it != DifficultyType.INCOMPLETE }.forEach { level ->
            val prob = (counts[level] ?: 0).toDouble() / n
            if (prob > 0) {
                val expectedAttempts = (1.0 / prob).toInt()
                val expectedMs = expectedAttempts * (avgGen + avgRate)
                println(
                    "    %-10s  p=%.2f  expected ~%d attempts  ~%dms per puzzle".format(
                        level,
                        prob,
                        expectedAttempts,
                        expectedMs,
                    ),
                )
            } else {
                println("    %-10s  p=0.00  not seen in $n samples".format(level))
            }
        }
        println()

        println("=== HodokuPuzzleGenerator.generate() spot check ===")
        listOf(DifficultyType.EASY, DifficultyType.HARD, DifficultyType.EXTREME).forEach { target ->
            val t = System.currentTimeMillis()
            val result = HodokuPuzzleGenerator.generate(target, maxAttempts = 50)
            val elapsed = System.currentTimeMillis() - t
            when (result) {
                is GenerationResult.Success ->
                    println("  $target -> SUCCESS  score=${result.puzzle.rating.score}  ${elapsed}ms")
                is GenerationResult.BestEffort ->
                    println("  $target -> BEST_EFFORT after ${result.attempts} attempts  " +
                        "got=${result.best.rating.difficulty}  score=${result.best.rating.score}  ${elapsed}ms")
            }
        }
        println()
    }
}
