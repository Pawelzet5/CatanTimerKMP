package io.github.pawelzielinski.catantimer.core.domain

sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Failure<out E: Error>(val error: E): Result<Nothing, E>
}

inline fun <T, E: Error> Result<T,E>.onSuccess(action: (T) -> Unit): Result<T,E> {
    return when(this) {
        is Result.Failure -> this
        is Result.Success -> {
            action(this.data)
            this
        }
    }
}
inline fun <T, E: Error> Result<T,E>.onFailure(action: (E) -> Unit): Result<T,E> {
    return when(this) {
        is Result.Failure -> {
            action(this.error)
            this
        }
        is Result.Success -> this
    }
}
typealias EmptyResult<E> = Result<Unit, E>