# Session Spec 09 — Screen: Gameplay

**Sesje planu:** 14  
**Gałąź startowa:** ostatni branch Sesji 13 (`session-13/game-config-screen`)  
**Mockup referencyjny:** `mockup_03_gameplay_dice_event.html`, `mockup_04_gameplay_menu_stats.html`  
**Design system:** `catan_companion_design_system.md`  

---

## Kontekst

Gameplay screen to najbardziej złożony ekran w aplikacji. Implementowany w trzech PR-ach — każdy PR to zamknięty, działający stan ekranu. Buduj inkrementalnie: najpierw dice phase działa end-to-end, potem dodajesz event + timer, na końcu statistics + settings.

Wszystkie zmiany są w **jednym pliku** `GameplayScreen.kt` — PR-y różnią się zakresem implementacji, nie liczbą plików.

---

## PR 14a — Dice selection phase

**Branch:** `session-14/gameplay-screen-dice` ← `session-13/game-config-screen`

### `presentation/screen/GameplayScreen.kt` — wersja początkowa

Patrz `mockup_03_gameplay_dice_event.html` sekcja 03 — Dice Selection Phase.

Zaimplementuj kompletny layout ekranu z obsługą fazy `DICE_SELECTION`:

**Elementy stałe (widoczne we wszystkich fazach):**
- App bar: numer tury (`gameplay_turn`), imię gracza (`gameplay_player_turn`), przycisk menu (3 kropki) → `showSettingsSheet`
- Jeśli ekspansja Cities & Knights aktywna: `BarbarianTracker` w app barze
- Turn navigator: strzałki poprzednia/następna tura, label z numerem tury

**Faza DICE_SELECTION:**
- `DiceRow` dla czerwonej kości
- `DiceRow` dla żółtej kości
- `EventDiceRow` — tylko gdy `game.expansions.contains(GameExpansion.CITIES_AND_KNIGHTS)`
- Przycisk "Continue" — aktywny gdy obie kości są wybrane; jeśli Cities&Knights to też event dice musi być wybrana
- Label "Turn X" nad kostkami

```kotlin
@Composable
fun GameplayScreen(
    gameId: Long,
    onNavigateToSummary: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameplayViewModel = koinViewModel { parametersOf(gameId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.phase) {
        GameplayPhase.DICE_SELECTION -> DiceSelectionContent(
            uiState = uiState,
            onDiceSelected = viewModel::onDiceSelected,
            onEventDiceSelected = { event ->
                val dice = uiState.pendingDiceEdit
                viewModel.onDiceSelected(dice?.red ?: 0, dice?.yellow ?: 0, event)
            },
            onContinue = viewModel::onContinueFromDice
        )
        GameplayPhase.EVENT -> { /* PR 14b */ }
        GameplayPhase.MAIN_TIMER -> { /* PR 14b */ }
        GameplayPhase.IN_BETWEEN_TIMER -> { /* PR 14b */ }
    }

    // Turn navigator (zawsze widoczny)
    // App bar (zawsze widoczny)
    // Settings sheet — PR 14c
    // Statistics popup — PR 14c
}
```

Dodaj `GameplayScreen` do `NavHost` w `App.kt`:
```kotlin
composable(
    route = CatanCompanionRoute.Gameplay.route,
    arguments = listOf(navArgument("gameId") { type = NavType.LongType })
) { backStackEntry ->
    GameplayScreen(
        gameId = backStackEntry.arguments?.getLong("gameId") ?: 0L,
        onNavigateToSummary = { gameId ->
            navController.navigate(CatanCompanionRoute.GameSummary.createRoute(gameId))
        },
        onNavigateBack = { navController.popBackStack() }
    )
}
```

### Commit
```
feat(ui): implement GameplayScreen dice selection phase
```

---

## PR 14b — Event phase, timer phase & turn navigation

**Branch:** `session-14/gameplay-screen-timer` ← `session-14/gameplay-screen-dice`

Patrz mockupy: sekcja 04 Event Phase, sekcja 05 Timer Phase.

### Aktualizacja `GameplayScreen.kt`

**Faza EVENT:**
Użyj `EventPhaseContent` — komponent już istnieje z Sesji 12.
```kotlin
GameplayPhase.EVENT -> EventPhaseContent(
    turn = uiState.currentTurn ?: return,
    game = uiState.game ?: return,
    barbarianState = uiState.barbarianState,
    onContinue = viewModel::onContinueFromEvent
)
```

**Faza MAIN_TIMER:**
Patrz mockup sekcja 05 — Timer Phase.
- Imię gracza (duże, na środku)
- `GameTimer` z `remainingMillis`
- `TimerControls` (start/stop, +10s, reset)
- Przycisk "Next Turn" — dostępny zawsze (kończy turę z aktualnym czasem)
- Opcjonalnie przycisk "In-Between Turn" — tylko gdy `game.specialTurnRuleEnabled`

