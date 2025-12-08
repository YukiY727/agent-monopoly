package com.monopoly.domain.strategy

import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty

/**
 * ROI（投資効率）重視の戦略
 *
 * すべての意思決定を投資効率（ROI: Return on Investment）に基づいて行います。
 * レント収益とコストの比率を計算し、合理的な判断を下します。
 *
 * - 購入判断: ROI（baseRent / price）が一定以上なら購入
 * - 建設判断: レント増加とコストのROIが一定以上なら建設
 * - 監獄脱出: 所持金が一定額以上あれば支払う
 * - オークション: ROIに基づいて入札額を決定
 */
class ROIStrategy : PlayerStrategy {
    /**
     * プロパティ購入判断
     *
     * ROI（baseRent / price）が閾値以上なら購入します。
     */
    override fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean {
        // 所持金が不足していたら購入しない
        if (property.price > currentMoney) {
            return false
        }

        // StreetPropertyの場合、ROIを計算
        if (property is StreetProperty) {
            val roi: Double = property.rent.base.toDouble() / property.price
            return roi >= MIN_PROPERTY_ROI
        }

        // StreetProperty以外は購入しない
        return false
    }

    /**
     * 家建設判断
     *
     * レント増加とコストのROIが閾値以上なら建設します。
     */
    override fun shouldBuildHouse(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        // 所持金が不足していたら建設しない
        if (property.houseCost > currentMoney) {
            return false
        }

        // 現在のレントと次のレントを計算
        val currentRent: Int = getCurrentRent(property)
        val nextRent: Int = getNextRent(property, property.buildings.houseCount + 1)

        // レント増加を計算
        val rentIncrease: Int = nextRent - currentRent

        // ROIを計算
        val roi: Double = rentIncrease.toDouble() / property.houseCost

        return roi >= MIN_BUILD_ROI
    }

    /**
     * ホテル建設判断
     *
     * レント増加とコストのROIが閾値以上なら建設します。
     */
    override fun shouldBuildHotel(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        // 所持金が不足していたら建設しない
        if (property.hotelCost > currentMoney) {
            return false
        }

        // 現在のレント（4家）とホテルのレントを計算
        val currentRent: Int = getCurrentRent(property)
        val hotelRent: Int = property.rent.withHotel

        // レント増加を計算
        val rentIncrease: Int = hotelRent - currentRent

        // ROIを計算
        val roi: Double = rentIncrease.toDouble() / property.hotelCost

        return roi >= MIN_BUILD_ROI
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
     * ROIに基づいて入札額を決定します：
     * - 高ROIなら積極的に入札（価格の70%）
     * - 低ROIなら消極的に入札（価格の40%）
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
        // ROIを計算して入札比率を決定
        val bidRatio: Double =
            if (property is StreetProperty) {
                val roi: Double = property.rent.base.toDouble() / property.price
                if (roi >= MIN_PROPERTY_ROI) HIGH_ROI_BID_RATIO else LOW_ROI_BID_RATIO
            } else {
                LOW_ROI_BID_RATIO
            }

        // ROIベースの入札額
        val roiBid: Int = (property.price * bidRatio).toInt()

        // 現在の入札額が既に高い場合はパス
        if (currentBid != null && currentBid >= roiBid) {
            return null
        }

        // 入札額が所持金を超える場合はパス
        if (roiBid > currentMoney) {
            return null
        }

        // 現在の入札額より高く入札する必要がある
        val myBid: Int = if (currentBid != null) maxOf(currentBid + 1, roiBid) else roiBid

        return myBid
    }

    /**
     * 現在のレントを取得
     */
    private fun getCurrentRent(property: StreetProperty): Int =
        when (property.buildings.houseCount) {
            0 -> property.rent.base
            1 -> property.rent.withHouse1
            2 -> property.rent.withHouse2
            3 -> property.rent.withHouse3
            4 -> property.rent.withHouse4
            else -> property.rent.base
        }

    /**
     * 次のレントを取得
     */
    private fun getNextRent(
        property: StreetProperty,
        houseCount: Int,
    ): Int =
        when (houseCount) {
            0 -> property.rent.base
            1 -> property.rent.withHouse1
            2 -> property.rent.withHouse2
            3 -> property.rent.withHouse3
            4 -> property.rent.withHouse4
            else -> property.rent.base
        }

    companion object {
        /** プロパティ購入の最低ROI */
        private const val MIN_PROPERTY_ROI = 0.03

        /** 建設の最低ROI */
        private const val MIN_BUILD_ROI = 0.2

        /** 高ROIプロパティの入札比率 */
        private const val HIGH_ROI_BID_RATIO = 0.7

        /** 低ROIプロパティの入札比率 */
        private const val LOW_ROI_BID_RATIO = 0.4

        /** 監獄脱出の最低所持金 */
        private const val MIN_MONEY_FOR_JAIL_PAYMENT = 150
    }
}
