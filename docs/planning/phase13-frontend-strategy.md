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
- Storybook（コンポーネント開発）
```

### 3. 状態管理戦略

#### Reactの状態管理の課題

**よくある問題**:
- Prop Drilling（props の多段階受け渡し）
- グローバル状態の肥大化
- 再レンダリングのパフォーマンス問題
- 非同期処理の複雑化
- ボイラープレートコードの増加

#### 状態管理ライブラリの比較

| ライブラリ | メリット | デメリット | 推奨度 |
|-----------|---------|----------|--------|
| **Zustand** | 軽量、シンプル、ボイラープレート少ない、TypeScript親和性高 | Redux比較で機能少ない | ⭐⭐⭐⭐⭐ |
| **Redux Toolkit** | 成熟、DevTools、ミドルウェア豊富 | 学習コスト高、ボイラープレート多い | ⭐⭐⭐⭐ |
| **Jotai** | Atomic、柔軟 | 新しい、コミュニティ小 | ⭐⭐⭐ |
| **Context API + useReducer** | 追加依存なし | 大規模化困難、パフォーマンス問題 | ⭐⭐ |

#### 推奨: **Zustand + TanStack Query**

**選定理由**:
- **Zustand**: UIの状態管理（設定、UI状態）
- **TanStack Query**: サーバー状態管理（API、WebSocket）

この組み合わせで**関心の分離**を実現し、複雑化を防ぎます。

#### 状態設計方針

**1. 状態の分類**

```typescript
// ❌ 悪い例: すべてを1つのストアに詰め込む
interface AppState {
  strategies: Strategy[];
  simulationConfig: SimulationConfig;
  simulationResults: SimulationResult[];
  currentSimulation: CurrentSimulation | null;
  websocketConnected: boolean;
  // ... 100行続く
}

// ✅ 良い例: 関心ごとに分離
// 1. UI状態 (Zustand)
interface UIStore {
  sidebarOpen: boolean;
  selectedTab: 'setup' | 'dashboard' | 'history';
  theme: 'light' | 'dark';
}

// 2. シミュレーション設定 (Zustand)
interface SimulationStore {
  config: SimulationConfig;
  updateConfig: (config: Partial<SimulationConfig>) => void;
  resetConfig: () => void;
}

// 3. サーバーデータ (TanStack Query)
// - strategies: useQuery(['strategies'])
// - simulationResult: useQuery(['simulation', id])
// - リアルタイム進捗: カスタムフック useSimulationProgress()
```

**2. ストアの粒度**

```typescript
// stores/useUIStore.ts
import { create } from 'zustand';

interface UIStore {
  sidebarOpen: boolean;
  toggleSidebar: () => void;
}

export const useUIStore = create<UIStore>((set) => ({
  sidebarOpen: true,
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
}));

// stores/useSimulationStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface SimulationStore {
  config: SimulationConfig;
  updatePlayers: (players: Player[]) => void;
  updateGameCount: (count: number) => void;
}

export const useSimulationStore = create<SimulationStore>()(
  persist(
    (set) => ({
      config: defaultConfig,
      updatePlayers: (players) =>
        set((state) => ({ config: { ...state.config, players } })),
      updateGameCount: (numberOfGames) =>
        set((state) => ({ config: { ...state.config, numberOfGames } })),
    }),
    { name: 'simulation-config' } // LocalStorage永続化
  )
);
```

**3. サーバー状態管理（TanStack Query）**

```typescript
// hooks/useStrategies.ts
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';

export function useStrategies() {
  return useQuery({
    queryKey: ['strategies'],
    queryFn: () => apiClient.getStrategies(),
    staleTime: 5 * 60 * 1000, // 5分間キャッシュ
  });
}

// hooks/useSimulation.ts
import { useMutation, useQueryClient } from '@tanstack/react-query';

export function useStartSimulation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (config: SimulationConfig) =>
      apiClient.startSimulation(config),
    onSuccess: (data) => {
      // キャッシュ無効化
      queryClient.invalidateQueries({ queryKey: ['simulations'] });
    },
  });
}
```

**4. WebSocket状態管理**

```typescript
// hooks/useSimulationProgress.ts
import { useEffect, useState } from 'react';
import { useWebSocket } from './useWebSocket';

interface ProgressEvent {
  type: 'progress';
  completed: number;
  total: number;
  percentage: number;
}

