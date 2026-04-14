# Session Spec 10 — Screens: Games List, Summary & HapticService

**Sesje planu:** 15, 16  
**Gałąź startowa:** ostatni branch Sesji 14 (`session-14/gameplay-screen-stats`)  
**Mockup referencyjny:** `mockup_05_end_game_summary.html`, `mockup_06_games_list.html`  
**Design system:** `catan_companion_design_system.md`  

---

## Kontekst

Ostatnia sesja UI. Po tej sesji aplikacja jest w pełni funkcjonalna end-to-end.

---

## PR 15a — Games list screen

**Branch:** `session-15/games-list-screen` ← `session-14/gameplay-screen-stats`

Patrz `mockup_06_games_list.html` sekcja 11 — Games List.

### `presentation/screen/GamesListScreen.kt`

Elementy UI:
- App bar "Games" z back button
- Dwie sekcje: "In Progress" i "Completed" (sekcja ukryta gdy pusta)
- `GameListItem` per gra
- Swipe-to-delete z `ConfirmationDialog`
- Tap na grę `IN_PROGRESS` → nawigacja do Gameplay
- Tap na grę `COMPLETED` → nawigacja do GameSummary

```kotlin
@Composable
fun GamesListScreen(
    onNavigateBack: () -> Unit,
    onResumeGame: (Long) -> Unit,
    onGameSummary: (Long) -> Unit,
    viewModel: GamesListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Sekcja IN PROGRESS
    if (uiState.inProgressGames.isNotEmpty()) {
        // Section header: games_in_progress
        // LazyColumn z GameListItem dla każdej gry
    }

    // Sekcja COMPLETED
    if (uiState.completedGames.isNotEmpty()) {
        // Section header: games_completed
        // LazyColumn z GameListItem
    }

    // Empty state gdy obie listy puste
}
```

Dodaj route do `App.kt`:
```kotlin
composable(CatanCompanionRoute.GamesList.route) {
    GamesListScreen(
        onNavigateBack = { navController.popBackStack() },
        onResumeGame = { gameId ->
            navController.navigate(CatanCompanionRoute.Gameplay.createRoute(gameId))
        },
        onGameSummary = { gameId ->
            navController.navigate(CatanCompanionRoute.GameSummary.createRoute(gameId))
        }
    )
}
```

### Commit
```
feat(ui): implement GamesListScreen
```

---

## PR 15b — Winner selection & game summary screens

**Branch:** `session-15/summary-screens` ← `session-15/games-list-screen`

Patrz `mockup_05_end_game_summary.html` sekcje 09 i 10.

### `presentation/screen/WinnerSelectionScreen.kt`

Patrz mockup sekcja 09 — End Game / Winner Selection.

Elementy UI:
- App bar "Select Winner"
- Lista graczy z gry jako Radio buttons
- Opcja "No winner (game abandoned)" z `winnerId = null`
- Przycisk "Confirm" → wywołuje `viewModel.onFinishSession(winnerId)` → nawigacja do GameSummary

```kotlin
@Composable
fun WinnerSelectionScreen(
    gameId: Long,
    onGameFinished: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameplayViewModel = koinViewModel { parametersOf(gameId) }
)
```

⚠️ `WinnerSelectionScreen` reużywa `GameplayViewModel` — sesja jest nadal aktywna. `onFinishSession` kończy sesję i czyści `GameSessionCoordinator`. Po nawigacji do `GameSummary` sesja jest już `null`.

### `presentation/screen/GameSummaryScreen.kt`

Patrz mockup sekcja 10 — Game Summary.

Elementy UI:
- App bar "Game Summary"
- Karta zwycięzcy (trophy icon + imię) — ukryta gdy `winnerId == null`
- Statystyki ogólne: łączna liczba tur, czas trwania, średni czas tury
- `DiceDistributionChart` z `uiState.statistics?.diceDistribution`
- Sekcja statystyk graczy (średni czas tury per gracz)
- Przycisk "Back to Home" → nawigacja do Dashboard z czyszczeniem backstack

```kotlin
@Composable
fun GameSummaryScreen(
    gameId: Long,
    onNavigateHome: () -> Unit,
    viewModel: GameSummaryViewModel = koinViewModel { parametersOf(gameId) }
)
```

