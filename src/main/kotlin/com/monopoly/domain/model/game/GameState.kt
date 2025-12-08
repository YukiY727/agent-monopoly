package com.monopoly.domain.model.game

import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.model.card.CardDeck
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.property.Property

class GameState(
    val players: List<Player>,
    val board: Board,
    val events: MutableList<GameEvent> = mutableListOf(),
    val chanceDeck: CardDeck = CardDeck(emptyList()),
    val communityChestDeck: CardDeck = CardDeck(emptyList()),
) {
    private var currentPlayerIndex: Int = 0
    private var gameOver: Boolean = false
    var turnNumber: Int = 0
        private set
    var lastDiceRoll: Int = 0 // Store the last dice roll for utility rent calculation

    val currentPlayer: Player
        get() = players[currentPlayerIndex]

    val isGameOver: Boolean
        get() = gameOver

    fun endGame() {
        gameOver = true
    }

    fun incrementTurnNumber() {
        turnNumber++
    }

    fun nextPlayer() {
        val nextIndex: Int = findNextActivePlayerIndex()
        currentPlayerIndex = nextIndex
    }

    private fun findNextActivePlayerIndex(): Int {
        var nextIndex: Int = (currentPlayerIndex + 1) % players.size
        while (shouldSkipPlayer(nextIndex)) {
            nextIndex = (nextIndex + 1) % players.size
        }
        return nextIndex
    }

    private fun shouldSkipPlayer(index: Int): Boolean = players[index].isBankrupt && getActivePlayerCount() > 1

    fun getActivePlayerCount(): Int = players.count { !it.isBankrupt }

    fun releaseProperty(property: Property) {
        board.updateProperty(property)
    }

    fun updateProperty(property: Property) {
        board.updateProperty(property)
    }
}
