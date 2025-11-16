package com.monopoly.simulation

import com.monopoly.config.GridSearchConfig
import com.monopoly.domain.model.Board
import com.monopoly.domain.strategy.StrategyRegistry
import com.monopoly.statistics.StatisticsCalculator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.io.File

/**
 * グリッドサーチ結果
 */
data class GridSearchResult(
    val results: List<ParameterSetResult>,
) {
    data class ParameterSetResult(
        val parameters: Map<String, Any>,
        val winRate: Double,
        val avgFinalAssets: Double,
        val gamesPlayed: Int,
    )

    val bestParameterSet: ParameterSetResult
        get() = results.maxByOrNull { it.winRate }!!

    fun toCsv(): String {
        val sb = StringBuilder()

        // ヘッダー
        val paramKeys = results.firstOrNull()?.parameters?.keys?.sorted() ?: emptyList()
        sb.appendLine((paramKeys + listOf("winRate", "avgFinalAssets", "gamesPlayed")).joinToString(","))

        // データ
        results.forEach { result ->
            val values = paramKeys.map { result.parameters[it].toString() } +
                listOf(result.winRate, result.avgFinalAssets, result.gamesPlayed)
            sb.appendLine(values.joinToString(","))
        }

        return sb.toString()
    }

    fun saveToFile(filePath: String) {
        File(filePath).writeText(toCsv())
    }
}

/**
 * グリッドサーチを実行するクラス
 */
class GridSearchRunner(
    private val parallelGameRunner: ParallelGameRunner,
    private val board: Board,
) {
    /**
     * グリッドサーチを実行
     */
    suspend fun run(config: GridSearchConfig): GridSearchResult {
        val combinations = config.generateCombinations()
        println("Grid Search Configuration:")
        println("  Strategy: ${config.strategy}")
        println("  Parameter combinations: ${combinations.size}")
        println("  Games per combination: ${config.gamesPerCombination}")
        println("  Opponents: ${config.opponents.map { it.strategy }}")
        println()

        val results = combinations.mapIndexed { index, params ->
            println("[${index + 1}/${combinations.size}] Testing: $params")
            runWithParameters(config, params)
        }

        println()
        println("Grid Search Complete!")
        println("Best parameters: ${results.maxByOrNull { it.winRate }?.parameters}")
        println("Best win rate: ${String.format("%.2f%%", (results.maxByOrNull { it.winRate }?.winRate ?: 0.0) * 100)}")

        return GridSearchResult(results)
    }

    /**
     * 特定のパラメータセットでゲームを実行
     */
    private suspend fun runWithParameters(
        config: GridSearchConfig,
        params: Map<String, Any>
    ): GridSearchResult.ParameterSetResult {
        // 対象戦略を生成
        val targetStrategy = StrategyRegistry.getStrategyWithParams(config.strategy, params)
            ?: throw IllegalArgumentException("Unknown strategy: ${config.strategy}")

        // 対戦相手を生成
        val opponents = config.opponents.map { opponentConfig ->
            val opponentParams = opponentConfig.parameters.mapValues { (_, value) ->
                convertJsonElement(value)
            }
            val opponentStrategy = StrategyRegistry.getStrategyWithParams(
                opponentConfig.strategy,
                opponentParams
            ) ?: throw IllegalArgumentException("Unknown strategy: ${opponentConfig.strategy}")

            Pair(opponentConfig.strategy, opponentStrategy)
        }

        // プレイヤー設定
        val playerStrategies = listOf(
            Pair("Target", targetStrategy)
        ) + opponents

        // ゲーム実行
        val multiGameResult = parallelGameRunner.runMultipleGames(
            numberOfGames = config.gamesPerCombination,
            playerStrategies = playerStrategies,
            board = board,
            showProgress = false
        )

        // 統計計算
        val calculator = StatisticsCalculator()
        val stats = calculator.calculate(multiGameResult)
        val targetPlayerStats = stats.playerStats["Target"]!!

        return GridSearchResult.ParameterSetResult(
            parameters = params,
            winRate = targetPlayerStats.winRate,
            avgFinalAssets = targetPlayerStats.averageFinalAssets,
            gamesPlayed = config.gamesPerCombination
        )
    }

    private fun convertJsonElement(element: JsonElement): Any {
        return when (element) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content
                    element.content.toBooleanStrictOrNull() != null -> element.content.toBoolean()
                    element.content.toIntOrNull() != null -> element.content.toInt()
                    element.content.toDoubleOrNull() != null -> element.content.toDouble()
                    else -> element.content
                }
            }
            else -> element.toString()
        }
    }
}
