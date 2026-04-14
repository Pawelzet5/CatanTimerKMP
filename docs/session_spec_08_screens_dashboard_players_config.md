# Session Spec 08 — Screens: Dashboard, Players & Config

**Sesje planu:** 13  
**Gałąź startowa:** ostatni branch Sesji 12 (`session-12/gameplay-components-and-di`)  
**Mockup referencyjny:** `mockup_01_dashboard.html`, `mockup_02_game_config.html`, `mockup_07_players.html`  
**Design system:** `catan_companion_design_system.md`  

---

## Kontekst

Pierwsze ekrany aplikacji. Po tej sesji nawigacja Dashboard → GameConfig → PlayersList działa end-to-end. `App.kt` przestaje być placeholderem.

---

## PR 13a — Dashboard screen & App entry point

**Branch:** `session-13/dashboard-screen` ← `session-12/gameplay-components-and-di`

### `App.kt`
Zastąp `Text("Hello world!")` pełną nawigacją:

```kotlin
@Composable
fun App() {
    CatanTimerTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = CatanCompanionRoute.Dashboard.route
        ) {
            composable(CatanCompanionRoute.Dashboard.route) {
                DashboardScreen(
                    onNewGame = { navController.navigate(CatanCompanionRoute.GameConfig.route) },
                    onResumeGame = { gameId ->
                        navController.navigate(CatanCompanionRoute.Gameplay.createRoute(gameId))
                    },
                    onGamesList = { navController.navigate(CatanCompanionRoute.GamesList.route) },
                    onPlayersList = { navController.navigate(CatanCompanionRoute.PlayersList.route) }
                )
            }
            composable(CatanCompanionRoute.GameConfig.route) {
                GameConfigScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onGameCreated = { gameId ->
                        navController.navigate(CatanCompanionRoute.Gameplay.createRoute(gameId)) {
                            popUpTo(CatanCompanionRoute.Dashboard.route)
                        }
                    },
                    onAddPlayer = {
                        navController.navigate(CatanCompanionRoute.PlayersList.createRoute(selectionMode = true))
                    }
                )
            }
            // Pozostałe route'y — dodawane w kolejnych sesjach jako stub:
            // composable(Gameplay), composable(PlayersList), etc.
        }
    }
}
```

⚠️ Ekrany które jeszcze nie istnieją — dodaj jako stub composable zwracający `Box { Text("Coming soon") }`. Nawigacja musi się kompilować.

### `presentation/screen/DashboardScreen.kt`
Patrz `mockup_01_dashboard.html`.

Elementy UI:
- Header z tytułem "Catan Companion" i subtitle
- Karta "Resume Last Game" — widoczna tylko gdy `uiState.resumableGame != null`
- Przycisk "New Game"
- Dwie karty: "Games" i "Players" z ikonami
- Sekcja "Recent Activity" ze statystykami (liczba gier, graczy)

```kotlin
@Composable
fun DashboardScreen(
    onNewGame: () -> Unit,
    onResumeGame: (Long) -> Unit,
    onGamesList: () -> Unit,
    onPlayersList: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // Layout zgodny z mockupem
}
```

### Commit
```
feat(ui): implement DashboardScreen and wire App navigation
```

---

## PR 13b — Players screens

**Branch:** `session-13/players-screens` ← `session-13/dashboard-screen`

Patrz `mockup_07_players.html` sekcje 12 i 13.

### `presentation/screen/PlayersListScreen.kt`

Elementy UI (sekcja 12 — Players List):
- App bar z tytułem "Players" i przyciskiem "Add"
- Lista graczy przez `PlayerListItem`
- Swipe-to-delete lub long press → menu z opcjami "Hide" i "Delete"
- Jeśli `isSelectionMode = true` → tryb selekcji dla GameConfig (checkboxy)
- Dialog potwierdzenia przed usunięciem (`ConfirmationDialog`)

```kotlin
@Composable
fun PlayersListScreen(
    isSelectionMode: Boolean = false,
    onNavigateBack: () -> Unit,
    onPlayerClick: (Long) -> Unit,       // do PlayerDetails (tylko poza selectionMode)
    onPlayerSelected: (Player) -> Unit,  // tylko w selectionMode
    viewModel: PlayersListViewModel = koinViewModel()
)
```

### `presentation/screen/PlayerDetailsScreen.kt`

Elementy UI (sekcja 13 — Player Details):
- App bar z nazwą gracza i przyciskiem back
- Pole edycji nazwy
- Statystyki: liczba gier, wygrane
- Przyciski "Hide Player" i "Delete Player"
- `ConfirmationDialog` przed usunięciem/ukryciem

```kotlin
@Composable
fun PlayerDetailsScreen(
    playerId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PlayerDetailsViewModel = koinViewModel { parametersOf(playerId) }
)
```

### Commit
```
feat(ui): implement PlayersListScreen and PlayerDetailsScreen
```

---

## PR 13c — Game config screen

**Branch:** `session-13/game-config-screen` ← `session-13/players-screens`

Patrz `mockup_02_game_config.html`.

### `presentation/screen/GameConfigScreen.kt`

Elementy UI:
- App bar "New Game" z back button
- Sekcja "Turn Duration" — slider lub stepper (minuty)
- Sekcja "Number of Players" — stepper 3–6
- Sekcja "Players" — lista wybranych graczy z drag handles + przycisk "Add Player"
- Sekcja "Game Options":
  - Toggle "In-Between Turns (5–6 players)"
  - Toggle "Seafarers"
  - Toggle "Cities & Knights"
- Przycisk "Start Game" (disabled gdy `!uiState.isValid`)
- Komunikat walidacji gdy `uiState.validationError != null`

```kotlin
@Composable
fun GameConfigScreen(
    onNavigateBack: () -> Unit,
    onGameCreated: (Long) -> Unit,
    onAddPlayer: () -> Unit,
    viewModel: GameConfigViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Obserwuj navigation event
    LaunchedEffect(Unit) {
        viewModel.navigateToGameplay.collect { gameId ->
            onGameCreated(gameId)
        }
    }
    // Layout zgodny z mockupem
}
```

### Commit
```
feat(ui): implement GameConfigScreen
```

---

## Uwagi ogólne dla tej sesji

- `koinViewModel()` — Koin Compose integration; `koinViewModel { parametersOf(id) }` dla ViewModeli z parametrami
- Obserwuj `SharedFlow` navigation events przez `LaunchedEffect` — nie przez `collectAsState`
- Wszystkie teksty przez `stringResource` — zero hardkodowanych stringów
- Back navigation: `navController.popBackStack()` — nie `finish()` ani `onBackPressed()`
- `selectionMode` w `PlayersListScreen` jest przekazywany jako argument nawigacyjny przez `navArgument` w `NavHost` — patrz `CatanCompanionRoute.PlayersList.createRoute(selectionMode = true)`
