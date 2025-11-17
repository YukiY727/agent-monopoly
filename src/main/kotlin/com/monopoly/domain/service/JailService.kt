package com.monopoly.domain.service

import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Money
import com.monopoly.domain.model.Player

/**
 * Jail（牢屋）システムのサービス
 *
 * Phase 16: Jailシステムの実装
 */
@Suppress("MagicNumber")
class JailService {
    companion object {
        const val JAIL_POSITION = 10
        const val FINE_AMOUNT = 50
        const val MAX_JAIL_TURNS = 3
    }

    /**
     * プレイヤーをJailに送る
     */
    fun sendToJail(
        player: Player,
        gameState: GameState,
    ) {
        player.sendToJail()

        gameState.events.add(
            GameEvent.PlayerSentToJail(
                turnNumber = gameState.turnNumber,
                playerName = player.name,
            ),
        )
    }

    /**
     * 罰金を払ってJailから脱出
     *
     * @return 脱出成功ならtrue
     */
    fun payFineToExit(
        player: Player,
        gameState: GameState,
    ): Boolean {
        if (!player.inJail) {
            return false
        }

        if (player.money < FINE_AMOUNT) {
            return false
        }

        player.pay(Money(FINE_AMOUNT))
        player.releaseFromJail()

        gameState.events.add(
            GameEvent.PlayerExitedJail(
                turnNumber = gameState.turnNumber,
                playerName = player.name,
                method = "paid_fine",
            ),
        )

        return true
    }

    /**
     * ゾロ目でJailから脱出を試みる
     *
     * @return 脱出成功ならtrue
     */
    fun attemptExitWithDoubles(
        player: Player,
        die1: Int,
        die2: Int,
        gameState: GameState,
    ): Boolean {
        if (!player.inJail) {
            return false
        }

        val isDoubles = die1 == die2

        if (isDoubles) {
            player.releaseFromJail()
            gameState.events.add(
                GameEvent.PlayerExitedJail(
                    turnNumber = gameState.turnNumber,
                    playerName = player.name,
                    method = "rolled_doubles",
                ),
            )
            return true
        }

        // ゾロ目でない場合、ターン数を増やす
        player.incrementJailTurns()

        // 3ターン経過したら強制脱出
        if (player.jailTurns >= MAX_JAIL_TURNS) {
            forceExit(player, gameState)
            return true
        }

        return false
    }

    /**
     * Get Out of Jail Freeカードを使って脱出
     *
     * @return 脱出成功ならtrue
     */
    fun useGetOutOfJailFreeCard(
        player: Player,
        gameState: GameState,
    ): Boolean {
        if (!player.inJail) {
            return false
        }

        if (player.getOutOfJailFreeCards == 0) {
            return false
        }

        player.useGetOutOfJailFreeCard()
        player.releaseFromJail()

        gameState.events.add(
            GameEvent.PlayerExitedJail(
                turnNumber = gameState.turnNumber,
                playerName = player.name,
                method = "used_card",
            ),
        )

        return true
    }

    /**
     * 3ターン経過後の強制脱出（罰金を払う）
     */
    private fun forceExit(
        player: Player,
        gameState: GameState,
    ) {
        player.pay(Money(FINE_AMOUNT))
        player.releaseFromJail()

        gameState.events.add(
            GameEvent.PlayerExitedJail(
                turnNumber = gameState.turnNumber,
                playerName = player.name,
                method = "forced_after_3_turns",
            ),
        )
    }
}