```kotlin
GameplayPhase.MAIN_TIMER -> TimerPhaseContent(
    uiState = uiState,
    onStartStop = {
        if (uiState.timerState.isRunning) viewModel.onStopTimer()
        else viewModel.onStartTimer()
    },
    onAddTime = viewModel::onAddTime,
    onReset = viewModel::onResetTimer,
    onNextTurn = viewModel::onNextTurn
)
```

**Turn Navigator:**
Pasek nawigacji historii tur — strzałki + numer tury.
- Lewy chevron → `viewModel.onNavigateToPreviousTurn()`
- Prawy chevron → `viewModel.onNavigateToNextTurn()`
- "Jump to current" button — widoczny gdy `!uiState.isViewingLatest`
- Gdy `isViewingLatest = false` — UI w trybie "read only": kostki pokazują historyczne wartości, timer/przyciski akcji ukryte

### Commit
```
feat(ui): add event phase, timer phase and turn navigation to GameplayScreen
```

---

## PR 14c — Statistics popup & settings sheet

**Branch:** `session-14/gameplay-screen-stats` ← `session-14/gameplay-screen-timer`

Patrz mockupy: sekcja 06 In-Game Menu, sekcja 07 Dice Statistics, sekcja 08 Change Game Settings.

### Aktualizacja `GameplayScreen.kt`

**In-Game Menu (Bottom Sheet):**
Patrz mockup sekcja 06. Wywoływany przez ikonę menu (3 kropki) w app barze.

```kotlin
if (uiState.showSettingsSheet) {
    ModalBottomSheet(onDismissRequest = { /* viewModel.hideSettingsSheet() */ }) {
        // Opcje menu:
        // - "View Statistics" → viewModel.onShowStatistics() → showStatisticsPopup = true
        // - "Change Game Settings" → rozwinięcie ustawień inline
        // - "End Game" → dialog potwierdzenia → nawigacja do WinnerSelection
    }
}
```

**Change Game Settings (inline w bottom sheet):**
Patrz mockup sekcja 08.
- Toggles dla expansions i specialTurnRule (takie same jak w GameConfig)
- Przycisk "Save Changes" → `viewModel.onUpdateGameSettings(...)`
- Przycisk "Start New Game With These Settings" → nawigacja do GameConfig
- Warning message: `settings_warning_message`

**Statistics Popup:**
Patrz mockup sekcja 07. Użyj `StatisticsPopup` komponentu z Sesji 12.

```kotlin
if (uiState.showStatisticsPopup && uiState.diceDistribution != null) {
    StatisticsPopup(
        distribution = uiState.diceDistribution,
        onDismiss = { /* viewModel.hideStatisticsPopup() */ }
    )
}
```

**End Game flow:**
- Menu "End Game" → `ConfirmationDialog`
- Potwierdzenie → `viewModel.onEndGame()` → nawigacja do `WinnerSelection`

Dodaj missing ViewModel metody jeśli potrzebne:
```kotlin
fun onShowStatisticsPopup() { _uiState.update { it.copy(showStatisticsPopup = true) } }
fun onHideStatisticsPopup() { _uiState.update { it.copy(showStatisticsPopup = false) } }
fun onShowSettingsSheet() { _uiState.update { it.copy(showSettingsSheet = true) } }
fun onHideSettingsSheet() { _uiState.update { it.copy(showSettingsSheet = false) } }
```

### Commit
```
feat(ui): add statistics popup and settings sheet to GameplayScreen
```

---

## Uwagi ogólne dla tej sesji

- **Tryb historii (isViewingLatest = false):** timer, przyciski Next Turn i Continue muszą być ukryte lub disabled — użytkownik tylko przegląda, nie może modyfikować aktywnej tury przez historyczny widok
- **Cities & Knights:** `EventDiceRow` i `BarbarianTracker` widoczne tylko gdy `game.expansions.contains(GameExpansion.CITIES_AND_KNIGHTS)` — zawsze sprawdzaj warunek
- **Seafarers:** wpływa tylko na komunikat w Event Phase (pirat vs złodziej) — `event_move_robber_or_pirate` vs `event_move_robber`
- `ModalBottomSheet` z Material3 — wymaga `ExperimentalMaterial3Api` jeśli jest w użyciu; dodaj `@OptIn` z komentarzem dlaczego
- Navigation event "End Game" → WinnerSelection: `onNavigateToWinnerSelection: (Long) -> Unit` jako parametr `GameplayScreen`
