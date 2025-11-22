package com.monopoly.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * PropertyBuildingsのテスト
 * 建物（家とホテル）の管理が正しく動作することを確認
 */
class PropertyBuildingsTest : StringSpec({
    "デフォルトで建物がない状態で初期化されること" {
        // Given & When
        val buildings = PropertyBuildings()

        // Then
        buildings.houseCount shouldBe 0
        buildings.hasHotel shouldBe false
    }

    "家が0件の状態で初期化できること" {
        // Given & When
        val buildings = PropertyBuildings(houseCount = 0, hasHotel = false)

        // Then
        buildings.houseCount shouldBe 0
        buildings.hasHotel shouldBe false
    }

    "家が1-4件の範囲で初期化できること" {
        // Given & When & Then
        for (count in 1..4) {
            val buildings = PropertyBuildings(houseCount = count, hasHotel = false)
            buildings.houseCount shouldBe count
            buildings.hasHotel shouldBe false
        }
    }

    "ホテルのみの状態で初期化できること" {
        // Given & When
        val buildings = PropertyBuildings(houseCount = 0, hasHotel = true)

        // Then
        buildings.houseCount shouldBe 0
        buildings.hasHotel shouldBe true
    }

    "家が5件以上でエラーになること" {
        // Given & When & Then
        shouldThrow<IllegalArgumentException> {
            PropertyBuildings(houseCount = 5, hasHotel = false)
        }
    }

    "家が負の数でエラーになること" {
        // Given & When & Then
        shouldThrow<IllegalArgumentException> {
            PropertyBuildings(houseCount = -1, hasHotel = false)
        }
    }

    "ホテルと家が同時に存在する場合エラーになること" {
        // Given & When & Then
        shouldThrow<IllegalArgumentException> {
            PropertyBuildings(houseCount = 1, hasHotel = true)
        }

        shouldThrow<IllegalArgumentException> {
            PropertyBuildings(houseCount = 4, hasHotel = true)
        }
    }

    "canBuildHouseが家0-3件の時trueを返すこと" {
        // Given & When & Then
        for (count in 0..3) {
            val buildings = PropertyBuildings(houseCount = count, hasHotel = false)
            buildings.canBuildHouse() shouldBe true
        }
    }

    "canBuildHouseが家4件の時falseを返すこと" {
        // Given
        val buildings = PropertyBuildings(houseCount = 4, hasHotel = false)

        // When & Then
        buildings.canBuildHouse() shouldBe false
    }

    "canBuildHouseがホテルがある時falseを返すこと" {
        // Given
        val buildings = PropertyBuildings(houseCount = 0, hasHotel = true)

        // When & Then
        buildings.canBuildHouse() shouldBe false
    }

    "canBuildHotelが家4件の時trueを返すこと" {
        // Given
        val buildings = PropertyBuildings(houseCount = 4, hasHotel = false)

        // When & Then
        buildings.canBuildHotel() shouldBe true
    }

    "canBuildHotelが家4件未満の時falseを返すこと" {
        // Given & When & Then
        for (count in 0..3) {
            val buildings = PropertyBuildings(houseCount = count, hasHotel = false)
            buildings.canBuildHotel() shouldBe false
        }
    }

    "canBuildHotelがすでにホテルがある時falseを返すこと" {
        // Given
        val buildings = PropertyBuildings(houseCount = 0, hasHotel = true)

        // When & Then
        buildings.canBuildHotel() shouldBe false
    }

    "getTotalBuildingCountが家の数を返すこと" {
        // Given & When & Then
        for (count in 0..4) {
            val buildings = PropertyBuildings(houseCount = count, hasHotel = false)
            buildings.getTotalBuildingCount() shouldBe count
        }
    }

    "getTotalBuildingCountがホテルの場合5を返すこと" {
        // Given
        val buildings = PropertyBuildings(houseCount = 0, hasHotel = true)

        // When & Then
        buildings.getTotalBuildingCount() shouldBe 5
    }
})
