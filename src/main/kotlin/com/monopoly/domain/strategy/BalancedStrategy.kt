package com.monopoly.domain.strategy

import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty

/**
 * バランス型の戦略
 *
 * ROI、資金管理、リスクのバランスを取りながら意思決定を行います。
 * 極端な判断を避け、複数の要素を考慮した中庸なアプローチを取ります。
 *
 * - 購入判断: ROI >= 0.02 かつ 価格が所持金の50%以下
 * - 建設判断: ROI >= 0.15 かつ コストが所持金の50%以下
 * - 監獄脱出: 所持金が一定額以上あれば支払う
 * - オークション: プロパティ価格の50%で入札
 */
class BalancedStrategy : PlayerStrategy {
    /**
     * プロパティ購入判断
     *
     * ROIと資金状況の両方を考慮して購入を判断します。
     */
    override fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean {
        // 所持金が不足していたら購入しない
        if (property.price > currentMoney) {
            return false
        }

        // 価格が所持金の一定割合を超えたら購入しない
        val affordablePrice: Double = currentMoney * PURCHASE_RATIO
        if (property.price > affordablePrice) {
            return false
        }

        // StreetPropertyの場合、ROIを確認
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
     * ROIと資金状況の両方を考慮して建設を判断します。
     */
    override fun shouldBuildHouse(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        // 所持金が不足していたら建設しない
        if (property.houseCost > currentMoney) {
            return false
        }

        // コストが所持金の一定割合を超えたら建設しない
        val affordableCost: Double = currentMoney * BUILD_RATIO
        if (property.houseCost > affordableCost) {
            return false
        }

        // レント増加を計算
        val currentRent: Int = getCurrentRent(property)
        val nextRent: Int = getNextRent(property, property.buildings.houseCount + 1)
        val rentIncrease: Int = nextRent - currentRent

        // ROIを計算
        val roi: Double = rentIncrease.toDouble() / property.houseCost

        return roi >= MIN_BUILD_ROI
    }

    /**
     * ホテル建設判断
     *
     * ROIと資金状況の両方を考慮して建設を判断します。
     */
    override fun shouldBuildHotel(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        // 所持金が不足していたら建設しない
        if (property.hotelCost > currentMoney) {
            return false
        }

        // コストが所持金の一定割合を超えたら建設しない
        val affordableCost: Double = currentMoney * BUILD_RATIO
        if (property.hotelCost > affordableCost) {
            return false
        }

        // レント増加を計算
        val currentRent: Int = getCurrentRent(property)
        val hotelRent: Int = property.rent.withHotel
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
     * バランスの取れた入札を行います：
     * - プロパティ価格の50%で入札
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
        // バランスの取れた入札額（プロパティ価格の50%）
        val balancedBid: Int = (property.price * BID_RATIO).toInt()

        // 現在の入札額が既に高い場合はパス
        if (currentBid != null && currentBid >= balancedBid) {
            return null
        }

        // 入札額が所持金を超える場合はパス
        if (balancedBid > currentMoney) {
            return null
        }

        // 現在の入札額より高く入札する必要がある
        val myBid: Int = if (currentBid != null) maxOf(currentBid + 1, balancedBid) else balancedBid

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
        private const val MIN_PROPERTY_ROI = 0.02

        /** 建設の最低ROI */
        private const val MIN_BUILD_ROI = 0.15

        /** 購入判断の比率（所持金の50%まで） */
        private const val PURCHASE_RATIO = 0.5

        /** 建設判断の比率（所持金の50%まで） */
        private const val BUILD_RATIO = 0.5

        /** オークション入札比率（プロパティ価格の50%） */
        private const val BID_RATIO = 0.5

        /** 監獄脱出の最低所持金 */
        private const val MIN_MONEY_FOR_JAIL_PAYMENT = 300
    }
}
