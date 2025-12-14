package com.monopoly.domain.model.player

import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty
import com.monopoly.domain.model.trade.TradeOffer

interface PlayerStrategy {
    fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean

    fun shouldBuildHouse(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean

    fun shouldBuildHotel(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean

    // Phase 3: 刑務所関連
    fun shouldPayToEscapeJail(currentMoney: Int): Boolean

    // Phase 7: オークション関連

    /**
     * オークションでの入札を決定
     *
     * @param property オークション対象のプロパティ
     * @param currentBid 現在の最高入札額（null = まだ誰も入札していない）
     * @param currentMoney プレイヤーの現在の所持金
     * @return 入札額（null = パス）
     */
    fun decideAuctionBid(
        property: Property,
        currentBid: Int?,
        currentMoney: Int,
    ): Int?

    // Phase 9: トレード関連

    /**
     * トレード提案を作成する
     *
     * 自分のターンで他のプレイヤーとトレードを提案するかどうかを決定する。
     * モノポリー完成を目指して、不足している物件を持っているプレイヤーに提案する。
     *
     * @param currentPlayer 現在のプレイヤー（自分）
     * @param otherPlayers 他のプレイヤーリスト
     * @return トレード提案（null = トレードしない）
     */
    fun proposeTradeOffer(
        currentPlayer: Player,
        otherPlayers: List<Player>,
    ): TradeOffer? = null // デフォルト: トレードしない

    /**
     * トレード提案を評価して受け入れるかどうかを決定する
     *
     * @param offer 受け取ったトレード提案
     * @param currentPlayer 現在のプレイヤー（自分）
     * @return 受け入れる場合true
     */
    fun evaluateTradeOffer(
        offer: TradeOffer,
        currentPlayer: Player,
    ): Boolean = false // デフォルト: 受け入れない
}
