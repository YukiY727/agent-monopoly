# ADR-0002: イベント駆動アーキテクチャの採用

**ステータス**: 承認済み
**日付**: 2025-11-12
**最終更新**: 2025-11-15
**決定者**: プロジェクトアーキテクト
**関連ADR**: ADR-0001 (モノリスアーキテクチャ)

---

## 決定 (Decision)

**選択**: **簡易イベントログによるイベント駆動アーキテクチャを段階的に導入**

- **Phase 1-12**: シンプルなイベントログ（単純イベントパターン）
- **Phase 13**: WebSocketによるブロードキャストパターン追加
- **将来**: 必要に応じてより高度なパターン（メディエーター、Saga等）を検討

---

## コンテキスト (Context)

### 状況

研究プラットフォームとして、以下の要件があります：

- **完全な履歴**: ゲームの全イベントを記録
- **再現性**: 任意時点の状態を再構築可能
- **将来の分析拡張**: 後から新しい統計分析を追加可能
- **Phase 13でのリアルタイム通信**: WebSocketでゲーム状態をリアルタイム配信

### プロジェクトの価値観との関連

```
保守性 > パフォーマンス > 開発速度
```

イベント駆動アーキテクチャは**保守性**（拡張性）と**パフォーマンス**（非同期処理）に影響します。

---

## イベント駆動アーキテクチャのパターン

イベント駆動アーキテクチャには複数のトポロジーと通信パターンがあります。

### トポロジー比較

| トポロジー | 説明 | 中央調整 | 複雑度 | 適用例 |
|----------|------|---------|-------|--------|
| **ブローカー** | イベントブローカーが中継 | なし | 低 | 単純なイベント伝播 |
| **メディエーター** | メディエーターが調整 | あり | 高 | 複雑なワークフロー |

### 通信パターン比較

| パターン | 説明 | 応答 | 順序保証 | 複雑度 |
|---------|------|------|---------|-------|
| **単純イベント** | Fire-and-Forget | なし | なし | 低 |
| **ブロードキャスト** | Pub-Sub | なし | なし | 低 |
| **リクエスト・リプライ** | 同期的な要求・応答 | あり | あり | 中 |
| **ワークフロー委譲** | Saga Pattern | あり | あり | 高 |

---

## 選択肢の詳細分析

### トポロジー1: ブローカートポロジー (Broker Topology)

#### 説明

イベントブローカー（Kafka、RabbitMQ等）が中心となり、イベントを各サブスクライバーに配信します。
中央調整者は存在せず、各コンポーネントが独立してイベントを処理します。

```
┌─────────┐      ┌─────────────┐      ┌─────────┐
│Publisher│─────>│Event Broker │─────>│Subscriber│
└─────────┘      │  (Kafka)    │      └─────────┘
                 └─────────────┘
                        │
                        └─────────>┌─────────┐
                                   │Subscriber│
                                   └─────────┘
```

#### メリット (Pros)

- ✅ **疎結合**: パブリッシャーとサブスクライバーが独立
- ✅ **スケーラビリティ**: サブスクライバーを独立してスケール
- ✅ **柔軟性**: 新しいサブスクライバーを容易に追加
- ✅ **非同期処理**: 高スループット

#### デメリット (Cons)

- ❌ **順序保証の困難**: イベント順序の管理が複雑
- ❌ **エラーハンドリング**: 分散エラー処理が必要
- ❌ **デバッグ困難**: イベントフローの追跡が難しい
- ❌ **インフラ必要**: ブローカー（Kafka等）の運用コスト

#### このプロジェクトでの評価

**❌ Phase 1-12では不採用**

**理由**:
- モノリスアーキテクチャではブローカーは過剰
- Phase 1-12ではリアルタイム処理不要
- インフラ運用コストが高い

**将来の検討**:
- Phase 14以降で大規模なリアルタイム分析が必要になったら検討
- 数千のゲームを同時実行し、リアルタイム統計が必要な場合

---

### トポロジー2: メディエータートポロジー (Mediator Topology)

#### 説明

メディエーター（オーケストレーター）が中央でワークフローを調整します。
イベントの順序、エラーハンドリング、補償処理を一元管理します。

