package com.kieronquinn.app.smartspacer.plugin.uber.notifications

import android.app.Notification
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugin.uber.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.uber.R
import com.kieronquinn.app.smartspacer.plugin.uber.UberPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.uber.model.NotificationData
import com.kieronquinn.app.smartspacer.plugin.uber.repositories.NotificationRepository
import com.kieronquinn.app.smartspacer.plugin.uber.utils.extensions.circleCrop
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerNotificationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import com.kieronquinn.app.smartspacer.sdk.utils.getResourceForIdentifier
import org.koin.android.ext.android.inject

/**
 *  Reads Uber's trip notification and turns it into [NotificationData].
 *
 *  The original version of this plugin only understood Uber's private custom notification layout
 *  (`ub__rich_notification_custom_big`) and dropped the notification entirely if that layout, or
 *  any one of nine internal view IDs, was missing. Uber has since redesigned its trip
 *  notifications around Android 16's Live Updates (`Notification.ProgressStyle`), so on current
 *  versions nothing matched and the Target never appeared.
 *
 *  This version reads the notification in two ways and merges the results:
 *
 *  1. Standard notification fields (title, text, large icon, progress, and the Android 16
 *     ProgressStyle extras such as the tracker icon). These are public and stable, so this is the
 *     main path for current Uber versions.
 *  2. Uber's custom RemoteViews, if present. Every field is optional and the layout name is no
 *     longer required to match, so older Uber versions keep their richer driver/car images.
 */
