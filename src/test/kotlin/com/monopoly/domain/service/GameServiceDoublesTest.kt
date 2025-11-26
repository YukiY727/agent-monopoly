package com.monopoly.domain.service

import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.impl.StandardDice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.random.Random

class GameServiceDoublesTest : StringSpec({
    // TC-300: ゾロ目を出したときにDoublesRolledイベントが記録される
    "should record DoublesRolled event when doubles are rolled" {
        // Arrange: ゾロ目(3,3)を出すDice
        val doublesRandom =
            object : Random() {
                override fun nextBits(bitCount: Int): Int = 0

                override fun nextInt(
                    from: Int,
                    until: Int,
                ): Int = 3
            }
        val dice = StandardDice(doublesRandom)

        val player1 = Player("Player 1", AlwaysBuyStrategy())
        val player2 = Player("Player 2", AlwaysBuyStrategy())
        val gameState = GameState(listOf(player1, player2), BoardFixtures.createStandardBoard())
        val gameService = GameService()

        // Act
        gameService.executeTurn(gameState, dice)

        // Assert
        val doublesEvents = gameState.events.filterIsInstance<GameEvent.DoublesRolled>()
        doublesEvents.size shouldBe 1
        doublesEvents[0].playerName shouldBe "Player 1"
        doublesEvents[0].doublesCount shouldBe 1
    }

    // TC-301: ゾロ目を出したときにプレイヤーの連続ゾロ目カウントが増える
    "should increment consecutive doubles count when doubles are rolled" {
        // Arrange
        val doublesRandom =
            object : Random() {
                override fun nextBits(bitCount: Int): Int = 0

                override fun nextInt(
                    from: Int,
                    until: Int,
                ): Int = 4
            }
        val dice = StandardDice(doublesRandom)

        val player1 = Player("Player 1", AlwaysBuyStrategy())
        val gameState = GameState(listOf(player1), BoardFixtures.createStandardBoard())
        val gameService = GameService()

        // Act
        gameService.executeTurn(gameState, dice)

        // Assert
        // PlayerStateのconsecutiveDoublesが1になっているはず
        // (実装後に確認方法を追加)
    }

    // TC-302: ゾロ目でないときに連続ゾロ目カウントがリセットされる
    "should reset consecutive doubles count when non-doubles are rolled" {
        // Arrange: 非ゾロ目(3,5)を出すDice
        val nonDoublesRandom =
            object : Random() {
                private var callCount = 0

                override fun nextBits(bitCount: Int): Int = callCount++

                override fun nextInt(
                    from: Int,
                    until: Int,
                ): Int {
                    callCount++
                    return if (callCount == 1) 3 else 5
                }
            }
        val dice = StandardDice(nonDoublesRandom)

        val player1 = Player("Player 1", AlwaysBuyStrategy())
        val gameState = GameState(listOf(player1), BoardFixtures.createStandardBoard())
        val gameService = GameService()

        // Act
        gameService.executeTurn(gameState, dice)

        // Assert
        val doublesEvents = gameState.events.filterIsInstance<GameEvent.DoublesRolled>()
        doublesEvents.size shouldBe 0
    }

    // TC-303: 3回連続ゾロ目でThreeConsecutiveDoublesイベントが記録される
    "should record ThreeConsecutiveDoubles event on third consecutive doubles" {
        // Arrange: 常にゾロ目を出すDice
        val doublesRandom =
            object : Random() {
                override fun nextBits(bitCount: Int): Int = 0

                override fun nextInt(
                    from: Int,
                    until: Int,
                ): Int = 5
            }
        val dice = StandardDice(doublesRandom)

        val player1 = Player("Player 1", AlwaysBuyStrategy())
        val gameState = GameState(listOf(player1), BoardFixtures.createStandardBoard())
        val gameService = GameService()

        // Act: 3回連続でターンを実行
        gameService.executeTurn(gameState, dice) // 1回目
        gameService.executeTurn(gameState, dice) // 2回目
        gameService.executeTurn(gameState, dice) // 3回目

        // Assert
        val threeDoublesEvents = gameState.events.filterIsInstance<GameEvent.ThreeConsecutiveDoubles>()
        threeDoublesEvents.size shouldBe 1
        threeDoublesEvents[0].playerName shouldBe "Player 1"
    }

    // TC-304: 3回連続ゾロ目後に連続ゾロ目カウントがリセットされる
    "should reset consecutive doubles count after three consecutive doubles" {
        // Arrange
        val doublesRandom =
            object : Random() {
                override fun nextBits(bitCount: Int): Int = 0

                override fun nextInt(
                    from: Int,
                    until: Int,
                ): Int = 6
            }
        val dice = StandardDice(doublesRandom)

        val player1 = Player("Player 1", AlwaysBuyStrategy())
        val gameState = GameState(listOf(player1), BoardFixtures.createStandardBoard())
        val gameService = GameService()

        // Act: 3回連続でターンを実行
        gameService.executeTurn(gameState, dice) // 1回目
        gameService.executeTurn(gameState, dice) // 2回目
        gameService.executeTurn(gameState, dice) // 3回目

        // Assert: 3回目の後、次のターンでは連続ゾロ目カウントが0からスタート
        // (実装後に確認方法を追加)
    }

    // TC-305: 2回連続ゾロ目の後、非ゾロ目でカウントリセット
    "should reset count after two doubles followed by non-doubles" {
        // Arrange: 最初2回はゾロ目、3回目は非ゾロ目
        var callCount = 0
        val mixedRandom =
            object : Random() {
                override fun nextBits(bitCount: Int): Int = callCount++

                override fun nextInt(
                    from: Int,
                    until: Int,
                ): Int {
                    callCount++
                    // 最初の2ターンはゾロ目、3ターン目は非ゾロ目
                    return when {
                        callCount <= 4 -> 4 // 1回目と2回目: (4,4)
                        callCount == 5 -> 2 // 3回目: (2,5)
                        else -> 5
                    }
                }
            }
        val dice = StandardDice(mixedRandom)

        val player1 = Player("Player 1", AlwaysBuyStrategy())
        val gameState = GameState(listOf(player1), BoardFixtures.createStandardBoard())
        val gameService = GameService()

        // Act
        gameService.executeTurn(gameState, dice) // 1回目: ゾロ目
        gameService.executeTurn(gameState, dice) // 2回目: ゾロ目
        gameService.executeTurn(gameState, dice) // 3回目: 非ゾロ目

        // Assert
        val doublesEvents = gameState.events.filterIsInstance<GameEvent.DoublesRolled>()
        doublesEvents.size shouldBe 2 // 1回目と2回目のみ

        val threeDoublesEvents = gameState.events.filterIsInstance<GameEvent.ThreeConsecutiveDoubles>()
        threeDoublesEvents.size shouldBe 0 // 3回連続ではない
    }
})
