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
