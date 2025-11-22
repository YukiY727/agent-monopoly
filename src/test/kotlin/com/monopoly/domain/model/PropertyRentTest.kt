package com.monopoly.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * PropertyRentのテスト
 * 家賃構造が正しく初期化され、値を保持することを確認
 */
class PropertyRentTest : StringSpec({
    "PropertyRentが正しく初期化されること" {
        // Given & When
        val rent =
            PropertyRent(
                base = 2,
                withHouse1 = 10,
                withHouse2 = 30,
                withHouse3 = 90,
                withHouse4 = 160,
                withHotel = 250,
            )

        // Then
        rent.base shouldBe 2
        rent.withHouse1 shouldBe 10
        rent.withHouse2 shouldBe 30
        rent.withHouse3 shouldBe 90
        rent.withHouse4 shouldBe 160
        rent.withHotel shouldBe 250
    }

    "全ての家賃が0でも初期化できること" {
        // Given & When
        val rent =
            PropertyRent(
                base = 0,
                withHouse1 = 0,
                withHouse2 = 0,
                withHouse3 = 0,
                withHouse4 = 0,
                withHotel = 0,
            )

        // Then
        rent.base shouldBe 0
        rent.withHouse1 shouldBe 0
    }

    "家賃が増加する構造を表現できること（Mediterranean Avenueの例）" {
        // Given & When
        val mediterraneanRent =
            PropertyRent(
                base = 2,
                withHouse1 = 10,
                withHouse2 = 30,
                withHouse3 = 90,
                withHouse4 = 160,
                withHotel = 250,
            )

        // Then
        mediterraneanRent.base shouldBe 2
        mediterraneanRent.withHouse1 shouldBe 10
        mediterraneanRent.withHouse2 shouldBe 30
        mediterraneanRent.withHouse3 shouldBe 90
        mediterraneanRent.withHouse4 shouldBe 160
        mediterraneanRent.withHotel shouldBe 250
    }
})
