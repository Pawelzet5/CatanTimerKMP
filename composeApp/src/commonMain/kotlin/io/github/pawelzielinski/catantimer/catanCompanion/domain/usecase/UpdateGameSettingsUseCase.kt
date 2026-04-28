package io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase

import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.catanCompanion.domain.error.GameValidationError
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.GameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.validation.validateSpecialTurnRule
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Result

class UpdateGameSettingsUseCase(
    private val gameRepository: GameRepository
) {
    /** Validates the special-turn-rule constraint then persists the updated settings. */
    suspend operator fun invoke(
        gameId: Long,
        playerCount: Int,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean
    ): EmptyResult<GameValidationError> {
        validateSpecialTurnRule(playerCount, specialTurnRuleEnabled)
            ?.let { return Result.Failure(it) }
        return when (gameRepository.updateGameSettings(gameId, expansions, specialTurnRuleEnabled)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> Result.Failure(GameValidationError.StorageError)
        }
    }
}
