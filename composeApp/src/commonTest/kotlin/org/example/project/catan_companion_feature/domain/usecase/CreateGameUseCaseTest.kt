package org.example.project.catan_companion_feature.domain.usecase

import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.data.fakes.repository.FakeGameRepository
import org.example.project.core.domain.IllegalOperationError
import org.example.project.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CreateGameUseCaseTest {

    private val fakeGameRepository = FakeGameRepository()
    private val useCase = CreateGameUseCase(fakeGameRepository)

    @Test
    fun `Game creation, valid input, returns success with game id`() = runTest {
        val result = useCase(
            turnDurationMillis = 180_000L,
            expansions = emptySet(),
            specialTurnRuleEnabled = false,
            playerIds = listOf(1L, 2L, 3L)
        )
        assertIs<Result.Success<Long>>(result)
        assertTrue(result.data > 0L)
    }

    @Test
    fun `Game creation, fewer than 3 players, returns failure`() = runTest {
        val result = useCase(
            turnDurationMillis = 180_000L,
            expansions = emptySet(),
            specialTurnRuleEnabled = false,
            playerIds = listOf(1L, 2L)
        )
        assertIs<Result.Failure<IllegalOperationError>>(result)
    }

    @Test
    fun `Game creation, more than 6 players, returns failure`() = runTest {
        val result = useCase(
            turnDurationMillis = 180_000L,
            expansions = emptySet(),
            specialTurnRuleEnabled = false,
            playerIds = listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L)
        )
        assertIs<Result.Failure<IllegalOperationError>>(result)
    }

    @Test
    fun `Game creation, duplicate player ids, returns failure`() = runTest {
        val result = useCase(
            turnDurationMillis = 180_000L,
            expansions = emptySet(),
            specialTurnRuleEnabled = false,
            playerIds = listOf(1L, 2L, 2L)
        )
        assertIs<Result.Failure<IllegalOperationError>>(result)
    }

    @Test
    fun `Game creation, specialTurnRule enabled with 4 players, returns failure`() = runTest {
        val result = useCase(
            turnDurationMillis = 180_000L,
            expansions = emptySet(),
            specialTurnRuleEnabled = true,
            playerIds = listOf(1L, 2L, 3L, 4L)
        )
        assertIs<Result.Failure<IllegalOperationError>>(result)
    }

    @Test
    fun `Game creation, specialTurnRule enabled with 5 players, returns success`() = runTest {
        val result = useCase(
            turnDurationMillis = 180_000L,
            expansions = emptySet(),
            specialTurnRuleEnabled = true,
            playerIds = listOf(1L, 2L, 3L, 4L, 5L)
        )
        assertIs<Result.Success<Long>>(result)
    }

    @Test
    fun `Game creation, exactly 3 players, returns success`() = runTest {
        val result = useCase(
            turnDurationMillis = 180_000L,
            expansions = emptySet(),
            specialTurnRuleEnabled = false,
            playerIds = listOf(1L, 2L, 3L)
        )
        assertIs<Result.Success<Long>>(result)
    }

    @Test
    fun `Game creation, exactly 6 players, returns success`() = runTest {
        val result = useCase(
            turnDurationMillis = 180_000L,
            expansions = emptySet(),
            specialTurnRuleEnabled = false,
            playerIds = listOf(1L, 2L, 3L, 4L, 5L, 6L)
        )
        assertIs<Result.Success<Long>>(result)
    }
}
