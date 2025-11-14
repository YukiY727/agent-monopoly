package com.monopoly.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BoardTest : StringSpec({
    // TC-020: Board初期化
    // Given: なし
    // When: 新しいBoardを作成
    // Then: getSpaceCount()が40
    "board should have 40 spaces" {
        val board = BoardFixtures.createStandardBoard()

        board.getSpaceCount() shouldBe 40
    }

    // TC-021: マス取得（有効な位置）
    // Given: Board
    // When: getSpace(5)
    // Then: 位置5のSpaceを返す
    "should return space at valid position" {
        val board = BoardFixtures.createStandardBoard()

        val space = board.getSpace(5)

        space.position shouldBe 5
    }

    // TC-022: 位置0がGOマス
    // Given: Board
    // When: getSpace(0)
    // Then: SpaceTypeがGO
    "position 0 should be GO space" {
        val board = BoardFixtures.createStandardBoard()

        val space = board.getSpace(0)

        space.spaceType shouldBe SpaceType.GO
    }

    // TC-023: 無効な位置でエラー
    // Given: Board
    // When: getSpace(-1) または getSpace(40)
    // Then: IllegalArgumentException
    "should throw exception for invalid position" {
        val board = BoardFixtures.createStandardBoard()

        shouldThrow<IllegalArgumentException> {
            board.getSpace(-1)
        }

        shouldThrow<IllegalArgumentException> {
            board.getSpace(40)
        }
    }
})
