# Phase 11: 戦略パラメータ調整 - 設計書

## アーキテクチャ設計

### コンポーネント構成

```
┌──────────────────────────────────────────┐
│   strategy-params.json (設定ファイル)     │
└──────────────────────────────────────────┘
              │
              │ 読み込み
              ▼
┌──────────────────────────────────────────┐
│    StrategyParameterLoader               │
│  - loadFromFile(path)                    │
│  - parseParameters(json)                 │
└──────────────────────────────────────────┘
              │
              │ パラメータMap
              ▼
┌──────────────────────────────────────────┐
│       StrategyRegistry                   │
│  - getStrategy(id, params)               │
│  - createWithParams(id, params)          │
└──────────────────────────────────────────┘
              │
              ▼
        BuyStrategy インスタンス


┌──────────────────────────────────────────┐
│   grid-search.json (グリッドサーチ設定)   │
└──────────────────────────────────────────┘
              │
              │ 読み込み
              ▼
┌──────────────────────────────────────────┐
│      GridSearchConfig                    │
│  - strategy: String                      │
│  - parameters: Map<String, List<Any>>    │
│  - opponents: List<OpponentConfig>       │
│  - gamesPerCombination: Int              │
└──────────────────────────────────────────┘
              │
              ▼
┌──────────────────────────────────────────┐
│       GridSearchRunner                   │
│  - run(config)                           │
│  - generateCombinations()                │
│  - runWithParams(params)                 │
└──────────────────────────────────────────┘
              │
              ▼
        GridSearchResult (CSV/JSON出力)
```

## データ構造設計

### StrategyParameters

戦略パラメータを型安全に管理するデータクラス群：

```kotlin
sealed class StrategyParameters {
    data class MonopolyFirst(
        val blockOpponentMonopoly: Boolean = true,
        val minCashReserve: Int = 300,
    ) : StrategyParameters()

    data class ROI(
        val minROI: Double = 0.15,
        val minCashReserve: Int = 300,
    ) : StrategyParameters()

    data class LowPrice(
        val maxPrice: Int = 200,
        val minCashReserve: Int = 200,
    ) : StrategyParameters()

    data class HighValue(
        val minRent: Int = 20,
        val minCashReserve: Int = 100,
    ) : StrategyParameters()

    data class Balanced(
        val threshold: Int = 80,
        val minCashReserve: Int = 400,
    ) : StrategyParameters()

    data class Aggressive(
        val minCashReserve: Int = 300,
    ) : StrategyParameters()

    object AlwaysBuy : StrategyParameters()
    object Random : StrategyParameters()
    object Conservative : StrategyParameters()
}
```

### StrategyConfig

JSON設定ファイルの構造：

```kotlin
data class StrategyConfig(
    val strategies: Map<String, Map<String, Any>>,
) {
    companion object {
        fun fromJson(json: String): StrategyConfig {
            // JSON解析
        }
    }

    fun getParameters(strategyId: String): Map<String, Any>? {
        return strategies[strategyId]
    }
}
```

### GridSearchConfig

グリッドサーチ設定：

```kotlin
data class GridSearchConfig(
    val strategy: String,
    val parameters: Map<String, List<Any>>,
    val opponents: List<OpponentConfig>,
    val gamesPerCombination: Int = 100,
) {
    data class OpponentConfig(
        val strategy: String,
        val parameters: Map<String, Any> = emptyMap(),
    )

    companion object {
        fun fromJson(json: String): GridSearchConfig {
            // JSON解析
        }
    }

    /**
     * 全パラメータの組み合わせを生成
     */
    fun generateCombinations(): List<Map<String, Any>> {
        // カルテシアン積を計算
    }
}
```

### GridSearchResult

グリッドサーチの結果：

```kotlin
data class GridSearchResult(
    val results: List<ParameterSetResult>,
    val bestParameterSet: ParameterSetResult,
) {
    data class ParameterSetResult(
        val parameters: Map<String, Any>,
        val winRate: Double,
        val avgFinalAssets: Double,
        val gamesPlayed: Int,
    )

    fun toCsv(): String {
        // CSV形式に変換
    }

    fun toJson(): String {
        // JSON形式に変換
    }
}
```

