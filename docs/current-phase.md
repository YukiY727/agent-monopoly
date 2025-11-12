# 現在のフェーズ: Phase 1

## 目標
1ゲームを最後まで実行し、勝者を表示できる

---

## クイックリファレンス

### 技術スタック
- Kotlin（JVM 21+）
- Kotest + MockK
- Gradle（Kotlin DSL）

### 実装範囲
- 最小限のゲームロジック
- 1つの戦略（AlwaysBuyStrategy）
- CLI出力のみ

詳細: [`phases/phase1/design.md`](phases/phase1/design.md)

---

## 重要ドキュメント

### Phase 1実装時の必読
1. **テスト仕様**: [`test-specifications/phase1-test-spec.md`](test-specifications/phase1-test-spec.md)
2. **ドメイン用語**: [`phases/phase1/domain-terms.md`](phases/phase1/domain-terms.md)
3. **詳細設計**: [`phases/phase1/design.md`](phases/phase1/design.md)
4. **コーディング規約**: [`phases/phase1/coding-guide.md`](phases/phase1/coding-guide.md)

### 参考
- ゲームルール: [`specifications/02-game-rules.md`](specifications/02-game-rules.md)

---

## 実装状況

- [ ] ステップ1: データモデル（1-2日）
- [ ] ステップ2: 戦略システム（0.5日）
- [ ] ステップ3: ゲームロジック（1-2日）
- [ ] ステップ4: CLI（0.5日）
- [ ] ステップ5: 統合テスト（0.5日）

詳細な実装順序: [`phases/phase1/design.md`](phases/phase1/design.md)

---

## Phase 1完了条件

- [ ] 全46テストケースがパス
- [ ] 統合テストがパス
- [ ] CLIで2プレイヤーゲームが完走
- [ ] 勝者が表示される
- [ ] コードがリファクタリング済み

---

## 次のアクション

1. TC-001のテストを書く
2. 最小実装
3. TC-002へ進む

**TDD開始**: [`test-specifications/phase1-test-spec.md`](test-specifications/phase1-test-spec.md) のTC-001から

---

**更新**: Phase 1完了時に次フェーズの内容に書き換える
