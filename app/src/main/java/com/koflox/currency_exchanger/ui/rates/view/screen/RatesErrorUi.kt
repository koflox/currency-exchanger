package com.koflox.currency_exchanger.ui.rates.view.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.koflox.currency_exchanger.ui.rates.state.RatesUiState

@Composable
internal fun RatesErrorUi(
    state: RatesUiState.Failure,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(id = state.messageResId))
    }
}
