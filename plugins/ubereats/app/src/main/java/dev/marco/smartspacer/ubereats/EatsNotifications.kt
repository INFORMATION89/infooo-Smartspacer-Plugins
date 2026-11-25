package dev.marco.smartspacer.ubereats

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.Icon
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider

/** The live order, as read from Uber Eats' (or the Uber app's) notification. */
data class OrderState(
    /** Uber Eats' status line, e.g. "Preparing your order" or "Arriving at 7:42 PM" */
    val title: String,
    /** The details under it, e.g. the restaurant or "Luis is heading your way" */
    val text: String?,
    val contentIntent: PendingIntent?,
    /** The courier's photo, cropped to a circle, once a courier is assigned */
    val courier: Bitmap? = null,
    /** Short time left as Uber Eats words it on the status chip, e.g. "12 min" */
    val timeLeft: String? = null,
    /** When the order is expected, if the notification counts down to it (Live Updates) */
    val etaMillis: Long? = null,
    /** Order progress, 0..1, if the notification has a progress bar */
    val progress: Float? = null,
    /** Key of the notification, so Native Smartspace can hide it on the lock screen */
    val notificationKey: String? = null,
    /** The app the order is in: Uber Eats, or the Uber app (which has Uber Eats inside) */
    val sourcePackage: String? = null
) {
    /** "12 min": Uber Eats' own text, or else the minutes until the expected time */
    fun timeLeftText(now: Long = System.currentTimeMillis()): String? =
        timeLeft ?: etaMillis?.let { eta ->
            val min = ((eta - now + 59_999) / 60_000).toInt()
            if (min > 0) "$min min" else null
        }

    /** "60%" */
    fun progressText(): String? = progress?.let { "${(it * 100).toInt()}%" }
}

object OrderStore {
    /** The live order, or null. Compose state, so the settings preview follows it live. */
    var state by mutableStateOf<OrderState?>(null)
}

/**
 * The plugin's notification listener. Android hands a listener every app's notifications and
 * can't be told to limit them to some apps, so this drops anything that isn't from Uber Eats
 * or the Uber app immediately, looking only at the package name, before reading any content.
 */
class EatsListener : NotificationListenerService() {

