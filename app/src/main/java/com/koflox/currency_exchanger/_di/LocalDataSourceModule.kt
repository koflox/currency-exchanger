package com.koflox.currency_exchanger._di

import android.content.Context
import androidx.room.Room
import com.koflox.currency_exchanger.data.source.local.RatesDataSourceLocal
import com.koflox.currency_exchanger.data.source.local.RatesDataSourceLocalImpl
import com.koflox.currency_exchanger.data.source.local.db.RatesDb
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

internal val localDataSourceModule = module {
    factory<RatesDataSourceLocal> {
        RatesDataSourceLocalImpl(
            ratesDao = get(),
            dispatcherIo = Dispatchers.IO,
        )
    }
    factory {
        createRatesDb(
            context = get(),
        )
    }
    factory {
        get<RatesDb>().getRatesDao()
    }
}

private fun createRatesDb(
    context: Context,
): RatesDb = Room.databaseBuilder(
    context.applicationContext,
    RatesDb::class.java,
    RatesDb.DATABASE_NAME,
).build()
