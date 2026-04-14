# Session Spec 01 — Domain: Enums & Models

**Sesje planu:** 1, 2  
**Gałąź startowa:** `main`  
**Spec referencyjny:** `catan_companion_implementation_spec_v2.md` sekcje 3.1, 3.2, 3.3  

---

## Kontekst

Implementacja czystej warstwy domenowej — zero zależności zewnętrznych, zero importów platformowych. Wszystkie klasy trafiają do `commonMain`.

Wiele z tych klas **już istnieje** w projekcie w różnym stanie ukończenia. Zadaniem jest doprowadzenie ich do zgodności ze spec v2.1, a nie tworzenie od zera. Zawsze sprawdź istniejący plik przed napisaniem nowego.

---

## PR 1a — Domain enums

**Branch:** `session-1/domain-enums` ← `main`

### Pliki do sprawdzenia / zaktualizowania

**`domain/enums/GameStatus.kt`**  
Istniejące wartości: `ACTIVE`, `FINISHED` → zmienić na `IN_PROGRESS`, `COMPLETED`.  
⚠️ Ta zmiana wymaga aktualizacji wszystkich miejsc w kodzie gdzie używany jest `GameStatus` — sprawdź wszystkie referencje przed commitem.

**`domain/enums/GameExpansion.kt`**  
Istniejący plik — sprawdź czy wartości `SEAFARERS`, `CITIES_AND_KNIGHTS` są zgodne ze spec. Jeśli tak — brak zmian.

**`domain/enums/EventDiceType.kt`**  
Istniejący plik — sprawdź czy wartości `POLITICS`, `SCIENCE`, `TRADE`, `BARBARIANS` są zgodne ze spec. Jeśli tak — brak zmian.

**`domain/enums/DiceType.kt`**  
Istniejący plik — używany tylko w warstwie prezentacji dla kolorów kości.  
Dodaj extension properties jeśli ich brakuje:
```kotlin
enum class DiceType {
    RED, YELLOW;
    // Color properties należą do warstwy prezentacji
    // Jeśli plik jest w domain/enums — przenieś color logic do presentation
    // lub użyj extension functions w pliku prezentacyjnym
}
```
⚠️ `Color` to import Compose — nie może być w `domain/enums/`. Jeśli obecna implementacja ma `Color` w enumie — wynieś color mapping do `presentation/` jako extension.

### Commit
```
feat(domain): update GameStatus enum values to IN_PROGRESS/COMPLETED
```

---

## PR 1b — Core domain models

**Branch:** `session-1/core-domain-models` ← `session-1/domain-enums`

### Pliki do sprawdzenia / zaktualizowania

**`domain/model/Player.kt`** (obecnie w `domain/dataclass/`)  
Dodaj brakujące pola:
```kotlin
data class Player(
    val id: Long = 0L,
    val name: String,
    val isHidden: Boolean = false,
    val gamesPlayed: Int = 0,    // derived — populated by repository
    val gamesWon: Int = 0        // derived — populated by repository
)
```

**`domain/model/Game.kt`** (obecnie w `domain/dataclass/`)  
Obecna implementacja używa `GameConfig` jako nested object. Zmienić na flat fields zgodnie ze spec:
```kotlin
data class Game(
    val id: Long = 0L,
    val turnDurationMillis: Long,
    val expansions: Set<GameExpansion>,
    val specialTurnRuleEnabled: Boolean,
    val status: GameStatus,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val winnerId: Long? = null,
    val players: List<GamePlayer> = emptyList()
)
```
⚠️ `GameConfig` i `GameSession` były project-specific — `GameConfig` zostaje usunięty jako osobna klasa (jego pola wchłania `Game`). `GameSession` przeniesiony do `domain/session/` w Sesji 4.

**`domain/model/GamePlayer.kt`** — nowy plik:
```kotlin
data class GamePlayer(
    val gameId: Long,
    val playerId: Long,
    val playerName: String,
    val orderIndex: Int
)
```

**`domain/model/Turn.kt`** (obecnie w `domain/dataclass/`)  
Dodaj brakujące pola i computed properties:
```kotlin
data class Turn(
    val id: Long = 0L,
    val gameId: Long,
    val number: Int,
    val playerId: Long,
    val playerName: String,        // dodać
    val secondaryPlayerId: Long? = null,
    val secondaryPlayerName: String? = null,  // dodać
    val redDice: Int? = null,
    val yellowDice: Int? = null,
    val eventDice: EventDiceType? = null,
    val durationMillis: Long = 0L
) {
    val diceSum: Int?
        get() = if (redDice != null && yellowDice != null) redDice + yellowDice else null
    val isRobberRoll: Boolean
        get() = diceSum == 7
}
```

**`domain/model/DiceRoll.kt`** — nowy plik:
```kotlin
data class DiceRoll(
    val red: Int,
    val yellow: Int,
    val event: EventDiceType? = null
) {
    val sum: Int get() = red + yellow
}
```

⚠️ Po zmianie `Game` sprawdź wszystkie referencje do `game.config.*` — zastąp bezpośrednimi polami `game.*`.

