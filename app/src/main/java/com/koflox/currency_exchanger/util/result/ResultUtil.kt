package com.koflox.currency_exchanger.util.result

import kotlinx.coroutines.TimeoutCancellationException
import kotlin.coroutines.cancellation.CancellationException

inline fun <R> runSafeCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        val isCoroutineCancellation = e is CancellationException && e !is TimeoutCancellationException
        when {
            isCoroutineCancellation || e is RuntimeException
                || e is Error -> throw e

            else -> Result.failure(e)
        }
    }
}
