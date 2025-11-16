package com.monopoly.cli

import com.monopoly.simulation.MultiGameResult

/**
 * 複数ゲーム実行結果のサマリーを表示
 */
class ResultSummaryPrinter {
    /**
     * 結果サマリーを表示
     *
     * @param result 複数ゲーム実行結果
     */
    fun print(result: MultiGameResult) {
        printHeader()
        printWinnerSummary(result)
        println()
        printTurnStatistics(result)
        printFooter()
    }

    /**
     * ヘッダーを表示
     */
    private fun printHeader() {
        println("=".repeat(60))
        println("SIMULATION RESULTS")
        println("=".repeat(60))
        println()
    }

    /**
     * 勝者サマリーを表示
     *
     * @param result 複数ゲーム実行結果
     */
    private fun printWinnerSummary(result: MultiGameResult) {
        println("Winner Summary:")
        println("-".repeat(40))

        val winsByPlayer = result.getWinsByPlayer()
            .toList()
            .sortedByDescending { (_, wins) -> wins }

        winsByPlayer.forEach { (player, wins) ->
            val winRate = (wins.toDouble() / result.totalGames * 100)
            println("  %-20s: %4d wins (%.1f%%)".format(player, wins, winRate))
        }
    }

    /**
     * ターン統計を表示
     *
     * @param result 複数ゲーム実行結果
     */
    private fun printTurnStatistics(result: MultiGameResult) {
        println("Turn Statistics:")
        println("-".repeat(40))
        println("  Average turns: %.1f".format(result.getAverageTurns()))
        println("  Min turns:     %d".format(result.getMinTurns()))
        println("  Max turns:     %d".format(result.getMaxTurns()))
    }

    /**
     * フッターを表示
     */
    private fun printFooter() {
        println()
        println("=".repeat(60))
    }
}
