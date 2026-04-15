# Session Spec 05 — Presentation: Helpers, States & ViewModels

**Sesje planu:** 8, 9  
**Gałąź startowa:** ostatni branch Sesji 7 (`session-7/data-tests`)  
**Spec referencyjny:** `catan_companion_implementation_spec_v2.md` sekcje 2.2, 5.1, 5.2, 5.4

---

## Kontekst

Implementacja warstwy prezentacji — bez UI. Po tej sesji wszystkie ViewModele istnieją i są gotowe do podpięcia ekranów. DI zostanie finalizowane w Sesji 12 (ViewModelModule).

---

## PR 8a — Presentation helpers

**Branch:** `session-8/presentation-helpers` ← `session-7/data-tests`

### `presentation/timer/TimerManager.kt`

`TimerManager` zarządza timerem coroutine. Tworzony bezpośrednio w `init` ViewModelu — nie wstrzykiwany przez Koin.

```kotlin
class TimerManager(private val scope: CoroutineScope) {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun start(fromMillis: Long) {
        timerJob?.cancel()
        _state.update { it.copy(remainingMillis = fromMillis, isRunning = true) }
        timerJob = scope.launch {
            var remaining = fromMillis
            while (remaining > 0) {
                delay(100L)
                remaining = (remaining - 100L).coerceAtLeast(0L)
                _state.update { it.copy(remainingMillis = remaining) }
            }
            _state.update { it.copy(isRunning = false) }
            // Timer reached zero — ViewModel obserwuje state i triggeruje HapticService
        }
    }

    fun stop(): Long {
        timerJob?.cancel()
        timerJob = null
        val elapsed = _state.value.remainingMillis
        _state.update { it.copy(isRunning = false) }
        return elapsed
    }

    fun addTime(millis: Long) {
        _state.update { it.copy(remainingMillis = it.remainingMillis + millis) }
    }

    fun reset(durationMillis: Long) {
        timerJob?.cancel()
        timerJob = null
        _state.update { TimerState(remainingMillis = durationMillis, isRunning = false) }
    }
}
```

### `presentation/util/TurnNavigator.kt`

Czysta klasa — zero I/O, zero coroutines. Immutable — każda operacja zwraca nową instancję.

```kotlin
class TurnNavigator(
    private val turns: List<Turn>,
    private val selectedIndex: Int = turns.lastIndex.coerceAtLeast(0)
) {
    val selectedTurn: Turn? get() = turns.getOrNull(selectedIndex)
    val isViewingLatest: Boolean get() = selectedIndex == turns.lastIndex
    val hasPrevious: Boolean get() = selectedIndex > 0
    val hasNext: Boolean get() = selectedIndex < turns.lastIndex

    fun selectPrevious(): TurnNavigator {
        if (!hasPrevious) return this
        return TurnNavigator(turns, selectedIndex - 1)
    }

    fun selectNext(): TurnNavigator {
        if (!hasNext) return this
        return TurnNavigator(turns, selectedIndex + 1)
    }

    fun selectLatest(): TurnNavigator {
        return TurnNavigator(turns, turns.lastIndex.coerceAtLeast(0))
    }

    companion object {
        fun from(session: GameSession): TurnNavigator {
            val allTurns = session.recentTurns + session.latestTurn
            return TurnNavigator(allTurns)
        }
    }
}
```

### Commit
```
feat(presentation): add TimerManager and TurnNavigator helpers
```

---

## PR 8b — UI States

**Branch:** `session-8/ui-states` ← `session-8/presentation-helpers`

UI state klasy są umieszczone obok ViewModeli i Screen — każda w pakiecie swojego widoku. Nie istnieje wspólny pakiet `presentation/state/`.

