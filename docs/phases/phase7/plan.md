# Phase 7 実装計画

**Phase 7**: 統計の可視化機能の実装

---

## 目標

統計結果をグラフで表示し、HTMLレポートに統合できるようにする

---

## 背景

Phase 6で統計データをJSON/CSV形式で保存できるようになりました。Phase 7では、この統計データを視覚的に理解しやすくするため、グラフを生成してHTMLレポートに統合します。

これにより、Phase 9での詳細分析や、研究発表での活用が容易になります。

---

## スコープ

### 実装する機能

1. **勝率の棒グラフ**
   - プレイヤー別の勝率を棒グラフで表示
   - SVG形式で生成
   - カラフルな表示

2. **ゲーム長の分布（ヒストグラム）**
   - ターン数の分布をヒストグラムで表示
   - 10ターン刻みでビン分け
   - SVG形式で生成

3. **統計レポートHTML**
   - 新しいHTMLレポート: `statistics-report-{timestamp}.html`
   - Phase 6の統計データ + Phase 7のグラフを統合
   - Phase 4のサマリーレポートとは別ファイル

4. **SVGグラフ生成**
   - 外部ライブラリを使わず、手動でSVG生成
   - 軸、ラベル、凡例を含む完全なグラフ
   - レスポンシブデザイン

### 実装しない機能

- インタラクティブなグラフ（Phase 12で検討）
- 複雑なグラフ（折れ線グラフ、散布図など、Phase 9/12で実装）
- グラフのアニメーション
- PDFエクスポート（Phase 12で検討）

---

## 設計方針

### 1. ChartGenerator インターフェース

グラフ生成の抽象化：

```kotlin
interface ChartGenerator {
    fun generate(data: ChartData): String  // SVG文字列を返す
}
```

### 2. 具体的なグラフジェネレーター

- `BarChartGenerator`: 棒グラフ生成
- `HistogramGenerator`: ヒストグラム生成

### 3. StatisticsReportGenerator

Phase 6の統計データとPhase 7のグラフを統合したHTMLレポート生成：

```kotlin
class StatisticsReportGenerator {
    fun generate(statistics: GameStatistics, result: MultiGameResult): String
    fun saveToFile(...): File
}
```

---

## タスク分解

### タスク1: チャートデータモデルの作成

**所要時間**: 0.5時間

- `ChartData` インターフェースを作成
- `BarChartData` データクラスを作成
- `HistogramData` データクラスを作成

### タスク2: BarChartGenerator の実装

**所要時間**: 1.5-2時間

- `BarChartGenerator` クラスを作成
- SVG棒グラフの生成ロジック
- 軸、ラベル、凡例の実装

### タスク3: HistogramGenerator の実装

**所要時間**: 1.5-2時間

- `HistogramGenerator` クラスを作成
- ヒストグラムのビン分けロジック
- SVG生成ロジック

### タスク4: StatisticsReportGenerator の実装

**所要時間**: 1-1.5時間

- `StatisticsReportGenerator` クラスを作成
- HTML生成ロジック
- グラフの埋め込み

### タスク5: Main.kt の更新

**所要時間**: 0.5時間

- 複数ゲーム実行後に統計レポートを生成
- CLI出力の調整

### タスク6: テスト

**所要時間**: 0.5-1時間

- グラフ生成のテスト
- HTMLレポートの視覚確認
- ブラウザでの表示確認

---

## 実装順序

1. **データモデル** → ChartData, BarChartData, HistogramData
2. **棒グラフ** → BarChartGenerator
3. **ヒストグラム** → HistogramGenerator
4. **統計レポート** → StatisticsReportGenerator
5. **CLI統合** → Main.kt の更新
6. **テスト** → 動作確認

---

## 成功基準

### 機能要件

- [ ] 勝率の棒グラフが生成される
- [ ] ターン数のヒストグラムが生成される
- [ ] 統計レポートHTMLが生成される
- [ ] グラフがブラウザで正しく表示される
- [ ] グラフが視覚的に分かりやすい

### 非機能要件

- [ ] SVGが有効なXMLである
- [ ] グラフが読みやすい（適切なサイズ、色、ラベル）
- [ ] レスポンシブデザインである
- [ ] 外部ライブラリに依存しない

---

## SVG生成の方針

### 1. 手動でSVG生成

外部ライブラリを使わず、Kotlinの文字列操作でSVGを生成：

```kotlin
fun generateBarChart(data: BarChartData): String {
    return """
        <svg width="600" height="400" xmlns="http://www.w3.org/2000/svg">
            <!-- グラフの内容 -->
        </svg>
    """.trimIndent()
}
```

### 2. SVGの構成要素

- `<rect>`: 棒グラフの棒
- `<text>`: ラベル、軸の目盛り
- `<line>`: 軸、グリッド線
- `<g>`: グループ化

---

## HTMLレポートの構成

```
┌─────────────────────────────────────┐
│  Statistics Report                  │
├─────────────────────────────────────┤
│  Game Summary                       │
│  - Total Games: 100                 │
│  - Date: 2025-11-16                 │
├─────────────────────────────────────┤
│  Player Win Rates                   │
│  [棒グラフ]                          │
├─────────────────────────────────────┤
│  Player Statistics Table            │
│  (Phase 6のデータ)                   │
├─────────────────────────────────────┤
│  Turn Distribution                  │
│  [ヒストグラム]                      │
├─────────────────────────────────────┤
│  Turn Statistics Table              │
│  (Phase 6のデータ)                   │
└─────────────────────────────────────┘
```

---

## リスクと対策

| リスク | 影響 | 対策 |
|-------|-----|-----|
| SVG生成の複雑さ | 実装時間増加 | シンプルなデザインに留める |
| ブラウザ互換性 | 表示崩れ | 標準的なSVG仕様を使用 |
| グラフの読みにくさ | ユーザー体験低下 | 適切な色、サイズ、ラベルを設定 |

---

## Phase 9への橋渡し

Phase 7で実装するグラフ生成の仕組みは、Phase 9で以下のように拡張されます：

- より詳細な統計グラフ（折れ線グラフ、散布図など）
- プロパティ別収益性のグラフ
- ターンごとの資産推移グラフ

Phase 7では「基本的なグラフ生成」に集中し、Phase 9で「高度な可視化」を追加する方針です。

---

**作成日**: 2025-11-16
