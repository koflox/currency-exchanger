package com.koflox.currency_exchanger.data.repo

import com.koflox.currency_exchanger.data.source.local.RatesDataSourceLocal
import com.koflox.currency_exchanger.data.source.remote.RatesDataSourceRemote
import com.koflox.currency_exchanger.domain.entity.ExchangeRates
import com.koflox.currency_exchanger.util.network.NetworkException

interface RatesRepository {
    suspend fun getExchangeRates(dsPriority: DataSourcePriority): ExchangeRates
}

internal class RatesRepositoryImpl(
    private val dsLocal: RatesDataSourceLocal,
    private val dsRemote: RatesDataSourceRemote,
) : RatesRepository {

    override suspend fun getExchangeRates(dsPriority: DataSourcePriority): ExchangeRates {
        return when (dsPriority) {
            DataSourcePriority.REMOTE -> {
                try {
                    dsRemote.getExchangeRates().also { exchangeRates ->
                        dsLocal.setExchangeRates(exchangeRates)
                    }
                } catch (ex: NetworkException) {
                    dsLocal.getExchangeRates()
                }
            }

            DataSourcePriority.LOCAL -> {
                dsLocal.getExchangeRates()
            }
        }
    }

}
