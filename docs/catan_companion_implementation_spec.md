# CatanCompanion — Implementation Specification

**Version:** 1.0  
**Last Updated:** 2026-04-12  
**Status:** Draft  
**Related Document:** catan_companion_design_spec.md

---

## 1. Technical Overview

### 1.1 Technology Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin (100%) |
| Architecture | Kotlin Multiplatform (KMP) |
| UI Framework | Compose Multiplatform |
| Database | Room (Multiplatform) |
| Dependency Injection | Koin |
| Async | Kotlin Coroutines + Flow |

### 1.2 Target Platforms

- Android (minSdk 24, targetSdk 34)
- iOS (via Kotlin/Native)
- Desktop/JVM (for architectural consistency)

### 1.3 Module Structure

```
project/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/
│   │   │   └── kotlin/org/example/project/
│   │   │       ├── catan_companion/          ← Feature module
│   │   │       │   ├── domain/
│   │   │       │   ├── data/
│   │   │       │   ├── presentation/
│   │   │       │   └── di/
│   │   │       └── core/                     ← Shared utilities
│   │   ├── androidMain/
│   │   ├── iosMain/
│   │   └── desktopMain/
│   └── composeResources/
│       ├── values/
│       │   └── strings.xml                   ← English (default)
│       └── values-pl/
│           └── strings.xml                   ← Polish
```

---

## 2. Architecture

### 2.1 Layer Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │ Composables │──│  ViewModel  │──│  UiState / UiEvent      │ │
│  │ (Screens)   │  │ (StateFlow) │  │  (Data Classes/Sealed)  │ │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘ │
│         │                │                                      │
│         ▼                ▼                                      │
├─────────────────────────────────────────────────────────────────┤
│                        DOMAIN LAYER                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │  Use Cases  │  │ Repository  │  │   Domain Models         │ │
│  │  (invoke)   │  │ (Interface) │  │   (Pure Kotlin)         │ │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘ │
│         │                │                                      │
│         ▼                ▼                                      │
├─────────────────────────────────────────────────────────────────┤
│                         DATA LAYER                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │ Repository  │  │    Room     │  │   Entity / Mapper       │ │
│  │   (Impl)    │  │    DAOs     │  │                         │ │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Package Structure

