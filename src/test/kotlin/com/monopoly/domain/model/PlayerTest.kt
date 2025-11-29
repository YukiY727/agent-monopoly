package com.monopoly.domain.model

import com.monopoly.domain.model.StreetProperty
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
            StreetProperty(
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
            StreetProperty(
                name = "Prop1",
                position = 1,
                price = 200,
                rent = PropertyRent(10, 50, 150, 450, 800, 1250),
                houseCost = 100,
                hotelCost = 100,
                colorGroup = ColorGroup.BROWN,
            )
        val property2: Property =
            StreetProperty(
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
            StreetProperty(
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
            StreetProperty(
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
            StreetProperty(
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

    // TC-019: Multiple payments leading to bankruptcy
    // Given: 所持金$1500のPlayer
    // When: 複数回の支払いで資金が徐々に減り、最終的に破産
    // Then: 破産フラグがtrueになる
    "should go bankrupt after multiple payments exceeding available money" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.pay(Money(800)) // 1500 - 800 = 700
        player.isBankrupt shouldBe false
        player.money shouldBe 700

        player.pay(Money(500)) // 700 - 500 = 200
        player.isBankrupt shouldBe false
        player.money shouldBe 200

        player.pay(Money(300)) // 200 - 300 = -100
        player.isBankrupt shouldBe true
        player.money shouldBe -100
    }

    // TC-020: Calculate total assets with many properties
    // Given: 複数のプロパティを所有するPlayer
    // When: calculateTotalAssets()
    // Then: 所持金とすべてのプロパティ価値の合計が返される
    "should calculate total assets correctly with multiple properties" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        player.pay(Money(300)) // 1500 - 300 = 1200

        val property1: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Prop1",
                position = 1,
                price = 100,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
            )
        val property2: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Prop2",
                position = 3,
                price = 150,
                baseRent = 15,
                colorGroup = ColorGroup.BROWN,
            )
        val property3: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Prop3",
                position = 5,
                price = 200,
                baseRent = 20,
                colorGroup = ColorGroup.LIGHT_BLUE,
            )
        val property4: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Prop4",
                position = 7,
                price = 250,
                baseRent = 25,
                colorGroup = ColorGroup.PINK,
            )

        player.acquireProperty(property1)
        player.acquireProperty(property2)
        player.acquireProperty(property3)
        player.acquireProperty(property4)

        // 1200 + 100 + 150 + 200 + 250 = 1900
        player.calculateTotalAssets() shouldBe Money(1900)
    }

    // TC-021: Receive money multiple times
    // Given: 所持金$1500のPlayer
    // When: 複数回お金を受け取る
    // Then: 所持金が累積される
    "should accumulate money when receiving multiple times" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.receiveMoney(Money(100))
        player.money shouldBe 1600

        player.receiveMoney(Money(50))
        player.money shouldBe 1650

        player.receiveMoney(Money(200))
        player.money shouldBe 1850
    }

    // TC-022: Position wrapping on advance
    // Given: 位置39のPlayer
    // When: 1マス前進
    // Then: 位置0（GO）に戻り、GOボーナスを受け取る
    "should wrap position to 0 and receive GO bonus when advancing from position 39" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        player.setPosition(39)

        val passedGo = player.advance(1)

        passedGo shouldBe true
        player.position shouldBe 0
        player.money shouldBe 1700 // 1500 + 200
    }

    // TC-023: Advance multiple laps
    // Given: 位置0のPlayer
    // When: 80マス前進（2周）
    // Then: 位置0に戻り、GOボーナスを受け取る
    "should receive GO bonus when advancing multiple laps" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        val passedGo = player.advance(80) // 2 full laps

        passedGo shouldBe true
        player.position shouldBe 0
        player.money shouldBe 1700 // 1500 + 200
    }

    // TC-024: Property ownership after bankruptcy
    // Given: プロパティを持つPlayer
    // When: 破産する
    // Then: すべてのプロパティが失われる
    "should lose all properties when going bankrupt" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property1: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Prop1",
                position = 1,
                price = 100,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
            )
        val property2: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Prop2",
                position = 3,
                price = 150,
                baseRent = 15,
                colorGroup = ColorGroup.BROWN,
            )
        val property3: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Prop3",
                position = 5,
                price = 200,
                baseRent = 20,
                colorGroup = ColorGroup.LIGHT_BLUE,
            )

        player.acquireProperty(property1)
        player.acquireProperty(property2)
        player.acquireProperty(property3)

        player.ownedProperties.size shouldBe 3

        player.goBankrupt()

        player.ownedProperties.size shouldBe 0
        player.isBankrupt shouldBe true
    }
})
