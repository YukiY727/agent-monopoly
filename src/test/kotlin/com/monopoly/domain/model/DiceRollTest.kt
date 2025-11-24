package com.monopoly.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiceRollTest {
    @Test
    fun `total should be sum of die1 and die2`() {
        // Arrange
        val diceRoll = DiceRoll(die1 = 3, die2 = 4)

        // Act & Assert
        assertEquals(7, diceRoll.total)
    }

    @Test
    fun `isDoubles should be true when die1 equals die2`() {
        // Arrange
        val diceRoll = DiceRoll(die1 = 5, die2 = 5)

        // Act & Assert
        assertTrue(diceRoll.isDoubles)
    }

    @Test
    fun `isDoubles should be false when die1 does not equal die2`() {
        // Arrange
        val diceRoll = DiceRoll(die1 = 3, die2 = 4)

        // Act & Assert
        assertFalse(diceRoll.isDoubles)
    }

    @Test
    fun `should handle minimum values`() {
        // Arrange
        val diceRoll = DiceRoll(die1 = 1, die2 = 1)

        // Act & Assert
        assertEquals(2, diceRoll.total)
        assertTrue(diceRoll.isDoubles)
    }

    @Test
    fun `should handle maximum values`() {
        // Arrange
        val diceRoll = DiceRoll(die1 = 6, die2 = 6)

        // Act & Assert
        assertEquals(12, diceRoll.total)
        assertTrue(diceRoll.isDoubles)
    }
}
