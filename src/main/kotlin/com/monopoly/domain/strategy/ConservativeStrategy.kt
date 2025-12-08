package com.monopoly.domain.strategy

import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty

/**
 * 保守的な戦略
 *
 * 一定額の現金を常に保持し、リスクを避けます。
 * - 購入・建設判断: 支払い後も最低現金を確保できる場合のみ実行
 * - 監獄脱出: 資金に余裕がある場合のみ支払い
 * - オークション: 控えめな入札（プロパティ価格の50%程度）
 *
 * @property minCashReserve 最低保持現金額（デフォルト: 400）
 */
class ConservativeStrategy(
    private val minCashReserve: Int = DEFAULT_MIN_CASH_RESERVE,
) : PlayerStrategy {
    /**
     * プロパティ購入判断
     *
     * 購入後も最低現金を確保できる場合のみ購入します。
     */
    override fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean {
        val afterPurchase: Int = currentMoney - property.price
        return afterPurchase >= minCashReserve
    }

    /**
     * 家建設判断
     *
     * 建設後も最低現金を確保できる場合のみ建設します。
     */
    override fun shouldBuildHouse(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        val afterBuild: Int = currentMoney - property.houseCost
        return afterBuild >= minCashReserve
    }

    /**
     * ホテル建設判断
     *
     * 建設後も最低現金を確保できる場合のみ建設します。
     */
    override fun shouldBuildHotel(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        val afterBuild: Int = currentMoney - property.hotelCost
        return afterBuild >= minCashReserve
    }

    /**
     * 監獄脱出判断
     *
     * 罰金支払い後も最低現金を確保できる場合のみ支払います。
     */
    override fun shouldPayToEscapeJail(currentMoney: Int): Boolean {
        val afterPayment: Int = currentMoney - JAIL_FINE
        return afterPayment >= minCashReserve
    }

    /**
     * オークション入札判断
     *
     * 保守的に入札します：
     * - プロパティ価格の50%程度で入札
     * - 入札後も最低現金を確保できる場合のみ
     * - 現在の入札額がすでに高い場合（価格の50%超）はパス
     *
     * @param property オークション対象のプロパティ
     * @param currentBid 現在の最高入札額（null = まだ誰も入札していない）
     * @param currentMoney プレイヤーの現在の所持金
     * @return 入札額（null = パス）
     */
    override fun decideAuctionBid(
        property: Property,
        currentBid: Int?,
        currentMoney: Int,
    ): Int? {
        // 保守的な入札額（プロパティ価格の50%）
        val conservativeBid: Int = (property.price * BID_RATIO).toInt()

        // 現在の入札額が既に高い場合はパス
        if (currentBid != null && currentBid >= conservativeBid) {
            return null
        }

        // 入札額を決定（現在の入札額+1 または 保守的入札額の大きい方）
        val myBid: Int = if (currentBid != null) maxOf(currentBid + 1, conservativeBid) else conservativeBid

        // 入札後も最低現金を確保できるか確認
        val afterBid: Int = currentMoney - myBid
        if (afterBid < minCashReserve) {
            return null
        }

        return myBid
    }

    companion object {
        /** デフォルトの最低保持現金額 */
        private const val DEFAULT_MIN_CASH_RESERVE = 400

        /** 監獄の罰金 */
        private const val JAIL_FINE = 50

        /** オークション入札比率（プロパティ価格の50%） */
        private const val BID_RATIO = 0.5
    }
}
