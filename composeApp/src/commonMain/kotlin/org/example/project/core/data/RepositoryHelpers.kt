package org.example.project.core.data

import androidx.sqlite.SQLiteException
import org.example.project.core.domain.DataError
import org.example.project.core.domain.Result
import org.example.project.core.util.LogUtils

private const val TAG = "Repository"

suspend fun <T> tryLocalRead(block: suspend () -> Result<T, DataError.Local>): Result<T, DataError.Local> =
    try {
        block()
    } catch (e: SQLiteException) {
        LogUtils.error(message = "SQLiteException during read: ${e.message}", tag = TAG, throwable = e)
        Result.Failure(DataError.Local.UNKNOWN)
    } catch (e: Exception) {
        LogUtils.error(message = "Unexpected exception during read: ${e.message}", tag = TAG, throwable = e)
        Result.Failure(DataError.Local.UNKNOWN)
    }

suspend fun <T> tryLocalWrite(block: suspend () -> Result<T, DataError.Local>): Result<T, DataError.Local> =
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
        LogUtils.error(message = "Unexpected exception during write: ${e.message}", tag = TAG, throwable = e)
        Result.Failure(DataError.Local.UNKNOWN)
    }