```
catan_companion/
├── domain/
│   ├── model/
│   │   ├── Player.kt
│   │   ├── Game.kt
│   │   ├── Turn.kt
│   │   ├── GamePlayer.kt
│   │   ├── DiceRoll.kt
│   │   └── GameStatistics.kt
│   ├── repository/
│   │   ├── PlayerRepository.kt
│   │   ├── GameRepository.kt
│   │   └── TurnRepository.kt
│   ├── usecase/
│   │   ├── player/
│   │   │   ├── GetAllPlayersUseCase.kt
│   │   │   ├── GetPlayerByIdUseCase.kt
│   │   │   ├── CreatePlayerUseCase.kt
│   │   │   ├── UpdatePlayerUseCase.kt
│   │   │   ├── HidePlayerUseCase.kt
│   │   │   └── DeletePlayerUseCase.kt
│   │   ├── game/
│   │   │   ├── GetAllGamesUseCase.kt
│   │   │   ├── GetGameByIdUseCase.kt
│   │   │   ├── GetInProgressGameUseCase.kt
│   │   │   ├── CreateGameUseCase.kt
│   │   │   ├── UpdateGameSettingsUseCase.kt
│   │   │   ├── EndGameUseCase.kt
│   │   │   └── DeleteGameUseCase.kt
│   │   ├── turn/
│   │   │   ├── GetTurnsForGameUseCase.kt
│   │   │   ├── GetCurrentTurnUseCase.kt
│   │   │   ├── CreateTurnUseCase.kt
│   │   │   ├── UpdateTurnUseCase.kt
│   │   │   └── CalculateBarbarianPositionUseCase.kt
│   │   └── statistics/
│   │       ├── GetGameStatisticsUseCase.kt
│   │       ├── GetPlayerStatisticsUseCase.kt
│   │       └── GetDiceDistributionUseCase.kt
│   └── enums/
│       ├── GameExpansion.kt
│       ├── GameStatus.kt
│       ├── EventDiceType.kt
│       └── DiceType.kt
│
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   └── CatanCompanionDatabase.kt
│   │   ├── dao/
│   │   │   ├── PlayerDao.kt
│   │   │   ├── GameDao.kt
│   │   │   ├── GamePlayerDao.kt
│   │   │   └── TurnDao.kt
│   │   ├── entity/
│   │   │   ├── PlayerEntity.kt
│   │   │   ├── GameEntity.kt
│   │   │   ├── GamePlayerEntity.kt
│   │   │   └── TurnEntity.kt
│   │   └── converter/
│   │       ├── GameExpansionConverter.kt
│   │       ├── GameStatusConverter.kt
│   │       └── EventDiceTypeConverter.kt
│   ├── repository/
│   │   ├── PlayerRepositoryImpl.kt
│   │   ├── GameRepositoryImpl.kt
│   │   └── TurnRepositoryImpl.kt
│   └── mapper/
│       ├── PlayerMapper.kt
│       ├── GameMapper.kt
│       └── TurnMapper.kt
│
├── presentation/
│   ├── navigation/
│   │   └── CatanCompanionNavigation.kt
│   ├── state/
│   │   ├── DashboardUiState.kt
│   │   ├── GameConfigUiState.kt
│   │   ├── GameplayUiState.kt
│   │   ├── PlayersListUiState.kt
│   │   ├── PlayerDetailsUiState.kt
│   │   ├── GamesListUiState.kt
│   │   └── GameSummaryUiState.kt
│   ├── viewmodel/
│   │   ├── DashboardViewModel.kt
│   │   ├── GameConfigViewModel.kt
│   │   ├── GameplayViewModel.kt
│   │   ├── PlayersListViewModel.kt
│   │   ├── PlayerDetailsViewModel.kt
│   │   ├── GamesListViewModel.kt
│   │   └── GameSummaryViewModel.kt
│   ├── screen/
│   │   ├── DashboardScreen.kt
│   │   ├── GameConfigScreen.kt
│   │   ├── GameplayScreen.kt
│   │   ├── PlayersListScreen.kt
│   │   ├── PlayerDetailsScreen.kt
│   │   ├── GamesListScreen.kt
│   │   ├── GameSummaryScreen.kt
│   │   └── WinnerSelectionScreen.kt
│   └── components/
│       ├── dice/
│       │   ├── DiceRow.kt
│       │   ├── Dice.kt
│       │   ├── EventDiceRow.kt
│       │   ├── EventDice.kt
│       │   └── DiceLayout.kt
│       ├── timer/
│       │   ├── GameTimer.kt
│       │   └── TimerControls.kt
│       ├── charts/
│       │   ├── DiceDistributionChart.kt
│       │   └── AnimatedBarChart.kt
│       ├── common/
│       │   ├── PlayerListItem.kt
│       │   ├── GameListItem.kt
│       │   └── ConfirmationDialog.kt
│       └── gameplay/
│           ├── TurnNavigator.kt
│           ├── EventPhaseContent.kt
│           ├── BarbarianTracker.kt
│           └── StatisticsPopup.kt
│
└── di/
    ├── CatanCompanionModule.kt
    ├── DatabaseModule.kt
    ├── RepositoryModule.kt
    ├── UseCaseModule.kt
    └── ViewModelModule.kt
```

---

## 3. Domain Layer

### 3.1 Domain Models

```kotlin
// domain/model/Player.kt
data class Player(
    val id: Long = 0L,
    val name: String,
    val isHidden: Boolean = false,
    val gamesPlayed: Int = 0,    // derived
    val gamesWon: Int = 0        // derived
)

// domain/model/Game.kt
data class Game(
    val id: Long = 0L,
    val turnDurationMillis: Long,
    val expansions: Set<GameExpansion>,
    val specialTurnRuleEnabled: Boolean,
    val status: GameStatus,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val winnerId: Long? = null,
    val players: List<GamePlayer> = emptyList()  // ordered
)

// domain/model/GamePlayer.kt
data class GamePlayer(
    val gameId: Long,
    val playerId: Long,
    val playerName: String,
    val orderIndex: Int
)

// domain/model/Turn.kt
data class Turn(
    val id: Long = 0L,
    val gameId: Long,
    val number: Int,
    val playerId: Long,
    val playerName: String,
    val secondaryPlayerId: Long? = null,
    val secondaryPlayerName: String? = null,
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

// domain/model/DiceRoll.kt
data class DiceRoll(
    val red: Int,
    val yellow: Int,
    val event: EventDiceType? = null
) {
    val sum: Int get() = red + yellow
}

// domain/model/GameStatistics.kt
data class GameStatistics(
    val totalTurns: Int,
    val averageTurnDurationMillis: Long,
    val diceDistribution: Map<Int, Int>,           // sum (2-12) -> count
    val eventDiceDistribution: Map<EventDiceType, Int>,
    val playerAverageTurnDurations: Map<Long, Long>  // playerId -> avgMillis
)
```

### 3.2 Repository Interfaces

