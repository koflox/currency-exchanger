package com.koflox.currency_exchanger._di

import com.koflox.currency_exchanger.domain.usecase.GetCurrentTimeUseCase
import com.koflox.currency_exchanger.domain.usecase.GetCurrentTimeUseCaseImpl
import com.koflox.currency_exchanger.domain.usecase.GetRatesUseCase
import com.koflox.currency_exchanger.domain.usecase.GetRatesUseCaseImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

internal val useCaseModule = module {
    factory<GetRatesUseCase> {
        GetRatesUseCaseImpl(
            ratesRepository = get(),
            getCurrentTimeUseCase = get(),
            dispatcherIo = Dispatchers.IO,
        )
    }
    factory<GetCurrentTimeUseCase> {
        GetCurrentTimeUseCaseImpl()
    }
}
