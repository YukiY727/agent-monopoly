package com.monopoly.domain.model.trade

import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.PropertyRent
import com.monopoly.domain.model.property.StreetProperty
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class TradeTest : StringSpec({

    "Trade.propose should create Proposed state" {
        val offer = createTestOffer()

        val trade = Trade.propose(offer)

        trade.shouldBeInstanceOf<Trade.Proposed>()
        trade.offer shouldBe offer
    }

    "Trade.Proposed.accept should transition to Accepted state" {
        val offer = createTestOffer()
        val proposed = Trade.propose(offer)

        val accepted = proposed.accept()

        accepted.shouldBeInstanceOf<Trade.Accepted>()
        accepted.offer shouldBe offer
    }

    "Trade.Proposed.reject should transition to Rejected state" {
        val offer = createTestOffer()
        val proposed = Trade.propose(offer)

        val rejected = proposed.reject()

        rejected.shouldBeInstanceOf<Trade.Rejected>()
        rejected.offer shouldBe offer
    }

    "Trade.Accepted.complete should transition to Completed state" {
        val offer = createTestOffer()
        val proposed = Trade.propose(offer)
        val accepted = proposed.accept()

        val completed = accepted.complete()

        completed.shouldBeInstanceOf<Trade.Completed>()
        completed.offer shouldBe offer
    }

    "Full trade lifecycle: Proposed -> Accepted -> Completed" {
        val offer = createTestOffer()

        val proposed: Trade.Proposed = Trade.propose(offer)
        proposed.shouldBeInstanceOf<Trade.Proposed>()

        val accepted: Trade.Accepted = proposed.accept()
        accepted.shouldBeInstanceOf<Trade.Accepted>()

        val completed: Trade.Completed = accepted.complete()
        completed.shouldBeInstanceOf<Trade.Completed>()
        completed.offer shouldBe offer
    }

    "Full trade lifecycle: Proposed -> Rejected" {
        val offer = createTestOffer()

        val proposed: Trade.Proposed = Trade.propose(offer)
        proposed.shouldBeInstanceOf<Trade.Proposed>()

        val rejected: Trade.Rejected = proposed.reject()
        rejected.shouldBeInstanceOf<Trade.Rejected>()
        rejected.offer shouldBe offer
    }
})

private fun createTestOffer(): TradeOffer {
    val player1 = Player("Alice", AlwaysPlayerStrategy())
    val player2 = Player("Bob", AlwaysPlayerStrategy())
    val property = StreetProperty(
        name = "Mediterranean Avenue",
        position = 1,
        price = 60,
        rent = PropertyRent(2, 10, 30, 90, 160, 250),
        houseCost = 50,
        hotelCost = 50,
        colorGroup = ColorGroup.BROWN,
    )

    return TradeOffer(
        proposer = player1,
        target = player2,
        offeredMoney = 100,
        requestedProperties = listOf(property),
    )
}
