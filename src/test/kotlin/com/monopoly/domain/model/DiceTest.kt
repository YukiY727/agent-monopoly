package com.monopoly.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import kotlin.random.Random

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

    // TC-041: カスタムRandomの使用
    "should use provided random generator" {
        val fixedRandom = Random(42)
        val dice = Dice(fixedRandom)

        val result = dice.roll()

        result shouldBeInRange 2..12
    }

    // TC-042: 決定的な結果
    "should produce deterministic results with seeded random" {
        val dice1 = Dice(Random(123))
        val dice2 = Dice(Random(123))

        val result1 = dice1.roll()
        val result2 = dice2.roll()

        result1 shouldBe result2
    }

    // TC-043: 最小値のロール
    "should roll minimum value of 2 when both dice show 1" {
        val minRandom =
            object : Random() {
                override fun nextBits(bitCount: Int): Int = 0

                override fun nextInt(until: Int): Int = 1

                override fun nextInt(
                    from: Int,
                    until: Int,
                ): Int = 1
            }
        val dice = Dice(minRandom)

        val result = dice.roll()

        result shouldBe 2
    }

    // TC-044: 最大値のロール
    "should roll maximum value of 12 when both dice show 6" {
        val maxRandom =
            object : Random() {
                override fun nextBits(bitCount: Int): Int = Int.MAX_VALUE

                override fun nextInt(until: Int): Int = 6

                override fun nextInt(
                    from: Int,
                    until: Int,
                ): Int = 6
            }
        val dice = Dice(maxRandom)

        val result = dice.roll()

        result shouldBe 12
    }

    // Phase 2: lastロール記録機能のテスト

    // TC-240: getLastRollが最後のサイコロの目を返す
    // Given: Dice
    // When: roll()を実行
    // Then: getLastRoll()が最後のdie1, die2を返す
    "getLastRoll should return last dice values" {
        val fixedRandom =
            object : Random() {
                private var callCount = 0

                override fun nextBits(bitCount: Int): Int = callCount++

                override fun nextInt(until: Int): Int = if (callCount == 0) 3 else 4

                override fun nextInt(
                    from: Int,
                    until: Int,
                ): Int {
                    callCount++
                    return if (callCount == 1) 3 else 4
                }
            }
        val dice = Dice(fixedRandom)

        dice.roll()
        val (die1, die2) = dice.getLastRoll()

        die1 shouldBe 3
        die2 shouldBe 4
    }

    // TC-241: getLastRollが複数回roll後も正しい値を返す
    // Given: Dice
    // When: roll()を2回実行
    // Then: getLastRoll()が2回目のdie1, die2を返す
    "getLastRoll should return latest dice values after multiple rolls" {
        val dice = Dice(Random(999))

        dice.roll() // 最初のロール
        val secondRollTotal = dice.roll() // 2回目のロール
        val (die1, die2) = dice.getLastRoll()

        // 2回目のロール結果と一致すること
        (die1 + die2) shouldBe secondRollTotal
    }
})
