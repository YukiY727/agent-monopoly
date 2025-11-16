package com.monopoly.statistics

/**
 * 資産推移のスナップショット
 *
 * @property turnNumber ターン番号
 * @property playerName プレイヤー名
 * @property cash 現金
 * @property totalAssets 総資産
 * @property propertiesCount 所有プロパティ数
 */
data class AssetSnapshot(
    val turnNumber: Int,
    val playerName: String,
    val cash: Int,
    val totalAssets: Int,
    val propertiesCount: Int,
)

/**
 * 全ゲームの資産推移
 *
 * @property snapshots スナップショットのリスト
 */
data class AssetHistory(
    val snapshots: List<AssetSnapshot>,
) {
    /**
     * プレイヤー別の平均資産推移を取得
     *
     * @param playerName プレイヤー名
     * @return ターン数と平均資産額のペアのリスト
     */
    fun getAverageAssetsByPlayer(playerName: String): List<Pair<Int, Double>> {
        return snapshots
            .filter { it.playerName == playerName }
            .groupBy { it.turnNumber }
            .mapValues { (_, snaps) ->
                snaps.map { it.totalAssets }.average()
            }
            .toList()
            .sortedBy { it.first }
    }

    /**
     * 全プレイヤーの名前を取得
     *
     * @return プレイヤー名のセット
     */
    fun getPlayerNames(): Set<String> {
        return snapshots.map { it.playerName }.toSet()
    }
}
