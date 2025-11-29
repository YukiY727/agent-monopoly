package com.monopoly.domain.service

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

class GameServiceProcessSpaceTest : StringSpec({
    // TC-140: 未所有プロパティに止まる（購入する）
    // Given: 所持金$1500のPlayer、価格$200の未所有Property、AlwaysBuyStrategy
    // When: processSpace(player, gameState)（playerが該当Propertyの位置）
    // Then: プロパティが購入される
    "should buy unowned property when landing on it" {
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
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
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        val playerA = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val playerB = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Park Place",
                position = 2,
                price = 350,
                baseRent = 50,
                colorGroup = ColorGroup.DARK_BLUE,
            )
        val board = BoardFixtures.createBoardWithProperties(listOf(property))
        val gameState = GameState(listOf(playerA, playerB), board)

        // Player Bがプロパティを所有
        val ownedProperty: Property = property.withOwner(playerB)
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
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        val playerA = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Boardwalk",
                position = 3,
                price = 400,
                baseRent = 50,
                colorGroup = ColorGroup.DARK_BLUE,
            )
        val board = BoardFixtures.createBoardWithProperties(listOf(property))
        val gameState = GameState(listOf(playerA), board)

        // Player Aがプロパティを所有
        val ownedProperty: Property = property.withOwner(playerA)
        playerA.acquireProperty(ownedProperty)
        board.updateProperty(ownedProperty)

        // Player Aがその位置に移動
        playerA.setPosition(3)

        val initialMoney = playerA.money

        gameService.processSpace(playerA, gameState)

        playerA.money shouldBe initialMoney
        playerA.ownedProperties.size shouldBe 1
    }

    // TC-143: レント支払い後に破産（プレイヤーとボード上のプロパティが解放される）
    // Given: Player A（所持金$100）が1つのプロパティを所有、Player Bのプロパティ（レント$150）
    // When: Player Aが Player Bのプロパティに止まる
    // Then: Player Aが破産し、Player Aが所有していたプロパティがボード上で解放される
    "should release player properties on board when player goes bankrupt after paying rent" {
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        val playerA = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val playerB = Player(name = "Bob", strategy = AlwaysBuyStrategy())

        // Player Aが所有するプロパティ
        val propertyOwnedByA: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 100,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
            )

        // Player Bが所有するプロパティ（高額レント）
        val propertyOwnedByB: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Park Place",
                position = 2,
                price = 350,
                baseRent = 150,
                colorGroup = ColorGroup.DARK_BLUE,
            )

        val board = BoardFixtures.createBoardWithProperties(listOf(propertyOwnedByA, propertyOwnedByB))
        val gameState = GameState(listOf(playerA, playerB), board)

        // Player Aの所持金を$100に設定
        playerA.subtractMoney(1400) // 1500 - 1400 = 100

        // Player Aがプロパティを所有
        val ownedByA = propertyOwnedByA.withOwner(playerA)
        playerA.acquireProperty(ownedByA)
        board.updateProperty(ownedByA)

        // Player Bがプロパティを所有
        val ownedByB = propertyOwnedByB.withOwner(playerB)
        playerB.acquireProperty(ownedByB)
        board.updateProperty(ownedByB)

        // processSpace前の状態を確認
        playerA.money shouldBe 100
        playerA.isBankrupt shouldBe false
        playerA.ownedProperties.size shouldBe 1
        board.getPropertyAt(1)?.ownership shouldBe PropertyOwnership.OwnedByPlayer(playerA)

        // Player Aが Player Bのプロパティに移動
        playerA.setPosition(2)

        // processSpaceを実行（レント支払い→破産）
        gameService.processSpace(playerA, gameState)

        // processSpace後の状態を確認
        // Player Aが破産している
        playerA.isBankrupt shouldBe true
        playerA.ownedProperties.size shouldBe 0
        playerA.money shouldBe -50 // 100 - 150 = -50

        // ボード上のプロパティ（position 1）がUnownedになっている
        val propertyOnBoard: Property? = board.getPropertyAt(1)
        propertyOnBoard shouldBe propertyOwnedByA.withoutOwner()
    }

    // TC-144: レント支払い前にプロパティリストを取得する必要性の確認
    // Given: Player（所持金$100、プロパティ1つ所有）
    // When: Player.pay($150)を呼び出す
    // Then: Playerが破産し、ownedPropertiesが空になる
    "player loses all properties when going bankrupt via pay method" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 100,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
            )

        // プレイヤーの所持金を$100に設定
        player.subtractMoney(1400) // 1500 - 1400 = 100

        // プレイヤーがプロパティを所有
        val ownedProperty: Property = property.withOwner(player)
        player.acquireProperty(ownedProperty)

        // pay前のプロパティ数を確認
        player.ownedProperties.size shouldBe 1

        // レント支払い（破産）
        player.pay(com.monopoly.domain.model.Money(150))

        // pay後、プレイヤーは破産し、プロパティを失っている
        player.isBankrupt shouldBe true
        player.ownedProperties.size shouldBe 0
    }
})
