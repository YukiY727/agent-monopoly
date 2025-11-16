package com.monopoly.simulation

import com.monopoly.cli.AtomicProgressTracker
import com.monopoly.domain.model.Board
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.service.GameService
import com.monopoly.domain.strategy.BuyStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 並列でゲームを実行するクラス
 *
 * Kotlin Coroutines を使用して複数のゲームを並列実行し、
 * パフォーマンスを向上させる
 */
class ParallelGameRunner(
    private val gameService: GameService,
    private val parallelism: Int = Runtime.getRuntime().availableProcessors(),
) {
    /**
     * 複数のゲームを並列実行
     *
     * @param numberOfGames 実行するゲーム数
     * @param playerStrategies プレイヤー名と戦略のペアのリスト
     * @param board ゲームボード
     * @param showProgress 進捗を表示するか
     * @return 複数ゲームの結果
     */
    suspend fun runMultipleGames(
        numberOfGames: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
        showProgress: Boolean = true,
    ): MultiGameResult = coroutineScope {
        val progressTracker = if (showProgress) {
            AtomicProgressTracker(numberOfGames).also { it.start() }
        } else {
            null
        }

        // 各ゲームを並列実行
        val deferredResults = (1..numberOfGames).map { gameNumber ->
            async(Dispatchers.Default) {
                runSingleGame(gameNumber, playerStrategies, board, progressTracker)
            }
        }

        // 全ての結果を待機
        val results = deferredResults.awaitAll()

        progressTracker?.finish()

        MultiGameResult(results, numberOfGames)
    }

    /**
     * 単一のゲームを実行
     */
    private fun runSingleGame(
        gameNumber: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
        progressTracker: AtomicProgressTracker?,
    ): SingleGameResult {
        // 各ゲームで独立した Dice インスタンスを作成
        val gameDice = Dice()

        // プレイヤーを作成
        val players = playerStrategies.map { (name, strategy) ->
            Player(name, strategy)
        }

        // ゲーム状態を初期化
        val gameState = GameState(
            players = players,
            board = board,
        )

        // ゲームを実行
        val winner = gameService.runGame(gameState, gameDice)

        // 進捗を更新
        progressTracker?.incrementAndDisplay()

        return SingleGameResult(
            gameNumber = gameNumber,
            winner = winner.name,
            totalTurns = gameState.turnNumber,
            finalState = gameState,
        )
    }
}
