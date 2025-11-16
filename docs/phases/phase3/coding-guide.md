# Phase 3 コーディング規約

このドキュメントはPhase 3固有のコーディング規約を記載します。
プロジェクト全体の規約は [`claude.md`](../../claude.md) を参照してください。

---

## 1. 戦略クラスの命名規則

### クラス名

戦略クラスは必ず`Strategy`で終わる名前にします。

```kotlin
// Good
class RandomStrategy : BuyStrategy
class ConservativeStrategy : BuyStrategy
class AggressiveStrategy : BuyStrategy

// Bad
class Random : BuyStrategy
class Conservative : BuyStrategy
```

### ファイル名

クラス名と同じファイル名を使用します。

```
RandomStrategy.kt
ConservativeStrategy.kt
```

---

## 2. 戦略クラスの実装パターン

### 2.1 基本構造

すべての戦略クラスは以下の構造に従います：

```kotlin
/**
 * [戦略の説明]
 *
 * @property [パラメータ名] [パラメータの説明]（パラメータがある場合）
 */
class XxxStrategy(
    private val param1: Type = defaultValue
) : BuyStrategy {
    override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
        // 所持金チェック（必須）
        if (playerMoney < property.price) {
            return false
        }

        // 戦略固有のロジック
        return [判定ロジック]
    }
}
```

### 2.2 所持金チェックは必須

すべての戦略で、まず所持金が足りるかをチェックします。

```kotlin
// Good
override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
    if (playerMoney < property.price) {
        return false
    }
    // 戦略固有のロジック
}

// Bad - 所持金チェックを忘れている
override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
    return random.nextBoolean()  // 所持金が足りなくても購入しようとする
}
```

---

## 3. テスト容易性のための設計

### 3.1 依存性の注入

Randomなど、テスト時に制御したい依存性はコンストラクタで受け取ります。

```kotlin
// Good - テスト時にシード固定のRandomを注入できる
class RandomStrategy(
    private val random: Random = Random.Default
) : BuyStrategy {
    override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
        if (playerMoney < property.price) return false
        return random.nextBoolean()
    }
}

// Bad - Randomをハードコードしている
class RandomStrategy : BuyStrategy {
    override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
        if (playerMoney < property.price) return false
        return Random.nextBoolean()  // テスト時に制御できない
    }
}
```

### 3.2 デフォルト引数の活用

実行時のデフォルト値はコンストラクタのデフォルト引数で指定します。

```kotlin
// Good
class ConservativeStrategy(
    private val minimumCashReserve: Int = 500
) : BuyStrategy

// 使用例
val strategy1 = ConservativeStrategy()           // デフォルト: $500
val strategy2 = ConservativeStrategy(300)        // カスタム: $300
val strategy3 = ConservativeStrategy(1000)       // カスタム: $1000
```

---

## 4. ドキュメンテーション

### 4.1 クラスのKDoc

戦略クラスには必ず説明を付けます。

```kotlin
/**
 * ランダムに購入判断を行う戦略
 *
 * 所持金が足りる場合、50%の確率でプロパティを購入します。
 * リスクの高い戦略で、破産しやすい傾向があります。
 *
 * @property random 乱数生成器（テスト時にシード固定のインスタンスを注入可能）
 */
class RandomStrategy(
    private val random: Random = Random.Default
) : BuyStrategy {
    // ...
}
```

```kotlin
/**
 * 一定額以上の現金を保持する保守的な戦略
 *
 * 購入後の所持金が閾値以上になる場合のみプロパティを購入します。
 * リスクを抑えた戦略で、破産しにくい傾向があります。
 *
 * @property minimumCashReserve 保持する最小現金額（デフォルト: $500）
 */
class ConservativeStrategy(
    private val minimumCashReserve: Int = 500
) : BuyStrategy {
    // ...
}
```

### 4.2 メソッドのKDoc

shouldBuyメソッドには説明を付けません（インターフェースで定義されているため）。

ただし、複雑なロジックの場合はインラインコメントを付けます。

```kotlin
override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
    // 所持金が足りない場合は購入しない
    if (playerMoney < property.price) {
        return false
    }

    // ROIを計算して閾値以上なら購入
    val roi = property.rent.toDouble() / property.price
    return roi >= minimumROI
}
```

---

## 5. テストコードの規約

### 5.1 テストクラス名

戦略クラス名 + `Test`

```kotlin
class RandomStrategyTest : StringSpec({
    // ...
})

class ConservativeStrategyTest : StringSpec({
    // ...
})
```

### 5.2 テストケース名

Given-When-Thenをテスト名に含めます。

