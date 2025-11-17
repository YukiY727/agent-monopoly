package com.monopoly.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
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
