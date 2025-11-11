# 技術選定（段階的アプローチ）

## プロジェクトの全体像

このプロジェクトは段階的に発展する**研究プラットフォーム**です：

```
Phase 1-12: シミュレーション研究基盤
    ↓ AI戦略を開発・洗練
Phase 13: Web UI（可視化ダッシュボード）
    ↓ AI戦略が完成
Phase 14+: 人間 vs AI のリアルタイム対戦（将来構想）
    ↓ 人間の行動データを収集
Phase 15+: 行動経済学分析（将来構想）
```

---

## 選定方針

1. **シンプルに始める（YAGNI原則）**: Phase 1-7はKotlinのみ、複雑度を最小化
2. **実測に基づく判断**: Phase 8でベンチマーク後、必要に応じて技術追加
3. **型安全性・保守性最優先**: 保守性 > パフォーマンス > 開発速度
4. **曳光弾開発**: 早期に動くものを作り、学習しながら機能追加
5. **段階的な投資**: 問題が発生してから解決策を導入

---

## 段階的な技術選定戦略

### 基本方針: Kotlinスタート → 実測 → 必要に応じて拡張

```
Phase 1-7: Kotlin のみ
    ↓
Phase 8: パフォーマンス実測
    ↓
目標達成？（10,000ゲーム/分）
    ├─ YES → Kotlin継続
    └─ NO  → Go並列エンジン追加を検討
        ↓
Phase 10: Python（強化学習）追加
    ↓
Phase 13: TypeScript（フロントエンド）追加
```

---

## 主要言語の比較検討

### 比較表

| 観点 | Kotlin | Go | TypeScript | Python |
|------|--------|-----|------------|--------|
| **実行時型安全性** | ✅ | ✅ | ❌ | ❌ |
| **Null安全性** | ✅✅ | ✅ | △ | ❌ |
| **ボイラープレート** | 少ない | 最少 | 少ない | 少ない |
| **開発速度** | ✅ | ✅ | ✅ | ✅✅ |
| **並列処理** | ✅ (Coroutines) | ✅✅ (goroutine) | △ (Worker Threads) | ❌ (GIL) |
| **パフォーマンス** | ✅ | ✅✅ | △ | ❌ |
| **Phase 13統合** | △ | △ | ✅✅ | ❌ |
| **強化学習** | ❌ | ❌ | △ | ✅✅ |
| **エコシステム** | ✅ (JVM) | ✅ | ✅ (npm) | ✅✅ (ML) |
| **デプロイ** | △ (JVM必要) | ✅✅ (単一バイナリ) | △ (Node.js必要) | △ |
| **学習コスト** | 中 | 低 | 低 | 低 |

### 各言語の詳細評価

#### Kotlin ⭐️（Phase 1-7で選定）
**メリット**:
- ✅✅ **言語レベルのNull安全性**（`?`による明示的なnullable型）
- ✅ **実行時型安全性**（JVM）
- ✅ ボイラープレートが少ない（data class、拡張関数等）
- ✅ Coroutinesで並列処理が簡潔（Phase 8で検証）
- ✅ Javaとの相互運用性（JVMエコシステムを利用）
- ✅ 関数型プログラミングのサポート
- ✅ 成熟したテストフレームワーク（Kotest）

**デメリット**:
- △ Goほど並列処理がシンプルではない
- △ JVM起動コスト
- △ シングルバイナリ配布不可

**Phase 1-7での位置付**:
- **全てをKotlinで実装**
- ゲームロジック、戦略、シミュレーション、レポート生成
- 保守性を最優先した選択

**Phase 8での評価基準**:
- Kotlin Coroutinesで10,000ゲーム/分達成できるか？
- 達成できない場合のみGo追加を検討

**Kotlinの主要機能例**:

```kotlin
// Null安全性
var player: Player? = null
player?.getName() // 安全な呼び出し
player!!.getName() // 明示的な非null保証

// data class（ボイラープレート削減）
data class Player(
    val id: String,
    val cash: Int,
    val position: Int
)

// Coroutines（並列処理）
suspend fun runSimulations(count: Int): List<GameResult> {
    return coroutineScope {
        (1..count).map { async { playGame() } }.awaitAll()
    }
}

// 拡張関数
fun Player.canAfford(price: Int): Boolean = cash >= price

// when式（パターンマッチング）
when (val space = board.getSpace(position)) {
    is PropertySpace -> handleProperty(space)
    is TaxSpace -> payTax(space.amount)
    else -> {}
}
```