class UberNotificationProvider: SmartspacerNotificationProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.notifications.uber"

        private const val TAG = "UberPlugin"

        //Legacy: Uber's own rich notification layout, used before the Live Updates redesign
        private const val IDENTIFIER_LAYOUT = "$PACKAGE_NAME:layout/ub__rich_notification_custom_big"
        private const val IDENTIFIER_TITLE = "$PACKAGE_NAME:id/ub__rich_notification_title"
        private const val IDENTIFIER_SUBTITLE = "$PACKAGE_NAME:id/ub__rich_notification_subtitle"
        private const val IDENTIFIER_PROGRESS_CAR = "$PACKAGE_NAME:id/progress_bar_indicator"
        private const val IDENTIFIER_PROGRESS = "$PACKAGE_NAME:id/progress_bar_filled"
        private const val IDENTIFIER_DRIVER = "$PACKAGE_NAME:id/ub__rich_notification_big_left_image"
        private const val IDENTIFIER_CAR = "$PACKAGE_NAME:id/ub__rich_notification_big_right_image"
        private const val IDENTIFIER_EXPANDED_TITLE =
            "$PACKAGE_NAME:id/ub__rich_notification_expanded_title"
        private const val IDENTIFIER_EXPANDED_SUBTITLE =
            "$PACKAGE_NAME:id/ub__rich_notification_expanded_subtitle"

        /*
         *  Android 16 (API 36) ProgressStyle extras. Several of these are @hide in the SDK, so the
         *  raw keys are spelled out here (values taken from AOSP android16-release
         *  core/java/android/app/Notification.java). Using strings also means this compiles and
         *  runs on every Android version: on older versions the keys are simply absent.
         */
        private const val EXTRA_PROGRESS_TRACKER_ICON = "android.progressTrackerIcon"
        private const val EXTRA_SHORT_CRITICAL_TEXT = "android.shortCriticalText"
        private const val TEMPLATE_PROGRESS_STYLE = "android.app.Notification\$ProgressStyle"
        private const val FLAG_PROMOTED_ONGOING = 0x00040000

        private val TRIP_CATEGORIES = setOf(
            Notification.CATEGORY_TRANSPORT,
            Notification.CATEGORY_NAVIGATION,
            Notification.CATEGORY_PROGRESS
        )

        //Below this, an Uber notification probably isn't the active trip (eg. a background service)
        private const val MIN_SCORE = 2
        //Fallback size for drawables with no intrinsic size (eg. some vectors)
        private const val FALLBACK_ICON_SIZE = 192
    }

    private val notificationRepository by inject<NotificationRepository>()

    override fun getConfig(smartspacerId: String): Config {
        return Config(setOf(PACKAGE_NAME))
    }

    override fun onNotificationsChanged(
        smartspacerId: String,
        isListenerEnabled: Boolean,
        notifications: List<StatusBarNotification>
    ) {
        //Only one trip can be active at a time, so show the single best match
        val data = notifications.mapNotNull {
            try {
                it.parse()
            }catch (e: Exception) {
                Log.w(TAG, "Failed to parse Uber notification (${e.javaClass.simpleName})")
                null
            }
        }.filter {
            it.score >= MIN_SCORE
        }.sortedWith(
            compareByDescending<NotificationData> { it.score }
                .thenByDescending { it.notification.postTime }
        ).take(1)
        notificationRepository.setNotifications(data)
    }

    @Suppress("DEPRECATION")
    private fun StatusBarNotification.parse(): NotificationData? {
        val n = notification
        if(n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return null
        val extras = n.extras ?: Bundle.EMPTY
        val custom = parseCustomViews(n)
        val title = custom?.title
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()?.takeIfNotBlank()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.takeIfNotBlank()
            ?: return null
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.takeIfNotBlank()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.takeIfNotBlank()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.takeIfNotBlank()
        val shortCriticalText = extras.getCharSequence(EXTRA_SHORT_CRITICAL_TEXT)
            ?.toString()?.takeIfNotBlank()
        val subtitle = custom?.subtitle ?: text ?: bigText ?: subText ?: shortCriticalText ?: ""
        val isProgressStyle = extras.getString(Notification.EXTRA_TEMPLATE) == TEMPLATE_PROGRESS_STYLE
        //Standard progress first: the legacy value is estimated from view padding
        val progress = extras.getProgress() ?: custom?.progress
        val trackerIcon = extras.getIconCompat(EXTRA_PROGRESS_TRACKER_ICON)?.loadBitmap()
            ?: custom?.progressCar
        val largeIcon = n.getLargeIcon()?.loadBitmap()
            ?: extras.getBitmapCompat(Notification.EXTRA_LARGE_ICON)
        val driver = custom?.driver ?: largeIcon?.circleCrop()
        val car = custom?.car
            ?: extras.getIconCompat(Notification.EXTRA_PICTURE_ICON)?.loadBitmap()
            ?: extras.getBitmapCompat(Notification.EXTRA_PICTURE)
        //Text inside the image: avoid repeating the Target's own title/subtitle where possible
        val expandedTitle = custom?.expandedTitle
            ?: shortCriticalText?.takeIf { it != title }
            ?: subText?.takeIf { it != subtitle }
            ?: subtitle.takeIfNotBlank()
        val expandedSubtitle = custom?.expandedSubtitle
            ?: listOfNotNull(bigText, subText, text).firstOrNull {
                it != subtitle && it != expandedTitle
            }
        val isOngoing = n.flags and Notification.FLAG_ONGOING_EVENT != 0
        val isPromoted = n.flags and FLAG_PROMOTED_ONGOING != 0
        val score = listOf(
            (custom?.isRichLayout == true) to 3,
            isProgressStyle to 3,
            isPromoted to 2,
            (progress != null) to 2,
            isOngoing to 1,
            (n.category?.let { it in TRIP_CATEGORIES } == true) to 1,
            (driver != null || car != null) to 1
        ).sumOf { (matches, points) -> if(matches) points else 0 }
        return NotificationData(
            this,
            n.smallIcon ?: Icon.createWithResource(provideContext(), R.drawable.ic_uber),
            title,
            subtitle,
            progress,
            trackerIcon,
            driver,
            car,
            expandedTitle,
            expandedSubtitle,
            score
        )
    }

    private fun Bundle.getProgress(): Float? {
        if(getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)) return null
        val max = getInt(Notification.EXTRA_PROGRESS_MAX, 0)
        if(max <= 0) return null
        val progress = getInt(Notification.EXTRA_PROGRESS, 0)
        return (progress / max.toFloat()).coerceIn(0f, 1f)
    }

    /**
     *  Fields read from Uber's own custom notification views. All are optional: older Uber
     *  versions fill most of them, newer ones may fill none.
     */
    private data class CustomViewData(
        val isRichLayout: Boolean,
        val title: String?,
        val subtitle: String?,
        val progress: Float?,
        val progressCar: Bitmap?,
        val driver: Bitmap?,
        val car: Bitmap?,
        val expandedTitle: String?,
        val expandedSubtitle: String?
    )

    @Suppress("DEPRECATION")
    private fun parseCustomViews(notification: Notification): CustomViewData? {
        val remoteViews = listOfNotNull(
            notification.bigContentView,
            notification.headsUpContentView,
            notification.contentView
        )
        if(remoteViews.isEmpty()) return null
        val uberContext = createUberContext()
        val richLayoutId = uberContext?.getResourceForIdentifier(IDENTIFIER_LAYOUT)
        val isRichLayout = richLayoutId != null && remoteViews.any { it.layoutId == richLayoutId }
        val views = remoteViews.mapNotNull { it.load() }
        if(views.isEmpty()) return null
        fun text(identifier: String) = views.firstNotNullOfOrNull {
            it.findViewByIdentifier<TextView>(identifier)?.text?.toString()?.takeIfNotBlank()
        }
        fun image(identifier: String) = views.firstNotNullOfOrNull {
            it.findViewByIdentifier<ImageView>(identifier)?.drawable?.toBitmapSafely()
        }
        val progress = views.firstNotNullOfOrNull {
            it.findViewByIdentifier<FrameLayout>(IDENTIFIER_PROGRESS)
        }?.let { calculateProgress(it.paddingLeft) }
        return CustomViewData(
            isRichLayout,
            text(IDENTIFIER_TITLE),
            text(IDENTIFIER_SUBTITLE),
            progress,
            image(IDENTIFIER_PROGRESS_CAR),
            image(IDENTIFIER_DRIVER),
            image(IDENTIFIER_CAR),
            text(IDENTIFIER_EXPANDED_TITLE),
            text(IDENTIFIER_EXPANDED_SUBTITLE)
        )
    }

    private fun RemoteViews.load(): View? {
        return try {
            apply(provideContext(), null)
        }catch (e: Exception) {
            Log.d(TAG, "Unable to load Uber RemoteViews (${e.javaClass.simpleName})")
            null
        }
    }

    private fun createUberContext(): Context? {
        return try {
            provideContext().createPackageContext(PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY)
        }catch (e: Exception){
            null
        }
    }

    private fun Icon.loadBitmap(): Bitmap? {
        return try {
            loadDrawable(provideContext())?.toBitmapSafely()
        }catch (e: Exception) {
            Log.d(TAG, "Unable to load Uber notification icon (${e.javaClass.simpleName})")
            null
        }
    }

    private fun Drawable.toBitmapSafely(): Bitmap? {
        return try {
            val width = intrinsicWidth.takeIf { it > 0 } ?: FALLBACK_ICON_SIZE
            val height = intrinsicHeight.takeIf { it > 0 } ?: FALLBACK_ICON_SIZE
            //Always copy into a software bitmap: later scaling/cropping must never touch a bitmap
            //the notification owns, and hardware bitmaps can't be drawn onto our Canvas
            toBitmap(width, height).copy(Bitmap.Config.ARGB_8888, false)
        }catch (e: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun Bundle.getIconCompat(key: String): Icon? {
        return getParcelable<Parcelable>(key) as? Icon
    }

    @Suppress("DEPRECATION")
    private fun Bundle.getBitmapCompat(key: String): Bitmap? {
        return when(val value = getParcelable<Parcelable>(key)) {
            is Bitmap -> value
            is Icon -> value.loadBitmap()
            else -> null
        }
    }

    private fun String.takeIfNotBlank(): String? = takeIf { it.isNotBlank() }

    private fun calculateProgress(leftPadding: Int): Float {
        return (leftPadding / getUsableWidth().toFloat()).coerceIn(0f, 1f)
    }

    private fun getUsableWidth(): Int {
        val displayWidth = Resources.getSystem().displayMetrics.widthPixels
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            displayWidth - this.edgeDistancePreS()
        } else {
            displayWidth - this.edgeDistanceS()
        }
    }

    private fun edgeDistanceS(): Int {
        return provideContext().resources.getDimensionPixelSize(
            R.dimen.progress_bar_expanded_screen_edges_distance_s)
    }

    private fun edgeDistancePreS(): Int {
        return provideContext().resources.getDimensionPixelSize(
            R.dimen.progress_bar_expanded_screen_edges_distance_before_s
        )
    }

}
