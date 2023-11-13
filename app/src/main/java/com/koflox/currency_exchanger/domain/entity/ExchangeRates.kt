package com.koflox.currency_exchanger.domain.entity

import java.math.BigDecimal

data class ExchangeRates(
    val base: Currency,
    val lastFetchTimeMillis: Long,
    val rates: Map<Currency, BigDecimal>,
)