```
┌─────────┐      ┌──────────────┐      ┌─────────┐
│Publisher│─────>│  Mediator    │─────>│Service A│
└─────────┘      │(Orchestrator)│      └─────────┘
                 └──────────────┘
                        │
                        ├─────────>┌─────────┐
                        │          │Service B│
                        │          └─────────┘
                        └─────────>┌─────────┐
                                   │Service C│
                                   └─────────┘
```

#### メリット (Pros)

- ✅ **ワークフロー管理**: 複雑なワークフローを一元管理
- ✅ **エラーハンドリング**: 補償処理、リトライを管理
- ✅ **順序保証**: イベント順序を確実に管理
- ✅ **デバッグ容易**: 中央でフローを追跡可能

#### デメリット (Cons)

- ❌ **複雑度**: メディエーターの実装が複雑
- ❌ **単一障害点**: メディエーターがボトルネックになる可能性
- ❌ **結合度**: メディエーターが全ワークフローを知る必要
- ❌ **スケーラビリティ**: メディエーターのスケーリングが必要

#### このプロジェクトでの評価

**❌ Phase 1-12では不採用**

**理由**:
- モノリス内では不要（直接呼び出しで十分）
- Phase 1-12では複雑なワークフローがない
- 複雑度が開発速度を阻害

**将来の検討**:
- Phase 10で強化学習エージェント連携時に検討
- 複雑な分散トランザクションが必要になったら検討

---

## 通信パターンの詳細分析

### パターン1: 単純イベント (Simple Event / Fire-and-Forget)

#### 説明

イベントを発行するだけで、応答を待たない最もシンプルなパターン。
イベントログに記録し、後から非同期に処理します。

```kotlin
// イベント発行（応答なし）
eventLog.record(
    GameEvent(
        timestamp = System.currentTimeMillis(),
        type = EventType.PROPERTY_PURCHASED,
        playerId = player.id,
        details = mapOf(
            "propertyName" to property.name,
            "price" to property.price
        )
    )
)
```

#### メリット (Pros)

- ✅✅ **最もシンプル**: 実装・理解が容易
- ✅ **高速**: 応答待ちなし
- ✅ **疎結合**: イベント発行者と処理者が独立
- ✅ **拡張性**: 後から新しいイベントハンドラーを追加可能

#### デメリット (Cons)

- ❌ **応答なし**: 処理結果を知る方法がない
- ❌ **順序保証なし**: イベント処理順序が不定
- ❌ **エラーハンドリング**: イベント処理エラーを検知できない

#### このプロジェクトでの評価

**✅ Phase 1-12で採用**

**理由**:
1. **シンプルさ**: 最もシンプルで開発速度が速い
2. **十分な機能**: Phase 1-12ではイベントログ記録が主目的
3. **拡張性**: 後から新しい統計分析を追加可能
4. **YAGNI**: 複雑な機能は不要

**実装**:
```kotlin
data class GameEvent(
    val timestamp: Long,
    val type: EventType,
    val playerId: String?,
    val details: Map<String, Any>
)

class EventLog {
    private val events = mutableListOf<GameEvent>()

    fun record(event: GameEvent) {
        events.add(event)
    }

    fun getEvents(): List<GameEvent> = events.toList()

    fun save(path: String) {
        File(path).writeText(
            Json.encodeToString(events)
        )
    }
}
```

---

### パターン2: ブロードキャスト (Broadcast / Pub-Sub)

#### 説明

1つのイベントを複数のサブスクライバーに同時配信します。
WebSocketでリアルタイム通信を実現します。

```
Publisher
    │
    ├─────> Subscriber 1 (Web UI)
    ├─────> Subscriber 2 (Analytics)
    └─────> Subscriber 3 (Logger)
```

#### メリット (Pros)

- ✅ **リアルタイム配信**: 複数クライアントに即座に配信
- ✅ **疎結合**: パブリッシャーはサブスクライバーを知らない
- ✅ **柔軟性**: サブスクライバーを動的に追加・削除
- ✅ **スケーラビリティ**: サブスクライバーを独立してスケール

#### デメリット (Cons)

