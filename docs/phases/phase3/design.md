# Phase 3 詳細設計: 建物システムとダブル（ゾロ目）

**フェーズ目標**: 建物建設とゾロ目ルールを実装し、ゲームの戦略性を向上

---

## 目次

1. [概要](#概要)
2. [建物システム](#建物システム)
3. [ダブル（ゾロ目）システム](#ダブルゾロ目システム)
4. [クラス設計](#クラス設計)
5. [TDD実装順序](#tdd実装順序)
6. [テストケースリスト](#テストケースリスト)

---

## 概要

### Phase 3で実装する機能

1. **建物システム**
   - 家（最大4件）とホテル（1件）の建設
   - 同色プロパティ全所有（モノポリー）の判定
   - 建物の均等建設ルール
   - 建物数に応じた家賃計算

2. **ダブル（ゾロ目）**
   - ゾロ目判定ロジック
   - 追加ターン実行
   - 3回連続ゾロ目の追跡

### 設計方針

- **TDD**: すべての機能をテストファーストで実装
- **段階的な実装**: 建物システム → ゾロ目の順
- **既存コードの拡張**: Phase 1/2のコードを柔軟に変更
- **イベント記録**: すべての建物とゾロ目イベントを記録

---

## 建物システム

### 要件

#### モノポリーの公式ルール

1. **建設条件**
   - 同色の物件を全て所有している（モノポリー）
   - ターン終了後に建設可能

2. **建設ルール**
   - 家は1件ずつ建設（最大4件まで）
   - 同色グループ内で均等に建設（差は1件まで）
   - 4件の家をホテル1件に交換可能

3. **家賃計算**
   - 素地（建物なし）: 基本家賃
   - 家1件: 基本家賃の約5倍
   - 家2件: 基本家賃の約15倍
   - 家3件: 基本家賃の約45倍
   - 家4件: 基本家賃の約80倍
   - ホテル: 基本家賃の約100倍

### データモデル

#### 建物の種類

```kotlin
enum class BuildingType {
    HOUSE,
    HOTEL
}
```

#### プロパティの拡張

```kotlin
data class Property(
    val name: String,
    val position: Int,
    val price: Int,
    val rent: PropertyRent,  // 新規: 家賃構造
    val colorGroup: ColorGroup,
    val houseCost: Int,  // 新規: 家の建設コスト
    val hotelCost: Int,  // 新規: ホテルの建設コスト
    val ownership: PropertyOwnership = PropertyOwnership.Unowned,
    val buildings: PropertyBuildings = PropertyBuildings()  // 新規: 建物管理
)
```

#### PropertyRent（家賃構造）

```kotlin
data class PropertyRent(
    val base: Int,  // 素地の家賃
    val withHouse1: Int,  // 家1件の家賃
    val withHouse2: Int,  // 家2件の家賃
    val withHouse3: Int,  // 家3件の家賃
    val withHouse4: Int,  // 家4件の家賃
    val withHotel: Int  // ホテルの家賃
)
```

#### PropertyBuildings（建物管理）

```kotlin
data class PropertyBuildings(
    val houseCount: Int = 0,  // 家の数（0-4）
    val hasHotel: Boolean = false  // ホテルの有無
) {
    init {
        require(houseCount in 0..4) { "House count must be between 0 and 4" }
        require(!(hasHotel && houseCount > 0)) { "Cannot have both hotel and houses" }
    }

    fun canBuildHouse(): Boolean = houseCount < 4 && !hasHotel

    fun canBuildHotel(): Boolean = houseCount == 4 && !hasHotel

    fun getTotalBuildingCount(): Int = if (hasHotel) 5 else houseCount
}
```

### ビジネスロジック

#### MonopolyChecker（モノポリー判定）

```kotlin
class MonopolyChecker {
    /**
     * プレイヤーが指定した色グループのモノポリーを持っているか判定
     */
    fun hasMonopoly(player: Player, colorGroup: ColorGroup, board: Board): Boolean {
        val propertiesInGroup: List<Property> = board.getPropertiesByColorGroup(colorGroup)
        val ownedProperties: List<Property> = propertiesInGroup.filter { property ->
            property.ownership is PropertyOwnership.Owned &&
            (property.ownership as PropertyOwnership.Owned).owner == player
        }
        return ownedProperties.size == propertiesInGroup.size
    }
}
```

#### BuildingService（建物建設サービス）

```kotlin
class BuildingService(
    private val monopolyChecker: MonopolyChecker = MonopolyChecker()
) {
    /**
     * 家を建設する
     */
    fun buildHouse(
        player: Player,
        property: Property,
        board: Board,
        gameState: GameState
    ): BuildResult {
        // モノポリーチェック
        if (!monopolyChecker.hasMonopoly(player, property.colorGroup, board)) {
            return BuildResult.NoMonopoly
        }

        // 建設可能かチェック
        if (!property.buildings.canBuildHouse()) {
            return BuildResult.CannotBuildHouse
        }

        // 均等建設ルールチェック
        if (!canBuildEvenly(player, property, board)) {
            return BuildResult.UnevenBuilding
        }

        // 資金チェック
        if (player.state.money.value < property.houseCost) {
            return BuildResult.InsufficientFunds
        }

        // 建設実行
        player.subtractMoney(Money(property.houseCost))
        property.buildings = property.buildings.copy(houseCount = property.buildings.houseCount + 1)

        // イベント記録
        gameState.events.add(
            GameEvent.HouseBuilt(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                playerName = player.name,
                propertyName = property.name,
                houseCount = property.buildings.houseCount
            )
        )

        return BuildResult.Success
    }

    /**
     * ホテルを建設する
     */
    fun buildHotel(
        player: Player,
        property: Property,
        board: Board,
        gameState: GameState
    ): BuildResult {
        // モノポリーチェック
        if (!monopolyChecker.hasMonopoly(player, property.colorGroup, board)) {
            return BuildResult.NoMonopoly
        }

        // 建設可能かチェック
        if (!property.buildings.canBuildHotel()) {
            return BuildResult.CannotBuildHotel
        }

        // 資金チェック
        if (player.state.money.value < property.hotelCost) {
            return BuildResult.InsufficientFunds
        }

        // 建設実行
        player.subtractMoney(Money(property.hotelCost))
        property.buildings = property.buildings.copy(houseCount = 0, hasHotel = true)

        // イベント記録
        gameState.events.add(
            GameEvent.HotelBuilt(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                playerName = player.name,
                propertyName = property.name
            )
        )

        return BuildResult.Success
    }

    /**
     * 均等建設ルールのチェック
     * 同色グループ内の他の物件と家の数の差が1以下であることを確認
     */
    private fun canBuildEvenly(player: Player, property: Property, board: Board): Boolean {
        val propertiesInGroup: List<Property> = board.getPropertiesByColorGroup(property.colorGroup)
            .filter { it.ownership is PropertyOwnership.Owned &&
                     (it.ownership as PropertyOwnership.Owned).owner == player }

        val currentHouseCount: Int = property.buildings.houseCount
        val maxHouseCount: Int = propertiesInGroup.maxOf { it.buildings.houseCount }

        // 建設後の家の数が、グループ内の最大数+1を超えないこと
        return currentHouseCount >= maxHouseCount - 1
    }
}

sealed class BuildResult {
    object Success : BuildResult()
    object NoMonopoly : BuildResult()
    object CannotBuildHouse : BuildResult()
    object CannotBuildHotel : BuildResult()
    object UnevenBuilding : BuildResult()
    object InsufficientFunds : BuildResult()
}
```

#### 家賃計算の拡張

```kotlin
// GameServiceのpayRentメソッドを拡張
fun calculateRent(property: Property, diceRoll: Int): Int {
    return when {
        property.buildings.hasHotel -> property.rent.withHotel
        property.buildings.houseCount == 4 -> property.rent.withHouse4
        property.buildings.houseCount == 3 -> property.rent.withHouse3
        property.buildings.houseCount == 2 -> property.rent.withHouse2
        property.buildings.houseCount == 1 -> property.rent.withHouse1
        else -> property.rent.base
    }
}
```

---

## ダブル（ゾロ目）システム

### 要件

#### モノポリーの公式ルール

1. **ゾロ目の効果**
   - 2つのサイコロが同じ目の場合、ゾロ目
   - ゾロ目の場合、もう一度ターンを実行

2. **3回連続ゾロ目**
   - 3回連続でゾロ目を出した場合、刑務所へ移動
   - スタートを通過してもボーナスなし
   - （刑務所システムはPhase 4で実装）

### データモデル

#### Diceの拡張

```kotlin
data class DiceRoll(
    val die1: Int,
    val die2: Int
) {
    val total: Int = die1 + die2
    val isDouble: Boolean = die1 == die2
}

class Dice(private val random: Random = Random.Default) {
    constructor(seed: Long) : this(Random(seed))

    fun roll(): DiceRoll {
        val die1: Int = random.nextInt(1, 7)
        val die2: Int = random.nextInt(1, 7)
        return DiceRoll(die1, die2)
    }
}
```

#### PlayerStateの拡張

```kotlin
data class PlayerState(
    val money: Money,
    val position: BoardPosition,
    val consecutiveDoubles: Int = 0  // 新規: 連続ゾロ目カウント
) {
    init {
        require(consecutiveDoubles >= 0) { "Consecutive doubles count cannot be negative" }
    }
}
```

### ビジネスロジック

#### GameServiceのexecuteTurn拡張

```kotlin
fun executeTurn(gameState: GameState) {
    val currentPlayer: Player = gameState.getCurrentPlayer()
    var continuesTurn: Boolean = true
    var doublesCount: Int = currentPlayer.state.consecutiveDoubles

    // ターン開始イベント
    gameState.events.add(
        GameEvent.TurnStarted(
            turnNumber = gameState.turnNumber,
            timestamp = System.currentTimeMillis(),
            playerName = currentPlayer.name
        )
    )

    while (continuesTurn) {
        // サイコロを振る
        val diceRoll: DiceRoll = dice.roll()

        // サイコロイベント
        gameState.events.add(
            GameEvent.DiceRolled(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                playerName = currentPlayer.name,
                die1 = diceRoll.die1,
                die2 = diceRoll.die2,
                total = diceRoll.total
            )
        )

        // ゾロ目チェック
        if (diceRoll.isDouble) {
            doublesCount++

            // 3回連続ゾロ目チェック
            if (doublesCount >= 3) {
                // ゾロ目イベント（3回連続）
                gameState.events.add(
                    GameEvent.ThreeDoublesRolled(
                        turnNumber = gameState.turnNumber,
                        timestamp = System.currentTimeMillis(),
                        playerName = currentPlayer.name
                    )
                )

                // 刑務所へ移動（Phase 4で実装）
                // 現時点では、ターン終了
                continuesTurn = false
                doublesCount = 0
            } else {
                // 移動とマス目処理
                movePlayer(currentPlayer, diceRoll.total, gameState)
                processSpace(currentPlayer, gameState)

                // 破産チェック
                if (currentPlayer.state is PlayerState.Bankrupt) {
                    continuesTurn = false
                } else {
                    // ゾロ目なので、もう一度ターン実行
                    continuesTurn = true
                }
            }
        } else {
            // ゾロ目ではない場合、通常の移動
            movePlayer(currentPlayer, diceRoll.total, gameState)
            processSpace(currentPlayer, gameState)
            continuesTurn = false
            doublesCount = 0
        }
    }

    // 連続ゾロ目カウントを更新
    currentPlayer.state = currentPlayer.state.copy(consecutiveDoubles = doublesCount)

    // ターン終了イベント
    gameState.events.add(
        GameEvent.TurnEnded(
            turnNumber = gameState.turnNumber,
            timestamp = System.currentTimeMillis(),
            playerName = currentPlayer.name
        )
    )

    gameState.turnNumber++
    gameState.nextPlayer()
}
```

---

## クラス設計

### 新規追加クラス

1. **PropertyRent**: 家賃構造を表現
2. **PropertyBuildings**: 建物管理
3. **BuildingType**: 建物の種類（HOUSE, HOTEL）
4. **MonopolyChecker**: モノポリー判定
5. **BuildingService**: 建物建設サービス
6. **BuildResult**: 建設結果
7. **DiceRoll**: サイコロの結果

### 既存クラスの拡張

1. **Property**: 家賃構造、建物管理、建設コストを追加
2. **Dice**: DiceRollを返すように変更
3. **PlayerState**: 連続ゾロ目カウントを追加
4. **GameService**: ゾロ目ロジック、家賃計算の拡張
5. **GameEvent**: 建物イベント、ゾロ目イベントを追加

---

## TDD実装順序

### ステップ1: 建物データモデル（1日目）

1. PropertyRentのテストと実装
2. PropertyBuildingsのテストと実装
3. PropertyへのフィールドValue追加とテスト更新

### ステップ2: モノポリー判定（1日目）

4. MonopolyCheckerのテストと実装
5. Boardへのヘルパーメソッド追加

### ステップ3: 建物建設（2日目）

6. BuildingServiceのテストと実装
   - buildHouseのテスト
   - buildHotelのテスト
   - 均等建設ルールのテスト

### ステップ4: 家賃計算の拡張（2日目）

7. calculateRentのテストと実装
8. processSpaceの更新とテスト

### ステップ5: ダブル（ゾロ目）（3日目）

9. DiceRollのテストと実装
10. Diceクラスの拡張
11. PlayerStateへの連続ゾロ目カウント追加
12. executeTurnのゾロ目ロジック実装

### ステップ6: 統合テストと修正（3日目）

13. Phase 3統合テスト
14. Phase 1/2のテストがすべてパスすることを確認
15. リファクタリング

---

## テストケースリスト

### 建物システム

#### PropertyBuildingsのテスト

- [ ] 家が0-4件の範囲で初期化できること
- [ ] 家が5件以上でエラーになること
- [ ] ホテルと家が同時に存在する場合エラーになること
- [ ] canBuildHouseが正しく判定すること
- [ ] canBuildHotelが正しく判定すること
- [ ] getTotalBuildingCountが正しい値を返すこと

#### MonopolyCheckerのテスト

- [ ] 同色プロパティを全て所有している場合、モノポリーと判定されること
- [ ] 同色プロパティの一部のみ所有している場合、モノポリーではないと判定されること
- [ ] 所有プロパティがない場合、モノポリーではないと判定されること

#### BuildingServiceのテスト

- [ ] モノポリーを持っている場合、家を建設できること
- [ ] モノポリーを持っていない場合、家を建設できないこと
- [ ] 資金不足の場合、家を建設できないこと
- [ ] 均等建設ルールに違反する場合、家を建設できないこと
- [ ] 家4件の状態で、ホテルを建設できること
- [ ] 家4件未満の状態で、ホテルを建設できないこと
- [ ] 建設後、HouseBuiltイベントが記録されること
- [ ] ホテル建設後、HotelBuiltイベントが記録されること

#### 家賃計算のテスト

- [ ] 素地の家賃が正しく計算されること
- [ ] 家1件の家賃が正しく計算されること
- [ ] 家2件の家賃が正しく計算されること
- [ ] 家3件の家賃が正しく計算されること
- [ ] 家4件の家賃が正しく計算されること
- [ ] ホテルの家賃が正しく計算されること

### ダブル（ゾロ目）システム

#### DiceRollのテスト

- [ ] totalが正しく計算されること
- [ ] isDoubleがゾロ目の場合trueになること
- [ ] isDoubleがゾロ目でない場合falseになること

#### Diceクラスのテスト

- [ ] rollがDiceRollを返すこと
- [ ] 各サイコロの目が1-6の範囲であること

#### executeTurnのテスト

- [ ] ゾロ目の場合、追加ターンが実行されること
- [ ] ゾロ目でない場合、ターンが終了すること
- [ ] 2回連続ゾロ目の場合、3回目のターンが実行されること
- [ ] 3回連続ゾロ目の場合、ターンが終了すること（Phase 4で刑務所実装）
- [ ] 連続ゾロ目カウントが正しく更新されること
- [ ] DiceRolledイベントが記録されること

### 統合テスト

- [ ] ゲーム全体が正しく実行されること
- [ ] 建物建設が正しく動作すること
- [ ] ゾロ目による追加ターンが正しく動作すること
- [ ] Phase 1/2のテストがすべてパスすること

---

## 注意事項

### データ移行

Phase 1/2で作成したプロパティデータに、以下のフィールドを追加する必要があります：

- `rent: PropertyRent`
- `houseCost: Int`
- `hotelCost: Int`
- `buildings: PropertyBuildings`

### イベント追加

GameEventに以下のイベントを追加：

- `HouseBuilt`: 家建設イベント
- `HotelBuilt`: ホテル建設イベント
- `ThreeDoublesRolled`: 3回連続ゾロ目イベント

### 既存テストの更新

PropertyクラスとDiceクラスの変更により、既存テストの更新が必要です。

---

**作成日**: 2025-11-22
