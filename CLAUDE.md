# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Project-level conventions supplement the root `CLAUDE.md` at `../CLAUDE.md`. Read that file first.

---

## Commands

```bash
# Android debug build
./gradlew :composeApp:assembleDebug

# Desktop run
./gradlew :composeApp:run

# All common (JVM) tests
./gradlew :composeApp:jvmTest

# Android unit tests (includes Robolectric DAO contract tests)
./gradlew :composeApp:testDebugUnitTest

# Single test class
./gradlew :composeApp:testDebugUnitTest --tests "org.example.project.catan_companion_feature.data.local.dao.RealTurnDaoContractTest"
```

---

## Database: Room (not SQLDelight)

Despite the root `CLAUDE.md` mentioning SQLDelight, this project uses **AndroidX Room** with KSP code generation.

- Entities: `data/local/entity/` — annotated with `@Entity`
- DAOs: `data/local/dao/` — annotated with `@Dao`, use `@Query` annotations
- Type converters: `data/local/converter/` — enum ↔ String conversions for Room
- Schema exports land in `composeApp/schemas/` (tracked in git)
- KSP targets: `kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64`, `kspDesktop`
- `DatabaseFactory` is an `expect`/`actual` — platform modules provide the concrete builder

---

## Error Handling

All repository and data operations return `Result<D, E : Error>` (`core/domain/Result.kt`).

```kotlin
sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Failure<out E : Error>(val error: E) : Result<Nothing, E>
}
typealias EmptyResult<E> = Result<Unit, E>
```

Wrap every DAO call with the helpers in `core/data/RepositoryHelpers.kt`:

- `tryLocalRead { ... }` — catches `SQLiteException` → `DataError.Local.UNKNOWN`
- `tryLocalWrite { ... }` — catches `SQLiteException` → `DataError.Local.DISK_FULL`

Never call DAO methods directly in repositories without one of these wrappers.

---

## Domain: GameSessionCoordinator

`GameSessionCoordinator` (`domain/GameSessionCoordinator.kt`) is the central stateful object for an active game session. It is **not** a use case — it owns `StateFlow<GameSession?>` and coordinates `GameRepository` + `TurnRepository`.

Key invariant: `session.isActiveTurnSelected` gates turn completion. Calling `completeTurn()` while a historical turn is selected returns `IllegalOperationError`.

`RECENT_TURNS_LIMIT = 3` — only the last 3 completed turns are kept in memory as `recentTurns`.

---

## Testing Architecture

Three test source sets:

| Source set | Runner | Purpose |
|---|---|---|
| `commonTest` | kotlin.test (JVM) | Repository impls, domain logic, fake DAOs |
| `androidUnitTest` | JUnit 4 + Robolectric | Real Room DAO contract tests |
| *(future)* `androidInstrumentedTest` | Device/emulator | — |

### DAO Contract Testing Pattern

Abstract contract in `commonTest/.../dao/TurnDaoContractTest` — contains all behavioural assertions and is shared across:

- `FakeTurnDaoContractTest` (commonTest) — verifies the in-memory fake matches Room's semantics
- `RealTurnDaoContractTest` (androidUnitTest) — verifies actual Room queries via Robolectric

When adding a new DAO method, add a contract test case to the abstract class **before** implementing it. The fake and the real DAO must both pass.

### Fake DAOs

Live in `commonTest/.../data/fakes/dao/`. They are in-memory `MutableMap` implementations that mirror Room's behaviour precisely. If a fake deviates from Room (e.g. wrong sort order, missing null handling), the contract tests will catch it.

### Test Naming Convention

Unit tests (ViewModels, UseCases, Repositories, helpers) use the `<Action>, <Prerequisites>, <Effect>` scheme:

```kotlin
fun `Timer start, game not started, timer begins from zero`()
fun `Turn end, one player remaining, game over state emitted`()
```

Three comma-separated parts, noun or infinitive form — no questions, no `should`.
See `docs/PROJECT_OVERRIDES.md` for the authoritative definition and examples.

### Logging

Use `core/util/LogUtils` (wraps Kermit). Never use `println` or `android.util.Log` directly.

### Git Workflow

When doing git operations be compliant with GIT_WORKFLOW.md 