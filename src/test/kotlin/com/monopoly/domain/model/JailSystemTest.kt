package com.monopoly.domain.model

import com.monopoly.domain.model.jail.JailStatus
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class JailSystemTest : StringSpec({
    // TC-JAIL-001: プレイヤーを刑務所に送る
    // Given: 通常状態のPlayer
    // When: sendToJail()を呼ぶ
    // Then: jailStatusがJailedになる
    "should send player to jail" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        
        player.sendToJail()
        
        player.state.jailStatus shouldBe JailStatus.Jailed
        player.state.turnsInJail shouldBe 0
    }

    // TC-JAIL-002: $50支払いで刑務所から脱出
    // Given: 刑務所に収監されているPlayer
    // When: escapeJailByPayment()を呼ぶ
    // Then: jailStatusがFreeになり、$50減る
    "should escape jail by paying $50" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        player.sendToJail()
        val initialMoney = player.money
        
        player.escapeJailByPayment()
        
        player.state.jailStatus shouldBe JailStatus.Free
        player.state.turnsInJail shouldBe 0
        player.money shouldBe initialMoney - 50
    }

    // TC-JAIL-003: 刑務所でのターン数をカウント
    // Given: 刑務所に収監されているPlayer
    // When: incrementJailTurn()を呼ぶ
    // Then: turnsInJailが1増える
    "should increment turns in jail" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        player.sendToJail()
        
        player.incrementJailTurn()
        
        player.state.jailStatus shouldBe JailStatus.Jailed
        player.state.turnsInJail shouldBe 1
        
        player.incrementJailTurn()
        player.state.turnsInJail shouldBe 2
    }

    // TC-JAIL-004: ゾロ目で刑務所から脱出
    // Given: 刑務所に収監されているPlayer
    // When: escapeJailByDoubles()を呼ぶ
    // Then: jailStatusがFreeになり、所持金は減らない
    "should escape jail by rolling doubles" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        player.sendToJail()
        val initialMoney = player.money
        
        player.escapeJailByDoubles()
        
        player.state.jailStatus shouldBe JailStatus.Free
        player.state.turnsInJail shouldBe 0
        player.money shouldBe initialMoney
    }

    // TC-JAIL-005: 強制脱出（3ターン経過後など）
    // Given: 刑務所に収監されているPlayer
    // When: forceEscapeJail()を呼ぶ
    // Then: jailStatusがFreeになり、$50減る
    "should force escape jail by paying $50" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        player.sendToJail()
        val initialMoney = player.money
        
        player.forceEscapeJail()
        
        player.state.jailStatus shouldBe JailStatus.Free
        player.state.turnsInJail shouldBe 0
        player.money shouldBe initialMoney - 50
    }
})
