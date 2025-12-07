package com.monopoly.server.dto

import com.monopoly.domain.event.GameEvent
import kotlinx.serialization.Serializable

/**
 * ゲームイベントのDTO（Data Transfer Object）
 * JSONシリアライズ可能な形式でイベント情報を保持
 */
@Serializable
data class GameEventDto(
    val turnNumber: Int,
    val timestamp: Long,
    val type: String,
    val description: String,
    val data: Map<String, String> = emptyMap(),
)

/**
 * GameEventをGameEventDtoに変換する
 */
fun GameEvent.toDto(): GameEventDto {
    return when (this) {
        is GameEvent.GameStarted ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "GameStarted",
                description = "Game started with ${playerNames.size} players",
                data = mapOf("playerNames" to playerNames.joinToString(", ")),
            )

        is GameEvent.TurnStarted ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "TurnStarted",
                description = "$playerName's turn started",
                data = mapOf("playerName" to playerName),
            )

        is GameEvent.DiceRolled ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "DiceRolled",
                description = "$playerName rolled $die1 + $die2 = $total",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "die1" to die1.toString(),
                        "die2" to die2.toString(),
                        "total" to total.toString(),
                    ),
            )

        is GameEvent.PlayerMoved ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "PlayerMoved",
                description = "$playerName moved from position $fromPosition to $toPosition${if (passedGo) " (passed GO)" else ""}",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "fromPosition" to fromPosition.toString(),
                        "toPosition" to toPosition.toString(),
                        "passedGo" to passedGo.toString(),
                    ),
            )

        is GameEvent.PropertyPurchased ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "PropertyPurchased",
                description = "$playerName purchased $propertyName for $$$price",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "propertyName" to propertyName,
                        "price" to price.toString(),
                    ),
            )

        is GameEvent.RentPaid ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "RentPaid",
                description = "$payerName paid $$$amount rent to $receiverName for $propertyName",
                data =
                    mapOf(
                        "payerName" to payerName,
                        "receiverName" to receiverName,
                        "propertyName" to propertyName,
                        "amount" to amount.toString(),
                    ),
            )

        is GameEvent.PlayerBankrupted ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "PlayerBankrupted",
                description = "$playerName went bankrupt with $$$finalMoney",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "finalMoney" to finalMoney.toString(),
                    ),
            )

        is GameEvent.GameEnded ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "GameEnded",
                description = "Game ended. Winner: ${winner ?: "None"} after $totalTurns turns",
                data =
                    mapOf(
                        "winner" to (winner ?: "None"),
                        "totalTurns" to totalTurns.toString(),
                    ),
            )

        is GameEvent.TurnEnded ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "TurnEnded",
                description = "$playerName's turn ended",
                data = mapOf("playerName" to playerName),
            )

        is GameEvent.DoublesRolled ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "DoublesRolled",
                description = "$playerName rolled doubles! (count: $doublesCount)",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "doublesCount" to doublesCount.toString(),
                    ),
            )

        is GameEvent.ThreeConsecutiveDoubles ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "ThreeConsecutiveDoubles",
                description = "$playerName rolled 3 consecutive doubles and goes to jail!",
                data = mapOf("playerName" to playerName),
            )

        is GameEvent.HouseBuilt ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "HouseBuilt",
                description = "$playerName built a house on $propertyName (total: $houseCount) for $$$cost",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "propertyName" to propertyName,
                        "houseCount" to houseCount.toString(),
                        "cost" to cost.toString(),
                    ),
            )

        is GameEvent.HotelBuilt ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "HotelBuilt",
                description = "$playerName built a hotel on $propertyName for $$$cost",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "propertyName" to propertyName,
                        "cost" to cost.toString(),
                    ),
            )

        is GameEvent.PlayerSentToJail ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "PlayerSentToJail",
                description = "$playerName was sent to jail. Reason: $reason",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "reason" to reason.toString(),
                    ),
            )

        is GameEvent.JailEscaped ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "JailEscaped",
                description = "$playerName escaped from jail by $method",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "method" to method.toString(),
                    ),
            )

        is GameEvent.JailTurnFailed ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "JailTurnFailed",
                description = "$playerName failed to escape jail this turn",
                data = mapOf("playerName" to playerName),
            )

        is GameEvent.CardDrawn ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "CardDrawn",
                description = "$playerName drew a $cardType card: $cardText",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "cardText" to cardText,
                        "cardType" to cardType.toString(),
                    ),
            )

        is GameEvent.CardHeld ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "CardHeld",
                description = "$playerName is holding a card: $cardText",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "cardText" to cardText,
                    ),
            )

        is GameEvent.MoneyPaid ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "MoneyPaid",
                description = "$playerName paid $$$amount for $reason",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "amount" to amount.toString(),
                        "reason" to reason,
                    ),
            )

        is GameEvent.MoneyReceived ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "MoneyReceived",
                description = "$playerName received $$$amount for $reason",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "amount" to amount.toString(),
                        "reason" to reason,
                    ),
            )

        is GameEvent.PropertyMortgaged ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "PropertyMortgaged",
                description = "$playerName mortgaged $propertyName for $$$mortgageValue",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "propertyName" to propertyName,
                        "mortgageValue" to mortgageValue.toString(),
                    ),
            )

        is GameEvent.PropertyUnmortgaged ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "PropertyUnmortgaged",
                description = "$playerName unmortgaged $propertyName for $$$unmortgageValue",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "propertyName" to propertyName,
                        "unmortgageValue" to unmortgageValue.toString(),
                    ),
            )

        is GameEvent.AuctionStarted ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "AuctionStarted",
                description = "Auction started for $propertyName with ${eligiblePlayers.size} players",
                data =
                    mapOf(
                        "propertyName" to propertyName,
                        "eligiblePlayers" to eligiblePlayers.joinToString(", "),
                    ),
            )

        is GameEvent.PlayerBidInAuction ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "PlayerBidInAuction",
                description = "$playerName bid $$$bidAmount on $propertyName",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "propertyName" to propertyName,
                        "bidAmount" to bidAmount.toString(),
                    ),
            )

        is GameEvent.PlayerPassedInAuction ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "PlayerPassedInAuction",
                description = "$playerName passed on $propertyName auction",
                data =
                    mapOf(
                        "playerName" to playerName,
                        "propertyName" to propertyName,
                    ),
            )

        is GameEvent.AuctionCompleted ->
            GameEventDto(
                turnNumber = turnNumber,
                timestamp = timestamp,
                type = "AuctionCompleted",
                description =
                    if (winnerName != null) {
                        "Auction completed: $winnerName won $propertyName for $$$winningBid"
                    } else {
                        "Auction failed: No bids for $propertyName"
                    },
                data =
                    mapOf(
                        "propertyName" to propertyName,
                        "winnerName" to (winnerName ?: "None"),
                        "winningBid" to winningBid.toString(),
                    ),
            )
    }
}
