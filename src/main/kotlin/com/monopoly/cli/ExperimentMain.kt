package com.monopoly.cli

import com.monopoly.domain.experiment.AggregatedStatistics
import com.monopoly.domain.experiment.ExperimentRunner
import com.monopoly.domain.experiment.GameStatistics
import com.monopoly.domain.experiment.StatisticsDisplay
import com.monopoly.domain.experiment.StatisticsWriter
import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.strategy.AlwaysPlayerStrategy

/**
 * 実験管理のメインエントリーポイント
 */
fun main() {
    println("=" * 60)
    println("Monopoly Experiment Runner - Phase 8")
    println("=" * 60)
    println()

    // 実験設定
    val gameCount = 10 // 実行するゲーム数
    val strategies: List<PlayerStrategy> =
        listOf(
            AlwaysPlayerStrategy(),
            AlwaysPlayerStrategy(),
        )

    println("Configuration:")
    println("  - Games to run: $gameCount")
    println("  - Players: ${strategies.size}")
    println("  - Strategy: AlwaysPlayerStrategy (all players)")
    println()

    // 進捗コールバック
    val progressCallback: (Int, Int) -> Unit = { current, total ->
        println("Progress: $current / $total games completed")
    }

    // 実験実行
    println("Starting experiment...")
    println()

    val runner = ExperimentRunner(progressCallback)
    val startTime: Long = System.currentTimeMillis()
    val results: List<GameStatistics> = runner.runExperiment(gameCount, strategies)
    val endTime: Long = System.currentTimeMillis()

    println()
    println("Experiment completed in ${(endTime - startTime) / 1000.0}s")
    println()

    // 統計集計
    val aggregated: AggregatedStatistics = AggregatedStatistics.from(results)

    // 結果表示
    val display = StatisticsDisplay()
    val output: String = display.format(aggregated)
    println(output)

    // ファイル保存
    val writer = StatisticsWriter()
    val files = writer.write(results, aggregated)

    println("Results saved:")
    println("  - JSON: ${files.jsonPath}")
    println("  - CSV:  ${files.csvPath}")
    println()
}

/** 文字列の繰り返し演算子 */
private operator fun String.times(count: Int): String = this.repeat(count)
