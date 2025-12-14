package com.monopoly.cli

import com.monopoly.domain.strategy.AggressiveStrategy
import com.monopoly.domain.strategy.BalancedStrategy
import com.monopoly.domain.strategy.ConservativeStrategy
import com.monopoly.domain.strategy.ROIStrategy
import com.monopoly.domain.strategy.RandomStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class ArgumentParserTest : DescribeSpec({
    describe("ArgumentParser") {
        val parser = ArgumentParser()

        describe("parse with no arguments") {
            it("should return default config") {
                val config = parser.parse(emptyArray())

                config?.gameCount shouldBe 10
                config?.playerCount shouldBe 4
            }

            it("should use default strategies") {
                val config = parser.parse(emptyArray())

                config?.strategies?.get(0).shouldBeInstanceOf<AggressiveStrategy>()
                config?.strategies?.get(1).shouldBeInstanceOf<ConservativeStrategy>()
                config?.strategies?.get(2).shouldBeInstanceOf<ROIStrategy>()
                config?.strategies?.get(3).shouldBeInstanceOf<BalancedStrategy>()
            }
        }

        describe("parse --help") {
            it("should return null for --help") {
                val config = parser.parse(arrayOf("--help"))
                config.shouldBeNull()
            }

            it("should return null for -h") {
                val config = parser.parse(arrayOf("-h"))
                config.shouldBeNull()
            }
        }

        describe("parse --games") {
            it("should parse --games option") {
                val config = parser.parse(arrayOf("--games", "100"))

                config?.gameCount shouldBe 100
            }

            it("should parse -g option") {
                val config = parser.parse(arrayOf("-g", "50"))

                config?.gameCount shouldBe 50
            }

            it("should throw for missing game count value") {
                shouldThrow<IllegalArgumentException> {
                    parser.parse(arrayOf("--games"))
                }.message shouldContain "Missing value for --games"
            }

            it("should throw for invalid game count") {
                shouldThrow<IllegalArgumentException> {
                    parser.parse(arrayOf("--games", "abc"))
                }.message shouldContain "Invalid game count"
            }
        }

        describe("parse --strategies") {
            it("should parse --strategies option") {
                val config = parser.parse(arrayOf("--strategies", "aggressive,conservative"))

                config?.playerCount shouldBe 2
                config?.strategies?.get(0).shouldBeInstanceOf<AggressiveStrategy>()
                config?.strategies?.get(1).shouldBeInstanceOf<ConservativeStrategy>()
            }

            it("should parse -s option") {
                val config = parser.parse(arrayOf("-s", "random,roi,balanced"))

                config?.playerCount shouldBe 3
                config?.strategies?.get(0).shouldBeInstanceOf<RandomStrategy>()
                config?.strategies?.get(1).shouldBeInstanceOf<ROIStrategy>()
                config?.strategies?.get(2).shouldBeInstanceOf<BalancedStrategy>()
            }

            it("should throw for missing strategies value") {
                shouldThrow<IllegalArgumentException> {
                    parser.parse(arrayOf("--strategies"))
                }.message shouldContain "Missing value for --strategies"
            }

            it("should throw for invalid strategy name") {
                shouldThrow<IllegalArgumentException> {
                    parser.parse(arrayOf("-s", "invalid"))
                }.message shouldContain "Unknown strategy"
            }
        }

        describe("parse combined options") {
            it("should parse both --games and --strategies") {
                val config = parser.parse(arrayOf("-g", "200", "-s", "aggressive,roi"))

                config?.gameCount shouldBe 200
                config?.playerCount shouldBe 2
            }

            it("should parse options in any order") {
                val config = parser.parse(arrayOf("-s", "conservative,balanced", "-g", "75"))

                config?.gameCount shouldBe 75
                config?.playerCount shouldBe 2
            }
        }

        describe("error handling") {
            it("should throw for unknown argument") {
                shouldThrow<IllegalArgumentException> {
                    parser.parse(arrayOf("--unknown"))
                }.message shouldContain "Unknown argument"
            }
        }
    }
})
