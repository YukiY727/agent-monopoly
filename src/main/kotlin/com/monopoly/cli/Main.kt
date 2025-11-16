package com.monopoly.cli

import com.monopoly.config.GridSearchConfig
import com.monopoly.config.StrategyConfig
import com.monopoly.config.StrategyParameterLoader
import com.monopoly.domain.model.Board
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.Space
import com.monopoly.domain.model.SpaceType
import com.monopoly.domain.service.GameService
import com.monopoly.domain.strategy.BuyStrategy
import com.monopoly.domain.strategy.StrategyRegistry
import com.monopoly.export.CsvExporter
import com.monopoly.export.JsonExporter
import com.monopoly.simulation.GameRunner
import com.monopoly.simulation.GridSearchRunner
import com.monopoly.simulation.MultiGameResult
import com.monopoly.simulation.ParallelGameRunner
import com.monopoly.statistics.BoardStatisticsCalculator
import com.monopoly.statistics.DetailedStatisticsCalculator
import com.monopoly.statistics.StatisticsCalculator
import com.monopoly.visualization.ResearchReportGenerator
import com.monopoly.visualization.StatisticsReportGenerator
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess

@Suppress("MagicNumber")
fun createSpaceAt(position: Int): Space =
    when (position) {
        0 -> Space.Go(0)
        1 ->
            Space.PropertySpace(
                position = 1,
                property = Property("Mediterranean Avenue", 1, 60, 2, ColorGroup.BROWN),
            )
        3 ->
            Space.PropertySpace(
                position = 3,
                property = Property("Baltic Avenue", 3, 60, 4, ColorGroup.BROWN),
            )
        6 ->
            Space.PropertySpace(
                position = 6,
                property = Property("Oriental Avenue", 6, 100, 6, ColorGroup.LIGHT_BLUE),
            )
        8 ->
            Space.PropertySpace(
                position = 8,
                property = Property("Vermont Avenue", 8, 100, 6, ColorGroup.LIGHT_BLUE),
            )
        9 ->
            Space.PropertySpace(
                position = 9,
                property = Property("Connecticut Avenue", 9, 120, 8, ColorGroup.LIGHT_BLUE),
            )
        else -> Space.Other(position, SpaceType.CHANCE)
    }

@Suppress("MagicNumber")
fun createStandardBoard(): Board {
    val spaces: List<Space> = (0..39).map { createSpaceAt(it) }
    return Board(spaces)
}

/**
 * ゲーム設定
 */
data class GameConfig(
    val strategy: BuyStrategy,
    val numberOfGames: Int = 1,
    val generateReport: Boolean = true,
    val exportJson: Boolean = true,
    val exportCsv: Boolean = true,
    val generateVisualization: Boolean = true,
    val parallel: Int? = null,
    val sequential: Boolean = false,
    val generateResearchReport: Boolean = false,
    val generatePdf: Boolean = false,
)

fun createStrategy(strategyName: String, config: StrategyConfig? = null): BuyStrategy {
    // 設定ファイルからパラメータを取得
    val params = config?.getParameters(strategyName) ?: emptyMap()

    // パラメータ付きで戦略を生成
    val strategy = if (params.isNotEmpty()) {
        StrategyRegistry.getStrategyWithParams(strategyName, params)
    } else {
        StrategyRegistry.getStrategy(strategyName)
    }

    if (strategy != null) {
        return strategy
    }

    println("Error: Unknown strategy '$strategyName'")
    println()
    println("Available strategies:")
    StrategyRegistry.listAll().forEach { metadata ->
        println("  ${metadata.id.padEnd(15)} - ${metadata.description}")
    }
    println()
    println("Use --list-strategies for detailed information")
    println("Use --strategy-info <id> for strategy details")
    exitProcess(1)
}

