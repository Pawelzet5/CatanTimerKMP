package io.github.pawelzielinski.catantimer.core.data

interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}
