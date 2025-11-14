package com.monopoly.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import io.kotest.core.spec.style.StringSpec

class ArchitectureTest : StringSpec({
    val classes =
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.monopoly")

    // レイヤー依存関係ルール: Clean Architectureの依存方向
    "domain layer should not depend on cli layer" {
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .accessClassesThat()
            .resideInAPackage("..cli..")
            .check(classes)
    }

    // ドメイン層は外部ライブラリに依存しない（Kotlinの標準ライブラリのみ）
    "domain layer should only depend on domain and kotlin stdlib" {
        classes()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..domain..",
                "java..",
                "kotlin..",
                "kotlinx..",
                // Kotlinコンパイラが生成するアノテーション
                "org.jetbrains.annotations..",
            )
            .check(classes)
    }

    // レイヤードアーキテクチャの検証: ドメイン層はCLI層に依存しない
    "domain layer should not access CLI layer" {
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .accessClassesThat()
            .resideInAPackage("..cli..")
            .check(classes)
    }

    // モデル層はサービス層に依存しない
    "domain model should not depend on domain service" {
        noClasses()
            .that()
            .resideInAPackage("..domain.model..")
            .should()
            .accessClassesThat()
            .resideInAPackage("..domain.service..")
            .check(classes)
    }

    // サービス層はモデル層とストラテジー層に依存できる
    "domain service may depend on model and strategy" {
        classes()
            .that()
            .resideInAPackage("..domain.service..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..domain.model..",
                "..domain.strategy..",
                "..domain.service..",
                "java..",
                "kotlin..",
                // Kotlinコンパイラが生成するアノテーション
                "org.jetbrains.annotations..",
            )
            .check(classes)
    }

    // パッケージの循環依存を禁止
    "packages should be free of cycles" {
        slices()
            .matching("com.monopoly.(*)..")
            .should()
            .beFreeOfCycles()
            .check(classes)
    }

    // ドメインモデルクラスは適切なパッケージに配置される
    "domain model classes should be well organized" {
        classes()
            .that()
            .resideInAPackage("..domain.model..")
            .should()
            .onlyBeAccessed()
            .byAnyPackage("..domain..", "..cli..")
            .check(classes)
    }

    // Strategyインターフェースは戦略パターンに従う
    "classes implementing BuyStrategy should reside in strategy package" {
        classes()
            .that()
            .implement("com.monopoly.domain.strategy.BuyStrategy")
            .should()
            .resideInAPackage("..domain.strategy..")
            .check(classes)
    }

    // Value Classesはドメインモデル層に配置される
    "value classes should reside in domain model package" {
        classes()
            .that()
            .areAnnotatedWith("kotlin.jvm.JvmInline")
            .should()
            .resideInAPackage("..domain.model..")
            .check(classes)
    }

    // ドメインサービスはServiceで終わる
    "domain services should have Service suffix" {
        classes()
            .that()
            .resideInAPackage("..domain.service..")
            .and()
            .areNotNestedClasses()
            .should()
            .haveSimpleNameEndingWith("Service")
            .check(classes)
    }

    // Strategyクラスは適切に命名される
    "strategy implementations should have Strategy suffix" {
        classes()
            .that()
            .resideInAPackage("..domain.strategy..")
            .and()
            .areNotInterfaces()
            .should()
            .haveSimpleNameEndingWith("Strategy")
            .check(classes)
    }
})
