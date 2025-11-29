package com.monopoly.domain.service

import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.PropertyOwnership
import com.monopoly.domain.model.PropertyTestFixtures
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class GameServiceBuyTest : StringSpec({
    // TC-110: 購入成功
    // Given: 所持金$1500のPlayer、価格$200の未所有Property
    // When: buyProperty(player, property)
    // Then: プレイヤーの所持金が$1300、propertyのownerがplayer、プレイヤーの所有プロパティに追加
    "should successfully buy property when player has enough money" {
        // Given
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
            )
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        // When
        val updatedProperty: Property = gameService.buyProperty(player, property)

        // Then
        player.money shouldBe 1300
        updatedProperty.ownership shouldBe PropertyOwnership.OwnedByPlayer(player)
        player.ownedProperties.size shouldBe 1
    }

    // TC-111: 購入後の所有者設定
    // Given: 未所有Property
    // When: buyProperty(player, property)
    // Then: property.ownership is OwnedByPlayer(player)
    "should set owner correctly after purchase" {
        // Given
        val player = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Park Place",
                position = 37,
                price = 350,
                baseRent = 35,
                colorGroup = ColorGroup.DARK_BLUE,
            )
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        // When
        val updatedProperty: Property = gameService.buyProperty(player, property)

        // Then
        updatedProperty.ownership shouldBe PropertyOwnership.OwnedByPlayer(player)
    }

    // TC-112: 購入後のプロパティリスト
    // Given: プロパティ0個のPlayer
    // When: buyProperty(player, property)
    // Then: player.ownedProperties.size が1
    "should add property to player's owned properties list" {
        // Given
        val player = Player(name = "Carol", strategy = AlwaysBuyStrategy())
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Boardwalk",
                position = 39,
                price = 400,
                baseRent = 50,
                colorGroup = ColorGroup.DARK_BLUE,
            )
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        // When
        gameService.buyProperty(player, property)

        // Then
        player.ownedProperties.size shouldBe 1
        player.ownedProperties[0].name shouldBe "Boardwalk"
    }

    // Phase 2: イベント記録のテスト

    // TC-232: buyProperty実行後、PropertyPurchasedイベントが記録される
    // Given: 所持金$1500のPlayer、価格$200のProperty、GameState
    // When: buyProperty(player, property, gameState)
    // Then: gameState.eventsにPropertyPurchasedイベントが追加されている、priceが200
    "should record PropertyPurchased event when buying property" {
        // Given
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
            )
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        // When
        gameService.buyProperty(player, property, gameState)

        // Then
        player.money shouldBe 1300
        val purchaseEvent: GameEvent.PropertyPurchased? =
            gameState.events.filterIsInstance<GameEvent.PropertyPurchased>().firstOrNull()
        purchaseEvent shouldNotBe null
        purchaseEvent?.playerName shouldBe "Alice"
        purchaseEvent?.propertyName shouldBe "Mediterranean Avenue"
        purchaseEvent?.price shouldBe 200
    }
})
