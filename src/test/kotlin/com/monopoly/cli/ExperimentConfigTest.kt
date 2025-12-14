package com.monopoly.cli

import com.monopoly.domain.strategy.AggressiveStrategy
import com.monopoly.domain.strategy.ConservativeStrategy
import com.monopoly.domain.strategy.RandomStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ExperimentConfigTest : DescribeSpec({
    describe("ExperimentConfig") {
        describe("validation") {
            it("should require positive game count") {
                shouldThrow<IllegalArgumentException> {
                    ExperimentConfig(
                        gameCount = 0,
                        strategies = listOf(AggressiveStrategy(), ConservativeStrategy()),
                    )
                }.message shouldBe "Game count must be positive"
            }

            it("should require negative game count to fail") {
                shouldThrow<IllegalArgumentException> {
                    ExperimentConfig(
                        gameCount = -1,
                        strategies = listOf(AggressiveStrategy(), ConservativeStrategy()),
                    )
                }.message shouldBe "Game count must be positive"
            }

            it("should require at least 2 players") {
                shouldThrow<IllegalArgumentException> {
                    ExperimentConfig(
                        gameCount = 10,
                        strategies = listOf(AggressiveStrategy()),
                    )
                }.message shouldBe "At least 2 players are required"
            }

            it("should allow maximum 8 players") {
                val strategies = (1..9).map { RandomStrategy() }

                shouldThrow<IllegalArgumentException> {
                    ExperimentConfig(
                        gameCount = 10,
                        strategies = strategies,
                    )
                }.message shouldBe "Maximum 8 players are supported"
            }
        }

        describe("valid configuration") {
            it("should accept 2 players") {
                val config = ExperimentConfig(
                    gameCount = 10,
                    strategies = listOf(AggressiveStrategy(), ConservativeStrategy()),
                )

                config.playerCount shouldBe 2
                config.gameCount shouldBe 10
            }

            it("should accept 8 players") {
                val strategies = (1..8).map { RandomStrategy() }
                val config = ExperimentConfig(
                    gameCount = 5,
                    strategies = strategies,
                )

                config.playerCount shouldBe 8
            }
        }
    }
})