```kotlin
// domain/repository/PlayerRepository.kt
interface PlayerRepository {
    fun getAllPlayers(): Flow<List<Player>>
    fun getVisiblePlayers(): Flow<List<Player>>
    fun getPlayerById(id: Long): Flow<Player?>
    suspend fun createPlayer(name: String): Result<Long, DataError.Local>
    suspend fun updatePlayer(player: Player): EmptyResult<DataError.Local>
    suspend fun hidePlayer(id: Long): EmptyResult<DataError.Local>
    suspend fun deletePlayer(id: Long): EmptyResult<DataError.Local>
    suspend fun canDeletePlayer(id: Long): Boolean
}

// domain/repository/GameRepository.kt
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
    ): Result<Long, DataError.Local>
    suspend fun updateGameSettings(
        gameId: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean
    ): EmptyResult<DataError.Local>
    suspend fun endGame(gameId: Long, winnerId: Long?): EmptyResult<DataError.Local>
    suspend fun deleteGame(id: Long): EmptyResult<DataError.Local>
}

// domain/repository/TurnRepository.kt
interface TurnRepository {
    fun getTurnsForGame(gameId: Long): Flow<List<Turn>>
    fun getTurnById(id: Long): Flow<Turn?>
    fun getCurrentTurn(gameId: Long): Flow<Turn?>
    suspend fun createTurn(
        gameId: Long,
        playerId: Long,
        number: Int
    ): Result<Long, DataError.Local>
    suspend fun updateTurn(turn: Turn): EmptyResult<DataError.Local>
    suspend fun updateDiceRoll(
        turnId: Long,
        redDice: Int,
        yellowDice: Int,
        eventDice: EventDiceType?
    ): EmptyResult<DataError.Local>
    suspend fun updateDuration(turnId: Long, durationMillis: Long): EmptyResult<DataError.Local>
    suspend fun setSecondaryPlayer(turnId: Long, playerId: Long): EmptyResult<DataError.Local>
}
```

### 3.3 Use Cases

All use cases follow the single-responsibility principle with a single `invoke()` operator function.

```kotlin
// domain/usecase/game/CreateGameUseCase.kt
class CreateGameUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        playerIds: List<Long>
    ): Result<Long> {
        // Validation
        if (playerIds.size < 3 || playerIds.size > 6) {
            return Result.failure(IllegalArgumentException("Player count must be 3-6"))
        }
        if (playerIds.distinct().size != playerIds.size) {
            return Result.failure(IllegalArgumentException("Duplicate players not allowed"))
        }
        if (specialTurnRuleEnabled && playerIds.size < 5) {
            return Result.failure(IllegalArgumentException("In-between turns requires 5+ players"))
        }
        
        return try {
            val gameId = gameRepository.createGame(
                turnDurationMillis = turnDurationMillis,
                expansions = expansions,
                specialTurnRuleEnabled = specialTurnRuleEnabled,
                playerIds = playerIds
            )
            Result.success(gameId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// domain/usecase/turn/CalculateBarbarianPositionUseCase.kt
class CalculateBarbarianPositionUseCase(
    private val turnRepository: TurnRepository
) {
    operator fun invoke(gameId: Long): Flow<BarbarianState> {
        return turnRepository.getTurnsForGame(gameId).map { turns ->
            val barbarianRolls = turns.count { it.eventDice == EventDiceType.BARBARIANS }
            val position = barbarianRolls % 8
            val raidsCompleted = barbarianRolls / 8
            
            BarbarianState(
                position = position,
                raidsCompleted = raidsCompleted,
                hasFirstRaidOccurred = raidsCompleted > 0 || position == 0 && barbarianRolls > 0
            )
        }
    }
}

data class BarbarianState(
    val position: Int,              // 0-7 (0 = start, 7 = island)
    val raidsCompleted: Int,
    val hasFirstRaidOccurred: Boolean
)
```

---

## 4. Data Layer

### 4.1 Room Database

```kotlin
// data/local/database/CatanCompanionDatabase.kt
@Database(
    entities = [
        PlayerEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        TurnEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    GameExpansionConverter::class,
    GameStatusConverter::class,
    EventDiceTypeConverter::class
)
abstract class CatanCompanionDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun gamePlayerDao(): GamePlayerDao
    abstract fun turnDao(): TurnDao
}
```

### 4.2 Entities

