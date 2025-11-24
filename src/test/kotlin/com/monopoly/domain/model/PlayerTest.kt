package com.monopoly.domain.model

import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PlayerTest : StringSpec({
    // TC-001: Player初期化
    // Given: なし
    // When: 新しいPlayerを作成
    // Then: 初期所持金が$1500、位置が0、破産フラグがfalse
    "player should be initialized with \$1500, position 0, and not bankrupt" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.money shouldBe 1500
        player.position shouldBe 0
        player.isBankrupt shouldBe false
    }

    // TC-002: 所持金の増加
    // Given: 所持金$1500のPlayer
    // When: $200を追加
    // Then: 所持金が$1700
    "should increase money when adding money" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.addMoney(200)

        player.money shouldBe 1700
    }

    // TC-003: 所持金の減少
    // Given: 所持金$1500のPlayer
    // When: $100を減らす
    // Then: 所持金が$1400
    "should decrease money when subtracting money" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.subtractMoney(100)

        player.money shouldBe 1400
    }

    // TC-004: プロパティ追加
    // Given: プロパティを持たないPlayer
    // When: Propertyを追加
    // Then: 所有プロパティリストに含まれる
    "should add property to owned properties" {
        val player: Player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property: Property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent =
                    PropertyRent(
                        base = 2,
                        withHouse1 = 10,
                        withHouse2 = 30,
                        withHouse3 = 90,
                        withHouse4 = 160,
                        withHotel = 250,
                    ),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            )

        player.addProperty(property)

        player.ownedProperties.size shouldBe 1
        player.ownedProperties[0] shouldBe property
    }

    // TC-005: 総資産計算(プロパティあり)
    // Given: 所持金$1000、価格$200のプロパティ2つ所有
    // When: getTotalAssets()
    // Then: $1400を返す
    "should calculate total assets with properties" {
        val player: Player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        player.subtractMoney(500) // 1500 - 500 = 1000

        val property1: Property =
            Property(
                name = "Prop1",
                position = 1,
                price = 200,
                rent = PropertyRent(10, 50, 150, 450, 800, 1250),
                houseCost = 100,
                hotelCost = 100,
                colorGroup = ColorGroup.BROWN,
            )
        val property2: Property =
            Property(
                name = "Prop2",
                position = 3,
                price = 200,
                rent = PropertyRent(10, 50, 150, 450, 800, 1250),
                houseCost = 100,
                hotelCost = 100,
                colorGroup = ColorGroup.BROWN,
            )
        player.addProperty(property1)
        player.addProperty(property2)

        player.getTotalAssets() shouldBe 1400
    }

    // TC-006: 総資産計算(プロパティなし)
    // Given: 所持金$1500、プロパティなし
    // When: getTotalAssets()
    // Then: $1500を返す
    "should calculate total assets without properties" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.getTotalAssets() shouldBe 1500
    }

    // TC-007: 破産フラグ
    // Given: Player
    // When: 破産フラグを設定
    // Then: isBankrupt()がtrue
    "should mark player as bankrupt when flag is set" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        // 破産処理(GameServiceが呼ぶ想定)
        player.markAsBankrupt()

        player.isBankrupt shouldBe true
    }

    // TC-008: Value objectアクセサー - moneyValue
    "should expose money as Money value object" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.moneyValue shouldBe Money.INITIAL_AMOUNT
    }

    // TC-009: Value objectアクセサー - positionValue
    "should expose position as BoardPosition value object" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.positionValue shouldBe BoardPosition.GO
    }

    // TC-010: receiveMoney
    "should receive money using Money value object" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.receiveMoney(Money(300))

        player.money shouldBe 1800
    }

    // TC-011: pay
    "should pay money using Money value object" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.pay(Money(500))

        player.money shouldBe 1000
    }

    // TC-012: moveTo
    "should move to specific position using BoardPosition" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.moveTo(BoardPosition(10))

        player.position shouldBe 10
    }

    // TC-013: advance
    "should advance position and receive GO bonus when passing GO" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        player.setPosition(35)

        val passedGo = player.advance(10)

        passedGo shouldBe true
        player.position shouldBe 5
        player.money shouldBe 1700 // 1500 + 200
    }

    // TC-014: advance without passing GO
    "should advance position without GO bonus when not passing GO" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        val passedGo = player.advance(5)

        passedGo shouldBe false
        player.position shouldBe 5
        player.money shouldBe 1500
    }

    // TC-015: acquireProperty
    "should acquire property using value object method" {
        val player: Player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property: Property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = PropertyRent(2, 10, 30, 90, 160, 250),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            )

        player.acquireProperty(property)

        player.ownedProperties.size shouldBe 1
        player.ownedProperties[0] shouldBe property
    }

    // TC-016: goBankrupt
    "should go bankrupt and clear properties" {
        val player: Player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property: Property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = PropertyRent(2, 10, 30, 90, 160, 250),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            )
        player.addProperty(property)

        player.goBankrupt()

        player.isBankrupt shouldBe true
        player.ownedProperties.size shouldBe 0
    }

    // TC-017: pay causing bankruptcy
    "should automatically go bankrupt when payment causes negative balance" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.pay(Money(2000))

        player.isBankrupt shouldBe true
        player.money shouldBe -500
    }

    // TC-018: calculateTotalAssets
    "should calculate total assets using Money value object" {
        val player: Player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property: Property =
            Property(
                name = "Prop1",
                position = 1,
                price = 200,
                rent = PropertyRent(10, 50, 150, 450, 800, 1250),
                houseCost = 100,
                hotelCost = 100,
                colorGroup = ColorGroup.BROWN,
            )
        player.addProperty(property)

        val totalAssets: Money = player.calculateTotalAssets()

        totalAssets shouldBe Money(1700)
    }
})
