package com.koflox.currency_exchanger.util.network

import com.google.gson.GsonBuilder
import com.koflox.currency_exchanger.data.entity.remote.ExchangeRatesResponse
import com.koflox.currency_exchanger.data.source.remote.deserializer.ExchangeRatesResponseDeserializer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun createOkHttpClient(
    httpLoggingInterceptor: Interceptor,
    authInterceptor: Interceptor,
): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(httpLoggingInterceptor)
    .addInterceptor(authInterceptor)
    .build()

fun createLoggingInterceptor(
    isEnabled: Boolean,
): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (isEnabled) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
}

inline fun <reified T> createRetrofitService(
    okHttpClient: OkHttpClient,
    url: String,
    converterFactory: Converter.Factory,
): T {
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(converterFactory)
        .build()
    return retrofit.create(T::class.java)
}

fun createGsonConverterFactory(
    gsonBuilder: GsonBuilder,
): Converter.Factory {
    val gson = with(gsonBuilder) {
        registerTypeAdapter(
            ExchangeRatesResponse::class.java,
            ExchangeRatesResponseDeserializer()
        )
        create()
    }
    return GsonConverterFactory.create(gson)
}
