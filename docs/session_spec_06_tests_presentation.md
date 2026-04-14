# Session Spec 06 — Unit Tests: Presentation

**Sesje planu:** 10  
**Gałąź startowa:** ostatni branch Sesji 9 (`session-9/games-gameplay-viewmodels`)  
**Spec referencyjny:** `catan_companion_implementation_spec_v2.md` sekcja 10  

---

## Kontekst

Testy dla `TimerManager`, `TurnNavigator` i kluczowych ViewModeli. Używaj fake'ów dla repozytoriów i `GameSessionCoordinator`.

---

## PR 10a — Helper tests

**Branch:** `session-10/helper-tests` ← `session-9/games-gameplay-viewmodels`

### `commonTest/.../presentation/TimerManagerTest.kt`

```kotlin
class TimerManagerTest {

    @Test
    fun `initial state has zero remaining and is not running`() = runTest {
        val timer = TimerManager(this)
        assertEquals(0L, timer.state.value.remainingMillis)
        assertFalse(timer.state.value.isRunning)
    }

    @Test
    fun `start sets isRunning to true`() = runTest {
        val timer = TimerManager(this)
        timer.start(60_000L)
        assertTrue(timer.state.value.isRunning)
        assertTrue(timer.state.value.remainingMillis <= 60_000L)
    }

    @Test
    fun `stop cancels timer and returns remaining millis`() = runTest {
        val timer = TimerManager(this)
        timer.start(60_000L)
        val remaining = timer.stop()
        assertFalse(timer.state.value.isRunning)
        assertTrue(remaining >= 0L)
    }

    @Test
    fun `addTime increases remaining millis`() = runTest {
        val timer = TimerManager(this)
        timer.reset(30_000L)
        timer.addTime(10_000L)
        assertEquals(40_000L, timer.state.value.remainingMillis)
    }

    @Test
    fun `reset sets remaining to given duration and stops timer`() = runTest {
        val timer = TimerManager(this)
        timer.start(60_000L)
        timer.reset(120_000L)
        assertEquals(120_000L, timer.state.value.remainingMillis)
        assertFalse(timer.state.value.isRunning)
    }

    @Test
    fun `timer reaches zero and sets isRunning to false`() = runTest {
        val timer = TimerManager(this)
        timer.start(200L)
        advanceTimeBy(500L)
        assertEquals(0L, timer.state.value.remainingMillis)
        assertFalse(timer.state.value.isRunning)
    }
}
```

### `commonTest/.../presentation/TurnNavigatorTest.kt`

```kotlin
class TurnNavigatorTest {

    private val turns = listOf(
        turn(id = 1, number = 0),
        turn(id = 2, number = 1),
        turn(id = 3, number = 2)
    )

    @Test
    fun `default construction selects last turn`() {
        val nav = TurnNavigator(turns)
        assertEquals(turns.last(), nav.selectedTurn)
        assertTrue(nav.isViewingLatest)
    }

    @Test
    fun `selectPrevious moves to previous turn`() {
        val nav = TurnNavigator(turns).selectPrevious()
        assertEquals(turns[1], nav.selectedTurn)
        assertFalse(nav.isViewingLatest)
    }

    @Test
    fun `selectPrevious at first turn is no-op`() {
        val nav = TurnNavigator(turns, selectedIndex = 0).selectPrevious()
        assertEquals(turns[0], nav.selectedTurn)
    }

    @Test
    fun `selectNext at last turn is no-op`() {
        val nav = TurnNavigator(turns).selectNext()
        assertEquals(turns.last(), nav.selectedTurn)
        assertTrue(nav.isViewingLatest)
    }

    @Test
    fun `selectLatest from middle returns to last`() {
        val nav = TurnNavigator(turns, selectedIndex = 0).selectLatest()
        assertEquals(turns.last(), nav.selectedTurn)
        assertTrue(nav.isViewingLatest)
    }

    @Test
    fun `single turn list — all navigation is no-op`() {
        val nav = TurnNavigator(listOf(turn(id = 1, number = 0)))
        assertFalse(nav.hasPrevious)
        assertFalse(nav.hasNext)
        assertTrue(nav.isViewingLatest)
    }

    @Test
    fun `empty list returns null selectedTurn`() {
        val nav = TurnNavigator(emptyList())
        assertNull(nav.selectedTurn)
    }

    private fun turn(id: Long, number: Int) = Turn(
        id = id, gameId = 1L, number = number,
        playerId = 1L, playerName = "Test"
    )
}
```

