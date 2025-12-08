# 現在のフェーズ: Phase 8（実験管理フェーズ）

## 目標

複数ゲームのシミュレーション実行と統計収集を可能にし、戦略の分析基盤を構築する

---

## クイックリファレンス

### 技術スタック

- Kotlin（JVM 21+）
- Kotest + MockK
- Gradle（Kotlin DSL）
- kotlinx-serialization（JSON保存用）
- Ktor（Web UI Server）

### 実装範囲

Phase 8の目標：

1. **複数ゲーム実行機能** - N回のゲームを自動実行
2. **基本統計収集** - 勝率、平均ターン数、資産推移の記録
3. **結果の永続化** - JSON/CSV形式での統計データ保存
4. **CLI統計表示** - 実験結果のサマリー表示

詳細: [`phases/phase8/design.md`](phases/phase8/design.md)（作成予定）

---

## 実装済み機能

### Phase 1: 最小限のゲーム実行（完了 ✅）

- [x] 基本ゲームループ
- [x] サイコロ振り、移動、家賃支払い
- [x] プロパティ購入判定
- [x] 破産処理とゲーム終了

### Phase 2: イベント記録システム（完了 ✅）

- [x] GameEvent sealed class
- [x] イベントログ記録
- [x] JSON保存機能

### Phase 3: Doubles、Jail、Cards（完了 ✅）

- [x] ダブル（ゾロ目）判定と追加ターン
- [x] 3回連続ゾロ目で刑務所送り
- [x] 刑務所システム（収監、脱出方法）
- [x] カードシステム（Chance、Community Chest）
- [x] カードデッキ管理

### Phase 4: 建物システム（完了 ✅）

- [x] 家（最大4件）とホテル（1件）の建設
- [x] 同色プロパティ全所有（モノポリー）の判定
- [x] 建物の均等建設ルール
- [x] 家賃計算の拡張（建物数に応じた変動）
- [x] BuildingService（建設ロジック）
- [x] MonopolyCheckerService（モノポリー判定）
- [x] GameServiceへの統合（ターン終了時の自動建設）

### Phase 5: 税金と特殊プロパティ（完了 ✅）

- [x] 税金マス処理（Income Tax, Luxury Tax）
- [x] 鉄道の家賃計算（所有数に応じた家賃）
- [x] 公共施設の家賃計算（サイコロの目 × 倍率）

### Phase 6: 抵当システム（完了 ✅）

- [x] 抵当設定・解除
- [x] 抵当価値計算（価格の50%）
- [x] 抵当解除コスト（110%）
- [x] 抵当中の家賃無効化

### Phase 7: オークションシステム（完了 ✅）

- [x] オークション機構
- [x] 入札管理
- [x] 落札判定
- [x] 購入拒否時のオークション開催

### Web UI Server（完了 ✅）

- [x] Ktorサーバー（ポート8080）
- [x] REST API（ゲーム開始、ターン実行、状態取得）
- [x] CORS設定
- [x] JSON serialization
- [x] 静的HTMLファイル提供

---

## 未実装機能

### 複数戦略（Phase 8以降で実装予定）

- [ ] ランダム戦略
- [ ] 保守的戦略（一定額以上の現金を保持）
- [ ] セット重視戦略（同色セット完成を優先）
- [ ] ROI戦略（投資効率を計算）

**注**: 現在は`AlwaysPlayerStrategy`のみ実装。複数戦略は実験管理システム完成後に実装

---

## 次のフェーズ: Phase 8（実験管理）

### 実装の目標

複数ゲームのシミュレーション実行と基本統計の収集により、戦略研究の基盤を構築する

### 実装内容

#### 1. 複数ゲーム実行エンジン

```kotlin
class ExperimentRunner(
    val gameCount: Int,
    val playerStrategies: List<PlayerStrategy>,
    val outputDir: String
)
```

- N回のゲームを自動実行
- 各ゲームの独立性を保証
- 進捗表示（CLI）

#### 2. 基本統計収集

```kotlin
data class GameStatistics(
    val gameId: String,
    val turnCount: Int,
    val winner: String,
    val finalAssets: Map<String, Int>,
    val bankruptcyOrder: List<String>
)
```

収集する統計：

- ゲームごとの勝者
- ターン数
- 各プレイヤーの最終資産
- 破産順序

