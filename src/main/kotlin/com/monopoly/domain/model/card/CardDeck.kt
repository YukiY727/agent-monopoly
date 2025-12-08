package com.monopoly.domain.model.card

import java.util.Collections

class CardDeck(
    initialCards: List<Card>,
) {
    private val cards: ArrayDeque<Card> = ArrayDeque(initialCards)

    init {
        shuffle()
    }

    fun shuffle() {
        val list = cards.toMutableList()
        Collections.shuffle(list)
        cards.clear()
        cards.addAll(list)
    }

    fun draw(): Card {
        if (cards.isEmpty()) {
            throw IllegalStateException("Deck is empty")
        }
        return cards.removeFirst()
    }

    fun returnCard(card: Card) {
        cards.addLast(card)
    }

    val size: Int
        get() = cards.size
}
