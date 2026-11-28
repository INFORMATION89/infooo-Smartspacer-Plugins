package dev.marco.smartspacer.transitgo

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
 * The card's subtitle: Transit's next step (or, when there's none, as on bike-station steps,
 * the line above the title like "Walk to Central Station"), and the arrival time or time left.
 */
fun cardSubtitle(state: GoState, settings: Settings): String? {
    val extra = when (settings.extra) {
        Settings.Extra.ARRIVAL -> state.arrival
        Settings.Extra.TIME_LEFT -> state.timeLeft
        Settings.Extra.NONE -> null
    }
    val base = state.text ?: state.previous
    val parts = if ((base?.length ?: 0) > LONG_LINE) listOf(extra, base) else listOf(base, extra)
    return parts.filterNotNull().joinToString(" · ").takeIf { it.isNotEmpty() }
}

const val SOURCE_BULLET = " ·"
private const val LONG_LINE = 16

/**
 * Uses the same Smartspacer template for the production target and Settings preview.
 * Preview mode removes actions and notification metadata without changing the live trip.
 */
internal fun buildTransitGoTarget(
    context: Context,
    state: GoState,
    settings: Settings,
    smartspacerId: String,
    isPreview: Boolean = false
): SmartspaceTarget {
    val openTrip = if (isPreview) null else TapAction(
        pendingIntent = state.contentIntent,
        intent = if (state.contentIntent == null) {
            context.packageManager.getLaunchIntentForPackage(TransitNotificationContract.PACKAGE_NAME)
        } else null
    )

    val icon = state.chip?.takeIf { settings.showRouteChip }
        ?.let { Icon(AndroidIcon.createWithBitmap(it), contentDescription = state.routeName, shouldTint = false) }
        ?: state.stepIcon
            ?.let { Icon(AndroidIcon.createWithBitmap(it), shouldTint = true) }
        ?: TransitIcon.get(context)
            ?.let { Icon(AndroidIcon.createWithBitmap(it), contentDescription = "Transit", shouldTint = false) }
        ?: Icon(AndroidIcon.createWithResource(context, R.drawable.ic_transit_go))

    val source = TransitIcon.glyph(context)?.let {
        ComplicationTemplate.Basic(
            "transit_go_source_$smartspacerId",
            Icon(AndroidIcon.createWithBitmap(it), contentDescription = "Transit", shouldTint = true),
            Text("Transit"),
            openTrip
        ).create()
    }

    return TargetTemplate.Basic(
        "transit_go_$smartspacerId",
        ComponentName(context, TransitGoTarget::class.java),
        SmartspaceTarget.FEATURE_UNDEFINED,
        Text(state.title),
        cardSubtitle(state, settings)?.let { Text(if (source != null) it + SOURCE_BULLET else it) },
        icon,
        openTrip,
        source
    ).create().apply {
        if (!isPreview && settings.hideNotification) {
            sourceNotificationKey = state.notificationKey
        }
    }
}

class TransitGoTarget : SmartspacerTargetProvider() {

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val state = GoStore.state ?: return emptyList()
        val context = provideContext()
        return listOf(buildTransitGoTarget(context, state, Settings(context), smartspacerId))
    }

    override fun getConfig(smartspacerId: String?): Config {
        val context = provideContext()
        return Config(
            label = context.getString(R.string.target_label),
            description = context.getString(R.string.target_description),
            icon = AndroidIcon.createWithResource(context, R.drawable.ic_transit_go),
            configActivity = Intent(context, SettingsActivity::class.java)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean = false
}
