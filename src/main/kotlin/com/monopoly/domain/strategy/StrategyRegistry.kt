package com.monopoly.domain.strategy

/**
 * 戦略のメタデータ
 */
data class StrategyMetadata(
    /** 戦略ID（コマンドライン引数で使用） */
    val id: String,

    /** 戦略の表示名 */
    val displayName: String,

    /** 戦略の説明（1行） */
    val description: String,

    /** 戦略の詳細説明 */
    val details: String,

    /** 戦略インスタンスを生成するファクトリ */
    val factory: () -> BuyStrategy,
)

/**
 * 全戦略の登録と管理
 */
object StrategyRegistry {
    private val strategies = LinkedHashMap<String, StrategyMetadata>()

    init {
        // 既存戦略の登録
        register(
            StrategyMetadata(
                id = "always",
                displayName = "Always Buy",
                description = "常に購入する",
                details = """
                    購入可能なプロパティは必ず購入します。
                    最もシンプルな戦略で、ベースライン比較に使用されます。
                """.trimIndent(),
                factory = { AlwaysBuyStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "random",
                displayName = "Random",
                description = "ランダムに購入",
                details = """
                    50%の確率で購入します。
                    確率的な振る舞いの検証に使用されます。
                """.trimIndent(),
                factory = { RandomStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "conservative",
                displayName = "Conservative",
                description = "保守的（一定額以上の現金を保持）",
                details = """
                    購入後の所持金が$500以上になる場合のみプロパティを購入します。
                    破産リスクを抑えた安全志向の戦略です。
                """.trimIndent(),
                factory = { ConservativeStrategy() }
            )
        )

        // 新しい高度な戦略の登録
        register(
            StrategyMetadata(
                id = "monopoly",
                displayName = "Monopoly First",
                description = "カラーグループの独占を優先",
                details = """
                    既に所有しているカラーグループのプロパティを優先的に購入し、
                    独占を完成させることを目指します。
                    また、他プレイヤーの独占を阻止することも考慮します。

                    パラメータ:
                    - blockOpponentMonopoly: 相手の独占を阻止するか（デフォルト: true）
                    - minCashReserve: 最低現金残高（デフォルト: $300）
                """.trimIndent(),
                factory = { MonopolyFirstStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "roi",
                displayName = "ROI",
                description = "投資収益率を重視",
                details = """
                    プロパティのROI（基本レント ÷ 価格）を計算し、
                    15%以上のROIがあるプロパティを購入します。
                    長期的な収益性を重視した戦略です。

                    パラメータ:
                    - minROI: 最低ROI閾値（デフォルト: 0.15 = 15%）
                    - minCashReserve: 最低現金残高（デフォルト: $300）
                """.trimIndent(),
                factory = { ROIStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "lowprice",
                displayName = "Low Price",
                description = "安いプロパティを優先",
                details = """
                    $200以下のプロパティを優先的に購入します。
                    早期に多くのプロパティを確保し、レント収入を増やすことを目指します。

                    パラメータ:
                    - maxPrice: 購入する最大価格（デフォルト: $200）
                    - minCashReserve: 最低現金残高（デフォルト: $200）
                """.trimIndent(),
                factory = { LowPriceStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "highvalue",
                displayName = "High Value",
                description = "高収益プロパティを優先",
                details = """
                    レントが$20以上のプロパティを優先的に購入します。
                    高価でも収益性が高ければ購入する、リスクを取った戦略です。

                    パラメータ:
                    - minRent: 購入する最低レント額（デフォルト: $20）
                    - minCashReserve: 最低現金残高（デフォルト: $100）
                """.trimIndent(),
                factory = { HighValueStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "balanced",
                displayName = "Balanced",
                description = "バランス型",
                details = """
                    カラーグループ、ROI、価格、レントを総合的に判断します。
                    複数の要因をスコアリングし、バランスの取れた購入判断を行います。

                    パラメータ:
                    - threshold: 購入スコア閾値（デフォルト: 80）
                    - minCashReserve: 最低現金残高（デフォルト: $400）
                """.trimIndent(),
                factory = { BalancedStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "aggressive",
                displayName = "Aggressive",
                description = "積極的（相手の独占を阻止）",
                details = """
                    他のプレイヤーのカラーグループ独占を阻止することを優先します。
                    相手が2つ所有しているカラーグループのプロパティは必ず購入を検討します。

                    パラメータ:
                    - minCashReserve: 最低現金残高（デフォルト: $300）
                """.trimIndent(),
                factory = { AggressiveStrategy() }
            )
        )
    }

    fun register(metadata: StrategyMetadata) {
        strategies[metadata.id] = metadata
    }

    fun getStrategy(id: String): BuyStrategy? {
        return strategies[id]?.factory?.invoke()
    }

    fun getMetadata(id: String): StrategyMetadata? {
        return strategies[id]
    }

    fun listAll(): List<StrategyMetadata> {
        return strategies.values.toList()
    }
}
