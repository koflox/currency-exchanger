package com.koflox.currency_exchanger.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.koflox.currency_exchanger.data.entity.local.ExchangeRatesLocal
import com.koflox.currency_exchanger.data.source.local.db.RatesDb.Companion.DATABASE_VERSION

@Database(
    entities = [
        ExchangeRatesLocal::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = false,
)
@TypeConverters(
    value = [
        RatesRoomConverters::class,
    ]
)
internal abstract class RatesDb : RoomDatabase() {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "rates.db"
    }

    internal abstract fun getRatesDao(): RatesDao

}
