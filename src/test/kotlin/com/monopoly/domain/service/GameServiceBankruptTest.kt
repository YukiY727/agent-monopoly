package com.monopoly.domain.service

import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.PropertyOwnership
import com.monopoly.domain.model.PropertyTestFixtures
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameServiceBankruptTest : StringSpec({
    // TC-130: 破産フラグ設定
    // Given: Player
    // When: bankruptPlayer(player)
    // Then: player.isBankrupt()がtrue
    "should set bankrupt flag when player goes bankrupt" {
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        gameService.bankruptPlayer(player)

        player.isBankrupt shouldBe true
    }

    // TC-131: 所有プロパティの解放
    // Given: 2つのプロパティを所有するPlayer
    // When: bankruptPlayer(player)
    // Then: 各プロパティのownershipがUnowned、プレイヤーの所有プロパティリストが空
    "should release all properties when player goes bankrupt" {
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        val property1: Property =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)

        val property2: Property =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)

        player.addProperty(property1)
        player.addProperty(property2)

        val releasedProperties = gameService.bankruptPlayer(player)

        player.isBankrupt shouldBe true
        player.ownedProperties.size shouldBe 0
        releasedProperties.size shouldBe 2
        releasedProperties[0].ownership shouldBe PropertyOwnership.Unowned
        releasedProperties[1].ownership shouldBe PropertyOwnership.Unowned
        releasedProperties[0].isOwned() shouldBe false
        releasedProperties[1].isOwned() shouldBe false
    }

    // Phase 2: イベント記録のテスト

    // TC-234: bankruptPlayer実行後、PlayerBankruptedイベントが記録される
    // Given: Player、GameState
    // When: bankruptPlayer(player, gameState)
    // Then: gameState.eventsにPlayerBankruptedイベントが追加されている、playerNameが正しい
    "should record PlayerBankrupted event when player goes bankrupt" {
        // Given
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        player.subtractMoney(1450) // Set player's money to $50
        val board = com.monopoly.domain.model.BoardFixtures.createStandardBoard()
        val gameState = com.monopoly.domain.model.GameState(listOf(player), board)

        // When
        gameService.bankruptPlayer(player, gameState)

        // Then
        player.isBankrupt shouldBe true
        val bankruptedEvents: List<com.monopoly.domain.event.GameEvent.PlayerBankrupted> =
            gameState.events.filterIsInstance<com.monopoly.domain.event.GameEvent.PlayerBankrupted>()
        bankruptedEvents.size shouldBe 1
        val bankruptedEvent: com.monopoly.domain.event.GameEvent.PlayerBankrupted = bankruptedEvents.first()
        bankruptedEvent.playerName shouldBe "Alice"
        bankruptedEvent.finalMoney shouldBe 50
    }
})
