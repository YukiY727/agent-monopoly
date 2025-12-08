package com.monopoly.domain.event

import com.monopoly.domain.model.card.CardType
import com.monopoly.domain.model.jail.JailEscapeMethod
import com.monopoly.domain.model.jail.JailReason

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
        val playerNames: List<String>,
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
        val totalTurns: Int,
    ) : GameEvent()

    /**
     * ターン開始イベント
     * @property playerName ターンを開始するプレイヤー名
     */
    data class TurnStarted(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
    ) : GameEvent()

    /**
     * ターン終了イベント
     * @property playerName ターンを終了したプレイヤー名
     */
    data class TurnEnded(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
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
        val total: Int,
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
        val passedGo: Boolean,
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
        val price: Int,
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
        val amount: Int,
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
        val finalMoney: Int,
    ) : GameEvent()

    /**
     * ゾロ目を出したイベント（Phase 2）
     * @property playerName ゾロ目を出したプレイヤー名
     * @property doublesCount 連続ゾロ目の回数（1, 2, or 3）
     */
    data class DoublesRolled(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val doublesCount: Int,
    ) : GameEvent()

    /**
     * 3回連続ゾロ目イベント（Phase 2）
     * プレイヤーは刑務所に送られる
     * @property playerName 3回連続ゾロ目を出したプレイヤー名
     */
    data class ThreeConsecutiveDoubles(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
    ) : GameEvent()

    /**
     * 家建設イベント（Phase 2）
     * @property playerName 建設したプレイヤー名
     * @property propertyName 建設したプロパティ名
     * @property houseCount 建設後の家の総数
     * @property cost 建設コスト
     */
    data class HouseBuilt(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val propertyName: String,
        val houseCount: Int,
        val cost: Int,
    ) : GameEvent()

    /**
     * ホテル建設イベント（Phase 2）
     * @property playerName 建設したプレイヤー名
     * @property propertyName 建設したプロパティ名
     * @property cost 建設コスト
     */
    data class HotelBuilt(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val propertyName: String,
        val cost: Int,
    ) : GameEvent()

    // Phase 3: 刑務所関連イベント

    /**
     * プレイヤーが刑務所に送られたイベント
     * @property playerName プレイヤー名
     * @property reason 理由
     */
    data class PlayerSentToJail(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val reason: JailReason,
    ) : GameEvent()

    /**
     * 刑務所から脱出したイベント
     * @property playerName プレイヤー名
     * @property method 脱出方法
     */
    data class JailEscaped(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val method: JailEscapeMethod,
    ) : GameEvent()

    /**
     * 刑務所脱出に失敗したイベント
     * @property playerName プレイヤー名
     */
    data class JailTurnFailed(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
    ) : GameEvent()

    // Phase 3: カード関連イベント

    /**
     * カードを引いたイベント
     * @property playerName プレイヤー名
     * @property cardText カードのテキスト
     * @property cardType カードの種類（CHANCE or COMMUNITY_CHEST）
     */
    data class CardDrawn(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val cardText: String,
        val cardType: CardType,
    ) : GameEvent()

    /**
     * カードを保持したイベント（Get Out of Jail Free）
     * @property playerName プレイヤー名
     * @property cardText カードのテキスト
     */
    data class CardHeld(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val cardText: String,
    ) : GameEvent()

    /**
     * お金を支払ったイベント（カードや税金など）
     * @property playerName プレイヤー名
     * @property amount 金額
     * @property reason 理由
     */
    data class MoneyPaid(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val amount: Int,
        val reason: String,
    ) : GameEvent()

    /**
     * お金を受け取ったイベント（カードなど）
     * @property playerName プレイヤー名
     * @property amount 金額
     * @property reason 理由
     */
    data class MoneyReceived(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val amount: Int,
        val reason: String,
    ) : GameEvent()

    // Phase 6: 抵当関連イベント

    /**
     * プロパティを抵当に入れたイベント
     * @property playerName プレイヤー名
     * @property propertyName プロパティ名
     * @property mortgageValue 抵当額（受け取った金額）
     */
    data class PropertyMortgaged(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val propertyName: String,
        val mortgageValue: Int,
    ) : GameEvent()

    /**
     * プロパティの抵当を解除したイベント
     * @property playerName プレイヤー名
     * @property propertyName プロパティ名
     * @property unmortgageValue 抵当解除額（支払った金額）
     */
    data class PropertyUnmortgaged(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val propertyName: String,
        val unmortgageValue: Int,
    ) : GameEvent()

    // Phase 7: オークション関連イベント

    /**
     * オークション開始イベント
     * @property propertyName オークション対象のプロパティ名
     * @property eligiblePlayers オークションに参加できるプレイヤー名のリスト
     */
    data class AuctionStarted(
        override val turnNumber: Int,
        override val timestamp: Long,
        val propertyName: String,
        val eligiblePlayers: List<String>,
    ) : GameEvent()

    /**
     * オークションで入札したイベント
     * @property playerName 入札したプレイヤー名
     * @property propertyName オークション対象のプロパティ名
     * @property bidAmount 入札額
     */
    data class PlayerBidInAuction(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val propertyName: String,
        val bidAmount: Int,
    ) : GameEvent()

    /**
     * オークションでパスしたイベント
     * @property playerName パスしたプレイヤー名
     * @property propertyName オークション対象のプロパティ名
     */
    data class PlayerPassedInAuction(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val propertyName: String,
    ) : GameEvent()

    /**
     * オークション完了イベント
     * @property propertyName オークション対象のプロパティ名
     * @property winnerName 落札者名（null = オークション不成立）
     * @property winningBid 落札額
     */
    data class AuctionCompleted(
        override val turnNumber: Int,
        override val timestamp: Long,
        val propertyName: String,
        val winnerName: String?,
        val winningBid: Int,
    ) : GameEvent()
}
