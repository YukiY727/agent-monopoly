package com.monopoly.statistics

/**
 * プロパティ別の統計情報
 *
 * @property propertyName プロパティ名
 * @property position ボード上の位置
 * @property colorGroup 色グループ
 * @property price 購入価格
 * @property purchaseCount 購入された回数
 * @property purchaseRate 購入された割合 (0.0-1.0)
 * @property totalRentCollected 総レント収入
 * @property averageRentPerGame ゲームあたりの平均レント収入
 * @property maxRentInSingleGame 単一ゲームでの最大レント収入
 * @property winRateWhenOwned このプロパティを所有した時の勝率
 * @property avgTurnsHeldByWinner 勝者が保持していた平均ターン数
 * @property roi 投資利益率 (Return on Investment)
 */
data class PropertyStatistics(
    val propertyName: String,
    val position: Int,
    val colorGroup: String,
    val price: Int,
    val purchaseCount: Int,
    val purchaseRate: Double,
    val totalRentCollected: Int,
    val averageRentPerGame: Double,
    val maxRentInSingleGame: Int,
    val winRateWhenOwned: Double,
    val avgTurnsHeldByWinner: Double,
    val roi: Double,
)
