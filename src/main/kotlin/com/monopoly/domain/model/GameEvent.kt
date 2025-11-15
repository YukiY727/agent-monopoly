package com.monopoly.domain.model

/**
 * ゲーム内で発生するすべてのイベントを表すsealed class
 * すべてのイベントはturnNumber（ターン番号）とtimestamp（発生時刻）を持つ
 */
sealed class GameEvent {
    abstract val turnNumber: Int
    abstract val timestamp: Long

    /**
     * ゲーム開始イベント
     * @property playerNames 参加プレイヤー名のリスト
     */
    data class GameStarted(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerNames: List<String>
    ) : GameEvent()

    /**
     * ゲーム終了イベント
     * @property winner 勝者の名前
     * @property totalTurns 総ターン数
     */
    data class GameEnded(
        override val turnNumber: Int,
        override val timestamp: Long,
        val winner: String?,
        val totalTurns: Int
    ) : GameEvent()

    /**
     * ターン開始イベント
     * @property playerName ターンを開始するプレイヤー名
     */
    data class TurnStarted(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String
    ) : GameEvent()

    /**
     * ターン終了イベント
     * @property playerName ターンを終了したプレイヤー名
     */
    data class TurnEnded(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String
    ) : GameEvent()

    /**
     * サイコロを振ったイベント
     * @property playerName サイコロを振ったプレイヤー名
     * @property die1 サイコロ1の目
     * @property die2 サイコロ2の目
     * @property total サイコロの合計値
     */
    data class DiceRolled(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val die1: Int,
        val die2: Int,
        val total: Int
    ) : GameEvent()

    /**
     * プレイヤー移動イベント
     * @property playerName 移動したプレイヤー名
     * @property fromPosition 移動前の位置
     * @property toPosition 移動後の位置
     * @property passedGo GO（スタート地点）を通過したかどうか
     */
    data class PlayerMoved(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val fromPosition: Int,
        val toPosition: Int,
        val passedGo: Boolean
    ) : GameEvent()

    /**
     * プロパティ購入イベント
     * @property playerName 購入したプレイヤー名
     * @property propertyName 購入したプロパティ名
     * @property price 購入価格
     */
    data class PropertyPurchased(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val propertyName: String,
        val price: Int
    ) : GameEvent()

    /**
     * レント支払いイベント
     * @property payerName レントを支払ったプレイヤー名
     * @property receiverName レントを受け取ったプレイヤー名
     * @property propertyName レントが発生したプロパティ名
     * @property amount レント金額
     */
    data class RentPaid(
        override val turnNumber: Int,
        override val timestamp: Long,
        val payerName: String,
        val receiverName: String,
        val propertyName: String,
        val amount: Int
    ) : GameEvent()

    /**
     * プレイヤー破産イベント
     * @property playerName 破産したプレイヤー名
     * @property finalMoney 破産時の所持金
     */
    data class PlayerBankrupted(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val finalMoney: Int
    ) : GameEvent()
}
