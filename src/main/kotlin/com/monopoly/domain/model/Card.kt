package com.monopoly.domain.model

enum class CardType {
    CHANCE,
    COMMUNITY_CHEST
}

/**
 * チャンス・共同基金カードを表すsealed class
 */
sealed class Card(
    open val id: String,
    open val text: String,
    open val type: CardType
) {
    /**
     * 特定のマスへ移動するカード
     * targetPosition または targetSpaceType のいずれかを指定
     */
    data class MoveTo(
        override val id: String,
        override val text: String,
        override val type: CardType,
        val targetPosition: Int? = null,
        val targetSpaceType: SpaceType? = null,
        val collectGoMoney: Boolean = true // GOを通ったら$200もらえるか
    ) : Card(id, text, type)

    /**
     * お金を支払うカード
     */
    data class PayMoney(
        override val id: String,
        override val text: String,
        override val type: CardType,
        val amount: Int
    ) : Card(id, text, type)

    /**
     * お金を受け取るカード
     */
    data class ReceiveMoney(
        override val id: String,
        override val text: String,
        override val type: CardType,
        val amount: Int
    ) : Card(id, text, type)

    /**
     * 刑務所へ行くカード
     */
    data class GoToJail(
        override val id: String,
        override val text: String,
        override val type: CardType
    ) : Card(id, text, type)

    /**
     * 刑務所から釈放されるカード
     */
    data class GetOutOfJailFree(
        override val id: String,
        override val text: String,
        override val type: CardType
    ) : Card(id, text, type)
    
    // 修理費やプレイヤー間のお金のやり取りは後回し
}
