package org.example.project.catan_companion_feature.data.fakes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.session.GameSession
import org.example.project.catan_companion_feature.domain.session.GameSessionCoordinator
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Error
import org.example.project.core.domain.Result

class FakeGameSessionCoordinator : GameSessionCoordinator {

    private val _session = MutableStateFlow<GameSession?>(null)
    override val currentSession: StateFlow<GameSession?> = _session.asStateFlow()

    fun setSession(session: GameSession?) {
        _session.value = session
    }

    override suspend fun startSession(gameId: Long): EmptyResult<DataError.Local> =
        Result.Success(Unit)

    override suspend fun completeTurn(durationMillis: Long): Result<Unit, Error> =
        Result.Success(Unit)

    override suspend fun finishSession(finishedAt: Long, winnerId: Long?): EmptyResult<DataError.Local> =
        Result.Success(Unit)

    override suspend fun updateSelectedTurnDice(
        redDice: Int?,
        yellowDice: Int?,
        eventDice: EventDiceType?
    ): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun updateSelectedTurnDuration(durationMillis: Long): EmptyResult<DataError.Local> =
        Result.Success(Unit)
}
