package com.monopoly.domain.model

import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class PropertyOwnershipTest : StringSpec({
    // TC-Ownership-001: Unowned initialization
    // Given: なし
    // When: PropertyOwnership.Unowned を取得
    // Then: Unowned インスタンスが取得できる
    "Unowned should be a singleton object" {
        val unowned = PropertyOwnership.Unowned

        unowned.shouldBeInstanceOf<PropertyOwnership.Unowned>()
    }

    // TC-Ownership-002: OwnedByPlayer initialization
    // Given: Player
    // When: PropertyOwnership.OwnedByPlayerを作成
    // Then: playerフィールドが正しく設定される
    "OwnedByPlayer should be initialized with player" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val ownership = PropertyOwnership.OwnedByPlayer(player)

        ownership.player shouldBe player
        ownership.shouldBeInstanceOf<PropertyOwnership.OwnedByPlayer>()
    }

    // TC-Ownership-003: OwnedByPlayer player access
    // Given: OwnedByPlayer
    // When: playerフィールドにアクセス
    // Then: 正しいPlayerが返される
    "OwnedByPlayer should allow access to player field" {
        val player = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val ownership = PropertyOwnership.OwnedByPlayer(player)

        ownership.player.name shouldBe "Bob"
        ownership.player.strategy.shouldBeInstanceOf<AlwaysBuyStrategy>()
    }

    // TC-Ownership-004: Type checking
    // Given: PropertyOwnership のインスタンス
    // When: インスタンスタイプをチェック
    // Then: 正しいサブクラスとして認識される
    "PropertyOwnership subclasses should be correctly identified" {
        val unowned: PropertyOwnership = PropertyOwnership.Unowned
        val owned: PropertyOwnership =
            PropertyOwnership.OwnedByPlayer(
                Player(name = "Charlie", strategy = AlwaysBuyStrategy()),
            )

        unowned.shouldBeInstanceOf<PropertyOwnership.Unowned>()
        owned.shouldBeInstanceOf<PropertyOwnership.OwnedByPlayer>()
    }

    // TC-Ownership-005: Equality check for Unowned
    // Given: PropertyOwnership.Unowned
    // When: 異なる参照で比較
    // Then: singleton なので同じインスタンス
    "Unowned should be a singleton and equal to itself" {
        val unowned1 = PropertyOwnership.Unowned
        val unowned2 = PropertyOwnership.Unowned

        unowned1 shouldBe unowned2
        (unowned1 === unowned2) shouldBe true
    }

    // TC-Ownership-006: Equality check for OwnedByPlayer
    // Given: 同じPlayerを持つOwnedByPlayer
    // When: 等価性をチェック
    // Then: data classなので内容が同じなら等しい
    "OwnedByPlayer with same player should be equal" {
        val player = Player(name = "David", strategy = AlwaysBuyStrategy())
        val ownership1 = PropertyOwnership.OwnedByPlayer(player)
        val ownership2 = PropertyOwnership.OwnedByPlayer(player)

        ownership1 shouldBe ownership2
    }

    // TC-Ownership-007: Inequality check for different players
    // Given: 異なるPlayerを持つOwnedByPlayer
    // When: 等価性をチェック
    // Then: 異なるPlayerなので等しくない
    "OwnedByPlayer with different players should not be equal" {
        val player1 = Player(name = "Eve", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Frank", strategy = AlwaysBuyStrategy())
        val ownership1 = PropertyOwnership.OwnedByPlayer(player1)
        val ownership2 = PropertyOwnership.OwnedByPlayer(player2)

        ownership1 shouldNotBe ownership2
    }

    // TC-Ownership-008: Unowned and OwnedByPlayer are different
    // Given: Unowned と OwnedByPlayer
    // When: 等価性をチェック
    // Then: 異なる型なので等しくない
    "Unowned and OwnedByPlayer should not be equal" {
        val unowned = PropertyOwnership.Unowned
        val owned =
            PropertyOwnership.OwnedByPlayer(
                Player(name = "George", strategy = AlwaysBuyStrategy()),
            )

        unowned shouldNotBe owned
    }

    // TC-Ownership-009: Multiple OwnedByPlayer instances
    // Given: 複数のPlayer
    // When: それぞれでOwnedByPlayerを作成
    // Then: 各インスタンスは独立している
    "Multiple OwnedByPlayer instances should be independent" {
        val player1 = Player(name = "Hannah", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Ian", strategy = AlwaysBuyStrategy())
        val player3 = Player(name = "Jane", strategy = AlwaysBuyStrategy())

        val ownership1 = PropertyOwnership.OwnedByPlayer(player1)
        val ownership2 = PropertyOwnership.OwnedByPlayer(player2)
        val ownership3 = PropertyOwnership.OwnedByPlayer(player3)

        ownership1.player shouldBe player1
        ownership2.player shouldBe player2
        ownership3.player shouldBe player3

        ownership1 shouldNotBe ownership2
        ownership2 shouldNotBe ownership3
        ownership1 shouldNotBe ownership3
    }
})
