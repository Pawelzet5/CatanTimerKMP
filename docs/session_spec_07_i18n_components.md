# Session Spec 07 — i18n & Common UI Components

**Sesje planu:** 11, 12  
**Gałąź startowa:** ostatni branch Sesji 10 (`session-10/viewmodel-tests`)  
**Mockup referencyjny:** wszystkie pliki mockup (komponenty przewijają się przez wiele ekranów)  
**Design system:** `catan_companion_design_system.md`

---

## Kontekst

Ta sesja produkuje wszystkie zasoby i komponenty wielokrotnego użytku. Po jej zakończeniu DI jest w pełni skonfigurowane (ViewModelModule), design system wdrożony, i można budować ekrany.

---

## PR 11a — String resources

**Branch:** `session-11/i18n` ← `session-10/viewmodel-tests`

### `composeResources/values/strings.xml`
Upewnij się że wszystkie poniższe klucze istnieją. Dodaj brakujące — nie usuwaj istniejących.

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Dashboard -->
    <string name="dashboard_title">Catan Companion</string>
    <string name="dashboard_subtitle">Board game timer &amp; tracker</string>
    <string name="dashboard_resume_game">Resume Last Game</string>
    <string name="dashboard_new_game">New Game</string>
    <string name="dashboard_games_list">Games</string>
    <string name="dashboard_players_list">Players</string>
    <string name="dashboard_recent_activity">Recent Activity</string>

    <!-- Game Configuration -->
    <string name="config_title">New Game</string>
    <string name="config_turn_duration">Turn Duration</string>
    <string name="config_player_count">Number of Players</string>
    <string name="config_players">Players</string>
    <string name="config_add_player">Add Player</string>
    <string name="config_options">Game Options</string>
    <string name="config_in_between_turns">In-Between Turns (5–6 players)</string>
    <string name="config_seafarers">Seafarers</string>
    <string name="config_cities_knights">Cities &amp; Knights</string>
    <string name="config_start_game">Start Game</string>

    <!-- Gameplay -->
    <string name="gameplay_turn">Turn %1$d</string>
    <string name="gameplay_player_turn">%1$s\'s Turn</string>
    <string name="gameplay_continue">Continue</string>
    <string name="gameplay_next_turn">Next Turn</string>
    <string name="gameplay_in_between">In-Between Turn (%1$s)</string>

    <!-- Timer -->
    <string name="timer_start">Start</string>
    <string name="timer_stop">Stop</string>
    <string name="timer_add_10">+10 sec</string>
    <string name="timer_reset">Reset</string>

    <!-- Events -->
    <string name="event_robber_title">Count Your Cards!</string>
    <string name="event_robber_message">All players must count their cards.\nIf you have more than 7, discard half.</string>
    <string name="event_move_robber">Move the thief to a different tile.</string>
    <string name="event_move_robber_or_pirate">Move the thief or pirate to a different tile.</string>
    <string name="event_robber_not_active">The thief cannot be moved yet.\n(First barbarian raid has not occurred)</string>
    <string name="event_barbarians_arrived">The barbarians have arrived!\nResolve the attack now.</string>

    <!-- Statistics -->
    <string name="stats_title">Statistics</string>
    <string name="stats_dice_distribution">Dice Distribution</string>
    <string name="stats_event_dice">Event Dice</string>
    <string name="stats_total_turns">Total Turns</string>
    <string name="stats_avg_turn_time">Average Turn Time</string>

    <!-- End Game -->
    <string name="end_game_select_winner">Select Winner</string>
    <string name="end_game_no_winner">No winner (game abandoned)</string>
    <string name="end_game_confirm">Confirm</string>
    <string name="end_game_summary">Game Summary</string>
    <string name="end_game_winner">Winner: %1$s</string>
    <string name="end_game_duration">Duration</string>
    <string name="end_game_back_home">Back to Home</string>

    <!-- Players -->
    <string name="players_title">Players</string>
    <string name="players_add">Add</string>
    <string name="players_games_count">%1$d games</string>
    <string name="players_hide">Hide Player</string>
    <string name="players_delete">Delete Player</string>

    <!-- Games -->
    <string name="games_title">Games</string>
    <string name="games_in_progress">In Progress</string>
    <string name="games_completed">Completed</string>
    <string name="games_turn_count">Turn %1$d</string>
    <string name="games_winner">Winner: %1$s</string>

    <!-- Menu -->
    <string name="menu_statistics">View Statistics</string>
    <string name="menu_settings">Change Game Settings</string>
    <string name="menu_end_game">End Game</string>

    <!-- Settings Warning -->
    <string name="settings_warning_title">Warning</string>
    <string name="settings_warning_message">Changing settings mid-game may cause data inconsistencies. Consider starting a new game instead.</string>
    <string name="settings_save">Save Changes</string>
    <string name="settings_new_game">Start New Game With These Settings</string>
    <string name="settings_cancel">Cancel</string>

    <!-- Validation -->
    <string name="validation_player_count">Player count must be 3-6</string>
    <string name="validation_duplicate_players">Duplicate players not allowed</string>
    <string name="validation_in_between_requires_5">In-between turns requires 5+ players</string>

    <!-- Common -->
    <string name="common_confirm">Confirm</string>
    <string name="common_cancel">Cancel</string>
    <string name="common_save">Save</string>
    <string name="common_delete">Delete</string>
    <string name="common_edit">Edit</string>
    <string name="common_back">Back</string>
