# Session Spec 04 — Unit Tests: Domain & Data

**Sesje planu:** 7  
**Gałąź startowa:** ostatni branch Sesji 6 (`session-6/di-wiring`)  
**Spec referencyjny:** `catan_companion_implementation_spec_v2.md` sekcja 10  

---

## Kontekst

Testy jednostkowe dla warstw domenowej i danych. Projekt ma już istniejące testy — zaktualizuj je do nowych interfejsów zamiast pisać od zera. Wzorzec fake DAO jest już ustalony w projekcie — trzymaj się go.

Wszystkie testy trafiają do `commonTest` — zero importów platformowych.

---

## PR 7a — Use case tests

**Branch:** `session-7/usecase-tests` ← `session-6/di-wiring`

### `commonTest/.../usecase/CreateGameUseCaseTest.kt`

```kotlin
class CreateGameUseCaseTest {
    private val fakeGameRepository = FakeGameRepository()
    private val useCase = CreateGameUseCase(fakeGameRepository)

    @Test
    fun `valid input returns success with game id`()

    @Test
    fun `less than 3 players returns failure`()

    @Test
    fun `more than 6 players returns failure`()

    @Test
    fun `duplicate player ids returns failure`()

    @Test
    fun `specialTurnRule enabled with 4 players returns failure`()

    @Test
    fun `specialTurnRule enabled with 5 players returns success`()

    @Test
    fun `exactly 3 players returns success`()

    @Test
    fun `exactly 6 players returns success`()
}
```

### `commonTest/.../usecase/GetGameStatisticsUseCaseTest.kt`

```kotlin
class GetGameStatisticsUseCaseTest {
    private val fakeGameRepository = FakeGameRepository()
    private val fakeTurnRepository = FakeTurnRepository()
    private val useCase = GetGameStatisticsUseCase(fakeGameRepository, fakeTurnRepository)

    @Test
    fun `empty turn list returns zeroed statistics without crash`()

    @Test
    fun `dice distribution groups correctly by sum`()
    // Turns z (red=3,yellow=4), (red=5,yellow=2), (red=3,yellow=4)
    // → counts[7] = 2, counts[7] = 2 ← oba to 7

    @Test
    fun `average duration calculated correctly`()
    // 3 tury z durationMillis: 60000, 90000, 120000 → avg = 90000

    @Test
    fun `turns without dice rolls excluded from distribution`()
    // Turn z redDice=null nie trafia do diceDistribution

    @Test
    fun `game not found returns failure`()

    @Test
    fun `player average durations calculated per player`()
}
```

### Commit
```
test(usecase): add CreateGameUseCase and GetGameStatisticsUseCase tests
```

---

## PR 7b — Domain helper & session tests

**Branch:** `session-7/domain-helper-tests` ← `session-7/usecase-tests`

### `commonTest/.../domain/BarbarianStateTest.kt`
Nowy plik — testuje `List<Turn>.toBarbarianState()`:

```kotlin
class BarbarianStateTest {

    @Test
    fun `zero barbarian rolls returns position 0 no raids`() {
        val turns = listOf(turnWithEvent(EventDiceType.TRADE), turnWithEvent(EventDiceType.POLITICS))
        val state = turns.toBarbarianState()
        assertEquals(0, state.position)
        assertEquals(0, state.raidsCompleted)
        assertFalse(state.hasFirstRaidOccurred)
    }

    @Test
    fun `7 barbarian rolls returns position 7 no raids`() {
        val turns = barbarianTurns(7)
        val state = turns.toBarbarianState()
        assertEquals(7, state.position)
        assertEquals(0, state.raidsCompleted)
        assertFalse(state.hasFirstRaidOccurred)
    }

    @Test
    fun `8 barbarian rolls resets to position 0 one raid completed`() {
        val turns = barbarianTurns(8)
        val state = turns.toBarbarianState()
        assertEquals(0, state.position)
        assertEquals(1, state.raidsCompleted)
        assertTrue(state.hasFirstRaidOccurred)
    }

    @Test
    fun `15 barbarian rolls returns position 7 one raid completed`() {
        val turns = barbarianTurns(15)
        val state = turns.toBarbarianState()
        assertEquals(7, state.position)
        assertEquals(1, state.raidsCompleted)
        assertTrue(state.hasFirstRaidOccurred)
    }

    @Test
    fun `16 barbarian rolls resets to position 0 two raids completed`() {
        val turns = barbarianTurns(16)
        val state = turns.toBarbarianState()
        assertEquals(0, state.position)
        assertEquals(2, state.raidsCompleted)
        assertTrue(state.hasFirstRaidOccurred)
    }

    @Test
    fun `empty list returns default state`() {
        val state = emptyList<Turn>().toBarbarianState()
        assertEquals(0, state.position)
        assertEquals(0, state.raidsCompleted)
        assertFalse(state.hasFirstRaidOccurred)
    }

    private fun barbarianTurns(count: Int) =
        (1..count).map { turnWithEvent(EventDiceType.BARBARIANS) }

    private fun turnWithEvent(event: EventDiceType?) = Turn(
        id = 0L, gameId = 1L, number = 0,
        playerId = 1L, playerName = "Test",
        eventDice = event
    )
}
```