**総合評価**: 保守性とバランスが最も優れており、Phase 1-7のベストチョイス

---

#### Go（Phase 8で必要に応じて追加）
**メリット**:
- ✅✅ **並列処理が極めて簡単**（goroutine/channel）
- ✅✅ **パフォーマンスが最高レベル**
- ✅✅ **シングルバイナリ**でデプロイが簡単
- ✅ シンプルな言語仕様、学習コスト低
- ✅ メモリ効率が良い
- ✅ コンパイルが高速

**デメリット**:
- △ Kotlinほど高機能ではない
- △ エラーハンドリングが冗長（`if err != nil`）
- △ Null安全性はKotlinより弱い
- △ エコシステムがJVMより小さい

**Phase 8以降での位置付**:
- **Kotlinでパフォーマンス不足の場合のみ追加**
- 並列実行エンジンとして独立したサービス
- KotlinコアとgRPCで通信

**Goの並列処理例**:

```go
// 信じられないほどシンプルな並列実行
func runSimulations(count int) []GameResult {
    results := make(chan GameResult, count)
    
    for i := 0; i < count; i++ {
        go func() {  // ← たったこれだけ！
            results <- playGame()
        }()
    }
    
    // 結果を収集
    var gameResults []GameResult
    for i := 0; i < count; i++ {
        gameResults = append(gameResults, <-results)
    }
    return gameResults
}
```

**総合評価**: 並列処理・パフォーマンスが必要な場合の最適解、Phase 8で追加を検討

---

#### TypeScript（Phase 13で選定）
**メリット**:
- ✅ フロントエンドのデファクトスタンダード
- ✅ React、Viteとの相性が最高
- ✅ エコシステムが豊富
- ✅ 学習コスト低
- ✅ 開発速度が速い

**デメリット**:
- ❌ 実行時の型チェックなし（`any`や型アサーションで回避可能）
- ❌ 並列処理がKotlin/Goより制限的
- ❌ Null安全性がKotlinより弱い

**Phase 13での位置付**:
- **フロントエンド専用**
- React + Vite
- Kotlin/Goバックエンドと通信（REST/WebSocket）

**総合評価**: フロントエンドには最適だが、バックエンドには不向き

---

#### Python（Phase 10で選定）
**メリット**:
- ✅✅ **強化学習ライブラリが最も豊富**（PyTorch、TensorFlow等）
- ✅ 深層強化学習のデファクトスタンダード
- ✅ 開発速度が最も速い

**デメリット**:
- ❌ 型安全性が弱い（mypyでカバー可能だが不完全）
- ❌ GILで並列処理が制限される
- ❌ 実行時エラーが多い
- ❌ 保守性が低い

**Phase 10での位置付**:
- **強化学習専用**（PyTorch、DQN、PPO、World Model）
- Kotlinと標準インターフェース（JSON/gRPC）で接続
- 独立したプロセスとして分離（保守性の低さを封じ込め）

**Python側の保守性向上策**:

```python
# 型ヒントを徹底
from typing import Protocol
from dataclasses import dataclass

@dataclass
class GameState:
    player_cash: int
    position: int
    properties_owned: list[int]

@dataclass
class Action:
    action_type: str  # "buy", "pass", "bid"
    amount: int | None = None

class Agent(Protocol):
    def decide(self, state: GameState) -> Action:
        ...

# mypy, ruffで静的型チェック
```

**総合評価**: 強化学習には必須だが、ドメイン層には不適

---

## 最終的な技術スタック（段階的アプローチ）

### Phase 1-7: Kotlinモノリス

#### プログラミング言語
**選定: Kotlin**

**選定理由**:
1. **Null安全性**: 言語レベルでnullable型を明示（`String?`）、コンパイル時にチェック
2. **保守性最優先**: 複雑なゲームロジックを正確に実装
3. **型安全性**: 実行時も型安全（JVM）
4. **開発速度**: data class、immutability、拡張関数で保守しやすいコード
5. **Coroutines**: 並列処理もKotlinで実装可能（Phase 8で検証）
6. **エコシステム**: JVMの成熟したエコシステムを利用可能

**実装範囲**:
- ゲームエンジン（全ルール実装）
- ドメインロジック
- 戦略パターン
- シミュレーション実行（シングル・並列）
- レポート生成（HTML、CSV）
- 統計収集・可視化

