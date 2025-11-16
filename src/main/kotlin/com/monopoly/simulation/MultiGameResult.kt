package com.monopoly.simulation

/**
 * 複数ゲーム実行の結果
 *
 * @property gameResults 各ゲームの結果リスト
 * @property totalGames 総ゲーム数
 */
data class MultiGameResult(
    val gameResults: List<SingleGameResult>,
    val totalGames: Int,
) {
    /**
     * プレイヤー別の勝利数を取得
     */
    fun getWinsByPlayer(): Map<String, Int> {
        return gameResults
            .groupBy { it.winner }
            .mapValues { (_, results) -> results.size }
    }

    /**
     * 平均ターン数を取得
     */
    fun getAverageTurns(): Double {
        if (gameResults.isEmpty()) return 0.0
        return gameResults.map { it.totalTurns }.average()
    }

    /**
     * 最短ゲームのターン数を取得
     */
    fun getMinTurns(): Int {
        return gameResults.minOfOrNull { it.totalTurns } ?: 0
    }

    /**
     * 最長ゲームのターン数を取得
     */
    fun getMaxTurns(): Int {
        return gameResults.maxOfOrNull { it.totalTurns } ?: 0
    }

    /**
     * 指定プレイヤーの勝率を取得
     *
     * @param playerName プレイヤー名
     * @return 勝率（0.0〜1.0）
     */
    fun getWinRate(playerName: String): Double {
        val wins = gameResults.count { it.winner == playerName }
        return wins.toDouble() / totalGames
    }
}
