package com.monopoly.domain.model

import kotlin.random.Random

class Dice(private val random: Random = Random.Default) {
    @Suppress("MagicNumber") // サイコロの標準的な値（1-6）
    fun roll(): Int {
        val die1 = random.nextInt(1, 7) // 1-6
        val die2 = random.nextInt(1, 7) // 1-6
        return die1 + die2 // 2-12
    }
}
