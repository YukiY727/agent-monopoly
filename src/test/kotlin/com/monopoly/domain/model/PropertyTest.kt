package com.monopoly.domain.model

import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PropertyTest : StringSpec({
    // TC-010: Property初期化
    // Given: なし
    // When: 新しいPropertyを作成（名前、位置、価格、レント指定）
    // Then: フィールドが正しく設定され、ownerがnull
    "property should be initialized with correct fields and null owner" {
        val property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = 2,
                colorGroup = ColorGroup.BROWN,
            )

        property.name shouldBe "Mediterranean Avenue"
        property.position shouldBe 1
        property.price shouldBe 60
        property.rent shouldBe 2
        property.colorGroup shouldBe ColorGroup.BROWN
        property.getOwner() shouldBe null
    }

    // TC-011: 所有者設定
    // Given: 未所有のProperty
    // When: 所有者をPlayerに設定
    // Then: getOwner()がそのPlayerを返す
    "should set owner correctly" {
        val property = Property("Park Place", 37, 350, 35, ColorGroup.DARK_BLUE)
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        property.setOwner(player)

        property.getOwner() shouldBe player
    }

    // TC-012: 所有判定（未所有）
    // Given: owner=nullのProperty
    // When: isOwned()
    // Then: false
    "should return false when property is not owned" {
        val property = Property("Boardwalk", 39, 400, 50, ColorGroup.DARK_BLUE)

        property.isOwned() shouldBe false
    }

    // TC-013: 所有判定（所有済み）
    // Given: ownerがPlayerのProperty
    // When: isOwned()
    // Then: true
    "should return true when property is owned" {
        val property = Property("Boardwalk", 39, 400, 50, ColorGroup.DARK_BLUE)
        val player = Player(name = "Bob", strategy = AlwaysBuyStrategy())

        property.setOwner(player)

        property.isOwned() shouldBe true
    }
})
