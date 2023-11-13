package com.koflox.currency_exchanger.ui.rates.view.screen

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.koflox.currency_exchanger.ui.rates.state.RatesUiState
import com.koflox.currency_exchanger.ui.rates.viewmodel.RatesIntent
import com.koflox.currency_exchanger.ui.rates.viewmodel.RatesViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun RatesScreen(
    viewModel: RatesViewModel = koinViewModel(),
) {
    val ratesUiState = viewModel.ratesUiState.collectAsStateWithLifecycle()
    val currentValue = viewModel.currentValueState
    when (val state = ratesUiState.value) {
        is RatesUiState.ExchangeRates -> {
            RatesUi(
                currentValueState = currentValue,
                ratesState = state,
                onCurrencySelected = { currencyCode ->
                    val intent = RatesIntent.NewCurrencySelected(currencyCode)
                    viewModel.onNewIntent(intent)
                },
                onValueChanged = { value ->
                    val intent = RatesIntent.NewValueEntered(value)
                    viewModel.onNewIntent(intent)
                },
            )
        }

        is RatesUiState.Failure -> RatesErrorUi(state)
        RatesUiState.Loading -> RatesLoadingUi()
    }
}
