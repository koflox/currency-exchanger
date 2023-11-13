package com.koflox.currency_exchanger

import android.app.Application
import com.koflox.currency_exchanger._di.koinModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CurrencyExchangerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CurrencyExchangerApp)
            modules(koinModules)
        }
    }
}
