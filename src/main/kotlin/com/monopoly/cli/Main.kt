package com.monopoly.cli

import com.monopoly.domain.model.game.Board
import com.monopoly.domain.model.game.GameState
import com.monopoly.domain.model.game.createStandardBoard
import com.monopoly.domain.model.game.impl.StandardDice
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.service.BuildingService
import com.monopoly.domain.service.GameService
import com.monopoly.domain.service.MonopolyCheckerService

@Suppress("MagicNumber")
fun main() {
    println("=".repeat(60))
    println("Monopoly Game - Phase 1")
    println("=".repeat(60))
    println()

    // プレイヤーの作成（StrategyFactory経由で戦略を取得）
    val alwaysStrategy: PlayerStrategy = StrategyFactory.create("always")
    val player1: Player = Player("Alice", alwaysStrategy)
    val player2: Player = Player("Bob", StrategyFactory.create("always"))
    println("Players:")
    println("  - ${player1.name} (always)")
    println("  - ${player2.name} (always)")
    println()

    // ゲームの初期化
    val board: Board = createStandardBoard()
    val gameState =
        GameState(
            players = listOf(player1, player2),
            board = board,
        )
    val dice = StandardDice()
    val gameService = GameService(BuildingService(MonopolyCheckerService()))

    println("Starting game...")
    println()

    // ゲームの実行
    val winner = gameService.runGame(gameState, dice)

    // 結果の表示
    println("=".repeat(60))
    println("Game Over!")
    println("=".repeat(60))
    println()
    println("Winner: ${winner.name}")
    println("Final Money: \$${winner.money}")
    println("Properties Owned: ${winner.ownedProperties.size}")
    println("Total Assets: \$${winner.getTotalAssets()}")
    println()
    println("Game Statistics:")
    println("  - Total Turns: ${gameState.turnNumber}")
    println("  - Active Players: ${gameState.getActivePlayerCount()}")
    println()

    // 全プレイヤーの最終状態
    println("Final Player Status:")
    gameState.players.forEach { player ->
        val status: String = if (player.isBankrupt) "BANKRUPT" else "ACTIVE"
        val playerInfo: String =
            "  - ${player.name}: $status | " +
                "Money: \$${player.money} | " +
                "Properties: ${player.ownedProperties.size} | " +
                "Total Assets: \$${player.getTotalAssets()}"
        println(playerInfo)
    }
    println()
    println("=".repeat(60))
}
