package com.fasting.time.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** A pill holding one choice out of a few, with the chosen one lit up. */
@Composable
fun <T> PillSelector(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    optionContent: @Composable (T) -> Unit,
) {
    Row(
        modifier = modifier
            .panel(CircleShape)
            .padding(6.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val emphasis by animateFloatAsState(if (isSelected) 1f else 0f, label = "emphasis")
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 72.dp, minHeight = 48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f * emphasis))
                    // The highlight is the selection feedback, so no ripple on top of it.
                    .selectable(
                        selected = isSelected,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onClick = { onSelect(option) },
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides
                        LocalContentColor.current.copy(alpha = 0.6f + 0.4f * emphasis),
                ) {
                    optionContent(option)
                }
            }
        }
    }
}
