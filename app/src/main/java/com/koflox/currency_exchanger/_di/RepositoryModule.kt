package com.koflox.currency_exchanger._di

import com.koflox.currency_exchanger.data.repo.RatesRepository
import com.koflox.currency_exchanger.data.repo.RatesRepositoryImpl
import org.koin.dsl.module

internal val repositoryModule = module {
    single<RatesRepository> {
        RatesRepositoryImpl(
            dsLocal = get(),
            dsRemote = get(),
        )
    }
}
