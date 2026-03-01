package org.example.project.catan_companion_feature.domain

import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Error
import org.example.project.core.domain.IllegalOperationError
import org.example.project.core.domain.Result
import org.example.project.core.domain.onFailure
import org.example.project.core.domain.onSuccess
import org.example.project.catan_companion_feature.domain.dataclass.GameSession
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.factory.TurnFactory
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.domain.repository.TurnRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.catan_companion_feature.domain.dataclass.Turn

class GameSessionCoordinator(
    private val gameRepository: GameRepository,
    private val turnRepository: TurnRepository
) {
    private val _currentSession = MutableStateFlow<GameSession?>(null)
    val currentSession: StateFlow<GameSession?> = _currentSession.asStateFlow()

    /**
     * Initializes a session for the given gameId.
     * Handles both new games (no turns in db) and session restoration (existing turns).
     * Clears any previous session state before initializing.
     */
    suspend fun startSession(gameId: Long): EmptyResult<DataError.Local> {
        clearSession()

        val game = gameRepository.getGame(gameId)
            .onFailure { return Result.Failure(it) }
            .let { (it as Result.Success).data }

        val turns = turnRepository.getTurnsForGame(gameId)
            .onFailure { return Result.Failure(it) }
            .let { (it as Result.Success).data }

        val currentTurn = if (turns.isEmpty()) {
            val initialTurn = TurnFactory.createFirst(game.config)
            val initialTurnId = turnRepository.addTurn(initialTurn)
                .onFailure { return Result.Failure(it) }
                .let { (it as Result.Success).data }
            initialTurn.copy(id = initialTurnId)
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
    suspend fun finishSession(): EmptyResult<DataError.Local> {
        val gameId = _currentSession.value?.game?.id
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        return gameRepository.saveGameAsFinished(gameId)
            .onSuccess { clearSession() }
    }

    /**
     * Completes the currently active turn with the given duration,
     * then creates and persists the next turn.
     * Returns IllegalOperationError if called while user is viewing a historical turn –
     * UI is responsible for preventing this from happening.
     */
    suspend fun completeTurn(durationMillis: Long): Result<Unit, Error> {
        val session = _currentSession.value
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        if (!session.isActiveTurnSelected) {
            return Result.Failure(IllegalOperationError)
        }

        val activeTurn = session.latestTurn

        val completedTurn = activeTurn.copy(durationMillis = durationMillis)
        turnRepository.updateTurn(completedTurn)
            .onFailure { return Result.Failure(it) }

        val nextTurn = TurnFactory.createNext(completedTurn, session.game.config)
        val nextTurnId = turnRepository.addTurn(nextTurn)
            .onFailure { return Result.Failure(it) }
            .let { (it as Result.Success).data }
        val nextTurnWithId = nextTurn.copy(id = nextTurnId)

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
     * Updates the selectedTurn in-memory only – no db operation.
     * Used for navigating between turns in UI.
     */
    fun selectTurn(turn: Turn) {
        _currentSession.update { it?.copy(selectedTurn = turn) }
    }

    /**
     * Restores selectedTurn to the currently active turn (last turn in db).
     * Used when user navigates back to the current turn from history.
     */
    fun selectActiveTurn(){
        _currentSession.update { it?.copy(selectedTurn = it.latestTurn) }
    }

    /**
     * Updates dice rolls for the currently selected turn and persists immediately.
     */
    suspend fun updateSelectedTurnDice(
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
    suspend fun updateSelectedTurnDuration(durationMillis: Long): EmptyResult<DataError.Local> {
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