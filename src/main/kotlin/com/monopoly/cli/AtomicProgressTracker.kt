package com.monopoly.cli

import java.util.concurrent.atomic.AtomicInteger

/**
 * スレッドセーフな進捗トラッカー
 *
 * 並列実行時に複数のコルーチンから安全に進捗を更新できる
 */
class AtomicProgressTracker(private val totalGames: Int) {
    private val completed = AtomicInteger(0)
    private var startTime: Long = 0

    /**
     * 進捗表示を開始
     */
    fun start() {
        startTime = System.currentTimeMillis()
        println("Starting $totalGames games...")
        displayProgress(0)
    }

    /**
     * 完了数をインクリメントして進捗を表示
     */
    fun incrementAndDisplay() {
        val current = completed.incrementAndGet()
        displayProgress(current)
    }

    /**
     * 進捗表示を完了
     */
    fun finish() {
        val elapsed = System.currentTimeMillis() - startTime
        println() // 改行
        println("Completed $totalGames games in ${elapsed}ms")
    }

    /**
     * 進捗バーを表示
     */
    private fun displayProgress(current: Int) {
        val percentage = (current * 100) / totalGames
        val barLength = 50
        val filledLength = (barLength * current) / totalGames
        val bar = "=".repeat(filledLength) + " ".repeat(barLength - filledLength)

        // \r で行頭に戻って上書き
        print("\r[$bar] $current/$totalGames ($percentage%)")
    }
}
