package io.github.pawelzielinski.catantimer.catanCompanion.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Turn
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.EventDiceType
import io.github.pawelzielinski.catantimer.catanCompanion.domain.factory.TurnFactory
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.GameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.TurnRepository
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Error
import io.github.pawelzielinski.catantimer.core.domain.IllegalOperationError
import io.github.pawelzielinski.catantimer.core.domain.Result
import io.github.pawelzielinski.catantimer.core.domain.onFailure
import io.github.pawelzielinski.catantimer.core.domain.onSuccess

interface GameSessionCoordinator {
    /** The currently active session, or null when no game is in progress. */
    val currentSession: StateFlow<GameSession?>

    /** Initializes a session for [gameId], restoring existing turns or creating the first one. */
    suspend fun startSession(gameId: Long): EmptyResult<DataError.Local>

    /** Marks the current game as finished and clears the session. */
    suspend fun finishSession(finishedAt: Long, winnerId: Long?): EmptyResult<DataError.Local>

    /**
     * Completes the active turn with the given duration and creates the next turn.
     * Returns [IllegalOperationError] if called while a historical turn is selected.
     */
    suspend fun completeTurn(durationMillis: Long): Result<Unit, Error>

    /** Updates dice rolls for the currently selected turn and persists immediately. */
    suspend fun updateSelectedTurnDice(redDice: Int?, yellowDice: Int?, eventDice: EventDiceType?): EmptyResult<DataError.Local>

    /** Updates duration for the currently selected turn and persists immediately. */
    suspend fun updateSelectedTurnDuration(durationMillis: Long): EmptyResult<DataError.Local>

    /** Updates dice rolls for any turn (including historical ones) and syncs all in-memory copies. */
    suspend fun updateTurnDice(turn: Turn, redDice: Int?, yellowDice: Int?, eventDice: EventDiceType?): EmptyResult<DataError.Local>
}

