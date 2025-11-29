package com.monopoly.domain.model.impl

import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.DiceRoll
import kotlin.random.Random

class StandardDice(private val random: Random = Random.Default) : Dice {
    @Suppress("MagicNumber") // サイコロの標準的な値（1-6）
    override fun roll(): DiceRoll {
        val die1 = random.nextInt(1, 7) // 1-6
        val die2 = random.nextInt(1, 7) // 1-6
        return DiceRoll(die1 = die1, die2 = die2)
    }
}
