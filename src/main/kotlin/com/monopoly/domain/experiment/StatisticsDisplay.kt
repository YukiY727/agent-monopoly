package com.monopoly.domain.experiment

/**
 * 統計情報をCLI表示用にフォーマットするクラス
 */
class StatisticsDisplay {
    /**
     * 集計統計をCLI表示用の文字列にフォーマットする
     *
     * @param aggregated 集計統計
     * @return フォーマットされた文字列
     */
    fun format(aggregated: AggregatedStatistics): String {
        val builder = StringBuilder()

        builder.append("=" * SEPARATOR_LENGTH)
        builder.append("\n")
        builder.append("Experiment Results\n")
        builder.append("=" * SEPARATOR_LENGTH)
        builder.append("\n\n")

        // 総ゲーム数
        builder.append("Total Games: ${aggregated.totalGames}\n")

        if (aggregated.totalGames == 0) {
            builder.append("\nNo games were played.\n")
            return builder.toString()
        }

        builder.append("\n")

        // 勝率
        builder.append("Win Rates:\n")
        aggregated.winRates
            .entries
            .sortedByDescending { it.value }
            .forEach { (player, rate) ->
                val percentage: String = String.format("%.2f", rate * 100.0)
                builder.append("  $player: $percentage%\n")
            }

        builder.append("\n")

        // 平均ターン数
        val avgTurns: String = String.format("%.2f", aggregated.averageTurnCount)
        builder.append("Average Turn Count: $avgTurns\n")

        builder.append("\n")

        // 平均最終資産
        builder.append("Average Final Assets:\n")
        aggregated.averageFinalAssets
            .entries
            .sortedByDescending { it.value }
            .forEach { (player, assets) ->
                val formattedAssets: String = String.format("%.2f", assets)
                builder.append("  $player: $$$formattedAssets\n")
            }

        builder.append("\n")

        // 実行時間
        val durationSeconds: Double = aggregated.totalDuration / 1000.0
        val formattedDuration: String = String.format("%.2f", durationSeconds)
        builder.append("Total Duration: ${formattedDuration}s\n")

        builder.append("\n")
        builder.append("=" * SEPARATOR_LENGTH)
        builder.append("\n")

        return builder.toString()
    }

    companion object {
        /** セパレータの長さ */
        private const val SEPARATOR_LENGTH = 60

        /** 文字列の繰り返し演算子（Kotlin標準にはないので拡張関数として定義） */
        private operator fun String.times(count: Int): String = this.repeat(count)
    }
}
