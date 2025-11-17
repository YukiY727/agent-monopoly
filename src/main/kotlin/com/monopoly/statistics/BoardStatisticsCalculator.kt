package com.monopoly.statistics

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.GameEvent
import com.monopoly.simulation.MultiGameResult

/**
 * ボード統計を計算
 */
class BoardStatisticsCalculator {
    /**
     * 複数ゲームからボード統計を計算
     */
    fun calculate(multiGameResult: MultiGameResult, board: Board): BoardStatistics {
        // プロパティ名 → 位置のマップを作成
        val propertyNameToPosition = board.getAllProperties().associate { it.name to it.position }

        val positionStatsMap = mutableMapOf<Int, MutablePositionStats>()

        multiGameResult.gameResults.forEach { gameResult ->
            gameResult.finalState.events.forEach { event ->
                when (event) {
                    is GameEvent.PlayerMoved -> {
                        val position = event.toPosition
                        val stats = positionStatsMap.getOrPut(position) {
                            MutablePositionStats(position)
                        }
                        stats.landedCount++
                    }
                    is GameEvent.RentPaid -> {
                        val position = propertyNameToPosition[event.propertyName]
                        if (position != null) {
                            val stats = positionStatsMap.getOrPut(position) {
                                MutablePositionStats(position)
                            }
                            stats.totalRentCollected += event.amount.toDouble()
                        }
                    }
                    is GameEvent.PropertyPurchased -> {
                        val position = propertyNameToPosition[event.propertyName]
                        if (position != null) {
                            val stats = positionStatsMap.getOrPut(position) {
                                MutablePositionStats(position)
                            }
                            stats.ownershipDistribution[event.playerName] =
                                stats.ownershipDistribution.getOrDefault(event.playerName, 0) + 1
                        }
                    }
                    else -> {
                        // 他のイベントは無視
                    }
                }
            }
        }

        return BoardStatistics(
            positionStats = positionStatsMap.mapValues { (_, stats) ->
                BoardStatistics.PositionStatistics(
                    position = stats.position,
                    landedCount = stats.landedCount,
                    totalRentCollected = stats.totalRentCollected,
                    ownershipDistribution = stats.ownershipDistribution.toMap()
                )
            },
            totalGames = multiGameResult.totalGames
        )
    }

    /**
     * 内部用の可変統計データ
     */
    private data class MutablePositionStats(
        val position: Int,
        var landedCount: Int = 0,
        var totalRentCollected: Double = 0.0,
        val ownershipDistribution: MutableMap<String, Int> = mutableMapOf()
    )
}
