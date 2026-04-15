package org.example.project.catan_companion_feature.domain.usecase

import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.makeTestGame
import org.example.project.catan_companion_feature.makeTestGamePlayers
import org.example.project.catan_companion_feature.makeTestTurn
import org.example.project.catan_companion_feature.data.fakes.repository.FakeGameRepository
import org.example.project.catan_companion_feature.data.fakes.repository.FakeTurnRepository
import org.example.project.catan_companion_feature.domain.dataclass.GameStatistics
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.core.domain.DataError
import org.example.project.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetGameStatisticsUseCaseTest {

    private val fakeGameRepository = FakeGameRepository()
    private val fakeTurnRepository = FakeTurnRepository()
    private val useCase = GetGameStatisticsUseCase(fakeGameRepository, fakeTurnRepository)

    @Test
    fun `empty turn list returns zeroed statistics without crash`() = runTest {
        val game = makeTestGame(id = 1L)
        fakeGameRepository.seedGame(game)

        val result = useCase(gameId = 1L)

        assertIs<Result.Success<GameStatistics>>(result)
        assertEquals(0, result.data.totalTurns)
        assertEquals(0L, result.data.averageTurnDurationMillis)
        assertEquals(emptyMap(), result.data.diceDistribution.counts)
        assertEquals(emptyMap(), result.data.eventDiceDistribution)
    }

    @Test
    fun `dice distribution groups correctly by sum`() = runTest {
        val game = makeTestGame(id = 1L)
        fakeGameRepository.seedGame(game)
        val turns = listOf(
            makeTestTurn(id = 1L, gameId = 1L, redDice = 3, yellowDice = 4),  // sum = 7
            makeTestTurn(id = 2L, gameId = 1L, redDice = 5, yellowDice = 2),  // sum = 7
            makeTestTurn(id = 3L, gameId = 1L, redDice = 3, yellowDice = 4)   // sum = 7
        )
        fakeTurnRepository.seedTurns(1L, *turns.toTypedArray())

        val result = useCase(gameId = 1L)

        assertIs<Result.Success<GameStatistics>>(result)
        assertEquals(3, result.data.diceDistribution.counts[7])
    }

    @Test
    fun `average duration calculated correctly`() = runTest {
        val game = makeTestGame(id = 1L)
        fakeGameRepository.seedGame(game)
        val turns = listOf(
            makeTestTurn(id = 1L, gameId = 1L, durationMillis = 60_000L),
            makeTestTurn(id = 2L, gameId = 1L, durationMillis = 90_000L),
            makeTestTurn(id = 3L, gameId = 1L, durationMillis = 120_000L)
        )
        fakeTurnRepository.seedTurns(1L, *turns.toTypedArray())

        val result = useCase(gameId = 1L)

        assertIs<Result.Success<GameStatistics>>(result)
        assertEquals(90_000L, result.data.averageTurnDurationMillis)
    }

    @Test
    fun `turns without dice rolls excluded from distribution`() = runTest {
        val game = makeTestGame(id = 1L)
        fakeGameRepository.seedGame(game)
        val turns = listOf(
            makeTestTurn(id = 1L, gameId = 1L, redDice = null, yellowDice = null),
            makeTestTurn(id = 2L, gameId = 1L, redDice = 3, yellowDice = 4)
        )
        fakeTurnRepository.seedTurns(1L, *turns.toTypedArray())

        val result = useCase(gameId = 1L)

        assertIs<Result.Success<GameStatistics>>(result)
        assertEquals(1, result.data.diceDistribution.counts.size)
        assertEquals(1, result.data.diceDistribution.counts[7])
    }

    @Test
    fun `game not found returns failure`() = runTest {
        val result = useCase(gameId = 999L)

        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `player average durations calculated per player`() = runTest {
        val players = makeTestGamePlayers(count = 2, gameId = 1L)
        val game = makeTestGame(id = 1L, players = players)
        fakeGameRepository.seedGame(game)
        val turns = listOf(
            makeTestTurn(id = 1L, gameId = 1L, playerId = 1L, durationMillis = 60_000L),
            makeTestTurn(id = 2L, gameId = 1L, playerId = 1L, durationMillis = 120_000L),
            makeTestTurn(id = 3L, gameId = 1L, playerId = 2L, durationMillis = 30_000L)
        )
        fakeTurnRepository.seedTurns(1L, *turns.toTypedArray())

        val result = useCase(gameId = 1L)

        assertIs<Result.Success<GameStatistics>>(result)
        assertEquals(90_000L, result.data.playerAverageTurnDurations[1L])
        assertEquals(30_000L, result.data.playerAverageTurnDurations[2L])
    }
}
