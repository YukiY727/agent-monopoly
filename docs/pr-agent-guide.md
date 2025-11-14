# PR Agent 使用ガイド

このプロジェクトでは、AI駆動のコードレビューツール **PR-Agent (CodiumAI)** を導入しています。

## 機能

PR-Agentは以下の機能を自動的に提供します：

1. **PR説明の自動生成** - コード変更を分析し、詳細な説明を生成
2. **AIコードレビュー** - コードの問題点や改善提案を自動検出
3. **改善提案** - より良いコード実装のサジェスチョン
4. **テストレビュー** - テストコードの品質チェック

## 自動実行

PR-Agentは以下のタイミングで自動的に実行されます：

- PRが作成された時（`opened`）
- PRが再オープンされた時（`reopened`）
- PRがドラフトから本番に変更された時（`ready_for_review`）
- PRに新しいコミットがプッシュされた時（`synchronize`）

## コマンド

PRのコメントで以下のコマンドを使用できます：

### `/describe`
PRの説明を自動生成します。変更内容を分析し、以下を含む詳細な説明を作成します：
- PR Type（新機能、バグ修正、リファクタリングなど）
- 変更されたファイルの一覧
- 変更の要約

```
/describe
```

### `/review`
コードレビューを実行します。以下の観点でチェックします：
- コードの品質
- 潜在的なバグ
- パフォーマンスの問題
- セキュリティの問題
- ベストプラクティスへの準拠

このプロジェクトでは特に以下をチェックします：
- オブジェクト指向エクササイズ9つのルールへの準拠
- Null安全性
- 型アノテーションの明示

```
/review
```

### `/improve`
コードの改善提案を生成します。より良い実装方法やリファクタリングの提案を受け取れます。

```
/improve
```

### `/ask`
コードに関する質問に答えます。

```
/ask "この関数はどのような場合に使用されますか？"
```

## プロジェクト固有の設定

このプロジェクトでは `.pr_agent.toml` で以下の設定を行っています：

- **言語**: 日本語（`language = "ja"`)
- **モデル**: Google Gemini 2.0 Flash（無料、高性能）
- **フォールバックモデル**: Gemini 1.5 Flash, Gemini 1.5 Pro
- **特別な指示**:
  - オブジェクト指向エクササイズの9つのルールのチェック
  - TDD原則への準拠確認
  - Null安全性の検証
  - 型アノテーションの確認

## 使用例

### 例1: 新しいPRを作成した後
```bash
# PRを作成すると自動的にdescribeとreviewが実行されます
git push origin feature/new-game-logic
gh pr create --title "新しいゲームロジックを追加"
```

### 例2: 特定の改善提案を受ける
PRページのコメント欄で：
```
/improve
```

### 例3: コードについて質問する
```
/ask "この変更がパフォーマンスに与える影響は？"
```

## セットアップ（リポジトリ管理者向け）

PR-Agentを使用するには、リポジトリのSecretsに以下を設定する必要があります：

### 1. Google API Keyの取得

1. [Google AI Studio](https://aistudio.google.com/app/apikey) にアクセス
2. 「Get API Key」または「APIキーを取得」をクリック
3. 新しいAPIキーを作成（無料で使用可能）
4. APIキーをコピー

### 2. GitHub Secretsの設定

1. GitHubリポジトリ → Settings → Secrets and variables → Actions
2. 「New repository secret」をクリック
3. Name: `GOOGLE_API_KEY`
4. Secret: 取得したGoogle API Keyを貼り付け
5. 「Add secret」をクリック

注: `GITHUB_TOKEN` は自動的に提供されるため、設定不要です。

### 料金について

Google Gemini 2.0 Flash は無料で使用できます：
- 1日あたり1,500リクエストまで無料
- レート制限: 1分あたり15リクエスト
- 一般的なPRレビューには十分な容量

## トラブルシューティング

### PR-Agentが動作しない
- PRがドラフト状態でないか確認してください
- リポジトリのSecretsに `GOOGLE_API_KEY` が設定されているか確認してください
- GitHub Actionsのログを確認してください
- Google API Keyが有効か確認してください（[Google AI Studio](https://aistudio.google.com/app/apikey)で確認可能）

### レビューコメントが多すぎる
`.pr_agent.toml` の `num_code_suggestions` を調整してください：
```toml
[pr_reviewer]
num_code_suggestions = 2  # デフォルトは4
```

## 参考リンク

- [PR-Agent 公式ドキュメント](https://github.com/Codium-ai/pr-agent)
- [PR-Agent 設定ガイド](https://pr-agent-docs.codium.ai/)

---

**作成日**: 2025-11-14
