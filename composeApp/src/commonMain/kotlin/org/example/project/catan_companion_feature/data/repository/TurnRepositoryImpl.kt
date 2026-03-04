package org.example.project.catan_companion_feature.data.repository

import org.example.project.catan_companion_feature.data.local.dao.TurnDao
import org.example.project.catan_companion_feature.data.local.mapper.toDomain
import org.example.project.catan_companion_feature.data.local.mapper.toEntity
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.repository.TurnRepository
import org.example.project.core.data.tryLocalRead
import org.example.project.core.data.tryLocalWrite
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

class TurnRepositoryImpl(
    private val turnDao: TurnDao
) : TurnRepository {

    override suspend fun addTurn(gameId: Long, turn: Turn): Result<Long, DataError.Local> =
        tryLocalWrite {
            Result.Success(turnDao.insertTurn(turn.toEntity(gameId)))
        }

    override suspend fun updateTurn(gameId: Long, turn: Turn): EmptyResult<DataError.Local> =
        tryLocalWrite {
            val updated = turnDao.updateTurn(turn.toEntity(gameId))
            if (updated == 0) return@tryLocalWrite Result.Failure(DataError.Local.NOT_FOUND)
            Result.Success(Unit)
        }

    override suspend fun getTurnsForGame(gameId: Long): Result<List<Turn>, DataError.Local> =
        tryLocalRead {
            Result.Success(turnDao.getTurnsForGame(gameId).map { it.toDomain() })
        }
}