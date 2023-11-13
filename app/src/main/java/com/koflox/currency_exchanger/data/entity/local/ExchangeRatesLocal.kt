package com.koflox.currency_exchanger.data.entity.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity
internal data class ExchangeRatesLocal(
    @PrimaryKey
    val baseCurrencyCode: String,
    val lastFetchTimeMillis: Long,
    val rates: Set<ExchangeRateLocal>,
)

internal data class ExchangeRateLocal(
    val currencyCode: String,
    val value: BigDecimal,
)
