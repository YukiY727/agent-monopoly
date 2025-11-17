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

#### 推奨: **ハイブリッドアプローチ**

Phase 13のUIは**性質が異なる2つの領域**で構成されるため、技術スタックを使い分けます。

##### 領域A: データ分析ダッシュボード（React）

**対象機能**:
- シミュレーション設定フォーム
- 統計グラフ（Recharts）
- 進捗バー、データテーブル
- 戦略比較チャート

**選定理由**:
- 宣言的UIで状態管理が重要
- データ可視化ライブラリが豊富
- フォームとグラフのエコシステムが成熟

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

##### 領域B: ゲーム可視化エンジン（TypeScript スクラッチ）

**対象機能**:
- モノポリーボードのレンダリング
- プレイヤーコマの移動アニメーション
- ターン再生（再生/停止/早送り/巻き戻し）
- イベントのビジュアル表現

**選定理由**:
- **頻繁なDOM更新**: Reactの仮想DOMはオーバーヘッド
- **アニメーション制御**: requestAnimationFrameで直接制御が必要
- **ゲーム特有ロジック**: デザインパターンで柔軟に実装
- **パフォーマンス**: Canvas/SVGを直接操作

**技術スタック**:
```
- TypeScript（純粋なOOP）
- Canvas API（高速レンダリング）
- SVG（インタラクティブな要素）
- デザインパターン（Observer, Command, State, Strategy）
- Web Animations API（スムーズなアニメーション）
```

**アーキテクチャ図**:
```
┌─────────────────────────────────────────────────┐
│     React Dashboard (領域A)                     │
│  ┌───────────────────────────────────────────┐  │
│  │ SimulationSetup, Charts, DataTable       │  │
│  └───────────────┬───────────────────────────┘  │
│                  │ イベント通信                 │
│  ┌───────────────▼───────────────────────────┐  │
│  │ GameVisualizationWrapper (React)         │  │
│  │  <canvas ref={canvasRef} />              │  │
│  └───────────────┬───────────────────────────┘  │
└──────────────────┼──────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────┐
│  Game Visualization Engine (領域B - TS)         │
│  ┌──────────────────────────────────────────┐  │
│  │ GameRenderer (Canvas操作)                │  │
│  │ AnimationController (タイムライン管理)   │  │
│  │ EventPlayer (イベント再生)               │  │
│  │ BoardRenderer (ボード描画)               │  │
│  └──────────────────────────────────────────┘  │
└──────────────────────────────────────────────────┘
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
│   │   ├── components/        # 領域A: React コンポーネント
│   │   │   ├── SimulationSetup/
│   │   │   │   ├── SimulationSetup.tsx
│   │   │   │   └── SimulationSetup.stories.tsx
│   │   │   ├── Dashboard/
│   │   │   │   ├── Dashboard.tsx
│   │   │   │   └── Dashboard.stories.tsx
│   │   │   ├── ProgressMonitor/
│   │   │   │   ├── ProgressMonitor.tsx
│   │   │   │   └── ProgressMonitor.stories.tsx
│   │   │   ├── ChartViewer/
│   │   │   │   ├── ChartViewer.tsx
│   │   │   │   └── ChartViewer.stories.tsx
│   │   │   └── GameVisualization/
│   │   │       ├── GameVisualizationWrapper.tsx  # React wrapper
│   │   │       └── GameVisualizationWrapper.stories.tsx
│   │   ├── game-engine/       # 🆕 領域B: TypeScript ゲームエンジン
│   │   │   ├── core/
│   │   │   │   ├── GameRenderer.ts          # メインレンダラー
│   │   │   │   ├── AnimationController.ts   # アニメーション管理
│   │   │   │   ├── EventPlayer.ts           # イベント再生
│   │   │   │   └── TimelineManager.ts       # タイムライン制御
│   │   │   ├── rendering/
│   │   │   │   ├── BoardRenderer.ts         # ボード描画
│   │   │   │   ├── PlayerRenderer.ts        # プレイヤーコマ描画
│   │   │   │   ├── PropertyRenderer.ts      # プロパティ描画
│   │   │   │   └── AnimationEngine.ts       # アニメーション実行
│   │   │   ├── patterns/
│   │   │   │   ├── Observer.ts              # Observer パターン
│   │   │   │   ├── Command.ts               # Command パターン
│   │   │   │   ├── State.ts                 # State パターン
│   │   │   │   └── Strategy.ts              # Strategy パターン
│   │   │   ├── models/
│   │   │   │   ├── GameState.ts             # ゲーム状態
│   │   │   │   ├── BoardModel.ts            # ボードモデル
│   │   │   │   └── PlayerModel.ts           # プレイヤーモデル
│   │   │   └── utils/
│   │   │       ├── CanvasUtils.ts           # Canvas ユーティリティ
│   │   │       └── AnimationUtils.ts        # アニメーションヘルパー
│   │   ├── hooks/
│   │   │   ├── useSimulation.ts
│   │   │   ├── useWebSocket.ts
│   │   │   ├── useSimulationProgress.ts
│   │   │   └── useGameEngine.ts             # 🆕 ゲームエンジン連携
│   │   ├── stores/
│   │   │   ├── useUIStore.ts
│   │   │   └── useSimulationStore.ts
│   │   ├── api/
│   │   │   └── client.ts
│   │   ├── types/
│   │   │   ├── simulation.ts
│   │   │   └── gameEvents.ts                # 🆕 ゲームイベント型定義
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

## ゲーム可視化エンジンの設計パターン（領域B）

このセクションでは、領域B（TypeScriptゲーム可視化エンジン）で使用するデザインパターンを詳述します。

### 1. Observer パターン（イベント駆動アーキテクチャ）

**目的**: ゲームイベントの発生を監視し、複数のレンダラーに通知

**実装例**:
```typescript
// patterns/Observer.ts
export interface Observer<T> {
  update(data: T): void;
}

