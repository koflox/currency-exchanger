package com.koflox.currency_exchanger.ui.rates.state

import androidx.annotation.StringRes

internal sealed class RatesUiState {
    data object Loading : RatesUiState()
    data class Failure(
        @StringRes
        val messageResId: Int,
    ) : RatesUiState()

    data class ExchangeRates(
        val baseCurrencyCode: String,
        val rates: List<ExchangeRateUiModel>,
        val currencies: CurrenciesUiModel,
    ) : RatesUiState()
}

internal data class ExchangeRateUiModel(
    val currencyCode: String,
    val value: String,
)

internal data class CurrenciesUiModel(
    val selectedCurrency: String,
    val selectedCurrencyIndex: Int,
    val currencies: List<String>,
)
