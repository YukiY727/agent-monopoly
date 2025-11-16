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
import com.monopoly.domain.strategy.BuyStrategy
import com.monopoly.domain.strategy.ConservativeStrategy
import com.monopoly.domain.strategy.RandomStrategy
import kotlin.system.exitProcess

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

fun createStrategy(strategyName: String): BuyStrategy =
    when (strategyName.lowercase()) {
        "always-buy" -> AlwaysBuyStrategy()
        "random" -> RandomStrategy()
        "conservative" -> ConservativeStrategy()
        else -> {
            println("Error: Unknown strategy '$strategyName'")
            println("Available strategies: always-buy, random, conservative")
            println("Use --help for more information")
            exitProcess(1)
        }
    }

fun parseArgs(args: Array<String>): String {
    if (args.contains("--help")) {
        printHelp()
        exitProcess(0)
    }

    val strategyIndex: Int = args.indexOf("--strategy")
    if (strategyIndex != -1 && strategyIndex + 1 < args.size) {
        return args[strategyIndex + 1]
    }

    return "always-buy" // デフォルト
}

fun printHelp() {
    println(
        """
        Monopoly Game Simulator - Phase 3

        Usage: ./gradlew run --args="--strategy <strategy-name>"

        Options:
          --strategy <name>  戦略を指定
                             always-buy: 常に購入する（Phase 1の戦略）
                             random: ランダムに購入する
                             conservative: 一定額以上の現金を保持
                             デフォルト: always-buy
          --help             ヘルプを表示

        Examples:
          ./gradlew run --args="--strategy always-buy"
          ./gradlew run --args="--strategy random"
          ./gradlew run --args="--strategy conservative"
        """.trimIndent(),
    )
}

@Suppress("MagicNumber")
fun main(args: Array<String>) {
    val strategyName: String = parseArgs(args)
    val strategy1: BuyStrategy = createStrategy(strategyName)
    val strategy2: BuyStrategy = createStrategy(strategyName)

    println("=".repeat(60))
    println("Monopoly Game - Phase 3")
    println("Strategy: $strategyName")
    println("=".repeat(60))
    println()

    // プレイヤーの作成
    val player1: Player = Player("Alice", strategy1)
    val player2: Player = Player("Bob", strategy2)
    println("Players:")
    println("  - ${player1.name} ($strategyName)")
    println("  - ${player2.name} ($strategyName)")
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
