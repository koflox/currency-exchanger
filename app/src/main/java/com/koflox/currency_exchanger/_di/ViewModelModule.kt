package com.koflox.currency_exchanger._di

import com.koflox.currency_exchanger.ui.rates.viewmodel.RatesViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val viewModelModule = module {
    viewModel {
        RatesViewModel(
            dispatcherDefault = Dispatchers.Default,
            getRatesUseCase = get(),
        )
    }
}
