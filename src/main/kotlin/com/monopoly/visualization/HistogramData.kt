package com.monopoly.visualization

/**
 * ヒストグラムのデータ
 *
 * @property title グラフのタイトル
 * @property bins ビンのリスト
 */
data class HistogramData(
    val title: String,
    val bins: List<Bin>,
) : ChartData {
    /**
     * ヒストグラムの1つのビン（区間）
     *
     * @property rangeStart 区間の開始値
     * @property rangeEnd 区間の終了値
     * @property count この区間に含まれるデータ数
     */
    data class Bin(
        val rangeStart: Int,
        val rangeEnd: Int,
        val count: Int,
    )
}