export class Subject<T> {
  private observers: Observer<T>[] = [];

  attach(observer: Observer<T>): void {
    this.observers.push(observer);
  }

  detach(observer: Observer<T>): void {
    const index = this.observers.indexOf(observer);
    if (index > -1) {
      this.observers.splice(index, 1);
    }
  }

  notify(data: T): void {
    for (const observer of this.observers) {
      observer.update(data);
    }
  }
}

// core/EventPlayer.ts
import { Subject } from '../patterns/Observer';
import { GameEvent } from '../types/gameEvents';

export class EventPlayer extends Subject<GameEvent> {
  private timeline: GameEvent[] = [];
  private currentIndex = 0;

  loadTimeline(events: GameEvent[]): void {
    this.timeline = events;
    this.currentIndex = 0;
  }

  next(): void {
    if (this.currentIndex < this.timeline.length) {
      const event = this.timeline[this.currentIndex];
      this.notify(event); // すべてのObserverに通知
      this.currentIndex++;
    }
  }

  playAll(): void {
    while (this.currentIndex < this.timeline.length) {
      this.next();
    }
  }
}

// rendering/PlayerRenderer.ts
import { Observer } from '../patterns/Observer';
import { GameEvent } from '../types/gameEvents';

export class PlayerRenderer implements Observer<GameEvent> {
  constructor(private canvas: HTMLCanvasElement) {}

  update(event: GameEvent): void {
    if (event.type === 'PlayerMoved') {
      this.animatePlayerMove(event.playerId, event.fromPosition, event.toPosition);
    }
  }

  private animatePlayerMove(playerId: string, from: number, to: number): void {
    // Canvas上でプレイヤーコマを移動アニメーション
  }
}
```

**使用シーン**:
- ゲームイベント（移動、購入、破産）の通知
- 複数のレンダラー（ボード、プレイヤー、ログ）への同時更新

### 2. Command パターン（再生制御）

**目的**: 再生操作（再生/停止/巻き戻し/早送り）をコマンドオブジェクトとして実装

**実装例**:
```typescript
// patterns/Command.ts
export interface Command {
  execute(): void;
  undo(): void;
}