</resources>
```

### `composeResources/values-pl/strings.xml`
Nowy plik — polskie tłumaczenie wszystkich kluczy:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Dashboard -->
    <string name="dashboard_title">Catan Companion</string>
    <string name="dashboard_subtitle">Timer i tracker do gier planszowych</string>
    <string name="dashboard_resume_game">Wznów ostatnią grę</string>
    <string name="dashboard_new_game">Nowa gra</string>
    <string name="dashboard_games_list">Gry</string>
    <string name="dashboard_players_list">Gracze</string>
    <string name="dashboard_recent_activity">Ostatnia aktywność</string>

    <!-- Game Configuration -->
    <string name="config_title">Nowa gra</string>
    <string name="config_turn_duration">Czas tury</string>
    <string name="config_player_count">Liczba graczy</string>
    <string name="config_players">Gracze</string>
    <string name="config_add_player">Dodaj gracza</string>
    <string name="config_options">Opcje gry</string>
    <string name="config_in_between_turns">Tury pośrednie (5–6 graczy)</string>
    <string name="config_seafarers">Żeglarze</string>
    <string name="config_cities_knights">Miasta i Rycerze</string>
    <string name="config_start_game">Rozpocznij grę</string>

    <!-- Gameplay -->
    <string name="gameplay_turn">Tura %1$d</string>
    <string name="gameplay_player_turn">Tura gracza %1$s</string>
    <string name="gameplay_continue">Dalej</string>
    <string name="gameplay_next_turn">Następna tura</string>
    <string name="gameplay_in_between">Tura pośrednia (%1$s)</string>

    <!-- Timer -->
    <string name="timer_start">Start</string>
    <string name="timer_stop">Stop</string>
    <string name="timer_add_10">+10 sek</string>
    <string name="timer_reset">Reset</string>

    <!-- Events -->
    <string name="event_robber_title">Policz karty!</string>
    <string name="event_robber_message">Wszyscy gracze muszą policzyć karty.\nJeśli masz więcej niż 7, odrzuć połowę.</string>
    <string name="event_move_robber">Przesuń złodzieja na inne pole.</string>
    <string name="event_move_robber_or_pirate">Przesuń złodzieja lub pirata na inne pole.</string>
    <string name="event_robber_not_active">Złodziej nie może być jeszcze przesunięty.\n(Pierwszy najazd barbarzyńców jeszcze nie nastąpił)</string>
    <string name="event_barbarians_arrived">Barbarzyńcy przybyli!\nRozstrzygnij atak.</string>

    <!-- Statistics -->
    <string name="stats_title">Statystyki</string>
    <string name="stats_dice_distribution">Rozkład wyników kości</string>
    <string name="stats_event_dice">Kostka zdarzeń</string>
    <string name="stats_total_turns">Łączna liczba tur</string>
    <string name="stats_avg_turn_time">Średni czas tury</string>

    <!-- End Game -->
    <string name="end_game_select_winner">Wybierz zwycięzcę</string>
    <string name="end_game_no_winner">Bez zwycięzcy (gra przerwana)</string>
    <string name="end_game_confirm">Potwierdź</string>
    <string name="end_game_summary">Podsumowanie gry</string>
    <string name="end_game_winner">Zwycięzca: %1$s</string>
    <string name="end_game_duration">Czas trwania</string>
    <string name="end_game_back_home">Wróć do menu</string>

    <!-- Players -->
    <string name="players_title">Gracze</string>
    <string name="players_add">Dodaj</string>
    <string name="players_games_count">%1$d gier</string>
    <string name="players_hide">Ukryj gracza</string>
    <string name="players_delete">Usuń gracza</string>

    <!-- Games -->
    <string name="games_title">Gry</string>
    <string name="games_in_progress">W toku</string>
    <string name="games_completed">Zakończone</string>
    <string name="games_turn_count">Tura %1$d</string>
    <string name="games_winner">Zwycięzca: %1$s</string>

    <!-- Menu -->
    <string name="menu_statistics">Pokaż statystyki</string>
    <string name="menu_settings">Zmień ustawienia gry</string>
    <string name="menu_end_game">Zakończ grę</string>

    <!-- Settings Warning -->
    <string name="settings_warning_title">Uwaga</string>
    <string name="settings_warning_message">Zmiana ustawień w trakcie gry może powodować niespójności danych. Rozważ rozpoczęcie nowej gry.</string>
    <string name="settings_save">Zapisz zmiany</string>
    <string name="settings_new_game">Rozpocznij nową grę z tymi ustawieniami</string>
    <string name="settings_cancel">Anuluj</string>

    <!-- Validation -->
    <string name="validation_player_count">Liczba graczy musi wynosić 3-6</string>
    <string name="validation_duplicate_players">Gracze nie mogą się powtarzać</string>
    <string name="validation_in_between_requires_5">Tury pośrednie wymagają min. 5 graczy</string>

    <!-- Common -->
    <string name="common_confirm">Potwierdź</string>
    <string name="common_cancel">Anuluj</string>
    <string name="common_save">Zapisz</string>
    <string name="common_delete">Usuń</string>
    <string name="common_edit">Edytuj</string>
    <string name="common_back">Wstecz</string>
</resources>
```

