package com.monopoly.domain.experiment

import com.monopoly.domain.model.player.PlayerStrategy

/**
 * 戦略情報
 *
 * 戦略の名前とインスタンスを保持
 */
data class StrategyInfo(
    val name: String,
    val strategy: PlayerStrategy,
)

/**
 * 戦略対戦結果
 *
 * 2つの戦略の対戦結果を保持
 */
data class StrategyMatchup(
    val strategy1: String,
    val strategy2: String,
    val strategy1Wins: Int,
    val strategy2Wins: Int,
    val gamesPlayed: Int,
) {
    /** 戦略1の勝率 */
    val strategy1WinRate: Double
        get() = if (gamesPlayed > 0) strategy1Wins.toDouble() / gamesPlayed else 0.0

    /** 戦略2の勝率 */
    val strategy2WinRate: Double
        get() = if (gamesPlayed > 0) strategy2Wins.toDouble() / gamesPlayed else 0.0
}

/**
 * 戦略比較マトリックス
 *
 * 全戦略の対戦結果を保持し、テーブル形式で出力可能
 */
data class ComparisonMatrix(
    val strategies: List<String>,
    val matchups: List<StrategyMatchup>,
) {
    /**
     * 指定した戦略ペアの勝率を取得
     *
     * @param strategy1 戦略1の名前
     * @param strategy2 戦略2の名前
     * @return 戦略1の勝率（見つからない場合はnull）
     */
    fun getWinRate(
        strategy1: String,
        strategy2: String,
    ): Double? {
        if (strategy1 == strategy2) return null

        val matchup: StrategyMatchup? =
            matchups.find {
                (it.strategy1 == strategy1 && it.strategy2 == strategy2) ||
                    (it.strategy1 == strategy2 && it.strategy2 == strategy1)
            }

        return when {
            matchup == null -> null
            matchup.strategy1 == strategy1 -> matchup.strategy1WinRate
            else -> matchup.strategy2WinRate
        }
    }

    /**
     * マトリックスをテーブル形式で出力
     */
    fun toTable(): String {
        val sb = StringBuilder()

        // ヘッダー行（戦略名の最大長を計算）
        val maxNameLen: Int = strategies.maxOf { it.length }.coerceAtLeast(MIN_COLUMN_WIDTH)
        val cellWidth: Int = CELL_WIDTH

        // ヘッダー
        sb.append(" ".repeat(maxNameLen + 2))
        strategies.forEach { name ->
            sb.append("| ${name.padEnd(cellWidth)} ")
        }
        sb.appendLine("|")

        // 区切り線
        sb.append("-".repeat(maxNameLen + 2))
        strategies.forEach { _ ->
            sb.append("+${"-".repeat(cellWidth + 2)}")
        }
        sb.appendLine("+")

        // データ行
        strategies.forEach { row ->
            sb.append(" ${row.padEnd(maxNameLen)} ")
            strategies.forEach { col ->
                val winRate: Double? = getWinRate(row, col)
                val cell: String =
                    when {
                        row == col -> "  -  "
                        winRate != null -> "%5.1f%%".format(winRate * 100)
                        else -> "  ?  "
                    }
                sb.append("| ${cell.padEnd(cellWidth)} ")
            }
            sb.appendLine("|")
        }

        return sb.toString()
    }

    companion object {
        private const val MIN_COLUMN_WIDTH = 8
        private const val CELL_WIDTH = 8
    }
}

/**
 * 戦略比較実験
 *
 * 複数の戦略を総当たりで対戦させ、勝率マトリックスを生成
 */
class StrategyComparisonExperiment(
    private val gamesPerMatchup: Int = DEFAULT_GAMES_PER_MATCHUP,
    private val progressCallback: ((String, Int, Int) -> Unit)? = null,
) {
    private val experimentRunner = ExperimentRunner()

    /**
     * 戦略比較実験を実行
     *
     * @param strategies 比較する戦略のリスト
     * @return 比較マトリックス
     */
    fun run(strategies: List<StrategyInfo>): ComparisonMatrix {
        val matchups: MutableList<StrategyMatchup> = mutableListOf()
        val totalMatchups: Int = strategies.size * (strategies.size - 1) / 2
        var completedMatchups = 0

        // 全ペアの組み合わせを生成して対戦
        for (i in strategies.indices) {
            for (j in (i + 1) until strategies.size) {
                val strategy1: StrategyInfo = strategies[i]
                val strategy2: StrategyInfo = strategies[j]

                val matchup: StrategyMatchup = runMatchup(strategy1, strategy2)
                matchups.add(matchup)

                completedMatchups++
                progressCallback?.invoke(
                    "${strategy1.name} vs ${strategy2.name}",
                    completedMatchups,
                    totalMatchups,
                )
            }
        }

        return ComparisonMatrix(
            strategies = strategies.map { it.name },
            matchups = matchups,
        )
    }

    /**
     * 2つの戦略の対戦を実行
     */
    private fun runMatchup(
        strategy1: StrategyInfo,
        strategy2: StrategyInfo,
    ): StrategyMatchup {
        var strategy1Wins = 0
        var strategy2Wins = 0

        repeat(gamesPerMatchup) { gameIndex ->
            val gameId: String = "matchup_${strategy1.name}_vs_${strategy2.name}_game_%03d".format(gameIndex + 1)
            val result: GameStatistics =
                experimentRunner.runSingleGame(
                    gameId = gameId,
                    strategies = listOf(strategy1.strategy, strategy2.strategy),
                )

            // Player1 = strategy1, Player2 = strategy2
            when (result.winner) {
                "Player1" -> strategy1Wins++
                "Player2" -> strategy2Wins++
            }
        }

        return StrategyMatchup(
            strategy1 = strategy1.name,
            strategy2 = strategy2.name,
            strategy1Wins = strategy1Wins,
            strategy2Wins = strategy2Wins,
            gamesPlayed = gamesPerMatchup,
        )
    }

    companion object {
        /** デフォルトの対戦ゲーム数 */
        private const val DEFAULT_GAMES_PER_MATCHUP = 100
    }
}
