package com.monopoly.domain.model

/**
 * ゲーム内のイベントを表すsealed class
 * すべてのイベントは発生したターン番号とタイムスタンプを持つ
 */
sealed class GameEvent(
    open val turnNumber: Int,
    open val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * ゲーム開始イベント
     */
    data class GameStarted(
        override val turnNumber: Int = 0,
        override val timestamp: Long = System.currentTimeMillis(),
        val playerNames: List<String>
    ) : GameEvent(turnNumber, timestamp)

    /**
     * サイコロが振られたイベント
     */
    data class DiceRolled(
        override val turnNumber: Int,
        override val timestamp: Long = System.currentTimeMillis(),
        val playerName: String,
        val die1: Int,
        val die2: Int
    ) : GameEvent(turnNumber, timestamp) {
        val total: Int = die1 + die2
    }

    /**
     * プレイヤーが移動したイベント
     */
    data class PlayerMoved(
        override val turnNumber: Int,
        override val timestamp: Long = System.currentTimeMillis(),
        val playerName: String,
        val fromPosition: Int,
        val toPosition: Int,
        val passedGo: Boolean
    ) : GameEvent(turnNumber, timestamp)

    /**
     * プロパティが購入されたイベント
     */
    data class PropertyPurchased(
        override val turnNumber: Int,
        override val timestamp: Long = System.currentTimeMillis(),
        val playerName: String,
        val propertyName: String,
        val price: Int
    ) : GameEvent(turnNumber, timestamp)

    /**
     * レントが支払われたイベント
     */
    data class RentPaid(
        override val turnNumber: Int,
        override val timestamp: Long = System.currentTimeMillis(),
        val payerName: String,
        val receiverName: String,
        val propertyName: String,
        val amount: Int
    ) : GameEvent(turnNumber, timestamp)

    /**
     * プレイヤーが破産したイベント
     */
    data class PlayerBankrupted(
        override val turnNumber: Int,
        override val timestamp: Long = System.currentTimeMillis(),
        val playerName: String,
        val finalMoney: Int
    ) : GameEvent(turnNumber, timestamp)

    /**
     * ターン開始イベント
     */
    data class TurnStarted(
        override val turnNumber: Int,
        override val timestamp: Long = System.currentTimeMillis(),
        val playerName: String
    ) : GameEvent(turnNumber, timestamp)

    /**
     * ターン終了イベント
     */
    data class TurnEnded(
        override val turnNumber: Int,
        override val timestamp: Long = System.currentTimeMillis(),
        val playerName: String
    ) : GameEvent(turnNumber, timestamp)

    /**
     * ゲーム終了イベント
     */
    data class GameEnded(
        override val turnNumber: Int,
        override val timestamp: Long = System.currentTimeMillis(),
        val winner: String,
        val totalTurns: Int
    ) : GameEvent(turnNumber, timestamp)

    /**
     * プレイヤーがJailに送られたイベント
     *
     * Phase 16: Jailシステム
     */
    data class PlayerSentToJail(
        override val turnNumber: Int,
        override val timestamp: Long = System.currentTimeMillis(),
        val playerName: String
    ) : GameEvent(turnNumber, timestamp)

    /**
     * プレイヤーがJailから脱出したイベント
     *
     * Phase 16: Jailシステム
     */
    data class PlayerExitedJail(
        override val turnNumber: Int,
        override val timestamp: Long = System.currentTimeMillis(),
        val playerName: String,
        val method: String // "paid_fine", "rolled_doubles", "used_card", "forced_after_3_turns"
    ) : GameEvent(turnNumber, timestamp)
}
