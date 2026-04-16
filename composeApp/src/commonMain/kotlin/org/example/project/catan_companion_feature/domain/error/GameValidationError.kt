package org.example.project.catan_companion_feature.domain.error

import org.example.project.core.domain.Error

sealed interface GameValidationError : Error {
    data object InvalidPlayerCount : GameValidationError
    data object DuplicatePlayers : GameValidationError
    data object SpecialTurnRuleRequiresFivePlayers : GameValidationError
    data object StorageError : GameValidationError
}
