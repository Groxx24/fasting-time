package com.fasting.time.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/** The fasting sky, from top to bottom: a night with nothing to eat in it. */
val NightColors = listOf(Color(0xFF0B1026), Color(0xFF1E1B4B), Color(0xFF020617))

/** The eating sky, from top to bottom: warm like a kitchen. */
val FeastColors = listOf(Color(0xFFEA580C), Color(0xFFDB2777), Color(0xFF9D174D))

/** The ring and bar of the no-eating sign. */
val Forbidden = Color(0xFFF87171)

/** Fasting figures in the history. */
val FastingTint = Color(0xFFC7D2FE)

/** Eating figures in the history. */
val EatingTint = Color(0xFFFED7AA)

private val Night = NightColors.first()

private val FastingColorScheme = darkColorScheme(
    primary = Color(0xFFFB923C),
    onPrimary = Night,
    background = Night,
    onBackground = Color.White,
    surface = Night,
    onSurface = Color.White,
)

@Composable
fun FastingTimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FastingColorScheme) {
        // Everything sits straight on the animated backdrop, never on a Material surface.
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            content = content,
        )
    }
}