```kotlin
// data/local/entity/PlayerEntity.kt
@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val isHidden: Boolean = false
)

// data/local/entity/GameEntity.kt
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val turnDurationMillis: Long,
    val expansions: Set<GameExpansion>,
    val specialTurnRuleEnabled: Boolean,
    val status: GameStatus,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val winnerId: Long? = null
)

// data/local/entity/GamePlayerEntity.kt
@Entity(
    tableName = "game_players",
    primaryKeys = ["gameId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("gameId"), Index("playerId")]
)
data class GamePlayerEntity(
    val gameId: Long,
    val playerId: Long,
    val orderIndex: Int
)

// data/local/entity/TurnEntity.kt
@Entity(
    tableName = "turns",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["secondaryPlayerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("gameId"), Index("playerId"), Index("secondaryPlayerId")]
)
data class TurnEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val gameId: Long,
    val number: Int,
    val playerId: Long,
    val secondaryPlayerId: Long? = null,
    val redDice: Int? = null,
    val yellowDice: Int? = null,
    val eventDice: EventDiceType? = null,
    val durationMillis: Long = 0L
)
```

### 4.3 DAOs

```kotlin
// data/local/dao/PlayerDao.kt
@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAll(): Flow<List<PlayerEntity>>
    
    @Query("SELECT * FROM players WHERE isHidden = 0 ORDER BY name ASC")
    fun getVisible(): Flow<List<PlayerEntity>>
    
    @Query("SELECT * FROM players WHERE id = :id")
    fun getById(id: Long): Flow<PlayerEntity?>
    
    @Insert
    suspend fun insert(player: PlayerEntity): Long
    
    @Update
    suspend fun update(player: PlayerEntity)
    
    @Query("UPDATE players SET isHidden = 1 WHERE id = :id")
    suspend fun hide(id: Long)
    
    @Delete
    suspend fun delete(player: PlayerEntity)
    
    @Query("SELECT COUNT(*) FROM game_players WHERE playerId = :playerId")
    suspend fun getGameCount(playerId: Long): Int
}

// data/local/dao/GameDao.kt
@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY startedAt DESC")
    fun getAll(): Flow<List<GameEntity>>
    
    @Query("SELECT * FROM games WHERE status = 'IN_PROGRESS' ORDER BY startedAt DESC")
    fun getInProgress(): Flow<List<GameEntity>>
    
    @Query("SELECT * FROM games WHERE status = 'COMPLETED' ORDER BY finishedAt DESC")
    fun getCompleted(): Flow<List<GameEntity>>
    
    @Query("SELECT * FROM games WHERE id = :id")
    fun getById(id: Long): Flow<GameEntity?>
    
    @Query("""
        SELECT * FROM games 
        WHERE status = 'IN_PROGRESS' 
        ORDER BY startedAt DESC 
        LIMIT 1
    """)
    fun getMostRecentInProgress(): Flow<GameEntity?>
    
    @Insert
    suspend fun insert(game: GameEntity): Long
    
    @Update
    suspend fun update(game: GameEntity)
    
    @Delete
    suspend fun delete(game: GameEntity)
}

// data/local/dao/TurnDao.kt
@Dao
interface TurnDao {
    @Query("SELECT * FROM turns WHERE gameId = :gameId ORDER BY number ASC")
    fun getForGame(gameId: Long): Flow<List<TurnEntity>>
    
    @Query("SELECT * FROM turns WHERE id = :id")
    fun getById(id: Long): Flow<TurnEntity?>
    
    @Query("""
        SELECT * FROM turns 
        WHERE gameId = :gameId 
        ORDER BY number DESC 
        LIMIT 1
    """)
    fun getCurrentForGame(gameId: Long): Flow<TurnEntity?>
    
    @Query("""
        SELECT COUNT(*) FROM turns 
        WHERE gameId = :gameId AND eventDice = 'BARBARIANS'
    """)
    fun getBarbarianRollCount(gameId: Long): Flow<Int>
    
    @Insert
    suspend fun insert(turn: TurnEntity): Long
    
    @Update
    suspend fun update(turn: TurnEntity)
}
```

### 4.4 Type Converters

```kotlin
// data/local/converter/GameExpansionConverter.kt
class GameExpansionConverter {
    @TypeConverter
    fun fromSet(expansions: Set<GameExpansion>): String {
        return expansions.joinToString(",") { it.name }
    }
    
    @TypeConverter
    fun toSet(value: String): Set<GameExpansion> {
        if (value.isBlank()) return emptySet()
        return value.split(",").map { GameExpansion.valueOf(it) }.toSet()
    }
}

// data/local/converter/GameStatusConverter.kt
class GameStatusConverter {
    @TypeConverter
    fun fromStatus(status: GameStatus): String = status.name
    
    @TypeConverter
    fun toStatus(value: String): GameStatus = GameStatus.valueOf(value)
}

// data/local/converter/EventDiceTypeConverter.kt
class EventDiceTypeConverter {
    @TypeConverter
    fun fromType(type: EventDiceType?): String? = type?.name
    
    @TypeConverter
    fun toType(value: String?): EventDiceType? = value?.let { EventDiceType.valueOf(it) }
}
```