- ❌ **順序保証なし**: サブスクライバー間で順序不定
- ❌ **配信保証なし**: サブスクライバーがダウンしても気づかない
- ❌ **ネットワーク負荷**: 全サブスクライバーに配信するコスト

#### このプロジェクトでの評価

**✅ Phase 13で採用**

**理由**:
1. **リアルタイムUI**: Web UIでゲーム進行をリアルタイム表示
2. **WebSocket**: Ktorで簡単に実装可能
3. **必要十分**: Phase 13の要件を満たす

**実装計画（Phase 13）**:
```kotlin
// Ktor WebSocket
fun Application.configureWebSocket() {
    routing {
        webSocket("/ws/game/{gameId}") {
            val gameId = call.parameters["gameId"]!!

            // イベント購読
            eventBus.subscribe(gameId) { event ->
                send(Frame.Text(Json.encodeToString(event)))
            }

            // クライアントからのメッセージ処理
            for (frame in incoming) {
                // ...
            }
        }
    }
}
```

**Phase 1-12**: 不要（リアルタイム通信なし）

---

### パターン3: リクエスト・リプライ (Request-Reply)

#### 説明

イベントを送信し、応答を待つ同期的なパターン。
非同期メッセージングで同期的な要求・応答を実現します。

```
Client ─────Request────> Service
       <────Reply──────
```

#### メリット (Pros)

- ✅ **応答保証**: 処理結果を確実に取得
- ✅ **エラーハンドリング**: エラーを同期的に処理
- ✅ **順序保証**: 要求・応答の順序が保証される

#### デメリット (Cons)

- ❌ **ブロッキング**: 応答待ちでブロック
- ❌ **タイムアウト管理**: タイムアウト処理が必要
- ❌ **複雑度**: イベント駆動の利点（非同期）を失う

#### このプロジェクトでの評価

**△ 条件付き採用（Phase 10）**

**Phase 1-12**: ❌ 不採用
- モノリス内では直接呼び出しで十分
- イベント駆動の必要性がない

**Phase 10 (Python統合)**: ✅ 採用検討
- Python強化学習エージェントとの通信
- gRPC（同期的なRPC）またはJSON over stdin/stdout

**実装例（Phase 10）**:
```kotlin
// Kotlinから強化学習エージェントに要求
suspend fun requestAction(gameState: GameState): Action {
    val request = ActionRequest(gameState)

    // gRPCまたはJSON over stdin
    val response = pythonClient.requestAction(request)

    return response.action
}
```

---

### パターン4: ワークフロー委譲 (Workflow Delegation / Saga Pattern)

#### 説明

複数のサービスにまたがる長期トランザクションを管理します。
各ステップの成功・失敗を追跡し、失敗時は補償トランザクションを実行します。

```
Saga Coordinator
    │
    ├─> Step 1: Reserve Inventory ────> Success
    ├─> Step 2: Charge Payment    ────> Success
    ├─> Step 3: Ship Order        ────> Failure
    │                                       │
    └─> Compensate Step 2 <─────────────────┘
        └─> Compensate Step 1
```

#### Saga パターンの種類

**Choreography Saga**: 各サービスが次のサービスにイベントを発行
```
Service A ─event─> Service B ─event─> Service C
         <─event─           <─event─
```

**Orchestration Saga**: 中央のオーケストレーターが全体を調整
```
        Orchestrator
         ↙  ↓  ↘
    Svc A  Svc B  Svc C
```

#### メリット (Pros)

- ✅ **分散トランザクション**: 複数サービスの一貫性を保証
- ✅ **補償処理**: 失敗時のロールバックを自動化
- ✅ **長期トランザクション**: 長時間実行されるワークフローを管理

#### デメリット (Cons)

- ❌❌ **最も複雑**: 実装・運用が極めて複雑
- ❌ **デバッグ困難**: 分散ワークフローの追跡が難しい
- ❌ **結果整合性**: 最終的な整合性のみ保証（即座の一貫性なし）
- ❌ **補償設計**: 各ステップの補償ロジックが必要

#### このプロジェクトでの評価

**❌ Phase 1-13では不採用**

**理由**:
1. **モノリス**: モノリス内ではACIDトランザクションで十分
2. **複雑度**: 開発・運用コストが極めて高い
3. **YAGNI**: 現時点で分散トランザクションの必要性なし

