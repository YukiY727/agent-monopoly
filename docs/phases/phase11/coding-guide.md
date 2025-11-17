# Phase 11: 戦略パラメータ調整 - コーディングガイド

## 実装順序

1. kotlinx-serialization依存関係の追加
2. StrategyConfigデータクラスの作成
3. StrategyParameterLoaderの実装
4. StrategyRegistryのパラメータ対応
5. GridSearchConfigの実装
6. GridSearchRunnerの実装
7. CLI拡張
8. サンプル設定ファイルの作成
9. テスト

## Step 1: kotlinx-serialization依存関係の追加

**ファイル**: `build.gradle.kts`

```kotlin
plugins {
    // 既存のプラグイン...
    kotlin("plugin.serialization") version "1.9.0"
}

dependencies {
    // 既存の依存関係...
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}
```

## Step 2: StrategyConfigデータクラスの作成

**ファイル**: `src/main/kotlin/com/monopoly/config/StrategyConfig.kt`

```kotlin
package com.monopoly.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull

/**
 * 戦略設定ファイルの構造
 */
@Serializable
data class StrategyConfig(
    val strategies: Map<String, Map<String, JsonElement>> = emptyMap(),
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(jsonString: String): StrategyConfig {
            return json.decodeFromString<StrategyConfig>(jsonString)
        }
    }

    /**
     * 指定した戦略のパラメータを取得
     *
     * @param strategyId 戦略ID
     * @return パラメータMap（型変換済み）
     */
    fun getParameters(strategyId: String): Map<String, Any> {
        val params = strategies[strategyId] ?: return emptyMap()
        return params.mapValues { (_, value) -> convertJsonElement(value) }
    }

    /**
     * JsonElementを適切な型に変換
     */
    private fun convertJsonElement(element: JsonElement): Any {
        return when (element) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content
                    element.booleanOrNull != null -> element.boolean
                    element.intOrNull != null -> element.int
                    element.doubleOrNull != null -> element.double
                    else -> element.content
                }
            }
            else -> element.toString()
        }
    }
}
```

## Step 3: StrategyParameterLoaderの実装

**ファイル**: `src/main/kotlin/com/monopoly/config/StrategyParameterLoader.kt`

```kotlin
package com.monopoly.config

import java.io.File

/**
 * 戦略パラメータ設定ファイルのローダー
 */
object StrategyParameterLoader {
    /**
     * 設定ファイルを読み込む
     *
     * @param filePath 設定ファイルパス
     * @return StrategyConfig
     */
    fun loadFromFile(filePath: String): StrategyConfig {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("Config file not found: $filePath")
        }

        val json = file.readText()
        return try {
            StrategyConfig.fromJson(json)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse config file: ${e.message}", e)
        }
    }

    /**
     * パラメータをバリデーション
     *
     * @param strategyId 戦略ID
     * @param params パラメータMap
     * @return バリデーション結果
     */
    fun validateParameters(strategyId: String, params: Map<String, Any>): Result<Unit> {
        return try {
            when (strategyId) {
                "monopoly" -> {
                    val minCash = params["minCashReserve"] as? Int
                    if (minCash != null && minCash < 0) {
                        throw IllegalArgumentException("minCashReserve must be >= 0")
                    }
                }
                "roi" -> {
                    val minROI = (params["minROI"] as? Number)?.toDouble()
                    if (minROI != null && (minROI < 0.0 || minROI > 1.0)) {
                        throw IllegalArgumentException("minROI must be between 0.0 and 1.0")
                    }
                    val minCash = params["minCashReserve"] as? Int
                    if (minCash != null && minCash < 0) {
                        throw IllegalArgumentException("minCashReserve must be >= 0")
                    }
                }
                "lowprice" -> {
                    val maxPrice = params["maxPrice"] as? Int
                    if (maxPrice != null && maxPrice < 0) {
                        throw IllegalArgumentException("maxPrice must be >= 0")
                    }
                }
                "highvalue" -> {
                    val minRent = params["minRent"] as? Int
                    if (minRent != null && minRent < 0) {
                        throw IllegalArgumentException("minRent must be >= 0")
                    }
                }
                "balanced" -> {
                    val threshold = params["threshold"] as? Int
                    if (threshold != null && threshold < 0) {
                        throw IllegalArgumentException("threshold must be >= 0")
                    }
                }
                "aggressive" -> {
                    val minCash = params["minCashReserve"] as? Int
                    if (minCash != null && minCash < 0) {
                        throw IllegalArgumentException("minCashReserve must be >= 0")
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Step 4: StrategyRegistryのパラメータ対応

**ファイル**: `src/main/kotlin/com/monopoly/domain/strategy/StrategyRegistry.kt`

StrategyRegistryに以下のメソッドを追加：

```kotlin
/**
 * パラメータを指定して戦略インスタンスを生成
 *
 * @param id 戦略ID
 * @param params パラメータMap（キー: パラメータ名、値: パラメータ値）
 * @return 戦略インスタンス
 */
