package com.monopoly.cli

/**
 * CLI引数をパースするクラス
 */
class ArgumentParser {
    /**
     * CLI引数をパースしてExperimentConfigを生成
     *
     * @param args コマンドライン引数
     * @return パース結果（成功時はExperimentConfig、ヘルプ表示時はnull）
     * @throws IllegalArgumentException パースエラー時
     */
    fun parse(args: Array<String>): ExperimentConfig? {
        if (args.isEmpty()) {
            return defaultConfig()
        }

        // ヘルプ表示
        if (args.any { it == "--help" || it == "-h" }) {
            printHelp()
            return null
        }

        var gameCount: Int = DEFAULT_GAME_COUNT
        var strategiesString: String? = null

        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "--games", "-g" -> {
                    i++
                    if (i >= args.size) {
                        throw IllegalArgumentException("Missing value for --games")
                    }
                    gameCount =
                        args[i].toIntOrNull()
                            ?: throw IllegalArgumentException("Invalid game count: ${args[i]}")
                }
                "--strategies", "-s" -> {
                    i++
                    if (i >= args.size) {
                        throw IllegalArgumentException("Missing value for --strategies")
                    }
                    strategiesString = args[i]
                }
                else -> {
                    throw IllegalArgumentException("Unknown argument: ${args[i]}")
                }
            }
            i++
        }

        val strategies =
            if (strategiesString != null) {
                StrategyFactory.createMultiple(strategiesString)
            } else {
                defaultStrategies()
            }

        return ExperimentConfig(
            gameCount = gameCount,
            strategies = strategies,
        )
    }

    private fun defaultConfig(): ExperimentConfig =
        ExperimentConfig(
            gameCount = DEFAULT_GAME_COUNT,
            strategies = defaultStrategies(),
        )

    private fun defaultStrategies() =
        StrategyFactory.createMultiple("aggressive,conservative,roi,balanced")

    private fun printHelp() {
        println(
            """
            |Monopoly Experiment Runner
            |
            |Usage: experiment [options]
            |
            |Options:
            |  -g, --games <count>       Number of games to run (default: $DEFAULT_GAME_COUNT)
            |  -s, --strategies <list>   Comma-separated list of strategies (default: aggressive,conservative,roi,balanced)
            |  -h, --help                Show this help message
            |
            |Available Strategies:
            |${formatStrategies()}
            |
            |Examples:
            |  # Run 100 games with default strategies (4 players)
            |  ./gradlew runExperiment --args="-g 100"
            |
            |  # Run with 2 players using specific strategies
            |  ./gradlew runExperiment --args="-g 50 -s aggressive,conservative"
            |
            |  # Run with 3 players
            |  ./gradlew runExperiment --args="-s random,roi,balanced"
            |
            |  # Run with all 7 strategies (7 players)
            |  ./gradlew runExperiment --args="-g 20 -s always,random,conservative,aggressive,setfocused,roi,balanced"
            """.trimMargin(),
        )
    }

    private fun formatStrategies(): String =
        StrategyFactory.availableStrategies.joinToString("\n") { name ->
            val description: String = StrategyFactory.getDescription(name)
            "  - $name: $description"
        }

    companion object {
        const val DEFAULT_GAME_COUNT = 10
    }
}
