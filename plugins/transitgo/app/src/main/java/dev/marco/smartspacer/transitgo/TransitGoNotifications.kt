package dev.marco.smartspacer.transitgo

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider

/** The live GO trip, as read from Transit's notification. */
data class GoState(
    /** Transit's instruction, e.g. "Leave in 5 minutes" */
    val title: String,
    /** The next step, e.g. "in 16 minutes" */
    val text: String?,
    val contentIntent: PendingIntent?,
    /** Transit's route chip (e.g. green "57-A"), or a bike/other chip, drawn on a square */
    val chip: Bitmap? = null,
    val routeName: String? = null,
    /** Transit's icon for the current step (wait, walk, ride, bike...) */
    val stepIcon: Bitmap? = null,
    /** Arrival time, e.g. "6:44 AM" */
    val arrival: String? = null,
    /** Time left in the trip, e.g. "3 h 07 min" */
    val timeLeft: String? = null,
    /** Transit's small line above the title, e.g. "Walk to Central Station" on bike-station steps */
    val previous: String? = null,
    /** Key of Transit's GO notification, so Native Smartspace can hide it on the lock screen */
    val notificationKey: String? = null
)

object GoStore {
    /** The live trip, or null. Compose state, so the settings preview follows it live. */
    var state by mutableStateOf<GoState?>(null)
}

/**
 * The plugin's notification listener. Android hands a listener every app's notifications and
 * can't be told to limit them to one app, so this drops anything that isn't Transit's GO channel
 * immediately, looking only at the package name and channel, before reading any content.
 *
 * (Smartspacer's own listener, via a SmartspacerNotificationProvider like the Uber plugin uses,
 * was tried first, but Smartspacer didn't deliver Transit's notifications to it.)
 */
class TransitGoListener : NotificationListenerService() {

    override fun onListenerConnected() {
        val active = activeNotifications.orEmpty()
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            // Counts only, no content
            Log.d("TransitGo", "listener connected: delivered=${active.size} " +
                "transit=${active.count { it.packageName == TransitNotificationContract.PACKAGE_NAME }} go=${active.count { isTransitGo(it) }}")
        }
        // Pick up a trip that was already running before we connected
        active.forEach { if (isTransitGo(it)) TripReader.onPosted(this, it) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (isTransitGo(sbn)) TripReader.onPosted(this, sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Drop unrelated notifications before even comparing their notification keys.
        if (isTransitGo(sbn)) TripReader.onRemoved(this, sbn.key)
    }

    // Notification access turned off (or the system unbinding us): we'd stop hearing about
    // the trip, so don't leave its last state stuck on the card
    override fun onListenerDisconnected() = TripReader.onLost(this)

    private fun isTransitGo(sbn: StatusBarNotification) =
        TransitGoLogic.isTransitNotification(sbn.packageName, sbn.notification.channelId)

    companion object {
    }
}

/** Turns Transit's notifications into the [GoState] the card shows. */
// bigContentView is deprecated for apps building notifications, but it's the only way to
// read another app's custom expanded layout
@Suppress("DEPRECATION")
object TripReader {
    private const val TAG = "TransitGo"

    /** A Transit GO-channel notification was posted or updated. */
    fun onPosted(context: Context, sbn: StatusBarNotification) {
        val isDebug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebug) logGoNotification(sbn)
        if (isLiveTrip(sbn)) update(context, sbn, isDebug)
    }

    /** Some notification was removed: clear the card if it was the live trip's. */
    fun onRemoved(context: Context, key: String) {
        if (TransitGoLogic.shouldClearTrip(GoStore.state?.notificationKey, key)) clearTrip(context)
    }

    /** We stopped receiving notifications. */
    fun onLost(context: Context) {
        if (GoStore.state != null) clearTrip(context)
    }