fun getStrategyWithParams(id: String, params: Map<String, Any>): BuyStrategy? {
    return when (id) {
        "monopoly" -> {
            val blockOpponentMonopoly = params["blockOpponentMonopoly"] as? Boolean ?: true
            val minCashReserve = (params["minCashReserve"] as? Number)?.toInt() ?: 300
            MonopolyFirstStrategy(blockOpponentMonopoly, minCashReserve)
        }
        "roi" -> {
            val minROI = (params["minROI"] as? Number)?.toDouble() ?: 0.15
            val minCashReserve = (params["minCashReserve"] as? Number)?.toInt() ?: 300
            ROIStrategy(minROI, minCashReserve)
        }
        "lowprice" -> {
            val maxPrice = (params["maxPrice"] as? Number)?.toInt() ?: 200
            val minCashReserve = (params["minCashReserve"] as? Number)?.toInt() ?: 200
            LowPriceStrategy(maxPrice, minCashReserve)
        }
        "highvalue" -> {
            val minRent = (params["minRent"] as? Number)?.toInt() ?: 20
            val minCashReserve = (params["minCashReserve"] as? Number)?.toInt() ?: 100
            HighValueStrategy(minRent, minCashReserve)
        }
        "balanced" -> {
            val threshold = (params["threshold"] as? Number)?.toInt() ?: 80
            val minCashReserve = (params["minCashReserve"] as? Number)?.toInt() ?: 400
            BalancedStrategy(threshold, minCashReserve)
        }
        "aggressive" -> {
            val minCashReserve = (params["minCashReserve"] as? Number)?.toInt() ?: 300
            AggressiveStrategy(minCashReserve)
        }
        else -> getStrategy(id)  // パラメータなしの戦略
    }
}

/**
 * 戦略のデフォルトパラメータを取得
 */
fun getDefaultParameters(id: String): Map<String, Any> {
    return when (id) {
        "monopoly" -> mapOf(
            "blockOpponentMonopoly" to true,
            "minCashReserve" to 300
        )
        "roi" -> mapOf(
            "minROI" to 0.15,
            "minCashReserve" to 300
        )
        "lowprice" -> mapOf(
            "maxPrice" to 200,
            "minCashReserve" to 200
        )
        "highvalue" -> mapOf(
            "minRent" to 20,
            "minCashReserve" to 100
        )
        "balanced" -> mapOf(
            "threshold" to 80,
            "minCashReserve" to 400
        )
        "aggressive" -> mapOf(
            "minCashReserve" to 300
        )
        else -> emptyMap()
    }
}
```

## Step 5: GridSearchConfigの実装

**ファイル**: `src/main/kotlin/com/monopoly/config/GridSearchConfig.kt`

```kotlin
package com.monopoly.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * グリッドサーチ設定
 */
@Serializable
data class GridSearchConfig(
    val strategy: String,
    val parameters: Map<String, JsonArray>,
    val opponents: List<OpponentConfig> = emptyList(),
    val gamesPerCombination: Int = 100,
) {
    @Serializable
    data class OpponentConfig(
        val strategy: String,
        val parameters: Map<String, JsonElement> = emptyMap(),
    )

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(jsonString: String): GridSearchConfig {
            return json.decodeFromString<GridSearchConfig>(jsonString)
        }
    }

    /**
     * 全パラメータの組み合わせを生成
     */
    fun generateCombinations(): List<Map<String, Any>> {
        if (parameters.isEmpty()) {
            return listOf(emptyMap())
        }

        val keys = parameters.keys.toList()
        val valueLists = parameters.values.map { jsonArray ->
            jsonArray.map { convertJsonElement(it) }
        }

        return cartesianProduct(valueLists).map { values ->
            keys.zip(values).toMap()
        }
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

    private fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
        if (lists.isEmpty()) {
            return listOf(emptyList())
        }

        val result = mutableListOf<List<T>>()
        cartesianProductHelper(lists, 0, mutableListOf(), result)
        return result
    }

    private fun <T> cartesianProductHelper(
        lists: List<List<T>>,
        index: Int,
        current: MutableList<T>,
        result: MutableList<List<T>>
    ) {
        if (index == lists.size) {
            result.add(current.toList())
            return
        }

        for (item in lists[index]) {
            current.add(item)
            cartesianProductHelper(lists, index + 1, current, result)
            current.removeAt(current.size - 1)
        }
    }
}
```

## Step 6: GridSearchRunnerの実装

**ファイル**: `src/main/kotlin/com/monopoly/simulation/GridSearchRunner.kt`

```kotlin
package com.monopoly.simulation