---

### Phase 8: パフォーマンス評価・判断フェーズ

#### ベンチマーク実施

**目標**: 10,000ゲーム/分

**Kotlin Coroutinesでの実装**:
```kotlin
suspend fun runParallelSimulations(count: Int): List<GameResult> {
    return coroutineScope {
        (1..count).map { 
            async(Dispatchers.Default) { 
                playGame() 
            } 
        }.awaitAll()
    }
}

// ベンチマーク
fun benchmark() {
    val start = System.currentTimeMillis()
    val results = runBlocking {
        runParallelSimulations(10_000)
    }
    val duration = System.currentTimeMillis() - start
    val gamesPerMinute = (10_000 / (duration / 1000.0)) * 60
    
    println("Performance: $gamesPerMinute games/minute")
}
```

#### 判断基準

```
ベンチマーク結果 >= 10,000ゲーム/分?
    │
    ├─ YES → Kotlin継続
    │        Phase 9-12へ進む
    │
    └─ NO  → Go並列エンジン追加を検討
             ↓
             詳細な原因分析
             ↓
             選択肢:
             1. Kotlinの最適化を試す
             2. Goで並列エンジンを実装
             3. 目標を調整
```

#### Go追加の場合の設計（必要な場合のみ）

**アーキテクチャ**:
```
┌──────────────────┐
│ Go               │
│ Simulation Engine│  ← 並列実行エンジン（goroutine）
└────────┬─────────┘
         │ gRPC
         ▼
┌──────────────────┐
│ Kotlin           │
│ Game Core        │  ← ゲームロジック（保守性重視）
└──────────────────┘
```

**Go側の実装例**:
```go
// simulation-engine/main.go
func RunParallelSimulations(count int) []Result {
    results := make(chan Result, count)
    
    // Goroutineで並列実行
    for i := 0; i < count; i++ {
        go func() {
            // Kotlin Game Coreを呼び出し（gRPC）
            result := callGameEngine()
            results <- result
        }()
    }
    
    return collectResults(results, count)
}
```

**プロジェクト構成（Go追加時）**:
```
monopoly/
├── core/              # Kotlin（既存）
│   ├── src/main/kotlin/
│   │   └── com/monopoly/
│   │       ├── domain/
│   │       ├── strategy/
│   │       └── grpc/      # gRPCサーバー
│   └── build.gradle.kts
└── sim-engine/        # Go（新規）
    ├── main.go
    ├── worker.go       # Goroutineワーカー
    ├── client.go       # gRPCクライアント
    └── go.mod
```

**所要時間**: 1週間程度

**注意**: この追加は**Phase 8でパフォーマンス不足が判明した場合のみ**実施

---

#### ビルドツール（Phase 1-7）
**選定: Gradle（Kotlin DSL）**

**選定理由**:
- ✅ Kotlinで設定を記述（型安全な設定）
- ✅ 柔軟性が高い
- ✅ インクリメンタルビルド、ビルドキャッシュで高速
- ✅ プラグインが豊富

---

#### テストフレームワーク
**選定: Kotest + MockK**

**選定理由**:
- ✅ **Kotest**: Kotlinネイティブなテストフレームワーク、流暢なDSL
- ✅ **MockK**: Kotlinのモックライブラリ、Coroutines対応
- ✅ 表現力の高いアサーション
- ✅ データ駆動テスト、プロパティベーステストをサポート

**注意**: Gradle Initは現時点でKotestを直接サポートしていないため、初期化後にJUnit 5からKotestに手動で切り替えます。

**Kotestの例**:
```kotlin
class GameEngineTest : StringSpec({
    "player should buy property when they have enough cash" {
        val player = Player("P1", cash = 1500, position = 0)
        val property = Property("Mediterranean Avenue", price = 60)
        
        player.buyProperty(property)
        
        player.cash shouldBe 1440
        player.properties shouldContain property
    }
    
    "player should not buy property when cash is insufficient" {
        val player = Player("P1", cash = 50, position = 0)
        val property = Property("Mediterranean Avenue", price = 60)
        
        shouldThrow<InsufficientFundsException> {
            player.buyProperty(property)
        }
    }
})
```

---

#### HTMLテンプレートエンジン
**選定: kotlinx.html**

