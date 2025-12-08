package com.monopoly.domain.strategy

import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty

/**
 * 積極的な戦略
 *
 * 早期投資を重視し、高リスクを取ります。
 * - 購入判断: 価格が所持金の80%以下なら購入
 * - 建設判断: コストが所持金の70%以下なら建設
 * - 監獄脱出: 所持金が最低額以上あれば積極的に支払う
 * - オークション: プロパティ価格の80%まで積極的に入札
 */
class AggressiveStrategy : PlayerStrategy {
    /**
     * プロパティ購入判断
     *
     * 価格が所持金の80%以下なら購入します。
     */
    override fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean {
        val affordablePrice: Double = currentMoney * PURCHASE_RATIO
        return property.price <= affordablePrice
    }

    /**
     * 家建設判断
     *
     * コストが所持金の70%以下なら建設します。
     */
    override fun shouldBuildHouse(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        val affordableCost: Double = currentMoney * BUILD_RATIO
        return property.houseCost <= affordableCost
    }

    /**
     * ホテル建設判断
     *
     * コストが所持金の70%以下なら建設します。
     */
    override fun shouldBuildHotel(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        val affordableCost: Double = currentMoney * BUILD_RATIO
        return property.hotelCost <= affordableCost
    }

    /**
     * 監獄脱出判断
     *
     * 所持金が最低額以上あれば積極的に支払います。
     */
    override fun shouldPayToEscapeJail(currentMoney: Int): Boolean = currentMoney >= MIN_MONEY_FOR_JAIL_PAYMENT

    /**
     * オークション入札判断
     *
     * 積極的に入札します：
     * - プロパティ価格の80%まで入札
     * - 所持金を超えない範囲で入札
     * - 現在の入札額が既に高い場合はパス
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
        // 積極的な入札額（プロパティ価格の80%）
        val aggressiveBid: Int = (property.price * BID_RATIO).toInt()

        // 現在の入札額が既に高い場合はパス
        if (currentBid != null && currentBid >= aggressiveBid) {
            return null
        }

        // 積極的入札額が所持金を超える場合はパス
        if (aggressiveBid > currentMoney) {
            return null
        }

        // 現在の入札額より高く入札する必要がある
        val myBid: Int = if (currentBid != null) maxOf(currentBid + 1, aggressiveBid) else aggressiveBid

        return myBid
    }

    companion object {
        /** 購入判断の比率（所持金の80%まで） */
        private const val PURCHASE_RATIO = 0.8

        /** 建設判断の比率（所持金の70%まで） */
        private const val BUILD_RATIO = 0.7

        /** 監獄脱出の最低所持金 */
        private const val MIN_MONEY_FOR_JAIL_PAYMENT = 100

        /** オークション入札比率（プロパティ価格の80%） */
        private const val BID_RATIO = 0.8
    }
}
