# Session Spec 02 — Data Layer

**Sesje planu:** 3  
**Gałąź startowa:** ostatni branch Sesji 1/2 (`session-2/repository-interfaces`)  
**Spec referencyjny:** `catan_companion_implementation_spec_v2.md` sekcje 4.1–4.5  

---

## Kontekst

Implementacja kompletnej warstwy danych: encje Room, DAO, konwertery typów, mappery i implementacje repozytoriów. Większość plików **już istnieje** — zadaniem jest aktualizacja do spec v2.1, nie pisanie od zera.

⚠️ Po tej sesji projekt powinien się **kompilować** mimo że warstwy wyżej (session, use cases, DI) są jeszcze niekompletne.

---

## PR 3a — Entities

**Branch:** `session-3/entities` ← `session-2/repository-interfaces`

### `data/local/entity/PlayerEntity.kt`
Dodaj brakujące pole `isHidden`:
```kotlin
@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val isHidden: Boolean = false   // ← dodać
)
```

### `data/local/entity/GameEntity.kt`
Dodaj brakujące pole `winnerId`:
```kotlin
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val turnDurationMillis: Long,
    val expansions: Set<GameExpansion>,
    val specialTurnRuleEnabled: Boolean,
    val status: GameStatus,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val winnerId: Long? = null      // ← dodać
)
```

### `data/local/entity/GamePlayerEntity.kt`
Istniejąca klasa `GamePlayerCrossRefEntity` — zmienić nazwę na `GamePlayerEntity` i upewnić się że zawiera `orderIndex`. Pełna definicja:
```kotlin
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
```

### `data/local/entity/TurnEntity.kt`
Istniejący plik — sprawdź zgodność ze spec. Powinien wyglądać jak poniżej (zmiana jest tylko jeśli coś nie pasuje):
```kotlin
@Entity(
    tableName = "turns",
    foreignKeys = [
        ForeignKey(entity = GameEntity::class, parentColumns = ["id"],
            childColumns = ["gameId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PlayerEntity::class, parentColumns = ["id"],
            childColumns = ["playerId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = PlayerEntity::class, parentColumns = ["id"],
            childColumns = ["secondaryPlayerId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("gameId"), Index("playerId"), Index("secondaryPlayerId")]
)
data class TurnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
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

### Commit
```
feat(data): update entities — add isHidden, winnerId, rename GamePlayerEntity
```

---

## PR 3b — DAOs & converters

**Branch:** `session-3/daos-and-converters` ← `session-3/entities`

### `data/local/dao/PlayerDao.kt`
Zastąp istniejący plik:
```kotlin
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
```

### `data/local/dao/GameDao.kt`
Zastąp istniejący plik — dodaj Flow-returning queries i brakujące metody:
```kotlin
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

    @Query("SELECT * FROM games WHERE status = 'IN_PROGRESS' ORDER BY startedAt DESC LIMIT 1")
    fun getMostRecentInProgress(): Flow<GameEntity?>

    @Insert
    suspend fun insert(game: GameEntity): Long

    @Update
    suspend fun update(game: GameEntity)

    @Delete
    suspend fun delete(game: GameEntity)
}
```

### `data/local/dao/GamePlayerDao.kt`
Nowy plik — zastępuje `insertGamePlayerCrossRefs` który był w `GameDao`:
```kotlin
@Dao
interface GamePlayerDao {
    @Query("SELECT * FROM game_players WHERE gameId = :gameId ORDER BY orderIndex ASC")
    suspend fun getForGame(gameId: Long): List<GamePlayerEntity>

    @Insert
    suspend fun insertAll(players: List<GamePlayerEntity>)

    @Query("DELETE FROM game_players WHERE gameId = :gameId")
    suspend fun deleteForGame(gameId: Long)
}
```

### `data/local/dao/TurnDao.kt`
Zastąp istniejący plik — zmień `suspend` queries na `Flow`:
```kotlin
@Dao
interface TurnDao {
    @Query("SELECT * FROM turns WHERE gameId = :gameId ORDER BY number ASC")
    fun getForGame(gameId: Long): Flow<List<TurnEntity>>

    @Query("SELECT * FROM turns WHERE id = :id")
    fun getById(id: Long): Flow<TurnEntity?>

    @Query("SELECT * FROM turns WHERE gameId = :gameId ORDER BY number DESC LIMIT 1")
    fun getCurrentForGame(gameId: Long): Flow<TurnEntity?>

