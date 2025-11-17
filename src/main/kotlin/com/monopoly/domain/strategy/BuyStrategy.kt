package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

interface BuyStrategy {
    /**
     * 購入判断（詳細コンテキスト版）
     *
     * デフォルト実装では既存のshouldBuy(property, currentMoney)に委譲します。
     * 高度な戦略を実装する場合は、このメソッドをオーバーライドしてください。
     *
     * @param context 購入判断に必要な全情報
     * @return 購入する場合true
     */
    fun shouldBuy(context: BuyDecisionContext): Boolean {
        // デフォルト実装: 既存メソッドへ委譲
        return shouldBuy(context.property, context.playerMoney)
    }

    /**
     * 購入判断（シンプル版）
     *
     * 既存の戦略との互換性のために残されています。
     * 新しい高度な戦略を実装する場合は、shouldBuy(context)をオーバーライドし、
     * このメソッドは未実装のままで構いません。
     *
     * @param property 購入対象のプロパティ
     * @param currentMoney プレイヤーの現在の所持金
     * @return 購入する場合true
     */
    fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean
}
