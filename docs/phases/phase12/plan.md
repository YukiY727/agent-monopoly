# Phase 12: 高度な可視化 - 実装計画

## 目標

研究レベルのグラフとレポートを生成する

## 実装範囲

### 1. ボード統計収集機能

**目的**: マス目ごとの停止回数や収益性を追跡する

**実装内容**:
- `BoardStatistics`: マス目ごとの統計データを保持
- `BoardStatisticsCalculator`: 複数ゲームからボード統計を計算

**データ項目**:
- マス目ごとの停止回数
- マス目ごとの総収益（レント収入）
- マス目の所有者分布

### 2. ヒートマップ可視化

**目的**: ボードのマス目を色分けして収益性を視覚化する

**実装内容**:
- `HeatmapData`: ヒートマップのデータ構造
- `HeatmapGenerator`: SVGヒートマップを生成

**表示内容**:
- 停止回数のヒートマップ
- 収益性のヒートマップ
- 色の濃さで値の大きさを表現

### 3. 戦略比較チャート

**目的**: 複数の戦略を多角的に比較する

**実装内容**:
- `RadarChartData` + `RadarChartGenerator`: レーダーチャート
- `ScatterPlotData` + `ScatterPlotGenerator`: 散布図

**レーダーチャート軸**:
- 勝率
- 平均最終資産
- プロパティ獲得数
- 破産回避率
- ゲーム終了速度（逆数）

**散布図**:
- X軸: 勝率
- Y軸: 平均最終資産
- 各点が1つの戦略を表す

### 4. 研究論文用レポート

**目的**: 学術的な分析に使えるレポートを生成する

**実装内容**:
- `ResearchReportGenerator`: 研究論文スタイルのHTMLレポート生成

**レポート構成**:
1. Abstract（要約）
2. Methodology（手法）
3. Results（結果）
   - 基本統計
   - 戦略比較
   - プロパティ分析
   - ボードヒートマップ
4. Discussion（考察）- プレースホルダー
5. Conclusion（結論）- プレースホルダー
6. References（参考文献）- プレースホルダー

### 5. PDF出力機能（オプション）

**目的**: HTMLレポートをPDFに変換する

**実装方針**:
- HTMLからPDF変換は外部ツール（wkhtmltopdf等）に依存
- CLIから `--pdf` オプションでPDF出力を有効化
- 外部ツールがインストールされていない場合はエラーメッセージを表示

## 実装タスク

1. ✅ ドキュメント作成
2. ⏸️ ボード統計収集機能実装
3. ⏸️ ヒートマップ生成機能実装
4. ⏸️ レーダーチャート生成機能実装
5. ⏸️ 散布図生成機能実装
6. ⏸️ 研究論文用レポート生成機能実装
7. ⏸️ PDF出力機能実装（オプション）
8. ⏸️ CLI拡張（--research-report, --pdf オプション）
9. ⏸️ 統合テスト

## アーキテクチャ方針

### データフロー

```
MultiGameResult
    ↓
DetailedStatisticsCalculator
    ↓
DetailedStatistics + BoardStatistics
    ↓
ResearchReportGenerator
    ↓
HTML/PDF
```

### 可視化コンポーネントの統一

- すべてのチャートジェネレータはSVG形式で出力
- データクラスとジェネレータを分離
- Phase 7の既存パターンを継承

### 研究レポートの独立性

- `ResearchReportGenerator` は `StatisticsReportGenerator` とは別クラス
- 異なる目的（一般向け vs 研究向け）に対応
- 両方のレポートを同時に生成可能

## 成果物

### コード
- `src/main/kotlin/com/monopoly/statistics/BoardStatistics.kt`
- `src/main/kotlin/com/monopoly/statistics/BoardStatisticsCalculator.kt`
- `src/main/kotlin/com/monopoly/visualization/HeatmapData.kt`
- `src/main/kotlin/com/monopoly/visualization/HeatmapGenerator.kt`
- `src/main/kotlin/com/monopoly/visualization/RadarChartData.kt`
- `src/main/kotlin/com/monopoly/visualization/RadarChartGenerator.kt`
- `src/main/kotlin/com/monopoly/visualization/ScatterPlotData.kt`
- `src/main/kotlin/com/monopoly/visualization/ScatterPlotGenerator.kt`
- `src/main/kotlin/com/monopoly/visualization/ResearchReportGenerator.kt`
- `src/main/kotlin/com/monopoly/cli/PdfExporter.kt`（オプション）

### CLI拡張
- `--research-report`: 研究論文用レポート生成
- `--pdf`: PDF出力を有効化（HTMLと併用）

### サンプル出力
- `monopoly-research-report.html`
- `monopoly-research-report.pdf`（オプション）

## 検証項目

- [ ] ヒートマップが直感的に理解できるか
- [ ] レーダーチャートで戦略の特性が比較できるか
- [ ] 散布図で戦略のポジショニングが分かるか
- [ ] 研究レポートが学術的な分析に使えるレベルか
- [ ] PDF出力が正しく動作するか（オプション）

## 作成日

2025-11-16
