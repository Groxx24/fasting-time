package com.fasting.time.ui.format

import com.fasting.time.domain.model.TimeOfDay

/** Hours and minutes on a 24-hour clock, two digits each, for example "08:05". */
internal fun TimeOfDay.toClockText(): String =
    listOf(hour, minute).joinToString(":") { it.toString().padStart(2, '0') }
