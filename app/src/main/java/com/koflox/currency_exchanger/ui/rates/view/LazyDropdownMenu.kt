package com.koflox.currency_exchanger.ui.rates.view

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.koflox.currency_exchanger.ui.theme.SpacingStepMedium
import com.koflox.currency_exchanger.ui.theme.SpacingStepSmall

private const val DEFAULT_SPAN = 4

@Composable
fun <T> LazyDropdownMenu(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedIndex: Int,
    selectedItem: T,
    onItemSelected: (index: Int, item: T) -> Unit,
    onCreateItemView: @Composable (T, Boolean, () -> Unit) -> Unit = { item, selected, onClick ->
        MenuItem(
            text = item.toString(),
            selected = selected,
            onClick = onClick,
        )
    },
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(SpacingStepSmall),
            )
            .wrapContentWidth()
            .pointerInput(null) {
                detectTapGestures(
                    onTap = {
                        expanded = true
                    },
                )
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .padding(
                    start = SpacingStepSmall,
                    top = SpacingStepSmall,
                    bottom = SpacingStepSmall,
                ),
            text = selectedItem.toString(),
        )
        val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
    }
    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
            ) {
                val gridState = rememberLazyGridState()
                LaunchedEffect("ScrollToSelected") {
                    gridState.scrollToItem(index = selectedIndex)
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(count = DEFAULT_SPAN),
                    state = gridState,
                ) {
                    itemsIndexed(items) { index, item ->
                        val isSelectedItem = index == selectedIndex
                        onCreateItemView(item, isSelectedItem) {
                            onItemSelected(index, item)
                            expanded = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = Modifier
                .clickable { onClick() }
                .fillMaxWidth()
                .padding(SpacingStepMedium),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text)
        }
    }
}
