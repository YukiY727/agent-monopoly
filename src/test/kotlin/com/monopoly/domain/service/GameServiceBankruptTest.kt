package com.monopoly.domain.service

import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.PropertyOwnership
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameServiceBankruptTest : StringSpec({
    // TC-130: 破産フラグ設定
    // Given: Player
    // When: bankruptPlayer(player)
    // Then: player.isBankrupt()がtrue
    "should set bankrupt flag when player goes bankrupt" {
        val gameService = GameService()
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        gameService.bankruptPlayer(player)

        player.isBankrupt shouldBe true
    }

    // TC-131: 所有プロパティの解放
    // Given: 2つのプロパティを所有するPlayer
    // When: bankruptPlayer(player)
    // Then: 各プロパティのownershipがUnowned、プレイヤーの所有プロパティリストが空
    "should release all properties when player goes bankrupt" {
        val gameService = GameService()
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        val property1 =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = 2,
                colorGroup = ColorGroup.BROWN,
            ).withOwner(player)

        val property2 =
            Property(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                rent = 4,
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
})