### `presentation/gameplay/GameplayUiState.kt`
```kotlin
data class GameplayUiState(
    val game: Game? = null,
    val currentTurn: Turn? = null,
    val displayedTurn: Turn? = null,
    val isViewingLatest: Boolean = true,
    val phase: GameplayPhase = GameplayPhase.DICE_SELECTION,
    val timerState: TimerState = TimerState(),
    val barbarianState: BarbarianState? = null,
    val diceDistribution: DiceDistribution? = null,
    val isEditing: Boolean = false,
    val pendingDiceEdit: DiceRoll? = null,
    val showStatisticsPopup: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class GameplayPhase {
    DICE_SELECTION,
    EVENT,
    MAIN_TIMER,
    IN_BETWEEN_TIMER
}

data class TimerState(
    val remainingMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isForSecondaryPlayer: Boolean = false
)
```

### `presentation/dashboard/DashboardUiState.kt`
```kotlin
data class DashboardUiState(
    val resumableGame: Game? = null,
    val isLoading: Boolean = false
)
```

### `presentation/gameconfig/GameConfigUiState.kt`
```kotlin
data class GameConfigUiState(
    val turnDurationMillis: Long = 120_000L,
    val selectedPlayers: List<Player> = emptyList(),
    val availablePlayers: List<Player> = emptyList(),
    val expansions: Set<GameExpansion> = emptySet(),
    val specialTurnRuleEnabled: Boolean = false,
    val isValid: Boolean = false,
    val validationError: String? = null,
    val isLoading: Boolean = false
)
```

### `presentation/players/PlayersListUiState.kt`
```kotlin
data class PlayersListUiState(
    val players: List<Player> = emptyList(),
    val isSelectionMode: Boolean = false,
    val isLoading: Boolean = false
)
```

