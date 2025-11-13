package com.monopoly.domain.model

class GameState(
    private val players: List<Player>,
    val board: Board,
) {
    private var currentPlayerIndex: Int = 0
    private var gameOver: Boolean = false
    private var turnNumber: Int = 0

    fun getPlayers(): List<Player> = players

    fun getCurrentPlayerIndex(): Int = currentPlayerIndex

    fun getCurrentPlayer(): Player = players[currentPlayerIndex]

    fun isGameOver(): Boolean = gameOver

    fun setGameOver(value: Boolean) {
        gameOver = value
    }

    fun getTurnNumber(): Int = turnNumber

    fun incrementTurnNumber() {
        turnNumber++
    }

    fun nextPlayer() {
        var nextIndex = (currentPlayerIndex + 1) % players.size

        // 破産したプレイヤーをスキップ
        while (players[nextIndex].isBankrupt && getActivePlayerCount() > 1) {
            nextIndex = (nextIndex + 1) % players.size
        }

        currentPlayerIndex = nextIndex
    }

    fun getActivePlayerCount(): Int = players.count { !it.isBankrupt }
}
