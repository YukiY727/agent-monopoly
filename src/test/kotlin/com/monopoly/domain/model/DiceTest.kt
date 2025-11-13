package com.monopoly.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeInRange

class DiceTest : StringSpec({
    // TC-040: サイコロの値範囲
    // Given: Dice
    // When: roll()を複数回実行
    // Then: すべての結果が2-12の範囲
    "dice roll should return values between 2 and 12" {
        val dice = Dice()

        // 100回ロールして全て範囲内であることを確認
        repeat(100) {
            val result = dice.roll()
            result shouldBeInRange 2..12
        }
    }
})
