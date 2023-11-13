package com.koflox.currency_exchanger.data.source.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.koflox.currency_exchanger.data.entity.local.ExchangeRateLocal
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.reflect.Type

internal class RatesRoomConverters : KoinComponent {

    private val gson: Gson by inject()

    @TypeConverter
    fun from(value: Set<ExchangeRateLocal>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun to(jsonString: String): Set<ExchangeRateLocal> {
        val mapType: Type = object : TypeToken<Set<ExchangeRateLocal>>() {}.type
        return gson.fromJson(jsonString, mapType)
    }

}
