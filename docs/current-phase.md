# 現在のフェーズ: Phase 9（複数戦略と支配関係分析）

## 目標

複数の基本戦略を実装し、戦略間の支配関係を分析する。
HTMLレポートとグラフ可視化により、実験結果を分かりやすく表示する。

---

## クイックリファレンス

### 技術スタック

- Kotlin（JVM 21+）
- Kotest + MockK
- Gradle（Kotlin DSL）
- kotlinx-serialization（JSON保存用）
- Ktor（Web UI Server）
- Chart.js（グラフ可視化）

### 実行コマンド

```bash
# 単一実験（2プレイヤー）
./gradlew runExperiment -PgameCount=100 -Pstrategies=aggressive,balanced

# 戦略比較マトリックス（全戦略ペア対戦）
./gradlew runComparison

# HTMLレポート確認
open experiment-results/*/comparison-report.html
```

---

## Phase 9 実装状況

### 完了 ✅

#### 1. 複数戦略の実装

- [x] AlwaysPlayerStrategy（既存）
- [x] RandomStrategy
- [x] ConservativeStrategy
- [x] AggressiveStrategy
- [x] SetFocusedStrategy
- [x] ROIStrategy
- [x] BalancedStrategy

#### 2. 戦略比較実験システム

- [x] StrategyComparisonExperiment
- [x] 対戦マトリックス生成
- [x] ComparisonMain CLI

#### 3. HTMLレポート生成

- [x] 勝率グラフ（Chart.js）
- [x] 対戦マトリックス表示
- [x] プレイヤー別統計

#### 4. トレード機能（前倒し実装）

- [x] TradeOffer, Trade ドメインモデル
- [x] TradeService（検証・実行）
- [x] TradeHelperService（戦略支援）
- [x] GameServiceへの統合
- [x] トレードイベント（Proposed/Accepted/Rejected/Completed）
- [x] AggressiveStrategy, BalancedStrategyへのトレードロジック追加

#### 5. 統計収集の拡張

- [x] トレード統計（tradesProposed, tradesAccepted, tradesCompleted）
- [x] PlayerStatisticsへの追加

### 未完了 ⏳

#### 統計的検定

- [ ] t検定（戦略間の有意差）
- [ ] 95%信頼区間計算
- [ ] Cohen's d（効果量）
- [ ] Bonferroni補正（多重比較）

#### Nash均衡探索

- [ ] 最適反応戦略の特定
- [ ] 支配戦略の検出
- [ ] 混合戦略Nash均衡の計算

---

## 実装済み機能（Phase 1-8）

### Phase 1: 最小限のゲーム実行 ✅

- 基本ゲームループ、サイコロ、移動、家賃支払い、破産処理

### Phase 2: イベント記録システム ✅

- GameEvent sealed class、イベントログ記録、JSON保存

### Phase 3: Doubles、Jail、Cards ✅

- ダブル判定、刑務所システム、カードシステム

### Phase 4: 建物システム ✅

- 家・ホテル建設、モノポリー判定、均等建設ルール

### Phase 5: 税金と特殊プロパティ ✅

- 税金マス、鉄道・公共施設の家賃計算

### Phase 6: 抵当システム ✅

- 抵当設定・解除、抵当中の家賃無効化

### Phase 7: オークションシステム ✅

- オークション機構、入札管理、落札判定

### Phase 8: 実験管理 ✅

- 複数ゲーム実行、統計収集、JSON/CSV保存、CLI統計表示

### Web UI Server ✅

- Ktorサーバー、REST API、静的HTMLファイル提供

---

## 実験結果サマリー（Phase 9）

### トレード機能の効果

| 指標 | 導入前 | 導入後 |
|------|--------|--------|
| 平均ターン数 | 1000 | 411 |
| 成立トレード数/ゲーム | 0 | ~293 |
| 建設された家/ゲーム | 0 | ~23 |
| 破産発生率 | 0% | 65% |

### 戦略勝率（vs Random）

| 戦略 | 勝率 |
|------|------|
| Conservative | 96% |
| Balanced | 94% |
| Aggressive | 87% |
| SetFocused | 71% |
| ROI | 71% |
| Always | 67% |

---

## 次のステップ

### Phase 9 残りタスク

1. **統計的検定の実装**
   - StatisticalAnalysisクラスの拡張
   - HTMLレポートへの有意性表示追加

2. **Nash均衡探索**
   - 対戦マトリックスからの支配戦略検出
   - 最適混合戦略の計算

### Phase 10 以降

| Phase | システム機能 | 研究テーマ |
|-------|--------------|------------|
| 10 | 詳細統計、高度な可視化 | リスク選好度の測定 |
| 11 | 並列実行（Coroutines） | サンクコスト効果の検証 |
| 12 | 多人数対応（3-4人） | 協調ゲーム理論 |
| 13 | - | 取引・交渉戦略 |

---

## 参考ドキュメント

- Phase 9詳細: [`phases/phase9/summary.md`](phases/phase9/summary.md)
- トレード実装計画: [`phases/phase9/trade-implementation-plan.md`](phases/phase9/trade-implementation-plan.md)
- ゲームルール: [`specifications/02-game-rules.md`](specifications/02-game-rules.md)
- 開発計画: [`planning/development-plan.md`](planning/development-plan.md)

---

**更新**: 2025-12-14