    /**
     * Transit posts several notifications on go_mode: an empty group summary, one-off alerts
     * (AUTO_CANCEL), and the live trip, which is the foreground-service one.
     */
    private fun isLiveTrip(sbn: StatusBarNotification): Boolean {
        return TransitGoLogic.isLiveTrip(sbn.notification.flags)
    }

    private fun clearTrip(context: Context) {
        GoStore.state = null
        SampleTrip.reroll() // the settings preview shows a fresh example from now on
        notifyTarget(context)
    }

    private fun update(context: Context, sbn: StatusBarNotification, isDebug: Boolean) {
        val n = sbn.notification
        // Transit's own text, in whatever language Transit uses (e.g. "Leave in 5 minutes").
        // Trimmed: on bike steps it's " 0 bikes", with a space where Transit draws an inline icon
        val title = n.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
            ?.takeIf { it.isNotEmpty() }?.let(TransitGoLogic::withBikesLeft) ?: "Transit GO"
        // Observed: text = null and subText = the next step ("in 16 minutes"); the details are
        // in the custom layout, which ChipRenderer reads
        val text = (n.extras.getCharSequence(Notification.EXTRA_TEXT)
            ?: n.extras.getCharSequence(Notification.EXTRA_SUB_TEXT))?.toString()?.trim()?.takeIf { it.isNotEmpty() }

        val r = ChipRenderer.render(context, n.bigContentView)

        if (isDebug) {
            // Structural diagnostics only: never write notification or trip text to Logcat.
            Log.d(TAG, "update: flags=0x${n.flags.toString(16)} " +
                "hasTitle=${title != "Transit GO"} hasText=${text != null} " +
                "hasCustomLayout=${n.bigContentView != null} route=${r?.routeName != null} " +
                "chip=${r?.chip != null} step=${r?.stepIcon != null} " +
                "arrival=${r?.arrival != null} timeLeft=${r?.timeLeft != null} " +
                "previous=${r?.previous != null}")
        }

        val new = GoState(
            title = title,
            text = text,
            contentIntent = n.contentIntent,
            chip = r?.chip,
            routeName = r?.routeName,
            stepIcon = r?.stepIcon,
            arrival = r?.arrival,
            timeLeft = r?.timeLeft,
            previous = r?.previous,
            notificationKey = sbn.key
        )
        // Transit re-posts the notification often with nothing visible changed: skip those, so
        // Smartspacer doesn't redraw the card and get sent the same icons again
        val old = GoStore.state
        if (old != null && TransitGoLogic.hasSameVisibleContent(old.visibleContent(), new.visibleContent())) {
            GoStore.state = old.copy(contentIntent = new.contentIntent) // keep the tap action fresh
            return
        }
        GoStore.state = new
        notifyTarget(context)
    }

    /** Same visible content despite Transit redrawing bitmaps for every notification update. */
    private fun GoState.visibleContent() = TransitGoLogic.VisibleTripContent(
        title = title,
        text = text,
        previous = previous,
        routeName = routeName,
        arrival = arrival,
        timeLeft = timeLeft,
        notificationKey = notificationKey,
        hasChip = chip != null,
        hasStepIcon = stepIcon != null
    )

    /** Debug-only structural diagnostics; never logs notification text or identifiers. */
    private fun logGoNotification(sbn: StatusBarNotification) {
        val n = sbn.notification
        val extras = n.extras
        Log.d(TAG, "go_mode: live=${isLiveTrip(sbn)} flags=0x${n.flags.toString(16)} " +
            "hasTitle=${extras.getCharSequence(Notification.EXTRA_TITLE) != null} " +
            "hasText=${extras.getCharSequence(Notification.EXTRA_TEXT) != null} " +
            "hasSubText=${extras.getCharSequence(Notification.EXTRA_SUB_TEXT) != null} " +
            "hasCustomLayout=${n.bigContentView != null}")
    }

    private fun notifyTarget(context: Context) =
        SmartspacerTargetProvider.notifyChange(context, TransitGoTarget::class.java)
}
