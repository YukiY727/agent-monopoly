# 現在のフェーズ: Phase 8（実験管理フェーズ）

## 目標

複数ゲームのシミュレーション実行と統計収集を可能にし、戦略の分析基盤を構築する

---

## クイックリファレンス

### 技術スタック

- Kotlin（JVM 21+）
- Kotest + MockK
- Gradle（Kotlin DSL）
- kotlinx-serialization（JSON保存用）
- Ktor（Web UI Server）

### 実装範囲

Phase 8の目標：

1. **複数ゲーム実行機能** - N回のゲームを自動実行
2. **基本統計収集** - 勝率、平均ターン数、資産推移の記録
3. **結果の永続化** - JSON/CSV形式での統計データ保存
4. **CLI統計表示** - 実験結果のサマリー表示

詳細: [`phases/phase8/design.md`](phases/phase8/design.md)（作成予定）

---

## 実装済み機能

### Phase 1: 最小限のゲーム実行（完了 ✅）

- [x] 基本ゲームループ
- [x] サイコロ振り、移動、家賃支払い
- [x] プロパティ購入判定
- [x] 破産処理とゲーム終了

### Phase 2: イベント記録システム（完了 ✅）

- [x] GameEvent sealed class
- [x] イベントログ記録
- [x] JSON保存機能

### Phase 3: Doubles、Jail、Cards（完了 ✅）

- [x] ダブル（ゾロ目）判定と追加ターン
- [x] 3回連続ゾロ目で刑務所送り
- [x] 刑務所システム（収監、脱出方法）
- [x] カードシステム（Chance、Community Chest）
- [x] カードデッキ管理

### Phase 4: 建物システム（完了 ✅）

- [x] 家（最大4件）とホテル（1件）の建設
- [x] 同色プロパティ全所有（モノポリー）の判定
- [x] 建物の均等建設ルール
- [x] 家賃計算の拡張（建物数に応じた変動）
- [x] BuildingService（建設ロジック）
- [x] MonopolyCheckerService（モノポリー判定）
- [x] GameServiceへの統合（ターン終了時の自動建設）

### Phase 5: 税金と特殊プロパティ（完了 ✅）

- [x] 税金マス処理（Income Tax, Luxury Tax）
- [x] 鉄道の家賃計算（所有数に応じた家賃）
- [x] 公共施設の家賃計算（サイコロの目 × 倍率）

### Phase 6: 抵当システム（完了 ✅）

- [x] 抵当設定・解除
- [x] 抵当価値計算（価格の50%）
- [x] 抵当解除コスト（110%）
- [x] 抵当中の家賃無効化

### Phase 7: オークションシステム（完了 ✅）

- [x] オークション機構
- [x] 入札管理
- [x] 落札判定
- [x] 購入拒否時のオークション開催

### Web UI Server（完了 ✅）

- [x] Ktorサーバー（ポート8080）
- [x] REST API（ゲーム開始、ターン実行、状態取得）
- [x] CORS設定
- [x] JSON serialization
- [x] 静的HTMLファイル提供

---

## 未実装機能

### 複数戦略（Phase 8以降で実装予定）

- [ ] ランダム戦略
- [ ] 保守的戦略（一定額以上の現金を保持）
- [ ] セット重視戦略（同色セット完成を優先）
- [ ] ROI戦略（投資効率を計算）

**注**: 現在は`AlwaysPlayerStrategy`のみ実装。複数戦略は実験管理システム完成後に実装

---

## 次のフェーズ: Phase 8（実験管理）

### 実装の目標

複数ゲームのシミュレーション実行と基本統計の収集により、戦略研究の基盤を構築する

### 実装内容

#### 1. 複数ゲーム実行エンジン

```kotlin
class ExperimentRunner(
    val gameCount: Int,
    val playerStrategies: List<PlayerStrategy>,
    val outputDir: String
)
```

- N回のゲームを自動実行
- 各ゲームの独立性を保証
- 進捗表示（CLI）

#### 2. 基本統計収集

