package com.monopoly.cli

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.PropertyRent
import com.monopoly.domain.model.Space
import com.monopoly.domain.model.SpaceType
import com.monopoly.domain.service.GameService
import com.monopoly.domain.strategy.AlwaysBuyStrategy

@Suppress("MagicNumber")
private fun createPropertyRent(base: Int): PropertyRent =
    PropertyRent(
        base = base,
        withHouse1 = base * 5,
        withHouse2 = base * 15,
        withHouse3 = base * 45,
        withHouse4 = base * 80,
        withHotel = base * 125,
    )

@Suppress("MagicNumber")
private fun createStandardBoard(): Board {
    val spaceList = mutableListOf<Space>()

    // 位置0: GO
    spaceList.add(Space.Go(0))

    // 位置1-39: 全28プロパティを定義
    for (i in 1..39) {
        val space =
            when (i) {
                // Brown (2 properties)
                1 ->
                    Space.PropertySpace(
                        position = 1,
                        property =
                            Property(
                                name = "Mediterranean Avenue",
                                position = 1,
                                price = 60,
                                rent = createPropertyRent(2),
                                houseCost = 50,
                                hotelCost = 50,
                                colorGroup = ColorGroup.BROWN,
                            ),
                    )
                3 ->
                    Space.PropertySpace(
                        position = 3,
                        property =
                            Property(
                                name = "Baltic Avenue",
                                position = 3,
                                price = 60,
                                rent = createPropertyRent(4),
                                houseCost = 50,
                                hotelCost = 50,
                                colorGroup = ColorGroup.BROWN,
                            ),
                    )
                // Light Blue (3 properties)
                6 ->
                    Space.PropertySpace(
                        position = 6,
                        property =
                            Property(
                                name = "Oriental Avenue",
                                position = 6,
                                price = 100,
                                rent = createPropertyRent(6),
                                houseCost = 50,
                                hotelCost = 50,
                                colorGroup = ColorGroup.LIGHT_BLUE,
                            ),
                    )
                8 ->
                    Space.PropertySpace(
                        position = 8,
                        property =
                            Property(
                                name = "Vermont Avenue",
                                position = 8,
                                price = 100,
                                rent = createPropertyRent(6),
                                houseCost = 50,
                                hotelCost = 50,
                                colorGroup = ColorGroup.LIGHT_BLUE,
                            ),
                    )
                9 ->
                    Space.PropertySpace(
                        position = 9,
                        property =
                            Property(
                                name = "Connecticut Avenue",
                                position = 9,
                                price = 120,
                                rent = createPropertyRent(8),
                                houseCost = 50,
                                hotelCost = 50,
                                colorGroup = ColorGroup.LIGHT_BLUE,
                            ),
                    )
                // Pink (3 properties)
                11 ->
                    Space.PropertySpace(
                        position = 11,
                        property =
                            Property(
                                name = "St. Charles Place",
                                position = 11,
                                price = 140,
                                rent = createPropertyRent(10),
                                houseCost = 100,
                                hotelCost = 100,
                                colorGroup = ColorGroup.PINK,
                            ),
                    )
                13 ->
                    Space.PropertySpace(
                        position = 13,
                        property =
                            Property(
                                name = "States Avenue",
                                position = 13,
                                price = 140,
                                rent = createPropertyRent(10),
                                houseCost = 100,
                                hotelCost = 100,
                                colorGroup = ColorGroup.PINK,
                            ),
                    )
                14 ->
                    Space.PropertySpace(
                        position = 14,
                        property =
                            Property(
                                name = "Virginia Avenue",
                                position = 14,
                                price = 160,
                                rent = createPropertyRent(12),
                                houseCost = 100,
                                hotelCost = 100,
                                colorGroup = ColorGroup.PINK,
                            ),
                    )
                // Orange (3 properties)
                16 ->
                    Space.PropertySpace(
                        position = 16,
                        property =
                            Property(
                                name = "St. James Place",
                                position = 16,
                                price = 180,
                                rent = createPropertyRent(14),
                                houseCost = 100,
                                hotelCost = 100,
                                colorGroup = ColorGroup.ORANGE,
                            ),
                    )
                18 ->
                    Space.PropertySpace(
                        position = 18,
                        property =
                            Property(
                                name = "Tennessee Avenue",
                                position = 18,
                                price = 180,
                                rent = createPropertyRent(14),
                                houseCost = 100,
                                hotelCost = 100,
                                colorGroup = ColorGroup.ORANGE,
                            ),
                    )
                19 ->
                    Space.PropertySpace(
                        position = 19,
                        property =
                            Property(
                                name = "New York Avenue",
                                position = 19,
                                price = 200,
                                rent = createPropertyRent(16),
                                houseCost = 100,
                                hotelCost = 100,
                                colorGroup = ColorGroup.ORANGE,
                            ),
                    )
                // Red (3 properties)
                21 ->
                    Space.PropertySpace(
                        position = 21,
                        property =
                            Property(
                                name = "Kentucky Avenue",
                                position = 21,
                                price = 220,
                                rent = createPropertyRent(18),
                                houseCost = 150,
                                hotelCost = 150,
                                colorGroup = ColorGroup.RED,
                            ),
                    )
                23 ->
                    Space.PropertySpace(
                        position = 23,
                        property =
                            Property(
                                name = "Indiana Avenue",
                                position = 23,
                                price = 220,
                                rent = createPropertyRent(18),
                                houseCost = 150,
                                hotelCost = 150,
                                colorGroup = ColorGroup.RED,
                            ),
                    )
                24 ->
                    Space.PropertySpace(
                        position = 24,
                        property =
                            Property(
                                name = "Illinois Avenue",
                                position = 24,
                                price = 240,
                                rent = createPropertyRent(20),
                                houseCost = 150,
                                hotelCost = 150,
                                colorGroup = ColorGroup.RED,
                            ),
                    )
                // Yellow (3 properties)
                26 ->
                    Space.PropertySpace(
                        position = 26,
                        property =
                            Property(
                                name = "Atlantic Avenue",
                                position = 26,
                                price = 260,
                                rent = createPropertyRent(22),
                                houseCost = 150,
                                hotelCost = 150,
                                colorGroup = ColorGroup.YELLOW,
                            ),
                    )
                27 ->
                    Space.PropertySpace(
                        position = 27,
                        property =
                            Property(
                                name = "Ventnor Avenue",
                                position = 27,
                                price = 260,
                                rent = createPropertyRent(22),
                                houseCost = 150,
                                hotelCost = 150,
                                colorGroup = ColorGroup.YELLOW,
                            ),
                    )
                29 ->
                    Space.PropertySpace(
                        position = 29,
                        property =
                            Property(
                                name = "Marvin Gardens",
                                position = 29,
                                price = 280,
                                rent = createPropertyRent(24),
                                houseCost = 150,
                                hotelCost = 150,
                                colorGroup = ColorGroup.YELLOW,
                            ),
                    )
                // Green (3 properties)
                31 ->
                    Space.PropertySpace(
                        position = 31,
                        property =
                            Property(
                                name = "Pacific Avenue",
                                position = 31,
                                price = 300,
                                rent = createPropertyRent(26),
                                houseCost = 200,
                                hotelCost = 200,
                                colorGroup = ColorGroup.GREEN,
                            ),
                    )
                32 ->
                    Space.PropertySpace(
                        position = 32,
                        property =
                            Property(
                                name = "North Carolina Avenue",
                                position = 32,
                                price = 300,
                                rent = createPropertyRent(26),
                                houseCost = 200,
                                hotelCost = 200,
                                colorGroup = ColorGroup.GREEN,
                            ),
                    )
                34 ->
                    Space.PropertySpace(
                        position = 34,
                        property =
                            Property(
                                name = "Pennsylvania Avenue",
                                position = 34,
                                price = 320,
                                rent = createPropertyRent(28),
                                houseCost = 200,
                                hotelCost = 200,
                                colorGroup = ColorGroup.GREEN,
                            ),
                    )
                // Dark Blue (2 properties)
                37 ->
                    Space.PropertySpace(
                        position = 37,
                        property =
                            Property(
                                name = "Park Place",
                                position = 37,
                                price = 350,
                                rent = createPropertyRent(35),
                                houseCost = 200,
                                hotelCost = 200,
                                colorGroup = ColorGroup.DARK_BLUE,
                            ),
                    )
                39 ->
                    Space.PropertySpace(
                        position = 39,
                        property =
                            Property(
                                name = "Boardwalk",
                                position = 39,
                                price = 400,
                                rent = createPropertyRent(50),
                                houseCost = 200,
                                hotelCost = 200,
                                colorGroup = ColorGroup.DARK_BLUE,
                            ),
                    )
                // その他のマス (鉄道、ユーティリティ、税金など) は後のPhaseで実装
                else -> Space.Other(i, SpaceType.CHANCE)
            }
        spaceList.add(space)
    }

    return Board(spaceList)
}

@Suppress("MagicNumber")
fun main() {
    println("=".repeat(60))
    println("Monopoly Game - Phase 1")
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