### Commit
```
test(presentation): add TimerManager and TurnNavigator unit tests
```

---

## PR 10b — ViewModel tests

**Branch:** `session-10/viewmodel-tests` ← `session-10/helper-tests`

### Fake dependencies

Utwórz lub zaktualizuj fake'i w `commonTest/.../fakes/`:

**`FakeGameSessionCoordinator.kt`** — jeśli nie istnieje:
```kotlin
class FakeGameSessionCoordinator : /* implementuje ten sam kontrakt */ {
    private val _session = MutableStateFlow<GameSession?>(null)
    val currentSession: StateFlow<GameSession?> = _session

    fun setSession(session: GameSession?) { _session.value = session }

    suspend fun startSession(gameId: Long): EmptyResult<DataError.Local> = Result.Success(Unit)
    suspend fun completeTurn(durationMillis: Long): Result<Unit, Error> = Result.Success(Unit)
    suspend fun finishSession(finishedAt: Long, winnerId: Long?): EmptyResult<DataError.Local> = Result.Success(Unit)
    suspend fun updateSelectedTurnDice(...): EmptyResult<DataError.Local> = Result.Success(Unit)
    suspend fun updateSelectedTurnDuration(...): EmptyResult<DataError.Local> = Result.Success(Unit)
}
```

### `commonTest/.../viewmodel/DashboardViewModelTest.kt`

```kotlin
class DashboardViewModelTest {

    @Test
    fun `initial state is loading`() = runTest {
        val viewModel = DashboardViewModel(FakeGameRepository())
        // Stan zależy od implementacji — sprawdź initial value
    }

    @Test
    fun `resumable game appears in state when in-progress game exists`() = runTest {
        val repo = FakeGameRepository()
        repo.setInProgressGame(testGame())
        val viewModel = DashboardViewModel(repo)
        val state = viewModel.uiState.value
        assertNotNull(state.resumableGame)
    }

    @Test
    fun `no resumable game when no in-progress games`() = runTest {
        val viewModel = DashboardViewModel(FakeGameRepository())
        assertNull(viewModel.uiState.value.resumableGame)
    }
}
```

### `commonTest/.../viewmodel/GameConfigViewModelTest.kt`

```kotlin
class GameConfigViewModelTest {

    @Test
    fun `isValid false with fewer than 3 players`() = runTest {
        val viewModel = makeViewModel()
        // selectPlayers(2 players)
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `isValid true with 3 players`() = runTest {
        val viewModel = makeViewModel()
        // selectPlayers(3 players)
        assertTrue(viewModel.uiState.value.isValid)
    }

    @Test
    fun `specialTurnRule invalid with fewer than 5 players`() = runTest {
        val viewModel = makeViewModel()
        viewModel.onSpecialTurnRuleToggled()
        // selectPlayers(4 players)
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `onStartGame emits navigation event on success`() = runTest {
        val viewModel = makeViewModel()
        val gameIds = mutableListOf<Long>()
        val job = launch { viewModel.navigateToGameplay.collect { gameIds.add(it) } }
        // selectPlayers(3 players)
        viewModel.onStartGame()
        advanceUntilIdle()
        assertTrue(gameIds.isNotEmpty())
        job.cancel()
    }

    private fun makeViewModel() = GameConfigViewModel(
        playerRepository = FakePlayerRepository(),
        createGameUseCase = CreateGameUseCase(FakeGameRepository())
    )
}
```

### `commonTest/.../viewmodel/GameplayViewModelTest.kt`