```kotlin
data class GameStatistics(
    val gameId: String,
    val turnCount: Int,
    val winner: String,
    val finalAssets: Map<String, Int>,
    val bankruptcyOrder: List<String>
)
```

収集する統計：

- ゲームごとの勝者
- ターン数
- 各プレイヤーの最終資産
- 破産順序

#### 3. 統計の集約と分析

```kotlin
data class AggregatedStatistics(
    val totalGames: Int,
    val winRateByPlayer: Map<String, Double>,
    val averageTurnCount: Double,
    val averageFinalAssets: Map<String, Double>
)
```

- 勝率計算（プレイヤー別）
- 平均ターン数
- 平均最終資産

#### 4. 結果の永続化

- JSON形式での統計データ保存
- CSV形式でのエクスポート（Excel等での分析用）
- ファイル命名規則: `experiment_YYYYMMDD_HHMMSS.json`

#### 5. CLI統計表示

```text
=== Experiment Results ===
Total Games: 100
Average Turns: 45.3

Win Rate:
  Alice (AlwaysPlayerStrategy): 52%
  Bob (AlwaysPlayerStrategy): 48%

Average Final Assets:
  Alice: $1,234
  Bob: $987
```

### 期待される効果

1. **戦略の定量的評価**: 勝率や資産推移で戦略の優劣を判定可能
2. **再現可能な実験**: 統計データを保存することで、後から分析可能
3. **研究基盤の確立**: Phase 9以降の詳細分析（HTMLレポート、グラフ可視化）の基盤

### 実装スコープ（Phase 8）

**実装する**:

- ✅ 複数ゲーム実行エンジン
- ✅ 基本統計収集（勝率、平均ターン数、資産）
- ✅ JSON/CSV保存
- ✅ CLI統計表示

**実装しない（Phase 9以降）**:

- ❌ HTMLレポート生成
- ❌ グラフ可視化（折れ線、棒グラフ等）
- ❌ 詳細統計（プロパティ別収益性、資産推移など）
- ❌ 並列実行

---

## Phase 8完了条件

- [ ] 指定回数（N回）のゲームを実行できる
- [ ] 各ゲームの統計データが収集される
- [ ] 統計データがJSON/CSV形式で保存される
- [ ] CLIで統計サマリーが表示される
- [ ] テストカバレッジが90%以上を維持

---

## Phase 9以降の計画（ゲーム理論・行動経済学研究）

### Phase 9: 戦略の支配関係分析（ゲーム理論）

**研究テーマ**: 異なる戦略間の対戦成績から支配戦略とNash均衡を探索

**実装内容**:

1. **複数の基本戦略を実装**

   ```kotlin
   - AlwaysPlayerStrategy (既存): 常に購入・建設
   - ConservativeStrategy: 保守的（資金温存、リスク回避）
   - AggressiveStrategy: 積極的（早期投資、高リスク）
   - BalancedStrategy: バランス型（状況判断）
   ```

2. **戦略対戦マトリックス生成**

   ```kotlin
   StrategyComparisonExperiment(
       strategies = listOf(Always, Conservative, Aggressive, Balanced),
       gamesPerPair = 100  // 各組み合わせ100回対戦
   )
   // 出力: 4x4対戦成績マトリックス
   ```

3. **統計的検定**

   - t検定: 戦略間の勝率差の有意性
   - 信頼区間計算: 95%信頼区間
   - 効果量: Cohen's d（差の大きさ）
   - p値調整: Bonferroni補正（多重比較）

4. **Nash均衡の探索**

   - 最適反応戦略の特定
   - 支配戦略の検出
   - 混合戦略Nash均衡の計算

5. **研究レポート生成**

   ```markdown
   # 戦略支配関係分析レポート

   ## 仮説
   H0: 全ての戦略の勝率は等しい
   H1: 有意差が存在する

   ## 対戦マトリックス
   |          | Always | Conservative | Aggressive | Balanced |
   |----------|--------|--------------|------------|----------|
   | Always   | 50.0%  | 62.3% **    | 45.1%      | 53.2%    |
   ...
   ** p < 0.01

   ## Nash均衡
   - 純粋戦略均衡: Balanced
   - 支配戦略: なし
   ```

