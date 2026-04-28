package io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase

import kotlinx.coroutines.test.runTest
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.repository.FakeGameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.catanCompanion.domain.error.GameValidationError
import io.github.pawelzielinski.catantimer.catanCompanion.testGame
import io.github.pawelzielinski.catantimer.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UpdateGameSettingsUseCaseTest {

    private val fakeGameRepository = FakeGameRepository()
    private val useCase = UpdateGameSettingsUseCase(fakeGameRepository)

    @Test
    fun `invoke, specialTurnRule enabled with sufficient players, returns success`() = runTest {
        // given
        fakeGameRepository.seedGame(testGame())

        // when
        val result = useCase(
            gameId = 1L,
            playerCount = 5,
            expansions = emptySet(),
            specialTurnRuleEnabled = true
        )

        // then
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `invoke, specialTurnRule enabled with insufficient players, returns InsufficientPlayersForSpecialTurnRule`() = runTest {
        // when
        val result = useCase(
            gameId = 1L,
            playerCount = 4,
            expansions = emptySet(),
            specialTurnRuleEnabled = true
        )

        // then
        assertIs<Result.Failure<GameValidationError>>(result)
        assertEquals(GameValidationError.InsufficientPlayersForSpecialTurnRule, result.error)
    }

    @Test
    fun `invoke, specialTurnRule disabled, persists regardless of player count`() = runTest {
        // given
        fakeGameRepository.seedGame(testGame())

        // when
        val result = useCase(
            gameId = 1L,
            playerCount = 3,
            expansions = emptySet(),
            specialTurnRuleEnabled = false
        )

        // then
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `invoke, game not found, returns StorageError`() = runTest {
        // when
        val result = useCase(
            gameId = 999L,
            playerCount = 5,
            expansions = emptySet(),
            specialTurnRuleEnabled = false
        )

        // then
        assertIs<Result.Failure<GameValidationError>>(result)
        assertEquals(GameValidationError.StorageError, result.error)
    }

    @Test
    fun `invoke, valid settings, updates expansions and specialTurnRule in repository`() = runTest {
        // given
        fakeGameRepository.seedGame(testGame())

        // when
        useCase(
            gameId = 1L,
            playerCount = 5,
            expansions = setOf(GameExpansion.SEAFARERS),
            specialTurnRuleEnabled = true
        )

        // then
        val updated = fakeGameRepository.games.find { it.id == 1L }
        assertEquals(setOf(GameExpansion.SEAFARERS), updated?.expansions)
        assertTrue(updated?.specialTurnRuleEnabled == true)
    }
}