    @Insert
    suspend fun insert(turn: TurnEntity): Long

    @Update
    suspend fun update(turn: TurnEntity)
}
```

### Konwertery
Istniejące konwertery — sprawdź zgodność. Jedyna wymagana zmiana to w `GameStatusConverter` — wartości muszą mapować `IN_PROGRESS`/`COMPLETED` (jeśli enum został zaktualizowany w Sesji 1, konwerter działa automatycznie przez `status.name`).

Upewnij się że wszystkie trzy istnieją i są poprawne:
- `GameExpansionConverter` — `Set<GameExpansion>` ↔ `String` (comma-separated)
- `GameStatusConverter` — `GameStatus` ↔ `String`
- `EventDiceTypeConverter` — `EventDiceType?` ↔ `String?`

### Commit
```
feat(data): update DAOs with Flow queries, add GamePlayerDao, verify converters
```

---

## PR 3c — Database & migration

**Branch:** `session-3/database-and-migration` ← `session-3/daos-and-converters`

### `data/local/database/CatanCompanionDatabase.kt`
Zaktualizuj istniejącą klasę (poprzednio `CatanTimerDatabase`) — zmień nazwę i dodaj `GamePlayerDao`:
```kotlin
@Database(
    entities = [
        PlayerEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        TurnEntity::class
    ],
    version = 2,        // ← zmiana z 1 na 2
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
    abstract fun gamePlayerDao(): GamePlayerDao   // ← nowe
    abstract fun turnDao(): TurnDao
}
```

⚠️ Zmiana nazwy klasy z `CatanTimerDatabase` na `CatanCompanionDatabase` wymaga aktualizacji:
- `DatabaseFactory.android.kt`, `DatabaseFactory.ios.kt`, `DatabaseFactory.desktop.kt`
- Koin module gdzie database jest rejestrowana

### `data/local/database/migrations/Migration_1_2.kt`
Nowy plik:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE games ADD COLUMN winnerId INTEGER")
        database.execSQL(
            "ALTER TABLE players ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0"
        )
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS game_players (
                gameId INTEGER NOT NULL,
                playerId INTEGER NOT NULL,
                orderIndex INTEGER NOT NULL,
                PRIMARY KEY(gameId, playerId),
                FOREIGN KEY(gameId) REFERENCES games(id) ON DELETE CASCADE,
                FOREIGN KEY(playerId) REFERENCES players(id) ON DELETE RESTRICT
            )
        """.trimIndent())
        database.execSQL(
            "UPDATE games SET status = 'IN_PROGRESS' WHERE status = 'ACTIVE'"
        )
        database.execSQL(
            "UPDATE games SET status = 'COMPLETED' WHERE status = 'FINISHED'"
        )
    }
}
```

Migrację dodaj do buildera bazy w `DatabaseFactory`:
```kotlin
.addMigrations(MIGRATION_1_2)
```

### Commit
```
chore(data): rename database to CatanCompanionDatabase, bump version to 2, add migration
```

---

## PR 3d — Mappers

**Branch:** `session-3/mappers` ← `session-3/database-and-migration`

Istniejące mappery jako extension functions — zachowaj konwencję, zaktualizuj do nowych modeli.

### `data/mapper/PlayerMappers.kt`
```kotlin
fun PlayerEntity.toDomain(gamesPlayed: Int = 0, gamesWon: Int = 0): Player = Player(
    id = id,
    name = name,
    isHidden = isHidden,
    gamesPlayed = gamesPlayed,
    gamesWon = gamesWon
)

fun Player.toEntity(): PlayerEntity = PlayerEntity(
    id = id,
    name = name,
    isHidden = isHidden
)
```

### `data/mapper/GameMappers.kt`
Mapowanie musi uwzględniać że `Game.players` jest ładowany osobno przez `GamePlayerDao` — nie jest częścią `GameEntity`.
```kotlin
fun GameEntity.toDomain(players: List<GamePlayer> = emptyList()): Game = Game(
    id = id,
    turnDurationMillis = turnDurationMillis,
    expansions = expansions,
    specialTurnRuleEnabled = specialTurnRuleEnabled,
    status = status,
    startedAt = startedAt,
    finishedAt = finishedAt,
    winnerId = winnerId,
    players = players
)

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    turnDurationMillis = turnDurationMillis,
    expansions = expansions,
    specialTurnRuleEnabled = specialTurnRuleEnabled,
    status = status,
    startedAt = startedAt,
    finishedAt = finishedAt,
    winnerId = winnerId
)

fun GamePlayerEntity.toDomain(playerName: String): GamePlayer = GamePlayer(
    gameId = gameId,
    playerId = playerId,
    playerName = playerName,
    orderIndex = orderIndex
)
```

