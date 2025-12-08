package com.monopoly.domain.strategy

import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty
import kotlin.random.Random

/**
 * ランダムに判断する戦略
 *
 * すべての意思決定をランダムに行います。
 * テスト時にはRandomインスタンスを注入することで、動作を再現可能にできます。
 *
 * @property random ランダム生成器（デフォルトはRandom.Default）
 */
class RandomStrategy(
    private val random: Random = Random.Default,
) : PlayerStrategy {
    /**
     * プロパティ購入判断
     *
     * 50%の確率で購入を決定します。
     */
    override fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean = random.nextBoolean()

    /**
     * 家建設判断
     *
     * 50%の確率で建設を決定します。
     */
    override fun shouldBuildHouse(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean = random.nextBoolean()

    /**
     * ホテル建設判断
     *
     * 50%の確率で建設を決定します。
     */
    override fun shouldBuildHotel(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean = random.nextBoolean()

    /**
     * 監獄脱出判断
     *
     * 50%の確率で罰金支払いを決定します。
     */
    override fun shouldPayToEscapeJail(currentMoney: Int): Boolean = random.nextBoolean()

    /**
     * オークション入札判断
     *
     * 50%の確率で入札を決定します。
     * 入札する場合、現在の入札額より高く、所持金以下の範囲でランダムに額を決定します。
     * 資金不足の場合はnullを返します。
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
        // 最低入札額を計算（現在の入札額+1、または1）
        val minBid: Int = (currentBid ?: 0) + 1

        // 資金不足の場合はパス
        if (currentMoney < minBid) {
            return null
        }

        // 50%の確率で入札
        val shouldBid: Boolean = random.nextBoolean()
        if (!shouldBid) {
            return null
        }

        // minBidからcurrentMoneyの範囲でランダムに入札額を決定
        return random.nextInt(minBid, currentMoney + 1)
    }
}
