# Phase 4 コーディングガイド

**Phase 4**: 結果サマリーレポート生成機能の実装

---

## 1. 基本方針

### 1.1 Phase 2のコードは変更しない

- `HtmlReportGenerator`は既存のまま維持
- 新しく`SummaryReportGenerator`を追加
- 両方のレポートを独立して生成

### 1.2 コードの再利用

Phase 2で実装した以下のパターンを再利用：

```kotlin
class SummaryReportGenerator {
    fun generate(gameState: GameState): String {
        // HTMLを生成
    }

    fun saveToFile(gameState: GameState, filename: String = generateFilename()): File {
        val html = generate(gameState)
        val file = File(filename)
        file.writeText(html)
        return file
    }

    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "game-summary-$timestamp.html"
    }
}
```

---

## 2. HTML生成パターン

### 2.1 セクション分割

各セクションを独立した関数で生成：

```kotlin
private fun generateWinnerSection(gameState: GameState): String {
    val winner = gameState.players.maxByOrNull { it.getTotalAssets() }
        ?: return "<p>No winner found</p>"

    return """
        <div class="winner-section">
            <div class="winner-badge">🏆 Winner: ${winner.name}</div>
            <div class="winner-stats">
                ${generateWinnerStats(winner)}
            </div>
        </div>
    """.trimIndent()
}
```

### 2.2 文字列補間を活用

Kotlinの文字列補間とトリプルクォートを使用：

```kotlin
private fun generatePlayerRow(entry: PlayerRankingEntry, index: Int): String {
    val rankEmoji = when (index) {
        0 -> "🥇"
        1 -> "🥈"
        2 -> "🥉"
        else -> ""
    }

    val statusClass = if (entry.player.isBankrupt) "status-bankrupt" else "status-active"
    val statusText = if (entry.player.isBankrupt) "BANKRUPT" else "ACTIVE"
    val bankruptClass = if (entry.player.isBankrupt) " bankrupted" else ""

    return """
        <tr class="rank-${index + 1}$bankruptClass">
            <td>${index + 1}${if (index < 3) " $rankEmoji" else ""}</td>
            <td>${entry.player.name}</td>
            <td class="$statusClass">$statusText</td>
            <td>\$${entry.totalAssets}</td>
            <td>\$${entry.player.money}</td>
            <td>${entry.player.ownedProperties.size}</td>
        </tr>
    """.trimIndent()
}
```

---

## 3. 統計計算パターン

### 3.1 イベントフィルタリング

特定のイベントタイプだけを集計：

```kotlin
// 購入イベントをカウント
val purchaseEvents = gameState.events.filterIsInstance<GameEvent.PropertyPurchased>()
val propertiesPurchased = purchaseEvents.size

// レント支払いイベントを集計
val rentEvents = gameState.events.filterIsInstance<GameEvent.RentPaid>()
val totalRentPaid = rentEvents.sumOf { it.amount }
```

### 3.2 プレイヤーランキング

総資産でソートしてランク付け：

```kotlin
fun getPlayerRanking(gameState: GameState): List<PlayerRankingEntry> {
    return gameState.players
        .map { player ->
            PlayerRankingEntry(
                player = player,
                rank = 0,
                totalAssets = player.getTotalAssets(),
            )
        }
        .sortedByDescending { it.totalAssets }
        .mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
}
```

---

## 4. CSS設計パターン

### 4.1 CSS変数の使用

Phase 2と同様にCSS変数で統一感を出す：

```css
:root {
    --color-primary: #2c3e50;
    --color-winner: #27ae60;
    --color-bankrupt: #e74c3c;
    --color-active: #3498db;
    --color-bg: #f5f5f5;
    --color-card: #ffffff;
}
```

### 4.2 グラデーションの活用

勝者セクションは目立つグラデーション：

```css
.winner-section {
    background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
    padding: 30px;
    border-radius: 15px;
    text-align: center;
    margin-bottom: 30px;
}
```

### 4.3 ランク別スタイル

```css
.ranking-table .rank-1 {
    background-color: #ffd700; /* ゴールド */
}

.ranking-table .rank-2 {
    background-color: #c0c0c0; /* シルバー */
}

.ranking-table .rank-3 {
    background-color: #cd7f32; /* ブロンズ */
}
```

---

## 5. データクラス設計

### 5.1 統計情報

```kotlin
data class GameStatistics(
    val totalTurns: Int,
    val totalPlayers: Int,
    val bankruptcies: Int,
    val propertiesPurchased: Int,
    val totalRentPaid: Int,
)
```

### 5.2 ランキングエントリ

```kotlin
data class PlayerRankingEntry(
    val player: Player,
    val rank: Int,
    val totalAssets: Int,
)
```

---

## 6. エスケープ処理

### 6.1 HTMLエスケープ

プレイヤー名など外部入力はエスケープ（Phase 2と同様）：

```kotlin
private fun escapeHtml(text: String): String =
    text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
```

---

## 7. Phase 2との差分

| 要素 | Phase 2 | Phase 4 |
|------|---------|---------|
| クラス名 | `HtmlReportGenerator` | `SummaryReportGenerator` |
| ファイル名 | `game-report-*.html` | `game-summary-*.html` |
| メインコンテンツ | イベントタイムライン | プレイヤーランキング＋プロパティ詳細 |
| 統計情報 | 基本的 | より詳細 |

---

## 8. Main.ktの統合

両方のレポートを生成：

```kotlin
// Phase 2 - 詳細レポート
val htmlReportGenerator = HtmlReportGenerator()
val detailedReportFile = htmlReportGenerator.saveToFile(gameState)
println("Detailed report generated: ${detailedReportFile.absolutePath}")

// Phase 4 - サマリーレポート
val summaryReportGenerator = SummaryReportGenerator()
val summaryReportFile = summaryReportGenerator.saveToFile(gameState)
println("Summary report generated: ${summaryReportFile.absolutePath}")
```

---

## 9. 命名規則

### 9.1 変数名

- `summaryReportGenerator`: サマリーレポート生成器
- `detailedReportFile`: 詳細レポートファイル（Phase 2）
- `summaryReportFile`: サマリーレポートファイル（Phase 4）

### 9.2 関数名

- `generateWinnerSection()`: 勝者情報セクションを生成
- `generatePlayerRankingSection()`: ランキングセクションを生成
- `generatePropertyDetailsSection()`: プロパティ詳細セクションを生成
- `generateStatisticsSection()`: 統計情報セクションを生成

---

## 10. テストの考え方

Phase 4では主にHTMLレポート生成のため、手動テストが中心：

1. **ゲーム実行**: 実際にゲームを実行してレポート生成
2. **HTMLファイル確認**: ブラウザで開いて視覚的に確認
3. **複数パターン**: 異なる戦略でゲーム実行して確認

必要に応じて以下の単体テストを追加：
- `calculateStatistics()`のロジック
- `getPlayerRanking()`のソート順
- HTMLエスケープ処理

---

**作成日**: 2025-11-16
