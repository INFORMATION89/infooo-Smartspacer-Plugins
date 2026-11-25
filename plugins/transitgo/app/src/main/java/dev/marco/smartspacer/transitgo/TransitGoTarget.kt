package dev.marco.smartspacer.transitgo

import android.content.ComponentName
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
 * the line above the title like "Walk to Central Station"), and the arrival time or time left,
 * joined with " · ". The time goes first when the line is long.
 */
fun cardSubtitle(state: GoState, settings: Settings): String? {
    val extra = when (settings.extra) {
        Settings.Extra.ARRIVAL -> state.arrival
        Settings.Extra.TIME_LEFT -> state.timeLeft
        Settings.Extra.NONE -> null
    }
    val base = state.text ?: state.previous
    // Long lines get cut off at the end, so put the short time first:
    //   "in 16 minutes · 6:44 AM"  but  "16 min · Walk to Main St & 5th Ave…"
    val parts = if ((base?.length ?: 0) > LONG_LINE) listOf(extra, base) else listOf(base, extra)
    return parts.filterNotNull().joinToString(" · ").takeIf { it.isNotEmpty() }
}

/** Divider between the subtitle and "[logo] Transit" */
const val SOURCE_BULLET = " ·"

/** Past this many characters the subtitle is likely to be cut off on the card */
private const val LONG_LINE = 16

class TransitGoTarget : SmartspacerTargetProvider() {

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val state = GoStore.state ?: return emptyList()
        val ctx = provideContext()
        val settings = Settings(ctx)
        // Tapping opens Transit on the trip (or just Transit if the notification has no intent)
        val openTrip = TapAction(
            pendingIntent = state.contentIntent,
            intent = if (state.contentIntent == null) {
                ctx.packageManager.getLaunchIntentForPackage(TransitNotificationContract.PACKAGE_NAME)
            } else null
        )

        // Route chip (full color) > step icon (tinted like Smartspacer's text)
        //   > Transit's app icon (full color) > bus glyph
        val icon = state.chip?.takeIf { settings.showRouteChip }
            ?.let { Icon(AndroidIcon.createWithBitmap(it), contentDescription = state.routeName, shouldTint = false) }
            ?: state.stepIcon
                ?.let { Icon(AndroidIcon.createWithBitmap(it), shouldTint = true) }
            ?: TransitIcon.get(ctx)
                ?.let { Icon(AndroidIcon.createWithBitmap(it), contentDescription = "Transit", shouldTint = false) }
            ?: Icon(AndroidIcon.createWithResource(ctx, R.drawable.ic_transit_go))

        // "[logo] Transit" after the subtitle (the supplemental spot, where weather usually
        // shows), so it's clear the info comes from Transit. Logo only, tinted like the text.
        val source = TransitIcon.glyph(ctx)?.let {
            ComplicationTemplate.Basic(
                "transit_go_source_$smartspacerId",
                Icon(AndroidIcon.createWithBitmap(it), contentDescription = "Transit", shouldTint = true),
                Text("Transit"),
                openTrip
            ).create()
        }

        return listOf(
            TargetTemplate.Basic(
                "transit_go_$smartspacerId",
                ComponentName(ctx, TransitGoTarget::class.java),
                SmartspaceTarget.FEATURE_UNDEFINED,
                Text(state.title),
                // A bullet at the end divides it from "[logo] Transit", which Smartspacer draws
                // right after the subtitle
                cardSubtitle(state, settings)?.let { Text(if (source != null) it + SOURCE_BULLET else it) },
                icon,
                openTrip,
                source
            ).create().apply {
                // Same trick as the Uber plugin: with Native Smartspace, the lock screen hides
                // this notification while our card is the first one showing. Nothing is cancelled.
                if (settings.hideNotification) sourceNotificationKey = state.notificationKey
            }
        )
    }

    override fun getConfig(smartspacerId: String?): Config {
        val ctx = provideContext()
        return Config(
            label = ctx.getString(R.string.target_label),
            description = ctx.getString(R.string.target_description),
            icon = AndroidIcon.createWithResource(ctx, R.drawable.ic_transit_go),
            configActivity = Intent(ctx, SettingsActivity::class.java)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean = false
}
