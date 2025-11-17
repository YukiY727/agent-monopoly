# Phase 4 ドメイン用語集

**Phase 4**: 結果サマリーレポート生成機能の実装

---

## 1. レポート関連用語

### 1.1 Summary Report（サマリーレポート）

**定義**: ゲーム結果に特化したコンパクトなレポート

**特徴**:
- 勝者情報を強調
- プレイヤーランキングを表示
- プロパティ所有詳細を表示
- イベントタイムラインは含めない
- 詳細レポートより軽量

**対比**: Detailed Report（詳細レポート、Phase 2）

### 1.2 Detailed Report（詳細レポート）

**定義**: Phase 2で実装した、全イベントタイムラインを含む詳細なレポート

**特徴**:
- 全イベントを時系列表示
- ゲームの流れを詳細に追跡
- ファイルサイズが大きい

---

## 2. 統計関連用語

### 2.1 Game Statistics（ゲーム統計）

**定義**: ゲーム全体の集計情報

**含まれる情報**:
- Total Turns（総ターン数）
- Total Players（総プレイヤー数）
- Bankruptcies（破産者数）
- Properties Purchased（購入プロパティ数）
- Total Rent Paid（総レント支払額）

### 2.2 Total Assets（総資産）

**定義**: プレイヤーの現金とプロパティ価値の合計

**計算式**:
```
総資産 = 現金 + (所有プロパティの価格合計)
```

**実装**: `Player.getTotalAssets()`

### 2.3 Player Ranking（プレイヤーランキング）

**定義**: 総資産でソートされたプレイヤーの順位

**特徴**:
- 総資産の降順でソート
- 1位〜3位にメダル表示（🥇🥈🥉）
- 破産プレイヤーも含む

---

## 3. UI関連用語

### 3.1 Winner Badge（勝者バッジ）

**定義**: 勝者を強調表示するUI要素

**表示内容**:
```
🏆 Winner: Alice
```

### 3.2 Rank Emoji（ランク絵文字）

**定義**: 順位を視覚的に表現する絵文字

- 1位: 🥇（ゴールドメダル）
- 2位: 🥈（シルバーメダル）
- 3位: 🥉（ブロンズメダル）

### 3.3 Status Badge（ステータスバッジ）

**定義**: プレイヤーの状態を表示するラベル

- `ACTIVE`: アクティブなプレイヤー（緑色）
- `BANKRUPT`: 破産したプレイヤー（赤色）

---

## 4. データ構造用語

### 4.1 GameStatistics

**型**: `data class`

**フィールド**:
```kotlin
data class GameStatistics(
    val totalTurns: Int,           // 総ターン数
    val totalPlayers: Int,         // 総プレイヤー数
    val bankruptcies: Int,         // 破産者数
    val propertiesPurchased: Int,  // 購入プロパティ数
    val totalRentPaid: Int,        // 総レント支払額
)
```

### 4.2 PlayerRankingEntry

**型**: `data class`

**フィールド**:
```kotlin
data class PlayerRankingEntry(
    val player: Player,      // プレイヤー
    val rank: Int,           // 順位（1〜）
    val totalAssets: Int,    // 総資産
)
```

---

## 5. HTMLセクション用語

### 5.1 Winner Section（勝者セクション）

**定義**: 勝者の情報を目立つように表示するセクション

**表示内容**:
- Winner Badge
- 最終資産額
- 現金
- 所有プロパティ数

### 5.2 Statistics Section（統計セクション）

**定義**: ゲーム全体の統計情報を表示するセクション

**表示内容**:
- 総ターン数
- プレイヤー数
- 破産者数
- 購入プロパティ数

### 5.3 Ranking Section（ランキングセクション）

**定義**: プレイヤーを総資産でランキング表示するセクション

**表示項目**:
- Rank（順位）
- Player（プレイヤー名）
- Status（ステータス）
- Final Assets（最終資産）
- Cash（現金）
- Properties（プロパティ数）

### 5.4 Properties Section（プロパティセクション）

**定義**: 各プレイヤーの所有プロパティ詳細を表示するセクション

**表示項目**:
- Property（プロパティ名）
- Price（価格）
- Rent（レント）
- Color Group（色グループ）

---

## 6. ファイル命名用語

### 6.1 Filename Pattern（ファイル名パターン）

Phase 2との対比:

| Phase | パターン | 例 |
|-------|---------|---|
| Phase 2 | `game-report-{timestamp}.html` | `game-report-20251116_143052.html` |
| Phase 4 | `game-summary-{timestamp}.html` | `game-summary-20251116_143052.html` |

### 6.2 Timestamp Format（タイムスタンプ形式）

**形式**: `yyyyMMdd_HHmmss`

**例**: `20251116_143052`（2025年11月16日 14:30:52）

---

## 7. CSS関連用語

### 7.1 Color Scheme（カラースキーム）

**定義**: レポート全体で統一されたカラーパレット

```css
:root {
    --color-primary: #2c3e50;   /* プライマリカラー */
    --color-winner: #27ae60;    /* 勝者カラー */
    --color-bankrupt: #e74c3c;  /* 破産カラー */
    --color-active: #3498db;    /* アクティブカラー */
    --color-bg: #f5f5f5;        /* 背景色 */
    --color-card: #ffffff;      /* カード色 */
}
```

### 7.2 Gradient Background（グラデーション背景）

**定義**: 勝者セクションに使用する2色のグラデーション

```css
background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
```

### 7.3 Rank Colors（ランクカラー）

**定義**: 順位に応じた背景色

- 1位: `#ffd700`（ゴールド）
- 2位: `#c0c0c0`（シルバー）
- 3位: `#cd7f32`（ブロンズ）

---

## 8. 実装クラス用語

### 8.1 SummaryReportGenerator

**定義**: サマリーレポートを生成するクラス

**責務**:
- HTMLの生成
- ファイルへの保存
- 統計情報の計算
- ランキングの生成

### 8.2 HtmlReportGenerator

**定義**: Phase 2で実装した詳細レポート生成クラス

**責務**:
- 詳細レポートの生成
- イベントタイムラインの表示

---

## 9. 対比表

| 用語 | Phase 2 | Phase 4 |
|------|---------|---------|
| レポート種類 | Detailed Report | Summary Report |
| ファイル名 | game-report-*.html | game-summary-*.html |
| メインコンテンツ | Event Timeline | Player Ranking + Property Details |
| クラス名 | HtmlReportGenerator | SummaryReportGenerator |
| ファイルサイズ | 大 | 小 |

---

**作成日**: 2025-11-16