**選定理由**:
- ✅ KotlinのDSLでタイプセーフにHTML生成
- ✅ シンプル、外部テンプレートファイル不要
- ✅ Phase 2-12の静的HTML生成に最適

**例**:
```kotlin
fun generateGameReport(game: Game): String = html {
    head {
        title { +"Monopoly Game Report" }
    }
    body {
        h1 { +"Game Results" }
        table {
            tr {
                th { +"Player" }
                th { +"Final Cash" }
            }
            game.players.forEach { player ->
                tr {
                    td { +player.name }
                    td { +player.cash.toString() }
                }
            }
        }
    }
}.toString()
```

---

#### JSON/CSVライブラリ
**選定: kotlinx.serialization（JSON）+ kotlin-csv（CSV）**

**選定理由**:
- ✅ **kotlinx.serialization**: Kotlin公式、型安全、高速
- ✅ **kotlin-csv**: シンプルで使いやすい

**代替案**:
- Jackson（Javaエコシステム、Kotlinでも使用可能）

---

#### ロギング
**選定: kotlin-logging + SLF4J + Logback**

**選定理由**:
- ✅ **kotlin-logging**: KotlinのためのSLF4Jラッパー
- ✅ 遅延評価でパフォーマンス向上

**例**:
```kotlin
private val logger = KotlinLogging.logger {}

logger.info { "Player ${player.name} bought ${property.name}" }
```

---

---

### Phase 10: 強化学習エージェント（Python追加）

#### プログラミング言語
**選定: Python（PyTorch）**

**選定理由**:
1. **PyTorchエコシステム**: 深層強化学習のデファクトスタンダード
2. **ライブラリ**: DQN、PPO、World Model等の実装が豊富
3. **研究事例**: 学習資料やコミュニティが充実

**実装範囲**:
- DQN（Deep Q-Network）
- PPO（Proximal Policy Optimization）
- World Model（環境のモデル化）

**重要**: Pythonは強化学習専用。ゲームロジックはKotlinに保持。

---

#### Kotlin ↔ Python 統合方法

**選定: JSON over stdin/stdout → gRPC（必要に応じて）**

**Phase 10初期（シンプル）**:
```kotlin
// Kotlinから呼び出し
suspend fun callPythonAgent(gameState: GameState): Action {
    val process = ProcessBuilder("python", "agent.py")
        .redirectInput(ProcessBuilder.Redirect.PIPE)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    
    val json = Json.encodeToString(gameState)
    process.outputStream.write(json.toByteArray())
    process.outputStream.close()
    
    val response = process.inputStream.bufferedReader().readText()
    return Json.decodeFromString<Action>(response)
}
```

```python
# Python側
import sys
import json

def decide(game_state: dict) -> dict:
    # PyTorchモデルで推論
    action = model.predict(game_state)
    return {"action": action}

if __name__ == "__main__":
    game_state = json.load(sys.stdin)
    action = decide(game_state)
    json.dump(action, sys.stdout)
```

**Phase 10後期（高速化が必要な場合）**:
- gRPCで通信（バイナリプロトコル、双方向ストリーミング）
- ONNX変換（学習後、推論のみKotlinで実行）

---

#### Python側の保守性向上策

```python
# 型ヒントを徹底
from typing import Protocol
from dataclasses import dataclass

@dataclass
class GameState:
    player_cash: int
    position: int
    properties_owned: list[int]

@dataclass
class Action:
    action_type: str  # "buy", "pass", "bid"
    amount: int | None = None

class Agent(Protocol):
    def decide(self, state: GameState) -> Action:
        ...

# mypy, ruffで静的型チェック
# pyproject.toml で設定
```

---

### Phase 13: Web UI（TypeScript追加）

#### バックエンドAPI

**選定: Ktor（Kotlin）**

**選定理由**:
- ✅ Kotlinネイティブ、Coroutines対応
- ✅ 軽量、シンプル
- ✅ Phase 1-12のKotlinコードをそのまま再利用
- ✅ WebSocketのサポート（リアルタイム通信）

**代替案**:
- Spring Boot: より機能豊富だが、Phase 13には過剰
- Javalin: シンプルだが、KtorよりKotlinサポートが弱い

