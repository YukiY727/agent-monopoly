plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    jacoco
}

group = "com.monopoly"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin標準ライブラリ
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // ロギング
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // HTML生成
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Ktor (Web Server)
    val ktorVersion = "2.3.12"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")

    // テスト - Kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-property:5.9.1")
    testImplementation("io.mockk:mockk:1.13.12")

    // アーキテクチャテスト - ArchUnit
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")

    // Detekt
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.monopoly.server.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

// ktlint設定
ktlint {
    version.set("1.0.1")
    android.set(false)
    ignoreFailures.set(false)
    // Exclude server package (web UI for manual testing)
    filter {
        exclude("**/server/**")
    }
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

// detekt設定
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    baseline = file("$projectDir/config/detekt/baseline.xml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    // Exclude server package (web UI for manual testing)
    exclude("**/server/**")

    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
}

// JaCoCo設定
jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/test/html"))
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        // Exclude generated code if any
                        "**/BuildConfig.*",
                        // Exclude CLI entry point (not testable in unit tests)
                        "**/cli/MainKt.*",
                        // Exclude server layer (web UI for manual testing)
                        "**/server/**",
                    )
                }
            },
        ),
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        // Exclude generated code if any
                        "**/BuildConfig.*",
                        // Exclude CLI entry point (not testable in unit tests)
                        "**/cli/MainKt.*",
                        // Exclude server layer (web UI for manual testing)
                        "**/server/**",
                    )
                }
            },
        ),
    )
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.95".toBigDecimal() // Domain layer: 95%以上を目標
            }
        }
        rule {
            element = "BUNDLE"
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal() // Branch: 90%以上を目標
            }
        }
        rule {
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.95".toBigDecimal() // Line: 95%以上を目標
            }
        }
    }
}

// Coverage summary task
tasks.register("coverageSummary") {
    group = "verification"
    description = "Print test coverage summary"
    dependsOn(tasks.jacocoTestReport)

    doLast {
        val reportFile = file("${layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.csv")
        if (reportFile.exists()) {
            val lines = reportFile.readLines()
            if (lines.size > 1) {
                // Aggregate all data lines (skip header)
                var instructionMissed = 0
                var instructionCovered = 0
                var branchMissed = 0
                var branchCovered = 0
                var lineMissed = 0
                var lineCovered = 0

                lines.drop(1).forEach { line ->
                    val cols = line.split(",")
                    if (cols.size >= 9) {
                        instructionMissed += cols[3].toIntOrNull() ?: 0
                        instructionCovered += cols[4].toIntOrNull() ?: 0
                        branchMissed += cols[5].toIntOrNull() ?: 0
                        branchCovered += cols[6].toIntOrNull() ?: 0
                        lineMissed += cols[7].toIntOrNull() ?: 0
                        lineCovered += cols[8].toIntOrNull() ?: 0
                    }
                }

                val instructionTotal = instructionMissed + instructionCovered
                val instructionCoverage =
                    if (instructionTotal > 0) {
                        (instructionCovered * 100.0 / instructionTotal)
                    } else {
                        0.0
                    }

                val branchTotal = branchMissed + branchCovered
                val branchCoverage =
                    if (branchTotal > 0) {
                        (branchCovered * 100.0 / branchTotal)
                    } else {
                        0.0
                    }

                val lineTotal = lineMissed + lineCovered
                val lineCoverage =
                    if (lineTotal > 0) {
                        (lineCovered * 100.0 / lineTotal)
                    } else {
                        0.0
                    }

                println("\n" + "=".repeat(60))
                println("📊 Test Coverage Summary")
                println("=".repeat(60))
                println("Instruction Coverage: %.2f%% (%d/%d)".format(instructionCoverage, instructionCovered, instructionTotal))
                println("Branch Coverage:      %.2f%% (%d/%d)".format(branchCoverage, branchCovered, branchTotal))
                println("Line Coverage:        %.2f%% (%d/%d)".format(lineCoverage, lineCovered, lineTotal))
                println("=".repeat(60))
                println("📄 HTML Report: file://${layout.buildDirectory.get()}/reports/jacoco/test/html/index.html")
                println("=".repeat(60) + "\n")
            }
        }
    }
}

// check task設定
tasks.check {
    dependsOn(tasks.ktlintCheck)
    dependsOn(tasks.detekt)
    dependsOn(tasks.jacocoTestCoverageVerification)
}
