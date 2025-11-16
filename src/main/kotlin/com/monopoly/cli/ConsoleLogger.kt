package com.monopoly.cli

import com.monopoly.domain.model.GameEvent

/**
 * ゲームイベントをコンソールに表示するロガー
 */
class ConsoleLogger {
    /**
     * イベントをフォーマットしてコンソールに出力
     */
    fun logEvent(event: GameEvent) {
        val message = formatEvent(event)
        println(message)
    }

    /**
     * イベントリストを順番に出力
     */
    fun logEvents(events: List<GameEvent>) {
        events.forEach { logEvent(it) }
    }

    /**
     * イベントをフォーマットされた文字列に変換
     */
    private fun formatEvent(event: GameEvent): String =
        when (event) {
            is GameEvent.GameStarted ->
                "🎮 Game Started! Players: ${event.playerNames.joinToString(", ")}"

            is GameEvent.DiceRolled ->
                "🎲 ${event.playerName} rolled ${event.die1} + ${event.die2} = ${event.total}"

            is GameEvent.PlayerMoved -> {
                val goBonus = if (event.passedGo) " (Passed GO! +\$200)" else ""
                "🚶 ${event.playerName} moved from position ${event.fromPosition} to ${event.toPosition}$goBonus"
            }

            is GameEvent.PropertyPurchased ->
                "💰 ${event.playerName} purchased ${event.propertyName} for \$${event.price}"

            is GameEvent.RentPaid ->
                "💸 ${event.payerName} paid \$${event.amount} rent to ${event.receiverName} for ${event.propertyName}"

            is GameEvent.PlayerBankrupted ->
                "💀 ${event.playerName} went BANKRUPT! (Final money: \$${event.finalMoney})"

            is GameEvent.TurnStarted ->
                "\n--- Turn ${event.turnNumber + 1}: ${event.playerName}'s turn ---"

            is GameEvent.TurnEnded ->
                ""

            is GameEvent.GameEnded ->
                "🏆 Game Over! Winner: ${event.winner} (Total turns: ${event.totalTurns})"

            is GameEvent.PlayerSentToJail ->
                "🚔 ${event.playerName} was sent to JAIL!"

            is GameEvent.PlayerExitedJail ->
                "🔓 ${event.playerName} exited jail (${event.method})"
        }
}
