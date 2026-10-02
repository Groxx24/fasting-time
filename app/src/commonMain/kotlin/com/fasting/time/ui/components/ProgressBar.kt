package com.fasting.time.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/** A rounded bar filled from the start up to [fraction], which is between 0 and 1. */
@Composable
fun ProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val corner = CornerRadius(size.height / 2)
        drawRoundRect(Color.White.copy(alpha = 0.14f), cornerRadius = corner)
        val filled = size.width * fraction.coerceIn(0f, 1f)
        // Never narrower than a dot, so the shortest fast still shows.
        if (fraction > 0f) {
            drawRoundRect(
                color = color,
                size = Size(filled.coerceAtLeast(size.height), size.height),
                cornerRadius = corner,
            )
        }
    }
}
