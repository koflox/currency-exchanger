package com.koflox.currency_exchanger.ui.rates.state

import androidx.annotation.StringRes

internal data class CurrentValueUiModel(
    val value: String,
    val maxValueLength: Int,
    @StringRes
    val labelResId: Int,
    val isError: Boolean,
)
