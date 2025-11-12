# Phase 1: コーディングガイド

このドキュメントは、Phase 1実装時のKotlin/Kotestのコーディング規約を定義します。

---

## パッケージ構造

```
com.monopoly
├── domain           # ドメイン層（ゲームロジック）
│   ├── model       # データモデル（Player, Property, Board, GameState）
│   ├── service     # ゲームロジック（GameService, Dice）
│   └── strategy    # 戦略パターン（Strategy, AlwaysBuyStrategy）
└── cli             # CLIエントリーポイント（MonopolyGame）
```

---

## Kotlin規約

### 命名規則
- **クラス**: PascalCase（`GameService`）
- **メソッド**: camelCase（`shouldBuyProperty`）
- **定数**: UPPER_SNAKE_CASE（`MAX_PLAYERS`）
- **パッケージ**: 小文字のみ（`com.monopoly.domain.model`）

### Null安全性
- **nullable型は明示的に**: `var owner: Player? = null`
- **安全な呼び出し演算子**: `player?.getName()`
- **非null保証は慎重に**: `player!!.getName()`（確実な場合のみ）

### 不変性
- **値オブジェクト**: `data class`でimmutable
- **エンティティ**: 必要に応じて`var`を使用
- **コレクション**: 防御的コピー（`ArrayList(originalList)`）

### 例

```kotlin
// 値オブジェクト（immutable）
data class Money(val amount: Int) {
    operator fun plus(other: Money) = Money(amount + other.amount)
}

// エンティティ（部分的にmutable）
data class Player(
    val id: String,
    var cash: Int,
    var position: Int,
    val ownedProperties: MutableSet<Property> = mutableSetOf()
)
```

---

## テストコード規約

### テストスタイル
- **フレームワーク**: Kotest StringSpec
- **テスト名**: "should + 動作"（英語）
- **Given-When-Thenコメント**: 各テストケースに必ず記載

### テストの構造

```kotlin
class PlayerTest : StringSpec({
    // TC-001: Player初期化
    // Given: なし
    // When: 新しいPlayerを作成
    // Then: 初期所持金が$1500、位置が0、破産フラグがfalse
    "player should be initialized with $1500, position 0, and not bankrupt" {
        // Given
        val strategy = AlwaysBuyStrategy()

        // When
        val player = Player(name = "Alice", strategy = strategy)

        // Then
        player.money shouldBe 1500
        player.position shouldBe 0
        player.isBankrupt() shouldBe false
    }
})
```

### アサーション
- **Kotest Matchers**を使用:
  - `shouldBe`: 等価性チェック
  - `shouldContain`: コレクション内包チェック
  - `shouldThrow`: 例外チェック

```kotlin
// 等価性
player.cash shouldBe 1500

// コレクション
player.ownedProperties shouldContain property

// 例外
shouldThrow<InsufficientFundsException> {
    player.buyProperty(expensiveProperty)
}
```

---

## エラーハンドリング

### Phase 1: 例外方式
Phase 1ではシンプルな例外方式を使用します。

```kotlin
fun buyProperty(property: Property) {
    if (cash < property.price) {
        throw InsufficientFundsException("Cash: $cash, Price: ${property.price}")
    }
    cash -= property.price
    ownedProperties.add(property)
}
```

### 将来（Phase 8以降）
必要に応じてResult型を検討します。

---

## テスタビリティ

### 依存性注入
依存をコンストラクタで注入します。

```kotlin
class GameService(
    private val dice: Dice = Dice()
) {
    fun executeTurn(gameState: GameState) {
        val roll = dice.roll()
        // ...
    }
}
```

### テスト用のシード
`Dice`はシード指定可能にします。

```kotlin
class Dice(private val random: Random = Random.Default) {
    constructor(seed: Long) : this(Random(seed))

    fun roll(): Int {
        val die1 = random.nextInt(1, 7)
        val die2 = random.nextInt(1, 7)
        return die1 + die2
    }
}

// テストコード
"dice should return deterministic values with seed" {
    val dice = Dice(seed = 42)

    val roll = dice.roll()

    // シード固定で結果が再現可能
    roll shouldBe 8  // 例
}
```

---

## Phase 1の簡略化

### ボード構成
- 全40マスを実装せず、一部のプロパティのみ実装してもOK
- 例: 10-15マス程度（GO + プロパティ10個程度）

### マス目の種類
- Phase 1ではプロパティとGOのみ実装
- 税金、チャンス、共同基金は無視

### ゾロ目
- Phase 1では無視（サイコロの合計値のみ使用）
- "もう一度振る"ルールは実装しない

---

## コード例

### data classの活用

```kotlin
data class Player(
    val name: String,
    var cash: Int = 1500,
    var position: Int = 0,
    val ownedProperties: MutableSet<Property> = mutableSetOf(),
    var bankrupt: Boolean = false,
    val strategy: Strategy
) {
    fun addMoney(amount: Int) {
        cash += amount
    }

    fun subtractMoney(amount: Int) {
        cash -= amount
    }

    fun isBankrupt(): Boolean = bankrupt

    fun getTotalAssets(): Int {
        return cash + ownedProperties.sumOf { it.price }
    }
}
```

### when式（パターンマッチング）

```kotlin
when (val space = board.getSpace(position)) {
    is PropertySpace -> handleProperty(space, player)
    is Go -> handleGo(player)
    else -> {}
}
```

### 拡張関数

```kotlin
fun Player.canAfford(price: Int): Boolean = cash >= price

// 使用例
if (player.canAfford(property.price)) {
    player.buyProperty(property)
}
```

---

## 参考リンク

- Kotlin公式ドキュメント: https://kotlinlang.org/docs/home.html
- Kotest公式ドキュメント: https://kotest.io/
- MockK公式ドキュメント: https://mockk.io/

---

**作成日**: 2025-11-12
**対象フェーズ**: Phase 1
