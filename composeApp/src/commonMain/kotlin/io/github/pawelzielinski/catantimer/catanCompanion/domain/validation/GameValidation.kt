package io.github.pawelzielinski.catantimer.catanCompanion.domain.validation

import io.github.pawelzielinski.catantimer.catanCompanion.domain.error.GameValidationError

fun validateSpecialTurnRule(playerCount: Int, specialTurnRuleEnabled: Boolean): GameValidationError? =
    if (specialTurnRuleEnabled && playerCount < 5)
        GameValidationError.InsufficientPlayersForSpecialTurnRule
    else null