// core/TimelineManager.ts
import { Command } from '../patterns/Command';
import { EventPlayer } from './EventPlayer';

export class PlayCommand implements Command {
  constructor(private player: EventPlayer) {}

  execute(): void {
    this.player.play();
  }

  undo(): void {
    this.player.pause();
  }
}

export class RewindCommand implements Command {
  private savedIndex: number = 0;

  constructor(private player: EventPlayer) {}

  execute(): void {
    this.savedIndex = this.player.getCurrentIndex();
    this.player.rewind(10); // 10イベント巻き戻し
  }

  undo(): void {
    this.player.seekTo(this.savedIndex);
  }
}

export class TimelineManager {
  private commandHistory: Command[] = [];
  private currentCommandIndex = -1;

  executeCommand(command: Command): void {
    command.execute();
    this.commandHistory = this.commandHistory.slice(0, this.currentCommandIndex + 1);
    this.commandHistory.push(command);
    this.currentCommandIndex++;
  }

  undo(): void {
    if (this.currentCommandIndex >= 0) {
      const command = this.commandHistory[this.currentCommandIndex];
      command.undo();
      this.currentCommandIndex--;
    }
  }

  redo(): void {
    if (this.currentCommandIndex < this.commandHistory.length - 1) {
      this.currentCommandIndex++;
      const command = this.commandHistory[this.currentCommandIndex];
      command.execute();
    }
  }
}
```

**使用シーン**:
- 再生/停止ボタンの実装
- Undo/Redoの実装
- タイムライン操作（シーク、早送り、巻き戻し）

### 3. State パターン（アニメーション状態管理）

**目的**: アニメーションの状態（停止/再生中/一時停止/完了）を管理

**実装例**:
```typescript
// patterns/State.ts
export interface AnimationState {
  play(controller: AnimationController): void;
  pause(controller: AnimationController): void;
  stop(controller: AnimationController): void;
  update(controller: AnimationController, deltaTime: number): void;
}

// core/AnimationController.ts
import { AnimationState } from '../patterns/State';

export class IdleState implements AnimationState {
  play(controller: AnimationController): void {
    controller.setState(new PlayingState());
    controller.startAnimation();
  }

  pause(controller: AnimationController): void {
    // 何もしない（すでに停止中）
  }

  stop(controller: AnimationController): void {
    // 何もしない（すでに停止中）
  }

  update(controller: AnimationController, deltaTime: number): void {
    // 何もしない
  }
}

export class PlayingState implements AnimationState {
  play(controller: AnimationController): void {
    // すでに再生中
  }

  pause(controller: AnimationController): void {
    controller.setState(new PausedState());
    controller.pauseAnimation();
  }

  stop(controller: AnimationController): void {
    controller.setState(new IdleState());
    controller.resetAnimation();
  }

  update(controller: AnimationController, deltaTime: number): void {
    controller.advanceAnimation(deltaTime);
  }
}

export class PausedState implements AnimationState {
  play(controller: AnimationController): void {
    controller.setState(new PlayingState());
    controller.resumeAnimation();
  }

  pause(controller: AnimationController): void {
    // すでに一時停止中
  }

  stop(controller: AnimationController): void {
    controller.setState(new IdleState());
    controller.resetAnimation();
  }

  update(controller: AnimationController, deltaTime: number): void {
    // 一時停止中は更新しない
  }
}

export class AnimationController {
  private state: AnimationState = new IdleState();
  private animationId: number | null = null;

  setState(state: AnimationState): void {
    this.state = state;
  }

  play(): void {
    this.state.play(this);
  }

  pause(): void {
    this.state.pause(this);
  }

  stop(): void {
    this.state.stop(this);
  }

  startAnimation(): void {
    let lastTime = performance.now();
    const animate = (currentTime: number) => {
      const deltaTime = currentTime - lastTime;
      lastTime = currentTime;

      this.state.update(this, deltaTime);

      this.animationId = requestAnimationFrame(animate);
    };
    this.animationId = requestAnimationFrame(animate);
  }

