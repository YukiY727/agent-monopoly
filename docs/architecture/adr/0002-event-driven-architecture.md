# ADR-0002: イベント駆動アーキテクチャの採用

**ステータス**: 承認済み  
**日付**: 2025-11-12  
**決定者**: プロジェクトアーキテクト  
**関連ADR**: ADR-0001 (モノリスアーキテクチャ)

---

## 決定 (Decision)

**選択**: 簡易イベントログによるイベント駆動アーキテクチャを採用

ゲーム内の全イベント（サイコロ、購入、レント、破産等）をイベントとして記録し、JSON/CSV形式で保存する。

---

## コンテキスト (Context)

研究プラットフォームとして、ゲームの完全な履歴と再現性が必須要件です。

---

## トレードオフ分析

### 得られるもの

- ✅ **完全な履歴**: 全てのイベントを記録
- ✅ **再現性**: 任意時点の状態を再構築可能
- ✅ **拡張性**: 後から新しい統計分析を追加可能
- ✅ **監査**: 全変更を追跡可能

### 失うもの

- ❌ **クエリの複雑化**: 現在の状態を得るにはイベント再生が必要
- ❌ **ストレージコスト**: 大容量のストレージが必要

### 判断

研究プラットフォームとして完全なデータ記録が必須。失うもの（クエリの複雑性）は受け入れ可能。

---

## 実装

```kotlin
data class GameEvent(
    val timestamp: Long,
    val type: EventType,
    val playerId: String?,
    val details: Map<String, Any>
)

class EventLog {
    private val events = mutableListOf<GameEvent>()
    
    fun record(event: GameEvent) {
        events.add(event)
    }
    
    fun save(path: String) {
        // JSON/CSV形式で保存
    }
}
```

---

## 参考資料

- [パターン比較](../patterns-comparison.md#データアーキテクチャ)
- Greg Young, "CQRS and Event Sourcing"

**作成日**: 2025-11-12