### 4.5 Mappers

```kotlin
// data/mapper/PlayerMapper.kt
class PlayerMapper {
    fun toDomain(entity: PlayerEntity, gamesPlayed: Int, gamesWon: Int): Player {
        return Player(
            id = entity.id,
            name = entity.name,
            isHidden = entity.isHidden,
            gamesPlayed = gamesPlayed,
            gamesWon = gamesWon
        )
    }
    
    fun toEntity(domain: Player): PlayerEntity {
        return PlayerEntity(
            id = domain.id,
            name = domain.name,
            isHidden = domain.isHidden
        )
    }
}
```

---

## 5. Presentation Layer

### 5.1 UI States

```kotlin
// presentation/state/GameplayUiState.kt
data class GameplayUiState(
    val game: Game? = null,
    val currentTurn: Turn? = null,
    val viewingTurn: Turn? = null,    // null = viewing current
    val phase: GameplayPhase = GameplayPhase.DICE_SELECTION,
    val timerState: TimerState = TimerState(),
    val barbarianState: BarbarianState? = null,
    val isEditing: Boolean = false,
    val pendingDiceEdit: DiceRoll? = null,
    val showStatisticsPopup: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class GameplayPhase {
    DICE_SELECTION,
    EVENT,              // robber or barbarians
    MAIN_TIMER,
    IN_BETWEEN_TIMER
}

data class TimerState(
    val remainingMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isForSecondaryPlayer: Boolean = false
)

// presentation/state/GameConfigUiState.kt
data class GameConfigUiState(
    val turnDurationMillis: Long = 120_000L,  // 2 minutes default
    val playerCount: Int = 3,
    val selectedPlayers: List<Player> = emptyList(),
    val expansions: Set<GameExpansion> = emptySet(),
    val specialTurnRuleEnabled: Boolean = false,
    val isValid: Boolean = false,
    val validationError: String? = null,
    val isLoading: Boolean = false
)
```

### 5.2 ViewModels

```kotlin
// presentation/viewmodel/GameplayViewModel.kt
class GameplayViewModel(
    private val gameId: Long,
    private val getGameByIdUseCase: GetGameByIdUseCase,
    private val getCurrentTurnUseCase: GetCurrentTurnUseCase,
    private val getTurnsForGameUseCase: GetTurnsForGameUseCase,
    private val createTurnUseCase: CreateTurnUseCase,
    private val updateTurnUseCase: UpdateTurnUseCase,
    private val calculateBarbarianPositionUseCase: CalculateBarbarianPositionUseCase,
    private val endGameUseCase: EndGameUseCase,
    private val updateGameSettingsUseCase: UpdateGameSettingsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GameplayUiState())
    val uiState: StateFlow<GameplayUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    
    init {
        loadGame()
        observeBarbarianPosition()
    }
    
    fun onDiceSelected(red: Int, yellow: Int, event: EventDiceType?) {
        // Update pending dice, check for events
    }
    
    fun onContinueFromDice() {
        // Save dice, determine if event phase needed
    }
    
    fun onContinueFromEvent() {
        // Transition to timer phase
    }
    
    fun onStartTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // Countdown logic with delay(100)
        }
    }
    
    fun onStopTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerState = it.timerState.copy(isRunning = false)) }
    }
    
    fun onAddTime() {
        _uiState.update { 
            it.copy(timerState = it.timerState.copy(
                remainingMillis = it.timerState.remainingMillis + 10_000
            ))
        }
    }
    
    fun onResetTimer() {
        val duration = _uiState.value.game?.turnDurationMillis ?: return
        _uiState.update {
            it.copy(timerState = it.timerState.copy(remainingMillis = duration))
        }
    }
    
    fun onNextTurn() {
        // Create new turn, reset state
    }
    
    fun onInBetweenTurn() {
        // Set secondary player, switch to in-between timer
    }
    
    fun onNavigateToPreviousTurn() {
        // Load previous turn for viewing
    }
    
    fun onNavigateToNextTurn() {
        // Load next turn or return to current
    }
    
    fun onJumpToCurrentTurn() {
        _uiState.update { it.copy(viewingTurn = null) }
    }
    
    fun onSaveEdit() {
        // Save pending dice edit, recalculate barbarians if needed
    }
    
    fun onCancelEdit() {
        _uiState.update { it.copy(isEditing = false, pendingDiceEdit = null) }
    }
    
    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
```