  pauseAnimation(): void {
    if (this.animationId !== null) {
      cancelAnimationFrame(this.animationId);
      this.animationId = null;
    }
  }

  resumeAnimation(): void {
    this.startAnimation();
  }

  resetAnimation(): void {
    if (this.animationId !== null) {
      cancelAnimationFrame(this.animationId);
      this.animationId = null;
    }
  }

  advanceAnimation(deltaTime: number): void {
    // アニメーションを進める
  }
}
```

**使用シーン**:
- アニメーションの状態遷移（アイドル→再生中→一時停止→完了）
- 状態に応じた振る舞いの変更

### 4. Strategy パターン（レンダリング戦略）

**目的**: Canvas vs SVGのレンダリング戦略を切り替え可能に

**実装例**:
```typescript
// patterns/Strategy.ts
export interface RenderStrategy {
  renderBoard(board: BoardModel): void;
  renderPlayer(player: PlayerModel): void;
  renderProperty(property: PropertyModel): void;
  clear(): void;
}

// rendering/CanvasRenderStrategy.ts
import { RenderStrategy } from '../patterns/Strategy';

export class CanvasRenderStrategy implements RenderStrategy {
  constructor(private ctx: CanvasRenderingContext2D) {}

  renderBoard(board: BoardModel): void {
    // Canvasで高速描画
    this.ctx.clearRect(0, 0, this.ctx.canvas.width, this.ctx.canvas.height);

    for (const space of board.spaces) {
      this.ctx.fillStyle = space.color;
      this.ctx.fillRect(space.x, space.y, space.width, space.height);

      this.ctx.strokeStyle = '#000';
      this.ctx.strokeRect(space.x, space.y, space.width, space.height);

      this.ctx.fillStyle = '#000';
      this.ctx.fillText(space.name, space.x + 5, space.y + 15);
    }
  }

  renderPlayer(player: PlayerModel): void {
    this.ctx.beginPath();
    this.ctx.arc(player.x, player.y, 10, 0, Math.PI * 2);
    this.ctx.fillStyle = player.color;
    this.ctx.fill();
    this.ctx.strokeStyle = '#000';
    this.ctx.stroke();
  }

  renderProperty(property: PropertyModel): void {
    // プロパティの所有状態を描画
  }

  clear(): void {
    this.ctx.clearRect(0, 0, this.ctx.canvas.width, this.ctx.canvas.height);
  }
}

// rendering/SVGRenderStrategy.ts
import { RenderStrategy } from '../patterns/Strategy';

export class SVGRenderStrategy implements RenderStrategy {
  constructor(private svgElement: SVGSVGElement) {}

  renderBoard(board: BoardModel): void {
    // SVGで描画（インタラクティブ性重視）
    this.svgElement.innerHTML = ''; // クリア

    for (const space of board.spaces) {
      const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
      rect.setAttribute('x', space.x.toString());
      rect.setAttribute('y', space.y.toString());
      rect.setAttribute('width', space.width.toString());
      rect.setAttribute('height', space.height.toString());
      rect.setAttribute('fill', space.color);
      rect.setAttribute('stroke', '#000');
      rect.addEventListener('click', () => {
        console.log(`Clicked on ${space.name}`);
      });
      this.svgElement.appendChild(rect);

      const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
      text.setAttribute('x', (space.x + 5).toString());
      text.setAttribute('y', (space.y + 15).toString());
      text.textContent = space.name;
      this.svgElement.appendChild(text);
    }
  }

  renderPlayer(player: PlayerModel): void {
    const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
    circle.setAttribute('cx', player.x.toString());
    circle.setAttribute('cy', player.y.toString());
    circle.setAttribute('r', '10');
    circle.setAttribute('fill', player.color);
    circle.setAttribute('stroke', '#000');
    this.svgElement.appendChild(circle);
  }

  renderProperty(property: PropertyModel): void {
    // プロパティの所有状態をSVGで描画
  }

