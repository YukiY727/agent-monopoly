# Phase 13: Web UI - フロントエンド方針

## 目的

このドキュメントはPhase 13（Web UI）の技術選定と実装方針を定義します。

## 現状分析

### 既存の資産

**強み**:
- Kotlinで実装された堅牢なゲームエンジン
- 充実した統計計算機能（Phase 6, 9）
- SVGベースの可視化機能（Phase 7, 9, 12）
- 並列実行による高速シミュレーション（Phase 8）
- HTMLレポート生成機能（再利用可能なテンプレート）

**制約**:
- 現在CLIベースでWebサーバー機能なし
- リアルタイム通信の仕組みがない
- ブラウザからのアクセスができない

## 技術選定

### 1. バックエンドフレームワーク

#### 選択肢

| フレームワーク | メリット | デメリット | 推奨度 |
|--------------|---------|----------|--------|
| **Ktor** | Kotlin製、軽量、コルーチンネイティブ、WebSocket対応 | Spring比較で機能少ない | ⭐⭐⭐⭐⭐ |
| **Spring Boot** | 成熟、豊富な機能、大規模プロジェクト向け | 重い、オーバースペック | ⭐⭐⭐ |
| **http4k** | 関数型、軽量、テスト容易 | WebSocketサポート限定的 | ⭐⭐⭐⭐ |

#### 推奨: **Ktor**

**理由**:
- Kotlinネイティブで既存コードとの親和性が高い
- コルーチンを活用した非同期処理（Phase 8と整合）
- WebSocketのファーストクラスサポート
- 軽量で学習曲線が緩やか
- 必要な機能が揃っている（ルーティング、CORS、WebSocket、JSON）

**依存関係追加**:
```kotlin
// build.gradle.kts
dependencies {
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    implementation("io.ktor:ktor-server-websockets:2.3.0")
    implementation("io.ktor:ktor-server-cors:2.3.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
}
```

### 2. フロントエンドフレームワーク

#### 選択肢

| フレームワーク | メリット | デメリット | 推奨度 |
|--------------|---------|----------|--------|
| **React** | 最も普及、豊富なライブラリ、求人多い | 学習曲線やや急 | ⭐⭐⭐⭐⭐ |
| **Vue 3** | 学習容易、日本語ドキュメント充実 | Reactより小規模コミュニティ | ⭐⭐⭐⭐ |
| **Svelte** | 高速、シンプル、バンドルサイズ小 | コミュニティ小、ライブラリ少 | ⭐⭐⭐ |
| **Vanilla JS + Alpine.js** | 依存なし、軽量 | 大規模化困難 | ⭐⭐ |

#### 推奨: **React + TypeScript**

**理由**:
- データ可視化ライブラリが豊富（Recharts, D3.js, Plotly.js）
- WebSocketとの統合が容易
- TypeScriptで型安全性を確保
- 保守性が高い
- 将来的な拡張に対応しやすい

**技術スタック**:
```
- React 18
- TypeScript
- Vite（ビルドツール）
- Recharts（チャート）
- TanStack Query（データフェッチング）
- Zustand（状態管理、軽量）
```

### 3. リアルタイム通信

#### 選択肢

| 技術 | ユースケース | 推奨度 |
|-----|------------|--------|
| **WebSocket** | 双方向通信、進捗更新、リアルタイムダッシュボード | ⭐⭐⭐⭐⭐ |
| **Server-Sent Events (SSE)** | サーバー→クライアント単方向、進捗のみ | ⭐⭐⭐⭐ |
| **HTTP Polling** | シンプル、互換性高い | ⭐⭐ |

#### 推奨: **WebSocket（メイン）+ REST API（設定）**

**使い分け**:
- **WebSocket**: リアルタイム進捗、ゲーム状態更新、統計ストリーム
- **REST API**: 初期設定、戦略一覧取得、レポート取得

## アーキテクチャ設計

### システム構成図