### `presentation/playerdetails/PlayerDetailsUiState.kt`
```kotlin
data class PlayerDetailsUiState(
    val player: Player? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### `presentation/gameslist/GamesListUiState.kt`
```kotlin
data class GamesListUiState(
    val inProgressGames: List<Game> = emptyList(),
    val completedGames: List<Game> = emptyList(),
    val isLoading: Boolean = false
)
```

### `presentation/gamesummary/GameSummaryUiState.kt`
```kotlin
data class GameSummaryUiState(
    val game: Game? = null,
    val statistics: GameStatistics? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Commit
```
feat(presentation): add all UI state classes and GameplayPhase enum
```

---

## PR 9a — Navigation

**Branch:** `session-9/navigation` ← `session-8/ui-states`

### `presentation/navigation/CatanCompanionNavigation.kt`

```kotlin
sealed class CatanCompanionRoute(val route: String) {
    object Dashboard : CatanCompanionRoute("dashboard")
    object GameConfig : CatanCompanionRoute("game_config")
    object Gameplay : CatanCompanionRoute("gameplay/{gameId}") {
        fun createRoute(gameId: Long) = "gameplay/$gameId"
    }
    object PlayersList : CatanCompanionRoute("players_list?selectionMode={selectionMode}") {
        fun createRoute(selectionMode: Boolean = false) = "players_list?selectionMode=$selectionMode"
    }
    object PlayerDetails : CatanCompanionRoute("player_details/{playerId}") {
        fun createRoute(playerId: Long) = "player_details/$playerId"
    }
    object GamesList : CatanCompanionRoute("games_list")
    object GameSummary : CatanCompanionRoute("game_summary/{gameId}") {
        fun createRoute(gameId: Long) = "game_summary/$gameId"
    }
    object WinnerSelection : CatanCompanionRoute("winner_selection/{gameId}") {
        fun createRoute(gameId: Long) = "winner_selection/$gameId"
    }
}
```

### Commit
```
feat(presentation): add navigation routes
```

---

## PR 9b — Dashboard, Config & Players ViewModels

**Branch:** `session-9/dashboard-config-players-viewmodels` ← `session-9/navigation`

### `presentation/dashboard/DashboardViewModel.kt`
```kotlin
class DashboardViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeResumableGame()
    }

    private fun observeResumableGame() {
        gameRepository.getMostRecentInProgressGame()
            .onEach { game ->
                _uiState.update { it.copy(resumableGame = game, isLoading = false) }
            }
            .catch { _uiState.update { it.copy(isLoading = false) } }
            .launchIn(viewModelScope)
    }
}
```

### `presentation/gameconfig/GameConfigViewModel.kt`
```kotlin
class GameConfigViewModel(
    private val playerRepository: PlayerRepository,
    private val createGameUseCase: CreateGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameConfigUiState())
    val uiState: StateFlow<GameConfigUiState> = _uiState.asStateFlow()

    private val _navigateToGameplay = MutableSharedFlow<Long>()
    val navigateToGameplay: SharedFlow<Long> = _navigateToGameplay.asSharedFlow()

    init {
        loadAvailablePlayers()
    }

    private fun loadAvailablePlayers() {
        playerRepository.getVisiblePlayers()
            .onEach { players ->
                _uiState.update { it.copy(availablePlayers = players) }
            }
            .launchIn(viewModelScope)
    }

    fun onPlayerToggled(player: Player) { /* add or remove from selectedPlayers, revalidate */ }
    fun onExpansionToggled(expansion: GameExpansion) { /* toggle in set, revalidate */ }
    fun onSpecialTurnRuleToggled() { /* toggle boolean, revalidate */ }
    fun onTurnDurationChanged(millis: Long) { /* update duration, revalidate */ }

    fun onStartGame() {
        viewModelScope.launch {
            val state = _uiState.value
            val result = createGameUseCase(
                turnDurationMillis = state.turnDurationMillis,
                expansions = state.expansions,
                specialTurnRuleEnabled = state.specialTurnRuleEnabled,
                playerIds = state.selectedPlayers.map { it.id }
            )
            when (result) {
                is Result.Success -> _navigateToGameplay.emit(result.data)
                is Result.Failure -> _uiState.update {
                    it.copy(validationError = "Invalid game configuration")
                }
            }
        }
    }

    private fun revalidate() {
        val state = _uiState.value
        val valid = state.selectedPlayers.size in 3..6 &&
            (!state.specialTurnRuleEnabled || state.selectedPlayers.size >= 5)
        _uiState.update { it.copy(isValid = valid, validationError = if (valid) null else it.validationError) }
    }
}
```

### `presentation/players/PlayersListViewModel.kt`
```kotlin
class PlayersListViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayersListUiState())
    val uiState: StateFlow<PlayersListUiState> = _uiState.asStateFlow()

    init {
        playerRepository.getVisiblePlayers()
            .onEach { players -> _uiState.update { it.copy(players = players) } }
            .launchIn(viewModelScope)
    }

    fun onCreatePlayer(name: String) {
        viewModelScope.launch { playerRepository.createPlayer(name) }
    }

    fun onHidePlayer(player: Player) {
        viewModelScope.launch { playerRepository.hidePlayer(player.id) }
    }

    fun onDeletePlayer(player: Player) {
        viewModelScope.launch {
            if (playerRepository.canDeletePlayer(player.id)) {
                playerRepository.deletePlayer(player.id)
            }
        }
    }
}
```

### `presentation/playerdetails/PlayerDetailsViewModel.kt`
```kotlin
class PlayerDetailsViewModel(
    private val playerId: Long,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerDetailsUiState())
    val uiState: StateFlow<PlayerDetailsUiState> = _uiState.asStateFlow()

    init {
        playerRepository.getPlayerById(playerId)
            .onEach { player -> _uiState.update { it.copy(player = player) } }
            .launchIn(viewModelScope)
    }

    fun onUpdateName(name: String) {
        viewModelScope.launch {
            _uiState.value.player?.let { playerRepository.updatePlayer(it.copy(name = name)) }
        }
    }

    fun onHidePlayer() {
        viewModelScope.launch { playerRepository.hidePlayer(playerId) }
    }

    fun onDeletePlayer() {
        viewModelScope.launch { playerRepository.deletePlayer(playerId) }
    }
}
```

### Commit
```
feat(presentation): add Dashboard, GameConfig, PlayersList, PlayerDetails ViewModels
```

---

## PR 9c — Games & Gameplay ViewModels

**Branch:** `session-9/games-gameplay-viewmodels` ← `session-9/dashboard-config-players-viewmodels`

### `presentation/gameslist/GamesListViewModel.kt`
```kotlin
class GamesListViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesListUiState())
    val uiState: StateFlow<GamesListUiState> = _uiState.asStateFlow()

    init {
        combine(
            gameRepository.getInProgressGames(),
            gameRepository.getCompletedGames()
        ) { inProgress, completed ->
            GamesListUiState(inProgressGames = inProgress, completedGames = completed)
        }
            .onEach { state -> _uiState.value = state }
            .launchIn(viewModelScope)
    }

    fun onDeleteGame(game: Game) {
        viewModelScope.launch { gameRepository.deleteGame(game.id) }
    }
}
```

### `presentation/gamesummary/GameSummaryViewModel.kt`
```kotlin
class GameSummaryViewModel(
    private val gameId: Long,
    private val gameRepository: GameRepository,
    private val getGameStatisticsUseCase: GetGameStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameSummaryUiState(isLoading = true))
    val uiState: StateFlow<GameSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val statsResult = getGameStatisticsUseCase(gameId)
            gameRepository.getGameById(gameId).firstOrNull()?.let { game ->
                _uiState.update { it.copy(game = game) }
            }
            when (statsResult) {
                is Result.Success -> _uiState.update {
                    it.copy(statistics = statsResult.data, isLoading = false)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(error = "Failed to load statistics", isLoading = false)
                }
            }
        }
    }
}
```

### `presentation/gameplay/GameplayViewModel.kt`

Najbardziej złożony ViewModel. `TimerManager` i `TurnNavigator` tworzone w `init`.

```kotlin
class GameplayViewModel(
    private val gameId: Long,
    private val sessionCoordinator: GameSessionCoordinator,
    private val turnRepository: TurnRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val timerManager = TimerManager(viewModelScope)
    private val _navigator = MutableStateFlow<TurnNavigator?>(null)

    private val _uiState = MutableStateFlow(GameplayUiState(isLoading = true))
    val uiState: StateFlow<GameplayUiState> = _uiState.asStateFlow()

    init {
        startSession()
        observeSession()
        observeTimer()
        observeTurnsForLiveStats()
    }

    private fun startSession() {
        viewModelScope.launch {
            sessionCoordinator.startSession(gameId)
        }
    }

    private fun observeSession() {
        sessionCoordinator.currentSession
            .filterNotNull()
            .onEach { session ->
                val navigator = TurnNavigator.from(session)
                _navigator.value = navigator
                _uiState.update {
                    it.copy(
                        game = session.game,
                        currentTurn = session.latestTurn,
                        displayedTurn = navigator.selectedTurn,
                        isViewingLatest = navigator.isViewingLatest,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTimer() {
        timerManager.state
            .onEach { timerState ->
                _uiState.update { it.copy(timerState = timerState) }
                // Gdy timer dobiegnie 0 i był uruchomiony → trigger haptic (Sesja 16)
            }
            .launchIn(viewModelScope)
    }

    private fun observeTurnsForLiveStats() {
        turnRepository.getTurnsForGame(gameId)
            .onEach { turns ->
                _uiState.update {
                    it.copy(
                        barbarianState = turns.toBarbarianState(),
                        diceDistribution = DiceDistribution(
                            turns.mapNotNull { t -> t.diceSum }
                                .groupingBy { it }.eachCount()
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onDiceSelected(red: Int, yellow: Int, event: EventDiceType?) {
        _uiState.update { it.copy(pendingDiceEdit = DiceRoll(red, yellow, event)) }
    }

    fun onContinueFromDice() {
        val dice = _uiState.value.pendingDiceEdit ?: return
        viewModelScope.launch {
            sessionCoordinator.updateSelectedTurnDice(dice.red, dice.yellow, dice.event)
            val phase = if (dice.sum == 7) GameplayPhase.EVENT else GameplayPhase.MAIN_TIMER
            if (phase == GameplayPhase.MAIN_TIMER) {
                timerManager.reset(_uiState.value.game?.turnDurationMillis ?: 120_000L)
            }
            _uiState.update { it.copy(phase = phase, pendingDiceEdit = null) }
        }
    }

    fun onContinueFromEvent() {
        timerManager.reset(_uiState.value.game?.turnDurationMillis ?: 120_000L)
        _uiState.update { it.copy(phase = GameplayPhase.MAIN_TIMER) }
    }

    fun onStartTimer() { timerManager.start(_uiState.value.timerState.remainingMillis) }
    fun onStopTimer() { timerManager.stop() }
    fun onAddTime() { timerManager.addTime(10_000L) }
    fun onResetTimer() {
        timerManager.reset(_uiState.value.game?.turnDurationMillis ?: 120_000L)
    }

    fun onNextTurn() {
        viewModelScope.launch {
            val elapsed = timerManager.stop()
            sessionCoordinator.completeTurn(elapsed)
            _uiState.update { it.copy(phase = GameplayPhase.DICE_SELECTION, pendingDiceEdit = null) }
        }
    }

    fun onNavigateToPreviousTurn() {
        val nav = _navigator.value?.selectPrevious() ?: return
        _navigator.value = nav
        _uiState.update { it.copy(displayedTurn = nav.selectedTurn, isViewingLatest = nav.isViewingLatest) }
    }

    fun onNavigateToNextTurn() {
        val nav = _navigator.value?.selectNext() ?: return
        _navigator.value = nav
        _uiState.update { it.copy(displayedTurn = nav.selectedTurn, isViewingLatest = nav.isViewingLatest) }
    }

    fun onJumpToCurrentTurn() {
        val nav = _navigator.value?.selectLatest() ?: return
        _navigator.value = nav
        _uiState.update { it.copy(displayedTurn = nav.selectedTurn, isViewingLatest = true) }
    }

    fun onSaveEdit() {
        val dice = _uiState.value.pendingDiceEdit ?: return
        viewModelScope.launch {
            sessionCoordinator.updateSelectedTurnDice(dice.red, dice.yellow, dice.event)
            _uiState.update { it.copy(isEditing = false, pendingDiceEdit = null) }
        }
    }

    fun onCancelEdit() {
        _uiState.update { it.copy(isEditing = false, pendingDiceEdit = null) }
    }

    fun onUpdateGameSettings(expansions: Set<GameExpansion>, specialTurnRuleEnabled: Boolean) {
        viewModelScope.launch {
            gameRepository.updateGameSettings(gameId, expansions, specialTurnRuleEnabled)
            _uiState.update { it.copy(showSettingsSheet = false) }
        }
    }

    fun onFinishSession(winnerId: Long?) {
        viewModelScope.launch {
            sessionCoordinator.finishSession(
                finishedAt = System.currentTimeMillis(),
                winnerId = winnerId
            )
        }
    }
}
```

### Commit
```
feat(presentation): add GamesListViewModel, GameSummaryViewModel, GameplayViewModel
```

---

## Uwagi ogólne dla tej sesji

- `SharedFlow` dla jednorazowych navigation events (np. `navigateToGameplay` w `GameConfigViewModel`) — użyj `MutableSharedFlow(replay = 0)`
- ViewModele nie mają jeszcze bindingów Koin — `ViewModelModule` jest w Sesji 12
- `System.currentTimeMillis()` w `onFinishSession` — akceptowalne dla MVP; nie wprowadzaj abstrakcji clock bez powodu
- `firstOrNull()` na Flow — bezpieczniejsze niż `first()` które rzuca jeśli Flow jest pusty
- Nie importuj niczego z `android.*` — wszystkie ViewModele w `commonMain`