**期待される成果**:

- どの戦略が最も強いかの科学的根拠
- 戦略の相性関係の理解
- ゲーム理論的な最適戦略の発見

---

### Phase 10: リスク選好の測定（行動経済学）

**研究テーマ**: プレイヤー戦略のリスク選好度を定量化し、リスク-リターンの関係を分析

**実装内容**:

1. **詳細な意思決定ログ**

   ```kotlin
   data class DecisionLog(
       val turnNumber: Int,
       val decision: Decision,
       val context: DecisionContext,
       val outcome: Outcome
   )

   sealed class Decision {
       data class PropertyPurchase(val property: Property, val accepted: Boolean)
       data class BuildingInvestment(val property: Property, val type: BuildingType)
       data class JailEscape(val paidFine: Boolean)
       data class AuctionBid(val amount: Int?)
   }
   ```

2. **リスクパラメータの測定**

   - 期待効用理論に基づくリスク回避係数の推定
   - プロパティ購入率（手持ち資金比）
   - 建物投資タイミング（資金余裕度）
   - 監獄脱出判断（機会費用 vs 確実なコスト）

3. **リスク-リターン分析**

   ```kotlin
   data class RiskReturnProfile(
       val expectedReturn: Double,      // 平均最終資産
       val volatility: Double,           // 標準偏差
       val sharpeRatio: Double,          // (期待収益 - 無リスク収益) / 標準偏差
       val maxDrawdown: Double,          // 最大資産減少幅
       val bankruptcyRate: Double        // 破産率
   )
   ```

4. **回帰分析**

   - 独立変数: リスクパラメータ（投資積極性、資金温存率）
   - 従属変数: 勝率、平均最終資産
   - モデル: 線形回帰、ロジスティック回帰

5. **可視化**

   - リスク-リターン散布図
   - 効率的フロンティアの描画
   - 意思決定ツリーの可視化

**期待される成果**:

- 最適なリスク選好度の発見
- リスクとリターンのトレードオフの定量化
- 行動経済学的な洞察（損失回避、確実性効果など）

---

### Phase 11: サンクコスト効果の検証（認知バイアス）

**研究テーマ**: 過去の投資が将来の意思決定に与える影響を測定

**実装内容**:

1. **サンクコストを考慮する戦略**

   ```kotlin
   class SunkCostStrategy(
       val sunkCostWeight: Double = 0.5  // 過去投資の重み
   ) : PlayerStrategy {
       override fun shouldBuildHouse(...): Boolean {
           val rationalDecision = expectedROI > threshold
           val sunkCostBias = pastInvestment * sunkCostWeight
           return rationalDecision || sunkCostBias > threshold
       }
   }
   ```

2. **測定指標**

   - 損失プロパティへの追加投資率
   - モノポリー完成までの粘り強さ
   - 破産寸前での行動変化
   - 合理的戦略からの乖離度

3. **実験デザイン**

   ```kotlin
   ExperimentDesign(
       control = RationalStrategy(),
       treatment = SunkCostStrategy(sunkCostWeight = 0.3),
       matchedPairs = true,  // 同じランダムシードで対戦
       sampleSize = 1000
   )
   ```

4. **統計分析**

   - 対応のあるt検定（マッチドペア）
   - 差分の差分法（DID: Difference-in-Differences）
   - 傾向スコアマッチング

5. **認知バイアスの影響測定**

   - サンクコスト効果の強度
   - パフォーマンスへの影響（正負）
   - バイアスが有利に働くケース

**期待される成果**:

- サンクコスト効果がモノポリーで有害か有益かの判定
- バイアスの最適な強度の発見
- 人間的な意思決定の再現と理解

---

### Phase 12: 協調ゲーム理論の応用（複雑な相互作用）