    override fun onListenerConnected() {
        // Pick up an order that was already on its way before we connected
        activeNotifications.orEmpty().forEach { if (isFromUber(it)) OrderReader.onPosted(this, it) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (isFromUber(sbn)) OrderReader.onPosted(this, sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Ignore unrelated packages before forwarding even the notification key.
        if (isFromUber(sbn)) OrderReader.onRemoved(this, sbn.key)
    }

    // Notification access turned off (or the system unbinding us): we'd stop hearing about
    // the order, so don't leave its last state stuck on the card
    override fun onListenerDisconnected() = OrderReader.onLost(this)

    private fun isFromUber(sbn: StatusBarNotification) = sbn.packageName in PACKAGES

    companion object {
        const val EATS_PACKAGE = "com.ubercab.eats"
        /** The Uber app, where food can also be ordered (its Uber Eats section) */
        const val UBER_PACKAGE = "com.ubercab"
        val PACKAGES = setOf(EATS_PACKAGE, UBER_PACKAGE)
    }
}

/**
 * Turns Uber Eats' notifications into the [OrderState] the card shows.
 *
 * Uber Eats also posts promos and one-off alerts, so each notification gets a score from how
 * much it looks like live order tracking (an Android 16 Live Update, an ongoing notification,
 * a progress bar...), the same approach as the updated Uber plugin. The best one at or above
 * [MIN_SCORE] is the order; the others are ignored.
 */
object OrderReader {
    private const val TAG = "UberEats"

    // Android 16 Live Update extras; spelled out because some are @hide in the SDK
    // (values from AOSP core/java/android/app/Notification.java). Absent on older Android.
    private const val EXTRA_SHORT_CRITICAL_TEXT = "android.shortCriticalText"
    private const val TEMPLATE_PROGRESS_STYLE = "android.app.Notification\$ProgressStyle"
    private const val FLAG_PROMOTED_ONGOING = 0x00040000

    private val ORDER_CATEGORIES = setOf(
        Notification.CATEGORY_PROGRESS,
        Notification.CATEGORY_TRANSPORT,
        Notification.CATEGORY_STATUS,
        Notification.CATEGORY_NAVIGATION
    )

    /** Below this, an Uber Eats notification is most likely a promo, not the order */
    private const val MIN_SCORE = 2

    /*
     * The Uber app posts both rides and food orders, and rides belong to the Uber plugin, so
     * its notifications only count when they look like food: a channel named for orders, or
     * order words in the text with no ride words. Uber Eats' own app needs no such check.
     */
    private val FOOD_CHANNEL = Regex("eats|order|deliver|food", RegexOption.IGNORE_CASE)
    private val FOOD_WORDS = Regex(
        "\\b(order|food|restaurant|courier|delivery|delivered|pedido|comida|restaurante|repartidor|entrega)",
        RegexOption.IGNORE_CASE
    )
    private val RIDE_WORDS = Regex("\\b(driver|ride|trip|conductor|viaje)", RegexOption.IGNORE_CASE)

    private const val MAX_CANDIDATES = 16
    private const val MAX_REMEMBERED_UBER_FOOD_KEYS = 32
    /** A stuck notification should not keep order state or a remembered food key forever. */
    private const val TRACKED_NOTIFICATION_MAX_AGE_MS = 12L * 60 * 60 * 1000

    /** Uber app notifications already seen to be a food order, so ETA-only updates still count. */
    private val uberFoodKeys = BoundedNotificationKeyStore(
        maxEntries = MAX_REMEMBERED_UBER_FOOD_KEYS,
        maxAgeMillis = TRACKED_NOTIFICATION_MAX_AGE_MS
    )

    private class Candidate(val state: OrderState, val score: Int, val postTime: Long)

    /** Order-looking notifications by key (usually just one); bounded and expired defensively. */
    private val candidates = LinkedHashMap<String, Candidate>()

    private fun pruneCandidates(now: Long): Boolean {
        val cutoff = now - TRACKED_NOTIFICATION_MAX_AGE_MS
        val expiredKeys = candidates.entries
            .filter { it.value.postTime < cutoff }
            .map { it.key }
        expiredKeys.forEach(candidates::remove)

        var changed = expiredKeys.isNotEmpty()
        while (candidates.size > MAX_CANDIDATES) {
            val oldestKey = candidates.minByOrNull { it.value.postTime }?.key ?: break
            candidates.remove(oldestKey)
            changed = true
        }
        return changed
    }

    private fun pruneTrackedState(now: Long = System.currentTimeMillis()): Boolean {
        uberFoodKeys.prune(now)
        return pruneCandidates(now)
    }

    @Synchronized
    fun onPosted(context: Context, sbn: StatusBarNotification) {
        val now = System.currentTimeMillis()
        pruneTrackedState(now)
        val isDebug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebug) logNotification(sbn)
        val parsed = runCatching { parse(context, sbn) }
            .onFailure { Log.w(TAG, "Couldn't read an Uber Eats notification (${it.javaClass.simpleName})") }
            .getOrNull()
        if (parsed != null && parsed.score >= MIN_SCORE) candidates[sbn.key] = parsed
        else candidates.remove(sbn.key)
        pruneTrackedState(now)
        publish(context)
    }

    @Synchronized
    fun onRemoved(context: Context, key: String) {
        uberFoodKeys.remove(key)
        val expired = pruneCandidates(System.currentTimeMillis())
        if (candidates.remove(key) != null || expired) publish(context)
    }

    @Synchronized
    fun onLost(context: Context) {
        candidates.clear()
        uberFoodKeys.clear()
        publish(context)
    }

    /** Shows the best candidate, and tells Smartspacer only when something visible changed. */
    private fun publish(context: Context) {
        val best = candidates.values.maxWithOrNull(compareBy<Candidate> { it.score }.thenBy { it.postTime })?.state
        val old = OrderStore.state
        if (best == null) {
            if (old != null) {
                OrderStore.state = null
                SampleOrder.reroll() // the settings preview shows a fresh example from now on
                notifyTarget(context)
            }
            return
        }
        // Uber Eats re-posts the notification often with nothing visible changed: skip those,
        // so Smartspacer doesn't redraw the card and get sent the same photo again
        if (old != null && old.looksSameAs(best)) {
            OrderStore.state = old.copy(contentIntent = best.contentIntent) // keep the tap action fresh
            return
        }
        OrderStore.state = best
        notifyTarget(context)
    }

    private fun parse(context: Context, sbn: StatusBarNotification): Candidate? {
        val n = sbn.notification
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return null
        val extras = n.extras ?: Bundle.EMPTY
        fun text(key: String) = extras.getCharSequence(key)?.toString()?.trim()?.takeIf { it.isNotEmpty() }

        // Uber Eats' own text, in whatever language Uber Eats uses
        val title = text(Notification.EXTRA_TITLE_BIG) ?: text(Notification.EXTRA_TITLE) ?: return null
        val body = text(Notification.EXTRA_TEXT) ?: text(Notification.EXTRA_BIG_TEXT) ?: text(Notification.EXTRA_SUB_TEXT)
        val shortCritical = text(EXTRA_SHORT_CRITICAL_TEXT)
        if (sbn.packageName == EatsListener.UBER_PACKAGE && !isUberFood(sbn, title, body)) return null

        val progress = extras.progress()
        val isProgressStyle = extras.getString(Notification.EXTRA_TEMPLATE) == TEMPLATE_PROGRESS_STYLE
        val isPromoted = n.flags and FLAG_PROMOTED_ONGOING != 0
        val isOngoing = n.flags and Notification.FLAG_ONGOING_EVENT != 0
        // A countdown to the expected time ("when" in the future, shown as a timer)
        val eta = n.`when`.takeIf {
            extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN, false) && it > System.currentTimeMillis()
        }
        val courier = n.getLargeIcon()?.bitmap(context)?.circleCrop()

        val score = listOf(
            isProgressStyle to 3,
            isPromoted to 2,
            (progress != null) to 2,
            isOngoing to 1,
            (n.category in ORDER_CATEGORIES) to 1,
            (eta != null || shortCritical != null) to 1
        ).sumOf { (matches, points) -> if (matches) points else 0 }

        return Candidate(
            OrderState(
                title = title,
                // Don't repeat the title or the time chip in the details
                text = body?.takeIf { it != title && it != shortCritical },
                contentIntent = n.contentIntent,
                courier = courier,
                timeLeft = shortCritical?.takeIf { it != title },
                etaMillis = eta,
                progress = progress,
                notificationKey = sbn.key,
                sourcePackage = sbn.packageName
            ),
            score,
            sbn.postTime
        )
    }

