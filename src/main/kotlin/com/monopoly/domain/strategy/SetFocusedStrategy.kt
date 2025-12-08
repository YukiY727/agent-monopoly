package com.monopoly.domain.strategy

import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty

/**
 * セット重視の戦略
 *
 * 同色プロパティのセット完成を優先します。
 * 安いプロパティを積極的に購入することで、セット完成を容易にします。
 *
 * - 購入判断: 安いプロパティ（<= 200）を優先、高いプロパティでも所持金の60%以下なら購入
 * - 建設判断: コストが所持金の60%以下なら建設
 * - 監獄脱出: 所持金が一定額以上あれば支払う
 * - オークション: プロパティ価格の60%で入札
 */
class SetFocusedStrategy : PlayerStrategy {
    /**
     * プロパティ購入判断
     *
     * 安いプロパティを優先的に購入します（セット完成を容易にするため）。
     */
    override fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean {
        // 安いプロパティ（セット完成しやすい）は積極的に購入
        if (property.price <= CHEAP_PROPERTY_THRESHOLD) {
            return true
        }

        // 高いプロパティでも所持金の一定割合以下なら購入
        val affordablePrice: Double = currentMoney * PURCHASE_RATIO
        return property.price <= affordablePrice
    }

    /**
     * 家建設判断
     *
     * コストが所持金の60%以下なら建設します。
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
     * コストが所持金の60%以下なら建設します。
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
     * 所持金が一定額以上あれば支払います。
     */
    override fun shouldPayToEscapeJail(currentMoney: Int): Boolean = currentMoney >= MIN_MONEY_FOR_JAIL_PAYMENT

    /**
     * オークション入札判断
     *
     * セット完成を目指して入札します：
     * - プロパティ価格の60%で入札
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
        // セット重視の入札額（プロパティ価格の60%）
        val setFocusedBid: Int = (property.price * BID_RATIO).toInt()

        // 現在の入札額が既に高い場合はパス
        if (currentBid != null && currentBid >= setFocusedBid) {
            return null
        }

        // 入札額が所持金を超える場合はパス
        if (setFocusedBid > currentMoney) {
            return null
        }

        // 現在の入札額より高く入札する必要がある
        val myBid: Int = if (currentBid != null) maxOf(currentBid + 1, setFocusedBid) else setFocusedBid

        return myBid
    }

    companion object {
        /** 安いプロパティの閾値（この価格以下は積極的に購入） */
        private const val CHEAP_PROPERTY_THRESHOLD = 200

        /** 購入判断の比率（所持金の60%まで） */
        private const val PURCHASE_RATIO = 0.6

        /** 建設判断の比率（所持金の60%まで） */
        private const val BUILD_RATIO = 0.6

        /** 監獄脱出の最低所持金 */
        private const val MIN_MONEY_FOR_JAIL_PAYMENT = 200

        /** オークション入札比率（プロパティ価格の60%） */
        private const val BID_RATIO = 0.6
    }
}
