package com.koflox.currency_exchanger._di

import com.google.gson.Gson
import com.koflox.currency_exchanger.BuildConfig
import com.koflox.currency_exchanger.data.source.remote.RatesDataSourceRemote
import com.koflox.currency_exchanger.data.source.remote.RatesDataSourceRemoteImpl
import com.koflox.currency_exchanger.data.source.remote.service.OpenExchangeRatesService
import com.koflox.currency_exchanger.util.network.AuthInterceptor
import com.koflox.currency_exchanger.util.network.createGsonConverterFactory
import com.koflox.currency_exchanger.util.network.createLoggingInterceptor
import com.koflox.currency_exchanger.util.network.createOkHttpClient
import com.koflox.currency_exchanger.util.network.createRetrofitService
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val QUALIFIER_LOGGING_INTERCEPTOR = "QUALIFIER_LOGGING_INTERCEPTOR"
private const val QUALIFIER_AUTH_INTERCEPTOR = "QUALIFIER_AUTH_INTERCEPTOR"
private const val QUALIFIER_OKHTTP_CLIENT = "QUALIFIER_OKHTTP_CLIENT"
private const val QUALIFIER_GSON_CONVERTER = "QUALIFIER_GSON_CONVERTER"

internal val remoteDataSourceModule = module {
    factory<Interceptor>(named(QUALIFIER_LOGGING_INTERCEPTOR)) {
        createLoggingInterceptor(
            isEnabled = BuildConfig.DEBUG,
        )
    }
    factory<Interceptor>(named(QUALIFIER_AUTH_INTERCEPTOR)) {
        AuthInterceptor(
            apiKeyParam = OpenExchangeRatesService.KEY_AUTH,
            apiKeyValue = BuildConfig.API_KEY_OPEN_EXCHANGE_RATES,
        )
    }
    factory(named(QUALIFIER_OKHTTP_CLIENT)) {
        createOkHttpClient(
            httpLoggingInterceptor = get(named(QUALIFIER_LOGGING_INTERCEPTOR)),
            authInterceptor = get(named(QUALIFIER_AUTH_INTERCEPTOR)),
        )
    }
    factory<OpenExchangeRatesService> {
        createRetrofitService(
            okHttpClient = get(named(QUALIFIER_OKHTTP_CLIENT)),
            url = OpenExchangeRatesService.BASE_URL,
            converterFactory = get(named(QUALIFIER_GSON_CONVERTER)),
        )
    }
    factory<RatesDataSourceRemote> {
        RatesDataSourceRemoteImpl(
            service = get(),
            dispatcherIo = Dispatchers.IO,
        )
    }
    factory(named(QUALIFIER_GSON_CONVERTER)) {
        createGsonConverterFactory(
            gsonBuilder = get<Gson>().newBuilder(),
        )
    }
}