### 5.3 Navigation

```kotlin
// presentation/navigation/CatanCompanionNavigation.kt
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

---

## 6. UI Components

### 6.1 Dice Component — Recommended Implementation

**Approach: Canvas-based rendering**

```kotlin
// presentation/components/dice/DiceLayout.kt
object DiceLayout {
    /**
     * Returns normalized positions (0.0 to 1.0) for dots on a dice face.
     * Positions are relative to the dice bounds.
     */
    fun getPositions(value: Int): List<Offset> = when (value) {
        1 -> listOf(Offset(0.5f, 0.5f))
        2 -> listOf(Offset(0.25f, 0.25f), Offset(0.75f, 0.75f))
        3 -> listOf(Offset(0.25f, 0.25f), Offset(0.5f, 0.5f), Offset(0.75f, 0.75f))
        4 -> listOf(
            Offset(0.25f, 0.25f), Offset(0.75f, 0.25f),
            Offset(0.25f, 0.75f), Offset(0.75f, 0.75f)
        )
        5 -> listOf(
            Offset(0.25f, 0.25f), Offset(0.75f, 0.25f),
            Offset(0.5f, 0.5f),
            Offset(0.25f, 0.75f), Offset(0.75f, 0.75f)
        )
        6 -> listOf(
            Offset(0.25f, 0.2f), Offset(0.75f, 0.2f),
            Offset(0.25f, 0.5f), Offset(0.75f, 0.5f),
            Offset(0.25f, 0.8f), Offset(0.75f, 0.8f)
        )
        else -> emptyList()
    }
}