#### 3. 統計の集約と分析

```kotlin
data class AggregatedStatistics(
    val totalGames: Int,
    val winRateByPlayer: Map<String, Double>,
    val averageTurnCount: Double,
    val averageFinalAssets: Map<String, Double>
)
```

- 勝率計算（プレイヤー別）
- 平均ターン数
- 平均最終資産

#### 4. 結果の永続化

- JSON形式での統計データ保存
- CSV形式でのエクスポート（Excel等での分析用）
- ファイル命名規則: `experiment_YYYYMMDD_HHMMSS.json`

#### 5. CLI統計表示

```text
=== Experiment Results ===
Total Games: 100
Average Turns: 45.3

Win Rate:
  Alice (AlwaysPlayerStrategy): 52%
  Bob (AlwaysPlayerStrategy): 48%

Average Final Assets:
  Alice: $1,234
  Bob: $987
```

### 期待される効果

1. **戦略の定量的評価**: 勝率や資産推移で戦略の優劣を判定可能
2. **再現可能な実験**: 統計データを保存することで、後から分析可能
3. **研究基盤の確立**: Phase 9以降の詳細分析（HTMLレポート、グラフ可視化）の基盤

### 実装スコープ（Phase 8）

**実装する**:

- ✅ 複数ゲーム実行エンジン
- ✅ 基本統計収集（勝率、平均ターン数、資産）
- ✅ JSON/CSV保存
- ✅ CLI統計表示

**実装しない（Phase 9以降）**:

- ❌ HTMLレポート生成
- ❌ グラフ可視化（折れ線、棒グラフ等）
- ❌ 詳細統計（プロパティ別収益性、資産推移など）
- ❌ 並列実行

---

## Phase 8完了条件

- [ ] 指定回数（N回）のゲームを実行できる
- [ ] 各ゲームの統計データが収集される
- [ ] 統計データがJSON/CSV形式で保存される
- [ ] CLIで統計サマリーが表示される
- [ ] テストカバレッジが90%以上を維持

---

## Phase 9以降の計画

### Phase 9: HTMLレポートと可視化

- HTMLレポート生成（ゲーム進行、統計グラフ）
- 戦略別勝率の棒グラフ
- ターン数分布のヒストグラム

### Phase 10: 詳細統計と分析

- プロパティ別収益性分析
- ターンごとの資産推移グラフ
- 破産タイミング分析

### Phase 11: 並列実行とパフォーマンス最適化

- マルチスレッド/コルーチンによる並列実行
- 目標: 10,000ゲーム/分以上

### Phase 12: 複数戦略の実装

- ランダム戦略
- 保守的戦略
- セット重視戦略
- ROI戦略

---

## 方針変更の記録

### 2025-12-08: Phase 4建物システムの完了確認

- **状況**: Phase 4建物システムは既に実装済みであることが判明
- **実装内容**:
  - BuildingService、MonopolyCheckerService実装済み
  - GameServiceへの統合完了（ターン終了時に自動建設）
  - 27テストケース（BuildingService: 10, MonopolyChecker: 6, PropertyBuildings: 11）
- **理由**: ドキュメントの更新漏れ。実装は完了していた

### 2025-12-08: Phase 8の定義変更

- **変更前**: Phase 8 = 複数戦略とゲーム可視化
- **変更後**: Phase 8 = 実験管理（複数ゲーム実行と基本統計）
- **理由**:
  - Phase 3-7でゲームの主要ルールが実装完了
  - 戦略研究のための実験基盤が必要
  - 複数戦略の実装は実験管理システム完成後の方が効率的
  - YAGNI原則に従い、必要な機能から実装

### 優先順位の考え方

1. **実験管理（Phase 8）**: 研究基盤として最優先
2. **可視化・分析（Phase 9-10）**: 実験結果の理解を深める
3. **パフォーマンス（Phase 11）**: 大規模実験のために必要
4. **複数戦略（Phase 12）**: 実験基盤完成後に実装

---

## 参考ドキュメント

- ゲームルール: [`specifications/02-game-rules.md`](specifications/02-game-rules.md)
- 開発計画: [`planning/development-plan.md`](planning/development-plan.md)
- 未実装機能: [`planning/unimplemented-features.md`](planning/unimplemented-features.md)

---

**更新**: 2025-12-08
