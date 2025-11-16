package com.monopoly.domain.model

import kotlin.random.Random

class Dice(private val random: Random = Random.Default) {
    var lastDie1: Int = 0
        private set
    var lastDie2: Int = 0
        private set

    @Suppress("MagicNumber") // サイコロの標準的な値（1-6）
    fun roll(): Int {
        lastDie1 = random.nextInt(1, 7) // 1-6
        lastDie2 = random.nextInt(1, 7) // 1-6
        return lastDie1 + lastDie2 // 2-12
    }
}
