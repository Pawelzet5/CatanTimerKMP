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

All repository and data operations return `Result<D, E : Error>` — see `core/domain/Result.kt`.
DAO calls must be wrapped with `tryLocalRead { }` / `tryLocalWrite { }` from `core/data/RepositoryHelpers.kt`.

---

## Domain: GameSessionCoordinator

`GameSessionCoordinator` (`domain/session/GameSessionCoordinator.kt`) is the central stateful object for an active game session. It is **not** a use case — it owns `StateFlow<GameSession?>` and coordinates `GameRepository` + `TurnRepository`.

Key invariant: `session.isActiveTurnSelected` gates turn completion. Calling `completeTurn()` while a historical turn is selected returns `IllegalOperationError`.

`RECENT_TURNS_LIMIT = 3` — only the last 3 completed turns are kept in memory as `recentTurns`.

---

## Presentation Layer

### Structure

The presentation layer is **feature-first** — each screen owns its own package in `catan_companion_feature/presentation/`. Routes are defined in `presentation/navigation/CatanCompanionNavigation.kt`.

Composable placement rules:

| Scope | Location |
|---|---|
| Used only in one `*Screen.kt` | Private `@Composable` in the same file |
| Used across multiple files in one `<screen>/` package | Separate `.kt` file in that `<screen>/` package |
| Shared across multiple screens (feature-level) | `presentation/components/` |
| Base UI component reusable outside the feature | `core/designsystem/components/` |

### Presentation Helpers

- `TimerManager` (`presentation/timer/TimerManager.kt`) — coroutine-based countdown timer. Exposes `StateFlow<TimerState>`. Methods: `start()`, `stop()`, `addTime()`, `reset()`. Inject a `CoroutineScope` on construction.
- `TurnNavigator` (`presentation/util/TurnNavigator.kt`) — pure immutable value for navigating turn history. Methods return a new `TurnNavigator` instance. Not injected; created directly in `GameplayViewModel`.

### Gameplay Phases

`GameplayPhase` enum drives the Gameplay screen's content:
- `DICE_SELECTION` — initial phase; player selects dice values
- `EVENT` — shown when dice sum is 7 (robber/pirate/barbarians)
- `MAIN_TIMER` — countdown timer phase
- `IN_BETWEEN_TIMER` — secondary player timer (Cities & Knights)

### Screen Conventions

Every screen follows the `android-presentation-mvi` skill structure. Additional project rules:
- Navigation overlays (bottom sheets, dialogs that trigger navigation) belong in Root, not the Screen composable — they need the navigation lambdas that Root owns.
- UI state classes live in `presentation/state/`. Each is a data class with `isLoading: Boolean` and `error: UiText?`.

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

ViewModels are declared in `CatanCompanionModule.kt` using `viewModelOf`.

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

### Logging

Use `core/util/LogUtils` (wraps Kermit). Never use `println` or `android.util.Log` directly.

### Git Workflow

When doing git operations be compliant with GIT_WORKFLOW.md

---

## Skills & Claude Code Conventions

This project uses KMP/CMP. The `android-*` skills below all apply — invoke them for the listed
tasks, but always generate output targeting `commonMain` unless a platform source set is
explicitly required.

| Task | Skill to invoke |
|---|---|
| New screen / ViewModel / State / Action / Event | `android-presentation-mvi` |
| New repository / DAO / mapper / data source | `android-data-layer` |
| Writing tests (ViewModel, repository, use case) | `android-testing` |
| Adding/modifying Koin modules | `android-di-koin` |
| New composable / UI component / layout | `android-compose-ui` |
| Navigation routes / nav graphs | `android-navigation` |
| Result / error type / DataError | `android-error-handling` |
| Module layout / Gradle structure | `android-module-structure` |

When invoking a skill, apply the overrides in the subsections below before generating any code.

### Skill-Specific Overrides

#### android-presentation-mvi
- Root composable name: `<Feature>ScreenRoot`, not `<Feature>Root` → `GameplayScreenRoot`
- Channel: always `Channel.BUFFERED` — skill defaults to rendezvous channel which silently
  drops events when `trySend` is called before `ObserveAsEvents` subscribes
- No `SavedStateHandle` — not available in `commonMain`

#### android-data-layer
- Result failure variant: `Result.Failure`, not `Result.Error` (matches `core/domain/Result.kt`)
- All DAO calls must be wrapped with `tryLocalRead { }` or `tryLocalWrite { }` from
  `core/data/RepositoryHelpers.kt` — never call DAO methods bare
- Room is behind `expect/actual` — no Room imports in `commonMain`

#### android-testing
- Test naming: strict `<Action>, <Prerequisites>, <Effect>` format (three comma-separated parts)
  → `Timer start, game not started, timer begins from zero`
- Test framework by source set: `commonTest` uses `kotlin.test`; JUnit5 only in `androidUnitTest`
- No `SavedStateHandle` — not applicable in `commonTest`

#### android-di-koin
- Koin module naming: `<feature>Module`, not `<feature><Layer>Module`
  → `databaseModule`, `repositoryModule`, not `catanDataModule`
- Koin startup: `initKoin.kt` (KMP multiplatform entry point), not `Application.onCreate()`

#### android-compose-ui
- No `@Preview` in `commonMain` — previews are Android-only
- State collection: `collectAsState()`, not `collectAsStateWithLifecycle()`
- No `R.string` / `stringResource()` — use `UiText` abstraction from `core/presentation`

#### android-error-handling
- Result failure variant: `Result.Failure`, not `Result.Error`
- The full Result infrastructure already exists in `core/domain/Result.kt` — do not recreate it
- This project is offline-only — all network helpers from this skill (`safeCall`,
  `responseToResult`, `constructRoute`, `HttpClient` extensions) do not apply;
  only `Result`, `EmptyResult`, and `DataError.Local` are relevant

#### android-module-structure
- This project is single-module KMP — there are no Gradle feature modules
- Features are isolated by package boundaries within `composeApp`, not Gradle module boundaries
- Source sets (`commonMain`, `androidMain`, `iosMain`, `desktopMain`) replace the
  `:core` / `:feature` Gradle module hierarchy
- No `:build-logic` or convention plugins apply to this project
- Use this skill only for conceptual guidance on layer responsibilities and dependency rules —
  ignore its Gradle module structure recommendations entirely