## StrategyRegistry拡張

### パラメータ付きファクトリ

```kotlin
object StrategyRegistry {
    // 既存のメタデータ管理...

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
                val minCashReserve = params["minCashReserve"] as? Int ?: 300
                MonopolyFirstStrategy(blockOpponentMonopoly, minCashReserve)
            }
            "roi" -> {
                val minROI = (params["minROI"] as? Number)?.toDouble() ?: 0.15
                val minCashReserve = params["minCashReserve"] as? Int ?: 300
                ROIStrategy(minROI, minCashReserve)
            }
            "lowprice" -> {
                val maxPrice = params["maxPrice"] as? Int ?: 200
                val minCashReserve = params["minCashReserve"] as? Int ?: 200
                LowPriceStrategy(maxPrice, minCashReserve)
            }
            "highvalue" -> {
                val minRent = params["minRent"] as? Int ?: 20
                val minCashReserve = params["minCashReserve"] as? Int ?: 100
                HighValueStrategy(minRent, minCashReserve)
            }
            "balanced" -> {
                val threshold = params["threshold"] as? Int ?: 80
                val minCashReserve = params["minCashReserve"] as? Int ?: 400
                BalancedStrategy(threshold, minCashReserve)
            }
            "aggressive" -> {
                val minCashReserve = params["minCashReserve"] as? Int ?: 300
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
            // ... 他の戦略
            else -> emptyMap()
        }
    }
}
```

## StrategyParameterLoader

### JSON読み込み

```kotlin
object StrategyParameterLoader {
    /**
     * 設定ファイルを読み込む
     *
     * @param filePath 設定ファイルパス
     * @return StrategyConfig
     */
    fun loadFromFile(filePath: String): StrategyConfig {
        val json = File(filePath).readText()
        return parseJson(json)
    }

    /**
     * JSONをパース
     */
    private fun parseJson(json: String): StrategyConfig {
        // kotlinx.serialization または Gson を使用
        val jsonObject = Json.parseToJsonElement(json).jsonObject
        val strategies = mutableMapOf<String, Map<String, Any>>()

        jsonObject.forEach { (strategyId, paramsElement) ->
            val params = parseParameters(paramsElement.jsonObject)
            strategies[strategyId] = params
        }

        return StrategyConfig(strategies)
    }

    /**
     * パラメータをパース（型を適切に変換）
     */
    private fun parseParameters(jsonObject: JsonObject): Map<String, Any> {
        val params = mutableMapOf<String, Any>()

        jsonObject.forEach { (key, value) ->
            params[key] = when (value) {
                is JsonPrimitive -> {
                    when {
                        value.isString -> value.content
                        value.booleanOrNull != null -> value.boolean
                        value.intOrNull != null -> value.int
                        value.doubleOrNull != null -> value.double
                        else -> value.content
                    }
                }
                else -> value.toString()
            }
        }

        return params
    }

    /**
     * パラメータをバリデーション
     */
    fun validateParameters(strategyId: String, params: Map<String, Any>): Result<Unit> {
        return try {
            when (strategyId) {
                "monopoly" -> {
                    require(params["minCashReserve"] is Int?) { "minCashReserve must be Int" }
                    val minCash = params["minCashReserve"] as? Int ?: 300
                    require(minCash >= 0) { "minCashReserve must be >= 0" }
                }
                "roi" -> {
                    val minROI = (params["minROI"] as? Number)?.toDouble()
                    require(minROI == null || minROI in 0.0..1.0) { "minROI must be between 0.0 and 1.0" }
                }
                // ... 他の戦略のバリデーション
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## GridSearchRunner

### グリッドサーチ実行

```kotlin
class GridSearchRunner(
    private val parallelGameRunner: ParallelGameRunner,
) {
    /**
     * グリッドサーチを実行
     */
    suspend fun run(config: GridSearchConfig): GridSearchResult {
        val combinations = config.generateCombinations()
        println("Running grid search with ${combinations.size} parameter combinations...")
        println("Games per combination: ${config.gamesPerCombination}")
        println()

        val results = combinations.mapIndexed { index, params ->
            println("[${ index + 1}/${combinations.size}] Testing parameters: $params")
            runWithParameters(config, params)
        }

        val bestResult = results.maxByOrNull { it.winRate }!!

        return GridSearchResult(
            results = results,
            bestParameterSet = bestResult
        )
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
            val opponentStrategy = StrategyRegistry.getStrategyWithParams(
                opponentConfig.strategy,
                opponentConfig.parameters
            ) ?: throw IllegalArgumentException("Unknown strategy: ${opponentConfig.strategy}")

            Pair(opponentConfig.strategy, opponentStrategy)
        }

        // プレイヤー設定
        val playerStrategies = listOf(
            Pair("Target-${config.strategy}", targetStrategy)
        ) + opponents

        // ゲーム実行
        val board = Board.createStandardBoard()
        val multiGameResult = parallelGameRunner.runMultipleGames(
            numberOfGames = config.gamesPerCombination,
            playerStrategies = playerStrategies,
            board = board,
            showProgress = false
        )

        // 統計計算
        val calculator = StatisticsCalculator()
        val stats = calculator.calculate(multiGameResult)
        val targetPlayerStats = stats.playerStats["Target-${config.strategy}"]!!

        return GridSearchResult.ParameterSetResult(
            parameters = params,
            winRate = targetPlayerStats.winRate,
            avgFinalAssets = targetPlayerStats.averageFinalAssets,
            gamesPlayed = config.gamesPerCombination
        )
    }
}
```

### 組み合わせ生成（カルテシアン積）

```kotlin
fun GridSearchConfig.generateCombinations(): List<Map<String, Any>> {
    if (parameters.isEmpty()) {
        return listOf(emptyMap())
    }

    val keys = parameters.keys.toList()
    val valueLists = parameters.values.toList()

    return cartesianProduct(valueLists).map { values ->
        keys.zip(values).toMap()
    }
}

