package com.koflox.currency_exchanger.data.entity.remote

internal data class ExchangeRatesResponse(
    val timestamp: Long,
    val base: String,
    val rates: Map<String, String>,
)
