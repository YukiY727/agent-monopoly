package com.monopoly.cli

import com.monopoly.domain.strategy.AggressiveStrategy
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import com.monopoly.domain.strategy.BalancedStrategy
import com.monopoly.domain.strategy.ConservativeStrategy
import com.monopoly.domain.strategy.ROIStrategy
import com.monopoly.domain.strategy.RandomStrategy
import com.monopoly.domain.strategy.SetFocusedStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class StrategyFactoryTest : DescribeSpec({
    describe("StrategyFactory") {
        describe("create") {
            it("should create AlwaysPlayerStrategy for 'always'") {
                val strategy = StrategyFactory.create("always")
                strategy.shouldBeInstanceOf<AlwaysPlayerStrategy>()
            }

            it("should create RandomStrategy for 'random'") {
                val strategy = StrategyFactory.create("random")
                strategy.shouldBeInstanceOf<RandomStrategy>()
            }

            it("should create ConservativeStrategy for 'conservative'") {
                val strategy = StrategyFactory.create("conservative")
                strategy.shouldBeInstanceOf<ConservativeStrategy>()
            }

            it("should create AggressiveStrategy for 'aggressive'") {
                val strategy = StrategyFactory.create("aggressive")
                strategy.shouldBeInstanceOf<AggressiveStrategy>()
            }

            it("should create SetFocusedStrategy for 'setfocused'") {
                val strategy = StrategyFactory.create("setfocused")
                strategy.shouldBeInstanceOf<SetFocusedStrategy>()
            }

            it("should create ROIStrategy for 'roi'") {
                val strategy = StrategyFactory.create("roi")
                strategy.shouldBeInstanceOf<ROIStrategy>()
            }

            it("should create BalancedStrategy for 'balanced'") {
                val strategy = StrategyFactory.create("balanced")
                strategy.shouldBeInstanceOf<BalancedStrategy>()
            }

            it("should be case-insensitive") {
                StrategyFactory.create("AGGRESSIVE").shouldBeInstanceOf<AggressiveStrategy>()
                StrategyFactory.create("Aggressive").shouldBeInstanceOf<AggressiveStrategy>()
                StrategyFactory.create("ROI").shouldBeInstanceOf<ROIStrategy>()
            }

            it("should throw for unknown strategy") {
                val exception = shouldThrow<IllegalArgumentException> {
                    StrategyFactory.create("unknown")
                }
                exception.message shouldContain "Unknown strategy: 'unknown'"
                exception.message shouldContain "Available strategies:"
            }
        }

        describe("createMultiple") {
            it("should create multiple strategies from comma-separated string") {
                val strategies = StrategyFactory.createMultiple("aggressive,conservative,roi")

                strategies shouldHaveSize 3
                strategies[0].shouldBeInstanceOf<AggressiveStrategy>()
                strategies[1].shouldBeInstanceOf<ConservativeStrategy>()
                strategies[2].shouldBeInstanceOf<ROIStrategy>()
            }

            it("should handle whitespace around names") {
                val strategies = StrategyFactory.createMultiple("aggressive , conservative , roi")

                strategies shouldHaveSize 3
                strategies[0].shouldBeInstanceOf<AggressiveStrategy>()
                strategies[1].shouldBeInstanceOf<ConservativeStrategy>()
                strategies[2].shouldBeInstanceOf<ROIStrategy>()
            }

            it("should ignore empty entries") {
                val strategies = StrategyFactory.createMultiple("aggressive,,conservative")

                strategies shouldHaveSize 2
            }

            it("should create all 7 strategies") {
                val strategies = StrategyFactory.createMultiple(
                    "always,random,conservative,aggressive,setfocused,roi,balanced",
                )

                strategies shouldHaveSize 7
            }
        }

        describe("availableStrategies") {
            it("should list all 7 strategies") {
                StrategyFactory.availableStrategies shouldHaveSize 7
            }

            it("should include all strategy names") {
                val names = StrategyFactory.availableStrategies

                names shouldBe listOf(
                    "always",
                    "random",
                    "conservative",
                    "aggressive",
                    "setfocused",
                    "roi",
                    "balanced",
                )
            }
        }

        describe("getDescription") {
            it("should return description for each strategy") {
                StrategyFactory.getDescription("always") shouldContain "Always"
                StrategyFactory.getDescription("random") shouldContain "random"
                StrategyFactory.getDescription("conservative") shouldContain "cash"
                StrategyFactory.getDescription("aggressive") shouldContain "aggressively"
                StrategyFactory.getDescription("setfocused") shouldContain "color sets"
                StrategyFactory.getDescription("roi") shouldContain "ROI"
                StrategyFactory.getDescription("balanced") shouldContain "Balances"
            }

            it("should return 'Unknown strategy' for unknown name") {
                StrategyFactory.getDescription("unknown") shouldBe "Unknown strategy"
            }
        }
    }
})
