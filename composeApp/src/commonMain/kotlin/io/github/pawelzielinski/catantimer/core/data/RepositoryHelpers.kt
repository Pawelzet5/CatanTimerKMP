package io.github.pawelzielinski.catantimer.core.data

import androidx.sqlite.SQLiteException
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.Result
import io.github.pawelzielinski.catantimer.core.util.LogUtils

private const val TAG = "Repository"

suspend fun <T> tryLocalRead(tag: String = TAG, block: suspend () -> Result<T, DataError.Local>): Result<T, DataError.Local> =
    try {
        block()
    } catch (e: SQLiteException) {
        LogUtils.error(message = "SQLiteException during read: ${e.message}", tag = tag, throwable = e)
        Result.Failure(DataError.Local.UNKNOWN)
    } catch (e: Exception) {
        LogUtils.error(message = "Unexpected exception during read: ${e.message}", tag = tag, throwable = e)
        Result.Failure(DataError.Local.UNKNOWN)
    }

suspend fun <T> tryLocalWrite(tag: String = TAG, block: suspend () -> Result<T, DataError.Local>): Result<T, DataError.Local> =
    try {
        block()
    } catch (e: SQLiteException) {
        LogUtils.error(
            message = "SQLiteException during write: ${e.message}",
            tag = TAG,
            throwable = e
        )
        Result.Failure(DataError.Local.DISK_FULL)
    } catch (e: Exception) {
        LogUtils.error(message = "Unexpected exception during write: ${e.message}", tag = tag, throwable = e)
        Result.Failure(DataError.Local.UNKNOWN)
    }