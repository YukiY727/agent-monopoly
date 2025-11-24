package com.monopoly.domain.model

import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PropertyTest : StringSpec({
    // Helper function to create test PropertyRent
    @Suppress("LongParameterList")
    fun createTestRent(
        base: Int = 2,
        withHouse1: Int = 10,
        withHouse2: Int = 30,
        withHouse3: Int = 90,
        withHouse4: Int = 160,
        withHotel: Int = 250,
    ): PropertyRent =
        PropertyRent(
            base = base,
            withHouse1 = withHouse1,
            withHouse2 = withHouse2,
            withHouse3 = withHouse3,
            withHouse4 = withHouse4,
            withHotel = withHotel,
        )

    // TC-010: Property初期化
    // Given: なし
    // When: 新しいPropertyを作成（名前、位置、価格、レント指定）
    // Then: フィールドが正しく設定され、ownershipがUnowned
    "property should be initialized with correct fields and unowned status" {
        val rent: PropertyRent = createTestRent()
        val property: Property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = rent,
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            )

        property.name shouldBe "Mediterranean Avenue"
        property.position shouldBe 1
        property.price shouldBe 60
        property.rent shouldBe rent
        property.houseCost shouldBe 50
        property.hotelCost shouldBe 50
        property.colorGroup shouldBe ColorGroup.BROWN
        property.ownership shouldBe PropertyOwnership.Unowned
        property.buildings shouldBe PropertyBuildings()
    }

    // TC-011: 所有者設定
    // Given: 未所有のProperty
    // When: 所有者をPlayerに設定
    // Then: ownershipがOwnedByPlayerになる
    "should set owner correctly" {
        val property: Property =
            Property(
                name = "Park Place",
                position = 37,
                price = 350,
                rent = createTestRent(base = 35),
                houseCost = 200,
                hotelCost = 200,
                colorGroup = ColorGroup.DARK_BLUE,
            )
        val player: Player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        val ownedProperty: Property = property.withOwner(player)

        ownedProperty.ownership shouldBe PropertyOwnership.OwnedByPlayer(player)
    }

    // TC-012: 所有判定（未所有）
    // Given: ownershipがUnownedのProperty
    // When: isOwned()
    // Then: false
    "should return false when property is not owned" {
        val property: Property =
            Property(
                name = "Boardwalk",
                position = 39,
                price = 400,
                rent = createTestRent(base = 50),
                houseCost = 200,
                hotelCost = 200,
                colorGroup = ColorGroup.DARK_BLUE,
            )

        property.isOwned() shouldBe false
    }

    // TC-013: 所有判定（所有済み）
    // Given: ownershipがOwnedByPlayerのProperty
    // When: isOwned()
    // Then: true
    "should return true when property is owned" {
        val property: Property =
            Property(
                name = "Boardwalk",
                position = 39,
                price = 400,
                rent = createTestRent(base = 50),
                houseCost = 200,
                hotelCost = 200,
                colorGroup = ColorGroup.DARK_BLUE,
            )
        val player: Player = Player(name = "Bob", strategy = AlwaysBuyStrategy())

        val ownedProperty: Property = property.withOwner(player)

        ownedProperty.isOwned() shouldBe true
    }

    // TC-014: 所有者解除
    "should remove owner correctly" {
        val property: Property =
            Property(
                name = "Park Place",
                position = 37,
                price = 350,
                rent = createTestRent(base = 35),
                houseCost = 200,
                hotelCost = 200,
                colorGroup = ColorGroup.DARK_BLUE,
            )
        val player: Player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val ownedProperty: Property = property.withOwner(player)

        val unownedProperty: Property = ownedProperty.withoutOwner()

        unownedProperty.ownership shouldBe PropertyOwnership.Unowned
        unownedProperty.isOwned() shouldBe false
    }

    // TC-015: positionValueアクセサー
    "should expose position as BoardPosition value object" {
        val property: Property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = createTestRent(),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            )

        property.positionValue shouldBe BoardPosition(1)
    }

    // TC-016: priceValueアクセサー
    "should expose price as Money value object" {
        val property: Property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = createTestRent(),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            )

        property.priceValue shouldBe Money(60)
    }

    // TC-017: rentValueアクセサー（建物なしの基本家賃）
    "should expose rent as Money value object" {
        val property: Property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = createTestRent(),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            )

        property.rentValue shouldBe Money(2)
    }
})
