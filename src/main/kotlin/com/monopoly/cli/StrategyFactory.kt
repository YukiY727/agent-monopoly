package com.monopoly.cli

import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.strategy.AggressiveStrategy
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import com.monopoly.domain.strategy.BalancedStrategy
import com.monopoly.domain.strategy.ConservativeStrategy
import com.monopoly.domain.strategy.ROIStrategy
import com.monopoly.domain.strategy.RandomStrategy
import com.monopoly.domain.strategy.SetFocusedStrategy

/**
 * 戦略名から戦略インスタンスを生成するファクトリ
 */
object StrategyFactory {
    /**
     * 利用可能な戦略名のリスト
     */
    val availableStrategies: List<String> =
        listOf(
            "always",
            "random",
            "conservative",
            "aggressive",
            "setfocused",
            "roi",
            "balanced",
        )

    /**
     * 戦略名から戦略インスタンスを生成
     *
     * @param name 戦略名（大文字小文字を区別しない）
     * @return 対応する戦略インスタンス
     * @throws IllegalArgumentException 不明な戦略名の場合
     */
    fun create(name: String): PlayerStrategy =
        when (name.lowercase()) {
            "always" -> AlwaysPlayerStrategy()
            "random" -> RandomStrategy()
            "conservative" -> ConservativeStrategy()
            "aggressive" -> AggressiveStrategy()
            "setfocused" -> SetFocusedStrategy()
            "roi" -> ROIStrategy()
            "balanced" -> BalancedStrategy()
            else -> throw IllegalArgumentException(
                "Unknown strategy: '$name'. Available strategies: ${availableStrategies.joinToString(", ")}",
            )
        }

    /**
     * 複数の戦略名から戦略インスタンスのリストを生成
     *
     * @param names カンマ区切りの戦略名
     * @return 戦略インスタンスのリスト
     */
    fun createMultiple(names: String): List<PlayerStrategy> =
        names
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { create(it) }

    /**
     * 戦略の説明を取得
     */
    fun getDescription(name: String): String =
        when (name.lowercase()) {
            "always" -> "Always purchases properties and builds when possible"
            "random" -> "Makes random decisions"
            "conservative" -> "Keeps cash reserves, avoids risky investments"
            "aggressive" -> "Invests aggressively, prioritizes monopolies"
            "setfocused" -> "Prioritizes completing color sets"
            "roi" -> "Calculates ROI before investment decisions"
            "balanced" -> "Balances cash reserves and investments"
            else -> "Unknown strategy"
        }
}
