package com.monopoly.domain.model

class GameState(
    val players: List<Player>,
    val board: Board,
    val events: MutableList<GameEvent> = mutableListOf(),
    initialChanceDeck: CardDeck = CardDeck.createChanceDeck(),
    initialCommunityChestDeck: CardDeck = CardDeck.createCommunityChestDeck()
) {
    private var currentPlayerIndex: Int = 0
    private var gameOver: Boolean = false
    var turnNumber: Int = 0
        private set

    // Phase 17: カードデッキ
    var chanceDeck: CardDeck = initialChanceDeck
        private set

    var communityChestDeck: CardDeck = initialCommunityChestDeck
        private set

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

    // Phase 17: カードデッキの更新
    fun updateChanceDeck(newDeck: CardDeck) {
        chanceDeck = newDeck
    }

    fun updateCommunityChestDeck(newDeck: CardDeck) {
        communityChestDeck = newDeck
    }
}
