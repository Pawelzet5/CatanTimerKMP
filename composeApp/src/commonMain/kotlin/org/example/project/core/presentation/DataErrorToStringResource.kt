package org.example.project.core.presentation

import org.example.pantrywisecmp.core.domain.util.DataError
import org.example.pantrywisecmp.core.domain.util.UiText
import pantrywisecmp.composeapp.generated.resources.Res
import pantrywisecmp.composeapp.generated.resources.error_bad_request
import pantrywisecmp.composeapp.generated.resources.error_conflict
import pantrywisecmp.composeapp.generated.resources.error_disk_full
import pantrywisecmp.composeapp.generated.resources.error_forbidden
import pantrywisecmp.composeapp.generated.resources.error_local_not_found
import pantrywisecmp.composeapp.generated.resources.error_no_internet
import pantrywisecmp.composeapp.generated.resources.error_not_found
import pantrywisecmp.composeapp.generated.resources.error_request_timeout
import pantrywisecmp.composeapp.generated.resources.error_server
import pantrywisecmp.composeapp.generated.resources.error_too_many_requests
import pantrywisecmp.composeapp.generated.resources.error_unauthorized
import pantrywisecmp.composeapp.generated.resources.error_unknown


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