```kotlin
class GameplayViewModelTest {

    @Test
    fun `initial phase is DICE_SELECTION`() = runTest {
        val viewModel = makeViewModel()
        assertEquals(GameplayPhase.DICE_SELECTION, viewModel.uiState.value.phase)
    }

    @Test
    fun `onContinueFromDice with sum 7 transitions to EVENT phase`() = runTest {
        val viewModel = makeViewModel()
        viewModel.onDiceSelected(red = 3, yellow = 4, event = null)
        viewModel.onContinueFromDice()
        advanceUntilIdle()
        assertEquals(GameplayPhase.EVENT, viewModel.uiState.value.phase)
    }

    @Test
    fun `onContinueFromDice with sum != 7 transitions to MAIN_TIMER phase`() = runTest {
        val viewModel = makeViewModel()
        viewModel.onDiceSelected(red = 2, yellow = 4, event = null)
        viewModel.onContinueFromDice()
        advanceUntilIdle()
        assertEquals(GameplayPhase.MAIN_TIMER, viewModel.uiState.value.phase)
    }

    @Test
    fun `onNavigateToPreviousTurn updates displayedTurn and isViewingLatest`() = runTest {
        val coordinator = FakeGameSessionCoordinator()
        coordinator.setSession(testSessionWithMultipleTurns())
        val viewModel = makeViewModel(coordinator = coordinator)
        advanceUntilIdle()
        viewModel.onNavigateToPreviousTurn()
        assertFalse(viewModel.uiState.value.isViewingLatest)
    }

    @Test
    fun `onJumpToCurrentTurn restores isViewingLatest`() = runTest {
        val coordinator = FakeGameSessionCoordinator()
        coordinator.setSession(testSessionWithMultipleTurns())
        val viewModel = makeViewModel(coordinator = coordinator)
        advanceUntilIdle()
        viewModel.onNavigateToPreviousTurn()
        viewModel.onJumpToCurrentTurn()
        assertTrue(viewModel.uiState.value.isViewingLatest)
    }

    @Test
    fun `onNextTurn resets phase to DICE_SELECTION`() = runTest {
        val viewModel = makeViewModel()
        viewModel.onDiceSelected(2, 4, null)
        viewModel.onContinueFromDice()
        advanceUntilIdle()
        viewModel.onNextTurn()
        advanceUntilIdle()
        assertEquals(GameplayPhase.DICE_SELECTION, viewModel.uiState.value.phase)
    }

    private fun makeViewModel(
        coordinator: FakeGameSessionCoordinator = FakeGameSessionCoordinator()
    ) = GameplayViewModel(
        gameId = 1L,
        sessionCoordinator = coordinator,
        turnRepository = FakeTurnRepository(),
        gameRepository = FakeGameRepository()
    )
}
```

### `commonTest/.../viewmodel/PlayersListViewModelTest.kt`

```kotlin
class PlayersListViewModelTest {

    @Test
    fun `players list updated from repository`() = runTest {
        val repo = FakePlayerRepository()
        repo.setPlayers(listOf(testPlayer(1L, "Alice"), testPlayer(2L, "Bob")))
        val viewModel = PlayersListViewModel(repo)
        assertEquals(2, viewModel.uiState.value.players.size)
    }

    @Test
    fun `onCreatePlayer calls repository`() = runTest {
        val repo = FakePlayerRepository()
        val viewModel = PlayersListViewModel(repo)
        viewModel.onCreatePlayer("Charlie")
        advanceUntilIdle()
        assertTrue(repo.createdPlayers.contains("Charlie"))
    }
}
```

### `commonTest/.../viewmodel/GameSummaryViewModelTest.kt`

```kotlin
class GameSummaryViewModelTest {

    @Test
    fun `statistics loaded on init`() = runTest {
        val gameRepo = FakeGameRepository()
        gameRepo.setGame(testGame(id = 1L))
        val turnRepo = FakeTurnRepository()
        turnRepo.setTurns(listOf(testTurnWithDice(3, 4)))
        val viewModel = GameSummaryViewModel(
            gameId = 1L,
            gameRepository = gameRepo,
            getGameStatisticsUseCase = GetGameStatisticsUseCase(gameRepo, turnRepo)
        )
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.statistics)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `error state on game not found`() = runTest {
        val viewModel = GameSummaryViewModel(
            gameId = 999L,
            gameRepository = FakeGameRepository(),
            getGameStatisticsUseCase = GetGameStatisticsUseCase(FakeGameRepository(), FakeTurnRepository())
        )
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }
}
```

### Commit
```
test(presentation): add ViewModel unit tests
```

---

## Uwagi ogólne dla tej sesji

- `runTest` + `advanceUntilIdle()` dla testów z coroutines
- `advanceTimeBy(millis)` dla `TimerManager` testów które czekają na upływ czasu
- Helper funkcje (`testGame()`, `testPlayer()`, `testTurn()`) — zdefiniuj w `DataPreparer.kt` lub osobnym pliku `TestFixtures.kt`
- Fake klasy powinny być w `commonTest/.../fakes/` — nie w `commonMain`
- `FakeGameSessionCoordinator` nie musi implementować interfejsu jeśli projekt nie definiuje interfejsu dla Coordinatora — może być po prostu klasą z identyczną sygnaturą publiczną
