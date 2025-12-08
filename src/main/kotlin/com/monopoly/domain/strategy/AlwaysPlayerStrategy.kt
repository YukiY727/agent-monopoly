package com.monopoly.domain.strategy

import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty

/**
 * 常に購入・建設可能なら実行する戦略
 *
 * @property maxBidRatio オークションでの最大入札率（所持金に対する割合）
 * @property initialBidRatio オークションでの初回入札率（プロパティ価格に対する割合）
 * @property minBid オークションでの最低入札額
 * @property bidIncrement オークションでの入札増加額
 */
class AlwaysPlayerStrategy(
    private val maxBidRatio: Double = DEFAULT_MAX_BID_RATIO,
    private val initialBidRatio: Double = DEFAULT_INITIAL_BID_RATIO,
    private val minBid: Int = DEFAULT_MIN_BID,
    private val bidIncrement: Int = DEFAULT_BID_INCREMENT,
) : PlayerStrategy {
    override fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean {
        return currentMoney >= property.price
    }

    override fun shouldBuildHouse(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        return currentMoney >= property.houseCost
    }

    override fun shouldBuildHotel(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        return currentMoney >= property.hotelCost
    }

    override fun shouldPayToEscapeJail(currentMoney: Int): Boolean {
        return currentMoney >= 50
    }

    override fun decideAuctionBid(
        property: Property,
        currentBid: Int?,
        currentMoney: Int,
    ): Int? {
        // 所持金がない場合はパス
        if (currentMoney <= 0) {
            return null
        }

        // 所持金のmaxBidRatio%までは入札可能
        val maxBid: Int = (currentMoney * maxBidRatio).toInt()

        return if (currentBid == null) {
            // 誰も入札していない場合、プロパティ価格のinitialBidRatio%から開始
            val initialBid: Int = (property.price * initialBidRatio).toInt().coerceAtLeast(minBid)
            if (initialBid <= maxBid) initialBid else null
        } else {
            // 既に入札がある場合、現在の入札額+bidIncrementで入札
            val nextBid: Int = currentBid + bidIncrement
            if (nextBid <= maxBid) nextBid else null
        }
    }

    companion object {
        /** デフォルトの最大入札率（所持金の80%） */
        private const val DEFAULT_MAX_BID_RATIO = 0.8

        /** デフォルトの初回入札率（プロパティ価格の30%） */
        private const val DEFAULT_INITIAL_BID_RATIO = 0.3

        /** デフォルトの最低入札額 */
        private const val DEFAULT_MIN_BID = 1

        /** デフォルトの入札増加額 */
        private const val DEFAULT_BID_INCREMENT = 10
    }
}
