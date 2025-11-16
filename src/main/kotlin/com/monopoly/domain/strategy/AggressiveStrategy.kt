package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * 積極的な戦略（相手の独占を阻止）
 *
 * 他のプレイヤーのカラーグループ独占を阻止することを優先します。
 *
 * @property minCashReserve 最低現金残高
 */
class AggressiveStrategy(
    private val minCashReserve: Int = 300,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        if (context.moneyAfterPurchase < minCashReserve) {
            return false
        }

        var score = 0

        // 他プレイヤーが同じカラーグループを所有している数
        context.otherPlayers.forEach { otherPlayer ->
            val sameColorCount = otherPlayer.properties.count {
                it.colorGroup == context.property.colorGroup
            }

            when (sameColorCount) {
                1 -> score += 40
                2 -> score += 100  // 独占阻止は最優先
            }
        }

        return score >= 40
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        throw NotImplementedError(
            "AggressiveStrategy はコンテキスト版のshouldBuyを使用してください"
        )
    }
}
