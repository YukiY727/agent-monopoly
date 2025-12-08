package com.monopoly.server.dto

import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.model.game.GameState
import com.monopoly.domain.model.jail.JailStatus
import com.monopoly.domain.model.property.StreetProperty
import kotlinx.serialization.Serializable

/**
 * プロパティの詳細情報DTO
 */
@Serializable
data class PropertyDetailDto(
    val name: String,
    val price: Int,
    val colorGroup: String?,
    val houseCount: Int,
    val hasHotel: Boolean,
    val isMortgaged: Boolean,
)

/**
 * プレイヤー状態のDTO
 */
@Serializable
data class PlayerStateDto(
    val name: String,
    val money: Int,
    val position: Int,
    val isBankrupt: Boolean,
    val ownedProperties: List<PropertyDetailDto>,
    val isInJail: Boolean,
)

/**
 * ダイスロール結果のDTO
 */
@Serializable
data class DiceRollDto(
    val die1: Int,
    val die2: Int,
    val total: Int,
    val isDoubles: Boolean,
)

/**
 * ゲーム全体の状態のDTO
 */
@Serializable
data class GameStateDto(
    val players: List<PlayerStateDto>,
    val currentPlayerIndex: Int,
    val turnNumber: Int,
    val isGameOver: Boolean,
    val lastDiceRoll: DiceRollDto? = null,
)

/**
 * ゲーム実行結果のDTO
 */
@Serializable
data class GameResultDto(
    val initialState: GameStateDto,
    val finalState: GameStateDto,
    val events: List<GameEventDto>,
    val winner: String?,
    val totalTurns: Int,
)

/**
 * GameStateをGameStateDtoに変換する
 */
fun GameState.toDto(): GameStateDto {
    // 最新のダイスロールイベントを探す
    val lastDiceRollEvent: GameEvent.DiceRolled? =
        events
            .filterIsInstance<GameEvent.DiceRolled>()
            .lastOrNull()

    val lastDiceRollDto: DiceRollDto? =
        lastDiceRollEvent?.let {
            DiceRollDto(
                die1 = it.die1,
                die2 = it.die2,
                total = it.total,
                isDoubles = it.die1 == it.die2,
            )
        }

    return GameStateDto(
        players =
            players.map { player ->
                PlayerStateDto(
                    name = player.name,
                    money = player.money,
                    position = player.position,
                    isBankrupt = player.isBankrupt,
                    ownedProperties =
                        player.ownedProperties.map { property ->
                            PropertyDetailDto(
                                name = property.name,
                                price = property.price,
                                colorGroup = if (property is StreetProperty) property.colorGroup.name else null,
                                houseCount = if (property is StreetProperty) property.buildings.houseCount else 0,
                                hasHotel = if (property is StreetProperty) property.buildings.hasHotel else false,
                                isMortgaged = property.isMortgaged(),
                            )
                        },
                    isInJail = player.state.jailStatus == JailStatus.Jailed,
                )
            },
        currentPlayerIndex = players.indexOf(currentPlayer),
        turnNumber = turnNumber,
        isGameOver = isGameOver,
        lastDiceRoll = lastDiceRollDto,
    )
}