```
┌─────────────────────────────────────────────────────────┐
│                    Browser (React)                      │
│  ┌─────────────────┐  ┌──────────────────────────────┐ │
│  │ 設定画面        │  │ ダッシュボード                │ │
│  │ - 戦略選択      │  │ - リアルタイムグラフ          │ │
│  │ - ゲーム数指定  │  │ - 進捗表示                    │ │
│  │ - パラメータ調整│  │ - 統計サマリー                │ │
│  └─────────────────┘  └──────────────────────────────┘ │
└────────┬─────────────────────────┬────────────────────┘
         │ HTTP (REST)             │ WebSocket
         │                         │
┌────────▼─────────────────────────▼────────────────────┐
│              Ktor Server (Port 8080)                   │
│  ┌──────────────────┐  ┌───────────────────────────┐  │
│  │ REST API         │  │ WebSocket Handler         │  │
│  │ /api/strategies  │  │ /ws/simulation            │  │
│  │ /api/simulate    │  │ - 進捗イベント送信        │  │
│  │ /api/reports/:id │  │ - ゲーム状態ブロードキャスト│ │
│  └──────────────────┘  └───────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │        既存のゲームエンジン (Kotlin)              │  │
│  │  - GameService                                   │  │
│  │  - ParallelGameRunner                            │  │
│  │  - StatisticsCalculator                          │  │
│  │  - DetailedStatisticsCalculator                  │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### ディレクトリ構成

```
agent-monopoly/
├── backend/                    # Kotlin バックエンド（既存）
│   ├── src/main/kotlin/com/monopoly/
│   │   ├── domain/            # ドメインロジック（既存）
│   │   ├── statistics/        # 統計（既存）
│   │   ├── visualization/     # SVG生成（既存）
│   │   ├── api/               # 🆕 REST API
│   │   │   ├── routes/
│   │   │   │   ├── StrategyRoutes.kt
│   │   │   │   ├── SimulationRoutes.kt
│   │   │   │   └── ReportRoutes.kt
│   │   │   └── models/
│   │   │       ├── SimulationRequest.kt
│   │   │       └── SimulationResponse.kt
│   │   ├── websocket/         # 🆕 WebSocket
│   │   │   ├── SimulationWebSocket.kt
│   │   │   └── ProgressEvent.kt
│   │   └── server/            # 🆕 サーバー起動
│   │       └── Application.kt
│   └── build.gradle.kts
│
├── frontend/                   # 🆕 React フロントエンド
│   ├── src/
│   │   ├── components/
│   │   │   ├── SimulationSetup.tsx
│   │   │   ├── Dashboard.tsx
│   │   │   ├── ProgressMonitor.tsx
│   │   │   └── ChartViewer.tsx
│   │   ├── hooks/
│   │   │   ├── useSimulation.ts
│   │   │   └── useWebSocket.ts
│   │   ├── api/
│   │   │   └── client.ts
│   │   ├── types/
│   │   │   └── simulation.ts
│   │   └── App.tsx
│   ├── package.json
│   └── vite.config.ts
│
└── docs/
    └── planning/
        └── phase13-frontend-strategy.md  # このファイル
