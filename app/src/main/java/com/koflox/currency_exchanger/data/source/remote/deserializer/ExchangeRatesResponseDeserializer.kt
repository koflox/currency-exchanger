package com.koflox.currency_exchanger.data.source.remote.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.koflox.currency_exchanger.data.entity.remote.ExchangeRatesResponse
import java.lang.reflect.Type

internal class ExchangeRatesResponseDeserializer : JsonDeserializer<ExchangeRatesResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): ExchangeRatesResponse {
        val jsonObject = json.asJsonObject
        val rates = mutableMapOf<String, String>().apply {
            for ((currencyCode, value) in jsonObject.get("rates").asJsonObject.entrySet()) {
                this[currencyCode] = value.asString
            }
        }
        return ExchangeRatesResponse(
            timestamp = jsonObject.get("timestamp")
                .asLong,
            base = jsonObject.get("base")
                .asString,
            rates = rates,
        )
    }
}
