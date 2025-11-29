package com.monopoly.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class ColorGroupTest : StringSpec({
    // TC-ColorGroup-001: All color groups are defined
    // Given: なし
    // When: ColorGroup.entries を取得
    // Then: 8種類のカラーグループがすべて存在する
    "should have all 8 color groups defined" {
        val colorGroups = ColorGroup.entries

        colorGroups shouldHaveSize 8
        colorGroups shouldContain ColorGroup.BROWN
        colorGroups shouldContain ColorGroup.LIGHT_BLUE
        colorGroups shouldContain ColorGroup.PINK
        colorGroups shouldContain ColorGroup.ORANGE
        colorGroups shouldContain ColorGroup.RED
        colorGroups shouldContain ColorGroup.YELLOW
        colorGroups shouldContain ColorGroup.GREEN
        colorGroups shouldContain ColorGroup.DARK_BLUE
    }

    // TC-ColorGroup-002: BROWN color group
    // Given: なし
    // When: ColorGroup.BROWNを取得
    // Then: BROWN定数が存在する
    "BROWN color group should exist" {
        val colorGroup = ColorGroup.BROWN

        colorGroup.name shouldBe "BROWN"
    }

    // TC-ColorGroup-003: LIGHT_BLUE color group
    // Given: なし
    // When: ColorGroup.LIGHT_BLUEを取得
    // Then: LIGHT_BLUE定数が存在する
    "LIGHT_BLUE color group should exist" {
        val colorGroup = ColorGroup.LIGHT_BLUE

        colorGroup.name shouldBe "LIGHT_BLUE"
    }

    // TC-ColorGroup-004: PINK color group
    // Given: なし
    // When: ColorGroup.PINKを取得
    // Then: PINK定数が存在する
    "PINK color group should exist" {
        val colorGroup = ColorGroup.PINK

        colorGroup.name shouldBe "PINK"
    }

    // TC-ColorGroup-005: ORANGE color group
    // Given: なし
    // When: ColorGroup.ORANGEを取得
    // Then: ORANGE定数が存在する
    "ORANGE color group should exist" {
        val colorGroup = ColorGroup.ORANGE

        colorGroup.name shouldBe "ORANGE"
    }

    // TC-ColorGroup-006: RED color group
    // Given: なし
    // When: ColorGroup.REDを取得
    // Then: RED定数が存在する
    "RED color group should exist" {
        val colorGroup = ColorGroup.RED

        colorGroup.name shouldBe "RED"
    }

    // TC-ColorGroup-007: YELLOW color group
    // Given: なし
    // When: ColorGroup.YELLOWを取得
    // Then: YELLOW定数が存在する
    "YELLOW color group should exist" {
        val colorGroup = ColorGroup.YELLOW

        colorGroup.name shouldBe "YELLOW"
    }

    // TC-ColorGroup-008: GREEN color group
    // Given: なし
    // When: ColorGroup.GREENを取得
    // Then: GREEN定数が存在する
    "GREEN color group should exist" {
        val colorGroup = ColorGroup.GREEN

        colorGroup.name shouldBe "GREEN"
    }

    // TC-ColorGroup-009: DARK_BLUE color group
    // Given: なし
    // When: ColorGroup.DARK_BLUEを取得
    // Then: DARK_BLUE定数が存在する
    "DARK_BLUE color group should exist" {
        val colorGroup = ColorGroup.DARK_BLUE

        colorGroup.name shouldBe "DARK_BLUE"
    }

    // TC-ColorGroup-010: valueOf works correctly
    // Given: 文字列の名前
    // When: ColorGroup.valueOfを使用
    // Then: 対応するColorGroupが返される
    "valueOf should return correct color group" {
        ColorGroup.valueOf("BROWN") shouldBe ColorGroup.BROWN
        ColorGroup.valueOf("LIGHT_BLUE") shouldBe ColorGroup.LIGHT_BLUE
        ColorGroup.valueOf("PINK") shouldBe ColorGroup.PINK
        ColorGroup.valueOf("ORANGE") shouldBe ColorGroup.ORANGE
        ColorGroup.valueOf("RED") shouldBe ColorGroup.RED
        ColorGroup.valueOf("YELLOW") shouldBe ColorGroup.YELLOW
        ColorGroup.valueOf("GREEN") shouldBe ColorGroup.GREEN
        ColorGroup.valueOf("DARK_BLUE") shouldBe ColorGroup.DARK_BLUE
    }

    // TC-ColorGroup-011: Enum ordinal values
    // Given: なし
    // When: ordinalを確認
    // Then: 定義順序通りの値が返される
    "should have correct ordinal values based on definition order" {
        ColorGroup.BROWN.ordinal shouldBe 0
        ColorGroup.LIGHT_BLUE.ordinal shouldBe 1
        ColorGroup.PINK.ordinal shouldBe 2
        ColorGroup.ORANGE.ordinal shouldBe 3
        ColorGroup.RED.ordinal shouldBe 4
        ColorGroup.YELLOW.ordinal shouldBe 5
        ColorGroup.GREEN.ordinal shouldBe 6
        ColorGroup.DARK_BLUE.ordinal shouldBe 7
    }

    // TC-ColorGroup-012: Color groups used in properties
    // Given: Property with ColorGroup
    // When: colorGroupフィールドにアクセス
    // Then: 正しいColorGroupが設定される
    "color groups should be usable in Property objects" {
        val brownProperty: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val blueProperty: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Park Place",
                position = 37,
                price = 350,
                baseRent = 35,
                colorGroup = ColorGroup.DARK_BLUE,
            )
        val greenProperty: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Pacific Avenue",
                position = 26,
                price = 300,
                baseRent = 26,
                colorGroup = ColorGroup.GREEN,
            )

        (brownProperty as StreetProperty).colorGroup shouldBe ColorGroup.BROWN
        (blueProperty as StreetProperty).colorGroup shouldBe ColorGroup.DARK_BLUE
        (greenProperty as StreetProperty).colorGroup shouldBe ColorGroup.GREEN
    }
})
