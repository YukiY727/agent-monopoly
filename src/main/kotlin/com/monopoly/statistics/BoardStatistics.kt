package com.monopoly.statistics

/**
 * ボード統計
 *
 * @property positionStats 位置ごとの統計
 * @property totalGames 総ゲーム数
 */
data class BoardStatistics(
    val positionStats: Map<Int, PositionStatistics>,
    val totalGames: Int,
) {
    /**
     * 位置統計
     *
     * @property position ボード上の位置（0-39）
     * @property landedCount 停止回数
     * @property totalRentCollected 総レント収入
     * @property ownershipDistribution 所有者分布（プレイヤー名 → 所有回数）
     */
    data class PositionStatistics(
        val position: Int,
        val landedCount: Int,
        val totalRentCollected: Double,
        val ownershipDistribution: Map<String, Int>,
    ) {
        /**
         * ゲームあたりの平均停止回数
         */
        fun averageLandedPerGame(totalGames: Int): Double =
            landedCount.toDouble() / totalGames

        /**
         * ゲームあたりの平均レント収入
         */
        fun averageRentPerGame(totalGames: Int): Double =
            totalRentCollected / totalGames
    }

    /**
     * 最も停止回数が多い位置
     */
    fun mostLandedPosition(): PositionStatistics? =
        positionStats.values.maxByOrNull { it.landedCount }

    /**
     * 最も収益性が高い位置
     */
    fun mostProfitablePosition(): PositionStatistics? =
        positionStats.values.maxByOrNull { it.totalRentCollected }
}
