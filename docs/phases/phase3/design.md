# Phase 3 詳細設計

**目標**: 異なる戦略を比較できるようにする

---

## 1. 設計概要

Phase 3では、Phase 1で定義した`BuyStrategy`インターフェースを利用して、2つの新しい戦略を実装します。また、コマンドライン引数で戦略を選択できるようにします。

### 設計方針

1. **既存インターフェースの活用**
   - Phase 1の`BuyStrategy`インターフェースをそのまま使用
   - 新しい戦略はこのインターフェースを実装するだけ

2. **戦略パターンの徹底**
   - 戦略の切り替えはコンパイル時ではなく実行時に行う
   - Main.ktで戦略インスタンスを生成し、Playerに渡す

3. **テスト容易性**
   - Randomを使う戦略はコンストラクタでRandomインスタンスを受け取る
   - テスト時にシード固定のRandomを注入可能

---

## 2. 戦略設計

### 2.1 既存の戦略インターフェース

```kotlin
interface BuyStrategy {
    fun shouldBuy(property: Property, playerMoney: Int): Boolean
}
```

このインターフェースはPhase 1で既に定義されています。

### 2.2 AlwaysBuyStrategy（既存）

Phase 1で実装済み。常に購入する戦略。

```kotlin
class AlwaysBuyStrategy : BuyStrategy {
    override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
        return playerMoney >= property.price
    }
}
```

### 2.3 RandomStrategy（新規）

購入判断をランダムに行う戦略。

```kotlin
class RandomStrategy(
    private val random: Random = Random.Default
) : BuyStrategy {
    override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
        // 所持金が足りない場合は購入しない
        if (playerMoney < property.price) {
            return false
        }
        // 50%の確率で購入
        return random.nextBoolean()
    }
}
```

**設計のポイント**:
- コンストラクタでRandomインスタンスを受け取る（デフォルトはRandom.Default）
- テスト時にシード固定のRandomを注入できる
- 所持金チェックは必須（破産を防ぐ）

### 2.4 ConservativeStrategy（新規）

一定額以上の現金を保持する慎重な戦略。

```kotlin
class ConservativeStrategy(
    private val minimumCashReserve: Int = 500
) : BuyStrategy {
    override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
        // 購入後の所持金が閾値以上なら購入
        val moneyAfterPurchase = playerMoney - property.price
        return moneyAfterPurchase >= minimumCashReserve
    }
}
```

**設計のポイント**:
- 閾値（minimumCashReserve）はコンストラクタで設定可能
- デフォルト値は$500
- 購入後の所持金が閾値以上であることを確認

---

## 3. 戦略選択機能の設計

### 3.1 コマンドライン引数

```
Usage: ./gradlew run --args="--strategy <strategy-name>"

Options:
  --strategy <name>  戦略を指定 (always-buy, random, conservative)
                     デフォルト: always-buy
  --help             ヘルプを表示

Examples:
  ./gradlew run --args="--strategy random"
  ./gradlew run --args="--strategy conservative"
```

### 3.2 戦略ファクトリー

Main.kt内に戦略を生成する関数を追加。

```kotlin
fun createStrategy(strategyName: String): BuyStrategy {
    return when (strategyName.lowercase()) {
        "always-buy" -> AlwaysBuyStrategy()
        "random" -> RandomStrategy()
        "conservative" -> ConservativeStrategy()
        else -> {
            println("Error: Unknown strategy '$strategyName'")
            println("Available strategies: always-buy, random, conservative")
            exitProcess(1)
        }
    }
}
```

### 3.3 引数パース

Main.kt内に引数をパースする関数を追加。

```kotlin
fun parseArgs(args: Array<String>): String {
    if (args.contains("--help")) {
        printHelp()
        exitProcess(0)
    }

    val strategyIndex = args.indexOf("--strategy")
    if (strategyIndex != -1 && strategyIndex + 1 < args.size) {
        return args[strategyIndex + 1]
    }

    return "always-buy" // デフォルト
}

fun printHelp() {
    println("""
        Monopoly Game Simulator - Phase 3

        Usage: ./gradlew run --args="--strategy <strategy-name>"

        Options:
          --strategy <name>  戦略を指定
                             always-buy: 常に購入する（Phase 1の戦略）
                             random: ランダムに購入する
                             conservative: 一定額以上の現金を保持
                             デフォルト: always-buy
          --help             ヘルプを表示

        Examples:
          ./gradlew run --args="--strategy random"
          ./gradlew run --args="--strategy conservative"
    """.trimIndent())
}
```

### 3.4 Main関数の変更