  clear(): void {
    this.svgElement.innerHTML = '';
  }
}

// core/GameRenderer.ts
import { RenderStrategy } from '../patterns/Strategy';

export class GameRenderer {
  private strategy: RenderStrategy;

  constructor(strategy: RenderStrategy) {
    this.strategy = strategy;
  }

  setStrategy(strategy: RenderStrategy): void {
    this.strategy = strategy;
  }

  render(gameState: GameState): void {
    this.strategy.clear();
    this.strategy.renderBoard(gameState.board);
    for (const player of gameState.players) {
      this.strategy.renderPlayer(player);
    }
  }
}
```

**使用シーン**:
- Canvas（高速）とSVG（インタラクティブ）の切り替え
- パフォーマンスとインタラクティビティのトレードオフ

### Canvas vs SVG 比較

| 項目 | Canvas | SVG |
|-----|--------|-----|
| **パフォーマンス** | ⭐⭐⭐⭐⭐ ピクセルベース、大量描画に強い | ⭐⭐⭐ DOMベース、オブジェクト数増で遅延 |
| **アニメーション** | ⭐⭐⭐⭐⭐ requestAnimationFrameで完全制御 | ⭐⭐⭐⭐ Web Animations APIやCSS利用 |
| **インタラクティビティ** | ⭐⭐ 手動でヒット検出実装 | ⭐⭐⭐⭐⭐ DOMイベントが使える |
| **拡大縮小** | ⭐⭐ ピクセルぼやけ | ⭐⭐⭐⭐⭐ ベクターで綺麗 |
| **デバッグ** | ⭐⭐ ピクセル確認のみ | ⭐⭐⭐⭐⭐ DevToolsで要素検査可能 |
| **推奨用途** | ゲームボード全体、高速アニメーション | プロパティカード、ホバー効果、クリック可能要素 |

**推奨アプローチ**:
- **ハイブリッド**: ボードはCanvas、プロパティカードやUIはSVG
- **Strategy パターン**: パフォーマンスが問題になったらCanvasに切り替え可能

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

### Phase 13-5: ゲーム可視化エンジン実装（領域B）

**目標**: TypeScriptスクラッチによるゲーム再生エンジン

**実装内容**:

#### 1. デザインパターン基盤の実装（1-2日）

```typescript
// patterns/Observer.ts
export interface Observer<T> { update(data: T): void; }
export class Subject<T> { /* ... */ }

// patterns/Command.ts
export interface Command { execute(): void; undo(): void; }

// patterns/State.ts
export interface AnimationState { /* ... */ }

// patterns/Strategy.ts
export interface RenderStrategy { /* ... */ }
```

**成果物**: 再利用可能なパターンライブラリ

#### 2. コアエンジン実装（2-3日）

```typescript
// core/EventPlayer.ts
export class EventPlayer extends Subject<GameEvent> {
  loadTimeline(events: GameEvent[]): void { /* ... */ }
  next(): void { /* ... */ }
  play(): void { /* ... */ }
  pause(): void { /* ... */ }
  seekTo(index: number): void { /* ... */ }
}

// core/AnimationController.ts
export class AnimationController {
  private state: AnimationState;
  play(): void { /* State パターン */ }
  pause(): void { /* State パターン */ }
  stop(): void { /* State パターン */ }
}

// core/TimelineManager.ts
export class TimelineManager {
  executeCommand(command: Command): void { /* Command パターン */ }
  undo(): void { /* ... */ }
  redo(): void { /* ... */ }
}

// core/GameRenderer.ts
export class GameRenderer {
  private strategy: RenderStrategy;
  render(gameState: GameState): void { /* Strategy パターン */ }
  setStrategy(strategy: RenderStrategy): void { /* ... */ }
}
```

**成果物**: 再生制御エンジン

#### 3. レンダリング実装（2-3日）

**Canvas戦略**:
```typescript
// rendering/CanvasRenderStrategy.ts
export class CanvasRenderStrategy implements RenderStrategy {
  renderBoard(board: BoardModel): void {
    // モノポリーボードを11x11レイアウトで描画
    // - 各マスを四角形で表示
    // - プロパティ名、価格を表示
    // - カラーグループを色分け
  }

