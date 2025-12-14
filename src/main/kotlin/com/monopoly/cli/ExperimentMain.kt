package com.monopoly.cli

import com.monopoly.domain.experiment.AggregatedStatistics
import com.monopoly.domain.experiment.ExperimentRunner
import com.monopoly.domain.experiment.GameStatistics
import com.monopoly.domain.experiment.HtmlReportWriter
import com.monopoly.domain.experiment.StatisticsDisplay
import com.monopoly.domain.experiment.StatisticsWriter
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 実験管理のメインエントリーポイント
 */
fun main(args: Array<String>) {
    println("=" * 60)
    println("Monopoly Experiment Runner - Phase 9")
    println("=" * 60)
    println()

    // CLI引数をパース
    val parser = ArgumentParser()
    val config: ExperimentConfig =
        try {
            parser.parse(args) ?: return // ヘルプ表示時は終了
        } catch (e: IllegalArgumentException) {
            System.err.println("Error: ${e.message}")
            System.err.println("Use --help for usage information")
            return
        }

    // 実験IDを生成（タイムスタンプベース）
    val formatter = SimpleDateFormat("yyyyMMdd-HHmmss")
    val experimentId: String = "exp-${formatter.format(Date())}"

    println("Experiment ID: $experimentId")
    println("Configuration:")
    println("  - Games to run: ${config.gameCount}")
    println("  - Players: ${config.playerCount}")
    println("  - Strategies:")
    config.strategies.forEachIndexed { index, strategy ->
        println("    ${index + 1}. ${strategy::class.simpleName}")
    }
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
    val results: List<GameStatistics> = runner.runExperiment(config.gameCount, config.strategies)
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

    // 実験ごとのディレクトリを作成
    val experimentDir = "experiment-results/$experimentId"

    // ファイル保存
    val writer = StatisticsWriter(outputDir = experimentDir)
    val files = writer.write(results, aggregated)

    // HTML レポート生成
    val htmlWriter = HtmlReportWriter(outputDir = experimentDir)
    val htmlPath: String = htmlWriter.write(results, aggregated)

    println("Results saved to: $experimentDir/")
    println("  - JSON: ${files.jsonPath}")
    println("  - CSV:  ${files.csvPath}")
    println("  - HTML: $htmlPath")
    println()
}

/** 文字列の繰り返し演算子 */
private operator fun String.times(count: Int): String = this.repeat(count)