import com.monopoly.config.GridSearchConfig
import com.monopoly.domain.model.Board
import com.monopoly.domain.strategy.StrategyRegistry
import com.monopoly.statistics.StatisticsCalculator
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
        println("Best win rate: ${results.maxByOrNull { it.winRate }?.winRate}")

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

    private fun convertJsonElement(element: kotlinx.serialization.json.JsonElement): Any {
        // GridSearchConfigと同じ変換ロジック
        return when (element) {
            is kotlinx.serialization.json.JsonPrimitive -> {
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
```

## Step 7: CLI拡張

**ファイル**: `src/main/kotlin/com/monopoly/cli/Main.kt`

main関数の最初に以下を追加：

```kotlin
// --show-params <strategy> オプション
val showParamsIndex = args.indexOf("--show-params")
if (showParamsIndex != -1 && showParamsIndex + 1 < args.size) {
    val strategyId = args[showParamsIndex + 1]
    val defaultParams = StrategyRegistry.getDefaultParameters(strategyId)
    if (defaultParams.isEmpty()) {
        println("Strategy '$strategyId' has no parameters")
    } else {
        println("Default parameters for '$strategyId':")
        defaultParams.forEach { (key, value) ->
            println("  $key: $value")
        }
    }
    exitProcess(0)
}

// --config <file> オプション
var strategyConfig: StrategyConfig? = null
val configIndex = args.indexOf("--config")
if (configIndex != -1 && configIndex + 1 < args.size) {
    val configFile = args[configIndex + 1]
    try {
        strategyConfig = StrategyParameterLoader.loadFromFile(configFile)
        println("Loaded configuration from: $configFile")
    } catch (e: Exception) {
        println("Error loading config file: ${e.message}")
        exitProcess(1)
    }
}

// --grid-search <file> オプション
val gridSearchIndex = args.indexOf("--grid-search")
if (gridSearchIndex != -1 && gridSearchIndex + 1 < args.size) {
    val gridSearchFile = args[gridSearchIndex + 1]
    try {
        val gridConfig = GridSearchConfig.fromJson(File(gridSearchFile).readText())
        val board = createStandardBoard()
        val gameService = GameService()
        val dice = Dice()
        val parallelGameRunner = ParallelGameRunner(gameService, dice)
        val runner = GridSearchRunner(parallelGameRunner, board)

        val result = runBlocking { runner.run(gridConfig) }

        // 結果を保存
        result.saveToFile("grid-search-results.csv")
        println("\nResults saved to grid-search-results.csv")
    } catch (e: Exception) {
        println("Error running grid search: ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
    exitProcess(0)
}
```

createStrategy関数を修正：

```kotlin
fun createStrategy(strategyName: String, config: StrategyConfig?): BuyStrategy {
    // 設定ファイルからパラメータを取得
    val params = config?.getParameters(strategyName) ?: emptyMap()

    // パラメータ付きで戦略を生成
    val strategy = if (params.isNotEmpty()) {
        StrategyRegistry.getStrategyWithParams(strategyName, params)
    } else {
        StrategyRegistry.getStrategy(strategyName)
    }

    if (strategy != null) {
        return strategy
    }

    // エラー処理（既存のコード）
    ...
}
```

## Step 8: サンプル設定ファイルの作成

**ファイル**: `config/strategy-params.json`

```json
{
  "strategies": {
    "monopoly": {
      "blockOpponentMonopoly": true,
      "minCashReserve": 250
    },
    "roi": {
      "minROI": 0.18,
      "minCashReserve": 350
    },
    "balanced": {
      "threshold": 70,
      "minCashReserve": 450
    }
  }
}
```

**ファイル**: `config/grid-search-example.json`

```json
{
  "strategy": "roi",
  "parameters": {
    "minROI": [0.10, 0.15, 0.20, 0.25],
    "minCashReserve": [200, 300, 400, 500]
  },
  "opponents": [
    {"strategy": "monopoly"},
    {"strategy": "aggressive"},
    {"strategy": "conservative"}
  ],
  "gamesPerCombination": 100
}
```

## チェックリスト

- [ ] kotlinx-serialization依存関係を追加
- [ ] StrategyConfigクラスを作成
- [ ] StrategyParameterLoaderを実装
- [ ] StrategyRegistryにgetStrategyWithParamsを追加
- [ ] StrategyRegistryにgetDefaultParametersを追加
- [ ] GridSearchConfigクラスを作成
- [ ] GridSearchRunnerを実装
- [ ] Main.ktに--show-paramsオプションを追加
- [ ] Main.ktに--configオプションを追加
- [ ] Main.ktに--grid-searchオプションを追加
- [ ] サンプル設定ファイルを作成
- [ ] 動作確認テスト
