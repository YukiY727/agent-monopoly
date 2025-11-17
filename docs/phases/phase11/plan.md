# Phase 11: 戦略パラメータ調整 - 実装計画

## 目標

戦略のパラメータを外部ファイルから設定できるようにし、最適なパラメータを見つけるためのグリッドサーチ機能を提供する。

## 現状分析

### Phase 10で実装した戦略のパラメータ

各戦略には調整可能なパラメータが存在：

1. **MonopolyFirstStrategy**
   - `blockOpponentMonopoly: Boolean = true`
   - `minCashReserve: Int = 300`

2. **ROIStrategy**
   - `minROI: Double = 0.15`
   - `minCashReserve: Int = 300`

3. **LowPriceStrategy**
   - `maxPrice: Int = 200`
   - `minCashReserve: Int = 200`

4. **HighValueStrategy**
   - `minRent: Int = 20`
   - `minCashReserve: Int = 100`

5. **BalancedStrategy**
   - `threshold: Int = 80`
   - `minCashReserve: Int = 400`

6. **AggressiveStrategy**
   - `minCashReserve: Int = 300`

### 課題

現在、これらのパラメータはコンストラクタのデフォルト値として固定されており：
- パラメータを変更するにはコードを修正する必要がある
- 複数のパラメータセットを試すのが困難
- 最適なパラメータを見つけるプロセスが手動

## 実装する機能

### 必須機能

1. **パラメータ設定ファイル（JSON）**
   - 戦略ごとにパラメータを定義
   - 複数の設定を一つのファイルに格納
   - デフォルト値との差分のみ記述可能

2. **設定ファイル読み込み**
   - JSONファイルからパラメータを読み込み
   - 戦略インスタンスを生成時にパラメータを適用
   - バリデーション（型チェック、範囲チェック）

3. **CLIオプション拡張**
   - `--config <file>`: 設定ファイルを指定
   - `--show-params`: 現在のパラメータを表示

### 拡張機能（グリッドサーチ）

4. **グリッドサーチ設定ファイル**
   - パラメータの探索範囲を定義
   - 各パラメータの候補値リストを指定

5. **グリッドサーチ実行機能**
   - 全パラメータの組み合わせを試す
   - 各組み合わせでN回のゲームを実行
   - 勝率を記録し、最適なパラメータセットを見つける

6. **CLIオプション**
   - `--grid-search <file>`: グリッドサーチ設定ファイルを指定
   - `--grid-games <N>`: 各パラメータセットで実行するゲーム数

## 設定ファイルフォーマット

### パラメータ設定ファイル（strategy-params.json）

```json
{
  "monopoly": {
    "blockOpponentMonopoly": true,
    "minCashReserve": 250
  },
  "roi": {
    "minROI": 0.18,
    "minCashReserve": 350
  },
  "balanced": {
    "threshold": 70,
    "minCashReserve": 450
  }
}
```

### グリッドサーチ設定ファイル（grid-search.json）

```json
{
  "strategy": "roi",
  "parameters": {
    "minROI": [0.10, 0.15, 0.20, 0.25],
    "minCashReserve": [200, 300, 400, 500]
  },
  "opponents": [
    {"strategy": "monopoly"},
    {"strategy": "aggressive"},
    {"strategy": "conservative"}
  ],
  "gamesPerCombination": 100
}
```

上記の例では、4 × 4 = 16通りのパラメータ組み合わせを試す。

## 設計方針

### アーキテクチャ

```
StrategyConfig (JSON) → StrategyParameterLoader → StrategyRegistry
                                                        ↓
                                              パラメータ付き戦略インスタンス
```

### パラメータ管理

- **型安全性**: Kotlinのデータクラスでパラメータを定義
- **バリデーション**: パラメータ範囲のチェック
- **デフォルト値**: 設定ファイルで指定されていない値はデフォルトを使用

### グリッドサーチ

- **並列実行**: Phase 8の並列実行機能を活用
- **進捗表示**: 全組み合わせ数と現在の進捗を表示
- **結果レポート**: 最適パラメータと勝率をCSV/JSON形式で出力

## 実装タスク

1. ✅ Phase 11ドキュメント作成
2. パラメータデータクラスの定義
3. StrategyParameterLoaderの実装
4. StrategyRegistryのパラメータ対応
5. GridSearchConfigの実装
6. GridSearchRunnerの実装
7. CLI拡張（--config, --show-params, --grid-search）
8. サンプル設定ファイルの作成
9. テスト

## 期待される成果

- パラメータをコード修正なしで調整可能
- グリッドサーチで最適なパラメータを発見
- 実験の再現性向上（設定ファイルを共有）
- パラメータチューニングの自動化

## 検証項目

- 設定ファイルが正しく読み込まれるか
- パラメータが戦略に正しく反映されるか
- グリッドサーチが全組み合わせを試すか
- 無効なパラメータでエラーが出るか
- デフォルト値が正しく使用されるか
