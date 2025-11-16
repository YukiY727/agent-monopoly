package com.monopoly.statistics

import com.monopoly.simulation.MultiGameResult

/**
 * 統計データを計算するクラス
 */
class StatisticsCalculator {
    /**
     * 統計データを計算
     *
     * @param result 複数ゲーム実行結果
     * @return 統計データ
     */
    fun calculate(result: MultiGameResult): GameStatistics {
        val playerStats = calculatePlayerStatistics(result)
        val turnStats = calculateTurnStatistics(result)

        return GameStatistics(
            totalGames = result.totalGames,
            playerStats = playerStats,
            turnStats = turnStats,
        )
    }

    /**
     * プレイヤー別統計を計算
     */
    private fun calculatePlayerStatistics(result: MultiGameResult): Map<String, PlayerStatistics> {
        // 全プレイヤー名を取得
        val allPlayerNames = result.gameResults
            .flatMap { it.finalState.players.map { player -> player.name } }
            .distinct()

        return allPlayerNames.associateWith { playerName ->
            calculateSinglePlayerStatistics(playerName, result)
        }
    }

    /**
     * 単一プレイヤーの統計を計算
     */
    private fun calculateSinglePlayerStatistics(
        playerName: String,
        result: MultiGameResult,
    ): PlayerStatistics {
        val wins = result.gameResults.count { it.winner == playerName }
        val winRate = wins.toDouble() / result.totalGames

        // 各ゲームでのプレイヤーの最終状態を取得
        val playerFinalStates = result.gameResults.mapNotNull { gameResult ->
            gameResult.finalState.players.find { it.name == playerName }
        }

        val averageFinalAssets = if (playerFinalStates.isNotEmpty()) {
            playerFinalStates.map { it.getTotalAssets() }.average()
        } else {
            0.0
        }

        val averageFinalCash = if (playerFinalStates.isNotEmpty()) {
            playerFinalStates.map { it.money }.average()
        } else {
            0.0
        }

        val averagePropertiesOwned = if (playerFinalStates.isNotEmpty()) {
            playerFinalStates.map { it.ownedProperties.size }.average()
        } else {
            0.0
        }

        return PlayerStatistics(
            playerName = playerName,
            wins = wins,
            winRate = winRate,
            averageFinalAssets = averageFinalAssets,
            averageFinalCash = averageFinalCash,
            averagePropertiesOwned = averagePropertiesOwned,
        )
    }

    /**
     * ターン統計を計算
     */
    private fun calculateTurnStatistics(result: MultiGameResult): TurnStatistics {
        val turns = result.gameResults.map { it.totalTurns }
        val averageTurns = if (turns.isNotEmpty()) turns.average() else 0.0

        return TurnStatistics(
            averageTurns = averageTurns,
            minTurns = result.getMinTurns(),
            maxTurns = result.getMaxTurns(),
            standardDeviation = TurnStatistics.calculateStandardDeviation(turns, averageTurns),
        )
    }
}