export function useSimulationProgress(simulationId: string) {
  const [progress, setProgress] = useState<ProgressEvent | null>(null);
  const { lastMessage, readyState } = useWebSocket(
    `ws://localhost:8080/ws/simulation/${simulationId}`
  );

  useEffect(() => {
    if (lastMessage !== null) {
      const event = JSON.parse(lastMessage.data);
      if (event.type === 'progress') {
        setProgress(event);
      }
    }
  }, [lastMessage]);

  return { progress, connected: readyState === WebSocket.OPEN };
}
```

**5. 複雑化を防ぐルール**

- ✅ **1ストア1責務**: UI、設定、データをストアで分離
- ✅ **サーバー状態はTanStack Query**: APIデータはZustandに入れない
- ✅ **派生状態を避ける**: 計算可能なものはストアに入れず、useMemoで算出
- ✅ **永続化は慎重に**: 設定のみLocalStorageに保存、一時データは保存しない
- ❌ **ストアのネスト禁止**: フラットな構造を保つ

### 4. Storybook導入

#### Storybookを使う理由

**メリット**:
1. **独立した開発環境**: バックエンド不要でコンポーネント開発
2. **ビジュアルテスト**: UIの状態を一覧で確認
3. **ドキュメント自動生成**: Propsの仕様が自動文書化
4. **デザインシステム構築**: 再利用可能なコンポーネントカタログ
5. **デバッグ効率化**: さまざまな状態を簡単に再現

**Phase 13での活用**:
```
frontend/
├── src/
│   └── components/
│       ├── ProgressBar/
│       │   ├── ProgressBar.tsx
│       │   └── ProgressBar.stories.tsx  # Storybookストーリー
│       ├── StrategySelector/
│       │   ├── StrategySelector.tsx
│       │   └── StrategySelector.stories.tsx
│       └── SimulationDashboard/
│           ├── SimulationDashboard.tsx
│           └── SimulationDashboard.stories.tsx
└── .storybook/
    ├── main.ts
    └── preview.ts
```

#### Storybookストーリーの例

```typescript
// src/components/ProgressBar/ProgressBar.stories.tsx
import type { Meta, StoryObj } from '@storybook/react';
import { ProgressBar } from './ProgressBar';

