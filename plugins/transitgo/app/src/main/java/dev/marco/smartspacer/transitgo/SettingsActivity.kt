package dev.marco.smartspacer.transitgo

import android.content.Intent
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat

/**
 * Android entry point for the plugin's own settings screen.
 * Lifecycle and system permission navigation stay here; Compose UI lives in TransitGoSettingsScreen.
 */
class SettingsActivity : ComponentActivity() {

    private lateinit var settingsScreen: TransitGoSettingsScreen
    private var accessGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        settingsScreen = TransitGoSettingsScreen(this)
        setContent { SettingsTheme { settingsScreen.Content(accessGranted) } }
    }

    override fun onResume() {
        super.onResume()
        accessGranted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        if (accessGranted) runCatching {
            android.service.notification.NotificationListenerService.requestRebind(
                android.content.ComponentName(this, TransitGoListener::class.java)
            )
        }
    }

    /** Opens this plugin's notification access switch, falling back to the system-wide list. */
    internal fun openNotificationAccess() {
        val detail = Intent(AndroidSettings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(
            AndroidSettings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
            android.content.ComponentName(this, TransitGoListener::class.java).flattenToString()
        )
        runCatching { startActivity(detail) }
            .onFailure { startActivity(Intent(AndroidSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    }
}