### Commit
```
chore(i18n): add complete EN and PL string resources
```

---

## PR 12a — Design System

**Branch:** `session-12/design-system` ← `session-11/i18n`

⚠️ **Krytyczne:** Design system jest fundamentem wszystkich ekranów. Żaden ekran ani komponent nie może definiować własnych kolorów, odstępów ani typografii inline. Wszystko musi pochodzić z tokenów zdefiniowanych w tej sekcji.

Zaimplementuj design system zgodnie z `catan_companion_design_system.md`. Ten plik definiuje dokładne wartości — użyj go jako jedynego źródła prawdy dla wartości liczbowych i kolorów.

### Struktura plików

```
core/designsystem/
├── Color.kt          # Wszystkie kolory palety — light i dark
├── Typography.kt     # Type scale — wszystkie style tekstowe
├── Spacing.kt        # Stałe odstępów (dp)
├── Theme.kt          # CatanTimerTheme — łączy wszystko razem
└── components/       # Bazowe, w pełni reużywalne komponenty UI
    ├── CatanButton.kt
    ├── CatanTextField.kt
    ├── CatanText.kt
    └── ...           # Inne prymitywy UI reużywalne potencjalnie poza feature
```

### `core/designsystem/Color.kt`

Zdefiniuj wszystkie kolory z design system jako nazwane stałe `Color`. Oddziel wartości palety od przypisań semantycznych:

```kotlin
// Paleta — surowe wartości
val CatanOrange = Color(0xFF...)
val CatanDarkBrown = Color(0xFF...)
// ... pozostałe zgodnie z catan_companion_design_system.md

// Schematy semantyczne
internal val LightColorScheme = lightColorScheme(
    primary = CatanOrange,
    // ... zgodnie z design system
)

internal val DarkColorScheme = darkColorScheme(
    primary = ...,
    // ... zgodnie z design system
)
```

### `core/designsystem/Typography.kt`

Zdefiniuj pełny type scale zgodny z design system. Każdy styl typograficzny musi być nazwany według roli semantycznej (np. `displayLarge` dla timera), nie wyglądu (nie `bigRedText`):

```kotlin
val CatanTypography = Typography(
    displayLarge = TextStyle(...),   // używany np. dla timera MM:SS
    titleLarge = TextStyle(...),     // nagłówki sekcji
    bodyMedium = TextStyle(...),     // treść ogólna
    // ... pozostałe zgodnie z design system
)
```

### `core/designsystem/Spacing.kt`

Zdefiniuj wszystkie używane w aplikacji wartości odstępów jako nazwane stałe. Zero hardkodowanych wartości `dp` poza tym plikiem:

```kotlin
object CatanSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
```

### `core/designsystem/Theme.kt`

`CatanTimerTheme` jest jedynym wrapperem całego content drzewa aplikacji. Łączy kolory, typografię i dostarcza je przez `MaterialTheme`:

```kotlin
@Composable
fun CatanTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CatanTypography,
        content = content
    )
}
```

Zaktualizuj `App.kt` żeby owijał content w `CatanTimerTheme`.