**Ktorの例**:
```kotlin
fun Application.module() {
    routing {
        get("/api/games") {
            val games = gameRepository.findAll()
            call.respond(games)
        }
        
        webSocket("/ws/game/{gameId}") {
            val gameId = call.parameters["gameId"]!!
            // リアルタイム通信
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val action = Json.decodeFromString<Action>(frame.readText())
                        // ゲームアクションを処理
                    }
                    else -> {}
                }
            }
        }
    }
}
```

**Phase 14以降での選択肢**:
- リアルタイム通信の負荷が高い場合、GoでWebSocketサーバーを追加することも検討可能

---

#### フロントエンド

**選定: React + TypeScript**

**選定理由**:
- ✅ エコシステムが最も豊富
- ✅ TypeScript対応が優れている
- ✅ コンポーネント指向で再利用性が高い
- ✅ 学習目的に適している

---

#### ビルドツール

**選定: Vite**

**選定理由**:
- ✅ 高速（esbuildベース）
- ✅ HMRが優れている
- ✅ TypeScript、Reactのネイティブサポート

---

#### Kotlin ↔ TypeScript 型定義の共有

**選定: OpenAPI Generator**

**方法**:
1. KotorでOpenAPI仕様を自動生成
2. OpenAPI GeneratorでTypeScript型定義を生成
3. フロントエンドで型安全にAPIを呼び出し

**例**:
```kotlin
// Kotlin（Ktor + OpenAPI plugin）
get("/api/games") {
    // OpenAPI仕様が自動生成される
}
```

```typescript
// TypeScript（自動生成された型）
import { Game, GameAPI } from './generated/api';

const games: Game[] = await GameAPI.getGames();
```

---

## 開発ツール

### リンター・フォーマッター（Kotlin）

**選定: ktlint + detekt**

**選定理由**:
- ✅ **ktlint**: Kotlinの公式コーディング規約に準拠
- ✅ **detekt**: 静的解析、コードの問題を検出
- ✅ Gradleプラグインで統合可能

---

### リンター・フォーマッター（TypeScript）

**選定: Biome**

**選定理由**:
- ✅ 高速（Rustで実装）
- ✅ リンター+フォーマッターが統合
- ✅ 設定不要

---

### バージョン管理

**選定: Git + GitHub**

**選定理由**:
- ✅ 業界標準
- ✅ GitHub Actionsで CI/CD が可能

---

## フェーズ別技術マップ

| フェーズ | 主要技術 | 追加技術 | 言語数 |
|---------|---------|---------|--------|
| Phase 1-4（MVP） | Kotlin, Gradle, Kotest | kotlinx.html（HTML生成） | 1 |
| Phase 5-7（シミュレーション基盤） | Kotlin Coroutines | kotlinx.serialization（統計出力） | 1 |
| Phase 8（パフォーマンス評価） | Kotlin Coroutines（ベンチマーク） | **Go（条件付き追加）** | 1-2 |
| Phase 9-12（詳細分析） | Kotlin | Chart.js（可視化） | 1-2 |
| Phase 10（強化学習） | **+ Python（PyTorch）** | gRPC（高速化時） | 2-3 |
| Phase 13（Web UI） | **+ TypeScript（React）** + Ktor | Vite, WebSocket | 3-4 |
| Phase 14+（将来構想） | リアルタイム対戦、行動経済学分析 | **Go WebSocket（条件付き）** | 3-4 |

**注意事項**:
- Phase 1-7: Kotlin のみ（シンプル）
- Phase 8: パフォーマンス不足の場合のみGo追加
- Phase 10: 強化学習でPython必須
- Phase 13: フロントエンドでTypeScript必須
- Phase 14+: リアルタイム負荷が高い場合のみGo WebSocket追加

---

## プロジェクト構成

### Phase 1-7: Kotlinモノリス

```
monopoly/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   └── kotlin/
│   │   │       └── com/
│   │   │           └── monopoly/
│   │   │               ├── domain/          # ドメイン層（ゲームロジック）
│   │   │               │   ├── model/       # エンティティ、値オブジェクト
│   │   │               │   ├── event/       # ドメインイベント
│   │   │               │   └── service/     # ドメインサービス
│   │   │               ├── strategy/        # 戦略パターン
│   │   │               │   ├── Strategy.kt
│   │   │               │   ├── AlwaysBuyStrategy.kt
│   │   │               │   └── RandomStrategy.kt
│   │   │               ├── simulation/      # シミュレーション実行
│   │   │               │   ├── SimulationEngine.kt
│   │   │               │   └── StatisticsCollector.kt
│   │   │               ├── report/          # HTML/CSV生成
│   │   │               │   ├── HtmlReportGenerator.kt
│   │   │               │   └── CsvExporter.kt
│   │   │               └── cli/             # CLIエントリーポイント
│   │   │                   └── Main.kt
│   │   └── test/
│   │       └── kotlin/
│   │           └── com/
│   │               └── monopoly/
│   │                   ├── domain/
│   │                   └── simulation/
│   └── build.gradle.kts
├── settings.gradle.kts
└── gradle/
```

