package io.github.pawelzielinski.catantimer.core.presentation

import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.error_unknown
import catantimer.composeapp.generated.resources.validation_duplicate_players
import catantimer.composeapp.generated.resources.validation_in_between_requires_5
import catantimer.composeapp.generated.resources.validation_player_count
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.error.GameValidationError
import io.github.pawelzielinski.catantimer.core.util.UiText

fun GameValidationError.toUiText(): UiText = UiText.StringResourceId(
    when (this) {
        GameValidationError.InvalidPlayerCount -> Res.string.validation_player_count
        GameValidationError.DuplicatePlayers -> Res.string.validation_duplicate_players
        GameValidationError.SpecialTurnRuleRequiresFivePlayers -> Res.string.validation_in_between_requires_5
        GameValidationError.StorageError -> Res.string.error_unknown
    }
)
