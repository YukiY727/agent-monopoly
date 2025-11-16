# Phase 9: 詳細統計の追加

**目標**: より詳細な分析項目を追加し、研究レベルのデータ分析を可能にする

---

## 概要

Phase 9では、Phase 6で実装した基本統計を大幅に拡張し、以下の詳細統計を追加します：

1. **プロパティ別の収益性分析**
2. **ターンごとの資産推移**
3. **破産タイミング分析**

これにより、モノポリーの戦略研究がより深く行えるようになります。

---

## 実装範囲

### 1. プロパティ別収益性分析

**PropertyStatistics**:
- プロパティごとの統計情報
- 購入回数
- レント収入の総額
- ROI（投資利益率）
- 勝利への貢献度

**実装内容**:
```kotlin
data class PropertyStatistics(
    val propertyName: String,
    val position: Int,
    val purchaseCount: Int,
    val totalRentCollected: Int,
    val averageRentPerGame: Double,
    val winRateWhenOwned: Double,
    val roi: Double  // Return on Investment
)
```

### 2. ターンごとの資産推移

**AssetHistory**:
- 各プレイヤーのターンごとの資産額
- 現金と総資産の推移
- グラフ化のためのデータ構造

**実装内容**:
```kotlin
data class AssetSnapshot(
    val turnNumber: Int,
    val playerName: String,
    val cash: Int,
    val totalAssets: Int,
    val propertiesCount: Int
)

data class AssetHistory(
    val snapshots: List<AssetSnapshot>
)
```

### 3. 破産タイミング分析

**BankruptcyAnalysis**:
- 破産が発生したターン数
- 破産時の状況
- 破産の原因分析

**実装内容**:
```kotlin
data class BankruptcyEvent(
    val gameNumber: Int,
    val turnNumber: Int,
    val playerName: String,
    val causePlayerName: String?,  // 破産させたプレイヤー
    val lastCash: Int,
    val propertiesOwned: Int
)

data class BankruptcyAnalysis(
    val bankruptcyEvents: List<BankruptcyEvent>,
    val averageBankruptcyTurn: Double,
    val bankruptcyDistribution: Map<IntRange, Int>
)
```

### 4. 詳細統計計算機

**DetailedStatisticsCalculator**:
- MultiGameResult から詳細統計を計算
- プロパティ統計の集計
- 資産推移の記録
- 破産分析の実施

---

## 可視化の拡張

### 1. 折れ線グラフ（資産推移）

**LineChartGenerator**:
- 複数プレイヤーの資産推移を表示
- SVG で生成
- 各プレイヤーを異なる色で表示

### 2. ランキング表（プロパティ収益性）

**HTMLテーブル**:
- プロパティ別ROIランキング
- ソート可能
- 色分けで視覚化

### 3. ヒストグラム（破産タイミング）

**既存のHistogramGeneratorを活用**:
- 破産が発生したターンの分布
- 早期破産 vs 後期破産の傾向

---

## 成果物

1. **詳細統計データモデル**
   - PropertyStatistics
   - AssetHistory
   - BankruptcyAnalysis

2. **詳細統計計算機**
   - DetailedStatisticsCalculator

3. **可視化コンポーネント**
   - LineChartGenerator（折れ線グラフ）
   - プロパティランキング表

4. **拡張されたHTMLレポート**
   - 詳細統計セクション追加
   - 折れ線グラフ追加
   - ランキング表追加

---

## データ収集の拡張

Phase 9では、ゲーム実行中のイベントをより詳細に記録する必要があります：

### 必要なイベント情報

1. **プロパティ購入イベント**
   - どのプレイヤーがいつ購入したか
   - 購入価格

2. **レント支払いイベント**
   - 誰が誰にいくら支払ったか
   - どのプロパティでのレント

3. **ターンごとのスナップショット**
   - 各ターン終了時の全プレイヤーの状態
   - 現金、総資産、所有プロパティ

4. **破産イベント**
   - 破産したプレイヤー
   - 破産させたプレイヤー
   - 破産時の状況

**既存のイベントシステムを活用**:
- Phase 2で実装したGameEventを利用
- 必要に応じて新しいイベントタイプを追加

---

## 検証項目

1. **正確性**
   - 統計計算が正確か
   - 資産推移が正しく記録されているか

2. **パフォーマンス**
   - 詳細統計の計算が高速か
   - メモリ使用量が適切か

3. **可視化**
   - グラフが見やすいか
   - データが正しく表示されているか

---

## 実装の優先順位

1. **High**: PropertyStatistics 実装
2. **High**: DetailedStatisticsCalculator 実装
3. **Medium**: AssetHistory 実装
4. **Medium**: LineChartGenerator 実装
5. **Low**: BankruptcyAnalysis 実装

---

## 制約と前提

### 制約

- イベントログが詳細に記録されている必要がある
- メモリ使用量の増加（特にAssetHistory）
- 計算時間の増加

### 前提

- Phase 2のイベントログ機能が利用可能
- Phase 6の基本統計が実装済み
- Phase 7の可視化機能が利用可能

---

## リスクと対策

| リスク | 影響 | 対策 |
|-------|-----|-----|
| メモリ不足 | 大量ゲーム実行時にOOM | サンプリング、集約 |
| 計算時間増加 | 統計計算が遅い | 並列計算、最適化 |
| データの欠損 | 統計が不正確 | イベントログの検証 |

---

## 次のフェーズへの準備

Phase 10（高度な戦略実装）では、この詳細統計を活用して：
- 各戦略の強みと弱みを分析
- プロパティごとの戦略効果を測定
- 戦略の最適化に役立てる

---

**作成日**: 2025-11-16
