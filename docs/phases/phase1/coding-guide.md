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

### Null安全性の原則

**基本方針: nullableを極力排除し、型で状態を表現する**

❌ **避けるべき設計**
```kotlin
// nullで状態を表現
class Property(val owner: Player? = null)

// 使用側でnullチェックが必要
if (property.owner != null) {
    println(property.owner.name)  // nullチェック必須
}
```

✅ **推奨される設計**
```kotlin
// sealed classで状態を明示的に表現
sealed class PropertyOwnership {
    object Unowned : PropertyOwnership()
    data class OwnedByPlayer(val player: Player) : PropertyOwnership()
}

data class Property(
    val ownership: PropertyOwnership = PropertyOwnership.Unowned
)

// 使用側は型安全にパターンマッチング
when (property.ownership) {
    is PropertyOwnership.OwnedByPlayer ->
        println(property.ownership.player.name)  // nullチェック不要
    is PropertyOwnership.Unowned ->
        println("No owner")
}
```

**nullableが許容されるケース**
- 外部API/DBからの入力で型変換前の一時的な状態
- Javaライブラリとの相互運用で避けられない場合

これらの場合も、可能な限り早期に非nullな型に変換する。

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

## Null回避のデザインパターン

### パターン1: Sealed Class（状態の明示的表現）

**使用場面**: オブジェクトが複数の排他的な状態を持つ場合

```kotlin
// 例1: プロパティの所有状態
sealed class PropertyOwnership {
    object Unowned : PropertyOwnership()
    data class OwnedByPlayer(val player: Player) : PropertyOwnership()
    object OwnedByBank : PropertyOwnership()  // 将来の拡張
}

// 例2: ゲーム結果
sealed class GameResult {
    data class Victory(val winner: Player) : GameResult()
    object Draw : GameResult()
    object Cancelled : GameResult()
}
```

### パターン2: デフォルト値の活用

**使用場面**: 「空」や「初期状態」が自然に存在する場合

```kotlin
// ❌ 避けるべき
data class PlayerStats(
    val gamesPlayed: Int?,
    val gamesWon: Int?
)

// ✅ 推奨
data class PlayerStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0
)
```

### パターン3: Option/Result型（Phase 2以降で検討）

Phase 1では例外方式で十分だが、Phase 2以降で検討:

```kotlin
// 将来の拡張例
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val error: String) : Result<Nothing>()
}

fun findPlayer(name: String): Result<Player>
```

### パターン4: Empty Object Pattern

**使用場面**: 「何もない」状態を表現したい場合

```kotlin
// ❌ 避けるべき
val properties: List<Property>? = null

// ✅ 推奨
val properties: List<Property> = emptyList()

// カスタムEmpty Object
object NoStrategy : BuyStrategy {
    override fun shouldBuy(property: Property, currentMoney: Int) = false
}
```

---

## オブジェクト指向エクササイズ 9つのルール

### ルール1: 1メソッド1インデント

**制約**: メソッド内のインデントは1段階まで

```kotlin
// ❌ 避けるべき（インデント2段階）
fun processPlayers(players: List<Player>) {
    for (player in players) {
        if (!player.isBankrupt) {
            player.takeTurn()
        }
    }
}

// ✅ 推奨（インデント1段階、メソッド抽出）
fun processPlayers(players: List<Player>) {
    players.forEach { processPlayer(it) }
}

private fun processPlayer(player: Player) {
    if (player.isBankrupt) return
    player.takeTurn()
}
```

### ルール2: else禁止

**制約**: else句を使わず、early returnやポリモーフィズムで代替

```kotlin
// ❌ 避けるべき
fun calculateRent(property: Property): Int {
    if (property.isOwned()) {
        return property.rent
    } else {
        return 0
    }
}

// ✅ 推奨（early return）
fun calculateRent(property: Property): Int {
    if (!property.isOwned()) return 0
    return property.rent
}

// ✅ 推奨（ポリモーフィズム）
sealed class PropertyState {
    abstract fun calculateRent(): Int

    object Unowned : PropertyState() {
        override fun calculateRent() = 0
    }

    data class Owned(val rent: Int) : PropertyState() {
        override fun calculateRent() = rent
    }
}
```

### ルール3: プリミティブ型のラップ

**制約**: Int, String等を意味のある型でラップ

