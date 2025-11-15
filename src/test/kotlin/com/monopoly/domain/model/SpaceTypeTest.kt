package com.monopoly.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class SpaceTypeTest : StringSpec({
    // TC-SpaceType-001: All space types are defined
    // Given: なし
    // When: SpaceType.entries を取得
    // Then: 10種類のスペースタイプがすべて存在する
    "should have all 10 space types defined" {
        val spaceTypes = SpaceType.entries

        spaceTypes shouldHaveSize 10
        spaceTypes shouldContain SpaceType.GO
        spaceTypes shouldContain SpaceType.PROPERTY
        spaceTypes shouldContain SpaceType.CHANCE
        spaceTypes shouldContain SpaceType.COMMUNITY_CHEST
        spaceTypes shouldContain SpaceType.TAX
        spaceTypes shouldContain SpaceType.RAILROAD
        spaceTypes shouldContain SpaceType.UTILITY
        spaceTypes shouldContain SpaceType.FREE_PARKING
        spaceTypes shouldContain SpaceType.GO_TO_JAIL
        spaceTypes shouldContain SpaceType.JAIL
    }

    // TC-SpaceType-002: GO type
    // Given: なし
    // When: SpaceType.GOを取得
    // Then: GO定数が存在する
    "GO space type should exist" {
        val spaceType = SpaceType.GO

        spaceType.name shouldBe "GO"
    }

    // TC-SpaceType-003: PROPERTY type
    // Given: なし
    // When: SpaceType.PROPERTYを取得
    // Then: PROPERTY定数が存在する
    "PROPERTY space type should exist" {
        val spaceType = SpaceType.PROPERTY

        spaceType.name shouldBe "PROPERTY"
    }

    // TC-SpaceType-004: CHANCE type
    // Given: なし
    // When: SpaceType.CHANCEを取得
    // Then: CHANCE定数が存在する
    "CHANCE space type should exist" {
        val spaceType = SpaceType.CHANCE

        spaceType.name shouldBe "CHANCE"
    }

    // TC-SpaceType-005: COMMUNITY_CHEST type
    // Given: なし
    // When: SpaceType.COMMUNITY_CHESTを取得
    // Then: COMMUNITY_CHEST定数が存在する
    "COMMUNITY_CHEST space type should exist" {
        val spaceType = SpaceType.COMMUNITY_CHEST

        spaceType.name shouldBe "COMMUNITY_CHEST"
    }

    // TC-SpaceType-006: TAX type
    // Given: なし
    // When: SpaceType.TAXを取得
    // Then: TAX定数が存在する
    "TAX space type should exist" {
        val spaceType = SpaceType.TAX

        spaceType.name shouldBe "TAX"
    }

    // TC-SpaceType-007: RAILROAD type
    // Given: なし
    // When: SpaceType.RAILROADを取得
    // Then: RAILROAD定数が存在する
    "RAILROAD space type should exist" {
        val spaceType = SpaceType.RAILROAD

        spaceType.name shouldBe "RAILROAD"
    }

    // TC-SpaceType-008: UTILITY type
    // Given: なし
    // When: SpaceType.UTILITYを取得
    // Then: UTILITY定数が存在する
    "UTILITY space type should exist" {
        val spaceType = SpaceType.UTILITY

        spaceType.name shouldBe "UTILITY"
    }

    // TC-SpaceType-009: FREE_PARKING type
    // Given: なし
    // When: SpaceType.FREE_PARKINGを取得
    // Then: FREE_PARKING定数が存在する
    "FREE_PARKING space type should exist" {
        val spaceType = SpaceType.FREE_PARKING

        spaceType.name shouldBe "FREE_PARKING"
    }

    // TC-SpaceType-010: GO_TO_JAIL type
    // Given: なし
    // When: SpaceType.GO_TO_JAILを取得
    // Then: GO_TO_JAIL定数が存在する
    "GO_TO_JAIL space type should exist" {
        val spaceType = SpaceType.GO_TO_JAIL

        spaceType.name shouldBe "GO_TO_JAIL"
    }

    // TC-SpaceType-011: JAIL type
    // Given: なし
    // When: SpaceType.JAILを取得
    // Then: JAIL定数が存在する
    "JAIL space type should exist" {
        val spaceType = SpaceType.JAIL

        spaceType.name shouldBe "JAIL"
    }

    // TC-SpaceType-012: valueOf works correctly
    // Given: 文字列の名前
    // When: SpaceType.valueOfを使用
    // Then: 対応するSpaceTypeが返される
    "valueOf should return correct space type" {
        SpaceType.valueOf("GO") shouldBe SpaceType.GO
        SpaceType.valueOf("PROPERTY") shouldBe SpaceType.PROPERTY
        SpaceType.valueOf("CHANCE") shouldBe SpaceType.CHANCE
        SpaceType.valueOf("JAIL") shouldBe SpaceType.JAIL
    }

    // TC-SpaceType-013: Enum ordinal values
    // Given: なし
    // When: ordinalを確認
    // Then: 定義順序通りの値が返される
    "should have correct ordinal values based on definition order" {
        SpaceType.GO.ordinal shouldBe 0
        SpaceType.PROPERTY.ordinal shouldBe 1
        SpaceType.CHANCE.ordinal shouldBe 2
        SpaceType.COMMUNITY_CHEST.ordinal shouldBe 3
        SpaceType.TAX.ordinal shouldBe 4
        SpaceType.RAILROAD.ordinal shouldBe 5
        SpaceType.UTILITY.ordinal shouldBe 6
        SpaceType.FREE_PARKING.ordinal shouldBe 7
        SpaceType.GO_TO_JAIL.ordinal shouldBe 8
        SpaceType.JAIL.ordinal shouldBe 9
    }
})
