package com.monopoly.domain.model

@JvmInline
value class BoardPosition(val value: Int) {
    init {
        require(value in 0..MAX_POSITION) { "Position must be between 0 and $MAX_POSITION" }
    }

    fun advance(steps: Int): AdvanceResult {
        val newPosition = (value + steps) % BOARD_SIZE
        val passedGo = value + steps >= BOARD_SIZE
        return AdvanceResult(BoardPosition(newPosition), passedGo)
    }

    companion object {
        private const val MAX_POSITION = 39
        private const val BOARD_SIZE = 40
        val GO = BoardPosition(0)
    }
}

data class AdvanceResult(
    val newPosition: BoardPosition,
    val passedGo: Boolean,
)