```kotlin
// ❌ 避けるべき
data class Player(
    val money: Int,
    val position: Int
)

// ✅ 推奨（値オブジェクト）
@JvmInline
value class Money(val amount: Int) {
    operator fun plus(other: Money) = Money(amount + other.amount)
    operator fun minus(other: Money) = Money(amount - other.amount)
    fun isEnough(required: Money) = amount >= required.amount
}

@JvmInline
value class BoardPosition(val value: Int) {
    init {
        require(value in 0..39) { "Position must be 0-39" }
    }

    fun advance(steps: Int) = BoardPosition((value + steps) % 40)
}

data class Player(
    val money: Money,
    val position: BoardPosition
)
```

### ルール4: 1行1ドット

**制約**: メソッドチェーンを避け、中間変数で意図を明確に

```kotlin
// ❌ 避けるべき
val totalAssets = player.getOwnedProperties().sumOf { it.getPrice() }

// ✅ 推奨
val ownedProperties = player.ownedProperties
val totalAssets = ownedProperties.sumOf { it.price }

// またはメソッドに抽出
fun Player.calculateTotalAssets(): Money {
    val propertyValue = ownedProperties.sumOf { it.price }
    return money.plus(propertyValue)
}
```

### ルール5: 省略禁止

**制約**: 変数名・メソッド名を省略しない

```kotlin
// ❌ 避けるべき
val p = Player("Alice", strat)
fun calc(p: Property): Int

// ✅ 推奨
val player = Player("Alice", strategy)
fun calculateRent(property: Property): Money
```

### ルール6: 小さなエンティティ

**制約**: クラスは50行以内、メソッドは10行以内を目安

```kotlin
// ✅ 推奨（責務を分割）
class GameTurnExecutor(
    private val diceRoller: DiceRoller,
    private val playerMover: PlayerMover,
    private val spaceProcessor: SpaceProcessor
) {
    fun executeTurn(player: Player, gameState: GameState) {
        val diceResult = diceRoller.roll()
        val newPosition = playerMover.move(player, diceResult)
        spaceProcessor.process(player, newPosition, gameState)
    }
}
```

### ルール7: 2インスタンス変数まで

**制約**: 1クラスのインスタンス変数は最大2つ

```kotlin
// ❌ 避けるべき（3つ以上のフィールド）
data class Player(
    val name: String,
    val money: Money,
    val position: BoardPosition,
    val properties: List<Property>
)

// ✅ 推奨（グルーピング）
data class PlayerState(
    val money: Money,
    val position: BoardPosition
)

data class Player(
    val identity: PlayerId,
    val state: PlayerState
)

// または振る舞いを中心に
class Player(
    val identity: PlayerId,
    private val assets: PlayerAssets  // money + properties
)
```

### ルール8: ファーストクラスコレクション

**制約**: コレクションは専用クラスでラップ

```kotlin
// ❌ 避けるべき
class Player(
    val ownedProperties: MutableList<Property> = mutableListOf()
)

// ✅ 推奨
class PropertyCollection(
    private val properties: List<Property> = emptyList()
) {
    fun add(property: Property): PropertyCollection =
        PropertyCollection(properties + property)

    fun calculateTotalValue(): Money =
        Money(properties.sumOf { it.price })

    fun contains(property: Property): Boolean =
        properties.contains(property)

    val size: Int get() = properties.size
}

class Player(
    val ownedProperties: PropertyCollection = PropertyCollection()
)
```

### ルール9: getter/setter/プロパティ禁止

**制約**: データ取得ではなく、振る舞いを中心に設計

```kotlin
// ❌ 避けるべき（データ中心）
class Player {
    fun getMoney(): Money
    fun setMoney(money: Money)
}
// 使用側でビジネスロジック
player.setMoney(player.getMoney().minus(price))

// ✅ 推奨（振る舞い中心）
class Player {
    fun pay(amount: Money) {
        require(canAfford(amount)) { "Insufficient funds" }
        money = money.minus(amount)
    }

    fun canAfford(amount: Money): Boolean =
        money.isEnough(amount)
}
// 使用側は意図を表現
player.pay(property.price)
```

### Phase 1での適用方針

これらのルールは理想形ですが、Phase 1では以下のバランスで適用:

- **厳密に適用**: ルール2(else禁止), ルール5(省略禁止)
- **可能な範囲で適用**: ルール1, 3, 6, 9
- **Phase 2以降で検討**: ルール4, 7, 8

TDDのリファクタリングフェーズで段階的に適用していきます。

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
