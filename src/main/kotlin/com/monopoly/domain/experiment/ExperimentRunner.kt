package com.monopoly.domain.experiment

import com.monopoly.cli.createStandardBoard
import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.model.game.Board
import com.monopoly.domain.model.game.GameState
import com.monopoly.domain.model.game.impl.StandardDice
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.service.BuildingService
import com.monopoly.domain.service.GameService
import com.monopoly.domain.service.MonopolyCheckerService

/**
 * ゲーム実験を実行するクラス
 *
 * 複数のゲームを実行し、統計情報を収集する
 */
class ExperimentRunner(
    private val progressCallback: ((Int, Int) -> Unit)? = null,
) {
    /**
     * 1ゲームを実行し、統計情報を返す
     *
     * @param gameId ゲームID（例: "game_001"）
     * @param strategies プレイヤーの戦略リスト
     * @return ゲーム統計情報
     */
    fun runSingleGame(
        gameId: String,
        strategies: List<PlayerStrategy>,
    ): GameStatistics {
        val startTimestamp: Long = System.currentTimeMillis()

        // プレイヤーの作成（Player1, Player2, ...）
        val players: List<Player> =
            strategies.mapIndexed { index, strategy ->
                Player("Player${index + 1}", strategy)
            }

        // ゲームの初期化（新しいボードと新しいサイコロを毎回作成）
        val board: Board = createStandardBoard()
        val gameState =
            GameState(
                players = players,
                board = board,
            )
        val dice = StandardDice()
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        // ゲームの実行（最大1000ターンで制限）
        val winner: Player = gameService.runGame(gameState, dice, maxTurns = 1000)

        // 破産順序を抽出（PlayerBankruptedイベントから）
        val bankruptcyOrder: List<String> =
            gameState.events
                .filterIsInstance<GameEvent.PlayerBankrupted>()
                .map { it.playerName }

        // 最終資産を計算（全プレイヤー）
        val finalAssets: Map<String, Int> =
            players.associate { player ->
                player.name to player.money
            }

        return GameStatistics(
            gameId = gameId,
            timestamp = startTimestamp,
            turnCount = gameState.turnNumber,
            winner = winner.name,
            finalAssets = finalAssets,
            bankruptcyOrder = bankruptcyOrder,
        )
    }

    /**
     * 複数ゲームを実行し、統計情報のリストを返す
     *
     * @param gameCount 実行するゲーム数
     * @param strategies プレイヤーの戦略リスト
     * @return ゲーム統計情報のリスト
     */
    fun runExperiment(
        gameCount: Int,
        strategies: List<PlayerStrategy>,
    ): List<GameStatistics> {
        val results: MutableList<GameStatistics> = mutableListOf()

        for (i in 1..gameCount) {
            val gameId: String = "game_%03d".format(i)
            val statistics: GameStatistics = runSingleGame(gameId, strategies)
            results.add(statistics)

            // 10ゲームごとに進捗を表示
            if (i % PROGRESS_INTERVAL == 0 || i == gameCount) {
                progressCallback?.invoke(i, gameCount)
            }
        }

        return results
    }

    companion object {
        /** 進捗表示の間隔（ゲーム数） */
        private const val PROGRESS_INTERVAL = 10
    }
}
