# Phase 6 ドメイン用語集

**Phase 6**: 基本統計収集機能の実装

---

## 1. 統計関連用語

### 1.1 Statistics（統計）

**定義**: 複数のゲーム結果から計算される集計データ

**Phase 6での範囲**:
- プレイヤー別勝率
- 平均ターン数
- 平均最終資産
- 標準偏差

**Phase 9以降での拡張**:
- プロパティ別収益性
- ターンごとの資産推移
- 破産タイミング分析

### 1.2 Game Statistics（ゲーム統計）

**定義**: 全体の統計データを保持するデータクラス

**含まれる情報**:
```kotlin
data class GameStatistics(
    val totalGames: Int,              // 総ゲーム数
    val playerStats: Map<String, PlayerStatistics>,  // プレイヤー別統計
    val turnStats: TurnStatistics,    // ターン統計
    val timestamp: Long,              // タイムスタンプ
)
```

---

## 2. プレイヤー統計用語

### 2.1 Player Statistics（プレイヤー統計）

**定義**: 特定プレイヤーの統計データ

**含まれる情報**:
- 勝利数（wins）
- 勝率（winRate）
- 平均最終資産（averageFinalAssets）
- 平均最終現金（averageFinalCash）
- 平均所有プロパティ数（averagePropertiesOwned）

### 2.2 Win Rate（勝率）

**定義**: プレイヤーが勝利した割合

**計算式**:
```
勝率 = プレイヤーの勝利数 / 総ゲーム数
```

**値の範囲**: 0.0〜1.0（パーセント表示時は0%〜100%）

**例**:
- 100ゲーム中58勝 → 勝率 = 0.58 (58%)

### 2.3 Average Final Assets（平均最終資産）

**定義**: 全ゲームにおけるプレイヤーの最終資産の平均値

**計算式**:
```
平均最終資産 = Σ(各ゲームの最終資産) / 総ゲーム数
```

**最終資産の定義**: 現金 + 所有プロパティの価格合計

### 2.4 Average Final Cash（平均最終現金）

**定義**: 全ゲームにおけるプレイヤーの最終現金の平均値

**Phase 6での用途**: プレイヤーの現金保有傾向を分析

### 2.5 Average Properties Owned（平均所有プロパティ数）

**定義**: 全ゲームにおけるプレイヤーの所有プロパティ数の平均値

**Phase 6での用途**: プレイヤーのプロパティ取得傾向を分析

---

## 3. ターン統計用語

### 3.1 Turn Statistics（ターン統計）

**定義**: ゲームのターン数に関する統計データ

**含まれる情報**:
```kotlin
data class TurnStatistics(
    val averageTurns: Double,       // 平均ターン数
    val minTurns: Int,              // 最短ターン数
    val maxTurns: Int,              // 最長ターン数
    val standardDeviation: Double,  // 標準偏差
)
```

### 3.2 Average Turns（平均ターン数）

**定義**: 全ゲームのターン数の平均値

**計算式**:
```
平均ターン数 = Σ(各ゲームのターン数) / 総ゲーム数
```

**Phase 6での用途**: ゲームの長さの傾向を把握

### 3.3 Min/Max Turns（最短/最長ターン数）

**定義**: 全ゲーム中の最短および最長のゲームのターン数

**Phase 6での用途**: ゲームの長さのばらつきを把握

### 3.4 Standard Deviation（標準偏差）

**定義**: データのばらつきの度合いを示す統計量

**記号**: σ（シグマ）

**計算式**:
```
σ = sqrt(Σ(x - μ)² / N)

x: 各ゲームのターン数
μ: 平均ターン数
N: ゲーム数
```

**意味**:
- 標準偏差が小さい → ゲームのターン数が安定している
- 標準偏差が大きい → ゲームのターン数にばらつきがある

**例**:
- 平均50ターン、標準偏差5 → ほとんどのゲームが45〜55ターン
- 平均50ターン、標準偏差20 → ゲームの長さにばらつきが大きい

---

## 4. エクスポート関連用語

### 4.1 Export（エクスポート）

**定義**: データをファイル形式で外部に出力すること

**Phase 6でのエクスポート形式**:
- JSON形式
- CSV形式

### 4.2 JSON (JavaScript Object Notation)

**定義**: 軽量なデータ交換フォーマット

**特徴**:
- 人間が読みやすい
- 機械が解析しやすい
- プログラム間でのデータ交換に適している

**Phase 6での用途**: 統計データの保存

**ファイル名パターン**: `simulation-stats-{timestamp}.json`

**例**:
```json
{
  "totalGames": 100,
  "playerStats": {
    "Alice": {
      "wins": 58,
      "winRate": 0.58
    }
  }
}
```

### 4.3 CSV (Comma-Separated Values)

**定義**: カンマ区切りのテキストファイル形式

**特徴**:
- Excel等の表計算ソフトで開ける
- 行ごとにデータを記録
- 1行目はヘッダー（列名）

