package io.github.pawelzielinski.catantimer.core.presentation

import catantimer.composeapp.generated.resources.Res
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.util.UiText
import catantimer.composeapp.generated.resources.error_bad_request
import catantimer.composeapp.generated.resources.error_conflict
import catantimer.composeapp.generated.resources.error_disk_full
import catantimer.composeapp.generated.resources.error_forbidden
import catantimer.composeapp.generated.resources.error_local_not_found
import catantimer.composeapp.generated.resources.error_no_internet
import catantimer.composeapp.generated.resources.error_not_found
import catantimer.composeapp.generated.resources.error_request_timeout
import catantimer.composeapp.generated.resources.error_server
import catantimer.composeapp.generated.resources.error_too_many_requests
import catantimer.composeapp.generated.resources.error_unauthorized
import catantimer.composeapp.generated.resources.error_unknown


fun DataError.toErrorMessage(): UiText {
    return UiText.StringResourceId(
        when (this) {
            DataError.Local.DISK_FULL -> Res.string.error_disk_full
            DataError.Local.NOT_FOUND -> Res.string.error_local_not_found
            DataError.Local.UNKNOWN -> Res.string.error_unknown

            DataError.Remote.BAD_REQUEST -> Res.string.error_bad_request
            DataError.Remote.REQUEST_TIMEOUT -> Res.string.error_request_timeout
            DataError.Remote.UNAUTHORIZED -> Res.string.error_unauthorized
            DataError.Remote.FORBIDDEN -> Res.string.error_forbidden
            DataError.Remote.NOT_FOUND -> Res.string.error_not_found
            DataError.Remote.CONFLICT -> Res.string.error_conflict
            DataError.Remote.TOO_MANY_REQUEST -> Res.string.error_too_many_requests
            DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
            DataError.Remote.SERVER_ERROR -> Res.string.error_server
            DataError.Remote.UNKNOWN -> Res.string.error_unknown
        }
    )
}