**将来の検討**:
- Phase 14以降でマイクロサービス化した場合のみ検討
- 複数サービスにまたがるトランザクションが必要になったら
- 例: ゲームエンジン（Kotlin） + 決済サービス + 通知サービス

---

## トレードオフ分析

### このプロジェクトで採用するパターン

| Phase | パターン | 理由 |
|-------|---------|------|
| **Phase 1-12** | 単純イベント | シンプル、イベントログ記録が主目的 |
| **Phase 13** | + ブロードキャスト | WebSocketでリアルタイムUI |
| **Phase 10** | + リクエスト・リプライ | Python統合（gRPC） |

### 得られるもの

#### Phase 1-12（単純イベント）

- ✅ **完全な履歴**: 全イベントを記録
- ✅ **再現性**: 任意時点の状態を再構築可能
- ✅ **拡張性**: 後から新しい統計分析を追加可能
- ✅ **シンプルさ**: 最小限の実装
- ✅ **監査**: 全変更を追跡可能

#### Phase 13（+ ブロードキャスト）

- ✅ **リアルタイムUI**: ゲーム進行をリアルタイム表示
- ✅ **マルチクライアント**: 複数ブラウザで同時閲覧
- ✅ **リアクティブ**: イベント駆動でUI更新

### 失うもの

- ❌ **クエリの複雑化**: 現在の状態を得るにはイベント再生が必要
- ❌ **ストレージコスト**: 全イベント保存で大容量
- ❌ **リアルタイム保証なし**（Phase 1-12）: 後処理のみ

### 採用しないパターンとその理由

| パターン | 不採用理由 |
|---------|----------|
| **ブローカー** | モノリスには過剰、インフラコスト高 |
| **メディエーター** | 複雑すぎる、直接呼び出しで十分 |
| **Saga** | 分散トランザクション不要、モノリスでACID可能 |

---

## 段階的な導入戦略

### Phase 1-7: シンプルなイベントログ

```kotlin
// イベント定義
enum class EventType {
    DICE_ROLLED,
    PLAYER_MOVED,
    PROPERTY_PURCHASED,
    RENT_PAID,
    PLAYER_BANKRUPT
}

data class GameEvent(
    val timestamp: Long,
    val type: EventType,
    val playerId: String?,
    val details: Map<String, Any>
)

// イベントログ
class EventLog {
    private val events = mutableListOf<GameEvent>()

    fun record(event: GameEvent) {
        events.add(event)
    }

    fun getEvents(): List<GameEvent> = events.toList()

    fun replay(): GameState {
        // イベント再生で状態を再構築
        val state = GameState.initial()
        events.forEach { event ->
            state.apply(event)
        }
        return state
    }
}
```

**特徴**:
- ✅ シンプルな`List<GameEvent>`
- ✅ JSON/CSV形式で保存
- ✅ 後から任意の統計分析を追加可能

---

### Phase 8-12: イベント分析の拡充

```kotlin
// イベントストリーム処理
class EventAnalyzer {
    fun analyzeTurnDuration(events: List<GameEvent>): Statistics {
        return events
            .filter { it.type == EventType.TURN_COMPLETED }
            .map { /* 分析 */ }
            .toStatistics()
    }

    fun analyzePropertyPurchasePatterns(events: List<GameEvent>): Report {
        // イベントストリームから購入パターンを分析
    }
}
```

**特徴**:
- ✅ イベントストリームから統計を生成
- ✅ 新しい分析を後から追加

---

### Phase 13: WebSocketでリアルタイム配信

```kotlin
// イベントバス（Pub-Sub）
class EventBus {
    private val subscribers =
        mutableMapOf<String, MutableSet<suspend (GameEvent) -> Unit>>()

    fun subscribe(gameId: String, handler: suspend (GameEvent) -> Unit) {
        subscribers
            .getOrPut(gameId) { mutableSetOf() }
            .add(handler)
    }

    suspend fun publish(gameId: String, event: GameEvent) {
        subscribers[gameId]?.forEach { handler ->
            handler(event)
        }
    }
}

// WebSocket
fun Application.configureWebSocket() {
    install(WebSockets)

    routing {
        webSocket("/ws/game/{gameId}") {
            val gameId = call.parameters["gameId"]!!

            // イベント購読
            eventBus.subscribe(gameId) { event ->
                send(Frame.Text(Json.encodeToString(event)))
            }

            // 接続維持
            for (frame in incoming) {
                // Keep-alive
            }
        }
    }
}
```

