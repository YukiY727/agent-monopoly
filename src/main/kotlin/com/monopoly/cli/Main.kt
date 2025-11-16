package com.monopoly.cli

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.Space
import com.monopoly.domain.model.SpaceType
import com.monopoly.domain.service.GameService
import com.monopoly.domain.strategy.AlwaysBuyStrategy

@Suppress("MagicNumber")
fun createSpaceAt(position: Int): Space =
    when (position) {
        0 -> Space.Go(0)
        1 ->
            Space.PropertySpace(
                position = 1,
                property = Property("Mediterranean Avenue", 1, 60, 2, ColorGroup.BROWN),
            )
        3 ->
            Space.PropertySpace(
                position = 3,
                property = Property("Baltic Avenue", 3, 60, 4, ColorGroup.BROWN),
            )
        6 ->
            Space.PropertySpace(
                position = 6,
                property = Property("Oriental Avenue", 6, 100, 6, ColorGroup.LIGHT_BLUE),
            )
        8 ->
            Space.PropertySpace(
                position = 8,
                property = Property("Vermont Avenue", 8, 100, 6, ColorGroup.LIGHT_BLUE),
            )
        9 ->
            Space.PropertySpace(
                position = 9,
                property = Property("Connecticut Avenue", 9, 120, 8, ColorGroup.LIGHT_BLUE),
            )
        else -> Space.Other(position, SpaceType.CHANCE)
    }

@Suppress("MagicNumber")
fun createStandardBoard(): Board {
    val spaces: List<Space> = (0..39).map { createSpaceAt(it) }
    return Board(spaces)
}

@Suppress("MagicNumber")
fun main() {
    println("=".repeat(60))
    println("Monopoly Game - Phase 2")
    println("=".repeat(60))
    println()

    // プレイヤーの作成
    val player1: Player = Player("Alice", AlwaysBuyStrategy())
    val player2: Player = Player("Bob", AlwaysBuyStrategy())
    println("Players:")
    println("  - ${player1.name} (AlwaysBuyStrategy)")
    println("  - ${player2.name} (AlwaysBuyStrategy)")
    println()

    // ゲームの初期化
    val board: Board = createStandardBoard()
    val gameState =
        GameState(
            players = listOf(player1, player2),
            board = board,
        )
    val dice = Dice()
    val gameService = GameService()
    val consoleLogger = ConsoleLogger()
    val htmlReportGenerator = HtmlReportGenerator()

    println("Starting game...")
    println()

    // ゲームの実行
    val winner = gameService.runGame(gameState, dice)

    // イベントログの表示
    println()
    println("=".repeat(60))
    println("Game Events:")
    println("=".repeat(60))
    consoleLogger.logEvents(gameState.events)

    // 結果の表示
    println()
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
    println("  - Total Events: ${gameState.events.size}")
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

    // HTMLレポートの生成
    val htmlFile = htmlReportGenerator.saveToFile(gameState)
    println("HTML report generated: ${htmlFile.absolutePath}")
    println()
    println("=".repeat(60))
}
