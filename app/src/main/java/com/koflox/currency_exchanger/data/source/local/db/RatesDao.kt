package com.koflox.currency_exchanger.data.source.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koflox.currency_exchanger.data.entity.local.ExchangeRatesLocal

@Dao
internal interface RatesDao {
    @Query("select * from exchangerateslocal")
    suspend fun getExchangeRates(): ExchangeRatesLocal

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setExchangeRates(rates: ExchangeRatesLocal)
}