---

### Phase 8: Go並列エンジン追加（条件付き）

**注意**: パフォーマンス不足が判明した場合のみ追加

```
monopoly/
├── core/                      # Kotlin（Phase 1-7）
│   ├── app/
│   │   ├── src/main/kotlin/
│   │   │   └── com/monopoly/
│   │   │       ├── domain/
│   │   │       ├── strategy/
│   │   │       └── grpc/          # gRPCサーバー（新規追加）
│   │   └── build.gradle.kts
│   └── settings.gradle.kts
│
└── sim-engine/                # Go（新規）
    ├── main.go                # エントリーポイント
    ├── worker.go              # Goroutineワーカー
    ├── client.go              # gRPCクライアント
    ├── collector.go           # 結果収集
    └── go.mod
```

---

### Phase 10: Python統合

```
monopoly/
├── core/                      # Kotlinコア（Phase 1-12）
│   └── src/main/kotlin/...
├── rl-agents/                 # Python強化学習エージェント
│   ├── agents/
│   │   ├── dqn_agent.py      # DQN実装
│   │   ├── ppo_agent.py      # PPO実装
│   │   └── world_model.py    # World Model実装
│   ├── training/
│   │   └── train.py          # 学習スクリプト
│   ├── models/               # 学習済みモデル
│   │   └── dqn_model.pth
│   ├── interface/            # Kotlin連携用
│   │   └── agent_server.py   # JSON/gRPC server
│   └── pyproject.toml        # Python依存関係
└── scripts/
    └── run_with_python_agent.sh
```

---

### Phase 13: モノレポ構成

```
monopoly/
├── backend/                   # Kotlinバックエンド（Ktor）
│   ├── src/
│   │   └── main/
│   │       └── kotlin/
│   │           └── com/
│   │               └── monopoly/
│   │                   ├── api/        # REST API
│   │                   │   ├── GameController.kt
│   │                   │   └── StatisticsController.kt
│   │                   └── websocket/  # WebSocket
│   │                       └── GameWebSocket.kt
│   └── build.gradle.kts
├── frontend/                  # TypeScriptフロントエンド（React）
│   ├── src/
│   │   ├── components/
│   │   │   ├── Board.tsx
│   │   │   ├── PlayerPanel.tsx
│   │   │   └── StatisticsChart.tsx
│   │   ├── pages/
│   │   │   ├── GamePage.tsx
│   │   │   └── AnalyticsPage.tsx
│   │   ├── api/              # 自動生成されたAPI型
│   │   │   └── generated/
│   │   └── App.tsx
│   ├── package.json
│   └── vite.config.ts
├── core/                      # Phase 1-12の共通コード
│   └── src/main/kotlin/com/monopoly/domain/
└── rl-agents/                 # Python強化学習エージェント
    └── ...
```

---

## 依存パッケージ

### Phase 1-4で必要な依存関係

**build.gradle.kts:**
```kotlin
plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
}

dependencies {
    // Kotlin標準ライブラリ
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // ロギング
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    
    // HTML生成
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.9.1")
    
    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // テスト
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.mockk:mockk:1.13.8")
}

tasks.test {
    useJUnitPlatform()
}
```

---

### Phase 10で追加（Python）

**pyproject.toml:**
```toml
[project]
name = "monopoly-rl"
version = "0.1.0"
requires-python = ">=3.11"

dependencies = [
    "torch>=2.0.0",
    "numpy>=1.24.0",
    "gymnasium>=0.29.0",  # OpenAI Gym後継
]

[project.optional-dependencies]
dev = [
    "mypy>=1.7.0",
    "ruff>=0.1.0",
    "pytest>=7.4.0",
]
```

---

### Phase 13で追加（バックエンド）