### `core/designsystem/components/` — bazowe komponenty UI

Komponenty w tym pakiecie są prymitywami — w pełni reużywalne, niezależne od domeny, potencjalnie używane poza `catan_companion_feature`. Każdy wrappuje odpowiedni komponent Material3 i wymusza tokeny design systemu.

Przykłady komponentów do zdefiniowania (dostosuj do potrzeb mockupów):

```kotlin
// CatanButton.kt
@Composable
fun CatanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)

// CatanTextField.kt
@Composable
fun CatanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
)
```

Zasady dla komponentów w `core/designsystem/components/`:
- Zero referencji do klas domenowych (`Player`, `Game` itp.)
- Zero `ViewModel` ani `koinViewModel()` wewnątrz
- Parametry wyłącznie prymitywne lub Compose typy (`String`, `Boolean`, `() -> Unit`, `Modifier`)
- Kolory i odstępy wyłącznie z `MaterialTheme` — nigdy hardkodowane

### Commit
```
feat(ui): implement design system — Color, Typography, Spacing, CatanTimerTheme, base components
```

---

## PR 12b — Feature-level shared components

**Branch:** `session-12/common-components` ← `session-12/design-system`

Patrz mockupy: `mockup_07_players.html` dla `PlayerListItem`, `mockup_06_games_list.html` dla `GameListItem`.

Komponenty w `presentation/components/` są współdzielone między wieloma widokami w obrębie `catan_companion_feature`. W odróżnieniu od `core/designsystem/components/` mogą znać typy domenowe.

### `presentation/components/PlayerListItem.kt`
```kotlin
@Composable
fun PlayerListItem(
    player: Player,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
// Wyświetla: imię gracza, liczbę gier (players_games_count)
// Patrz mockup_07_players.html — sekcja 12 Players List
```

### `presentation/components/GameListItem.kt`
```kotlin
@Composable
fun GameListItem(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
// Wyświetla: status, datę, liczbę tur, zwycięzcę (jeśli completed)
// Patrz mockup_06_games_list.html — sekcja 11 Games List
```

### `presentation/components/ConfirmationDialog.kt`
```kotlin
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = stringResource(Res.string.common_confirm),
    dismissLabel: String = stringResource(Res.string.common_cancel),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
)
```

### Commit
```
feat(ui): add feature-level shared components — PlayerListItem, GameListItem, ConfirmationDialog
```

---

## PR 12c — Dice components

**Branch:** `session-12/dice-components` ← `session-12/common-components`

Patrz mockupy: `mockup_03_gameplay_dice_event.html` sekcja 03 — Dice Selection Phase.

Komponenty kości są używane wyłącznie w kontekście rozgrywki — mieszkają w `presentation/components/dice/`.

### `presentation/components/dice/DiceLayout.kt`
Już zdefiniowany w spec — obiekt z `getPositions(value: Int): List<Offset>`.

### `presentation/components/dice/Dice.kt`
Canvas-based rendering. Patrz spec sekcja 6.1 dla pełnej implementacji.
- Parametry: `value`, `isSelected`, `backgroundColor`, `dotColor`, `onClick`, `size`
- Selekcja sygnalizowana zielonym borderem
- Rozmiar domyślny: `50.dp`

### `presentation/components/dice/DiceRow.kt`
```kotlin
@Composable
fun DiceRow(
    selectedValue: Int?,
    diceType: DiceType,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
)
// Row z 6 kośćmi (wartości 1–6)
```

### `presentation/components/dice/EventDice.kt`
```kotlin
@Composable
fun EventDice(
    type: EventDiceType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
// Patrz mockup — kostka zdarzeń z ikoną symbolu
```

### `presentation/components/dice/EventDiceRow.kt`
```kotlin
@Composable
fun EventDiceRow(
    selectedType: EventDiceType?,
    onTypeSelected: (EventDiceType) -> Unit,
    modifier: Modifier = Modifier
)
// Row z 4 kostkami zdarzeń (tylko gdy ekspansja Cities & Knights aktywna)
```

⚠️ `DiceType.backgroundColor` i `DiceType.dotColor` — jeśli są w `domain/enums/DiceType.kt` jako Compose `Color` — przenieś do extension functions w `presentation/components/dice/DiceTypeExtensions.kt`. Compose `Color` nie może być w `domain/`.

### Commit
```
feat(ui): add Canvas-based Dice components
```

---

## PR 12d — Timer & chart components

**Branch:** `session-12/timer-chart-components` ← `session-12/dice-components`