// presentation/components/dice/Dice.kt
@Composable
fun Dice(
    value: Int,
    isSelected: Boolean,
    backgroundColor: Color,
    dotColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp
) {
    val borderColor = if (isSelected) Color.Green else Color.Black
    val dotRadius = size * 0.08f
    
    Canvas(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .border(3.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        // Background
        drawRoundRect(
            color = backgroundColor.copy(alpha = 0.8f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        
        // Dots
        val positions = DiceLayout.getPositions(value)
        val padding = size.toPx() * 0.15f
        val drawableSize = size.toPx() - (padding * 2)
        
        positions.forEach { normalizedPos ->
            val x = padding + (normalizedPos.x * drawableSize)
            val y = padding + (normalizedPos.y * drawableSize)
            drawCircle(
                color = dotColor,
                radius = dotRadius.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

// presentation/components/dice/DiceRow.kt
@Composable
fun DiceRow(
    selectedValue: Int?,
    diceType: DiceType,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (1..6).forEach { value ->
            Dice(
                value = value,
                isSelected = value == selectedValue,
                backgroundColor = diceType.backgroundColor,
                dotColor = diceType.dotColor,
                onClick = { onValueSelected(value) }
            )
        }
    }
}
```

**DiceType extension properties:**

```kotlin
// domain/enums/DiceType.kt
enum class DiceType {
    RED,
    YELLOW;
    
    val backgroundColor: Color
        get() = when (this) {
            RED -> Color.Red
            YELLOW -> Color.Yellow
        }
    
    val dotColor: Color
        get() = when (this) {
            RED -> Color.Yellow
            YELLOW -> Color.Red
        }
}
```

### 6.2 Timer Component

```kotlin
// presentation/components/timer/GameTimer.kt
@Composable
fun GameTimer(
    remainingMillis: Long,
    modifier: Modifier = Modifier
) {
    val minutes = (remainingMillis / 60_000).toInt()
    val seconds = ((remainingMillis % 60_000) / 1000).toInt()
    
    Text(
        text = String.format("%02d:%02d", minutes, seconds),
        style = MaterialTheme.typography.displayLarge,
        modifier = modifier
    )
}

// presentation/components/timer/TimerControls.kt
@Composable
fun TimerControls(
    isRunning: Boolean,
    onStartStop: () -> Unit,
    onAddTime: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onStartStop) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) 
                    stringResource(Res.string.timer_stop) 
                    else stringResource(Res.string.timer_start)
            )
        }
        
        TextButton(onClick = onAddTime) {
            Text(stringResource(Res.string.timer_add_10))
        }
        
        IconButton(onClick = onReset) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(Res.string.timer_reset)
            )
        }
    }
}
```

### 6.3 Barbarian Tracker

```kotlin
// presentation/components/gameplay/BarbarianTracker.kt
@Composable
fun BarbarianTracker(
    position: Int,  // 0-7
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Sailing,  // or custom ship icon
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$position/8",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
```

### 6.4 Animated Bar Chart

```kotlin
// presentation/components/charts/AnimatedBarChart.kt
@Composable
fun AnimatedBarChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 1000
) {
    val maxValue = data.values.maxOrNull() ?: 1
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            val animatedHeight by animateFloatAsState(
                targetValue = value.toFloat() / maxValue,
                animationSpec = tween(durationMillis = animationDuration)
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight(animatedHeight)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(text = label, style = MaterialTheme.typography.labelSmall)
                Text(text = value.toString(), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
```

---

## 7. Dependency Injection

### 7.1 Module Organization

```kotlin
// di/CatanCompanionModule.kt
val catanCompanionModule = module {
    includes(
        databaseModule,
        repositoryModule,
        useCaseModule,
        viewModelModule
    )
}

// di/DatabaseModule.kt
val databaseModule = module {
    single { 
        Room.databaseBuilder(
            context = get(),
            klass = CatanCompanionDatabase::class.java,
            name = "catan_companion.db"
        ).build()
    }
    single { get<CatanCompanionDatabase>().playerDao() }
    single { get<CatanCompanionDatabase>().gameDao() }
    single { get<CatanCompanionDatabase>().gamePlayerDao() }
    single { get<CatanCompanionDatabase>().turnDao() }
}

// di/RepositoryModule.kt
val repositoryModule = module {
    single { PlayerMapper() }
    single { GameMapper() }
    single { TurnMapper() }
    
    single<PlayerRepository> { PlayerRepositoryImpl(get(), get()) }
    single<GameRepository> { GameRepositoryImpl(get(), get(), get()) }
    single<TurnRepository> { TurnRepositoryImpl(get(), get()) }
}

// di/UseCaseModule.kt
val useCaseModule = module {
    // Player use cases
    factory { GetAllPlayersUseCase(get()) }
    factory { GetPlayerByIdUseCase(get()) }
    factory { CreatePlayerUseCase(get()) }
    factory { UpdatePlayerUseCase(get()) }
    factory { HidePlayerUseCase(get()) }
    factory { DeletePlayerUseCase(get()) }
    
    // Game use cases
    factory { GetAllGamesUseCase(get()) }
    factory { GetGameByIdUseCase(get()) }
    factory { GetInProgressGameUseCase(get()) }
    factory { CreateGameUseCase(get()) }
    factory { UpdateGameSettingsUseCase(get()) }
    factory { EndGameUseCase(get()) }
    factory { DeleteGameUseCase(get()) }
    
    // Turn use cases
    factory { GetTurnsForGameUseCase(get()) }
    factory { GetCurrentTurnUseCase(get()) }
    factory { CreateTurnUseCase(get()) }
    factory { UpdateTurnUseCase(get()) }
    factory { CalculateBarbarianPositionUseCase(get()) }
    
    // Statistics use cases
    factory { GetGameStatisticsUseCase(get()) }
    factory { GetPlayerStatisticsUseCase(get()) }
    factory { GetDiceDistributionUseCase(get()) }
}

// di/ViewModelModule.kt
val viewModelModule = module {
    viewModelOf(::DashboardViewModel)
    viewModelOf(::GameConfigViewModel)
    viewModel { params -> GameplayViewModel(params.get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModelOf(::PlayersListViewModel)
    viewModel { params -> PlayerDetailsViewModel(params.get(), get()) }
    viewModelOf(::GamesListViewModel)
    viewModel { params -> GameSummaryViewModel(params.get(), get(), get()) }
}
```

---

## 8. Internationalization (i18n)

### 8.1 Resource Files Structure

```
composeResources/
├── values/
│   └── strings.xml          ← English (default)
└── values-pl/
    └── strings.xml          ← Polish
```

### 8.2 String Resources Example

```xml
<!-- values/strings.xml (English - DEFAULT) -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Dashboard -->
    <string name="dashboard_title">Catan Companion</string>
    <string name="dashboard_resume_game">Resume Last Game</string>
    <string name="dashboard_new_game">New Game</string>
    <string name="dashboard_games_list">Games</string>
    <string name="dashboard_players_list">Players</string>
    
    <!-- Game Configuration -->
    <string name="config_title">Game Setup</string>
    <string name="config_turn_duration">Turn Duration</string>
    <string name="config_player_count">Number of Players</string>
    <string name="config_players">Players</string>
    <string name="config_add_player">Add Player</string>
    <string name="config_options">Game Options</string>
    <string name="config_in_between_turns">In-Between Turns (5-6 players)</string>
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
</resources>
```

### 8.3 Usage in Composables

```kotlin
// ALWAYS use stringResource, NEVER hardcode strings
@Composable
fun DashboardScreen(...) {
    Text(text = stringResource(Res.string.dashboard_title))
    
    Button(onClick = onNewGame) {
        Text(text = stringResource(Res.string.dashboard_new_game))
    }
}

// For formatted strings
Text(
    text = stringResource(Res.string.gameplay_turn, turnNumber)
)
```

### 8.4 i18n Rules

1. **No hardcoded strings** — all user-facing text must come from resources
2. **Placeholders for dynamic content** — use `%1$s`, `%1$d` etc.
3. **Context-appropriate translations** — not literal word-for-word
4. **Consistent terminology** — same term for same concept throughout
5. **String keys follow hierarchy** — `screen_element_description` pattern

---

## 9. Platform-Specific Considerations

### 9.1 Vibration (expect/actual)

```kotlin
// commonMain - domain/service/HapticService.kt
expect class HapticService {
    fun vibrateTimerEnd()
}

// androidMain
actual class HapticService(private val context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)
    
    actual fun vibrateTimerEnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100, 50, 100),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 100, 50, 100, 50, 100), -1)
        }
    }
}

