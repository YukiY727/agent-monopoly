package com.monopoly.domain.service
import com.monopoly.domain.strategy.AlwaysBuyStrategy

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * Mortgage（抵当）システムのテスト
 *
 * Phase 18: 抵当システムの実装
 */
class MortgageServiceTest : DescribeSpec({
    val mortgageService = MortgageService()

    describe("抵当設定") {
        it("should mortgage property for 50% of price") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()
            val gameState = GameState(listOf(player), board)

            val property = Property(
                name = "Test Property",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
            ).withOwner(player)

            player.addProperty(property)
            val initialMoney = player.money

            val mortgaged = mortgageService.mortgageProperty(player, property, gameState)

            mortgaged.isMortgaged shouldBe true
            player.money shouldBe initialMoney + 100 // 50% of $200
        }
    }

    describe("抵当解除") {
        it("should unmortgage property for 110% of mortgage value") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()
            val gameState = GameState(listOf(player), board)

            val property = Property(
                name = "Test Property",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
                isMortgaged = true,
            ).withOwner(player)

            player.addProperty(property)
            player.addMoney(1000)
            val initialMoney = player.money

            val unmortgaged = mortgageService.unmortgageProperty(player, property, gameState)

            unmortgaged.isMortgaged shouldBe false
            player.money shouldBe initialMoney - 110 // 110% of $100
        }
    }
})
