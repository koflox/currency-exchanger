package com.koflox.currency_exchanger.domain.entity

import java.math.BigDecimal

fun generateCurrency(
    code: String = "USD",
): Currency = Currency(code)

fun generateExchangeRates(
    base: Currency = generateCurrency(),
    lastFetchTimeMillis: Long = -1,
    rates: Map<Currency, BigDecimal> = mapOf(),
): ExchangeRates = ExchangeRates(
    base = base,
    lastFetchTimeMillis = lastFetchTimeMillis,
    rates = rates,
)
