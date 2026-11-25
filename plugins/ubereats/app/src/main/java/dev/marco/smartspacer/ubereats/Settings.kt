package dev.marco.smartspacer.ubereats

import android.content.Context
import androidx.core.content.edit

/** Plugin settings, stored in SharedPreferences. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** Hide Uber Eats' order notification on the lock screen (needs Native Smartspace). */
    var hideNotification: Boolean
        get() = prefs.getBoolean(KEY_HIDE, true)
        set(value) = prefs.edit { putBoolean(KEY_HIDE, value) }

    /** Use the courier's photo as the card icon, once there is one; otherwise Uber Eats' icon. */
    var showCourier: Boolean
        get() = prefs.getBoolean(KEY_COURIER, true)
        set(value) = prefs.edit { putBoolean(KEY_COURIER, value) }

    /** What to add after the order details: "· 12 min" or "· 60%", or nothing */
    enum class Extra { TIME_LEFT, PROGRESS, NONE }

    var extra: Extra
        get() = prefs.getString(KEY_EXTRA, null)?.let { runCatching { Extra.valueOf(it) }.getOrNull() } ?: Extra.TIME_LEFT
        set(value) = prefs.edit { putString(KEY_EXTRA, value.name) }

    companion object {
        private const val KEY_EXTRA = "card_extra"
        private const val KEY_HIDE = "hide_notification"
        private const val KEY_COURIER = "show_courier"
    }
}
