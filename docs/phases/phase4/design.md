# Phase 4 詳細設計

**目標**: ゲーム結果をサマリー形式で確認できるようにする

---

## 1. 設計概要

Phase 4では、Phase 2で実装した詳細レポートとは別に、結果に特化したサマリーレポートを実装します。

### 設計方針

1. **2つのレポートの併存**
   - HtmlReportGenerator（Phase 2）: 詳細レポート
   - SummaryReportGenerator（Phase 4）: サマリーレポート
   - 両方のレポートを生成

2. **サマリーレポートの特徴**
   - コンパクトで見やすい
   - 結果の要約に特化
   - イベントタイムラインは含めない
   - 統計情報とランキングを重視

3. **Phase 2のコードは維持**
   - HtmlReportGeneratorは変更なし
   - Main.ktで両方のレポートを生成

---

## 2. クラス設計

### 2.1 SummaryReportGenerator

```kotlin
class SummaryReportGenerator {
    /**
     * ゲーム状態からサマリーレポートを生成
     */
    fun generate(gameState: GameState): String {
        // HTMLを生成して返す
    }

    /**
     * サマリーレポートをファイルに保存
     */
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

    // 以下、内部メソッド
    private fun generateWinnerSection(gameState: GameState): String
    private fun generatePlayerRankingSection(gameState: GameState): String
    private fun generatePropertyDetailsSection(gameState: GameState): String
    private fun generateStatisticsSection(gameState: GameState): String
    private fun getStyleSheet(): String
}
```

---

## 3. レポート構成

### 3.1 HTML構造

```html
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>Monopoly Game Summary</title>
    <style>...</style>
</head>
<body>
    <div class="container">
        <h1>🏆 Game Summary</h1>

        <!-- 勝者情報 -->
        <div class="winner-section">
            ...
        </div>

        <!-- ゲーム統計 -->
        <div class="statistics-section">
            ...
        </div>

        <!-- プレイヤーランキング -->
        <div class="ranking-section">
            ...
        </div>

        <!-- 所有プロパティ詳細 -->
        <div class="properties-section">
            ...
        </div>
    </div>
</body>
</html>
```

### 3.2 勝者情報セクション

```html
<div class="winner-section">
    <div class="winner-badge">🏆 Winner: Alice</div>
    <div class="winner-stats">
        <div class="stat">
            <span class="stat-label">Final Assets</span>
            <span class="stat-value">$2,500</span>
        </div>
        <div class="stat">
            <span class="stat-label">Cash</span>
            <span class="stat-value">$1,500</span>
        </div>
        <div class="stat">
            <span class="stat-label">Properties</span>
            <span class="stat-value">5</span>
        </div>
    </div>
</div>
```

### 3.3 ゲーム統計セクション

```html
<div class="statistics-section">
    <h2>📊 Game Statistics</h2>
    <table class="stats-table">
        <tr>
            <th>Total Turns</th>
            <td>50</td>
        </tr>
        <tr>
            <th>Players</th>
            <td>2</td>
        </tr>
        <tr>
            <th>Bankruptcies</th>
            <td>1</td>
        </tr>
        <tr>
            <th>Properties Purchased</th>
            <td>5</td>
        </tr>
    </table>
</div>
```

### 3.4 プレイヤーランキングセクション

```html
<div class="ranking-section">
    <h2>👥 Player Ranking</h2>
    <table class="ranking-table">
        <thead>
            <tr>
                <th>Rank</th>
                <th>Player</th>
                <th>Status</th>
                <th>Final Assets</th>
                <th>Cash</th>
                <th>Properties</th>
            </tr>
        </thead>
        <tbody>
            <tr class="rank-1">
                <td>1st 🥇</td>
                <td>Alice</td>
                <td class="status-active">ACTIVE</td>
                <td>$2,500</td>
                <td>$1,500</td>
                <td>5</td>
            </tr>
            <tr class="rank-2 bankrupted">
                <td>2nd 🥈</td>
                <td>Bob</td>
                <td class="status-bankrupt">BANKRUPT</td>
                <td>$0</td>
                <td>$0</td>
                <td>0</td>
            </tr>
        </tbody>
    </table>
</div>
```

### 3.5 所有プロパティ詳細セクション

```html
<div class="properties-section">
    <h2>🏠 Property Details</h2>

    <div class="player-properties">
        <h3>Alice's Properties</h3>
        <table class="properties-table">
            <thead>
                <tr>
                    <th>Property</th>
                    <th>Price</th>
                    <th>Rent</th>
                    <th>Color Group</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>Mediterranean Avenue</td>
                    <td>$60</td>
                    <td>$2</td>
                    <td class="color-brown">BROWN</td>
                </tr>
                <!-- ... -->
            </tbody>
        </table>
    </div>

    <div class="player-properties">
        <h3>Bob's Properties</h3>
        <p class="no-properties">No properties owned</p>
    </div>
</div>
```

---

## 4. CSS設計

### 4.1 カラースキーム

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

### 4.2 主要スタイル

