package com.monopoly.domain.experiment

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 信頼区間
 */
data class ConfidenceInterval(
    val lower: Double,
    val upper: Double,
    val point: Double,
    val confidenceLevel: Double,
)

/**
 * 仮説検定の結果
 */
data class HypothesisTestResult(
    val zScore: Double,
    val pValue: Double,
    val significant: Boolean,
    val alpha: Double = 0.05,
)

/**
 * 支配関係の分析結果
 */
data class DominanceAnalysis(
    /** 厳密に支配する戦略（全ての他戦略に勝つ） */
    val strictlyDominant: String?,
    /** 弱く支配する戦略（全ての他戦略に負けない） */
    val weaklyDominant: List<String>,
    /** 厳密に支配される戦略（全ての他戦略に負ける） */
    val strictlyDominated: List<String>,
    /** 弱く支配される戦略（全ての他戦略に勝てない） */
    val weaklyDominated: List<String>,
)

/**
 * 統計分析ユーティリティ
 *
 * 勝率の統計的検定と効果量計算を提供
 */
object StatisticalAnalysis {
    /** 有意水準（デフォルト: 5%） */
    private const val DEFAULT_ALPHA = 0.05

    /** 勝率の閾値（50%以上で勝ち） */
    private const val WIN_THRESHOLD = 0.5

    /**
     * 勝率の信頼区間を計算（Wald法）
     *
     * @param wins 勝利数
     * @param total 総対戦数
     * @param confidenceLevel 信頼水準（デフォルト: 0.95）
     * @return 信頼区間
     */
    fun confidenceInterval(
        wins: Int,
        total: Int,
        confidenceLevel: Double = 0.95,
    ): ConfidenceInterval {
        val p: Double = wins.toDouble() / total
        val z: Double = getZScore(confidenceLevel)
        val se: Double = sqrt(p * (1 - p) / total)
        val margin: Double = z * se

        return ConfidenceInterval(
            lower = (p - margin).coerceIn(0.0, 1.0),
            upper = (p + margin).coerceIn(0.0, 1.0),
            point = p,
            confidenceLevel = confidenceLevel,
        )
    }

    /**
     * Cohen's d効果量を計算
     *
     * 2つの勝率の差の大きさを標準化した指標
     * - 0.2: 小さな効果
     * - 0.5: 中程度の効果
     * - 0.8: 大きな効果
     *
     * @param winRate1 戦略1の勝率
     * @param winRate2 戦略2の勝率
     * @param sampleSize サンプルサイズ
     * @return Cohen's d
     */
    fun cohensD(
        winRate1: Double,
        winRate2: Double,
        sampleSize: Int,
    ): Double {
        // 勝率の差
        val diff: Double = winRate1 - winRate2

        // プールされた標準偏差（二項分布の場合）
        val pooledP: Double = (winRate1 + winRate2) / 2
        val pooledSD: Double = sqrt(pooledP * (1 - pooledP))

        return if (pooledSD > 0) abs(diff) / pooledSD else 0.0
    }

    /**
     * 二つの比率のZ検定
     *
     * 2つの勝率に統計的有意差があるかを検定
     *
     * @param wins1 戦略1の勝利数
     * @param total1 戦略1の総対戦数
     * @param wins2 戦略2の勝利数
     * @param total2 戦略2の総対戦数
     * @param alpha 有意水準（デフォルト: 0.05）
     * @return 検定結果
     */
    fun twoProportionZTest(
        wins1: Int,
        total1: Int,
        wins2: Int,
        total2: Int,
        alpha: Double = DEFAULT_ALPHA,
    ): HypothesisTestResult {
        val p1: Double = wins1.toDouble() / total1
        val p2: Double = wins2.toDouble() / total2

        // プールされた比率
        val pooledP: Double = (wins1 + wins2).toDouble() / (total1 + total2)

        // 標準誤差
        val se: Double = sqrt(pooledP * (1 - pooledP) * (1.0 / total1 + 1.0 / total2))

        // Z統計量
        val zScore: Double = if (se > 0) (p1 - p2) / se else 0.0

        // p値（両側検定）
        val pValue: Double = 2 * (1 - normalCDF(abs(zScore)))

        return HypothesisTestResult(
            zScore = zScore,
            pValue = pValue,
            significant = pValue < alpha,
            alpha = alpha,
        )
    }

    /**
     * Bonferroni補正
     *
     * 多重比較時のp値を補正
     *
     * @param pValues 元のp値リスト
     * @param numComparisons 比較の総数
     * @return 補正後のp値リスト
     */
    fun bonferroniCorrection(
        pValues: List<Double>,
        numComparisons: Int,
    ): List<Double> = pValues.map { (it * numComparisons).coerceAtMost(1.0) }

    /**
     * 支配関係を分析
     *
     * @param matrix 対戦マトリックス
     * @return 支配関係の分析結果
     */
    fun analyzeDominance(matrix: ComparisonMatrix): DominanceAnalysis {
        val strategies: List<String> = matrix.strategies

        // 各戦略の勝率を集計
        val winRates: Map<String, Map<String, Double>> =
            strategies.associateWith { strategy ->
                strategies
                    .filter { it != strategy }
                    .associateWith { opponent ->
                        matrix.getWinRate(strategy, opponent) ?: 0.5
                    }
            }

        // 厳密支配: 全ての他戦略に勝つ（勝率 > 0.5）
        val strictlyDominant: String? =
            strategies.find { strategy ->
                winRates[strategy]?.values?.all { it > WIN_THRESHOLD } == true
            }

        // 弱支配: 全ての他戦略に負けない（勝率 >= 0.5）
        val weaklyDominant: List<String> =
            strategies.filter { strategy ->
                winRates[strategy]?.values?.all { it >= WIN_THRESHOLD } == true
            }

        // 厳密被支配: 全ての他戦略に負ける（勝率 < 0.5）
        val strictlyDominated: List<String> =
            strategies.filter { strategy ->
                winRates[strategy]?.values?.all { it < WIN_THRESHOLD } == true
            }

        // 弱被支配: 全ての他戦略に勝てない（勝率 <= 0.5）
        val weaklyDominated: List<String> =
            strategies.filter { strategy ->
                winRates[strategy]?.values?.all { it <= WIN_THRESHOLD } == true
            }

        return DominanceAnalysis(
            strictlyDominant = strictlyDominant,
            weaklyDominant = weaklyDominant,
            strictlyDominated = strictlyDominated,
            weaklyDominated = weaklyDominated,
        )
    }

    /**
     * 信頼水準に対応するZ値を取得
     */
    private fun getZScore(confidenceLevel: Double): Double =
        when {
            confidenceLevel >= 0.99 -> 2.576
            confidenceLevel >= 0.95 -> 1.96
            confidenceLevel >= 0.90 -> 1.645
            else -> 1.96
        }

    /**
     * 標準正規分布の累積分布関数（近似）
     *
     * Abramowitz and Stegunの近似式を使用
     */
    private fun normalCDF(z: Double): Double {
        val a1 = 0.254829592
        val a2 = -0.284496736
        val a3 = 1.421413741
        val a4 = -1.453152027
        val a5 = 1.061405429
        val p = 0.3275911

        val sign: Int = if (z < 0) -1 else 1
        val absZ: Double = abs(z) / sqrt(2.0)

        val t: Double = 1.0 / (1.0 + p * absZ)
        val y: Double = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-absZ * absZ)

        return 0.5 * (1.0 + sign * y)
    }
}
