package com.monopoly.domain.service
import com.monopoly.domain.strategy.AlwaysBuyStrategy

import com.monopoly.domain.model.AlwaysBuyStrategy
import com.monopoly.domain.model.Board
import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * Jail（牢屋）システムのテスト
 *
 * Phase 16: Jailシステムの実装
 */
class JailServiceTest : DescribeSpec({
    val jailService = JailService()

    describe("Jailへの収監") {
        it("should send player to jail") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()
            val gameState = GameState(listOf(player), board)

            jailService.sendToJail(player, gameState)

            player.inJail shouldBe true
            player.position shouldBe 10 // Jail position
            player.jailTurns shouldBe 0
        }
    }

    describe("Jailからの脱出 - 罰金") {
        it("should allow paying fine to exit jail") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()
            val gameState = GameState(listOf(player), board)

            jailService.sendToJail(player, gameState)
            player.addMoney(1000) // 罰金を払える資金を追加

            val result = jailService.payFineToExit(player, gameState)

            result shouldBe true
            player.inJail shouldBe false
            player.money shouldBe (1500 + 1000 - 50) // 初期$1500 + $1000 - 罰金$50
        }

        it("should not allow exiting if player cannot afford fine") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()
            val gameState = GameState(listOf(player), board)

            jailService.sendToJail(player, gameState)
            player.subtractMoney(1500) // 資金を0にする

            val result = jailService.payFineToExit(player, gameState)

            result shouldBe false
            player.inJail shouldBe true
        }
    }

    describe("Jailからの脱出 - ゾロ目") {
        it("should allow exiting with doubles") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()
            val gameState = GameState(listOf(player), board)

            jailService.sendToJail(player, gameState)

            val result = jailService.attemptExitWithDoubles(player, die1 = 3, die2 = 3, gameState)

            result shouldBe true
            player.inJail shouldBe false
        }

        it("should not allow exiting without doubles") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()
            val gameState = GameState(listOf(player), board)

            jailService.sendToJail(player, gameState)

            val result = jailService.attemptExitWithDoubles(player, die1 = 3, die2 = 4, gameState)

            result shouldBe false
            player.inJail shouldBe true
            player.jailTurns shouldBe 1
        }
    }

    describe("Jailからの脱出 - Get Out of Jail Free カード") {
        it("should allow exiting with card") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()
            val gameState = GameState(listOf(player), board)

            jailService.sendToJail(player, gameState)
            player.addGetOutOfJailFreeCard()

            val result = jailService.useGetOutOfJailFreeCard(player, gameState)

            result shouldBe true
            player.inJail shouldBe false
            player.getOutOfJailFreeCards shouldBe 0
        }

        it("should not allow exiting without card") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()
            val gameState = GameState(listOf(player), board)

            jailService.sendToJail(player, gameState)

            val result = jailService.useGetOutOfJailFreeCard(player, gameState)

            result shouldBe false
            player.inJail shouldBe true
        }
    }

    describe("Jail 3ターン経過後の強制脱出") {
        it("should force exit after 3 turns") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()
            val gameState = GameState(listOf(player), board)

            jailService.sendToJail(player, gameState)
            player.addMoney(1000)

            // 3回失敗
            jailService.attemptExitWithDoubles(player, die1 = 1, die2 = 2, gameState)
            jailService.attemptExitWithDoubles(player, die1 = 1, die2 = 3, gameState)
            jailService.attemptExitWithDoubles(player, die1 = 2, die2 = 3, gameState)

            player.jailTurns shouldBe 3
            player.inJail shouldBe false // 自動的に脱出
            player.money shouldBe (1500 + 1000 - 50) // 罰金$50が自動的に支払われる
        }
    }
})