**build.gradle.kts:**
```kotlin
dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-websockets:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    
    // OpenAPI
    implementation("io.ktor:ktor-server-openapi:2.3.7")
}
```

---

### Phase 13で追加（フロントエンド）

**package.json:**
```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@vitejs/plugin-react": "^4.2.0",
    "typescript": "^5.3.0",
    "vite": "^5.0.0",
    "@biomejs/biome": "^1.4.0"
  }
}
```

---

## 技術選定の変更方針

この技術選定は**暫定**であり、以下の場合に見直します：

1. **パフォーマンス不足**: Phase 8でベンチマーク結果が目標（10,000ゲーム/分）に達しない場合
2. **開発効率の低下**: 選定した技術で開発が著しく滞る場合
3. **より良い選択肢の出現**: 新しい技術やライブラリが登場した場合

**変更の判断基準:**
- 各フェーズ完了時の振り返りで評価
- 具体的な問題が発生した場合に検討
- メリット・デメリットを文書化して判断

---

## 技術選定の根拠まとめ

### なぜ「Kotlinスタート → 必要に応じてGo追加」か？

1. **YAGNI原則（You Aren't Gonna Need It）**
   - 問題が起きていないのに解決しない
   - Phase 8で実測してから判断
   
2. **保守性を最優先**
   - ゲームロジックの正確性が最重要
   - Kotlinの型安全性・Null安全性でコアを実装
   
3. **リスク最小化**
   - 1言語から始めてシンプルに
   - 複雑度を段階的に追加
   
4. **データに基づく判断**
   - 推測ではなく、ベンチマーク結果で判断
   - 10,000ゲーム/分達成できなければGo追加

### なぜKotlinか？（Phase 1-7のコア言語）

1. **保守性 > パフォーマンス**（プロジェクトの最優先事項）
   - 言語レベルのNull安全性（`?`による明示的なnullable型）
   - data class、immutabilityで保守しやすいコード
   - 拡張関数、高階関数で読みやすいコード

2. **Javaの堅牢性 + TypeScriptの開発速度**
   - 実行時型安全性（JVM）
   - ボイラープレートが少ない
   
3. **Coroutinesで並列処理が可能**（Phase 8で検証）
   - 並列実行がKotlinでも実装可能
   - まずKotlinで試し、不足があればGo

4. **エコシステムが成熟**
   - JVMエコシステムを利用可能
   - Gradle、テストツール、ライブラリが充実

### なぜGo？（Phase 8で条件付き追加）

1. **並列処理が極めて簡単**
   - goroutine/channelで直感的に並列化
   - Kotlinより記述量が少ない

2. **パフォーマンスが最高レベル**
   - コンパイル言語で高速
   - メモリ効率が良い

3. **追加が容易**
   - gRPCでKotlinコアと接続
   - 並列実行エンジンとして独立
   
4. **条件付き追加の理由**
   - Phase 8でパフォーマンス不足が判明した場合のみ
   - 不要な複雑度を避ける

### なぜTypeScriptか？（フロントエンド）

1. **フロントエンドのデファクトスタンダード**
   - React、Viteとの相性が良い
   - エコシステムが最も豊富

2. **学習目的に適している**
   - フロントエンド学習が今回の目的の1つ

3. **Phase 13での統合**
   - OpenAPI Generatorで型定義を共有可能

### なぜPythonか？（強化学習のみ）

1. **深層強化学習のデファクトスタンダード**
   - PyTorchエコシステムが最も充実
   - DQN、PPO、World Modelの実装事例が豊富

2. **専用プロセスとして分離**
   - Pythonの保守性の低さを限定的な範囲に封じ込める
   - Kotlinとは標準インターフェースで接続

3. **Phase 10以降の追加**
   - Phase 1-9はKotlinのみで完結
   - 必要になってから導入

---

## 次のアクション

1. ✅ プロジェクト仕様の策定（完了）
2. ✅ 開発計画の策定（完了）
3. ✅ 技術選定（完了）
4. **⏭️ 開発環境のセットアップ**
   - JDK 21+ インストール確認
   - IntelliJ IDEA セットアップ
   - Gradle Init でプロジェクト作成
   - Kotestへの切り替え
5. Phase 1の詳細設計とタスク分解
6. Phase 1の実装開始

**重要**: Phase 1-7はKotlinのみ。シンプルに始めることが成功の鍵。

---

**作成日**: 2025-11-11
**最終更新**: 2025-11-11