package com.monopoly.simulation

import com.monopoly.cli.ProgressDisplay
import com.monopoly.domain.model.Board
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.service.GameService
import com.monopoly.domain.strategy.BuyStrategy

/**
 * 複数ゲームの実行を管理するクラス
 *
 * @property gameService ゲームサービス
 * @property dice サイコロ
 */
class GameRunner(
    private val gameService: GameService,
    private val dice: Dice,
) {
    /**
     * 複数ゲームを実行
     *
     * @param numberOfGames 実行するゲーム数
     * @param playerStrategies プレイヤー名と戦略のペアリスト
     * @param board ゲームボード
     * @param showProgress 進捗表示するか（デフォルト: true）
     * @return 複数ゲーム実行結果
     */
    fun runMultipleGames(
        numberOfGames: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
        showProgress: Boolean = true,
    ): MultiGameResult {
        val results = mutableListOf<SingleGameResult>()
        val progressDisplay = createProgressDisplay(numberOfGames, showProgress)

        progressDisplay?.start()

        repeat(numberOfGames) { gameIndex ->
            val result = runSingleGame(gameIndex, playerStrategies, board)
            results.add(result)
            progressDisplay?.update(gameIndex + 1)
        }

        progressDisplay?.finish()

        return MultiGameResult(
            gameResults = results,
            totalGames = numberOfGames,
        )
    }

    /**
     * 1ゲームを実行
     *
     * @param gameIndex ゲームインデックス（0から開始）
     * @param playerStrategies プレイヤー名と戦略のペアリスト
     * @param board ゲームボード
     * @return 単一ゲーム実行結果
     */
    private fun runSingleGame(
        gameIndex: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
    ): SingleGameResult {
        // 新しいGameStateを作成（完全に独立）
        val gameState = createInitialGameState(playerStrategies, board)

        // ゲーム実行
        gameService.runGame(gameState, dice)

        // 結果を返す
        return SingleGameResult(
            gameNumber = gameIndex + 1,
            winner = determineWinner(gameState),
            totalTurns = gameState.turnNumber,
            finalState = gameState,
        )
    }

    /**
     * 初期ゲーム状態を作成
     *
     * @param playerStrategies プレイヤー名と戦略のペアリスト
     * @param board ゲームボード
     * @return 初期ゲーム状態
     */
    private fun createInitialGameState(
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
    ): GameState {
        val players = playerStrategies.map { (name, strategy) ->
            Player(name, strategy)
        }
        return GameState(players = players, board = board)
    }

    /**
     * 勝者を判定
     *
     * @param gameState ゲーム状態
     * @return 勝者名
     */
    private fun determineWinner(gameState: GameState): String {
        return gameState.players
            .maxByOrNull { it.getTotalAssets() }
            ?.name
            ?: "Unknown"
    }

    /**
     * 進捗表示インスタンスを作成
     *
     * @param numberOfGames ゲーム数
     * @param showProgress 進捗表示するか
     * @return 進捗表示インスタンス（表示しない場合はnull）
     */
    private fun createProgressDisplay(
        numberOfGames: Int,
        showProgress: Boolean,
    ): ProgressDisplay? {
        return if (showProgress) ProgressDisplay(numberOfGames) else null
    }
}
