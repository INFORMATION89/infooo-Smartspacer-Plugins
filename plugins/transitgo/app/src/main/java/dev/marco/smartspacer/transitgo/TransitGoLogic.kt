package dev.marco.smartspacer.transitgo

import android.app.Notification

/**
 * Pure decisions used by the Transit notification adapter.
 *
 * Keep notification parsing rules here so they can be regression-tested without a running
 * Transit installation or real travel notifications.
 */
internal object TransitGoLogic {
    private val bikesPattern = Regex("""^(\d+)\s+(bikes?|bicis?)$""", RegexOption.IGNORE_CASE)

    const val FEW_BIKES = 3
    const val CAUTION = "\u26A0\uFE0E"

    fun isTransitNotification(packageName: String, channelId: String?): Boolean =
        packageName == TransitNotificationContract.PACKAGE_NAME &&
            channelId == TransitNotificationContract.GO_CHANNEL_ID

    fun isLiveTrip(flags: Int): Boolean =
        flags and Notification.FLAG_GROUP_SUMMARY == 0 &&
            flags and Notification.FLAG_FOREGROUND_SERVICE != 0

    fun withBikesLeft(title: String): String {
        val match = bikesPattern.matchEntire(title) ?: return title
        val count = match.groupValues[1].toIntOrNull() ?: return title
        val spanish = match.groupValues[2].startsWith("bici", ignoreCase = true)
        val text = when {
            !spanish -> "$title left"
            count == 1 -> "$title restante"
            else -> "$title restantes"
        }
        return if (count < FEW_BIKES) "$CAUTION $text" else text
    }

    fun shouldClearTrip(currentNotificationKey: String?, removedNotificationKey: String): Boolean =
        currentNotificationKey != null && currentNotificationKey == removedNotificationKey

    data class VisibleTripContent(
        val title: String,
        val text: String?,
        val previous: String?,
        val routeName: String?,
        val arrival: String?,
        val timeLeft: String?,
        val notificationKey: String?,
        val hasChip: Boolean,
        val hasStepIcon: Boolean
    )

    fun hasSameVisibleContent(
        current: VisibleTripContent,
        updated: VisibleTripContent
    ): Boolean = current == updated
}
