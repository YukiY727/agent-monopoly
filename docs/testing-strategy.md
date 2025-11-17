# テスト戦略

## 目的

このドキュメントは、agent-monopolyプロジェクトにおけるテスト戦略とTDD（Test-Driven Development）の方針を定義します。

## 現状の問題点

### Phase 1-5（既存のテストあり）
- ✅ ドメインモデルのテスト（Player, Board, Property, etc.）
- ✅ GameServiceのテスト（移動、購入、レント支払い、破産処理）
- ✅ 戦略のテスト（AlwaysBuyStrategy）

### Phase 6-12（テスト未実装）
- ❌ 統計機能（StatisticsCalculator, DetailedStatisticsCalculator, BoardStatisticsCalculator）
- ❌ 可視化機能（LineChartGenerator, HeatmapGenerator, RadarChartGenerator, ScatterPlotGenerator）
- ❌ レポート生成（ResearchReportGenerator）
- ❌ PDF Export（PdfExporter）

**結果**: コードが動かない、バグが潜在している可能性が高い

## TDD（Test-Driven Development）の原則

### Red-Green-Refactorサイクル

```
1. 🔴 Red: 失敗するテストを書く
   - 仕様を明確化
   - まだ実装していない機能のテストを書く
   - テストが失敗することを確認

2. 🟢 Green: 最小限の実装でテストを通す
   - テストが通る最小限のコードを書く
   - クイックに実装（汚くてもOK）
   - テストがパスすることを確認

3. 🔵 Refactor: リファクタリング
   - コードの品質を向上
   - テストは変更しない（テストがパスし続けることを確認）
   - 重複排除、命名改善、構造改善
```

### TDDのメリット

1. **仕様の明確化**: テストがドキュメントになる
2. **バグの早期発見**: 実装前にテストがあるのでバグが混入しにくい
3. **リファクタリングの安全性**: テストがあるので安心してコード改善できる
4. **設計の改善**: テスト可能なコードは良い設計になりやすい
5. **開発速度の向上**: 長期的にはデバッグ時間が減り開発が速くなる

## テストの種類

### 1. 単体テスト（Unit Test）

**対象**: 1つのクラス、1つのメソッド

**特徴**:
- 高速（ミリ秒単位）
- 外部依存なし（モック、スタブを使用）
- 多数作成（全ロジックをカバー）

**例**:
```kotlin
class StatisticsCalculatorTest {
    @Test
    fun `2ゲームの結果から正しく勝率を計算する`() {
        // Arrange
        val games = listOf(
            createGame(winner = "Alice"),
            createGame(winner = "Bob")
        )
        val calculator = StatisticsCalculator()

        // Act
        val stats = calculator.calculate(games)

        // Assert
        assertThat(stats.getWinRate("Alice")).isEqualTo(0.5)
        assertThat(stats.getWinRate("Bob")).isEqualTo(0.5)
    }
}
```

### 2. 統合テスト（Integration Test）

**対象**: 複数のクラスの連携、外部システムとの統合

**特徴**:
- やや低速（秒単位）
- 実際の依存関係を使用
- 重要なワークフローをテスト

**例**:
```kotlin
class GameIntegrationTest {
    @Test
    fun `ゲーム全体が正常に動作する`() {
        // Arrange
        val board = createStandardBoard()
        val players = listOf(
            Player("Alice", AlwaysBuyStrategy()),
            Player("Bob", AlwaysBuyStrategy())
        )
        val gameService = GameService(board)

        // Act
        val result = gameService.playGame(players)

        // Assert
        assertThat(result.winner).isNotNull()
        assertThat(result.turns).isGreaterThan(0)
    }
}
```

### 3. E2Eテスト（End-to-End Test）

**対象**: システム全体（CLI、API、フロントエンドなど）

**特徴**:
- 低速（分単位）
- ユーザーの視点でテスト
- 少数作成（主要なユーザーシナリオのみ）

**Phase 13以降で追加**:
- REST APIのテスト
- WebSocketのテスト
- フロントエンドのテスト（Playwright, Cypress）

## テストカバレッジの目標

### カバレッジ目標

| レイヤー | 目標カバレッジ | 備考 |
|---------|--------------|------|
| **ドメインロジック** | 90%以上 | 最重要（ビジネスロジック） |
| **統計・計算** | 90%以上 | バグの影響大 |
| **可視化（SVG生成）** | 70%以上 | 出力形式のテストで代替可 |
| **CLI** | 50%以上 | E2Eテストで補完 |
| **統合テスト** | 主要シナリオ | ハッピーパス + エラーケース |

### カバレッジ測定

```bash
# JaCoCo（Java Code Coverage）を使用
./gradlew test jacocoTestReport

# カバレッジレポート確認
open build/reports/jacoco/test/html/index.html
```

## テストの構造（AAAパターン）

すべてのテストは**Arrange-Act-Assert**パターンに従う：

```kotlin
@Test
fun `テストの説明`() {
    // Arrange（準備）: テストデータ、モックの準備
    val input = "test data"
    val expected = "expected result"
    val sut = SystemUnderTest()

    // Act（実行）: テスト対象のメソッドを実行
    val actual = sut.process(input)

    // Assert（検証）: 結果を検証
    assertThat(actual).isEqualTo(expected)
}
```

## テストの命名規則

### テストクラス

```kotlin
// パターン: {対象クラス名}Test
class StatisticsCalculatorTest
class HeatmapGeneratorTest
```

### テストメソッド

バッククォートを使った日本語説明（推奨）:

