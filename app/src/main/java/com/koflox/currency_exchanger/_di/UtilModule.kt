package com.koflox.currency_exchanger._di

import com.google.gson.Gson
import org.koin.dsl.module

internal val utilModule = module {
    single { Gson() }
}
