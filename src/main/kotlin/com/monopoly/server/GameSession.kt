package com.monopoly.server

import com.monopoly.cli.StrategyFactory
import com.monopoly.domain.model.game.GameState
import com.monopoly.domain.model.game.impl.StandardDice
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.service.BuildingService
import com.monopoly.domain.service.GameService
import com.monopoly.domain.service.MonopolyCheckerService

/**
 * ゲームセッションを管理するシングルトン
 * 簡易的に1つのゲーム状態のみを保持
 */
object GameSession {
    private var currentGameState: GameState? = null
    private val gameService = GameService(BuildingService(MonopolyCheckerService()))
    private val dice = StandardDice()

    fun startNewGame(): GameState {
        // プレイヤーを作成（StrategyFactory経由で戦略を取得）
        val players: List<Player> =
            listOf(
                Player("Alice", StrategyFactory.create("always")),
                Player("Bob", StrategyFactory.create("always")),
            )

        // ボードを作成
        val board = com.monopoly.domain.model.game.createStandardBoard()

        // ゲーム状態を初期化
        val gameState = GameState(players, board)
        currentGameState = gameState

        return gameState
    }

    fun executeTurn(): GameState {
        val gameState =
            currentGameState
                ?: throw IllegalStateException("No active game. Call startNewGame() first.")

        if (!gameState.isGameOver) {
            gameService.executeTurn(gameState, dice)

            // ゲーム終了判定: プレイヤーが1人になったらゲーム終了
            if (gameService.checkGameEnd(gameState)) {
                gameState.endGame()

                // 勝者を決定
                val activePlayers: List<Player> = gameState.players.filter { !it.isBankrupt }
                val winner: Player? = activePlayers.firstOrNull()

                // GameEndedイベントを記録
                gameState.events.add(
                    com.monopoly.domain.event.GameEvent.GameEnded(
                        turnNumber = gameState.turnNumber,
                        timestamp = System.currentTimeMillis(),
                        winner = winner?.name,
                        totalTurns = gameState.turnNumber,
                    ),
                )
            }
        }

        return gameState
    }

    fun getCurrentState(): GameState {
        return currentGameState
            ?: throw IllegalStateException("No active game. Call startNewGame() first.")
    }

    fun hasActiveGame(): Boolean = currentGameState != null
}