```kotlin
@Test
fun `空のゲームリストから統計を計算するとエラーを投げる`()

@Test
fun `3人のプレイヤーで100ゲーム実行すると正しい勝率が計算される`()

@Test
fun `HeatmapのSVG出力に40個のセルが含まれる`()
```

## モックとスタブ

### MockK（Kotlinモックライブラリ）

```kotlin
// build.gradle.kts
testImplementation("io.mockk:mockk:1.13.8")
```

**使用例**:
```kotlin
@Test
fun `外部サービスへの依存をモックする`() {
    // Arrange
    val mockService = mockk<ExternalService>()
    every { mockService.fetchData() } returns "mocked data"

    val sut = MyClass(mockService)

    // Act
    val result = sut.process()

    // Assert
    verify(exactly = 1) { mockService.fetchData() }
    assertThat(result).isEqualTo("processed: mocked data")
}
```

## テストデータ作成（Fixtures）

テストデータは再利用可能なFixtureとして作成：

```kotlin
// src/test/kotlin/com/monopoly/fixtures/GameFixtures.kt
object GameFixtures {
    fun createSimpleGame(
        playerCount: Int = 2,
        winner: String = "Alice"
    ): Game {
        val players = (1..playerCount).map {
            Player("Player$it", AlwaysBuyStrategy())
        }
        return Game(
            players = players,
            winner = players.first { it.name == winner },
            turns = 100
        )
    }

    fun createGameWithEvents(events: List<GameEvent>): Game {
        // ...
    }
}
```

## Phase 6-12のテスト追加計画

### 優先順位

1. **高優先度（統計計算）**: 数値の正確性が重要
   - StatisticsCalculator
   - DetailedStatisticsCalculator
   - BoardStatisticsCalculator

2. **中優先度（可視化）**: 出力形式の検証
   - LineChartGenerator
   - HeatmapGenerator
   - RadarChartGenerator
   - ScatterPlotGenerator

3. **低優先度（レポート生成）**: E2Eで確認可能
   - ResearchReportGenerator
   - PdfExporter（外部コマンド依存のためモック推奨）

### テストの実装方針

#### 統計計算のテスト

```kotlin
class StatisticsCalculatorTest {
    @Test
    fun `1ゲームの結果から統計を計算する`() { /* ... */ }

    @Test
    fun `複数ゲームの勝率を正しく計算する`() { /* ... */ }

    @Test
    fun `空のゲームリストでは例外を投げる`() {
        // Arrange
        val calculator = StatisticsCalculator()

        // Act & Assert
        assertThrows<IllegalArgumentException> {
            calculator.calculate(emptyList())
        }
    }
}
```

#### 可視化のテスト

```kotlin
class HeatmapGeneratorTest {
    @Test
    fun `Heatmapの生成されたSVGに40個のセルが含まれる`() {
        // Arrange
        val data = createHeatmapData(40)
        val generator = HeatmapGenerator()

        // Act
        val svg = generator.generate(data)

        // Assert
        val cellCount = svg.count("<rect")
        assertThat(cellCount).isEqualTo(40)
    }

    @Test
    fun `色の補間が正しく計算される`() {
        // 最小値=0.0, 最大値=1.0のとき
        // 0.5は中間色になる
        val generator = HeatmapGenerator()
        val color = generator.interpolateColor(0.5, 0.0, 1.0)

        // 白(#FFFFFF)から青(#0000FF)の中間は#8080FF
        assertThat(color).isEqualTo("#8080FF")
    }
}
```

## CI/CDでのテスト自動実行

### GitHub Actionsの設定例

```yaml
# .github/workflows/test.yml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
      - name: Check coverage threshold
        run: |
          # カバレッジが80%未満ならFail
          ./gradlew jacocoTestCoverageVerification
```

## Phase 13以降のTDD実践

Phase 13（Web UI）以降は**TDDを徹底**します：

### バックエンド（Ktor API）

```kotlin
// 1. テストを先に書く（Red）
@Test
fun `GET /api/strategies は戦略一覧を返す`() {
    testApplication {
        // Act
        val response = client.get("/api/strategies")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val strategies = response.body<List<Strategy>>()
        assertTrue(strategies.isNotEmpty())
    }
}

// 2. 最小限の実装（Green）
routing {
    get("/api/strategies") {
        call.respond(StrategyRepository.getAll())
    }
}

// 3. リファクタリング（Refactor）
// ルーティングを分離、エラーハンドリング追加など
```

### フロントエンド（React + TypeScript）

```typescript
// Vitest + React Testing Library
describe('SimulationSetup', () => {
  it('戦略を選択できる', async () => {
    // Arrange
    render(<SimulationSetup />);

    // Act
    const select = screen.getByLabelText('戦略選択');
    await userEvent.selectOptions(select, 'monopoly');

    // Assert
    expect(select).toHaveValue('monopoly');
  });
});
```

## まとめ

### 重要なポイント

1. ✅ **TDDサイクルを守る**: Red → Green → Refactor
2. ✅ **テストファースト**: 実装前にテストを書く
3. ✅ **1テスト1検証**: テストは小さく、明確に
4. ✅ **AAA構造**: Arrange-Act-Assert
5. ✅ **自動実行**: CI/CDで毎回テスト実行
6. ✅ **カバレッジ確認**: 重要なロジックは90%以上

### 次のステップ

1. Phase 6-12のテストを追加（この戦略に基づいて）
2. 全テストを実行して動作確認
3. Phase 13以降はTDDで開発

## 参照

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [MockK Documentation](https://mockk.io/)
- [Test Driven Development: By Example（Kent Beck）](https://www.amazon.com/dp/0321146530)
