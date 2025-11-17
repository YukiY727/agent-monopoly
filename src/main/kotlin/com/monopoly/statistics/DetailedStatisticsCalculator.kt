package com.monopoly.statistics

import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.Space
import com.monopoly.simulation.MultiGameResult
import kotlin.math.max

/**
 * 詳細統計を計算するクラス
 */
class DetailedStatisticsCalculator(
    private val basicCalculator: StatisticsCalculator = StatisticsCalculator(),
) {
    /**
     * 詳細統計を計算
     *
     * @param multiGameResult 複数ゲームの結果
     * @return 詳細統計
     */
    fun calculate(multiGameResult: MultiGameResult): DetailedStatistics {
        val basicStats = basicCalculator.calculate(multiGameResult)
        val propertyStats = calculatePropertyStatistics(multiGameResult)
        val assetHistory = calculateAssetHistory(multiGameResult)
        val bankruptcyAnalysis = calculateBankruptcyAnalysis(multiGameResult)

        return DetailedStatistics(
            basicStats = basicStats,
            propertyStatistics = propertyStats,
            assetHistory = assetHistory,
            bankruptcyAnalysis = bankruptcyAnalysis,
        )
    }

    /**
     * プロパティ統計を計算
     */
    private fun calculatePropertyStatistics(
        result: MultiGameResult,
    ): List<PropertyStatistics> {
        val allProperties = extractAllProperties(result)
        return allProperties.map { property ->
            calculateSinglePropertyStatistics(result, property)
        }.sortedByDescending { it.roi }
    }

    /**
     * 全プロパティを抽出
     */
    private fun extractAllProperties(result: MultiGameResult): List<Property> {
        return result.gameResults
            .firstOrNull()
            ?.finalState
            ?.board
            ?.getSpaces()
            ?.filterIsInstance<Space.PropertySpace>()
            ?.map { it.property }
            ?: emptyList()
    }

    /**
     * 単一プロパティの統計を計算
     */
    private fun calculateSinglePropertyStatistics(
        result: MultiGameResult,
        property: Property,
    ): PropertyStatistics {
        var purchaseCount = 0
        var totalRentCollected = 0
        var maxRentInGame = 0
        var winCountWhenOwned = 0
        var totalTurnsHeldByWinner = 0

        result.gameResults.forEach { game ->
            val events = game.finalState.events

            val wasPurchased = events.any { event ->
                event is GameEvent.PropertyPurchased &&
                    event.propertyName == property.name
            }

            if (wasPurchased) {
                purchaseCount++

                val rentInThisGame = events
                    .filterIsInstance<GameEvent.RentPaid>()
                    .filter { it.propertyName == property.name }
                    .sumOf { it.amount }

                totalRentCollected += rentInThisGame
                maxRentInGame = max(maxRentInGame, rentInThisGame)

                val winner = game.finalState.players.find { it.name == game.winner }
                if (winner?.ownedProperties?.any { it.name == property.name } == true) {
                    winCountWhenOwned++
                    totalTurnsHeldByWinner += game.totalTurns
                }
            }
        }

        return PropertyStatistics(
            propertyName = property.name,
            position = property.position,
            colorGroup = property.colorGroup.name,
            price = property.price,
            purchaseCount = purchaseCount,
            purchaseRate = purchaseCount.toDouble() / result.totalGames,
            totalRentCollected = totalRentCollected,
            averageRentPerGame = if (purchaseCount > 0) {
                totalRentCollected.toDouble() / purchaseCount
            } else {
                0.0
            },
            maxRentInSingleGame = maxRentInGame,
            winRateWhenOwned = if (purchaseCount > 0) {
                winCountWhenOwned.toDouble() / purchaseCount
            } else {
                0.0
            },
            avgTurnsHeldByWinner = if (winCountWhenOwned > 0) {
                totalTurnsHeldByWinner.toDouble() / winCountWhenOwned
            } else {
                0.0
            },
            roi = if (purchaseCount > 0) {
                (totalRentCollected.toDouble() / property.price) / result.totalGames
            } else {
                0.0
            },
        )
    }

    /**
     * 資産推移を計算
     */
    private fun calculateAssetHistory(
        result: MultiGameResult,
    ): AssetHistory {
        val snapshots = result.gameResults.flatMap { game ->
            // 最終状態のスナップショット
            game.finalState.players.map { player ->
                AssetSnapshot(
                    turnNumber = game.totalTurns,
                    playerName = player.name,
                    cash = player.money,
                    totalAssets = player.getTotalAssets(),
                    propertiesCount = player.ownedProperties.size,
                )
            }
        }

        return AssetHistory(snapshots)
    }

    /**
     * 破産分析を計算
     */
    private fun calculateBankruptcyAnalysis(
        result: MultiGameResult,
    ): BankruptcyAnalysis {
        val bankruptcyEvents = result.gameResults.flatMap { game ->
            game.finalState.events
                .filterIsInstance<GameEvent.PlayerBankrupted>()
                .map { event ->
                    BankruptcyEvent(
                        gameNumber = game.gameNumber,
                        turnNumber = event.turnNumber,
                        playerName = event.playerName,
                        causePlayerName = null, // イベントにcreditorフィールドがない
                        lastCash = event.finalMoney,
                        propertiesOwned = 0, // イベントにプロパティ情報がない
                    )
                }
        }

        val averageTurn = if (bankruptcyEvents.isNotEmpty()) {
            bankruptcyEvents.map { it.turnNumber }.average()
        } else {
            0.0
        }

        val distribution = bankruptcyEvents
            .groupBy { it.turnNumber / 10 * 10 }
            .mapKeys { (key, _) -> key..(key + 9) }
            .mapValues { it.value.size }

        return BankruptcyAnalysis(
            bankruptcyEvents = bankruptcyEvents,
            totalBankruptcies = bankruptcyEvents.size,
            averageBankruptcyTurn = averageTurn,
            bankruptcyDistribution = distribution,
        )
    }
}
