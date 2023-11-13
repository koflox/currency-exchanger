package com.koflox.currency_exchanger.ui.rates.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.currency_exchanger.R
import com.koflox.currency_exchanger.domain.entity.Currency
import com.koflox.currency_exchanger.domain.entity.ExchangeRates
import com.koflox.currency_exchanger.domain.usecase.ExchangeValueException
import com.koflox.currency_exchanger.domain.usecase.GetRatesUseCase
import com.koflox.currency_exchanger.ui.rates.state.CurrenciesUiModel
import com.koflox.currency_exchanger.ui.rates.state.CurrentValueUiModel
import com.koflox.currency_exchanger.ui.rates.state.ExchangeRateUiModel
import com.koflox.currency_exchanger.ui.rates.state.RatesUiState
import com.koflox.currency_exchanger.util.database.DatabaseException
import com.koflox.currency_exchanger.util.network.NetworkException
import com.koflox.currency_exchanger.util.number.formatDecimal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.MathContext
import java.math.RoundingMode
import kotlin.time.Duration.Companion.milliseconds

internal class RatesViewModel(
    private val dispatcherDefault: CoroutineDispatcher,
    private val getRatesUseCase: GetRatesUseCase,
) : ViewModel() {

    companion object {
        private const val DEFAULT_VALUE_FOR_NEW_CURRENCY = "1"
        private val VALUE_INPUT_DELAY = 10L.milliseconds
        private const val MAX_CURRENT_VALUE_LENGTH = 20
        private val outputRateMathContext = MathContext(2, RoundingMode.HALF_UP)
    }

    private val _ratesUiState: MutableStateFlow<RatesUiState> = MutableStateFlow(
        RatesUiState.Loading
    )
    val ratesUiState: StateFlow<RatesUiState> = _ratesUiState

    var currentValueState by mutableStateOf(getInitialCurrentValueState())
        private set

    private var exchangeRateCalculationJob: Job? = null
    private var currentSelectedCurrency: Currency = getRatesUseCase.getBaseCurrency()

    init {
        initDataUpdates()
        getExchangeRates()
    }

    private fun initDataUpdates() {
        viewModelScope.launch {
            getRatesUseCase.initDataUpdates()
        }
    }

    private fun getExchangeRates() {
        viewModelScope.launch {
            val result = getRatesUseCase.getExchangeRates()
            handleRatesResult(result)
        }
    }

    fun onNewIntent(intent: RatesIntent) {
        when (intent) {
            is RatesIntent.NewCurrencySelected -> onNewCurrencySelected(intent)
            is RatesIntent.NewValueEntered -> onNewValueEntered(intent)
        }
    }

    private fun getInitialCurrentValueState() = CurrentValueUiModel(
        value = DEFAULT_VALUE_FOR_NEW_CURRENCY,
        maxValueLength = MAX_CURRENT_VALUE_LENGTH,
        labelResId = R.string.label_current_value,
        isError = false,
    )

    private fun onNewValueEntered(intent: RatesIntent.NewValueEntered) {
        val currentValue = currentValueState.value
        val isLimitReached = intent.value.length > MAX_CURRENT_VALUE_LENGTH
        val newValue = if (isLimitReached) currentValue else intent.value
        currentValueState = currentValueState.copy(
            value = newValue,
        )
        if (isLimitReached) return
        exchangeRateCalculationJob?.cancel()
        exchangeRateCalculationJob = viewModelScope.launch {
            delay(VALUE_INPUT_DELAY)
            val result = getRatesUseCase.exchangeValue(
                requestedCurrency = currentSelectedCurrency,
                value = intent.value,
            )
            handleRatesResult(result)
        }
    }

    private fun onNewCurrencySelected(intent: RatesIntent.NewCurrencySelected) {
        viewModelScope.launch {
            val currency = Currency(intent.currencyCode)
            currentSelectedCurrency = currency
            currentValueState = getInitialCurrentValueState()
            val result = getRatesUseCase.exchangeValue(
                requestedCurrency = currency,
                value = DEFAULT_VALUE_FOR_NEW_CURRENCY,
            )
            handleRatesResult(result)
        }
    }

    private fun handleRatesResult(result: Result<ExchangeRates>) {
        result.fold(
            onSuccess = ::handleExchangeRatesSuccess,
            onFailure = ::handleExchangeRatesFailure,
        )
    }

    private fun handleExchangeRatesFailure(error: Throwable) {
        when (error) {
            is NetworkException, is DatabaseException -> {
                _ratesUiState.update {
                    RatesUiState.Failure(R.string.message_data_source_failure)
                }
            }

            is ExchangeValueException.InvalidValue -> {
                currentValueState = currentValueState.copy(
                    isError = true,
                )
            }

            else -> {
                _ratesUiState.update {
                    RatesUiState.Failure(R.string.message_general_failure)
                }
            }
        }
    }

    private fun handleExchangeRatesSuccess(rates: ExchangeRates) {
        viewModelScope.launch(dispatcherDefault) {
            _ratesUiState.update {
                val currencies = rates.rates.map { (currency, _) ->
                    currency.code
                }
                val selectedCurrencyIndex = currencies.indexOf(currentSelectedCurrency.code)
                RatesUiState.ExchangeRates(
                    baseCurrencyCode = rates.base
                        .code,
                    rates = rates.rates.map { (currency, rate) ->
                        ExchangeRateUiModel(
                            currencyCode = currency.code,
                            value = formatDecimal(rate, outputRateMathContext),
                        )
                    },
                    currencies = CurrenciesUiModel(
                        selectedCurrency = currentSelectedCurrency.code,
                        selectedCurrencyIndex = selectedCurrencyIndex,
                        currencies = currencies,
                    )
                )
            }
        }
        viewModelScope.launch {
            currentValueState = currentValueState.copy(
                isError = false,
            )
        }
    }

}
