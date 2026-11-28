package com.kieronquinn.app.smartspacer.plugin.uber.drawing

import kotlin.math.roundToInt

/**
 * Formats an already-normalized notification progress value for display.
 * Missing or non-finite values are omitted; finite out-of-range values are clamped
 * only for presentation and never written back to the source data.
 */
internal fun Float?.toProgressPercentage(): String? {
    val value = this?.takeIf { it.isFinite() } ?: return null
    return "${(value.coerceIn(0f, 1f) * 100f).roundToInt()}%"
}