fun parseArgs(args: Array<String>, strategyConfig: StrategyConfig? = null): GameConfig {
    if (args.contains("--help")) {
        printHelp()
        exitProcess(0)
    }

    var strategyName = "always"
    var numberOfGames = 1
    var generateReport = true
    var exportJson = true
    var exportCsv = true
    var generateVisualization = true
    var parallel: Int? = null
    var sequential = false
    var generateResearchReport = false
    var generatePdf = false

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--strategy" -> {
                if (i + 1 < args.size) {
                    strategyName = args[i + 1]
                    i++
                }
            }
            "--games" -> {
                if (i + 1 < args.size) {
                    numberOfGames = args[i + 1].toIntOrNull() ?: run {
                        println("Error: --games requires a valid integer")
                        exitProcess(1)
                    }
                    if (numberOfGames <= 0) {
                        println("Error: --games must be a positive integer")
                        exitProcess(1)
                    }
                    i++
                }
            }
            "--parallel" -> {
                if (i + 1 < args.size) {
                    parallel = args[i + 1].toIntOrNull() ?: run {
                        println("Error: --parallel requires a valid integer")
                        exitProcess(1)
                    }
                    if (parallel!! <= 0) {
                        println("Error: --parallel must be a positive integer")
                        exitProcess(1)
                    }
                    i++
                }
            }
            "--sequential" -> {
                sequential = true
            }
            "--no-report" -> {
                generateReport = false
            }
            "--export-json" -> {
                exportJson = true
                exportCsv = false
            }
            "--export-csv" -> {
                exportJson = false
                exportCsv = true
            }
            "--no-export" -> {
                exportJson = false
                exportCsv = false
            }
            "--no-visualize" -> {
                generateVisualization = false
            }
            "--research-report" -> {
                generateResearchReport = true
            }
            "--pdf" -> {
                generatePdf = true
            }
        }
        i++
    }

    val strategy = createStrategy(strategyName, strategyConfig)
    return GameConfig(strategy, numberOfGames, generateReport, exportJson, exportCsv, generateVisualization, parallel, sequential, generateResearchReport, generatePdf)
}

fun printHelp() {
    println(
        """
        Monopoly Game Simulator - Phase 8

        Usage: ./gradlew run --args="[options]"

        Options:
          --strategy <name>  戦略を指定
                             always-buy: 常に購入する（Phase 1の戦略）
                             random: ランダムに購入する
                             conservative: 一定額以上の現金を保持
                             デフォルト: always-buy
          --games <N>        実行するゲーム数を指定（デフォルト: 1）
          --parallel <N>     並列度を指定（デフォルト: CPUコア数）
          --sequential       逐次実行を強制（デバッグ用）
          --no-report        HTMLレポート生成を抑制
          --export-json      JSON形式のみでエクスポート
          --export-csv       CSV形式のみでエクスポート
          --no-export        統計エクスポートを抑制
          --no-visualize     統計可視化レポート生成を抑制
          --research-report  研究論文用レポートを生成（Phase 12）
          --pdf              研究レポートをPDFでも出力（--research-report必須）
          --help             ヘルプを表示

        Examples:
          # 単一ゲーム実行
          ./gradlew run --args="--strategy always-buy"

          # 100ゲーム実行（並列実行、デフォルト並列度）
          ./gradlew run --args="--strategy random --games 100"

          # 並列度を指定して実行
          ./gradlew run --args="--strategy random --games 1000 --parallel 4"

          # 逐次実行（デバッグ用）
          ./gradlew run --args="--strategy random --games 100 --sequential"
        """.trimIndent(),
    )
}

