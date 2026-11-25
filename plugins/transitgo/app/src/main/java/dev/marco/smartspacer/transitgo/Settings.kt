package dev.marco.smartspacer.transitgo

import android.content.Context
import androidx.core.content.edit

/** Plugin settings, stored in SharedPreferences. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** Hide Transit's GO notification on the lock screen (needs Native Smartspace). */
    var hideNotification: Boolean
        get() = prefs.getBoolean(KEY_HIDE, true)
        set(value) = prefs.edit { putBoolean(KEY_HIDE, value) }

    /** Use Transit's colorful route chip (e.g. green "57-A") as the card icon; otherwise the step icon. */
    var showRouteChip: Boolean
        get() = prefs.getBoolean(KEY_CHIP, true)
        set(value) = prefs.edit { putBoolean(KEY_CHIP, value) }

    /** What to add after the next step: "in 20 minutes · 6:12 AM" or "· 4 h 00 min", or nothing */
    enum class Extra { ARRIVAL, TIME_LEFT, NONE }

    var extra: Extra
        get() = prefs.getString(KEY_EXTRA, null)?.let { runCatching { Extra.valueOf(it) }.getOrNull() } ?: Extra.ARRIVAL
        set(value) = prefs.edit { putString(KEY_EXTRA, value.name) }

    companion object {
        private const val KEY_EXTRA = "card_extra"
        private const val KEY_HIDE = "hide_notification"
        private const val KEY_CHIP = "show_route_chip"
    }
}
