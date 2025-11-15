package com.monopoly.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class SpaceTest : StringSpec({
    // TC-Space-001: Go space initialization
    // Given: なし
    // When: Goスペースを作成
    // Then: position と spaceType が正しく設定される
    "Go space should be initialized with correct position and type" {
        val goSpace = Space.Go(position = 0)

        goSpace.position shouldBe 0
        goSpace.spaceType shouldBe SpaceType.GO
    }

    // TC-Space-002: PropertySpace initialization
    // Given: Property
    // When: PropertySpaceを作成
    // Then: position, spaceType, property が正しく設定される
    "PropertySpace should be initialized with correct position, type and property" {
        val property = Property(
            name = "Mediterranean Avenue",
            position = 1,
            price = 60,
            rent = 2,
            colorGroup = ColorGroup.BROWN,
        )
        val propertySpace = Space.PropertySpace(position = 1, property = property)

        propertySpace.position shouldBe 1
        propertySpace.spaceType shouldBe SpaceType.PROPERTY
        propertySpace.property shouldBe property
    }

    // TC-Space-003: PropertySpace property access
    // Given: PropertySpace
    // When: propertyフィールドにアクセス
    // Then: 正しいPropertyが返される
    "PropertySpace should allow access to property field" {
        val property = Property(
            name = "Park Place",
            position = 37,
            price = 350,
            rent = 35,
            colorGroup = ColorGroup.DARK_BLUE,
        )
        val propertySpace = Space.PropertySpace(position = 37, property = property)

        propertySpace.property.name shouldBe "Park Place"
        propertySpace.property.price shouldBe 350
    }

    // TC-Space-004: Other space with different types
    // Given: なし
    // When: Other スペースを異なるSpaceTypeで作成
    // Then: position と spaceType が正しく設定される
    "Other space should be initialized with custom space type" {
        val chanceSpace = Space.Other(position = 7, spaceType = SpaceType.CHANCE)
        val taxSpace = Space.Other(position = 4, spaceType = SpaceType.TAX)

        chanceSpace.position shouldBe 7
        chanceSpace.spaceType shouldBe SpaceType.CHANCE

        taxSpace.position shouldBe 4
        taxSpace.spaceType shouldBe SpaceType.TAX
    }

    // TC-Space-005: Sealed class type checking
    // Given: 異なるSpaceサブクラス
    // When: インスタンスタイプをチェック
    // Then: 正しいサブクラスとして認識される
    "Space subclasses should be correctly identified" {
        val goSpace: Space = Space.Go(0)
        val propertySpace: Space = Space.PropertySpace(
            1,
            Property("Test", 1, 60, 2, ColorGroup.BROWN),
        )
        val otherSpace: Space = Space.Other(7, SpaceType.CHANCE)

        goSpace.shouldBeInstanceOf<Space.Go>()
        propertySpace.shouldBeInstanceOf<Space.PropertySpace>()
        otherSpace.shouldBeInstanceOf<Space.Other>()
    }

    // TC-Space-006: Go space at different positions
    // Given: なし
    // When: 異なる位置でGoスペースを作成（通常は0だが、他の位置でも作成可能）
    // Then: 指定した位置が正しく設定される
    "Go space can be created at any position" {
        val goSpace = Space.Go(position = 10)

        goSpace.position shouldBe 10
        goSpace.spaceType shouldBe SpaceType.GO
    }

    // TC-Space-007: Other space for all applicable space types
    // Given: なし
    // When: Other スペースを様々なSpaceTypeで作成
    // Then: それぞれのタイプが正しく設定される
    "Other space should support various space types" {
        val communityChest = Space.Other(2, SpaceType.COMMUNITY_CHEST)
        val railroad = Space.Other(5, SpaceType.RAILROAD)
        val utility = Space.Other(12, SpaceType.UTILITY)
        val freeParking = Space.Other(20, SpaceType.FREE_PARKING)
        val goToJail = Space.Other(30, SpaceType.GO_TO_JAIL)
        val jail = Space.Other(10, SpaceType.JAIL)

        communityChest.spaceType shouldBe SpaceType.COMMUNITY_CHEST
        railroad.spaceType shouldBe SpaceType.RAILROAD
        utility.spaceType shouldBe SpaceType.UTILITY
        freeParking.spaceType shouldBe SpaceType.FREE_PARKING
        goToJail.spaceType shouldBe SpaceType.GO_TO_JAIL
        jail.spaceType shouldBe SpaceType.JAIL
    }
})
