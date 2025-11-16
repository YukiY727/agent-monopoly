package com.monopoly.visualization

/**
 * 棒グラフを生成するクラス
 */
class BarChartGenerator {
    private val width = 600
    private val height = 400
    private val padding = 60

    /**
     * 棒グラフのSVGを生成
     *
     * @param data 棒グラフデータ
     * @return SVG文字列
     */
    fun generate(data: BarChartData): String {
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

            // 棒グラフ
            appendLine(generateBars(data.bars, chartWidth, chartHeight))

            // ラベル
            appendLine(generateLabels(data.bars, chartWidth))

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

            // Y軸のラベル（Win Rate）
            appendLine("  <text x=\"20\" y=\"${height / 2}\" text-anchor=\"middle\" font-size=\"12\" transform=\"rotate(-90, 20, ${height / 2})\">Win Rate</text>")
        }
    }

    /**
     * 棒グラフを生成
     */
    private fun generateBars(bars: List<BarChartData.Bar>, chartWidth: Int, chartHeight: Int): String {
        if (bars.isEmpty()) return ""

        val maxValue = bars.maxOfOrNull { it.value } ?: 1.0
        val barWidth = (chartWidth / bars.size * 0.7).toInt()
        val barSpacing = chartWidth / bars.size

        return buildString {
            bars.forEachIndexed { index, bar ->
                val barHeight = (bar.value / maxValue * chartHeight).toInt()
                val x = padding + index * barSpacing + (barSpacing - barWidth) / 2
                val y = height - padding - barHeight

                // 棒
                appendLine("  <rect x=\"$x\" y=\"$y\" width=\"$barWidth\" height=\"$barHeight\" fill=\"${bar.color}\" rx=\"2\"/>")

                // 値のラベル
                val percentage = bar.value * 100
                appendLine("  <text x=\"${x + barWidth / 2}\" y=\"${y - 5}\" text-anchor=\"middle\" font-size=\"12\" font-weight=\"bold\">${String.format("%.1f%%", percentage)}</text>")
            }
        }
    }

    /**
     * X軸のラベルを生成
     */
    private fun generateLabels(bars: List<BarChartData.Bar>, chartWidth: Int): String {
        if (bars.isEmpty()) return ""

        val barSpacing = chartWidth / bars.size

        return buildString {
            bars.forEachIndexed { index, bar ->
                val x = padding + index * barSpacing + barSpacing / 2
                val y = height - padding + 20

                appendLine("  <text x=\"$x\" y=\"$y\" text-anchor=\"middle\" font-size=\"12\">${escapeXml(bar.label)}</text>")
            }
        }
    }

    /**
     * XML/SVG用にエスケープ
     */
    private fun escapeXml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
}
