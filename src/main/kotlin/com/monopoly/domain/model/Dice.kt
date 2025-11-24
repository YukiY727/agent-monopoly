package com.monopoly.domain.model

import kotlin.random.Random

class Dice(private val random: Random = Random.Default) {
    private var lastDie1: Int = 0
    private var lastDie2: Int = 0

    @Suppress("MagicNumber") // サイコロの標準的な値（1-6）
    fun roll(): DiceRoll {
        lastDie1 = random.nextInt(1, 7) // 1-6
        lastDie2 = random.nextInt(1, 7) // 1-6
        return DiceRoll(die1 = lastDie1, die2 = lastDie2)
    }

    /**
     * 最後にロールしたサイコロの目を取得
     * @return (die1, die2)のPair
     */
    fun getLastRoll(): Pair<Int, Int> = Pair(lastDie1, lastDie2)
}
