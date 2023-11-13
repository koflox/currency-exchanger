package com.koflox.currency_exchanger.util.network

import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class NetworkException : Exception() {
    class NoConnectivity : NetworkException()
    class ResponseDeserialization : NetworkException()
    class RequestTimeout : NetworkException()
}

inline fun <R> runRemoteDataSourceCatching(block: () -> R): R {
    return try {
        block()
    } catch (e: Throwable) {
        val exception = when (e) {
            is UnknownHostException -> NetworkException.NoConnectivity()
            is ClassCastException, is IllegalStateException -> NetworkException.ResponseDeserialization()
            is SocketTimeoutException -> NetworkException.RequestTimeout()
            else -> e
        }
        throw exception
    }
}
