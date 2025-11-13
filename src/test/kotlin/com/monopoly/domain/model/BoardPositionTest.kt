package com.monopoly.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BoardPositionTest : StringSpec({
    "should create position with valid value" {
        val position = BoardPosition(5)

        position.value shouldBe 5
    }

    "should throw exception for negative position" {
        shouldThrow<IllegalArgumentException> {
            BoardPosition(-1)
        }
    }

    "should throw exception for position greater than 39" {
        shouldThrow<IllegalArgumentException> {
            BoardPosition(40)
        }
    }

    "should have GO constant at position 0" {
        BoardPosition.GO.value shouldBe 0
    }

    "should advance without passing GO" {
        val position = BoardPosition(5)

        val result = position.advance(10)

        result.newPosition.value shouldBe 15
        result.passedGo shouldBe false
    }

    "should advance and pass GO" {
        val position = BoardPosition(35)

        val result = position.advance(10)

        result.newPosition.value shouldBe 5
        result.passedGo shouldBe true
    }

    "should advance exactly to GO" {
        val position = BoardPosition(30)

        val result = position.advance(10)

        result.newPosition.value shouldBe 0
        result.passedGo shouldBe true
    }

    "should advance from GO without passing GO" {
        val position = BoardPosition(0)

        val result = position.advance(5)

        result.newPosition.value shouldBe 5
        result.passedGo shouldBe false
    }

    "should handle large advancement" {
        val position = BoardPosition(5)

        val result = position.advance(50)

        result.newPosition.value shouldBe 15
        result.passedGo shouldBe true
    }

    "should advance zero steps" {
        val position = BoardPosition(10)

        val result = position.advance(0)

        result.newPosition.value shouldBe 10
        result.passedGo shouldBe false
    }

    "should create position at boundary 0" {
        val position = BoardPosition(0)

        position.value shouldBe 0
    }

    "should create position at boundary 39" {
        val position = BoardPosition(39)

        position.value shouldBe 39
    }
})