const meta: Meta<typeof ProgressBar> = {
  title: 'Simulation/ProgressBar',
  component: ProgressBar,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof ProgressBar>;

// 基本状態
export const Default: Story = {
  args: {
    completed: 0,
    total: 1000,
    percentage: 0,
  },
};

// 進行中
export const InProgress: Story = {
  args: {
    completed: 450,
    total: 1000,
    percentage: 45,
  },
};

// 完了
export const Completed: Story = {
  args: {
    completed: 1000,
    total: 1000,
    percentage: 100,
  },
};

// 大量ゲーム
export const LargeScale: Story = {
  args: {
    completed: 5420,
    total: 10000,
    percentage: 54.2,
  },
};
```

#### Storybookアドオン推奨

```json
// package.json
{
  "devDependencies": {
    "@storybook/react": "^7.5.0",
    "@storybook/addon-essentials": "^7.5.0",  // 基本アドオンセット
    "@storybook/addon-interactions": "^7.5.0", // インタラクションテスト
    "@storybook/addon-a11y": "^7.5.0",         // アクセシビリティチェック
    "@storybook/addon-links": "^7.5.0",        // ストーリー間リンク
    "@chromatic-com/storybook": "^1.0.0"      // ビジュアルリグレッションテスト
  }
}
```

#### Storybookワークフロー

```bash
# 開発中: Storybookで各コンポーネントを作成
npm run storybook  # http://localhost:6006

# ビルド: 静的サイト生成（デプロイ可能）
npm run build-storybook

# テスト: インタラクションテストを実行
npm run test-storybook
```

**開発フロー**:
1. コンポーネント設計
2. Storybookでストーリー作成
3. 各状態（loading, error, empty, success）を作成
4. インタラクションテスト追加
5. 実際のアプリに統合

### 5. リアルタイム通信

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
│   │   │   ├── SimulationSetup/
│   │   │   │   ├── SimulationSetup.tsx
│   │   │   │   └── SimulationSetup.stories.tsx
│   │   │   ├── Dashboard/
│   │   │   │   ├── Dashboard.tsx
│   │   │   │   └── Dashboard.stories.tsx
│   │   │   ├── ProgressMonitor/
│   │   │   │   ├── ProgressMonitor.tsx
│   │   │   │   └── ProgressMonitor.stories.tsx
│   │   │   └── ChartViewer/
│   │   │       ├── ChartViewer.tsx
│   │   │       └── ChartViewer.stories.tsx
│   │   ├── hooks/
│   │   │   ├── useSimulation.ts
│   │   │   ├── useWebSocket.ts
│   │   │   └── useSimulationProgress.ts
│   │   ├── stores/
│   │   │   ├── useUIStore.ts
│   │   │   └── useSimulationStore.ts
│   │   ├── api/
│   │   │   └── client.ts
│   │   ├── types/
│   │   │   └── simulation.ts
│   │   └── App.tsx
│   ├── .storybook/
│   │   ├── main.ts
│   │   └── preview.ts
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
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
1. プロジェクトセットアップ
   - React + Vite + TypeScript
   - Zustand（状態管理）
   - TanStack Query（データフェッチ）
   - Storybook
2. 基本コンポーネントをStorybookで開発
   - `StrategySelector`: 戦略選択ドロップダウン
   - `GameCountInput`: ゲーム数入力
   - `ProgressBar`: 進捗バー
   - `StartButton`: 実行ボタン
3. シミュレーション設定画面の組み立て
   - 上記コンポーネントを組み合わせ
   - useSimulationStoreで状態管理
4. 基本的なダッシュボード
   - ProgressMonitorコンポーネント
   - SimpleChartコンポーネント（勝率のみ）
   - useSimulationProgress（WebSocket）
5. API統合
   - useStrategiesフック
   - useStartSimulationフック

**開発フロー**:
```
1. Storybookで各コンポーネントを作成・確認
2. コンポーネントを統合してページ作成
3. API/WebSocketと接続
4. ブラウザで動作確認
```

**成果物**:
- ブラウザでシミュレーション実行可能
- リアルタイム進捗表示
- Storybookコンポーネントカタログ（http://localhost:6006）

**期間**: 4-5日

**技術スタック確定**:
```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "zustand": "^4.4.0",
    "@tanstack/react-query": "^5.0.0",
    "recharts": "^2.10.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@vitejs/plugin-react": "^4.2.0",
    "typescript": "^5.2.0",
    "vite": "^5.0.0",
    "@storybook/react": "^7.5.0",
    "@storybook/addon-essentials": "^7.5.0"
  }
}
```

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
| **フロントエンド技術の学習コスト** | 開発遅延 | シンプルなMVPから開始、Storybookでコンポーネント単位で学習 |
| **状態管理の複雑化** | バグ増加、保守困難 | Zustand + TanStack Queryで関心分離、1ストア1責務ルール徹底 |
| **過度な再レンダリング** | パフォーマンス低下 | React.memo、useMemo活用、小さなストア粒度 |
| **WebSocket接続の安定性** | UX低下 | 再接続ロジック実装、接続状態の可視化、フォールバックpolling |
| **大量シミュレーションでのメモリ不足** | サーバークラッシュ | ストリーミング処理、結果の段階的破棄、進捗間引き |
| **CORSの設定ミス** | 開発困難 | 開発時はCORS全許可、本番で適切に制限 |
| **Storybookの保守コスト** | ストーリーが古くなる | CI/CDでストーリーの動作確認、addon-interactionsで自動テスト |
| **TypeScript型定義の不整合** | ランタイムエラー | API型定義をバックエンドから自動生成（OpenAPI等）|

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

### 技術選定
- [ ] バックエンド: Ktor
- [ ] フロントエンド: React + TypeScript
- [ ] 状態管理: Zustand + TanStack Query
- [ ] コンポーネント開発: Storybook
- [ ] ビルドツール: Vite

### 状態管理戦略
- [ ] UI状態とサーバー状態の分離方針
- [ ] ストア粒度の設計（1ストア1責務）
- [ ] WebSocket状態管理のアプローチ

### 開発フロー
- [ ] Storybook導入の承認
- [ ] コンポーネントファーストの開発フロー

### アーキテクチャ
- [ ] REST API + WebSocketのハイブリッド設計
- [ ] ディレクトリ構成
- [ ] API設計

### 実装計画
- [ ] Phase 13-1: バックエンドAPI（2-3日）
- [ ] Phase 13-2: WebSocket対応（2-3日）
- [ ] Phase 13-3: フロントエンドMVP（4-5日）
- [ ] Phase 13-4: インタラクティブダッシュボード（5-7日）
- [ ] Phase 13-5: 1ゲーム詳細再生（オプション、5-7日）
