package io.github.pawelzielinski.catantimer.catanCompanion.data.fakes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Turn
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.EventDiceType
import io.github.pawelzielinski.catantimer.catanCompanion.domain.session.GameSession
import io.github.pawelzielinski.catantimer.catanCompanion.domain.session.GameSessionCoordinator
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Error
import io.github.pawelzielinski.catantimer.core.domain.Result

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

    override suspend fun updateTurnDice(
        turn: Turn,
        redDice: Int?,
        yellowDice: Int?,
        eventDice: EventDiceType?
    ): EmptyResult<DataError.Local> = Result.Success(Unit)

}
