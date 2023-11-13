package com.koflox.currency_exchanger.ui.rates.viewmodel

internal sealed class RatesIntent {
    class NewCurrencySelected(
        val currencyCode: String,
    ) : RatesIntent()

    class NewValueEntered(
        val value: String,
    ) : RatesIntent()
}
