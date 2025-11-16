package com.monopoly.statistics

/**
 * 詳細統計（Phase 6の基本統計を拡張）
 *
 * @property basicStats 基本統計（Phase 6）
 * @property propertyStatistics プロパティ別統計
 * @property assetHistory 資産推移
 * @property bankruptcyAnalysis 破産分析
 * @property timestamp タイムスタンプ
 */
data class DetailedStatistics(
    val basicStats: GameStatistics,
    val propertyStatistics: List<PropertyStatistics>,
    val assetHistory: AssetHistory,
    val bankruptcyAnalysis: BankruptcyAnalysis,
    val timestamp: Long = System.currentTimeMillis(),
)
