# モノポリー研究プラットフォーム

モノポリーゲームの戦略研究のためのシミュレーションプラットフォーム。

## 🎯 プロジェクト概要

このプロジェクトは、モノポリーゲームの様々な戦略を実装・比較し、最適な戦略を研究するためのプラットフォームです。

### 主な機能

- ✅ **完全なゲームシミュレーション**: モノポリーのルールを忠実に実装
- ✅ **建物システム**: 家・ホテルの建設、レント計算
- ✅ **監獄システム**: 3連続ゾロ目での投獄、脱獄メカニズム
- ✅ **カードシステム**: チャンス・共同基金カード
- ✅ **実験管理**: 複数ゲームの自動実行と統計分析
- ✅ **Web UI**: ゲーム進行の可視化（開発中）

## 🚀 クイックスタート

### 必要な環境

- Java 21 以上
- Gradle 8.5 以上

### 実行方法

プロジェクトには3つの実行モードがあります：

#### 1. 実験モード（推奨）

複数ゲームを実行して統計を収集します：

```bash
./gradlew runExperiment
```

**出力:**
- コンソールに集計統計を表示（勝率、平均ターン数、平均最終資産）
- `experiment-results/` ディレクトリにJSON・CSVファイルを保存

**設定変更:**
[ExperimentMain.kt](src/main/kotlin/com/monopoly/cli/ExperimentMain.kt) でゲーム数や戦略をカスタマイズ可能。

#### 2. 単一ゲームモード

1回のゲームを実行して詳細を確認します：

```bash
./gradlew runGame
```

**出力:**
- ゲームの進行状況
- 最終結果（勝者、資産、ターン数）

#### 3. Webサーバーモード

ブラウザでゲームを可視化します（開発中）：

```bash
./gradlew run
```

ブラウザで `http://localhost:8080` を開いてアクセス。

## 📊 実験結果の見方

### 出力ファイル

実験実行後、`experiment-results/` ディレクトリに以下が保存されます：

**JSON形式** (`experiment-YYYYMMDD-HHMMSS.json`):
```json
{
  "games": [
    {
      "gameId": "game_001",
      "timestamp": 1765161387292,
      "turnCount": 155,
      "winner": "Player1",
      "finalAssets": {
        "Player1": 1627,
        "Player2": -17
      },
      "bankruptcyOrder": []
    }
  ],
  "aggregated": {
    "totalGames": 10,
    "winRates": {
      "Player1": 0.5,
      "Player2": 0.5
    },
    "averageTurnCount": 289.8,
    "averageFinalAssets": {
      "Player1": 3402.6,
      "Player2": 3356.9
    },
    "totalDuration": 70
  }
}
```

**CSV形式** (`experiment-YYYYMMDD-HHMMSS.csv`):
```csv
gameId,timestamp,turnCount,winner,finalAssets,bankruptcyOrder
game_001,1765161387292,155,Player1,"Player1:1627;Player2:-17",""
game_002,1765161387331,95,Player1,"Player1:60;Player2:-4",""
```

### コンソール出力

```
============================================================
Experiment Results
============================================================

Total Games: 10

Win Rates:
  Player1: 50.00%
  Player2: 50.00%

Average Turn Count: 289.80

Average Final Assets:
  Player1: $3402.60
  Player2: $3356.90

Total Duration: 0.07s

============================================================
```

## 🧪 テスト実行

全テストを実行：

```bash
./gradlew test
```

カバレッジレポート生成：

```bash
./gradlew jacocoTestReport
```

レポートは `build/reports/jacoco/test/html/index.html` に生成されます。

## 📁 プロジェクト構造

```
src/main/kotlin/com/monopoly/
├── cli/
│   ├── Main.kt              # 単一ゲーム実行
│   ├── ExperimentMain.kt    # 実験実行
│   └── BoardFactory.kt      # ボード生成
├── domain/
│   ├── model/               # ドメインモデル
│   │   ├── core/            # 基本型（Money, BoardPosition）
│   │   ├── game/            # ゲーム状態・ボード・サイコロ
│   │   ├── player/          # プレイヤー・戦略
│   │   ├── property/        # プロパティ・建物
│   │   ├── jail/            # 監獄システム
│   │   └── card/            # カードシステム
│   ├── service/             # ドメインサービス
│   │   ├── GameService.kt
│   │   ├── BuildingService.kt
│   │   └── MonopolyCheckerService.kt
│   ├── experiment/          # 実験管理
│   │   ├── GameStatistics.kt
│   │   ├── AggregatedStatistics.kt
│   │   ├── ExperimentRunner.kt
│   │   ├── StatisticsWriter.kt
│   │   └── StatisticsDisplay.kt
│   └── strategy/            # プレイヤー戦略
│       └── AlwaysPlayerStrategy.kt
└── server/                  # Web UI（開発中）
```

## 🎮 戦略のカスタマイズ

`PlayerStrategy` インターフェースを実装して独自の戦略を作成できます：

```kotlin
class CustomStrategy : PlayerStrategy {
    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        // 購入判断ロジック
    }

    override fun shouldBuildHouse(property: StreetProperty, currentMoney: Int): Boolean {
        // 家建設判断ロジック
    }

    override fun shouldBuildHotel(property: StreetProperty, currentMoney: Int): Boolean {
        // ホテル建設判断ロジック
    }

    override fun shouldPayToEscapeJail(currentMoney: Int): Boolean {
        // 監獄脱出判断ロジック
    }

    override fun decideAuctionBid(
        property: Property,
        currentBid: Int?,
        currentMoney: Int
    ): Int? {
        // オークション入札ロジック
    }
}
```

## 🔧 開発ガイド

### コーディング規約

プロジェクトの開発哲学とコーディング規約は [CLAUDE.md](CLAUDE.md) を参照してください。

主な原則：
- **TDD（Test-Driven Development）**: テストを先に書く
- **YAGNI**: 必要になってから実装する
- **型安全性**: Null安全性を徹底、型で状態を表現
- **保守性 > パフォーマンス > 開発速度**

### コード品質チェック

```bash
# Linter（ktlint）
./gradlew ktlintCheck

# 静的解析（detekt）
./gradlew detekt

# 全チェック（テスト + lint + カバレッジ）
./gradlew check
```

## 📝 実装済み機能

### Phase 1-7（完了）
- ✅ 基本的なゲームループ
- ✅ プロパティの購入・レント支払い
- ✅ GO通過ボーナス
- ✅ 破産処理
- ✅ 建物システム（家・ホテル）
- ✅ 監獄システム
- ✅ カードシステム（チャンス・共同基金）

### Phase 8（完了）
- ✅ 実験管理システム
- ✅ 統計収集・集計
- ✅ JSON/CSV出力
- ✅ CLI統計表示

### Phase 9-12（計画中）
- ⏳ 複数の戦略実装
- ⏳ 戦略の比較分析
- ⏳ 最適戦略の探索

## 📄 ライセンス

このプロジェクトは研究・教育目的で開発されています。

## 🤝 貢献

プロジェクトへの貢献を歓迎します。Pull Requestを送る前に：

1. 全テストが通ることを確認（`./gradlew test`）
2. コード品質チェックをパス（`./gradlew check`）
3. TDDアプローチに従う

---

**作成日**: 2024-12-08
**言語**: Kotlin 2.0.21
**フレームワーク**: Gradle 8.5
