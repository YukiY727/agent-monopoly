# Phase 10: 高度な戦略実装 - ドメイン用語

## 戦略関連の用語

### Buy Strategy（購入戦略）

プレイヤーがプロパティに止まった際に、そのプロパティを購入するかどうかを決定するアルゴリズム。

### Decision Context（判断コンテキスト）

戦略が購入判断を行う際に利用可能な情報の集合。
プロパティ、現在の所持金、所有プロパティ、ボード情報、全プレイヤー情報、ターン数などを含む。

### Color Group（カラーグループ）

モノポリーボード上で同じ色に分類されているプロパティのグループ。
例: 茶色グループ（Mediterranean Avenue, Baltic Avenue）

### Monopoly（独占）

プレイヤーが特定のカラーグループの全てのプロパティを所有している状態。
独占を達成すると、そのグループのプロパティのレントが2倍になる。

## 戦略タイプの分類

### Reactive Strategy（反応型戦略）

プロパティの情報のみに基づいて判断する戦略。
例: AlwaysBuyStrategy, RandomStrategy, ConservativeStrategy

### Context-Aware Strategy（コンテキスト認識型戦略）

ゲームの状況（自分の所有プロパティ、他プレイヤーの状況など）を考慮して判断する戦略。
例: MonopolyFirstStrategy, BalancedStrategy, AggressiveStrategy

### Offensive Strategy（攻撃的戦略）

積極的にプロパティを購入し、収益を最大化することを目指す戦略。
例: AlwaysBuyStrategy, HighValueStrategy

### Defensive Strategy（防御的戦略）

破産リスクを抑え、安全性を重視する戦略。
例: ConservativeStrategy, LowPriceStrategy

### Blocking Strategy（阻止型戦略）

他のプレイヤーの独占を阻止することを優先する戦略。
例: AggressiveStrategy

## 戦略評価の指標

### ROI（Return on Investment / 投資収益率）

プロパティの価格に対するレント収入の比率。

**計算式**:
```
ROI = 基本レント ÷ プロパティ価格
```

例:
- Mediterranean Avenue: レント $2, 価格 $60 → ROI = 2/60 = 0.033 (3.3%)
- Boardwalk: レント $50, 価格 $400 → ROI = 50/400 = 0.125 (12.5%)

### Win Rate（勝率）

特定の戦略を使用した場合に、ゲームに勝利する確率。

### Average Assets（平均資産）

ゲーム終了時のプレイヤーの平均資産額（現金 + プロパティ価値）。

### Bankruptcy Rate（破産率）

特定の戦略を使用した場合に、破産する確率。

## 戦略パラメータ

### Threshold（閾値）

購入判断を行う際の基準値。
例: ROI戦略での最低ROI、保守的戦略での最低現金残高

### Cash Reserve（現金保留額）

破産を避けるために確保しておく最低現金額。

### Score（スコア）

複数の要因を統合した評価値。
スコアが閾値を超えた場合に購入すると判断する。

### Weight（重み）

複数の要因をスコアリングする際の各要因の重要度。

## 戦略別の用語

### MonopolyFirstStrategy関連

#### Monopoly Completion Priority（独占完成優先度）

カラーグループの独占を完成させることの優先度。
独占が完成すればレントが2倍になるため、高い優先度が与えられる。

#### Opponent Blocking（相手阻止）

他プレイヤーの独占を阻止するために、相手が欲しがるプロパティを購入すること。

### BalancedStrategy関連

#### Multi-Factor Scoring（多要因スコアリング）

複数の要因（カラーグループ、ROI、価格、レント）を総合的に評価し、
スコアとして統合する手法。

### AggressiveStrategy関連

#### Monopoly Denial（独占拒否）

他プレイヤーが独占を完成させることを防ぐ戦略的行動。
相手が2つ所有しているカラーグループの残りのプロパティを購入し、独占を阻止する。

## アルゴリズム用語

### Greedy Algorithm（貪欲アルゴリズム）

その時点で最も良い選択を行う短期的な最適化手法。
AlwaysBuyStrategyやHighValueStrategyはこの手法に近い。

### Heuristic（ヒューリスティック）

経験則に基づく判断方法。
完全な最適解は保証されないが、実用的な解を素早く得られる。

### Look-Ahead（先読み）

将来の状況を予測して判断を行う手法。
現在のPhase 10では未実装だが、将来的な拡張の可能性がある。

## 戦略設計パターン

### Strategy Pattern（戦略パターン）

アルゴリズムをカプセル化し、実行時に選択可能にするデザインパターン。
BuyStrategyインターフェースはこのパターンを実装している。

### Factory Pattern（ファクトリパターン）

オブジェクトの生成を専用のファクトリに委譲するパターン。
StrategyRegistryはこのパターンを利用している。

### Registry Pattern（レジストリパターン）

オブジェクトを一元管理し、IDで検索できるようにするパターン。
StrategyRegistryがこのパターンを実装している。

## モノポリー固有の用語

### Landing Probability（着地確率）

サイコロの確率分布やボードの構造により、特定のマスに止まる確率。
統計的に、オレンジ・赤グループは着地確率が高い傾向がある。

### Rent Multiplier（レント倍率）

独占を達成した場合のレント増加倍率（通常2倍）。

### Property Value（プロパティ価値）

プロパティの購入価格。
ゲーム終了時の資産計算では、この価値が使用される。

### Base Rent（基本レント）

プロパティの基本レント額（建物を建てていない状態）。

## 最適化用語

### Parameter Tuning（パラメータ調整）

戦略のパラメータ（閾値、重みなど）を調整し、勝率を最適化する作業。
Phase 11で実装予定。

### Grid Search（グリッドサーチ）

パラメータの組み合わせを網羅的に試し、最適な組み合わせを見つける手法。

### Trade-off（トレードオフ）

ある利点を得るために別の利点を犠牲にすること。
例: 攻撃性（高収益）と安全性（破産リスク）のトレードオフ。

## まとめ

Phase 10では、これらの用語を理解することで、
高度な戦略の実装と評価が可能になります。
特に、ROI、スコアリング、独占優先度などの概念は、
戦略の設計と実装において重要な役割を果たします。
