package com.koflox.currency_exchanger.data.source.remote.service

import com.koflox.currency_exchanger.data.entity.remote.ExchangeRatesResponse
import retrofit2.http.GET

internal interface OpenExchangeRatesService {

    companion object {
        const val BASE_URL = "https://openexchangerates.org/api/"
        const val KEY_AUTH = "app_id"
    }

    @GET("latest.json")
    suspend fun getExchangeRates(): ExchangeRatesResponse

}
