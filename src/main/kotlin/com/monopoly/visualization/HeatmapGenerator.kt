package com.monopoly.visualization

/**
 * ヒートマップSVGを生成
 */
class HeatmapGenerator {
    /**
     * ヒートマップSVGを生成
     */
    fun generate(data: HeatmapData): String {
        val cellSize = 50
        val fontSize = 10
        val gridSize = 11 // モノポリーボードは11x11グリッド

        val positions = calculateBoardPositions(gridSize, cellSize)

        return buildString {
            val svgWidth = gridSize * cellSize
            val svgHeight = gridSize * cellSize

            appendLine("<svg width=\"$svgWidth\" height=\"$svgHeight\" xmlns=\"http://www.w3.org/2000/svg\">")

            // 背景
            appendLine("  <rect width=\"$svgWidth\" height=\"$svgHeight\" fill=\"#f0f0f0\" />")

            // セルを描画
            data.cells.forEach { cell ->
                val (x, y) = positions.getOrDefault(cell.position, Pair(0, 0))
                val intensity = if (data.maxValue > data.minValue) {
                    (cell.value - data.minValue) / (data.maxValue - data.minValue)
                } else {
                    0.0
                }
                val color = interpolateColor(intensity)

                // セルの矩形
                appendLine("  <rect x=\"$x\" y=\"$y\" width=\"$cellSize\" height=\"$cellSize\" " +
                    "fill=\"$color\" stroke=\"#333\" stroke-width=\"1\" />")

                // ラベル
                val textX = x + cellSize / 2
                val textY = y + cellSize / 2
                appendLine("  <text x=\"$textX\" y=\"$textY\" font-size=\"$fontSize\" " +
                    "text-anchor=\"middle\" dominant-baseline=\"middle\">${cell.label}</text>")
            }

            // タイトル（中央上部）
            val titleX = svgWidth / 2
            val titleY = -10
            appendLine("  <text x=\"$titleX\" y=\"$titleY\" font-size=\"16\" font-weight=\"bold\" " +
                "text-anchor=\"middle\">${data.title}</text>")

            appendLine("</svg>")
        }
    }

    /**
     * ボード上の位置からSVG座標を計算
     *
     * モノポリーボードは時計回り:
     * - 0-10: 下辺（右→左）
     * - 11-20: 左辺（下→上）
     * - 21-30: 上辺（左→右）
     * - 31-39: 右辺（上→下）
     */
    private fun calculateBoardPositions(gridSize: Int, cellSize: Int): Map<Int, Pair<Int, Int>> {
        val positions = mutableMapOf<Int, Pair<Int, Int>>()

        // 下辺（0-10）
        for (i in 0..10) {
            val x = (gridSize - 1 - i) * cellSize
            val y = (gridSize - 1) * cellSize
            positions[i] = Pair(x, y)
        }

        // 左辺（11-20）
        for (i in 11..20) {
            val x = 0
            val y = (gridSize - 1 - (i - 10)) * cellSize
            positions[i] = Pair(x, y)
        }

        // 上辺（21-30）
        for (i in 21..30) {
            val x = (i - 20) * cellSize
            val y = 0
            positions[i] = Pair(x, y)
        }

        // 右辺（31-39）
        for (i in 31..39) {
            val x = (gridSize - 1) * cellSize
            val y = (i - 30) * cellSize
            positions[i] = Pair(x, y)
        }

        return positions
    }

    /**
     * 強度（0.0-1.0）から色を補間
     *
     * 白 → 薄い青 → 青 → 濃い青
     */
    private fun interpolateColor(intensity: Double): String {
        val clampedIntensity = intensity.coerceIn(0.0, 1.0)

        val r = (255 * (1 - clampedIntensity)).toInt()
        val g = (255 * (1 - clampedIntensity)).toInt()
        val b = 255

        return String.format("#%02x%02x%02x", r, g, b)
    }
}
