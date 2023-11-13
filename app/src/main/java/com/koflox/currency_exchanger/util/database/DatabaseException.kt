package com.koflox.currency_exchanger.util.database

sealed class DatabaseException : Exception() {
    class NoData : DatabaseException()
}

inline fun <R> runLocalDataSourceCatching(block: () -> R): R {
    return try {
        block()
    } catch (e: Throwable) {
        when (e) {
            is NullPointerException -> throw DatabaseException.NoData()
            else -> throw e
        }
    }
}