@Suppress("MagicNumber")
fun main(args: Array<String>) = runBlocking {
    // --list-strategies オプション
    if (args.contains("--list-strategies")) {
        println("Available Strategies:")
        println()
        StrategyRegistry.listAll().forEachIndexed { index, metadata ->
            println("${(index + 1).toString().padStart(2)}. ${metadata.id.padEnd(15)} - ${metadata.displayName}")
            println("    ${metadata.description}")
            println()
        }
        println("Use --strategy-info <id> for detailed information about a specific strategy")
        exitProcess(0)
    }

    // --strategy-info <id> オプション
    val strategyInfoIndex = args.indexOf("--strategy-info")
    if (strategyInfoIndex != -1 && strategyInfoIndex + 1 < args.size) {
        val strategyId = args[strategyInfoIndex + 1]
        val metadata = StrategyRegistry.getMetadata(strategyId)
        if (metadata != null) {
            println("Strategy: ${metadata.displayName} (${metadata.id})")
            println("=".repeat(60))
            println()
            println("Description:")
            println("  ${metadata.description}")
            println()
            println("Details:")
            metadata.details.lines().forEach { line ->
                println("  $line")
            }
        } else {
            println("Error: Strategy '$strategyId' not found.")
            println()
            println("Available strategies:")
            StrategyRegistry.listAll().forEach {
                println("  - ${it.id}")
            }
            println()
            println("Use --list-strategies for more information")
        }
        exitProcess(0)
    }

    // --show-params <strategy> オプション
    val showParamsIndex = args.indexOf("--show-params")
    if (showParamsIndex != -1 && showParamsIndex + 1 < args.size) {
        val strategyId = args[showParamsIndex + 1]
        val defaultParams = StrategyRegistry.getDefaultParameters(strategyId)
        if (defaultParams.isEmpty()) {
            println("Strategy '$strategyId' has no parameters")
        } else {
            println("Default parameters for '$strategyId':")
            defaultParams.forEach { (key, value) ->
                println("  $key: $value")
            }
        }
        exitProcess(0)
    }

    // --config <file> オプション
    var strategyConfig: StrategyConfig? = null
    val configIndex = args.indexOf("--config")
    if (configIndex != -1 && configIndex + 1 < args.size) {
        val configFile = args[configIndex + 1]
        try {
            strategyConfig = StrategyParameterLoader.loadFromFile(configFile)
            println("Loaded configuration from: $configFile")
        } catch (e: Exception) {
            println("Error loading config file: ${e.message}")
            exitProcess(1)
        }
    }

    // --grid-search <file> オプション
    val gridSearchIndex = args.indexOf("--grid-search")
    if (gridSearchIndex != -1 && gridSearchIndex + 1 < args.size) {
        val gridSearchFile = args[gridSearchIndex + 1]
        try {
            val gridConfig = GridSearchConfig.fromJson(File(gridSearchFile).readText())
            val board = createStandardBoard()
            val gameService = GameService()
            val dice = Dice()
            val parallelGameRunner = ParallelGameRunner(gameService, dice)
            val runner = GridSearchRunner(parallelGameRunner, board)

            val result = runner.run(gridConfig)

            // 結果を保存
            result.saveToFile("grid-search-results.csv")
            println("\nResults saved to grid-search-results.csv")
        } catch (e: Exception) {
            println("Error running grid search: ${e.message}")
            e.printStackTrace()
            exitProcess(1)
        }
        exitProcess(0)
    }

    val config: GameConfig = parseArgs(args, strategyConfig)

    if (config.numberOfGames == 1) {
        runSingleGame(config)
    } else {
        runMultipleGames(config)
    }
}

/**
 * 単一ゲームを実行（Phase 4までの動作）
 */
@Suppress("MagicNumber")
private fun runSingleGame(config: GameConfig) {
    val strategy1: BuyStrategy = config.strategy
    val strategy2: BuyStrategy = config.strategy

    println("=".repeat(60))
    println("Monopoly Game - Phase 8 (Single Game)")
    println("=".repeat(60))
    println()

    // プレイヤーの作成
    val player1: Player = Player("Alice", strategy1)
    val player2: Player = Player("Bob", strategy2)
    println("Players:")
    println("  - ${player1.name}")
    println("  - ${player2.name}")
    println()

    // ゲームの初期化
    val board: Board = createStandardBoard()
    val gameState =
        GameState(
            players = listOf(player1, player2),
            board = board,
        )
    val dice = Dice()
    val gameService = GameService()
    val consoleLogger = ConsoleLogger()
    val htmlReportGenerator = HtmlReportGenerator()
    val summaryReportGenerator = SummaryReportGenerator()

    println("Starting game...")
    println()

    // ゲームの実行
    val winner = gameService.runGame(gameState, dice)

    // イベントログの表示
    println()
    println("=".repeat(60))
    println("Game Events:")
    println("=".repeat(60))
    consoleLogger.logEvents(gameState.events)

    // 結果の表示
    println()
    println("=".repeat(60))
    println("Game Over!")
    println("=".repeat(60))
    println()
    println("Winner: ${winner.name}")
    println("Final Money: \$${winner.money}")
    println("Properties Owned: ${winner.ownedProperties.size}")
    println("Total Assets: \$${winner.getTotalAssets()}")
    println()
    println("Game Statistics:")
    println("  - Total Turns: ${gameState.turnNumber}")
    println("  - Active Players: ${gameState.getActivePlayerCount()}")
    println("  - Total Events: ${gameState.events.size}")
    println()

    // 全プレイヤーの最終状態
    println("Final Player Status:")
    gameState.players.forEach { player ->
        val status: String = if (player.isBankrupt) "BANKRUPT" else "ACTIVE"
        val playerInfo: String =
            "  - ${player.name}: $status | " +
                "Money: \$${player.money} | " +
                "Properties: ${player.ownedProperties.size} | " +
                "Total Assets: \$${player.getTotalAssets()}"
        println(playerInfo)
    }
    println()

    if (config.generateReport) {
        // HTMLレポートの生成（Phase 2 - 詳細レポート）
        val detailedReportFile = htmlReportGenerator.saveToFile(gameState)
        println("Detailed report generated: ${detailedReportFile.absolutePath}")

        // サマリーレポートの生成（Phase 4 - サマリーレポート）
        val summaryReportFile = summaryReportGenerator.saveToFile(gameState)
        println("Summary report generated: ${summaryReportFile.absolutePath}")
    }

    println()
    println("=".repeat(60))
}

