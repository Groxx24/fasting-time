package com.fasting.time.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/** A dark see-through surface that keeps white text readable over both skies. */
fun Modifier.panel(shape: Shape): Modifier =
    clip(shape).background(Color.Black.copy(alpha = 0.34f))
