# Session Spec 03 — Session, Use Cases & DI

**Sesje planu:** 4, 5, 6  
**Gałąź startowa:** ostatni branch Sesji 3 (`session-3/repository-impls`)  
**Spec referencyjny:** `catan_companion_implementation_spec_v2.md` sekcje 3.4, 3.5, 7  

---

## Kontekst

Ta sesja kończy backend projektu. Po jej zakończeniu cała logika domenowa i data layer jest gotowa i podpięta przez DI. Projekt powinien się kompilować w całości.

---

## PR 4a — TurnFactory & GameSession

**Branch:** `session-4/turn-factory-and-session` ← `session-3/repository-impls`

### `domain/factory/TurnFactory.kt`
Istniejący plik — zaktualizuj do nowego `Game` model (bez `GameConfig`). `TurnFactory` operuje teraz bezpośrednio na `game.players` i `game.specialTurnRuleEnabled`:

```kotlin
object TurnFactory {

    fun createFirst(game: Game): Turn {
        val firstPlayer = game.players.first()
        val secondaryPlayer = if (game.specialTurnRuleEnabled) {
            game.players[3 % game.players.size]
        } else null

        return Turn(
            number = 0,
            gameId = game.id,
            playerId = firstPlayer.playerId,
            playerName = firstPlayer.playerName,
            secondaryPlayerId = secondaryPlayer?.playerId,
            secondaryPlayerName = secondaryPlayer?.playerName
        )
    }

    fun createNext(currentTurn: Turn, game: Game): Turn {
        val nextNumber = currentTurn.number + 1
        val players = game.players
        val playerIndex = nextNumber % players.size
        val nextPlayer = players[playerIndex]
        val secondaryPlayer = if (game.specialTurnRuleEnabled) {
            players[(playerIndex + 3) % players.size]
        } else null

        return Turn(
            number = nextNumber,
            gameId = game.id,
            playerId = nextPlayer.playerId,
            playerName = nextPlayer.playerName,
            secondaryPlayerId = secondaryPlayer?.playerId,
            secondaryPlayerName = secondaryPlayer?.playerName
        )
    }
}
```

### `domain/session/GameSession.kt`
Przenieś z `domain/dataclass/GameSession.kt` do `domain/session/GameSession.kt`. Zaktualizuj model — usuń zależność od `GameConfig`:

```kotlin
data class GameSession(
    val game: Game,
    val latestTurn: Turn,
    val selectedTurn: Turn,
    val recentTurns: List<Turn>
) {
    val isActiveTurnSelected: Boolean
        get() = selectedTurn.id == latestTurn.id
}
```

Usuń stary plik `domain/dataclass/GameSession.kt`.

### Commit
```
refactor(domain): update TurnFactory for new Game model, move GameSession to domain/session
```

---

## PR 4b — GameSessionCoordinator

**Branch:** `session-4/game-session-coordinator` ← `session-4/turn-factory-and-session`

### `domain/session/GameSessionCoordinator.kt`
Przenieś z `domain/GameSessionCoordinator.kt` do `domain/session/GameSessionCoordinator.kt`.

Zaktualizuj do nowych interfejsów repozytorium i nowego `Game` modelu. Kluczowe zmiany:

1. `startSession` — używa `gameRepository.getGameById(gameId)` (Flow) zamiast `getGame`. Collect first value: `.first()` lub `.firstOrNull()`
2. `startSession` — `turnRepository.getTurnsForGame(gameId)` zwraca teraz Flow — zbierz jednorazowo przez `.first()`  
3. `completeTurn` — wywołuje `TurnFactory.createNext(completedTurn, session.game)` zamiast `TurnFactory.createNext(completedTurn, session.game.config)`
4. `finishSession` — wywołuje `gameRepository.endGame(gameId, winnerId = null)` — winnerId przekazywany z WinnerSelectionScreen zostanie dodany później
5. Usuń metody `selectTurn` i `selectActiveTurn` — nawigacja historii przeniesiona do `TurnNavigator` w warstwie prezentacji

Zachowana sygnatura publiczna:
```kotlin
class GameSessionCoordinator(
    private val gameRepository: GameRepository,
    private val turnRepository: TurnRepository
) {
    val currentSession: StateFlow<GameSession?>

    suspend fun startSession(gameId: Long): EmptyResult<DataError.Local>
    suspend fun finishSession(finishedAt: Long, winnerId: Long?): EmptyResult<DataError.Local>
    suspend fun completeTurn(durationMillis: Long): Result<Unit, Error>
    suspend fun updateSelectedTurnDice(
        redDice: Int?,
        yellowDice: Int?,
        eventDice: EventDiceType?
    ): EmptyResult<DataError.Local>
    suspend fun updateSelectedTurnDuration(durationMillis: Long): EmptyResult<DataError.Local>
}
```

⚠️ `RECENT_TURNS_LIMIT` zostaje jako `private const val` w companion object.

### Commit
```
refactor(session): move and update GameSessionCoordinator, remove turn navigation methods
```

---

## PR 5a — Use Cases

**Branch:** `session-5/use-cases` ← `session-4/game-session-coordinator`

### `domain/usecase/CreateGameUseCase.kt`
Walidacja biznesowa przed delegowaniem do repozytorium:

