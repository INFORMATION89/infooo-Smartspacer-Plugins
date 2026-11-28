package dev.marco.smartspacer.ubereats

import android.content.Intent
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import dev.infooo.smartspacer.settings.PluginSettingsScaffold
import dev.infooo.smartspacer.settings.SettingsChoiceOption
import dev.infooo.smartspacer.settings.SettingsChoicePreference
import dev.infooo.smartspacer.settings.SettingsDivider
import dev.infooo.smartspacer.settings.SettingsGroup
import dev.infooo.smartspacer.settings.SettingsLinkPreference
import dev.infooo.smartspacer.settings.SettingsSectionTitle
import dev.infooo.smartspacer.settings.SettingsStatusPreference
import dev.infooo.smartspacer.settings.SettingsSwitchPreference
import dev.infooo.smartspacer.settings.SettingsTheme
import dev.infooo.smartspacer.settings.SmartspacerTargetPreview

class SettingsActivity : ComponentActivity() {

    private var accessGranted by mutableStateOf(false)
    private var settingsVersion by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SettingsTheme {
                SettingsContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accessGranted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        if (accessGranted) runCatching {
            android.service.notification.NotificationListenerService.requestRebind(
                android.content.ComponentName(this, EatsListener::class.java)
            )
        }
    }

    private fun onSettingChanged() {
        settingsVersion++
        SmartspacerTargetProvider.notifyChange(this, UberEatsTarget::class.java)
    }

    private fun openNotificationAccess() {
        val detail = Intent(AndroidSettings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(
            AndroidSettings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
            android.content.ComponentName(this, EatsListener::class.java).flattenToString()
        )
        runCatching { startActivity(detail) }
            .onFailure { startActivity(Intent(AndroidSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    }

    private fun openSource() {
        startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(SOURCE_URL)))
    }

    @Composable
    private fun SettingsContent() {
        val settings = remember { Settings(this) }
        val liveOrder = OrderStore.state
        val sampleOrder = remember(SampleOrder.generation) { SampleOrder.create(this) }
        val state = liveOrder ?: sampleOrder
        val previewTarget = remember(state, settingsVersion) {
            buildUberEatsTarget(this, state, settings, PREVIEW_TARGET_ID, isPreview = true)
        }

        PluginSettingsScaffold(title = stringResource(R.string.app_name)) {
            SmartspacerTargetPreview(
                target = previewTarget,
                isExample = liveOrder == null,
                exampleLabel = stringResource(R.string.preview_example)
            )

            SettingsSectionTitle(stringResource(R.string.section_card))
            SettingsGroup {
                SettingsSwitchPreference(
                    title = stringResource(R.string.setting_courier_title),
                    summary = stringResource(R.string.setting_courier_summary),
                    icon = Icons.Rounded.DeliveryDining,
                    checked = settings.showCourier,
                    onCheckedChange = {
                        settings.showCourier = it
                        onSettingChanged()
                    }
                )
                SettingsDivider()
                SettingsChoicePreference(
                    title = stringResource(R.string.setting_extra_title),
                    summary = stringResource(R.string.setting_extra_summary),
                    icon = Icons.Rounded.Schedule,
                    selectedValue = settings.extra.name,
                    options = listOf(
                        SettingsChoiceOption(Settings.Extra.TIME_LEFT.name, stringResource(R.string.setting_extra_time_left)),
                        SettingsChoiceOption(Settings.Extra.PROGRESS.name, stringResource(R.string.setting_extra_progress)),
                        SettingsChoiceOption(Settings.Extra.NONE.name, stringResource(R.string.setting_extra_none))
                    ),
                    onValueChange = {
                        settings.extra = Settings.Extra.valueOf(it)
                        onSettingChanged()
                    }
                )
            }

            SettingsSectionTitle(stringResource(R.string.section_lock_screen))
            SettingsGroup {
                SettingsSwitchPreference(
                    title = stringResource(R.string.setting_hide_title),
                    summary = stringResource(R.string.setting_hide_summary),
                    icon = Icons.Rounded.VisibilityOff,
                    checked = settings.hideNotification,
                    onCheckedChange = {
                        settings.hideNotification = it
                        onSettingChanged()
                    }
                )
            }

            SettingsSectionTitle(stringResource(R.string.section_permissions))
            SettingsGroup {
                SettingsStatusPreference(
                    title = stringResource(R.string.setting_access),
                    summary = stringResource(if (accessGranted) R.string.setting_access_on else R.string.setting_access_off),
                    icon = if (accessGranted) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                    status = stringResource(if (accessGranted) R.string.setting_access_badge_on else R.string.setting_access_badge_off),
                    onClick = ::openNotificationAccess
                )
            }

            SettingsGroup {
                SettingsLinkPreference(
                    title = stringResource(R.string.source_code),
                    icon = Icons.Rounded.Code,
                    onClick = ::openSource
                )
            }
        }
    }

    private companion object {
        const val SOURCE_URL = "https://github.com/INFORMATION89/infooo-Smartspacer-Plugins"
        const val PREVIEW_TARGET_ID = "ubereats_settings_preview"
    }
}
