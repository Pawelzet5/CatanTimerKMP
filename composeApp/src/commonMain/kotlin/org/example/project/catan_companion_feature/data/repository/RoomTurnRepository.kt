package io.github.pawelzielinski.catantimer.catan_companion_feature.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.dao.PlayerDao
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.dao.TurnDao
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.entity.TurnEntity
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.mapper.toDomain
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.mapper.toEntity
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Turn
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.EventDiceType
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.repository.TurnRepository
import io.github.pawelzielinski.catantimer.core.data.tryLocalRead
import io.github.pawelzielinski.catantimer.core.data.tryLocalWrite
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Result

class RoomTurnRepository(
    private val turnDao: TurnDao,
    private val playerDao: PlayerDao
) : TurnRepository {

    override fun getTurnsForGame(gameId: Long): Flow<List<Turn>> =
        turnDao.getForGame(gameId).map { entities ->
            entities.map { entity -> entity.toDomainWithPlayerName() }
        }

    override fun getTurnById(id: Long): Flow<Turn?> =
        turnDao.getById(id).map { entity -> entity?.toDomainWithPlayerName() }

    override fun getCurrentTurn(gameId: Long): Flow<Turn?> =
        turnDao.getCurrentForGame(gameId).map { entity -> entity?.toDomainWithPlayerName() }

    override suspend fun createTurn(
        gameId: Long,
        playerId: Long,
        number: Int
    ): Result<Long, DataError.Local> = tryLocalWrite {
        val id = turnDao.insert(
            TurnEntity(gameId = gameId, number = number, playerId = playerId)
        )
        Result.Success(id)
    }

    override suspend fun updateTurn(turn: Turn): EmptyResult<DataError.Local> = tryLocalWrite {
        turnDao.update(turn.toEntity())
        Result.Success(Unit)
    }

    override suspend fun updateDiceRoll(
        turnId: Long,
        redDice: Int,
        yellowDice: Int,
        eventDice: EventDiceType?
    ): EmptyResult<DataError.Local> = tryLocalWrite {
        val entity = turnDao.getById(turnId).first()
            ?: return@tryLocalWrite Result.Failure(DataError.Local.NOT_FOUND)
        turnDao.update(entity.copy(redDice = redDice, yellowDice = yellowDice, eventDice = eventDice))
        Result.Success(Unit)
    }

    override suspend fun updateDuration(
        turnId: Long,
        durationMillis: Long
    ): EmptyResult<DataError.Local> = tryLocalWrite {
        val entity = turnDao.getById(turnId).first()
            ?: return@tryLocalWrite Result.Failure(DataError.Local.NOT_FOUND)
        turnDao.update(entity.copy(durationMillis = durationMillis))
        Result.Success(Unit)
    }

    override suspend fun setSecondaryPlayer(
        turnId: Long,
        playerId: Long
    ): EmptyResult<DataError.Local> = tryLocalWrite {
        val entity = turnDao.getById(turnId).first()
            ?: return@tryLocalWrite Result.Failure(DataError.Local.NOT_FOUND)
        turnDao.update(entity.copy(secondaryPlayerId = playerId))
        Result.Success(Unit)
    }

    private suspend fun TurnEntity.toDomainWithPlayerName(): Turn {
        val playerName = playerDao.getByIdOnce(playerId)?.name ?: ""
        val secondaryPlayerName = secondaryPlayerId?.let {
            playerDao.getByIdOnce(it)?.name
        }
        return toDomain(playerName = playerName, secondaryPlayerName = secondaryPlayerName)
    }
}
