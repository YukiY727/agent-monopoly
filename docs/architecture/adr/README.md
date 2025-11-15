# Architecture Decision Records (ADR)

このディレクトリには、プロジェクトの重要なアーキテクチャ決定が記録されています。

## ADRとは

Architecture Decision Records (ADR) は、アーキテクチャ上の重要な決定とその理由を文書化する手法です。

### ADRの原則

1. **不変**: 一度記録したADRは変更しない（新しいADRで更新・廃止を記録）
2. **簡潔**: 1つのADRは1つの決定のみを記録
3. **トレードオフ重視**: メリットだけでなくデメリットも必ず記載
4. **コンテキスト明記**: なぜその時点でその決定をしたのか、状況を記録

## ADR一覧

### 承認済み

- [ADR-0001: モノリスアーキテクチャの採用](0001-monolith-architecture.md) - 2025-11-12
- [ADR-0002: イベント駆動アーキテクチャの採用](0002-event-driven-architecture.md) - 2025-11-12
- [ADR-0003: Kotlinをコア言語として採用](0003-kotlin-as-core-language.md) - 2025-11-12
- [ADR-0004: レイヤードアーキテクチャの採用](0004-layered-architecture.md) - 2025-11-12

### 提案中

なし

### 廃止

なし

## 新しいADRの作成方法

1. `template.md` をコピー
2. ファイル名を `XXXX-decision-title.md` に変更（XXXXは連番）
3. テンプレートに従って記述
4. このREADME.mdのADRリストに追加
5. プルリクエストで承認を得る

## 参考資料

- [Architecture Decision Records (ADR) の紹介](https://github.com/joelparkerhenderson/architecture-decision-record)
- Michael Nygard, "Documenting Architecture Decisions"
