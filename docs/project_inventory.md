# CatanCompanion — Project Inventory

**Date:** 2026-04-13  
**Branch:** `data-layer-impl`  
**Spec:** `docs/catan_companion_implementation_spec.md` + `docs/catan_companion_design_spec.md`

---

## Current Project Structure

```
composeApp/src/
│
├── commonMain/
│   └── kotlin/org/example/project/
│       ├── App.kt                                      ← "Hello world!" placeholder
│       ├── di/
│       │   ├── Modules.kt                              ← coreModule + catanCompanionModule
│       │   └── initKoin.kt
│       ├── core/
│       │   ├── data/
│       │   │   └── RepositoryHelpers.kt                ← tryLocalRead / tryLocalWrite
│       │   ├── domain/
│       │   │   ├── DataError.kt
│       │   │   ├── Error.kt
│       │   │   ├── IllegalOperationError.kt
│       │   │   └── Result.kt
│       │   ├── presentation/
│       │   │   └── DataErrorToStringResource.kt
│       │   └── util/
│       │       ├── LogUtils.kt
│       │       └── UiText.kt
│       └── catan_companion_feature/
│           ├── AppConstants.kt
│           ├── domain/
│           │   ├── GameSessionCoordinator.kt           ← stateful session coordinator (project-specific)
│           │   ├── dataclass/
│           │   │   ├── Game.kt
│           │   │   ├── GameConfig.kt                   ← project-specific grouping model
│           │   │   ├── GameSession.kt                  ← project-specific in-memory session
│           │   │   ├── GameSummary.kt
│           │   │   ├── Player.kt
│           │   │   └── Turn.kt
│           │   ├── enums/
│           │   │   ├── DiceType.kt
│           │   │   ├── EventDiceType.kt
│           │   │   ├── GameExpansion.kt
│           │   │   └── GameStatus.kt
│           │   ├── factory/
│           │   │   └── TurnFactory.kt                  ← project-specific addition
│           │   └── repository/
│           │       ├── GameRepository.kt
│           │       ├── PlayerRepository.kt
│           │       └── TurnRepository.kt
│           └── data/
│               ├── local/
│               │   ├── CatanTimerDatabase.kt
│               │   ├── DatabaseConstants.kt
│               │   ├── DatabaseConstructor.kt
│               │   ├── DatabaseFactory.kt              ← expect declaration
│               │   ├── converter/
│               │   │   ├── EventDiceTypeConverter.kt
│               │   │   ├── GameExpansionConverter.kt
│               │   │   └── GameStatusConverter.kt
│               │   ├── dao/
│               │   │   ├── GameDao.kt
│               │   │   ├── PlayerDao.kt
│               │   │   └── TurnDao.kt
│               │   ├── entity/
│               │   │   ├── GameEntity.kt
│               │   │   ├── GamePlayerCrossRefEntity.kt
│               │   │   ├── GameSummaryProjection.kt    ← Room projection (project-specific)
│               │   │   ├── PlayerEntity.kt
│               │   │   └── TurnEntity.kt
│               │   └── mapper/
│               │       ├── GameMappers.kt
│               │       ├── PlayerMappers.kt
│               │       └── TurnMappers.kt
│               └── repository/
│                   ├── GameRepositoryImpl.kt
│                   ├── PlayerRepositoryImpl.kt
│                   └── TurnRepositoryImpl.kt
│
├── androidMain/
│   └── kotlin/org/example/project/
│       ├── CatanTimerApplication.kt
│       ├── MainActivity.kt
│       ├── catan_companion_feature/data/local/DatabaseFactory.android.kt
│       └── di/Modules.android.kt
│
├── iosMain/
│   └── kotlin/org/example/project/
│       ├── MainViewController.kt
│       ├── catan_companion_feature/data/local/DatabaseFactory.ios.kt
│       └── di/Modules.ios.kt
│
├── desktopMain/
│   └── kotlin/org/example/project/
│       ├── main.kt
│       ├── catan_companion_feature/data/local/DatabaseFactory.desktop.kt
│       └── di/Modules.desktop.kt
│
├── commonTest/
│   └── kotlin/org/example/project/
│       ├── ComposeAppCommonTest.kt
│       ├── core/data/RepositoryHelpersTest.kt
│       └── catan_companion_feature/
│           ├── DataPreparer.kt
│           ├── data/
│           │   ├── fakes/
│           │   │   ├── dao/
│           │   │   │   ├── FakeGameDao.kt
│           │   │   │   ├── FakePlayerDao.kt
│           │   │   │   └── FakeTurnDao.kt
│           │   │   └── repository/
│           │   │       ├── FakeGameRepository.kt
│           │   │       └── FakeTurnRepository.kt
│           │   ├── local/dao/
│           │   │   ├── TurnDaoContractTest.kt          ← abstract contract base
│           │   │   └── FakeTurnDaoContractTest.kt
│           │   └── repository/
│           │       ├── GameRepositoryImplTest.kt
│           │       ├── PlayerRepositoryImplTest.kt
│           │       └── TurnRepositoryImplTest.kt
│           └── domain/
│               └── GameSessionCoordinatorTest.kt
│
└── androidUnitTest/
    └── kotlin/org/example/project/catan_companion_feature/data/local/dao/
        ├── BaseDaoTest.kt
        └── RealTurnDaoContractTest.kt                  ← Room + Robolectric
```