class DefaultGameSessionCoordinator(
    private val gameRepository: GameRepository,
    private val turnRepository: TurnRepository
) : GameSessionCoordinator {
    private val _currentSession = MutableStateFlow<GameSession?>(null)
    override val currentSession: StateFlow<GameSession?> = _currentSession.asStateFlow()

    /**
     * Initializes a session for the given gameId.
     * Handles both new games (no turns in db) and session restoration (existing turns).
     * Clears any previous session state before initializing.
     */
    override suspend fun startSession(gameId: Long): EmptyResult<DataError.Local> {
        clearSession()

        val game = try {
            gameRepository.getGameById(gameId).first()
        } catch (e: Exception) {
            return Result.Failure(DataError.Local.NOT_FOUND)
        } ?: return Result.Failure(DataError.Local.NOT_FOUND)

        val turns = try {
            turnRepository.getTurnsForGame(gameId).first()
        } catch (e: Exception) {
            return Result.Failure(DataError.Local.UNKNOWN)
        }

        val currentTurn = if (turns.isEmpty()) {
            val template = TurnFactory.createFirst(game)
            val turnId = turnRepository.createTurn(gameId, template.playerId, template.number)
                .onFailure { return Result.Failure(it) }
                .let { (it as Result.Success).data }
            if (template.secondaryPlayerId != null) {
                turnRepository.setSecondaryPlayer(turnId, template.secondaryPlayerId)
                    .onFailure { return Result.Failure(it) }
            }
            template.copy(id = turnId)
        } else {
            turns.last()
        }

        val recentTurns = turns
            .dropLast(1)
            .takeLast(RECENT_TURNS_LIMIT)

        _currentSession.update {
            GameSession(
                game = game,
                latestTurn = currentTurn,
                selectedTurn = currentTurn,
                recentTurns = recentTurns
            )
        }

        return Result.Success(Unit)
    }

    /**
     * Marks the current game as finished and clears the session.
     */
    override suspend fun finishSession(finishedAt: Long, winnerId: Long?): EmptyResult<DataError.Local> {
        val gameId = _currentSession.value?.game?.id
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        return gameRepository.endGame(gameId, winnerId = winnerId)
            .onSuccess { clearSession() }
    }

    /**
     * Completes the currently active turn with the given duration,
     * then creates and persists the next turn.
     * Returns IllegalOperationError if called while user is viewing a historical turn —
     * UI is responsible for preventing this from happening.
     */
    override suspend fun completeTurn(durationMillis: Long): Result<Unit, Error> {
        val session = _currentSession.value
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        if (!session.isActiveTurnSelected) {
            return Result.Failure(IllegalOperationError)
        }
        val gameId = session.game.id

        val completedTurn = session.latestTurn.copy(durationMillis = durationMillis)
        turnRepository.updateTurn(completedTurn)
            .onFailure { return Result.Failure(it) }

        val nextTemplate = TurnFactory.createNext(completedTurn, session.game)
        val nextTurnId = turnRepository.createTurn(gameId, nextTemplate.playerId, nextTemplate.number)
            .onFailure { return Result.Failure(it) }
            .let { (it as Result.Success).data }
        if (nextTemplate.secondaryPlayerId != null) {
            turnRepository.setSecondaryPlayer(nextTurnId, nextTemplate.secondaryPlayerId)
                .onFailure { return Result.Failure(it) }
        }
        val nextTurnWithId = nextTemplate.copy(id = nextTurnId)

        val updatedRecentTurns = (session.recentTurns + completedTurn)
            .takeLast(RECENT_TURNS_LIMIT)

        _currentSession.update {
            session.copy(
                latestTurn = nextTurnWithId,
                selectedTurn = nextTurnWithId,
                recentTurns = updatedRecentTurns
            )
        }

        return Result.Success(Unit)
    }

    /**
     * Updates dice rolls for the currently selected turn and persists immediately.
     */
    override suspend fun updateSelectedTurnDice(
        redDice: Int?,
        yellowDice: Int?,
        eventDice: EventDiceType?
    ): EmptyResult<DataError.Local> {
        val session = _currentSession.value
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        val updatedTurn = session.selectedTurn.copy(
            redDice = redDice,
            yellowDice = yellowDice,
            eventDice = eventDice
        )

        return turnRepository.updateTurn(updatedTurn)
            .onSuccess {
                _currentSession.update { session ->
                    session?.let { s ->
                        val newLatest = if (s.latestTurn.id == updatedTurn.id) updatedTurn else s.latestTurn
                        s.copy(selectedTurn = updatedTurn, latestTurn = newLatest)
                    }
                }
            }
    }

    /**
     * Updates duration for the currently selected turn and persists immediately.
     */
    override suspend fun updateSelectedTurnDuration(durationMillis: Long): EmptyResult<DataError.Local> {
        val session = _currentSession.value
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        val updatedTurn = session.selectedTurn.copy(durationMillis = durationMillis)

        return turnRepository.updateTurn(updatedTurn)
            .onSuccess {
                _currentSession.update { it?.copy(selectedTurn = updatedTurn) }
            }
    }

    /**
     * Updates dice rolls for any turn (including historical ones) and syncs all in-memory copies.
     */
    override suspend fun updateTurnDice(
        turn: Turn,
        redDice: Int?,
        yellowDice: Int?,
        eventDice: EventDiceType?
    ): EmptyResult<DataError.Local> {
        val session = _currentSession.value
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        val updatedTurn = turn.copy(redDice = redDice, yellowDice = yellowDice, eventDice = eventDice)

        return turnRepository.updateTurn(updatedTurn).onSuccess {
            _currentSession.update { s ->
                s?.let {
                    val newLatest = if (it.latestTurn.id == updatedTurn.id) updatedTurn else it.latestTurn
                    val newRecent = it.recentTurns.map { t -> if (t.id == updatedTurn.id) updatedTurn else t }
                    val newSelected = if (it.selectedTurn.id == updatedTurn.id) updatedTurn else it.selectedTurn
                    it.copy(latestTurn = newLatest, recentTurns = newRecent, selectedTurn = newSelected)
                }
            }
        }
    }

    private fun clearSession() {
        _currentSession.update { null }
    }

    companion object {
        private const val RECENT_TURNS_LIMIT = 3
    }
}
