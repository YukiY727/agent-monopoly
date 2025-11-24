# 現在のフェーズ: Phase 2（最終段階）

## 目標

ゲーム進行を最初から最後まで追跡できるようにする（最小限の実装）

---

## クイックリファレンス

### 技術スタック

- Kotlin（JVM 21+）
- Kotest + MockK
- Gradle（Kotlin DSL）
- kotlinx-serialization（JSON保存用）

### 実装範囲

- イベント記録システム（実装済み）
- EventLogger（JSON保存/読み込み）- **残タスク**

詳細: [`phases/phase2/design.md`](phases/phase2/design.md)

---

## 重要ドキュメント

### Phase 2実装時の必読

1. **Phase 2設計**: [`phases/phase2/design.md`](phases/phase2/design.md)
2. **未実装ルール**: [`planning/unimplemented-features.md`](planning/unimplemented-features.md)

### 参考

- ゲームルール: [`specifications/02-game-rules.md`](specifications/02-game-rules.md)
- 開発計画: [`planning/development-plan.md`](planning/development-plan.md)

---

## 実装状況

### Phase 2

- [x] GameEvent sealed classの定義
- [x] GameStateへのイベントログ追加
- [x] GameServiceでのイベント記録
- [ ] EventLogger（JSON保存/読み込み）- **次のタスク**

---

## Phase 2完了条件

- [x] イベント記録システムが実装されている
- [ ] EventLoggerでJSON保存/読み込みができる
- [ ] Phase 1のテストケースがすべてパス
- [ ] JSONファイルが正しいフォーマットで出力される

---

## 次のアクション

1. **EventLoggerを実装** - JSON保存/読み込み機能
2. **Phase 2を完了**
3. **Phase 3の詳細設計を開始** - 建物システムとゾロ目

### Phase 3に向けて

Phase 2完了後、すぐにPhase 3（建物システムとゾロ目）の実装に移行します。

詳細: [`planning/unimplemented-features.md`](planning/unimplemented-features.md)

---

## 方針変更の記録

### 2025-11-22: Phase 2の範囲縮小

- **変更前**: HTMLレポート生成、CLI視覚化を含む
- **変更後**: EventLogger（JSON保存/読み込み）のみ
- **理由**:
  - YAGNI原則に従い、必要最小限の実装に集中
  - HTMLレポートとCLI視覚化は、より完全なルール（建物システム等）が実装されてから実装する方が効率的
  - Phase 3-7で本質的なゲームルールを実装することを優先

### Phase 2以降の計画

- **Phase 3**: 建物システムとダブル（ゾロ目）
- **Phase 4**: 刑務所とカードシステム
- **Phase 5**: 税金と特殊プロパティ
- **Phase 6**: 抵当システム
- **Phase 7**: オークションシステム
- **Phase 8**: 複数戦略とゲーム可視化（BoardRenderer、CLI視覚化）
- **Phase 9**: 複数ゲーム実行と基本統計
- **Phase 10**: HTMLレポートと統計の可視化

---

**更新**: 2025-11-22