---

## Domain Layer

### Models (`domain/dataclass/`)

| Class | Status | Notes |
|---|---|---|
| `Player` | Exists — incomplete | Missing: `isHidden`, `gamesPlayed` (derived), `gamesWon` (derived) |
| `Game` | Exists — diverges from spec | Uses nested `GameConfig` instead of flat fields. Missing: `winnerId` |
| `GameConfig` | Exists — project-specific | Not in spec. Groups config fields; contains `players: List<Player>` and computed `hasEventDice` |
| `GameSession` | Exists — project-specific | Not in spec. In-memory active session with `latestTurn`, `selectedTurn`, `recentTurns` |
| `GameSummary` | Exists — incomplete | Missing: `expansions`, player names |
| `Turn` | Exists — incomplete | Missing computed properties: `diceSum`, `isRobberRoll` |
| `GamePlayer` | **Missing** | Spec domain model for ordered player-in-game |
| `DiceRoll` | **Missing** | Spec model: `red`, `yellow`, `event`, computed `sum` |
| `GameStatistics` | **Missing** | Spec model: totals, distributions, per-player averages |
| `BarbarianState` | **Missing** | Spec model: `position`, `raidsCompleted`, `hasFirstRaidOccurred` |

### Enums (`domain/enums/`)

| Enum | Status | Notes |
|---|---|---|
| `GameExpansion` | Exists — matches spec | `SEAFARERS`, `CITIES_AND_KNIGHTS` |
| `EventDiceType` | Exists — matches spec | `POLITICS`, `SCIENCE`, `TRADE`, `BARBARIANS` |
| `DiceType` | Exists | |
| `GameStatus` | Exists — **values mismatch spec** | Current: `ACTIVE`/`FINISHED`. Spec: `IN_PROGRESS`/`COMPLETED` |

### Repository Interfaces (`domain/repository/`)

| Interface | Existing methods | Missing vs spec |
|---|---|---|
| `PlayerRepository` | `addPlayer`, `getPlayer`, `getPlayers(query): Flow`, `getPlayersForGame`, `removePlayer` | `updatePlayer`, `hidePlayer`, `canDeletePlayer`; `getAllPlayers` / `getVisiblePlayers` as Flow |
| `GameRepository` | `addGame`, `getGame`, `getGameSummaries(): Flow`, `getActiveGame` (stub), `saveGameAsFinished` | `updateGameSettings`, `deleteGame`; `getAllGames` / `getInProgressGames` / `getCompletedGames` / `getMostRecentInProgressGame` as Flows |
| `TurnRepository` | `addTurn`, `updateTurn`, `getTurnsForGame` (all `suspend`, not Flow) | Flow-returning variants for all queries; `getTurnById`, `getCurrentTurn`, `updateDiceRoll`, `updateDuration`, `setSecondaryPlayer` |

### Use Cases (`domain/usecase/`) — **entirely missing**

All 16 use cases from the spec are absent. The project uses `GameSessionCoordinator` for active gameplay, but the broader use-case layer is not implemented.

| Group | Missing use cases |
|---|---|
| player/ | `GetAllPlayersUseCase`, `GetPlayerByIdUseCase`, `CreatePlayerUseCase`, `UpdatePlayerUseCase`, `HidePlayerUseCase`, `DeletePlayerUseCase` |
| game/ | `GetAllGamesUseCase`, `GetGameByIdUseCase`, `GetInProgressGameUseCase`, `CreateGameUseCase`, `UpdateGameSettingsUseCase`, `EndGameUseCase`, `DeleteGameUseCase` |
| turn/ | `GetTurnsForGameUseCase`, `GetCurrentTurnUseCase`, `CreateTurnUseCase`, `UpdateTurnUseCase`, `CalculateBarbarianPositionUseCase` |
| statistics/ | `GetGameStatisticsUseCase`, `GetPlayerStatisticsUseCase`, `GetDiceDistributionUseCase` |

### Other domain

| Class | Status |
|---|---|
| `GameSessionCoordinator` | Exists — project-specific stateful coordinator (not in spec) |
| `TurnFactory` | Exists — project-specific factory (not in spec) |

---

## Data Layer

### Entities (`data/local/entity/`)

| Entity | Status | Notes |
|---|---|---|
| `PlayerEntity` | Exists — incomplete | Missing: `isHidden` field |
| `GameEntity` | Exists — incomplete | Missing: `winnerId` field |
| `GamePlayerCrossRefEntity` | Exists — diverges from spec | Spec names it `GamePlayerEntity`. Has `playerIndex` ordering field (better than spec) |
| `TurnEntity` | Exists — matches spec | |
| `GameSummaryProjection` | Exists — project-specific | Room `@DatabaseView`-style projection; not in spec |

### DAOs (`data/local/dao/`)

