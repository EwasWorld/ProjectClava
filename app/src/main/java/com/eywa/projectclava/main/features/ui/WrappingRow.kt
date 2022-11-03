package com.eywa.projectclava.main.features.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.roundToInt


@Composable
fun WrappingRow(
        modifier: Modifier = Modifier,
        spacing: Dp = 5.dp,
        content: @Composable () -> Unit
) {
    val spacingPx = with(LocalDensity.current) { spacing.toPx().roundToInt() }

    Layout(
            modifier = modifier,
            content = content
    ) { measurables, constraints ->
        // Measure each child
        val placeables = measurables.map { measurable -> measurable.measure(constraints) }

        // Calculate the height and width
        val (width, height) = place(placeables, spacingPx, constraints.maxWidth) { _, _, _ -> }

        check(width <= constraints.maxWidth && height <= constraints.maxHeight) { "Doesn't fit!" }

        layout(width, height) {
            place(
                    placeables,
                    spacingPx,
                    constraints.maxWidth,
            ) { placeable, x, y -> placeable.placeRelative(x = x, y = y) }
        }
    }
}

fun place(
        placeables: List<Placeable>,
        spacing: Int,
        maxWidth: Int,
        onEach: (Placeable, x: Int, y: Int) -> Unit
): Pair<Int, Int> {
    var currentMaxHeight = 0
    var currentMaxWidth = 0
    var yPosition = 0
    var xPosition = 0

    placeables.forEach { placeable ->
        if (xPosition + placeable.width > maxWidth) {
            currentMaxWidth = max(currentMaxWidth, xPosition - spacing)
            xPosition = 0
            yPosition += currentMaxHeight
            currentMaxHeight = 0
        }

        onEach(placeable, xPosition, yPosition)

        xPosition += placeable.width + spacing
        currentMaxHeight = max(currentMaxHeight, placeable.height)
    }

    return max(currentMaxWidth, xPosition - spacing) to yPosition + currentMaxHeight
}