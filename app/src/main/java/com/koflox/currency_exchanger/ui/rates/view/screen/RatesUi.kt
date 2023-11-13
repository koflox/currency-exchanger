package com.koflox.currency_exchanger.ui.rates.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.koflox.currency_exchanger.ui.rates.state.CurrentValueUiModel
import com.koflox.currency_exchanger.ui.rates.state.ExchangeRateUiModel
import com.koflox.currency_exchanger.ui.rates.state.RatesUiState
import com.koflox.currency_exchanger.ui.rates.view.LazyDropdownMenu
import com.koflox.currency_exchanger.ui.theme.SpacingStepMedium
import com.koflox.currency_exchanger.ui.theme.SpacingStepSmall

@Composable
internal fun RatesUi(
    ratesState: RatesUiState.ExchangeRates,
    currentValueState: CurrentValueUiModel,
    onCurrencySelected: (String) -> Unit,
    onValueChanged: (String) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier.padding(SpacingStepMedium),
    ) {
        val (tfValue, menuRates, ratesGrid) = createRefs()
        TextField(
            modifier = Modifier.constrainAs(tfValue) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                end.linkTo(menuRates.start, margin = SpacingStepSmall)
                width = Dimension.fillToConstraints
            },
            value = currentValueState.value,
            onValueChange = { newString ->
                onValueChanged.invoke(newString)
            },
            label = {
                Text(
                    text = stringResource(
                        id = currentValueState.labelResId
                    )
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = LocalTextStyle.current
                .copy(textAlign = TextAlign.End),
            singleLine = true,
            isError = currentValueState.isError,
            supportingText = {
                Text(
                    text = "${currentValueState.value.length} / ${currentValueState.maxValueLength}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            },
        )
        LazyDropdownMenu(
            modifier = Modifier
                .constrainAs(menuRates) {
                    end.linkTo(parent.end)
                    top.linkTo(tfValue.top)
                }
                .defaultMinSize(
                    minHeight = TextFieldDefaults.MinHeight
                ),
            selectedItem = ratesState.currencies.selectedCurrency,
            items = ratesState.currencies.currencies,
            selectedIndex = ratesState.currencies.selectedCurrencyIndex,
            onItemSelected = { _, item -> onCurrencySelected.invoke(item) },
        )
        LazyVerticalGrid(
            modifier = Modifier.constrainAs(ratesGrid) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(tfValue.bottom)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
            },
            columns = GridCells.Adaptive(minSize = 80.dp),
            verticalArrangement = Arrangement.spacedBy(SpacingStepMedium),
            horizontalArrangement = Arrangement.spacedBy(SpacingStepSmall),
        ) {
            ratesState.rates.forEach { model ->
                item(key = model.currencyCode) {
                    RateItemUi(model)
                }
            }
        }
    }
}

@Composable
private fun RateItemUi(model: ExchangeRateUiModel) {
    Column(
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = model.currencyCode,
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = model.value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
