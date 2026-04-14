package org.example.project.catan_companion_feature.data.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.data.local.dao.TurnDao
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.repository.TurnRepository
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

class TurnRepositoryImpl(
    private val turnDao: TurnDao
) : TurnRepository {

    override fun getTurnsForGame(gameId: Long): Flow<List<Turn>> = TODO("Implemented in session-3/repository-impls")

    override fun getTurnById(id: Long): Flow<Turn?> = TODO("Implemented in session-3/repository-impls")

    override fun getCurrentTurn(gameId: Long): Flow<Turn?> = TODO("Implemented in session-3/repository-impls")

    override suspend fun createTurn(gameId: Long, playerId: Long, number: Int): Result<Long, DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun updateTurn(turn: Turn): EmptyResult<DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun updateDiceRoll(turnId: Long, redDice: Int, yellowDice: Int, eventDice: EventDiceType?): EmptyResult<DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun updateDuration(turnId: Long, durationMillis: Long): EmptyResult<DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun setSecondaryPlayer(turnId: Long, playerId: Long): EmptyResult<DataError.Local> = TODO("Implemented in session-3/repository-impls")
}
