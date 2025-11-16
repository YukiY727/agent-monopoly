package com.monopoly.cli

/**
 * 複数ゲーム実行の進捗を表示
 *
 * @property totalGames 総ゲーム数
 */
class ProgressDisplay(
    private val totalGames: Int,
) {
    private var startTime: Long = 0
    private val barLength = 40

    /**
     * 進捗表示を開始
     */
    fun start() {
        startTime = System.currentTimeMillis()
        println("Running $totalGames games...")
        println()
    }

    /**
     * 進捗を更新
     *
     * @param currentGame 現在のゲーム数（1から開始）
     */
    fun update(currentGame: Int) {
        val progress = (currentGame.toDouble() / totalGames * 100).toInt()
        val filledLength = (barLength * currentGame / totalGames)
        val bar = buildProgressBar(filledLength)

        // \r でカーソルを行頭に戻して上書き
        print("\rProgress: [$bar] $currentGame/$totalGames ($progress%)")
        System.out.flush()  // バッファをフラッシュ
    }

    /**
     * 進捗表示を終了
     */
    fun finish() {
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
        println()
        println()
        println("Completed $totalGames games in %.2f seconds".format(elapsedTime))
        println()
    }

    /**
     * プログレスバーを構築
     *
     * @param filledLength 埋める長さ
     * @return プログレスバー文字列
     */
    private fun buildProgressBar(filledLength: Int): String {
        return "=".repeat(filledLength) + " ".repeat(barLength - filledLength)
    }
}
