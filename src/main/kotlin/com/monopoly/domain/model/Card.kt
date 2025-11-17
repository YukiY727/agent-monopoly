package com.monopoly.domain.model

/**
 * Monopolyのカード（ChanceとCommunity Chest）
 *
 * Phase 17: カードシステムの実装
 */
sealed class Card(val description: String) {
    /**
     * Jail から出るカード
     */
    data object GetOutOfJailFreeCard : Card("Get Out of Jail Free")

    /**
     * GO に進む
     */
    data object AdvanceToGo : Card("Advance to GO"), MoveTo {
        override val position: Int = 0
    }

    /**
     * 特定の位置に移動するカード
     */
    interface MoveTo {
        val position: Int
    }

    /**
     * Jail に行く
     */
    data object GoToJail : Card("Go to Jail"), MoveTo {
        override val position: Int = 10
    }

    /**
     * お金を受け取る
     */
    data class CollectMoney(val amount: Int) : Card("Collect $$amount")

    /**
     * お金を支払う
     */
    data class PayMoney(val amount: Int) : Card("Pay $$amount")

    /**
     * 指定された位置に移動
     */
    data class MoveToPosition(override val position: Int, val desc: String) : Card(desc), MoveTo

    /**
     * 各プレイヤーからお金を受け取る
     */
    data class CollectFromEachPlayer(val amount: Int) : Card("Collect $$amount from each player")

    /**
     * 各プレイヤーにお金を支払う
     */
    data class PayEachPlayer(val amount: Int) : Card("Pay $$amount to each player")

    /**
     * 家とホテルの修理費用を支払う
     */
    data class PayRepairs(val perHouse: Int, val perHotel: Int) : Card("Pay repairs: $$perHouse per house, $$perHotel per hotel")
}

/**
 * カードデッキ（immutable）
 */
class CardDeck(private val cards: List<Card>) {
    val size: Int
        get() = cards.size

    /**
     * カードを1枚引く
     * @return 引いたカードと新しいデッキ
     */
    fun draw(): Pair<Card, CardDeck> {
        require(cards.isNotEmpty()) { "Deck is empty" }

        val card = cards.first()
        val remainingCards = cards.drop(1)

        // GetOutOfJailFreeCard は引いた後デッキに戻さない
        val newCards = if (card is Card.GetOutOfJailFreeCard) {
            remainingCards
        } else {
            remainingCards + card // 引いたカードを最後に追加
        }

        return card to CardDeck(newCards)
    }

    companion object {
        /**
         * Chanceカードデッキを作成
         */
        fun createChanceDeck(): CardDeck {
            val cards = listOf(
                Card.AdvanceToGo,
                Card.GoToJail,
                Card.GetOutOfJailFreeCard,
                Card.MoveToPosition(5, "Advance to Reading Railroad"),
                Card.MoveToPosition(39, "Advance to Boardwalk"),
                Card.CollectMoney(150),
                Card.CollectMoney(50),
                Card.PayMoney(15),
                Card.PayRepairs(25, 100),
                Card.CollectFromEachPlayer(50)
            )
            return CardDeck(cards.shuffled())
        }

        /**
         * Community Chestカードデッキを作成
         */
        fun createCommunityChestDeck(): CardDeck {
            val cards = listOf(
                Card.AdvanceToGo,
                Card.GoToJail,
                Card.GetOutOfJailFreeCard,
                Card.CollectMoney(200),
                Card.CollectMoney(100),
                Card.CollectMoney(50),
                Card.CollectMoney(25),
                Card.PayMoney(50),
                Card.PayMoney(100),
                Card.PayRepairs(40, 115),
                Card.PayEachPlayer(50),
                Card.CollectFromEachPlayer(10)
            )
            return CardDeck(cards.shuffled())
        }
    }
}