```

## API設計

### REST API エンドポイント

#### 1. 戦略管理

```http
GET /api/strategies
```

**Response**:
```json
{
  "strategies": [
    {
      "id": "monopoly",
      "displayName": "Monopoly First Strategy",
      "description": "Prioritizes completing color group monopolies",
      "parameters": [
        {
          "name": "blockOpponentMonopoly",
          "type": "boolean",
          "default": true
        },
        {
          "name": "minCashReserve",
          "type": "integer",
          "default": 300,
          "min": 0,
          "max": 1000
        }
      ]
    }
  ]
}
```

#### 2. シミュレーション開始

```http
POST /api/simulation
```

**Request**:
```json
{
  "players": [
    {
      "name": "Alice",
      "strategy": "monopoly",
      "parameters": {
        "blockOpponentMonopoly": true,
        "minCashReserve": 250
      }
    },
    {
      "name": "Bob",
      "strategy": "roi",
      "parameters": {
        "minROI": 0.20
      }
    }
  ],
  "numberOfGames": 1000,
  "parallel": 8
}
```

**Response**:
```json
{
  "simulationId": "sim-abc123",
  "status": "running",
  "websocketUrl": "/ws/simulation/sim-abc123"
}
```

#### 3. シミュレーション結果取得

```http
GET /api/simulation/{id}
```

**Response**:
```json
{
  "simulationId": "sim-abc123",
  "status": "completed",
  "statistics": {
    "totalGames": 1000,
    "playerStats": {
      "Alice": {
        "wins": 550,
        "winRate": 0.55,
        "averageFinalAssets": 2800
      }
    }
  },
  "reportUrl": "/api/reports/sim-abc123"
}
```

#### 4. レポート取得

```http
GET /api/reports/{id}?format=html|json
```

### WebSocket プロトコル

#### 接続

```
ws://localhost:8080/ws/simulation/{simulationId}
```

#### メッセージ形式

**進捗更新**:
```json
{
  "type": "progress",
  "simulationId": "sim-abc123",
  "completed": 250,
  "total": 1000,
  "percentage": 25.0,
  "timestamp": 1234567890
}
```

**中間統計**:
```json
{
  "type": "interim_stats",
  "simulationId": "sim-abc123",
  "completed": 500,
  "stats": {
    "playerStats": {
      "Alice": {
        "wins": 275,
        "winRate": 0.55
      }
    }
  }
}
```

**完了通知**:
```json
{
  "type": "completed",
  "simulationId": "sim-abc123",
  "finalStats": { /* ... */ }
}
```

**エラー通知**:
```json
{
  "type": "error",
  "simulationId": "sim-abc123",
  "message": "Simulation failed: Invalid strategy parameter"
}
```

## 段階的実装計画

### Phase 13-1: バックエンドAPI（MVP）

**目標**: CLIからWeb APIへの移行

**実装内容**:
1. Ktorサーバーセットアップ
2. 基本的なREST API
   - GET /api/strategies
   - POST /api/simulation
   - GET /api/simulation/{id}
3. 既存のGameRunnerをAPI経由で呼び出し
4. JSON レスポンス生成

**成果物**:
- 動作するREST API
- Postmanでテスト可能

**期間**: 2-3日

### Phase 13-2: WebSocket対応

**目標**: リアルタイム進捗通知

**実装内容**:
1. WebSocketハンドラー実装
2. ParallelGameRunnerに進捗コールバック追加
3. 進捗イベントのブロードキャスト
4. 接続管理（複数クライアント対応）

**成果物**:
- WebSocketでリアルタイム進捗受信
- WebSocket CLIツールでテスト可能

**期間**: 2-3日

### Phase 13-3: フロントエンドMVP

**目標**: シンプルな設定画面とダッシュボード

**実装内容**:
1. React + Viteプロジェクトセットアップ
2. シミュレーション設定画面
   - 戦略選択ドロップダウン
   - ゲーム数入力
   - 実行ボタン
3. 基本的なダッシュボード
   - 進捗バー
   - リアルタイムグラフ（勝率のみ）
4. WebSocket接続管理

**成果物**:
- ブラウザでシミュレーション実行可能
- リアルタイム進捗表示

**期間**: 4-5日

### Phase 13-4: インタラクティブダッシュボード

**目標**: 高度な分析UI

**実装内容**:
1. 複数グラフの同時表示
   - 勝率推移
   - 資産推移
   - プロパティ分析
2. フィルタリング機能
3. データテーブル（ソート・検索）
4. グラフのズーム・パン
5. CSV/PDFエクスポート

**成果物**:
- インタラクティブな分析ダッシュボード

**期間**: 5-7日

### Phase 13-5: 1ゲーム詳細再生（オプション）

**目標**: ゲームの可視化再生

**実装内容**:
1. ボード可視化コンポーネント
2. ターンごとの状態再生
3. イベントログ表示
4. 再生コントロール（再生/停止/早送り）

**成果物**:
- ゲームのビジュアル再生機能

**期間**: 5-7日

## トレードオフ分析

### 1. フルスタックTypeScript vs Kotlin + TypeScript

| 項目 | TypeScript (Node.js) | Kotlin + TypeScript (推奨) |
|-----|---------------------|---------------------------|
| 既存コード再利用 | ❌ 全て書き直し | ✅ そのまま使用 |
| 開発速度 | ❌ 遅い（移植必要） | ✅ 速い |
| 保守性 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| パフォーマンス | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 学習コスト | ⭐⭐⭐⭐ | ⭐⭐⭐ |

**結論**: 既存資産を活かすためKotlin + TypeScriptを推奨

### 2. SPA vs MPA

| 項目 | SPA (推奨) | MPA |
|-----|-----------|-----|
| ユーザー体験 | ⭐⭐⭐⭐⭐ スムーズ | ⭐⭐⭐ ページ遷移あり |
| リアルタイム性 | ✅ WebSocketと相性良 | ❌ 制限あり |
| 初期ロード | ❌ やや遅い | ✅ 速い |
| SEO | ❌ 不要（内部ツール） | ✅ 良好 |
| 開発複雑度 | ⭐⭐⭐⭐ | ⭐⭐ |

**結論**: ダッシュボードの性質上SPAが適切

### 3. GraphQL vs REST

| 項目 | GraphQL | REST (推奨) |
|-----|---------|------------|
| 柔軟性 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 学習コスト | ❌ 高い | ✅ 低い |
| オーバーフェッチ | ✅ なし | ❌ あり得る |
| 実装コスト | ❌ 高い | ✅ 低い |

**結論**: シンプルなデータ構造なのでRESTで十分

## リスクと対策

| リスク | 影響 | 対策 |
|-------|-----|-----|
| フロントエンド技術の学習コスト | 開発遅延 | シンプルなMVPから開始、段階的に機能追加 |
| WebSocket接続の安定性 | UX低下 | 再接続ロジック実装、フォールバックとしてpolling |
| 大量シミュレーションでのメモリ不足 | サーバークラッシュ | ストリーミング処理、結果の段階的破棄 |
| CORSの設定ミス | 開発困難 | 開発時はCORS全許可、本番で制限 |

## 代替案：軽量アプローチ

もしフルスタックSPAが過剰と判断される場合の代替案：

### 軽量版: HTML + Alpine.js + HTMX

**構成**:
- バックエンド: Ktor（同じ）
- フロントエンド: サーバーサイドレンダリングHTML + Alpine.js（インタラクティビティ）
- リアルタイム: SSE（Server-Sent Events）

**メリット**:
- フロントエンドのビルドプロセス不要
- 学習コスト低い
- シンプル

**デメリット**:
- 複雑なUIは困難
- 保守性が劣る

**推奨**: 本格的なダッシュボードを目指すならReact、学習目的や簡易版なら軽量版

## 次のステップ

この方針ドキュメントに基づいて：

1. ✅ 技術スタック確定: Ktor + React + TypeScript
2. ⏸️ Phase 13-1から実装開始
3. ⏸️ 各フェーズごとに動作確認
4. ⏸️ 必要に応じて方針を見直し

## 作成日

2025-11-16

## 承認

- [ ] 技術選定の承認
- [ ] アーキテクチャ設計の承認
- [ ] 実装計画の承認