```css
/* 勝者セクション */
.winner-section {
    background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
    padding: 30px;
    border-radius: 15px;
    text-align: center;
    margin-bottom: 30px;
}

.winner-badge {
    font-size: 2em;
    font-weight: bold;
    color: var(--color-winner);
}

/* ランキングテーブル */
.ranking-table .rank-1 {
    background-color: #ffd700; /* ゴールド */
}

.ranking-table .rank-2 {
    background-color: #c0c0c0; /* シルバー */
}

.ranking-table .rank-3 {
    background-color: #cd7f32; /* ブロンズ */
}

/* 破産プレイヤー */
.bankrupted {
    opacity: 0.6;
    background-color: #fadbd8 !important;
}
```

---

## 5. 統計情報の計算

### 5.1 計算するメトリクス

```kotlin
data class GameStatistics(
    val totalTurns: Int,
    val totalPlayers: Int,
    val bankruptcies: Int,
    val propertiesPurchased: Int,
    val totalRentPaid: Int,
)

fun calculateStatistics(gameState: GameState): GameStatistics {
    val bankruptcies = gameState.players.count { it.isBankrupt }

    val purchaseEvents = gameState.events.filterIsInstance<GameEvent.PropertyPurchased>()
    val propertiesPurchased = purchaseEvents.size

    val rentEvents = gameState.events.filterIsInstance<GameEvent.RentPaid>()
    val totalRentPaid = rentEvents.sumOf { it.amount }

    return GameStatistics(
        totalTurns = gameState.turnNumber,
        totalPlayers = gameState.players.size,
        bankruptcies = bankruptcies,
        propertiesPurchased = propertiesPurchased,
        totalRentPaid = totalRentPaid,
    )
}
```

### 5.2 プレイヤーランキング

```kotlin
fun getPlayerRanking(gameState: GameState): List<PlayerRankingEntry> {
    return gameState.players
        .map { player ->
            PlayerRankingEntry(
                player = player,
                rank = 0, // あとで設定
                totalAssets = player.getTotalAssets(),
            )
        }
        .sortedByDescending { it.totalAssets }
        .mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
}

data class PlayerRankingEntry(
    val player: Player,
    val rank: Int,
    val totalAssets: Int,
)
```

---

## 6. Main.ktの変更

```kotlin
@Suppress("MagicNumber")
fun main(args: Array<String>) {
    // ... (既存のコード)

    // ゲームの実行
    val winner = gameService.runGame(gameState, dice)

    // イベントログの表示（既存）
    println()
    println("=".repeat(60))
    println("Game Events:")
    println("=".repeat(60))
    consoleLogger.logEvents(gameState.events)

    // 結果の表示（既存）
    println()
    println("=".repeat(60))
    println("Game Over!")
    println("=".repeat(60))
    // ... (既存のコード)

    // HTMLレポートの生成（Phase 2 - 詳細レポート）
    val htmlReportGenerator = HtmlReportGenerator()
    val detailedReportFile = htmlReportGenerator.saveToFile(gameState)
    println("Detailed report generated: ${detailedReportFile.absolutePath}")

    // サマリーレポートの生成（Phase 4 - 新規）
    val summaryReportGenerator = SummaryReportGenerator()
    val summaryReportFile = summaryReportGenerator.saveToFile(gameState)
    println("Summary report generated: ${summaryReportFile.absolutePath}")

    println()
    println("=".repeat(60))
}
```

---

## 7. ディレクトリ構造

```
src/main/kotlin/com/monopoly/
  ├── cli/
  │   ├── Main.kt                    (変更)
  │   ├── ConsoleLogger.kt           (既存)
  │   ├── HtmlReportGenerator.kt     (既存)
  │   └── SummaryReportGenerator.kt  (新規)
  └── ...
```

---

## 8. Phase 2のレポートとの比較

| 要素 | Phase 2（詳細レポート） | Phase 4（サマリーレポート） |
|------|----------------------|-------------------------|
| ゲームサマリー | ⭕ あり | ⭕ あり（より詳細） |
| プレイヤー状態 | ⭕ テーブル形式 | ⭕ ランキング形式 |
| イベントタイムライン | ⭕ **メイン** | ❌ なし |
| 所有プロパティ詳細 | ❌ なし | ⭕ **メイン** |
| 統計情報 | ⭕ 基本的 | ⭕ **詳細** |
| ファイル名 | game-report-*.html | game-summary-*.html |
| ファイルサイズ | 大（全イベント含む） | 小（要約のみ） |

---

## 9. 今後の拡張性

### Phase 6での統計拡張

Phase 6（基本統計収集）で複数ゲーム実行時は、サマリーレポートに以下を追加予定：

- 複数ゲームの集計結果
- 戦略別の勝率
- 平均ゲームターン数
- 平均最終資産額

### Phase 7での可視化拡張

Phase 7（統計の可視化）では、サマリーレポートにグラフを追加予定：

- 勝率の棒グラフ
- ゲーム長の分布
- 資産推移グラフ

---

**作成日**: 2025-11-16
**最終更新**: 2025-11-16
