package org.example.project.catan_companion_feature.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.factory.TurnFactory
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.domain.repository.TurnRepository
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Error
import org.example.project.core.domain.IllegalOperationError
import org.example.project.core.domain.Result
import org.example.project.core.domain.onFailure
import org.example.project.core.domain.onSuccess

interface GameSessionCoordinator {
    val currentSession: StateFlow<GameSession?>
    suspend fun startSession(gameId: Long): EmptyResult<DataError.Local>
    suspend fun finishSession(finishedAt: Long, winnerId: Long?): EmptyResult<DataError.Local>
    suspend fun completeTurn(durationMillis: Long): Result<Unit, Error>
    suspend fun updateSelectedTurnDice(redDice: Int?, yellowDice: Int?, eventDice: EventDiceType?): EmptyResult<DataError.Local>
    suspend fun updateSelectedTurnDuration(durationMillis: Long): EmptyResult<DataError.Local>
}

class GameSessionCoordinatorImpl(
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
                _currentSession.update { it?.copy(selectedTurn = updatedTurn) }
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

    private fun clearSession() {
        _currentSession.update { null }
    }

    companion object {
        private const val RECENT_TURNS_LIMIT = 3
    }
}
