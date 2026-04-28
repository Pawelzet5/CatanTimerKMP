package io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase

import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.catanCompanion.domain.error.GameValidationError
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.GameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.validation.validateSpecialTurnRule
import io.github.pawelzielinski.catantimer.core.domain.Result

class CreateGameUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        playerIds: List<Long>
    ): Result<Long, GameValidationError> {
        if (playerIds.size !in 3..6) {
            return Result.Failure(GameValidationError.InvalidPlayerCount)
        }
        if (playerIds.distinct().size != playerIds.size) {
            return Result.Failure(GameValidationError.DuplicatePlayers)
        }
        validateSpecialTurnRule(playerIds.size, specialTurnRuleEnabled)
            ?.let { return Result.Failure(it) }
        return when (val result = gameRepository.createGame(
            turnDurationMillis = turnDurationMillis,
            expansions = expansions,
            specialTurnRuleEnabled = specialTurnRuleEnabled,
            playerIds = playerIds
        )) {
            is Result.Success -> Result.Success(result.data)
            is Result.Failure -> Result.Failure(GameValidationError.StorageError)
        }
    }
}
