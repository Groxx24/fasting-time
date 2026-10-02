package com.fasting.time.ui.format

import androidx.compose.runtime.Composable
import com.fasting.time.resources.Res
import com.fasting.time.resources.duration_hours_minutes
import com.fasting.time.resources.duration_minutes
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

/** Hours, minutes and seconds, two digits each. */
internal fun Duration.toTimerText(): String =
    toComponents { hours, minutes, seconds, _ ->
        listOf(hours, minutes, seconds).joinToString(":") { it.toString().padStart(2, '0') }
    }

/** A length of time to read at a glance, for example "16h 05m" or "45m". Seconds are dropped. */
@Composable
internal fun Duration.toHoursMinutesText(): String =
    toComponents { hours, minutes, _, _ ->
        if (hours > 0) {
            stringResource(
                Res.string.duration_hours_minutes,
                hours.toString(),
                minutes.toString().padStart(2, '0'),
            )
        } else {
            stringResource(Res.string.duration_minutes, minutes.toString())
        }
    }