  renderPlayer(player: PlayerModel): void {
    // プレイヤーコマを円で描画
    // - 複数プレイヤーが同じマスにいる場合は重ねて表示
  }
}

// rendering/BoardRenderer.ts
export class BoardRenderer implements Observer<GameEvent> {
  update(event: GameEvent): void {
    if (event.type === 'PropertyPurchased') {
      this.highlightProperty(event.propertyPosition);
    }
  }
}

// rendering/PlayerRenderer.ts
export class PlayerRenderer implements Observer<GameEvent> {
  update(event: GameEvent): void {
    if (event.type === 'PlayerMoved') {
      this.animateMove(event.playerId, event.fromPosition, event.toPosition);
    }
  }

  private animateMove(playerId: string, from: number, to: number): void {
    // Web Animations API or requestAnimationFrameでスムーズ移動
    const duration = 500; // 500ms
    const startTime = performance.now();

    const animate = (currentTime: number) => {
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);

      // イージング関数適用
      const eased = this.easeInOutCubic(progress);

      // 位置計算と描画
      const currentPos = this.interpolatePosition(from, to, eased);
      this.drawPlayerAt(playerId, currentPos);

      if (progress < 1) {
        requestAnimationFrame(animate);
      }
    };

    requestAnimationFrame(animate);
  }

  private easeInOutCubic(t: number): number {
    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
  }
}

// rendering/AnimationEngine.ts
export class AnimationEngine {
  private animations: Map<string, Animation> = new Map();

  addAnimation(id: string, animation: Animation): void { /* ... */ }
  cancelAnimation(id: string): void { /* ... */ }
  update(deltaTime: number): void { /* すべてのアニメーションを更新 */ }
}
```

**SVG戦略（代替）**:
```typescript
// rendering/SVGRenderStrategy.ts
export class SVGRenderStrategy implements RenderStrategy {
  renderBoard(board: BoardModel): void {
    // SVG要素を動的生成
    // - クリック可能なプロパティ
    // - ホバーで詳細表示
  }

  renderPlayer(player: PlayerModel): void {
    // SVG circle要素でプレイヤー描画
    // - CSS transitionでアニメーション
  }
}
```

**成果物**:
- Canvas/SVG両対応のレンダラー
- スムーズなアニメーション

#### 4. React統合（1日）

```typescript
// components/GameVisualization/GameVisualizationWrapper.tsx
import React, { useRef, useEffect } from 'react';
import { useGameEngine } from '../../hooks/useGameEngine';

interface Props {
  gameEvents: GameEvent[];
  onEventChange?: (eventIndex: number) => void;
}

