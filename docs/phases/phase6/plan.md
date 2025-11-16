# Phase 6 実装計画

**Phase 6**: 基本統計収集機能の実装

---

## 目標

複数ゲームの結果を集計し、統計データとしてファイルに保存できるようにする

---

## 背景

Phase 5で複数ゲーム実行の基盤が完成しました。Phase 6では、実行結果を統計として集計し、JSON/CSV形式でファイルに保存する機能を追加します。

これにより、Phase 7でのグラフ可視化や、研究分析の基盤が整います。

---

## スコープ

### 実装する機能

1. **統計データの計算**
   - プレイヤー別勝率
   - 平均ゲームターン数
   - 平均最終資産額
   - ターン数の標準偏差
   - 最短/最長ゲーム情報

2. **JSON形式での出力**
   - 統計データをJSON形式で保存
   - ファイル名: `simulation-stats-{timestamp}.json`
   - 人間が読みやすいフォーマット（pretty print）

3. **CSV形式での出力**
   - ゲーム別の詳細データをCSV形式で保存
   - ファイル名: `simulation-results-{timestamp}.csv`
   - Excel等で開いて分析可能

4. **CLI オプションの拡張**
   - `--export-json`: JSON形式でエクスポート
   - `--export-csv`: CSV形式でエクスポート
   - デフォルト: 両方エクスポート（複数ゲーム実行時のみ）

### 実装しない機能

- グラフ生成（Phase 7で実装）
- 詳細な統計分析（Phase 9で実装）
- データベース保存（将来的な拡張として検討）
- リアルタイムストリーミング（Phase 13で検討）

---

## 設計方針

### 1. StatisticsCalculator クラス

統計計算を担当：

```kotlin
class StatisticsCalculator {
    fun calculate(result: MultiGameResult): GameStatistics
}
```

### 2. GameStatistics データクラス

統計データを保持：

```kotlin
data class GameStatistics(
    val totalGames: Int,
    val playerStats: Map<String, PlayerStatistics>,
    val gameStats: TurnStatistics,
    val timestamp: Long,
)

data class PlayerStatistics(
    val playerName: String,
    val wins: Int,
    val winRate: Double,
    val averageFinalAssets: Double,
    val averageFinalCash: Double,
    val averagePropertiesOwned: Double,
)

data class TurnStatistics(
    val averageTurns: Double,
    val minTurns: Int,
    val maxTurns: Int,
    val standardDeviation: Double,
)
```

### 3. エクスポート機能

- `JsonExporter`: JSON形式でエクスポート
- `CsvExporter`: CSV形式でエクスポート

---

## タスク分解

### タスク1: 統計データモデルの作成

**所要時間**: 0.5時間

- `GameStatistics` データクラスを作成
- `PlayerStatistics` データクラスを作成
- `TurnStatistics` データクラスを作成

### タスク2: StatisticsCalculator の実装

**所要時間**: 1-1.5時間

- `StatisticsCalculator` クラスを作成
- 統計計算ロジックの実装
  - プレイヤー別統計
  - ターン統計
  - 標準偏差の計算

### タスク3: JsonExporter の実装

**所要時間**: 0.5-1時間

- `JsonExporter` クラスを作成
- Kotlinの標準ライブラリでJSON生成
- ファイル保存機能

### タスク4: CsvExporter の実装

**所要時間**: 0.5-1時間

- `CsvExporter` クラスを作成
- CSV形式での出力
- ヘッダー行の生成

### タスク5: CLI引数の拡張

**所要時間**: 0.5時間

- `--export-json` オプションの追加
- `--export-csv` オプションの追加
- GameConfig の拡張

### タスク6: Main.kt の更新

**所要時間**: 0.5-1時間

- エクスポート機能の統合
- 複数ゲーム実行後に統計をエクスポート

### タスク7: テスト

**所要時間**: 0.5-1時間

- `StatisticsCalculator` のテスト
- エクスポート機能のテスト
- 統合テスト

---

## 実装順序

1. **データモデル** → GameStatistics, PlayerStatistics, TurnStatistics
2. **統計計算** → StatisticsCalculator
3. **エクスポート機能** → JsonExporter, CsvExporter
4. **CLI統合** → Main.kt の更新
5. **テスト** → 動作確認

---

## 成功基準

### 機能要件

- [ ] 複数ゲーム実行後に統計が計算される
- [ ] JSON形式でエクスポートできる
- [ ] CSV形式でエクスポートできる
- [ ] 統計データが正確である（勝率、平均値など）
- [ ] ファイルが正しい形式で保存される

### 非機能要件

- [ ] 10,000ゲームの統計計算が1秒以内に完了する
- [ ] JSONファイルが人間が読みやすい形式である
- [ ] CSVファイルがExcelで開ける
- [ ] ファイル名に衝突が発生しない（タイムスタンプ使用）

---

## ファイル形式の例

### JSON形式

```json
{
  "totalGames": 100,
  "timestamp": 1700000000000,
  "playerStats": {
    "Alice": {
      "playerName": "Alice",
      "wins": 58,
      "winRate": 0.58,
      "averageFinalAssets": 1234.5,
      "averageFinalCash": 678.9,
      "averagePropertiesOwned": 3.2
    },
    "Bob": {
      "playerName": "Bob",
      "wins": 42,
      "winRate": 0.42,
      "averageFinalAssets": 1100.2,
      "averageFinalCash": 550.3,
      "averagePropertiesOwned": 2.8
    }
  },
  "turnStats": {
    "averageTurns": 52.3,
    "minTurns": 35,
    "maxTurns": 78,
    "standardDeviation": 8.5
  }
}
```

### CSV形式

```csv
GameNumber,Winner,TotalTurns,AliceFinalAssets,AliceFinalCash,AliceProperties,BobFinalAssets,BobFinalCash,BobProperties
1,Alice,50,1500,800,4,1200,600,3
2,Bob,45,1100,500,3,1400,700,4
3,Alice,55,1600,900,5,1000,400,2
...
```

---

## リスクと対策

| リスク | 影響 | 対策 |
|-------|-----|-----|
| JSON生成の複雑さ | 実装時間増加 | Kotlinの標準ライブラリで手動生成 |
| 大量データでのメモリ不足 | CSV生成時にクラッシュ | ストリーミング書き込みを使用 |
| ファイル名の衝突 | データ上書き | タイムスタンプをミリ秒単位で使用 |

---

## Phase 7への橋渡し

Phase 6で保存したJSON/CSVファイルは、Phase 7で以下のように活用されます：

- JSONファイル: グラフ生成の入力データとして使用
- CSVファイル: Excel等での手動分析に使用
- 統計データ: HTMLレポートに統計セクションとして追加

Phase 6では「データの保存」に集中し、Phase 7で「可視化」を追加する方針です。

---

**作成日**: 2025-11-16
