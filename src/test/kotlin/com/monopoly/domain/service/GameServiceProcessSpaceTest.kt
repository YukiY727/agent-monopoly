package com.monopoly.domain.service

import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameServiceProcessSpaceTest : StringSpec({
    // TC-140: 未所有プロパティに止まる（購入する）
    // Given: 所持金$1500のPlayer、価格$200の未所有Property、AlwaysBuyStrategy
    // When: processSpace(player, gameState)（playerが該当Propertyの位置）
    // Then: プロパティが購入される
    "should buy unowned property when landing on it" {
        val gameService = GameService()
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 200,
                rent = 20,
                colorGroup = ColorGroup.BROWN,
            )
        val board = BoardFixtures.createBoardWithProperties(listOf(property))
        val gameState = GameState(listOf(player), board)

        // プレイヤーを位置1に移動
        player.setPosition(1)

        gameService.processSpace(player, gameState)

        player.money shouldBe 1300 // 1500 - 200
        player.ownedProperties.size shouldBe 1
    }

    // TC-141: 他プレイヤーのプロパティに止まる（レント支払い）
    // Given: Player A（所持金$1500）、Player Bが所有するProperty（レント$50）、Player Aがその位置
    // When: processSpace(playerA, gameState)
    // Then: Player Aの所持金が$1450、Player Bの所持金が増加
    "should pay rent when landing on other player's property" {
        val gameService = GameService()
        val playerA = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val playerB = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val property =
            Property(
                name = "Park Place",
                position = 2,
                price = 350,
                rent = 50,
                colorGroup = ColorGroup.DARK_BLUE,
            )
        val board = BoardFixtures.createBoardWithProperties(listOf(property))
        val gameState = GameState(listOf(playerA, playerB), board)

        // Player Bがプロパティを所有
        val ownedProperty = property.withOwner(playerB)
        playerB.acquireProperty(ownedProperty)
        board.updateProperty(ownedProperty)

        // Player Aがその位置に移動
        playerA.setPosition(2)

        gameService.processSpace(playerA, gameState)

        playerA.money shouldBe 1450 // 1500 - 50
        playerB.money shouldBe 1550 // 1500 + 50
    }

    // TC-142: 自分のプロパティに止まる（何も起きない）
    // Given: Player A、Player Aが所有するProperty、Player Aがその位置
    // When: processSpace(playerA, gameState)
    // Then: 何も変化しない
    "should do nothing when landing on own property" {
        val gameService = GameService()
        val playerA = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property =
            Property(
                name = "Boardwalk",
                position = 3,
                price = 400,
                rent = 50,
                colorGroup = ColorGroup.DARK_BLUE,
            )
        val board = BoardFixtures.createBoardWithProperties(listOf(property))
        val gameState = GameState(listOf(playerA), board)

        // Player Aがプロパティを所有
        val ownedProperty = property.withOwner(playerA)
        playerA.acquireProperty(ownedProperty)
        board.updateProperty(ownedProperty)

        // Player Aがその位置に移動
        playerA.setPosition(3)

        val initialMoney = playerA.money

        gameService.processSpace(playerA, gameState)

        playerA.money shouldBe initialMoney
        playerA.ownedProperties.size shouldBe 1
    }
})
