package com.monopoly.domain.service
import com.monopoly.domain.strategy.AlwaysBuyStrategy

import com.monopoly.domain.model.AlwaysBuyStrategy
import com.monopoly.domain.model.Board
import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * PropertyDevelopmentService（家・ホテル建設サービス）のテスト
 *
 * Phase 14: 家・ホテルシステムの実装
 */
class PropertyDevelopmentServiceTest : DescribeSpec({
    val service = PropertyDevelopmentService()

    describe("カラーグループ独占の判定") {
        it("should detect color group monopoly when player owns all properties") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()

            // BROWNグループのプロパティを取得（位置1と3）
            val prop1 = board.getPropertyAt(1)!!
            val prop2 = board.getPropertyAt(3)!!

            player.addProperty(prop1.withOwner(player))
            player.addProperty(prop2.withOwner(player))

            service.hasColorGroupMonopoly(player, ColorGroup.BROWN, board) shouldBe true
        }

        it("should not detect monopoly when player owns only some properties") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()

            val prop1 = board.getPropertyAt(1)!!
            player.addProperty(prop1.withOwner(player))

            service.hasColorGroupMonopoly(player, ColorGroup.BROWN, board) shouldBe false
        }
    }

    describe("家賃計算") {
        val player = Player("Alice", AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()

        it("should double base rent when color group monopoly exists (no houses)") {
            val prop = board.getPropertyAt(1)!! // Mediterranean Avenue, baseRent=2
            val ownedProp = prop.withOwner(player)

            // BROWNグループを独占
            player.addProperty(ownedProp)
            player.addProperty(board.getPropertyAt(3)!!.withOwner(player))

            val baseRent = service.calculateRent(ownedProp, board, player, diceRoll = 0)
            rent shouldBe 4 // 2 * 2 (monopoly doubles base rent)
        }

        it("should calculate rent with 1 house") {
            val prop = Property(
                name = "Test Property",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
                houses = 1,
            ).withOwner(player)

            val baseRent = service.calculateRent(prop, board, player, diceRoll = 0)
            rent shouldBe 50 // 1 house rent (typically 5x base rent)
        }

        it("should calculate rent with 2 houses") {
            val prop = Property(
                name = "Test Property",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
                houses = 2,
            ).withOwner(player)

            val baseRent = service.calculateRent(prop, board, player, diceRoll = 0)
            rent shouldBe 150 // 2 houses rent
        }

        it("should calculate rent with hotel") {
            val prop = Property(
                name = "Test Property",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
                hasHotel = true,
                houses = 0, // Hotel replaces houses
            ).withOwner(player)

            val baseRent = service.calculateRent(prop, board, player, diceRoll = 0)
            rent shouldBe 250 // Hotel rent
        }
    }

    describe("家の建設") {
        it("should allow building house on monopoly property") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()

            // BROWNグループを独占
            val prop1 = board.getPropertyAt(1)!!.withOwner(player)
            val prop2 = board.getPropertyAt(3)!!.withOwner(player)
            player.addProperty(prop1)
            player.addProperty(prop2)

            player.addMoney(1000) // 建設費用を追加

            val result = service.buildHouse(player, prop1, board)
            result.houses shouldBe 1
        }

        it("should enforce even building rule") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()

            // BROWNグループを独占
            val prop1 = Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
                houses = 2,
            ).withOwner(player)
            val prop2 = Property(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                baseRent = 4,
                colorGroup = ColorGroup.BROWN,
                houses = 0, // No houses
            ).withOwner(player)

            player.addProperty(prop1)
            player.addProperty(prop2)
            player.addMoney(1000)

            // prop1に3軒目を建てようとすると失敗（均等建設ルール違反）
            shouldThrow<IllegalArgumentException> {
                service.buildHouse(player, prop1, board)
            }
        }

        it("should not allow building more than 4 houses") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()

            val prop = Property(
                name = "Test Property",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
                houses = 4,
            ).withOwner(player)

            player.addProperty(prop)
            player.addMoney(1000)

            shouldThrow<IllegalArgumentException> {
                service.buildHouse(player, prop, board)
            }
        }
    }

    describe("ホテルの建設") {
        it("should allow building hotel after 4 houses") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()

            // BROWNグループを独占して4軒ずつ家を建てた状態
            val prop1 = Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
                houses = 4,
            ).withOwner(player)
            val prop2 = Property(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                baseRent = 4,
                colorGroup = ColorGroup.BROWN,
                houses = 4,
            ).withOwner(player)

            player.addProperty(prop1)
            player.addProperty(prop2)
            player.addMoney(1000)

            val result = service.buildHotel(player, prop1, board)
            result.hasHotel shouldBe true
            result.houses shouldBe 0 // 4軒の家がホテルに置き換わる
        }

        it("should not allow building hotel without 4 houses") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val board = BoardFixtures.createStandardBoard()

            val prop = Property(
                name = "Test Property",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
                houses = 3, // Only 3 houses
            ).withOwner(player)

            player.addProperty(prop)
            player.addMoney(1000)

            shouldThrow<IllegalArgumentException> {
                service.buildHotel(player, prop, board)
            }
        }
    }

    describe("家・ホテルの売却") {
        it("should allow selling house for half price") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val initialMoney = player.money

            val prop = Property(
                name = "Test Property",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
                houses = 2,
            ).withOwner(player)

            player.addProperty(prop)

            val result = service.sellHouse(player, prop)
            result.houses shouldBe 1
            player.money shouldBe initialMoney + 50 // 建設費$100の50%
        }

        it("should allow selling hotel for half price") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val initialMoney = player.money

            val prop = Property(
                name = "Test Property",
                position = 1,
                price = 200,
                baseRent = 10,
                colorGroup = ColorGroup.BROWN,
                hasHotel = true,
            ).withOwner(player)

            player.addProperty(prop)

            val result = service.sellHotel(player, prop)
            result.hasHotel shouldBe false
            result.houses shouldBe 4 // ホテルが4軒の家に戻る
            player.money shouldBe initialMoney + 50 // ホテル建設費$100の50%
        }
    }
})
