package com.monopoly.visualization

/**
 * ヒストグラムを生成するクラス
 */
class HistogramGenerator {
    private val width = 600
    private val height = 400
    private val padding = 60

    /**
     * ヒストグラムのSVGを生成
     *
     * @param data ヒストグラムデータ
     * @return SVG文字列
     */
    fun generate(data: HistogramData): String {
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        return buildString {
            appendLine("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")
            appendLine("  <!-- Background -->")
            appendLine("  <rect width=\"$width\" height=\"$height\" fill=\"#ffffff\"/>")

            // タイトル
            appendLine(generateTitle(data.title))

            // 軸
            appendLine(generateAxes())

            // ヒストグラム
            appendLine(generateBins(data.bins, chartWidth, chartHeight))

            // ラベル
            appendLine(generateLabels(data.bins, chartWidth))

            appendLine("</svg>")
        }
    }

    /**
     * タイトルを生成
     */
    private fun generateTitle(title: String): String {
        return "  <text x=\"${width / 2}\" y=\"30\" text-anchor=\"middle\" font-size=\"18\" font-weight=\"bold\">$title</text>"
    }

    /**
     * 軸を生成
     */
    private fun generateAxes(): String {
        return buildString {
            // Y軸
            appendLine("  <line x1=\"$padding\" y1=\"$padding\" x2=\"$padding\" y2=\"${height - padding}\" stroke=\"#333\" stroke-width=\"2\"/>")
            // X軸
            appendLine("  <line x1=\"$padding\" y1=\"${height - padding}\" x2=\"${width - padding}\" y2=\"${height - padding}\" stroke=\"#333\" stroke-width=\"2\"/>")

            // Y軸のラベル（Frequency）
            appendLine("  <text x=\"20\" y=\"${height / 2}\" text-anchor=\"middle\" font-size=\"12\" transform=\"rotate(-90, 20, ${height / 2})\">Frequency</text>")
            // X軸のラベル（Turns）
            appendLine("  <text x=\"${width / 2}\" y=\"${height - 20}\" text-anchor=\"middle\" font-size=\"12\">Turns</text>")
        }
    }

    /**
     * ヒストグラムのビンを生成
     */
    private fun generateBins(bins: List<HistogramData.Bin>, chartWidth: Int, chartHeight: Int): String {
        if (bins.isEmpty()) return ""

        val maxCount = bins.maxOfOrNull { it.count } ?: 1
        val binWidth = (chartWidth / bins.size * 0.9).toInt()
        val binSpacing = chartWidth / bins.size

        return buildString {
            bins.forEachIndexed { index, bin ->
                val barHeight = if (maxCount > 0) {
                    (bin.count.toDouble() / maxCount * chartHeight).toInt()
                } else {
                    0
                }
                val x = padding + index * binSpacing + (binSpacing - binWidth) / 2
                val y = height - padding - barHeight

                // ビン（棒）
                appendLine("  <rect x=\"$x\" y=\"$y\" width=\"$binWidth\" height=\"$barHeight\" fill=\"#2ecc71\" rx=\"2\"/>")

                // カウントのラベル
                if (bin.count > 0) {
                    appendLine("  <text x=\"${x + binWidth / 2}\" y=\"${y - 5}\" text-anchor=\"middle\" font-size=\"12\" font-weight=\"bold\">${bin.count}</text>")
                }
            }
        }
    }

    /**
     * X軸のラベル（範囲）を生成
     */
    private fun generateLabels(bins: List<HistogramData.Bin>, chartWidth: Int): String {
        if (bins.isEmpty()) return ""

        val binSpacing = chartWidth / bins.size

        return buildString {
            bins.forEachIndexed { index, bin ->
                val x = padding + index * binSpacing + binSpacing / 2
                val y = height - padding + 20

                val label = "${bin.rangeStart}-${bin.rangeEnd}"
                appendLine("  <text x=\"$x\" y=\"$y\" text-anchor=\"middle\" font-size=\"11\">$label</text>")
            }
        }
    }
}