/**
 * カルテシアン積を計算
 */
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
```

## JSON処理

kotlinx.serialization を使用：

```kotlin
// build.gradle.kts に追加
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}
```

## CLI統合

### Main.kt拡張

```kotlin
// --config <file> オプション
val configIndex = args.indexOf("--config")
if (configIndex != -1 && configIndex + 1 < args.size) {
    val configFile = args[configIndex + 1]
    val config = StrategyParameterLoader.loadFromFile(configFile)
    // configを使用してゲーム実行
}

// --show-params <strategy> オプション
val showParamsIndex = args.indexOf("--show-params")
if (showParamsIndex != -1 && showParamsIndex + 1 < args.size) {
    val strategyId = args[showParamsIndex + 1]
    val defaultParams = StrategyRegistry.getDefaultParameters(strategyId)
    println("Default parameters for '$strategyId':")
    defaultParams.forEach { (key, value) ->
        println("  $key: $value")
    }
    exitProcess(0)
}

// --grid-search <file> オプション
val gridSearchIndex = args.indexOf("--grid-search")
if (gridSearchIndex != -1 && gridSearchIndex + 1 < args.size) {
    val gridSearchFile = args[gridSearchIndex + 1]
    val gridConfig = GridSearchConfig.fromJson(File(gridSearchFile).readText())
    val runner = GridSearchRunner(parallelGameRunner)
    val result = runBlocking { runner.run(gridConfig) }

    // 結果を表示・保存
    println("\nGrid Search Results:")
    println("Best parameters: ${result.bestParameterSet.parameters}")
    println("Win rate: ${result.bestParameterSet.winRate}")

    File("grid-search-results.csv").writeText(result.toCsv())
    println("\nResults saved to grid-search-results.csv")

    exitProcess(0)
}
```

## エラーハンドリング

- 設定ファイルが存在しない場合: FileNotFoundExceptionで明確なエラーメッセージ
- JSON形式が不正な場合: JsonExceptionで行番号を表示
- パラメータ型が不正な場合: バリデーションでエラー
- パラメータ範囲外の場合: バリデーションでエラー

## パフォーマンス考慮

- グリッドサーチは並列実行を活用
- 組み合わせ数が多い場合は警告表示
- 進捗表示で長時間実行を視覚化