/**
 * 複数ゲームを実行（Phase 5の新機能、Phase 8で並列化）
 */
@Suppress("MagicNumber")
private suspend fun runMultipleGames(config: GameConfig) {
    val executionMode = if (config.sequential) "Sequential" else "Parallel"
    println("=".repeat(60))
    println("Monopoly Game - Phase 8 (Multiple Games - $executionMode)")
    println("Games: ${config.numberOfGames}")
    if (!config.sequential) {
        val parallelism = config.parallel ?: Runtime.getRuntime().availableProcessors()
        println("Parallelism: $parallelism")
    }
    println("=".repeat(60))
    println()

    val board: Board = createStandardBoard()
    val playerStrategies = listOf(
        "Alice" to config.strategy,
        "Bob" to config.strategy,
    )

    val gameService = GameService()

    // 並列実行 or 逐次実行
    val result = if (config.sequential) {
        // 逐次実行（デバッグ用）
        val dice = Dice()
        val gameRunner = GameRunner(gameService, dice)
        gameRunner.runMultipleGames(
            numberOfGames = config.numberOfGames,
            playerStrategies = playerStrategies,
            board = board,
            showProgress = true,
        )
    } else {
        // 並列実行（Phase 8の新機能）
        val parallelism = config.parallel ?: Runtime.getRuntime().availableProcessors()
        val parallelGameRunner = ParallelGameRunner(gameService, parallelism)
        parallelGameRunner.runMultipleGames(
            numberOfGames = config.numberOfGames,
            playerStrategies = playerStrategies,
            board = board,
            showProgress = true,
        )
    }

    // 結果サマリー表示
    val summaryPrinter = ResultSummaryPrinter()
    summaryPrinter.print(result)

    // 統計エクスポート（Phase 6の新機能）
    if (config.numberOfGames > 1) {
        exportStatistics(result, config)
    }

    // オプション: 最後のゲームのみレポート生成
    if (config.generateReport) {
        val lastGameState = result.gameResults.last().finalState
        val summaryReportGenerator = SummaryReportGenerator()
        val reportFile = summaryReportGenerator.saveToFile(lastGameState)
        println("Last game summary report: ${reportFile.absolutePath}")
        println()
    }
}

/**
 * 統計データをエクスポート
 */
private fun exportStatistics(result: MultiGameResult, config: GameConfig) {
    // 統計計算
    val calculator = StatisticsCalculator()
    val statistics = calculator.calculate(result)

    // JSON エクスポート
    if (config.exportJson) {
        val jsonExporter = JsonExporter()
        val jsonFile = jsonExporter.export(statistics)
        println("Statistics exported to JSON: ${jsonFile.absolutePath}")
    }

    // CSV エクスポート
    if (config.exportCsv) {
        val csvExporter = CsvExporter()
        val csvFile = csvExporter.export(result)
        println("Results exported to CSV: ${csvFile.absolutePath}")
    }

    // 統計可視化レポート生成（Phase 7の新機能）
    if (config.generateVisualization) {
        val statisticsReportGenerator = StatisticsReportGenerator()
        val visualizationFile = statisticsReportGenerator.saveToFile(statistics)
        println("Statistics visualization report: ${visualizationFile.absolutePath}")
    }

    // 研究論文用レポート生成（Phase 12の新機能）
    if (config.generateResearchReport) {
        println("\nGenerating research report...")

        val board = createStandardBoard()
        val boardStats = BoardStatisticsCalculator().calculate(result, board)
        val detailedStats = DetailedStatisticsCalculator().calculate(result)

        val researchReportGenerator = ResearchReportGenerator()
        val htmlFile = researchReportGenerator.saveToFile(detailedStats, boardStats)

        println("Research report saved: ${htmlFile.absolutePath}")

        // PDF出力（オプション）
        if (config.generatePdf) {
            println("Generating PDF...")
            val pdfExporter = PdfExporter()
            val pdfFile = File(htmlFile.absolutePath.replace(".html", ".pdf"))

            pdfExporter.export(htmlFile, pdfFile)
                .onSuccess {
                    println("PDF report saved: ${it.absolutePath}")
                }
                .onFailure {
                    println("PDF generation failed: ${it.message}")
                }
        }
    }

    println()
}