export const GameVisualizationWrapper: React.FC<Props> = ({
  gameEvents,
  onEventChange
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const { engine, controls } = useGameEngine(canvasRef, gameEvents);

  return (
    <div className="game-visualization">
      <canvas
        ref={canvasRef}
        width={800}
        height={800}
        style={{ border: '1px solid #ccc' }}
      />
      <div className="controls">
        <button onClick={controls.play}>▶️ Play</button>
        <button onClick={controls.pause}>⏸️ Pause</button>
        <button onClick={controls.stop}>⏹️ Stop</button>
        <button onClick={controls.rewind}>⏪ Rewind</button>
        <button onClick={controls.fastForward}>⏩ Fast Forward</button>
      </div>
    </div>
  );
};

// hooks/useGameEngine.ts
import { useEffect, useState } from 'react';
import { GameRenderer } from '../game-engine/core/GameRenderer';
import { EventPlayer } from '../game-engine/core/EventPlayer';
import { AnimationController } from '../game-engine/core/AnimationController';
import { CanvasRenderStrategy } from '../game-engine/rendering/CanvasRenderStrategy';

export function useGameEngine(
  canvasRef: React.RefObject<HTMLCanvasElement>,
  events: GameEvent[]
) {
  const [engine, setEngine] = useState<GameRenderer | null>(null);
  const [eventPlayer, setEventPlayer] = useState<EventPlayer | null>(null);

  useEffect(() => {
    if (!canvasRef.current) return;

    const ctx = canvasRef.current.getContext('2d');
    if (!ctx) return;

    // エンジン初期化
    const strategy = new CanvasRenderStrategy(ctx);
    const renderer = new GameRenderer(strategy);
    const player = new EventPlayer();

    player.loadTimeline(events);

    // Observer登録
    const boardRenderer = new BoardRenderer(ctx);
    const playerRenderer = new PlayerRenderer(ctx);
    player.attach(boardRenderer);
    player.attach(playerRenderer);

    setEngine(renderer);
    setEventPlayer(player);

    // クリーンアップ
    return () => {
      player.detach(boardRenderer);
      player.detach(playerRenderer);
    };
  }, [canvasRef, events]);

  const controls = {
    play: () => eventPlayer?.play(),
    pause: () => eventPlayer?.pause(),
    stop: () => eventPlayer?.stop(),
    rewind: () => eventPlayer?.rewind(10),
    fastForward: () => eventPlayer?.fastForward(10),
  };

  return { engine, eventPlayer, controls };
}
```

**成果物**: ReactとTypeScriptエンジンの連携

#### 5. イベントログとUI（1日）

```typescript
// components/GameVisualization/EventLog.tsx
export const EventLog: React.FC<{ events: GameEvent[] }> = ({ events }) => {
  return (
    <div className="event-log">
      <h3>Event Timeline</h3>
      <ul>
        {events.map((event, index) => (
          <li key={index} className={`event-${event.type}`}>
            <span className="event-time">Turn {event.turn}</span>
            <span className="event-description">
              {formatEvent(event)}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
};

function formatEvent(event: GameEvent): string {
  switch (event.type) {
    case 'PlayerMoved':
      return `${event.playerName} moved from ${event.from} to ${event.to}`;
    case 'PropertyPurchased':
      return `${event.playerName} purchased ${event.propertyName}`;
    case 'RentPaid':
      return `${event.playerName} paid $${event.amount} rent to ${event.owner}`;
    case 'PlayerBankrupt':
      return `${event.playerName} went bankrupt`;
    default:
      return 'Unknown event';
  }
}
```

**成果物**:
- イベントログUI
- タイムラインスライダー
- 再生速度コントロール

**総期間**: 7-9日

**技術スタック（領域B専用）**:
```json
{
  "devDependencies": {
    "typescript": "^5.2.0",
    "@types/node": "^20.0.0"
  }
}
```

**パフォーマンス目標**:
- 60 FPS アニメーション
- 1000イベント以上の再生に対応
- メモリ使用量 < 100MB

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
| **Canvas描画のパフォーマンス問題** | 低FPS、カクつき | requestAnimationFrameで最適化、描画範囲の限定、ダーティフラグ導入 |
| **アニメーションの複雑化** | メモリリーク、バグ | AnimationControllerでライフサイクル管理、cancelAnimationFrame確実実行 |
| **デザインパターンの過度な抽象化** | 開発速度低下 | 必要最小限のパターンのみ実装、YAGNIの原則を守る |
| **Canvas vs SVGの技術選択ミス** | パフォーマンス低下 | Strategy パターンで切り替え可能に、初期はCanvasで実装 |
| **ゲームエンジンとReactの結合** | 再レンダリングループ | useRefでCanvas参照、useEffectの依存配列を最小化 |
| **1000+イベントの再生負荷** | ブラウザフリーズ | イベントの間引き、Web Worker検討、仮想化（見える範囲のみ描画）|
| **クロスブラウザ互換性** | Canvas/アニメーション動作差異 | モダンブラウザのみ対応（Chrome, Firefox, Edge）、polyfillは使わない |
| **アニメーションデバッグ困難** | 開発効率低下 | アニメーション速度調整UI、ステップ実行機能、DevToolsフレンドリーな実装 |

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