```kotlin
"should buy property when random returns true and has enough money" {
    // Given
    val strategy = RandomStrategy(Random(seed = 1))
    val property = Property("Test", 1, 200, 10, ColorGroup.BROWN)

    // When & Then
    // ...
}

"should not buy property when not enough money" {
    // ...
}
```

### 5.3 Randomを使うテスト

シード固定のRandomを使います。

```kotlin
// Good
"should return approximately 50% true" {
    val strategy = RandomStrategy(Random(seed = 1))
    val property = Property("Test", 1, 200, 10, ColorGroup.BROWN)

    val results = (1..100).map { strategy.shouldBuy(property, 1500) }
    val trueCount = results.count { it }

    trueCount shouldBeInRange 40..60  // 約50%
}

// Bad - Randomがランダムすぎてテストが不安定
"should return 50% true" {
    val strategy = RandomStrategy()  // シード固定していない
    val results = (1..10).map { strategy.shouldBuy(property, 1500) }
    val trueCount = results.count { it }

    trueCount shouldBe 5  // 失敗する可能性が高い
}
```

---

## 6. コマンドライン引数の規約

### 6.1 引数名

小文字のケバブケース（ハイフン区切り）を使用します。

```kotlin
// Good
--strategy always-buy
--strategy random
--strategy conservative

// Bad
--strategy AlwaysBuy
--strategy RANDOM
--strategy conservative_strategy
```

### 6.2 エラーメッセージ

不正な引数の場合は分かりやすいエラーメッセージを表示します。

```kotlin
fun createStrategy(strategyName: String): BuyStrategy {
    return when (strategyName.lowercase()) {
        "always-buy" -> AlwaysBuyStrategy()
        "random" -> RandomStrategy()
        "conservative" -> ConservativeStrategy()
        else -> {
            println("Error: Unknown strategy '$strategyName'")
            println("Available strategies: always-buy, random, conservative")
            println("Use --help for more information")
            exitProcess(1)
        }
    }
}
```

---

## 7. マジックナンバーの扱い

### 7.1 戦略のパラメータ

戦略のパラメータ（閾値など）は明示的に名前を付けます。

```kotlin
// Good
class ConservativeStrategy(
    private val minimumCashReserve: Int = 500  // 明示的な名前
) : BuyStrategy {
    override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
        return playerMoney - property.price >= minimumCashReserve
    }
}

// Bad
class ConservativeStrategy : BuyStrategy {
    override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
        return playerMoney - property.price >= 500  // マジックナンバー
    }
}
```

### 7.2 @Suppress の使用

明らかなマジックナンバー（確率の50%など）は`@Suppress`を使用しません。

```kotlin
// Good - 50%は明らかなので@Suppressは不要
override fun shouldBuy(property: Property, playerMoney: Int): Boolean {
    if (playerMoney < property.price) return false
    return random.nextBoolean()  // 50%の確率
}
```

---

## 8. Phase 3固有の注意事項

### 8.1 既存コードの変更は最小限に

Main.kt以外のファイルは変更しません。

```
変更するファイル:
- src/main/kotlin/com/monopoly/cli/Main.kt

追加するファイル:
- src/main/kotlin/com/monopoly/domain/strategy/RandomStrategy.kt
- src/main/kotlin/com/monopoly/domain/strategy/ConservativeStrategy.kt
- src/test/kotlin/com/monopoly/domain/strategy/RandomStrategyTest.kt
- src/test/kotlin/com/monopoly/domain/strategy/ConservativeStrategyTest.kt
```

### 8.2 後方互換性の維持

引数なしでも実行できるようにします（デフォルトはalways-buy）。

```kotlin
// 引数なしで実行 → always-buyを使用
./gradlew run

// 引数ありで実行 → 指定した戦略を使用
./gradlew run --args="--strategy random"
```

---

## 9. チェックリスト

Phase 3の実装時は以下をチェックします：

- [ ] 戦略クラス名が`Strategy`で終わっている
- [ ] すべての戦略で所持金チェックを実装している
- [ ] Randomなどの依存性をコンストラクタで注入している
- [ ] デフォルト引数を適切に設定している
- [ ] KDocで戦略の説明を記載している
- [ ] テストでシード固定のRandomを使用している（Randomを使う戦略の場合）
- [ ] コマンドライン引数がケバブケースになっている
- [ ] エラーメッセージが分かりやすい
- [ ] 引数なしでも実行できる（デフォルト動作）
- [ ] Main.kt以外のファイルを変更していない

---

**作成日**: 2025-11-16
**最終更新**: 2025-11-16