**Phase 6での用途**: ゲーム別詳細データの保存

**ファイル名パターン**: `simulation-results-{timestamp}.csv`

**例**:
```csv
GameNumber,Winner,TotalTurns,AliceFinalAssets
1,Alice,50,1500
2,Bob,45,1200
```

---

## 5. データ処理用語

### 5.1 Statistics Calculator（統計計算機）

**定義**: MultiGameResultから統計データを計算するクラス

**責務**:
- プレイヤー別統計の計算
- ターン統計の計算
- GameStatisticsの生成

**パッケージ**: `com.monopoly.statistics`

### 5.2 Json Exporter（JSONエクスポーター）

**定義**: 統計データをJSON形式でエクスポートするクラス

**責務**:
- GameStatisticsをJSON文字列に変換
- ファイルへの保存
- ファイル名の生成

**パッケージ**: `com.monopoly.export`

### 5.3 CSV Exporter（CSVエクスポーター）

**定義**: ゲーム結果をCSV形式でエクスポートするクラス

**責務**:
- MultiGameResultをCSV文字列に変換
- ヘッダー行の生成
- ファイルへの保存

**パッケージ**: `com.monopoly.export`

---

## 6. ファイル命名用語

### 6.1 Timestamp（タイムスタンプ）

**定義**: ファイル生成時刻を示す文字列

**形式**: `yyyyMMdd_HHmmss`

**例**: `20251116_143052`（2025年11月16日 14:30:52）

**Phase 6での用途**: ファイル名の一意性を保証

### 6.2 File Naming Pattern（ファイル命名パターン）

Phase 6で生成されるファイル：

| 種類 | パターン | 例 |
|------|---------|---|
| JSON統計 | `simulation-stats-{timestamp}.json` | `simulation-stats-20251116_143052.json` |
| CSV結果 | `simulation-results-{timestamp}.csv` | `simulation-results-20251116_143052.csv` |

---

## 7. CLI オプション用語

### 7.1 --export-json オプション

**定義**: JSON形式のみでエクスポート

**使用法**: `--export-json`

**動作**:
- JSON形式で統計をエクスポート
- CSV形式はエクスポートしない

### 7.2 --export-csv オプション

**定義**: CSV形式のみでエクスポート

**使用法**: `--export-csv`

**動作**:
- CSV形式でゲーム結果をエクスポート
- JSON形式はエクスポートしない

### 7.3 --no-export オプション

**定義**: エクスポートを抑制

**使用法**: `--no-export`

**動作**:
- JSON/CSV両方ともエクスポートしない
- ターミナル表示のみ

---

## 8. 統計計算用語

### 8.1 Variance（分散）

**定義**: 標準偏差の2乗

**計算式**:
```
分散 = Σ(x - μ)² / N
```

**Phase 6での用途**: 標準偏差を計算する際の中間値

### 8.2 Mean（平均値）

**定義**: データの合計をデータ数で割った値

**別名**: Average（平均）

**Phase 6での使用箇所**:
- 平均ターン数
- 平均最終資産
- 平均最終現金

### 8.3 Aggregation（集計）

**定義**: 複数のデータを集めて計算すること

**Phase 6での集計対象**:
- プレイヤー別の勝利数
- ゲーム別のターン数
- プレイヤー別の最終資産

---

## 9. データ構造用語

### 9.1 Map<String, PlayerStatistics>

**定義**: プレイヤー名をキーとするプレイヤー統計のマップ

**例**:
```kotlin
mapOf(
    "Alice" to PlayerStatistics(...),
    "Bob" to PlayerStatistics(...),
)
```

**Phase 6での用途**: プレイヤー名から統計データを高速に取得

---

## 10. パフォーマンス用語

### 10.1 In-Memory Processing（インメモリ処理）

**定義**: 全データをメモリ上に保持して処理すること

**Phase 6の方針**: インメモリ処理（10,000ゲーム程度を想定）

**Phase 9での改善**: ストリーミング処理の検討（100,000ゲーム以上）

### 10.2 Streaming Processing（ストリーミング処理）

**定義**: データを少しずつ読み込みながら処理すること

**Phase 6では未実装**: 将来的な拡張として検討

---

## 11. 対比表

| 用語 | Phase 5 | Phase 6 |
|------|---------|---------|
| データ保存 | なし | JSON/CSV |
| 統計計算 | 基本的（勝率、平均） | 詳細（標準偏差含む） |
| エクスポート | なし | JsonExporter, CsvExporter |
| パッケージ | simulation, cli | simulation, cli, statistics, export |
| ファイル生成 | HTMLレポートのみ | HTMLレポート + JSON + CSV |

---

## 12. 将来の拡張用語（参考）

Phase 7以降で追加予定：

- **Visualization（可視化）**: グラフ生成（Phase 7）
- **Advanced Statistics（高度な統計）**: 詳細分析（Phase 9）
- **Data Export（データエクスポート）**: データベース保存（将来）

---

**作成日**: 2025-11-16
