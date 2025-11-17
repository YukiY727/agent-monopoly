# Phase 5 実装計画

**Phase 5**: 複数ゲーム実行機能の実装

---

## 目標

N回のゲームを連続実行し、結果を集計できるようにする

---

## 背景

Phase 4までで1回のゲーム実行とレポート生成が完成しました。Phase 5では、統計分析の基盤として、複数のゲームを連続実行し、各ゲームの結果を記録する機能を追加します。

これにより、Phase 6での統計収集の準備が整います。

---

## スコープ

### 実装する機能

1. **N回のゲーム実行**
   - コマンドライン引数で実行回数を指定
   - 各ゲームは完全に独立（新しいGameStateで実行）
   - 各ゲームの結果を記録

2. **進捗表示**
   - 現在何ゲーム目を実行中かを表示
   - 進捗率の表示
   - シンプルなプログレスバー

3. **実行結果サマリー**
   - 全ゲームの勝者一覧
   - プレイヤー別の勝利数
   - 平均ターン数
   - 最短/最長ゲームのターン数

4. **レポート生成の制御**
   - 複数ゲーム実行時は詳細レポート（game-report-*.html）を生成しない
   - 最後に実行結果サマリーのみ表示
   - オプション: 最後のゲームのみレポート生成

### 実装しない機能

- 統計データのファイル保存（Phase 6で実装）
- グラフ表示（Phase 7で実装）
- 並列実行（Phase 8で実装）
- 詳細な統計分析（Phase 6で実装）

---

## 設計方針

### 1. GameRunner クラス

複数ゲーム実行を管理する新しいクラスを追加：

```kotlin
class GameRunner(
    private val gameService: GameService,
    private val dice: Dice,
) {
    fun runMultipleGames(
        numberOfGames: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
        showProgress: Boolean = true,
    ): MultiGameResult
}
```

### 2. MultiGameResult データクラス

複数ゲーム実行の結果を保持：

```kotlin
data class MultiGameResult(
    val gameResults: List<SingleGameResult>,
    val totalGames: Int,
)

data class SingleGameResult(
    val gameNumber: Int,
    val winner: String,
    val totalTurns: Int,
    val finalState: GameState,
)
```

### 3. CLI引数の拡張

既存の引数に加えて、実行回数を指定：

```bash
# 1回実行（既存）
./gradlew run --args="--strategy always-buy"

# 100回実行（新機能）
./gradlew run --args="--strategy always-buy --games 100"
```

### 4. 進捗表示

シンプルなプログレスバーをターミナルに表示：

```
Running 100 games...
Progress: [====================] 100/100 (100%)
```

---

## タスク分解

### タスク1: データモデルの作成

**所要時間**: 0.5時間

- `SingleGameResult` データクラスを作成
- `MultiGameResult` データクラスを作成

### タスク2: GameRunner クラスの実装

**所要時間**: 1-1.5時間

- `GameRunner` クラスを作成
- `runMultipleGames()` メソッドを実装
- 各ゲームの初期化と実行
- 結果の記録

### タスク3: 進捗表示機能の実装

**所要時間**: 0.5-1時間

- `ProgressDisplay` クラスを作成
- プログレスバーの描画ロジック
- 進捗率の計算と表示

### タスク4: 結果サマリー表示機能

**所要時間**: 0.5-1時間

- `ResultSummaryPrinter` クラスを作成
- 勝者集計ロジック
- サマリー情報の整形と出力

### タスク5: CLI引数の拡張

**所要時間**: 0.5時間

- `--games` オプションの追加
- `--no-report` オプションの追加（レポート生成を抑制）
- ヘルプメッセージの更新

### タスク6: Main.kt の更新

**所要時間**: 0.5-1時間

- GameRunner の統合
- 引数に応じて単一/複数実行を切り替え
- レポート生成の制御

### タスク7: テスト

**所要時間**: 0.5-1時間

- `GameRunner` のテスト
- `MultiGameResult` のテスト
- 統合テスト（複数ゲーム実行）

---

## 実装順序

1. **データモデル** → SingleGameResult, MultiGameResult
2. **GameRunner** → 複数ゲーム実行のコアロジック
3. **進捗表示** → ProgressDisplay
4. **結果サマリー** → ResultSummaryPrinter
5. **CLI統合** → Main.kt の更新
6. **テスト** → 動作確認

---

## 成功基準

### 機能要件

- [ ] `--games N` で N 回のゲームを実行できる
- [ ] 各ゲームが独立して実行される（状態が持ち越されない）
- [ ] 進捗表示が正しく動作する
- [ ] 実行結果サマリーが表示される
- [ ] プレイヤー別の勝利数が正しく集計される

### 非機能要件

- [ ] 100ゲーム実行が1分以内に完了する（並列化前）
- [ ] メモリリークが発生しない
- [ ] 進捗表示がターミナルを乱さない

---

## リスクと対策

| リスク | 影響 | 対策 |
|-------|-----|-----|
| メモリ不足 | 大量ゲーム実行時にクラッシュ | GameState を保持せず、結果のみ記録 |
| 進捗表示の不具合 | ターミナル表示が崩れる | シンプルなプログレスバー実装 |
| パフォーマンス低下 | 実行時間が長すぎる | Phase 8で並列化予定、Phase 5では直列実行 |

---

## Phase 6への橋渡し

Phase 5で実装する `MultiGameResult` は、Phase 6で以下のように拡張されます：

- 統計データの計算（勝率、平均ターン数など）
- JSON/CSV形式でのファイル保存
- より詳細な集計機能

Phase 5では「実行」に集中し、Phase 6で「分析」を追加する方針です。

---

**作成日**: 2025-11-16
