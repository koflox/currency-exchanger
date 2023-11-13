package com.koflox.currency_exchanger.domain.usecase

import com.koflox.currency_exchanger.data.repo.DataSourcePriority
import com.koflox.currency_exchanger.data.repo.RatesRepository
import com.koflox.currency_exchanger.domain.entity.Currency
import com.koflox.currency_exchanger.domain.entity.ExchangeRates
import com.koflox.currency_exchanger.util.database.DatabaseException
import com.koflox.currency_exchanger.util.result.runSafeCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.minutes

sealed class ExchangeValueException : Exception() {
    class InvalidValue : ExchangeValueException()
    data class MissingCurrency(val currency: Currency) : ExchangeValueException()
}

interface GetRatesUseCase {
    fun getBaseCurrency(): Currency
    suspend fun initDataUpdates()
    suspend fun getExchangeRates(): Result<ExchangeRates>
    suspend fun exchangeValue(requestedCurrency: Currency, value: String): Result<ExchangeRates>
}

internal class GetRatesUseCaseImpl(
    private val ratesRepository: RatesRepository,
    private val getCurrentTimeUseCase: GetCurrentTimeUseCase,
    private val dispatcherIo: CoroutineDispatcher,
) : GetRatesUseCase {

    companion object {
        private const val CURRENCY_CODE_USD = "USD"
        private val DEFAULT_BASE_CURRENCY = Currency(CURRENCY_CODE_USD)
        private val DATA_FETCH_INTERVAL = 30.minutes

        private const val DEFAULT_DECIMAL_SCALE = 2
        private val DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP
    }

    override fun getBaseCurrency(): Currency = DEFAULT_BASE_CURRENCY

    override suspend fun initDataUpdates() {
        coroutineScope {
            while (isActive) {
                delay(DATA_FETCH_INTERVAL)
                checkDataUpdate()
            }
        }
    }

    override suspend fun getExchangeRates(): Result<ExchangeRates> = runSafeCatching {
        withContext(dispatcherIo) {
            checkDataUpdate()
            mapRatesToDefaultScale(
                rates = ratesRepository.getExchangeRates(DataSourcePriority.LOCAL),
            )
        }
    }

    private suspend fun checkDataUpdate() {
        val lastFetchTimeMillis: Long? = try {
            ratesRepository.getExchangeRates(DataSourcePriority.LOCAL)
                .lastFetchTimeMillis
        } catch (ex: DatabaseException) {
            null
        }
        val doesDataRequireUpdate = if (lastFetchTimeMillis == null) {
            true
        } else {
            val currentTimeMillis = getCurrentTimeUseCase.getCurrentTimeMillis()
            lastFetchTimeMillis + DATA_FETCH_INTERVAL.inWholeMilliseconds < currentTimeMillis
        }
        if (doesDataRequireUpdate) {
            ratesRepository.getExchangeRates(DataSourcePriority.REMOTE)
        }
    }

    override suspend fun exchangeValue(requestedCurrency: Currency, value: String): Result<ExchangeRates> = runSafeCatching {
        withContext(dispatcherIo) {
            val decimalValue = value.trim()
                .toBigDecimalOrNull() ?: throw ExchangeValueException.InvalidValue()
            val usdRates = ratesRepository.getExchangeRates(DataSourcePriority.LOCAL)
            val newRates = mutableMapOf<Currency, BigDecimal>().apply {
                for ((currency, rate) in usdRates.rates) {
                    val exchangedValue = when (requestedCurrency) {
                        currency -> decimalValue
                        getBaseCurrency() -> decimalValue.multiply(rate)
                        else -> {
                            val usdRate = usdRates.rates[currency]
                                ?: throw ExchangeValueException.MissingCurrency(currency = currency)
                            val requestedCurrencyRate = usdRates.rates[requestedCurrency]
                                ?: throw ExchangeValueException.MissingCurrency(currency = currency)
                            if (requestedCurrencyRate == BigDecimal.ZERO) {
                                BigDecimal.ZERO
                            } else {
                                decimalValue.divide(requestedCurrencyRate, DEFAULT_DECIMAL_SCALE, DEFAULT_ROUNDING_MODE)
                                    .multiply(usdRate)
                            }
                        }
                    }.setScale(DEFAULT_DECIMAL_SCALE, DEFAULT_ROUNDING_MODE)
                    this[currency] = exchangedValue
                }
            }
            usdRates.copy(
                base = requestedCurrency,
                rates = newRates,
            )
        }
    }

    private fun mapRatesToDefaultScale(rates: ExchangeRates): ExchangeRates {
        return rates.copy(
            rates = rates.rates.mapValues {
                it.value.setScale(DEFAULT_DECIMAL_SCALE, DEFAULT_ROUNDING_MODE)
            }
        )
    }

}