**研究テーマ**: 3人以上のゲームにおける暗黙的協調と裏切りのパターン分析

**実装内容**:

1. **多人数ゲーム対応**

   ```kotlin
   class MultiPlayerExperiment(
       val playerCount: Int = 4,
       val strategies: List<PlayerStrategy>
   )
   ```

2. **協調検出アルゴリズム**

   ```kotlin
   data class Coalition(
       val members: Set<String>,
       val duration: Int,  // 協調が続いたターン数
       val benefit: Map<String, Int>  // 各メンバーの利益
   )

   // 暗黙的協調の検出
   - 同じプレイヤーへの連続攻撃回避
   - オークション入札の自粛パターン
   - 有利な取引の提供
   ```

3. **ゲーム理論的分析**

   - Shapley値: 連合への各プレイヤーの貢献度
   - コア: 安定的な配分の集合
   - 裏切りの誘因分析

4. **取引・交渉戦略（将来拡張）**

   ```kotlin
   interface NegotiationStrategy {
       fun proposeTradeOffer(...): TradeOffer?
       fun evaluateTradeOffer(...): Boolean
       fun formAlliance(...): Coalition?
   }
   ```

5. **シミュレーション**

   - 繰り返しゲーム（同じプレイヤーで複数回）
   - 評判システムの影響
   - しっぺ返し戦略の効果

**期待される成果**:

- 3人以上のゲームでの最適戦略
- 協調が生まれる条件の理解
- 裏切りの最適タイミング
- 取引・交渉システムの設計指針

---

## フェーズ間の依存関係

```text
Phase 8 (実験管理)
    ↓ 基盤
Phase 9 (戦略の支配関係) ← 複数の基本戦略実装
    ↓ 「なぜ強いか？」の分析
Phase 10 (リスク選好) ← 意思決定ログの詳細化
    ↓ 認知バイアスの追加
Phase 11 (サンクコスト) ← Phase 10の分析手法を応用
    ↓ 複雑な相互作用へ
Phase 12 (協調ゲーム) ← これまでの全知見を統合
```

**段階的な複雑化**:

1. Phase 9: 2人ゲームの基本分析
2. Phase 10: 意思決定の深掘り
3. Phase 11: 認知バイアスの追加
4. Phase 12: 多人数の複雑な相互作用

各フェーズで得られた知見が次のフェーズに活きる設計になっています。

---

## 方針変更の記録

### 2025-12-08: Phase 4建物システムの完了確認

- **状況**: Phase 4建物システムは既に実装済みであることが判明
- **実装内容**:
  - BuildingService、MonopolyCheckerService実装済み
  - GameServiceへの統合完了（ターン終了時に自動建設）
  - 27テストケース（BuildingService: 10, MonopolyChecker: 6, PropertyBuildings: 11）
- **理由**: ドキュメントの更新漏れ。実装は完了していた

### 2025-12-08: Phase 8の定義変更

- **変更前**: Phase 8 = 複数戦略とゲーム可視化
- **変更後**: Phase 8 = 実験管理（複数ゲーム実行と基本統計）
- **理由**:
  - Phase 3-7でゲームの主要ルールが実装完了
  - 戦略研究のための実験基盤が必要
  - 複数戦略の実装は実験管理システム完成後の方が効率的
  - YAGNI原則に従い、必要な機能から実装

### 優先順位の考え方

1. **実験管理（Phase 8）**: 研究基盤として最優先
2. **可視化・分析（Phase 9-10）**: 実験結果の理解を深める
3. **パフォーマンス（Phase 11）**: 大規模実験のために必要
4. **複数戦略（Phase 12）**: 実験基盤完成後に実装

---

## 参考ドキュメント

- ゲームルール: [`specifications/02-game-rules.md`](specifications/02-game-rules.md)
- 開発計画: [`planning/development-plan.md`](planning/development-plan.md)
- 未実装機能: [`planning/unimplemented-features.md`](planning/unimplemented-features.md)

---

**更新**: 2025-12-08
