package io.github.pawelzielinski.catantimer.core.presentation

import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.error_disk_full
import catantimer.composeapp.generated.resources.error_local_not_found
import catantimer.composeapp.generated.resources.error_unknown
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.util.UiText

fun DataError.Local.toErrorMessage(): UiText = UiText.StringResourceId(
    when (this) {
        DataError.Local.DISK_FULL -> Res.string.error_disk_full
        DataError.Local.NOT_FOUND -> Res.string.error_local_not_found
        DataError.Local.UNKNOWN -> Res.string.error_unknown
    }
)