**特徴**:
- ✅ WebSocketでリアルタイム配信
- ✅ 複数クライアントに同時配信
- ✅ ゲーム進行をリアルタイム表示

---

### Phase 10: Python統合（リクエスト・リプライ）

```kotlin
// gRPC クライアント
class PythonAgentClient(private val stub: AgentServiceStub) {
    suspend fun requestAction(gameState: GameState): Action {
        val request = ActionRequest.newBuilder()
            .setGameState(gameState.toProto())
            .build()

        val response = stub.getAction(request)
        return Action.fromProto(response.action)
    }
}
```

**特徴**:
- ✅ 同期的な要求・応答
- ✅ gRPCで型安全な通信
- ✅ タイムアウト管理

---

## 実装計画

### Phase 1-7

1. **EventLogクラス作成**
   - `List<GameEvent>`でイベント保存
   - JSON/CSV形式で永続化

2. **全ゲームロジックにイベント記録**
   - サイコロ、移動、購入、レント、破産

3. **イベント再生機能**
   - 任意時点の状態を再構築

### Phase 8-12

4. **イベント分析機能**
   - イベントストリームから統計生成
   - 新しい分析を追加

### Phase 13

5. **EventBus実装**
   - Pub-Subパターン
   - WebSocketで配信

6. **WebSocket API**
   - Ktor WebSocket
   - リアルタイム配信

### Phase 10

7. **Python統合**
   - gRPCまたはJSON over stdin
   - リクエスト・リプライパターン

---

## 成功基準

- [x] Phase 1-7: 全イベントを記録、JSON/CSV保存
- [ ] Phase 8-12: イベントストリームから統計生成
- [ ] Phase 13: WebSocketでリアルタイム配信
- [ ] Phase 10: Python強化学習エージェントと通信

---

## 見直し基準

以下の場合に、より高度なパターンを検討します：

- **Phase 8完了時**: イベント分析機能の評価
- **Phase 13完了時**: WebSocketパフォーマンスの評価
- **大規模化**: 数千ゲームの同時実行が必要になった場合
  - → ブローカートポロジー（Kafka）を検討
- **マイクロサービス化**: サービスを分割した場合
  - → メディエーター、Sagaパターンを検討

---

## 参考資料

- Martin Fowler, "Event-Driven Architecture"
- Chris Richardson, "Microservices Patterns" (Saga Pattern)
- Greg Young, "CQRS and Event Sourcing"
- [パターン比較](../patterns-comparison.md#データアーキテクチャ)
- [アーキテクチャ原則](../architecture-principles.md#トレードオフの具体例)

---

## まとめ

### 採用パターン

| Pattern | Phase | 理由 |
|---------|-------|------|
| **単純イベント** | 1-12 | シンプル、イベントログ記録 |
| **ブロードキャスト** | 13 | WebSocketでリアルタイムUI |
| **リクエスト・リプライ** | 10 | Python統合（gRPC） |

### トレードオフの受け入れ

**得られるもの**:
- 完全な履歴と再現性
- 後からの分析拡張性
- リアルタイムUI（Phase 13）

**失うもの**:
- クエリのシンプルさ
- ストレージコスト

**判断**: 研究プラットフォームとして得られるものが大きい

### 将来の拡張パス

```
Phase 1-12: 単純イベント
    ↓
Phase 13: + ブロードキャスト
    ↓
将来（必要に応じて）:
    ├─ ブローカー（Kafka）: 大規模リアルタイム分析
    ├─ メディエーター: 複雑なワークフロー
    └─ Saga: マイクロサービス化時の分散トランザクション
```

全ての拡張は**実測に基づいて判断**し、新しいADRとして記録します。

---

**作成日**: 2025-11-12
**最終更新**: 2025-11-15
**承認者**: プロジェクトアーキテクト