### `data/mapper/TurnMappers.kt`
`Turn` wymaga `playerName` i `secondaryPlayerName` które nie są w encji — repository musi je dostarczyć:
```kotlin
fun TurnEntity.toDomain(playerName: String, secondaryPlayerName: String? = null): Turn = Turn(
    id = id,
    gameId = gameId,
    number = number,
    playerId = playerId,
    playerName = playerName,
    secondaryPlayerId = secondaryPlayerId,
    secondaryPlayerName = secondaryPlayerName,
    redDice = redDice,
    yellowDice = yellowDice,
    eventDice = eventDice,
    durationMillis = durationMillis
)

fun Turn.toEntity(): TurnEntity = TurnEntity(
    id = id,
    gameId = gameId,
    number = number,
    playerId = playerId,
    secondaryPlayerId = secondaryPlayerId,
    redDice = redDice,
    yellowDice = yellowDice,
    eventDice = eventDice,
    durationMillis = durationMillis
)
```

### Commit
```
feat(data): update mappers to match revised domain models
```

---

## PR 3e — Repository implementations

**Branch:** `session-3/repository-impls` ← `session-3/mappers`

### `data/repository/PlayerRepositoryImpl.kt`
Zaimplementuj wszystkie metody z `PlayerRepository`. Kluczowe decyzje:
- `gamesPlayed` = `playerDao.getGameCount(id)` wywołane przy mapowaniu
- `gamesWon` = brak dedykowanego DAO query — na razie `0` z TODO
- `canDeletePlayer` = `getGameCount(id) == 0`
- `getAllPlayers()` i `getVisiblePlayers()` — mapuj Flow używając `.map { list -> list.map { entity -> entity.toDomain(...) } }`

```kotlin
class PlayerRepositoryImpl(
    private val playerDao: PlayerDao
) : PlayerRepository {
    override fun getAllPlayers(): Flow<List<Player>> =
        playerDao.getAll().map { entities ->
            entities.map { entity ->
                val gamesPlayed = playerDao.getGameCount(entity.id)
                entity.toDomain(gamesPlayed = gamesPlayed)
            }
        }
    // ... pozostałe metody
}
```

⚠️ `getGameCount` jest `suspend` a Flow map nie jest — użyj `transform` lub `map` z `flowOn` / rozważ czy `gamesPlayed` wymaga osobnego query per emisję czy można zoptymalizować.

### `data/repository/GameRepositoryImpl.kt`
Kluczowe decyzje:
- `getGameById`, `getAllGames`, etc. — zwracają `Flow<Game>` gdzie `Game.players` jest ładowany przez `GamePlayerDao` i `PlayerDao` żeby uzyskać `playerName`
- `createGame` — insert do `GameEntity` + insert listy `GamePlayerEntity` w jednej transakcji (`@Transaction`)
- `endGame` — `update(game.copy(status = COMPLETED, finishedAt = ..., winnerId = ...))`
- Usuń istniejące stubby `getActiveGame` z TODO

### `data/repository/TurnRepositoryImpl.kt`
Kluczowe decyzje:
- `getTurnsForGame` — Flow gdzie każda `TurnEntity` jest mapowana z `playerName` pobranym z `PlayerDao`
- `updateDiceRoll`, `updateDuration`, `setSecondaryPlayer` — pobierz entity przez suspend query, skopiuj z nowymi wartościami, wywołaj `update`

### Commit
```
feat(data): implement repository classes with full Flow support
```

---

## Uwagi ogólne dla tej sesji

- Nie używaj `!!` — jeśli Flow może emitować null to użyj `filterNotNull()` lub obsłuż null jawnie
- `@Transaction` na metodach które wykonują więcej niż jedną operację bazodanową
- Istniejące fake DAO w `commonTest/` będą wymagały aktualizacji po zmianie interfejsów DAO — zrób to w tej samej sesji jeśli kompilacja tego wymaga, ale nie pisz nowych testów (Sesja 7)
- `GameSummaryProjection` — istniejący Room projection — sprawdź czy jest nadal używany; jeśli nie — usuń
