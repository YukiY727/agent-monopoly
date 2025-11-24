package com.monopoly.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption

/**
 * ArchUnitテストの共通基盤
 *
 * ClassFileImporterの初期化コストを削減するため、
 * クラスインポートを1回だけ実行し、全テストで共有する。
 */
object ArchUnitTestBase {
    /**
     * テスト対象のJavaクラス群（遅延初期化）
     *
     * 初回アクセス時に1回だけクラスをインポートし、
     * 以降は同じインスタンスを再利用する。
     */
    val classes: JavaClasses by lazy {
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.monopoly")
    }
}
