package com.koflox.currency_exchanger.data.source.remote

import com.koflox.currency_exchanger.data.entity.remote.ExchangeRatesResponse
import com.koflox.currency_exchanger.data.source.remote.service.OpenExchangeRatesService
import com.koflox.currency_exchanger.domain.entity.Currency
import com.koflox.currency_exchanger.domain.entity.ExchangeRates
import com.koflox.currency_exchanger.util.network.runRemoteDataSourceCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.math.BigDecimal

internal interface RatesDataSourceRemote {
    suspend fun getExchangeRates(): ExchangeRates
}

internal class RatesDataSourceRemoteImpl(
    private val service: OpenExchangeRatesService,
    private val dispatcherIo: CoroutineDispatcher,
) : RatesDataSourceRemote {
    override suspend fun getExchangeRates(): ExchangeRates = runRemoteDataSourceCatching {
        withContext(dispatcherIo) {
            val response = service.getExchangeRates()
            mapExchangeRates(response)
        }
    }

    private fun mapExchangeRates(response: ExchangeRatesResponse): ExchangeRates = ExchangeRates(
        base = Currency(
            code = response.base,
        ),
        lastFetchTimeMillis = System.currentTimeMillis(),
        rates = with(mutableMapOf<Currency, BigDecimal>()) {
            for ((currencyCode, rateString) in response.rates) {
                val currency = Currency(currencyCode)
                this[currency] = rateString.toBigDecimal()
            }
            toMap()
        }
    )

}
