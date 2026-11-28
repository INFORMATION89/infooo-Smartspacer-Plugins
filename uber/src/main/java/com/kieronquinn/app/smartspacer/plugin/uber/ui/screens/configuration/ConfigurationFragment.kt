package com.kieronquinn.app.smartspacer.plugin.uber.ui.screens.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.uber.R
import com.kieronquinn.app.smartspacer.plugin.uber.drawing.drawExampleTargetImage
import com.kieronquinn.app.smartspacer.plugin.uber.ui.screens.configuration.ConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import dev.infooo.smartspacer.settings.PluginSettingsScaffold
import dev.infooo.smartspacer.settings.SettingsDivider
import dev.infooo.smartspacer.settings.SettingsGroup
import dev.infooo.smartspacer.settings.SettingsSectionTitle
import dev.infooo.smartspacer.settings.SettingsSwitchPreference
import dev.infooo.smartspacer.settings.SettingsTheme
import dev.infooo.smartspacer.settings.SmartspacerTargetPreview
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfigurationFragment: Fragment(), BackAvailable {

    private val viewModel by viewModel<ConfigurationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val context = LocalContext.current
            val configState by viewModel.state.collectAsState()
            val notifications by viewModel.notifications.collectAsState()
            val settings = (configState as? State.Loaded)?.targetData
                ?: com.kieronquinn.app.smartspacer.plugin.uber.targets.UberTarget.UberTargetData()
            val exampleImage = remember(context) { context.drawExampleTargetImage() }
            val activeNotification = notifications.firstOrNull()
            val previewTarget = remember(
                activeNotification,
                settings.showExpandedInfo,
                exampleImage
            ) {
                context.createUberPreviewTarget(
                    activeNotification,
                    settings.showExpandedInfo,
                    exampleImage
                )
            }

            SettingsTheme {
                PluginSettingsScaffold(title = stringResource(R.string.target_label)) {
                    SettingsSectionTitle(stringResource(R.string.settings_preview_title))
                    SmartspacerTargetPreview(
                        target = previewTarget,
                        isExample = activeNotification == null,
                        exampleLabel = stringResource(R.string.preview_example_label)
                    )
                    SettingsSectionTitle(stringResource(R.string.settings_preferences_title))
                    SettingsGroup {
                        SettingsSwitchPreference(
                            title = stringResource(R.string.configuration_show_expanded_info_title),
                            summary = stringResource(R.string.configuration_show_expanded_info_content),
                            icon = androidx.compose.material.icons.Icons.Default.Visibility,
                            checked = settings.showExpandedInfo,
                            onCheckedChange = viewModel::onShowExpandedInfoChanged
                        )
                        SettingsDivider()
                        SettingsSwitchPreference(
                            title = stringResource(R.string.configuration_hide_notification_title),
                            summary = stringResource(R.string.configuration_hide_notification_content),
                            icon = androidx.compose.material.icons.Icons.Default.NotificationsOff,
                            checked = settings.hideNotification,
                            onCheckedChange = viewModel::onHideNotificationChanged
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val smartspacerId = requireActivity().intent.getStringExtra(EXTRA_SMARTSPACER_ID) ?: return
        viewModel.setup(smartspacerId)
    }

}
