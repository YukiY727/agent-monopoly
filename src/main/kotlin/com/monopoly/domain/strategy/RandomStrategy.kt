package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property
import kotlin.random.Random

/**
 * ランダムに購入判断を行う戦略
 *
 * 所持金が足りる場合、50%の確率でプロパティを購入します。
 * リスクの高い戦略で、AlwaysBuyStrategyよりは破産しにくい傾向があります。
 *
 * @property random 乱数生成器（テスト時にシード固定のインスタンスを注入可能）
 */
class RandomStrategy(
    private val random: Random = Random.Default,
) : BuyStrategy {
    override fun shouldBuy(
        property: Property,
        playerMoney: Int,
    ): Boolean {
        // 所持金が足りない場合は購入しない
        if (playerMoney < property.price) {
            return false
        }

        // 50%の確率で購入
        return random.nextBoolean()
    }
}
