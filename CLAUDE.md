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

# All common (JVM + desktop) tests
./gradlew :composeApp:test

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

`GameSessionCoordinator` (`domain/session/GameSessionCoordinator.kt`) is the central stateful object for an active game session. It is **not** a use case — it owns `StateFlow<GameSession?>` and coordinates `GameRepository` + `TurnRepository`.

Key invariant: `session.isActiveTurnSelected` gates turn completion. Calling `completeTurn()` while a historical turn is selected returns `IllegalOperationError`.

`RECENT_TURNS_LIMIT = 3` — only the last 3 completed turns are kept in memory as `recentTurns`.

---

## Presentation Layer

### Structure

The presentation layer is **feature-first** per `docs/PROJECT_OVERRIDES.md`. Each screen owns its own package. See that file for the authoritative directory layout.

Current screens and their ViewModels (all in `catan_companion_feature/presentation/`):

| Screen | ViewModel | Route |
|---|---|---|
| Dashboard | `DashboardViewModel` | `CatanCompanionRoute.Dashboard` |
| Game Config | `GameConfigViewModel` | `CatanCompanionRoute.GameConfig` |
| Gameplay | `GameplayViewModel` | `CatanCompanionRoute.Gameplay(gameId)` |
| Players List | `PlayersListViewModel` | `CatanCompanionRoute.PlayersList` |
| Player Details | `PlayerDetailsViewModel` | `CatanCompanionRoute.PlayerDetails(playerId)` |
| Games List | `GamesListViewModel` | `CatanCompanionRoute.GamesList` |
| Game Summary | `GameSummaryViewModel` | `CatanCompanionRoute.GameSummary(gameId)` |

Routes are defined as a sealed class in `presentation/navigation/CatanCompanionNavigation.kt`.

### Presentation Helpers

- `TimerManager` (`presentation/timer/TimerManager.kt`) — coroutine-based countdown timer. Exposes `StateFlow<TimerState>`. Methods: `start()`, `stop()`, `addTime()`, `reset()`. Inject a `CoroutineScope` on construction.
- `TurnNavigator` (`presentation/util/TurnNavigator.kt`) — pure immutable value for navigating turn history. Methods return a new `TurnNavigator` instance. Not injected; created directly in `GameplayViewModel`.

### Gameplay Phases

`GameplayPhase` enum drives the Gameplay screen's content:
- `DICE_SELECTION` — initial phase; player selects dice values
- `EVENT` — shown when dice sum is 7 (robber/pirate/barbarians)
- `MAIN_TIMER` — countdown timer phase
- `IN_BETWEEN_TIMER` — secondary player timer (Cities & Knights)

### UI State

All UI state classes live in `presentation/state/`. Each is a data class with an `isLoading` flag and an optional `error: UiText?`. Events (one-shot navigation) are modelled as `SharedFlow` or `Channel` on the ViewModel, **not** inside `UiState`.

---

## Dependency Injection

All Koin modules are in `catan_companion_feature/di/`:

| Module file | Provides |
|---|---|
| `DatabaseModule.kt` | `CatanCompanionDatabase`, all DAOs |
| `RepositoryModule.kt` | `GameRepository`, `PlayerRepository`, `TurnRepository` impls |
| `SessionModule.kt` | `GameSessionCoordinator` (single) |
| `UseCaseModule.kt` | `CreateGameUseCase`, `GetGameStatisticsUseCase` (factory) |
| `CatanCompanionModule.kt` | Aggregates all the above |

ViewModels are declared in `CatanCompanionModule.kt` (or a dedicated `ViewModelModule.kt` once screens are added) using `viewModelOf`.

Platform-specific modules live in `androidMain/di/Modules.android.kt`, `iosMain/di/Modules.ios.kt`, `desktopMain/di/Modules.desktop.kt`. They provide platform implementations (e.g. `DatabaseFactory`) and are passed to `startKoin` in `initKoin.kt`.

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