    /** Whether a notification from the Uber app is a food order (not a ride). */
    private fun isUberFood(sbn: StatusBarNotification, title: String, body: String?): Boolean {
        val now = System.currentTimeMillis()
        if (uberFoodKeys.containsAndRefresh(sbn.key, now)) return true
        val words = listOfNotNull(title, body).joinToString(" ")
        val food = sbn.notification.channelId?.let { FOOD_CHANNEL.containsMatchIn(it) } == true ||
            (FOOD_WORDS.containsMatchIn(words) && !RIDE_WORDS.containsMatchIn(words))
        if (food) uberFoodKeys.remember(sbn.key, now)
        return food
    }

    private fun Bundle.progress(): Float? {
        if (getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)) return null
        val max = getInt(Notification.EXTRA_PROGRESS_MAX, 0)
        if (max <= 0) return null
        return (getInt(Notification.EXTRA_PROGRESS, 0) / max.toFloat()).coerceIn(0f, 1f)
    }

    private fun Icon.bitmap(context: Context): Bitmap? = runCatching {
        val d = loadDrawable(context) ?: return null
        val w = d.intrinsicWidth.takeIf { it > 0 } ?: 192
        val h = d.intrinsicHeight.takeIf { it > 0 } ?: 192
        // Software copy: it's cropped here and sent over Binder later
        d.toBitmap(w, h).copy(Bitmap.Config.ARGB_8888, false)
    }.getOrNull()

    /** Same text, time, progress and whether there's a photo (bitmaps are new every time). */
    private fun OrderState.looksSameAs(other: OrderState) =
        title == other.title && text == other.text && timeLeft == other.timeLeft &&
            etaMillis?.div(60_000) == other.etaMillis?.div(60_000) &&
            progressText() == other.progressText() && notificationKey == other.notificationKey &&
            sourcePackage == other.sourcePackage &&
            (courier == null) == (other.courier == null)

    /**
     * Debug builds only: one line per Uber Eats or Uber notification, so the reading can be tuned
     * against a real order. View with `adb logcat -s UberEats`.
     */
    private fun logNotification(sbn: StatusBarNotification) {
        val n = sbn.notification
        val extras = n.extras
        val extraTypes = extras?.keySet()?.sorted()?.joinToString { key ->
            @Suppress("DEPRECATION")
            val value = runCatching { extras.get(key) }.getOrNull()
            "$key:${value?.javaClass?.simpleName ?: "null"}"
        }
        Log.d(TAG, "notification: source=${sbn.packageName} flags=0x${n.flags.toString(16)} " +
            "category=${n.category ?: "none"} channel=${n.channelId ?: "none"} " +
            "hasTitle=${extras?.containsKey(Notification.EXTRA_TITLE) == true} " +
            "hasBody=${extras?.containsKey(Notification.EXTRA_TEXT) == true} " +
            "extraTypes=[$extraTypes]")
    }

    private fun notifyTarget(context: Context) =
        SmartspacerTargetProvider.notifyChange(context, UberEatsTarget::class.java)
}

/** The bitmap cropped to a centered circle (for the courier's photo). */
fun Bitmap.circleCrop(): Bitmap {
    val size = minOf(width, height)
    val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = BitmapShader(this@circleCrop, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            setLocalMatrix(android.graphics.Matrix().apply {
                setTranslate(-(width - size) / 2f, -(height - size) / 2f)
            })
        }
    }
    Canvas(out).drawCircle(size / 2f, size / 2f, size / 2f, paint)
    return out
}