Dodaj routes do `App.kt`:
```kotlin
composable(
    route = CatanCompanionRoute.WinnerSelection.route,
    arguments = listOf(navArgument("gameId") { type = NavType.LongType })
) { backStackEntry ->
    WinnerSelectionScreen(
        gameId = backStackEntry.arguments?.getLong("gameId") ?: 0L,
        onGameFinished = { gameId ->
            navController.navigate(CatanCompanionRoute.GameSummary.createRoute(gameId)) {
                popUpTo(CatanCompanionRoute.Dashboard.route)
            }
        },
        onNavigateBack = { navController.popBackStack() }
    )
}
composable(
    route = CatanCompanionRoute.GameSummary.route,
    arguments = listOf(navArgument("gameId") { type = NavType.LongType })
) { backStackEntry ->
    GameSummaryScreen(
        gameId = backStackEntry.arguments?.getLong("gameId") ?: 0L,
        onNavigateHome = {
            navController.navigate(CatanCompanionRoute.Dashboard.route) {
                popUpTo(CatanCompanionRoute.Dashboard.route) { inclusive = true }
            }
        }
    )
}
```

### Commit
```
feat(ui): implement WinnerSelectionScreen and GameSummaryScreen
```

---

## PR 16a — HapticService expect/actual

**Branch:** `session-16/haptic-service` ← `session-15/summary-screens`

### `commonMain/.../presentation/service/HapticService.kt`
```kotlin
// Why expect/actual: vibration API is entirely platform-specific.
// iOS uses UIFeedbackGenerator, Android uses Vibrator/VibrationEffect,
// Desktop has no equivalent (no-op).
expect class HapticService {
    fun vibrateTimerEnd()
}
```

### `androidMain/.../presentation/service/HapticService.android.kt`
```kotlin
actual class HapticService(private val context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    actual fun vibrateTimerEnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100, 50, 100), -1
                )
            )
        } else {
            @Suppress("DEPRECATION") // Required for API < 26 support
            vibrator?.vibrate(longArrayOf(0, 100, 50, 100, 50, 100), -1)
        }
    }
}
```

### `iosMain/.../presentation/service/HapticService.ios.kt`
```kotlin
actual class HapticService {
    actual fun vibrateTimerEnd() {
        UINotificationFeedbackGenerator().notificationOccurred(
            UINotificationFeedbackType.UINotificationFeedbackTypeSuccess
        )
    }
}
```

### `desktopMain/.../presentation/service/HapticService.desktop.kt`
```kotlin
actual class HapticService {
    actual fun vibrateTimerEnd() {
        // Desktop: no-op — no vibration API available
    }
}
```

### Wiring do GameplayViewModel
Zaktualizuj `GameplayViewModel.observeTimer()` żeby reagował na timer = 0:

```kotlin
private fun observeTimer() {
    var wasRunning = false
    timerManager.state
        .onEach { timerState ->
            _uiState.update { it.copy(timerState = timerState) }
            if (wasRunning && !timerState.isRunning && timerState.remainingMillis == 0L) {
                hapticService.vibrateTimerEnd()
            }
            wasRunning = timerState.isRunning
        }
        .launchIn(viewModelScope)
}
```

`HapticService` dla Androida wymaga `Context` — dodaj do platformowych modułów DI:
```kotlin
// androidMain di/Modules.android.kt
single { HapticService(androidContext()) }

// iosMain / desktopMain di/
single { HapticService() }
```

Dodaj `hapticService: HapticService` jako parametr konstruktora `GameplayViewModel` i zaktualizuj `ViewModelModule`.

### Commit
```
feat(platform): implement HapticService expect/actual for Android, iOS, Desktop
```

---

## Uwagi ogólne dla tej sesji

- `WinnerSelectionScreen` reużywa `GameplayViewModel` przez `koinViewModel { parametersOf(gameId) }` — Koin zwróci tę samą instancję jeśli scope jest taki sam (NavBackStackEntry scope)
- `popUpTo(Dashboard) { inclusive = true }` po zakończeniu gry — czyści cały backstack do Dashboard
- `GameSummaryScreen` działa na każdej grze niezależnie od statusu — `GetGameStatisticsUseCase` nie sprawdza czy gra jest COMPLETED
- `@Suppress("DEPRECATION")` w `HapticService.android.kt` — wymagany komentarz wyjaśniający powód
