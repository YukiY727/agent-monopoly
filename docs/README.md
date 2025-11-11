# モノポリーゲーム ドキュメント

このディレクトリには、モノポリーゲーム研究プラットフォームの全ドキュメントが含まれています。

## 📚 ドキュメント構成

### 📋 specifications/ - 仕様書

プロジェクトの要件定義と仕様書です。

- **[01. プロジェクト概要](specifications/01-overview.md)**
  - プロジェクトの目的（行動経済学、ゲーム理論、強化学習研究）
  - 主要機能（ゲームプレイ、データ収集、分析、AI対戦）
  - 技術的目標（データ完全性、保守性、再現性、分析容易性）

- **[02. ゲームルール](specifications/02-game-rules.md)**
  - 基本設定とゲームの流れ
  - ボード構成とターン進行
  - 物件システム（購入、家賃、建設、抵当）
  - オークションシステム、交渉・トレード
  - 特殊マス・カード、破産処理

- **[03. 分析機能](specifications/03-analysis-features.md)**
  - 行動経済学分析（リスク選好、損失回避、サンクコスト、オークション行動）
  - ゲーム理論分析（ナッシュ均衡、協力vs競争、支配戦略、パレート効率性）
  - 時系列分析（資産推移、現金保有、物件保有、マーケットシェア、勢力変化）
  - 強化学習用データ（State-Action-Reward、OpenAI Gym、TensorFlow形式）

- **[04. システム要件](specifications/04-system-requirements.md)**
  - 機能要件（FR-001〜FR-024）
  - 非機能要件（パフォーマンス、可用性、拡張性、保守性、データ完全性）
  - データ保存要件（イベント保存、イベントの種類、保存データ構造）

- **[05. UI仕様](specifications/05-ui-specifications.md)**
  - ゲーム画面（ボード、情報パネル、ログ、アクションボタン）
  - 管理者画面（ゲーム一覧、個別分析画面、タブ構成）
  - 分析グラフの共通仕様
  - レスポンシブデザイン

### 📅 planning/ - 計画・設計

開発計画と技術選定に関するドキュメントです。

- **[開発計画](planning/development-plan.md)**
  - 曳光弾開発アプローチ
  - Phase 1〜13の開発フェーズ
  - マイルストーン（MVP、シミュレーション基盤、拡張機能）
  - アーキテクチャ方針（イベント駆動、モノリス）
  - リスクと対策

- **[技術選定](planning/technology-selection.md)**
  - 段階的アプローチによる技術スタック
  - Kotlinをコア言語とした選定理由
  - 各Phase毎の技術選定戦略

### 🎯 phases/ - フェーズ別詳細

各開発フェーズの詳細設計とタスク分解です。

#### Phase 1: 最小限のゲーム実行

- **[詳細設計](phases/phase1/design.md)**
  - TDD開発の流れ
  - パッケージ構造とクラス設計
  - テストケースリスト（46ケース）
  - TDD実装順序（ステップ1-5）

- **[詳細計画](phases/phase1/plan.md)**
  - Phase 1のゴールと実装範囲
  - タスク分解（33タスク）
  - 見積もりと優先順位

### 🧪 test-specifications/ - テスト仕様

TDD実装のためのテスト仕様書です。

- **[Phase 1 テスト仕様](test-specifications/phase1-test-spec.md)**
  - 全46テストケース（Given-When-Then形式）
  - データモデル: 21ケース
  - ゲームロジック: 20ケース
  - 戦略: 2ケース
  - 統合テスト: 3ケース

## 🎯 クイックリファレンス

### プロジェクトの核心

このプロジェクトは単なるゲームではなく、**研究プラットフォーム**です:

1. **完全なデータ記録**: 全てのアクション、状態遷移をイベントとして記録
2. **多角的分析**: 行動経済学、ゲーム理論、時系列の3つの視点から分析
3. **AI研究基盤**: 強化学習用のデータを標準形式でエクスポート

### 主要な技術的特徴

- **イベントソーシング**: 全ての状態変更をイベントとして記録し、任意時点の状態を再構築可能
- **リアルタイム通信**: WebSocketによる同期的なマルチプレイヤー対戦
- **拡張性重視**: ドメインロジックとインフラの明確な分離により、長期的な保守性を確保

## 📖 読み方のガイド

### 実装者向け

1. まず [プロジェクト概要](specifications/01-overview.md) で全体像を把握
2. [開発計画](planning/development-plan.md) で開発フェーズを確認
3. 現在のフェーズの詳細設計を参照:
   - Phase 1 → [詳細設計](phases/phase1/design.md) + [テスト仕様](test-specifications/phase1-test-spec.md)
4. 実装する機能に応じて、詳細仕様を参照:
   - ゲームロジック → [ゲームルール](specifications/02-game-rules.md)
   - 分析機能 → [分析機能](specifications/03-analysis-features.md)
   - UI実装 → [UI仕様](specifications/05-ui-specifications.md)

### 研究者向け

1. [プロジェクト概要](specifications/01-overview.md) で研究目的を確認
2. [分析機能](specifications/03-analysis-features.md) で利用可能な分析手法を把握
3. [システム要件](specifications/04-system-requirements.md) のデータ保存要件でデータ形式を確認

### プランナー向け

1. [プロジェクト概要](specifications/01-overview.md) で全体像を把握
2. [ゲームルール](specifications/02-game-rules.md) でゲームの仕様を理解
3. [UI仕様](specifications/05-ui-specifications.md) でユーザー体験を確認
4. [開発計画](planning/development-plan.md) でマイルストーンとリスクを確認

## 🔄 元ドキュメントとの関係

このディレクトリの内容は、ルートディレクトリの [requirements.md](../requirements.md) を構造化・分割したものです。内容は同一ですが、以下の利点があります:

- **検索性**: 必要な情報を素早く見つけられる
- **可読性**: 各ドキュメントが適切なサイズに分割されている
- **保守性**: 部分的な更新が容易
- **AI可読性**: LLMが効率的に理解・参照できる構造

## 📝 更新履歴

- 2025-11-12: ディレクトリ構造を再編成（specifications/, planning/, phases/, test-specifications/）
- 2025-11-09: 初版作成（requirements.mdを分割）
