package dev.marco.smartspacer.transitgo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Named layout and motion values for the settings screen.
 * Keep intentional visual constants centralized; don't turn every one-off number into a token.
 */
internal object TransitGoSettingsTokens {
    val ACCESS_GREEN = Color(0xFF1E8E3E)
    val FADE_EDGE = 24.dp
    val ACCENT_PALETTE = listOf(
        Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFF9AB00), Color(0xFFEA4335),
        Color(0xFFA142F4), Color(0xFF24C1E0), Color(0xFFFA7B17), Color(0xFFF538A0)
    )

    const val CHIP_PHASE = 0.35f
    const val LABEL_FULL_BUTTON = 1.3f
    const val LABEL_FULL_SCREEN = 2.2f

    val LIST_BOTTOM_GAP = 18.dp
    val CHIP_GAP_MIN = 8.dp
    val CHIP_GAP_MAX = 160.dp
    val END_ROW_GAP = 8.dp

    const val KEY_FIRST_ROW = "first_card_row"
    const val KEY_EXTRA_ROW = "extra_row"
    const val KEY_HIDE_ROW = "hide_row"
    const val KEY_LAST_ROW = "notification_access"
    const val KEY_LOCK_HEADER = "lock_screen_header"
    const val KEY_PERMISSIONS_HEADER = "permissions_header"

    val END_ITEMS = listOf(KEY_FIRST_ROW, KEY_EXTRA_ROW, KEY_LOCK_HEADER, KEY_HIDE_ROW, KEY_PERMISSIONS_HEADER, KEY_LAST_ROW)
    val END_ROWS = listOf(KEY_FIRST_ROW, KEY_EXTRA_ROW, KEY_HIDE_ROW, KEY_LAST_ROW)
    val ROWS_AFTER_CARD = setOf(KEY_EXTRA_ROW)

    val NAV_BAR_OPTICAL = 16.dp
    val HEADER_BLUR_MIN = 8.dp
    val HEADER_BLUR_MAX = 28.dp
    val HEADER_BLUR_DISTANCE = 240.dp
    val HEADER_SHADOW = 10.dp
    const val HEADER_SHADOW_ALPHA = 0.025f
    const val HEADER_TINT_ALPHA = 0.72f
    const val SHADOW_FROM = 0.5f
    val ROW_SPACING = 2.dp
    val CHIP_HEIGHT = 40.dp
    val CHIP_HEIGHT_FULL = 64.dp
}