| DAO | Existing methods | Missing vs spec |
|---|---|---|
| `PlayerDao` | `insertPlayer`, `getPlayer`, `getAllPlayers` (suspend), `searchPlayers` (Flow), `getPlayersForGame`, `getGameCountForPlayer`, `deletePlayer` | `getById` as Flow, `update(entity)`, `hide(id)` query |
| `GameDao` | `insertGame`, `insertGamePlayerCrossRefs`, `getGame`, `getGameSummaries()` (Flow), `updateGameStatusToFinished` | `getAll`/`getInProgress`/`getCompleted`/`getMostRecentInProgress` as Flows, `update(entity)`, `delete(entity)` |
| `TurnDao` | `insertTurn`, `updateTurn`, `getLastTurn`, `getTurnsForGame` (all `suspend`) | Flow-returning variants; `getById` (Flow), `getCurrentForGame` (Flow), `getBarbarianRollCount` (Flow) |
| `GamePlayerDao` | **Missing entirely** | Spec has a dedicated DAO; project merges into `GameDao.insertGamePlayerCrossRefs` |

### Database

| Class | Status | Notes |
|---|---|---|
| `CatanTimerDatabase` | Exists | Named `CatanTimerDatabase` (spec: `CatanCompanionDatabase`); correct entities and converters |

### Type Converters (`data/local/converter/`)

All three converters exist and match the spec: `GameExpansionConverter`, `GameStatusConverter`, `EventDiceTypeConverter`.

### Mappers (`data/local/mapper/`)

`PlayerMappers.kt`, `GameMappers.kt`, `TurnMappers.kt` — all exist. Implemented as extension functions rather than mapper classes as spec suggests; functionally equivalent.

### Repository Implementations (`data/repository/`)

| Impl | Status | Notes |
|---|---|---|
| `PlayerRepositoryImpl` | Exists | Implements current interface only |
| `GameRepositoryImpl` | Exists — partially stubbed | `getActiveGame()` returns `NOT_FOUND` with a TODO comment |
| `TurnRepositoryImpl` | Exists | All methods suspend; no Flow exposure |

---

## Presentation Layer — **entirely missing**

| Category | Missing |
|---|---|
| ViewModels | `DashboardViewModel`, `GameConfigViewModel`, `GameplayViewModel`, `PlayersListViewModel`, `PlayerDetailsViewModel`, `GamesListViewModel`, `GameSummaryViewModel` |
| UI States | `DashboardUiState`, `GameConfigUiState`, `GameplayUiState`, `PlayersListUiState`, `PlayerDetailsUiState`, `GamesListUiState`, `GameSummaryUiState`, `TimerState`, `GameplayPhase` |
| Screens | `DashboardScreen`, `GameConfigScreen`, `GameplayScreen`, `PlayersListScreen`, `PlayerDetailsScreen`, `GamesListScreen`, `GameSummaryScreen`, `WinnerSelectionScreen` |
| Dice components | `DiceRow`, `Dice`, `EventDiceRow`, `EventDice`, `DiceLayout` |
| Timer components | `GameTimer`, `TimerControls` |
| Chart components | `DiceDistributionChart`, `AnimatedBarChart` |
| Gameplay components | `TurnNavigator`, `EventPhaseContent`, `BarbarianTracker`, `StatisticsPopup` |
| Common components | `PlayerListItem`, `GameListItem`, `ConfirmationDialog` |
| Navigation | `CatanCompanionNavigation` |

`App.kt` contains only a `Text("Hello world!")` placeholder.

---

## DI Layer

### What exists

| Module | Contents |
|---|---|
| `coreModule` | Database singleton (via `DatabaseFactory`) |
| `catanCompanionModule` | DAOs (3 singletons) + repository bindings (`GameRepository`, `PlayerRepository`, `TurnRepository`) |
| `platformModule` (per platform) | `DatabaseFactory` actual — Android, iOS, Desktop |

### What's missing

- No use case bindings (use cases don't exist yet)
- No ViewModel bindings
- Spec calls for separate `DatabaseModule`, `RepositoryModule`, `UseCaseModule`, `ViewModelModule` files — currently one flat module

---

## String Resources

| File | Status |
|---|---|
| `composeResources/values/strings.xml` | Exists (English default) |
| `composeResources/values-pl/strings.xml` | **Missing** — Polish locale required by spec |

---

## Summary

| Layer | Exists | Partially implemented | Missing |
|---|---|---|---|
| Domain models | 2 (project-specific) | 4 (field/property gaps) | 4 |
| Domain enums | 4 | 1 (values mismatch) | — |
| Repository interfaces | 3 | 3 (method gaps) | — |
| Use cases | — | — | 16 |
| Entities | 5 | 2 (field gaps) | — |
| DAOs | 3 | 3 (method gaps) | 1 (`GamePlayerDao`) |
| Repository impls | 2 | 1 (stubbed method) | — |
| Mappers | 3 | — | — |
| Type converters | 3 | — | — |
| DI modules | 1 | — | Use case + ViewModel bindings |
| ViewModels | — | — | 7 |
| UI state classes | — | — | 9+ |
| Screens | — | — | 8 |
| Components | — | — | ~15 |
| Navigation | — | — | 1 |
| String resources | 1 (EN) | — | 1 (PL) |
