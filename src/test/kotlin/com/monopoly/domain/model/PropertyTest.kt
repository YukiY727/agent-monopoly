package com.monopoly.domain.model

import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PropertyTest : StringSpec({
    // TC-010: Property初期化
    // Given: なし
    // When: 新しいPropertyを作成（名前、位置、価格、レント指定）
    // Then: フィールドが正しく設定され、ownershipがUnowned
    "property should be initialized with correct fields and unowned status" {
        val property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )

        property.name shouldBe "Mediterranean Avenue"
        property.position shouldBe 1
        property.price shouldBe 60
        property.rent shouldBe 2
        property.colorGroup shouldBe ColorGroup.BROWN
        property.ownership shouldBe PropertyOwnership.Unowned
    }

    // TC-011: 所有者設定
    // Given: 未所有のProperty
    // When: 所有者をPlayerに設定
    // Then: ownershipがOwnedByPlayerになる
    "should set owner correctly" {
        val property = Property("Park Place", 37, 350, 35, ColorGroup.DARK_BLUE)
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        val ownedProperty = property.withOwner(player)

        ownedProperty.ownership shouldBe PropertyOwnership.OwnedByPlayer(player)
    }

    // TC-012: 所有判定（未所有）
    // Given: ownershipがUnownedのProperty
    // When: isOwned()
    // Then: false
    "should return false when property is not owned" {
        val property = Property("Boardwalk", 39, 400, 50, ColorGroup.DARK_BLUE)

        property.isOwned() shouldBe false
    }

    // TC-013: 所有判定（所有済み）
    // Given: ownershipがOwnedByPlayerのProperty
    // When: isOwned()
    // Then: true
    "should return true when property is owned" {
        val property = Property("Boardwalk", 39, 400, 50, ColorGroup.DARK_BLUE)
        val player = Player(name = "Bob", strategy = AlwaysBuyStrategy())

        val ownedProperty = property.withOwner(player)

        ownedProperty.isOwned() shouldBe true
    }

    // TC-014: 所有者解除
    "should remove owner correctly" {
        val property = Property("Park Place", 37, 350, 35, ColorGroup.DARK_BLUE)
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val ownedProperty = property.withOwner(player)

        val unownedProperty = ownedProperty.withoutOwner()

        unownedProperty.ownership shouldBe PropertyOwnership.Unowned
        unownedProperty.isOwned() shouldBe false
    }

    // TC-015: positionValueアクセサー
    "should expose position as BoardPosition value object" {
        val property = Property("Mediterranean Avenue", 1, 60, 2, ColorGroup.BROWN)

        property.positionValue shouldBe BoardPosition(1)
    }

    // TC-016: priceValueアクセサー
    "should expose price as Money value object" {
        val property = Property("Mediterranean Avenue", 1, 60, 2, ColorGroup.BROWN)

        property.priceValue shouldBe Money(60)
    }

    // TC-017: rentValueアクセサー
    "should expose rent as Money value object" {
        val property = Property("Mediterranean Avenue", 1, 60, 2, ColorGroup.BROWN)

        property.rentValue shouldBe Money(2)
    }
})
