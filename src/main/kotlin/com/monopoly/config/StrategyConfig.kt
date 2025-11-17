package com.monopoly.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
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
