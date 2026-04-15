package org.example.project.core.data

import androidx.sqlite.SQLiteException
import kotlinx.coroutines.test.runTest
import org.example.project.core.domain.DataError
import org.example.project.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RepositoryHelpersTest {

    // region tryLocalRead
    @Test
    fun `tryLocalRead, block succeeds, returns block result`() = runTest {
        // WHEN
        val result = tryLocalRead {
            Result.Success(42)
        }

        // THEN
        assertIs<Result.Success<Int>>(result)
        assertEquals(42, result.data)
    }

    @Test
    fun `tryLocalRead, block returns Failure, propagates Failure without catching`() = runTest {
        // WHEN
        val result = tryLocalRead {
            Result.Failure(DataError.Local.NOT_FOUND)
        }

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `tryLocalRead, block throws SQLiteException, returns UNKNOWN error`() = runTest {
        // WHEN
        val result = tryLocalRead<Unit> {
            throw SQLiteException("disk error")
        }

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    @Test
    fun `tryLocalRead, unexpected exception thrown, returns UNKNOWN error`() = runTest {
        // WHEN
        val result = tryLocalRead<Unit> {
            throw RuntimeException("something went wrong")
        }

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion

    // region tryLocalWrite

    @Test
    fun `tryLocalWrite, block succeeds, returns block result`() = runTest {
        // WHEN
        val result = tryLocalWrite {
            Result.Success(99L)
        }

        // THEN
        assertIs<Result.Success<Long>>(result)
        assertEquals(99L, result.data)
    }

    @Test
    fun `tryLocalWrite, block returns Failure, propagates Failure without catching`() = runTest {
        // WHEN
        val result = tryLocalWrite {
            Result.Failure(DataError.Local.NOT_FOUND)
        }

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `tryLocalWrite, block throws SQLiteException, returns DISK_FULL error`() = runTest {
        // WHEN
        val result = tryLocalWrite<Unit> {
            throw SQLiteException("no space left")
        }

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `tryLocalWrite, unexpected exception thrown, returns UNKNOWN error`() = runTest {
        // WHEN
        val result = tryLocalWrite<Unit> {
            throw RuntimeException("something went wrong")
        }

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion

    // region read vs write contract

    @Test
    fun `tryLocalRead and tryLocalWrite, SQLiteException thrown, map to different errors`() = runTest {
        // Dokumentuje świadomą decyzję projektową: read → UNKNOWN, write → DISK_FULL
        val readResult = tryLocalRead<Unit> { throw SQLiteException("error") }
        val writeResult = tryLocalWrite<Unit> { throw SQLiteException("error") }

        assertEquals(DataError.Local.UNKNOWN, (readResult as Result.Failure).error)
        assertEquals(DataError.Local.DISK_FULL, (writeResult as Result.Failure).error)
    }

    // endregion
}