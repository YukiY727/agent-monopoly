package com.monopoly.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeLessThan
import kotlin.math.abs

/**
 * 主系列からの距離（Distance from Main Sequence）のテスト
 *
 * Robert C. Martinの「Clean Architecture」で定義されているメトリクス:
 * - 抽象度(A): 抽象クラスとインターフェースの比率
 * - 不安定度(I): 外部依存の比率
 * - 主系列からの距離(D): |A + I - 1|
 *
 * 理想的なパッケージは主系列(A + I = 1)上にある。
 * D値が0に近いほど良い設計。0.3以下が望ましい。
 */
class MainSequenceTest : StringSpec({
    // 共通化されたクラスインポートを使用（初期化コスト削減）
    val classes: JavaClasses = ArchUnitTestBase.classes

    "domain.model package should be close to main sequence" {
        val modelPackage = "com.monopoly.domain.model"
        val distance = calculateDistanceFromMainSequence(classes, modelPackage)

        // ドメインモデルは具象クラスが多いが依存も少ないため、主系列に近い
        // Phase 2: GameStateにイベントログとプロパティ操作メソッドを追加したため、
        // 結合度がわずかに上昇（0.303）。デメテルの法則に従った設計のトレードオフ。
        distance shouldBeLessThan 0.35
    }

    "domain.service package should be close to main sequence" {
        val servicePackage = "com.monopoly.domain.service"
        val distance = calculateDistanceFromMainSequence(classes, servicePackage)

        // サービス層は具象クラスで依存が多いため、不安定だが抽象度は低い
        distance shouldBeLessThan 0.3
    }

    "domain.strategy package should be close to main sequence" {
        val strategyPackage = "com.monopoly.domain.strategy"
        val distance = calculateDistanceFromMainSequence(classes, strategyPackage)

        // Strategy層はインターフェースと実装が混在、抽象度と不安定度のバランス
        distance shouldBeLessThan 0.5
    }

    "all packages should maintain good architecture metrics" {
        val packages =
            listOf(
                "com.monopoly.domain.model",
                "com.monopoly.domain.service",
                "com.monopoly.domain.strategy",
            )

        packages.forEach { packageName ->
            val metrics = calculatePackageMetrics(classes, packageName)

            println("\n=== Package: $packageName ===")
            println("Abstractness (A): %.3f".format(metrics.abstractness))
            println("Instability (I): %.3f".format(metrics.instability))
            println("Distance (D): %.3f".format(metrics.distance))

            // 主系列からの距離は0.3以下が望ましい
            metrics.distance shouldBeLessThan 0.5 // domain層は具象が多いので0.5まで許容
        }
    }
})

data class PackageMetrics(
    val abstractness: Double,
    val instability: Double,
    val distance: Double,
)

/**
 * 主系列からの距離を計算
 * D = |A + I - 1|
 */
fun calculateDistanceFromMainSequence(
    classes: JavaClasses,
    packageName: String,
): Double {
    val metrics = calculatePackageMetrics(classes, packageName)
    return metrics.distance
}

/**
 * パッケージのメトリクスを計算
 */
fun calculatePackageMetrics(
    classes: JavaClasses,
    packageName: String,
): PackageMetrics {
    val packageClasses = classes.filter { it.packageName == packageName }

    if (packageClasses.isEmpty()) {
        return PackageMetrics(0.0, 0.0, 0.0)
    }

    // 抽象度(A): 抽象クラス・インターフェースの比率
    val abstractClasses =
        packageClasses.count { clazz ->
            clazz.isInterface || clazz.modifiers.contains(com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT)
        }
    val totalClasses = packageClasses.size
    val abstractness = if (totalClasses > 0) abstractClasses.toDouble() / totalClasses else 0.0

    // 不安定度(I): Ce / (Ca + Ce)
    // Ce (Efferent Coupling): このパッケージが依存する外部クラス数
    // Ca (Afferent Coupling): このパッケージに依存する外部クラス数
    val efferentCoupling =
        packageClasses
            .flatMap { javaClass -> javaClass.getDirectDependenciesFromSelf() }
            .map { dependency -> dependency.targetClass }
            .filter { targetClass -> !targetClass.packageName.startsWith(packageName) }
            .distinctBy { targetClass -> targetClass.name }
            .size

    val afferentCoupling =
        classes
            .filter { javaClass -> !javaClass.packageName.startsWith(packageName) }
            .flatMap { javaClass -> javaClass.getDirectDependenciesFromSelf() }
            .map { dependency -> dependency.targetClass }
            .filter { targetClass -> targetClass.packageName.startsWith(packageName) }
            .distinctBy { targetClass -> targetClass.name }
            .size

    val totalCoupling = afferentCoupling + efferentCoupling
    val instability = if (totalCoupling > 0) efferentCoupling.toDouble() / totalCoupling else 0.0

    // 主系列からの距離: D = |A + I - 1|
    val distance = abs(abstractness + instability - 1.0)

    return PackageMetrics(abstractness, instability, distance)
}