// iosMain
actual class HapticService {
    actual fun vibrateTimerEnd() {
        // iOS haptic implementation
    }
}

// desktopMain
actual class HapticService {
    actual fun vibrateTimerEnd() {
        // No-op or system beep
    }
}
```

### 9.2 Room Database (KMP)

Room Multiplatform requires platform-specific database builder:

```kotlin
// commonMain
expect fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<CatanCompanionDatabase>

// androidMain
actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<CatanCompanionDatabase> {
    val appContext = context as Context
    return Room.databaseBuilder(
        appContext,
        CatanCompanionDatabase::class.java,
        "catan_companion.db"
    )
}

// iosMain / desktopMain
// Platform-specific implementations
```

---

## 10. Testing Strategy

### 10.1 Unit Tests

| Layer | What to Test |
|-------|--------------|
| Domain | Use cases (business logic), Domain model validation |
| Data | Mappers, Repository logic (with fake DAOs) |
| Presentation | ViewModel state transitions, Event handling |

### 10.2 Key Test Cases

**CalculateBarbarianPositionUseCase:**
- 0 barbarian rolls → position 0
- 7 barbarian rolls → position 7 (on island)
- 8 barbarian rolls → position 0 (reset), 1 raid completed
- 15 barbarian rolls → position 7, 1 raid completed

**CreateGameUseCase:**
- Valid input → success
- < 3 players → failure
- > 6 players → failure
- Duplicate players → failure
- In-between rule with < 5 players → failure

**GameplayViewModel:**
- Dice selection → state update
- Roll = 7 → event phase triggered
- Roll = 7 + Cities & Knights + no raids → different event message
- Timer countdown → state updates
- Timer reaches 0 → vibration triggered

---

## 11. Migration Notes

### 11.1 From Existing Codebase

Based on provided entity files, the following changes are needed:

1. **Add `winnerId` to `GameEntity`** — currently missing
2. **Add `GamePlayerEntity`** — junction table for player ordering
3. **Add `isHidden` to `PlayerEntity`** — for soft delete
4. **Keep `DiceType` enum** — but ensure it's only used in presentation layer for colors
5. **EventDice icons** — migrate to Compose Multiplatform resources

### 11.2 Database Migration

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add winnerId to games
        database.execSQL("ALTER TABLE games ADD COLUMN winnerId INTEGER")
        
        // Create game_players table
        database.execSQL("""
            CREATE TABLE game_players (
                gameId INTEGER NOT NULL,
                playerId INTEGER NOT NULL,
                orderIndex INTEGER NOT NULL,
                PRIMARY KEY(gameId, playerId),
                FOREIGN KEY(gameId) REFERENCES games(id) ON DELETE CASCADE,
                FOREIGN KEY(playerId) REFERENCES players(id) ON DELETE RESTRICT
            )
        """)
        
        // Add isHidden to players
        database.execSQL("ALTER TABLE players ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0")
    }
}
```

---

## 12. Checklist Before Implementation

- [ ] Review CLAUDE.md rules compliance
- [ ] Confirm Room Multiplatform setup works for all targets
- [ ] Prepare i18n resource files (EN + PL)
- [ ] Create EventDice icon assets in composeResources
- [ ] Set up Koin modules structure
- [ ] Define navigation graph
- [ ] Implement Canvas-based Dice component
- [ ] Write unit tests for critical use cases

---

*End of Implementation Specification*