### `commonTest/.../domain/GameSessionCoordinatorTest.kt`
Istniejący test — zaktualizuj do nowych interfejsów:
- `startSession` używa teraz `gameRepository.getGameById` (Flow) zamiast `getGame`
- `completeTurn` używa `TurnFactory.createNext(turn, game)` zamiast `TurnFactory.createNext(turn, game.config)`
- Usuń testy dla `selectTurn`/`selectActiveTurn` — te metody zostały usunięte
- Dodaj test: `finishSession` przyjmuje `winnerId: Long?`

### Commit
```
test(domain): add BarbarianStateTest, update GameSessionCoordinatorTest
```

---

## PR 7c — Data mapper & repository tests

**Branch:** `session-7/data-tests` ← `session-7/domain-helper-tests`

### Fake DAO aktualizacja
Zanim napiszesz testy — upewnij się że fake DAO w `commonTest/` implementują zaktualizowane interfejsy DAO. Jeśli jeszcze nie zostały zaktualizowane po zmianach w Sesji 3:
- `FakeGameDao` — dodaj `getInProgress()`, `getCompleted()`, `getMostRecentInProgress()` jako Flow
- `FakeTurnDao` — zmień `getTurnsForGame` na Flow-returning
- Dodaj `FakeGamePlayerDao` jeśli brakuje

### `commonTest/.../data/mapper/PlayerMappersTest.kt`
```kotlin
class PlayerMappersTest {
    @Test
    fun `PlayerEntity toDomain maps all fields correctly`()

    @Test
    fun `PlayerEntity toDomain with default gamesPlayed and gamesWon`()

    @Test
    fun `Player toEntity drops derived fields`()
    // gamesPlayed i gamesWon nie trafiają do encji
}
```

### `commonTest/.../data/mapper/TurnMappersTest.kt`
```kotlin
class TurnMappersTest {
    @Test
    fun `TurnEntity toDomain maps all fields including playerName`()

    @Test
    fun `TurnEntity toDomain with null secondaryPlayerId`()

    @Test
    fun `Turn toEntity drops playerName and secondaryPlayerName`()
    // Nazwy graczy nie są przechowywane w encji
}
```

### `commonTest/.../data/mapper/GameMappersTest.kt`
```kotlin
class GameMappersTest {
    @Test
    fun `GameEntity toDomain maps all fields`()

    @Test
    fun `GameEntity toDomain accepts empty players list`()

    @Test
    fun `Game toEntity drops players list`()
}
```

### Repozytoria — aktualizacja istniejących testów
Zaktualizuj `GameRepositoryImplTest`, `PlayerRepositoryImplTest`, `TurnRepositoryImplTest` do nowych interfejsów. Upewnij się że testy pokrywają:

**PlayerRepositoryImplTest:**
- `getAllPlayers()` emituje Flow z listą
- `canDeletePlayer` zwraca `false` gdy gracz ma rozgrywki
- `hidePlayer` ustawia `isHidden = true`

**GameRepositoryImplTest:**
- `createGame` tworzy `GameEntity` + `GamePlayerEntity` w transakcji
- `endGame` ustawia `status = COMPLETED` i `winnerId`
- `getInProgressGames()` zwraca tylko gry `IN_PROGRESS`

**TurnRepositoryImplTest:**
- `getTurnsForGame` zwraca Flow
- `updateDiceRoll` aktualizuje tylko pola dice
- `updateDuration` aktualizuje tylko `durationMillis`

### Commit
```
test(data): update mapper tests and repository tests to new interfaces
```

---

## Uwagi ogólne dla tej sesji

- Używaj `runTest` z `kotlinx-coroutines-test` dla wszystkich suspend funkcji i Flow
- `turbine` library do testowania Flow — sprawdź czy jest już w `libs.versions.toml`; jeśli nie — zapytaj zanim dodasz
- Fake repozytoria w `commonTest/fakes/` — trzymaj się istniejącego wzorca projektu
- Nie testuj integracji Room (to `androidUnitTest`) — tylko logikę przez fake DAO
- `DataPreparer.kt` w `commonTest` — sprawdź co jest zdefiniowane i użyj istniejących helper funkcji
