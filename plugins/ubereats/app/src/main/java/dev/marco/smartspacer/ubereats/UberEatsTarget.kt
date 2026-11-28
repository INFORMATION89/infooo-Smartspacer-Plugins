package dev.marco.smartspacer.ubereats

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate

/**
 * The card's subtitle: Uber Eats' details (restaurant, courier...) and the time left or the
 * progress, joined with " · ". The time goes first when the line is long.
 */
fun cardSubtitle(state: OrderState, settings: Settings): String? {
    val extra = when (settings.extra) {
        Settings.Extra.TIME_LEFT -> state.timeLeftText()
        Settings.Extra.PROGRESS -> state.progressText()
        Settings.Extra.NONE -> null
    }
    val base = state.text
    val parts = if ((base?.length ?: 0) > LONG_LINE) listOf(extra, base) else listOf(base, extra)
    return parts.filterNotNull().joinToString(" · ").takeIf { it.isNotEmpty() }
}

const val SOURCE_BULLET = " ·"
const val SOURCE_NAME = "Uber Eats"
private const val LONG_LINE = 16

/**
 * Builds the production card and the Settings preview from the same Smartspacer template.
 * Preview mode omits tap actions and notification metadata, leaving production state untouched.
 */
internal fun buildUberEatsTarget(
    context: Context,
    state: OrderState,
    settings: Settings,
    smartspacerId: String,
    isPreview: Boolean = false
): SmartspaceTarget {
    val pkg = state.sourcePackage ?: EatsIcon.defaultPackage(context)
    val name = EatsIcon.name(pkg)
    val openOrder = if (isPreview) null else TapAction(
        pendingIntent = state.contentIntent,
        intent = if (state.contentIntent == null) context.packageManager.getLaunchIntentForPackage(pkg) else null
    )

    val appIcon = EatsIcon.get(context, pkg)
        ?.let { Icon(AndroidIcon.createWithBitmap(it), contentDescription = name, shouldTint = false) }
    val courier = state.courier?.takeIf { settings.showCourier }
        ?.let { Icon(AndroidIcon.createWithBitmap(it), shouldTint = false) }
    val icon = courier ?: appIcon ?: Icon(AndroidIcon.createWithResource(context, R.drawable.ic_order))

    val source = EatsIcon.glyph(context, pkg)?.let {
        ComplicationTemplate.Basic(
            "uber_eats_source_$smartspacerId",
            Icon(AndroidIcon.createWithBitmap(it), contentDescription = name, shouldTint = true),
            Text(EatsIcon.afterLogo(pkg)),
            openOrder
        ).create()
    }

    return TargetTemplate.Basic(
        "uber_eats_$smartspacerId",
        ComponentName(context, UberEatsTarget::class.java),
        SmartspaceTarget.FEATURE_UNDEFINED,
        Text(state.title),
        cardSubtitle(state, settings)?.let { Text(if (source != null) it + SOURCE_BULLET else it) },
        icon,
        openOrder,
        source
    ).create().apply {
        if (!isPreview && settings.hideNotification) {
            sourceNotificationKey = state.notificationKey
        }
    }
}

class UberEatsTarget : SmartspacerTargetProvider() {

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val state = OrderStore.state ?: return emptyList()
        val context = provideContext()
        return listOf(buildUberEatsTarget(context, state, Settings(context), smartspacerId))
    }

    override fun getConfig(smartspacerId: String?): Config {
        val context = provideContext()
        return Config(
            label = context.getString(R.string.target_label),
            description = context.getString(R.string.target_description),
            icon = AndroidIcon.createWithResource(context, R.drawable.ic_order),
            configActivity = Intent(context, SettingsActivity::class.java)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean = false
}
