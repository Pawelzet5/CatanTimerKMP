package io.github.pawelzielinski.catantimer.catan_companion_feature.domain.error

import io.github.pawelzielinski.catantimer.core.domain.Error

sealed interface GameValidationError : Error {
    data object InvalidPlayerCount : GameValidationError
    data object DuplicatePlayers : GameValidationError
    data object SpecialTurnRuleRequiresFivePlayers : GameValidationError
    data object StorageError : GameValidationError
}
