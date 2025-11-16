package com.monopoly.statistics

/**
 * 破産イベント
 *
 * @property gameNumber ゲーム番号
 * @property turnNumber ターン番号
 * @property playerName 破産したプレイヤー名
 * @property causePlayerName 破産させたプレイヤー名（nullable）
 * @property lastCash 破産時の現金
 * @property propertiesOwned 破産時の所有プロパティ数
 */
data class BankruptcyEvent(
    val gameNumber: Int,
    val turnNumber: Int,
    val playerName: String,
    val causePlayerName: String?,
    val lastCash: Int,
    val propertiesOwned: Int,
)

/**
 * 破産分析
 *
 * @property bankruptcyEvents 破産イベントのリスト
 * @property totalBankruptcies 総破産数
 * @property averageBankruptcyTurn 平均破産ターン
 * @property bankruptcyDistribution ターン範囲ごとの破産数
 */
data class BankruptcyAnalysis(
    val bankruptcyEvents: List<BankruptcyEvent>,
    val totalBankruptcies: Int,
    val averageBankruptcyTurn: Double,
    val bankruptcyDistribution: Map<IntRange, Int>,
) {
    /**
     * プレイヤー別の破産回数
     *
     * @return プレイヤー名と破産回数のマップ
     */
    fun getBankruptcyCountByPlayer(): Map<String, Int> {
        return bankruptcyEvents
            .groupingBy { it.playerName }
            .eachCount()
    }

    /**
     * 破産させたプレイヤーランキング
     *
     * @return プレイヤー名と破産させた回数のペアのリスト（降順）
     */
    fun getTopBankruptcyCausers(): List<Pair<String, Int>> {
        return bankruptcyEvents
            .mapNotNull { it.causePlayerName }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
    }
}