### Commit
```
feat(domain): add and update core domain models (Player, Game, GamePlayer, Turn, DiceRoll)
```

---

## PR 1c — Statistics models & barbarian helper

**Branch:** `session-1/statistics-models` ← `session-1/core-domain-models`

**`domain/model/DiceDistribution.kt`** — nowy plik:
```kotlin
data class DiceDistribution(
    val counts: Map<Int, Int>   // sum (2–12) -> roll count
)
```

**`domain/model/GameStatistics.kt`** — nowy plik:
```kotlin
data class GameStatistics(
    val totalTurns: Int,
    val averageTurnDurationMillis: Long,
    val diceDistribution: DiceDistribution,
    val eventDiceDistribution: Map<EventDiceType, Int>,
    val playerAverageTurnDurations: Map<Long, Long>  // playerId -> avgMillis
)
```

**`domain/model/BarbarianState.kt`** — nowy plik zawierający model i extension:
```kotlin
data class BarbarianState(
    val position: Int,                  // 0–7 (0 = start, 7 = island)
    val raidsCompleted: Int,
    val hasFirstRaidOccurred: Boolean
)

fun List<Turn>.toBarbarianState(): BarbarianState {
    val barbarianRolls = count { it.eventDice == EventDiceType.BARBARIANS }
    val position = barbarianRolls % 8
    val raidsCompleted = barbarianRolls / 8
    return BarbarianState(
        position = position,
        raidsCompleted = raidsCompleted,
        hasFirstRaidOccurred = raidsCompleted > 0 || (position == 0 && barbarianRolls > 0)
    )
}
```

### Commit
```
feat(domain): add DiceDistribution, GameStatistics, BarbarianState models
```

---

## PR 2a — Repository interfaces

**Branch:** `session-2/repository-interfaces` ← `session-1/statistics-models`

Istniejące interfejsy mają niepełne lub niezgodne sygnatury. Zamień je w całości na poniższe.

**`domain/repository/PlayerRepository.kt`:**
```kotlin
interface PlayerRepository {
    fun getAllPlayers(): Flow<List<Player>>
    fun getVisiblePlayers(): Flow<List<Player>>
    fun getPlayerById(id: Long): Flow<Player?>
    suspend fun createPlayer(name: String): Long
    suspend fun updatePlayer(player: Player)
    suspend fun hidePlayer(id: Long)
    suspend fun deletePlayer(id: Long)
    suspend fun canDeletePlayer(id: Long): Boolean
}
```

**`domain/repository/GameRepository.kt`:**
```kotlin
interface GameRepository {
    fun getAllGames(): Flow<List<Game>>
    fun getInProgressGames(): Flow<List<Game>>
    fun getCompletedGames(): Flow<List<Game>>
    fun getGameById(id: Long): Flow<Game?>
    fun getMostRecentInProgressGame(): Flow<Game?>
    suspend fun createGame(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        playerIds: List<Long>
    ): Long
    suspend fun updateGameSettings(
        gameId: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean
    )
    suspend fun endGame(gameId: Long, winnerId: Long?)
    suspend fun deleteGame(id: Long)
}
```

**`domain/repository/TurnRepository.kt`:**
```kotlin
interface TurnRepository {
    fun getTurnsForGame(gameId: Long): Flow<List<Turn>>
    fun getTurnById(id: Long): Flow<Turn?>
    fun getCurrentTurn(gameId: Long): Flow<Turn?>
    suspend fun createTurn(gameId: Long, playerId: Long, number: Int): Long
    suspend fun updateTurn(turn: Turn)
    suspend fun updateDiceRoll(turnId: Long, redDice: Int, yellowDice: Int, eventDice: EventDiceType?)
    suspend fun updateDuration(turnId: Long, durationMillis: Long)
    suspend fun setSecondaryPlayer(turnId: Long, playerId: Long)
}
```

⚠️ Po zmianie interfejsów — istniejące implementacje (`*RepositoryImpl`) przestaną się kompilować. To oczekiwane — zostaną zaktualizowane w Sesji 3. Tymczasowo możesz dodać `TODO("Implemented in Session 3")` do brakujących metod lub oznaczyć plik jako `@Suppress("UNUSED")`.

### Commit
```
feat(domain): define complete repository interfaces with Flow support
```

---

## Uwagi ogólne dla tej sesji

- Wszystkie pliki trafiają do `commonMain` — zero importów `android.*`, `platform.*`, `java.awt.*`
- Żadnych `!!` — nullability obsługiwana przez typy nullable i elvis operator
- `val` wszędzie gdzie możliwe
- Nie modyfikuj plików poza wymienionymi — szczególnie nie ruszaj `data/` ani `di/`
- `GameConfig` jako osobna klasa domenowa przestaje istnieć — jej pola wchłania `Game`. Sprawdź czy `GameConfig` nie jest importowany gdzieś poza `GameSessionCoordinator` (Coordinator zostanie zaktualizowany w Sesji 4)