```kotlin
class CreateGameUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        playerIds: List<Long>
    ): Result<Long, IllegalOperationError> {
        if (playerIds.size < 3 || playerIds.size > 6) {
            return Result.Failure(IllegalOperationError)
        }
        if (playerIds.distinct().size != playerIds.size) {
            return Result.Failure(IllegalOperationError)
        }
        if (specialTurnRuleEnabled && playerIds.size < 5) {
            return Result.Failure(IllegalOperationError)
        }
        return try {
            val gameId = gameRepository.createGame(
                turnDurationMillis = turnDurationMillis,
                expansions = expansions,
                specialTurnRuleEnabled = specialTurnRuleEnabled,
                playerIds = playerIds
            )
            Result.Success(gameId)
        } catch (e: Exception) {
            Result.Failure(IllegalOperationError)
        }
    }
}
```

### `domain/usecase/GetGameStatisticsUseCase.kt`
Agreguje dane z repozytorium — całe obliczenia w czystym Kotlinie:

```kotlin
class GetGameStatisticsUseCase(
    private val gameRepository: GameRepository,
    private val turnRepository: TurnRepository
) {
    suspend operator fun invoke(gameId: Long): Result<GameStatistics, DataError.Local> {
        val game = gameRepository.getGameById(gameId).firstOrNull()
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        val turns = turnRepository.getTurnsForGame(gameId).first()

        val completedTurns = turns.filter { it.durationMillis > 0 }

        val avgDuration = if (completedTurns.isEmpty()) 0L
            else completedTurns.sumOf { it.durationMillis } / completedTurns.size

        val diceCounts = turns
            .mapNotNull { it.diceSum }
            .groupingBy { it }
            .eachCount()

        val eventCounts = turns
            .mapNotNull { it.eventDice }
            .groupingBy { it }
            .eachCount()

        val playerAvgDurations = game.players.associate { gamePlayer ->
            val playerTurns = completedTurns.filter { it.playerId == gamePlayer.playerId }
            val avg = if (playerTurns.isEmpty()) 0L
                else playerTurns.sumOf { it.durationMillis } / playerTurns.size
            gamePlayer.playerId to avg
        }

        return Result.Success(
            GameStatistics(
                totalTurns = turns.size,
                averageTurnDurationMillis = avgDuration,
                diceDistribution = DiceDistribution(diceCounts),
                eventDiceDistribution = eventCounts,
                playerAverageTurnDurations = playerAvgDurations
            )
        )
    }
}
```

### Commit
```
feat(domain): implement CreateGameUseCase and GetGameStatisticsUseCase
```

---

## PR 6a — Core DI modules

**Branch:** `session-6/di-core-modules` ← `session-5/use-cases`

Przepisz istniejące moduły Koin na nową strukturę. Usuń stary `Modules.kt` / `catanCompanionModule` jeśli był jednym flat plikiem.

### `di/DatabaseModule.kt`
```kotlin
val databaseModule = module {
    single { get<CatanCompanionDatabase>().playerDao() }
    single { get<CatanCompanionDatabase>().gameDao() }
    single { get<CatanCompanionDatabase>().gamePlayerDao() }
    single { get<CatanCompanionDatabase>().turnDao() }
}
// Uwaga: sama instancja CatanCompanionDatabase jest rejestrowana
// w platformowych Modules.android.kt / .ios.kt / .desktop.kt przez DatabaseFactory
```

### `di/RepositoryModule.kt`
```kotlin
val repositoryModule = module {
    single<PlayerRepository> { PlayerRepositoryImpl(get()) }
    single<GameRepository> { GameRepositoryImpl(get(), get(), get()) }
    single<TurnRepository> { TurnRepositoryImpl(get(), get()) }
}
```

### `di/SessionModule.kt`
```kotlin
val sessionModule = module {
    // Singleton — jedna aktywna sesja na cały cykl życia aplikacji
    single { GameSessionCoordinator(get(), get()) }
}
```

### `di/UseCaseModule.kt`
```kotlin
val useCaseModule = module {
    factory { CreateGameUseCase(get()) }
    factory { GetGameStatisticsUseCase(get(), get()) }
}
```

### Commit
```
feat(di): add DatabaseModule, RepositoryModule, SessionModule, UseCaseModule
```

---

## PR 6b — DI wiring & platform modules

**Branch:** `session-6/di-wiring` ← `session-6/di-core-modules`

### `di/CatanCompanionModule.kt`
Główny moduł agregujący — ViewModelModule zostanie dodany w Sesji 12:
```kotlin
val catanCompanionModule = module {
    includes(
        databaseModule,
        repositoryModule,
        sessionModule,
        useCaseModule
    )
}
```

### Platformowe moduły — aktualizacja
Zaktualizuj `androidMain/.../di/Modules.android.kt`, `iosMain/.../di/Modules.ios.kt`, `desktopMain/.../di/Modules.desktop.kt`:
- Zmień referencję z `CatanTimerDatabase` na `CatanCompanionDatabase`
- Upewnij się że `catanCompanionModule` jest includowany w `startKoin`
- Dodaj `.addMigrations(MIGRATION_1_2)` do buildera bazy jeśli jeszcze nie ma

### `initKoin.kt` — weryfikacja
Sprawdź czy `initKoin` ładuje `catanCompanionModule`. Jeśli nie — dodaj.

### Commit
```
feat(di): wire CatanCompanionModule, update platform DI modules
```

---

## Uwagi ogólne dla tej sesji

- Po PR 6b projekt powinien się **w pełni kompilować** — zweryfikuj przed commitem
- `GameConfig` klasa domenowa nie powinna już istnieć w żadnym pliku po PR 4a — upewnij się że wszystkie importy zostały usunięte
- Stary `domain/dataclass/` folder powinien być pusty lub usunięty po przeniesieniu klas w PR 4a
- Nie twórz `ViewModelModule` — to Sesja 12
- Koin `single` dla repozytoriów zapewnia że jest jeden egzemplarz w całej aplikacji — to ważne dla `GameSessionCoordinator` który trzyma stan
