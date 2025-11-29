package com.monopoly.domain.service

import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.BoardPosition
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.PropertyTestFixtures
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import com.monopoly.domain.model.BuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameServiceProcessSpaceEdgeCaseTest : StringSpec({
    val gameService = GameService(BuildingService(MonopolyCheckerService()))

    // GOマスに止まるケース
    // Given: PlayerがGOマスに止まっている
    // When: processSpace(player, gameState)
    // Then: 何も起こらない（GOボーナスはadvanceで処理済み）
    "should do nothing when landing on GO space" {
        val player = Player("Alice", AlwaysBuyStrategy())
        player.moveTo(BoardPosition.GO)
        val gameState =
            GameState(
                players = listOf(player),
                board = BoardFixtures.createStandardBoard(),
            )

        val initialMoney = player.money

        gameService.processSpace(player, gameState)

        player.money shouldBe initialMoney
    }

    // Otherマス（CHANCE, COMMUNITY CHESTなど）に止まるケース
    // Given: PlayerがOtherマス（CHANCE）に止まっている
    // When: processSpace(player, gameState)
    // Then: 何も起こらない（Phase 1では未実装）
    "should do nothing when landing on Other space like CHANCE" {
        val player = Player("Bob", AlwaysBuyStrategy())
        // 位置2はCHANCEマス（BoardFixtures参照）
        player.moveTo(BoardPosition(2))
        val gameState =
            GameState(
                players = listOf(player),
                board = BoardFixtures.createStandardBoard(),
            )

        val initialMoney = player.money

        gameService.processSpace(player, gameState)

        player.money shouldBe initialMoney
    }

    // プレイヤーが購入しないケース
    // Given: 所持金が不足しているPlayer、未所有Property
    // When: processSpace(player, gameState)
    // Then: プロパティは購入されない
    "should not buy property when player does not have enough money" {
        // 所持金が不足する戦略
        val neverBuyStrategy =
            object : BuyStrategy {
                override fun shouldBuy(
                    property: Property,
                    currentMoney: Int,
                ): Boolean = false

                override fun shouldBuildHouse(
                    property: com.monopoly.domain.model.StreetProperty,
                    currentMoney: Int,
                ): Boolean = false

                override fun shouldBuildHotel(
                    property: com.monopoly.domain.model.StreetProperty,
                    currentMoney: Int,
                ): Boolean = false
            }

        val player = Player("Charlie", neverBuyStrategy)
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 200,
                baseRent = 20,
                colorGroup = ColorGroup.BROWN,
            )
        val board = BoardFixtures.createBoardWithProperties(listOf(property))
        val gameState = GameState(listOf(player), board)

        player.moveTo(BoardPosition(1))

        val initialMoney = player.money

        gameService.processSpace(player, gameState)

        player.money shouldBe initialMoney
        player.ownedProperties.size shouldBe 0
    }
})
