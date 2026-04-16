package org.example.project.catan_companion_feature.domain.usecase

import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.error.GameValidationError
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.core.domain.Result

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
        if (specialTurnRuleEnabled && playerIds.size < 5) {
            return Result.Failure(GameValidationError.SpecialTurnRuleRequiresFivePlayers)
        }
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