Patrz mockupy: `mockup_03_gameplay_dice_event.html` sekcja 05 Timer Phase, `mockup_04_gameplay_menu_stats.html` sekcja 07 Dice Statistics.

### `presentation/components/timer/GameTimer.kt`
```kotlin
@Composable
fun GameTimer(
    remainingMillis: Long,
    modifier: Modifier = Modifier
)
// Wyświetla MM:SS — displayLarge typography z CatanTypography
// Patrz mockup sekcja 05 — Timer Phase
```

### `presentation/components/timer/TimerControls.kt`
```kotlin
@Composable
fun TimerControls(
    isRunning: Boolean,
    onStartStop: () -> Unit,
    onAddTime: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
)
// Play/Pause icon + "+10 sec" button + Reset icon
```

### `presentation/components/charts/DiceStatisticsChart.kt`
```kotlin
@Composable
fun DiceStatisticsChart(
    distribution: DiceDistribution,
    modifier: Modifier = Modifier
)
// Animowane słupki z label i value pod każdym
// animateFloatAsState dla wysokości słupków
// Klucze osi X: "2", "3", ..., "12"
// Patrz mockup_04_gameplay_menu_stats.html sekcja 07 — Dice Statistics
```

Komponent nazwany `DiceStatisticsChart` (nie `AnimatedBarChart` ani `DiceDistributionChart`) — nazwa opisuje domenowy cel, nie implementację.

### Commit
```
feat(ui): add GameTimer, TimerControls, DiceStatisticsChart components
```

---

## PR 12e — Gameplay components & DI finalizacja

**Branch:** `session-12/gameplay-components-and-di` ← `session-12/timer-chart-components`

Patrz mockupy: `mockup_03_gameplay_dice_event.html` sekcje 04, 05, `mockup_04_gameplay_menu_stats.html` sekcje 06, 07, 08.

### `presentation/components/gameplay/BarbarianTracker.kt`
```kotlin
@Composable
fun BarbarianTracker(
    state: BarbarianState,
    modifier: Modifier = Modifier
)
// Wyświetla pozycję (0–7), liczbę rajdów
// Patrz mockup — widoczny w górnym pasku podczas rozgrywki
```

### `presentation/components/gameplay/EventPhaseContent.kt`
```kotlin
@Composable
fun EventPhaseContent(
    turn: Turn,
    game: Game,
    barbarianState: BarbarianState?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
)
// Wyświetla odpowiedni komunikat w zależności od:
// - diceSum == 7 + Cities&Knights + brak rajdów → robber not active
// - diceSum == 7 → move robber/pirate (zależy od Seafarers)
// - eventDice == BARBARIANS && position == 7 → barbarians arrived
// Patrz mockup_03 sekcja 04 — Event Phase
```

### `presentation/components/gameplay/StatisticsPopup.kt`
```kotlin
@Composable
fun StatisticsPopup(
    distribution: DiceDistribution,
    onDismiss: () -> Unit
)
// Modal/BottomSheet z DiceStatisticsChart
// Patrz mockup_04 sekcja 07 — Dice Statistics
```

### `di/ViewModelModule.kt`
```kotlin
val viewModelModule = module {
    viewModelOf(::DashboardViewModel)
    viewModelOf(::GameConfigViewModel)
    viewModel { params -> GameplayViewModel(params.get(), get(), get(), get()) }
    viewModelOf(::PlayersListViewModel)
    viewModel { params -> PlayerDetailsViewModel(params.get(), get()) }
    viewModelOf(::GamesListViewModel)
    viewModel { params -> GameSummaryViewModel(params.get(), get(), get()) }
}
```

### `di/CatanCompanionModule.kt` — aktualizacja
Dodaj `viewModelModule` do `includes`:
```kotlin
val catanCompanionModule = module {
    includes(
        databaseModule,
        repositoryModule,
        sessionModule,
        useCaseModule,
        viewModelModule
    )
}
```

### Commit
```
feat(ui): add gameplay components (BarbarianTracker, EventPhaseContent, StatisticsPopup), finalize DI
```

---

## Uwagi ogólne dla tej sesji

- Wszystkie stringi przez `stringResource(Res.string.key)` — zero hardkodowanych tekstów
- Kolory, typography i odstępy wyłącznie z `MaterialTheme` / `CatanTimerTheme` tokenów — zero hardkodowanych wartości `Color(...)`, `sp`, `dp` poza `core/designsystem/`
- Compose komponenty: `@Composable`, zero logiki biznesowej, zero `ViewModel` referencji
- `DiceType.backgroundColor` i `DiceType.dotColor` — extension functions w `presentation/`, nie w `domain/`