```kotlin
fun main(args: Array<String>) {
    val strategyName = parseArgs(args)
    val strategy1 = createStrategy(strategyName)
    val strategy2 = createStrategy(strategyName)

    println("=".repeat(60))
    println("Monopoly Game - Phase 3")
    println("Strategy: $strategyName")
    println("=".repeat(60))
    println()

    val player1: Player = Player("Alice", strategy1)
    val player2: Player = Player("Bob", strategy2)

    // 以降は既存のコードと同じ
    // ...
}
```

---

## 4. ディレクトリ構造

```
src/main/kotlin/com/monopoly/
  ├── domain/
  │   ├── strategy/
  │   │   ├── BuyStrategy.kt         (既存)
  │   │   ├── AlwaysBuyStrategy.kt   (既存)
  │   │   ├── RandomStrategy.kt      (新規)
  │   │   └── ConservativeStrategy.kt (新規)
  │   └── ...
  └── cli/
      └── Main.kt                      (変更)

src/test/kotlin/com/monopoly/
  └── domain/
      └── strategy/
          ├── AlwaysBuyStrategyTest.kt  (既存)
          ├── RandomStrategyTest.kt     (新規)
          └── ConservativeStrategyTest.kt (新規)
```

---

## 5. テスト設計

### 5.1 RandomStrategyのテスト

```kotlin
class RandomStrategyTest : StringSpec({
    "should buy property when random returns true and has enough money" {
        // シード固定でtrueを返すRandom
        val random = Random(seed = 1)
        val strategy = RandomStrategy(random)
        val property = Property("Test", 1, 200, 10, ColorGroup.BROWN)

        // 複数回テストして動作を確認
        val results = (1..100).map {
            strategy.shouldBuy(property, 1500)
        }

        // 約50%がtrueであることを確認
        val trueCount = results.count { it }
        trueCount shouldBeInRange 40..60
    }

    "should not buy property when not enough money" {
        val strategy = RandomStrategy()
        val property = Property("Test", 1, 200, 10, ColorGroup.BROWN)

        strategy.shouldBuy(property, 100) shouldBe false
    }
})
```

### 5.2 ConservativeStrategyのテスト

```kotlin
class ConservativeStrategyTest : StringSpec({
    "should buy property when enough cash reserve remains" {
        val strategy = ConservativeStrategy(minimumCashReserve = 500)
        val property = Property("Test", 1, 200, 10, ColorGroup.BROWN)

        // $1500 - $200 = $1300 (>= $500) なので購入する
        strategy.shouldBuy(property, 1500) shouldBe true
    }

    "should not buy property when cash reserve would be below minimum" {
        val strategy = ConservativeStrategy(minimumCashReserve = 500)
        val property = Property("Test", 1, 300, 10, ColorGroup.BROWN)

        // $700 - $300 = $400 (< $500) なので購入しない
        strategy.shouldBuy(property, 700) shouldBe false
    }

    "should buy property when cash reserve would be exactly at minimum" {
        val strategy = ConservativeStrategy(minimumCashReserve = 500)
        val property = Property("Test", 1, 500, 10, ColorGroup.BROWN)

        // $1000 - $500 = $500 (== $500) なので購入する
        strategy.shouldBuy(property, 1000) shouldBe true
    }
})
```

---

## 6. Phase 1, 2への影響

### 変更が必要なファイル

1. **Main.kt**
   - 引数パース処理を追加
   - 戦略ファクトリー関数を追加
   - ヘルプメッセージを追加
   - 既存の機能は維持

### 変更が不要なファイル

- すべてのPhase 1, 2のファイル（Main.kt以外）
- BuyStrategyインターフェース
- GameService
- GameState
- その他すべてのドメインモデル

---

## 7. 戦略比較の方法

Phase 3完了後、以下の方法で戦略を比較できます：

1. **各戦略で個別にゲームを実行**
   ```bash
   ./gradlew run --args="--strategy always-buy"
   ./gradlew run --args="--strategy random"
   ./gradlew run --args="--strategy conservative"
   ```

2. **HTMLレポートで挙動を確認**
   - 各戦略でHTMLレポートが生成される
   - イベントタイムラインで購入判断の違いを確認
   - 最終資産額の違いを確認

3. **Phase 5以降で統計的比較**
   - 複数回実行して勝率を比較
   - 平均資産額、平均ゲーム長を比較

---

## 8. 今後の拡張性

Phase 3で実装する戦略選択機能は、今後の拡張に対応しやすい設計です：

### Phase 10での拡張

Phase 10（高度な戦略実装）では、以下のような戦略を追加予定：

- セット重視戦略（同色セット完成を優先）
- ROI戦略（投資効率を計算）
- 位置戦略（高収益マスを優先）

これらの戦略も同じ`BuyStrategy`インターフェースを実装し、戦略ファクトリーに追加するだけで利用可能になります。

---

**作成日**: 2025-11-16
**最終更新**: 2025-11-16
