package com.koflox.currency_exchanger.data.source.local

import com.koflox.currency_exchanger.data.entity.local.ExchangeRateLocal
import com.koflox.currency_exchanger.data.entity.local.ExchangeRatesLocal
import com.koflox.currency_exchanger.data.source.local.db.RatesDao
import com.koflox.currency_exchanger.domain.entity.Currency
import com.koflox.currency_exchanger.domain.entity.ExchangeRates
import com.koflox.currency_exchanger.util.database.runLocalDataSourceCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.math.BigDecimal

internal interface RatesDataSourceLocal {
    suspend fun getExchangeRates(): ExchangeRates
    suspend fun setExchangeRates(rates: ExchangeRates)
}

internal class RatesDataSourceLocalImpl(
    private val ratesDao: RatesDao,
    private val dispatcherIo: CoroutineDispatcher,
) : RatesDataSourceLocal {

    override suspend fun getExchangeRates(): ExchangeRates = runLocalDataSourceCatching {
        withContext(dispatcherIo) {
            mapExchangeRatesLocal(
                data = ratesDao.getExchangeRates(),
            )
        }
    }

    override suspend fun setExchangeRates(rates: ExchangeRates) = runLocalDataSourceCatching {
        withContext(dispatcherIo) {
            ratesDao.setExchangeRates(
                rates = mapExchangeRates(rates),
            )
        }
    }

    private fun mapExchangeRates(data: ExchangeRates): ExchangeRatesLocal {
        return ExchangeRatesLocal(
            baseCurrencyCode = data.base
                .code,
            lastFetchTimeMillis = data.lastFetchTimeMillis,
            rates = data.rates.map { common ->
                ExchangeRateLocal(
                    currencyCode = common.key.code,
                    value = common.value,
                )
            }.toSet()
        )
    }

    private fun mapExchangeRatesLocal(data: ExchangeRatesLocal): ExchangeRates {
        return ExchangeRates(
            base = Currency(
                code = data.baseCurrencyCode,
            ),
            lastFetchTimeMillis = data.lastFetchTimeMillis,
            rates = with(mutableMapOf<Currency, BigDecimal>()) {
                for ((currencyCode, rate) in data.rates) {
                    val currency = Currency(currencyCode)
                    this[currency] = rate
                }
                toMap()
            }
        )
    }

}
