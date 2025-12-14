package com.monopoly.domain.experiment

import com.monopoly.domain.model.game.createStandardBoard
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

        // プレイヤーごとの行動統計を収集
        val playerStatistics: List<PlayerStatistics> = collectPlayerStatistics(players, gameState.events)

        return GameStatistics(
            gameId = gameId,
            timestamp = startTimestamp,
            turnCount = gameState.turnNumber,
            winner = winner.name,
            finalAssets = finalAssets,
            bankruptcyOrder = bankruptcyOrder,
            playerStatistics = playerStatistics,
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

    /**
     * イベントログからプレイヤーごとの行動統計を収集
     */
    private fun collectPlayerStatistics(
        players: List<Player>,
        events: List<GameEvent>,
    ): List<PlayerStatistics> =
        players.map { player ->
            val name: String = player.name

            // プロパティ購入数
            val propertiesPurchased: Int =
                events
                    .filterIsInstance<GameEvent.PropertyPurchased>()
                    .count { it.playerName == name }

            // 家建設数
            val housesBuild: Int =
                events
                    .filterIsInstance<GameEvent.HouseBuilt>()
                    .count { it.playerName == name }

            // ホテル建設数
            val hotelsBuilt: Int =
                events
                    .filterIsInstance<GameEvent.HotelBuilt>()
                    .count { it.playerName == name }

            // 支払った家賃総額
            val rentPaid: Int =
                events
                    .filterIsInstance<GameEvent.RentPaid>()
                    .filter { it.payerName == name }
                    .sumOf { it.amount }

            // 受け取った家賃総額
            val rentReceived: Int =
                events
                    .filterIsInstance<GameEvent.RentPaid>()
                    .filter { it.receiverName == name }
                    .sumOf { it.amount }

            // 刑務所に入った回数
            val timesInJail: Int =
                events
                    .filterIsInstance<GameEvent.PlayerSentToJail>()
                    .count { it.playerName == name }

            // トレード提案数
            val tradesProposed: Int =
                events
                    .filterIsInstance<GameEvent.TradeProposed>()
                    .count { it.proposerName == name }

            // トレード受け入れ数（相手からの提案を受け入れた回数）
            val tradesAccepted: Int =
                events
                    .filterIsInstance<GameEvent.TradeAccepted>()
                    .count { it.targetName == name }

            // 成立したトレード数（提案者として）
            val tradesCompleted: Int =
                events
                    .filterIsInstance<GameEvent.TradeCompleted>()
                    .count { it.proposerName == name }

            PlayerStatistics(
                name = name,
                propertiesPurchased = propertiesPurchased,
                housesBuild = housesBuild,
                hotelsBuilt = hotelsBuilt,
                rentPaid = rentPaid,
                rentReceived = rentReceived,
                timesInJail = timesInJail,
                finalMoney = player.money,
                tradesProposed = tradesProposed,
                tradesAccepted = tradesAccepted,
                tradesCompleted = tradesCompleted,
            )
        }

    companion object {
        /** 進捗表示の間隔（ゲーム数） */
        private const val PROGRESS_INTERVAL = 10
    }
}
