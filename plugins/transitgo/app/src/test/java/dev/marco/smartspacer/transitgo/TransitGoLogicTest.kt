package dev.marco.smartspacer.transitgo

import android.app.Notification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransitGoLogicTest {

    @Test
    fun notificationFilterRequiresTransitPackageAndGoChannel() {
        assertTrue(
            TransitGoLogic.isTransitNotification(
                TransitNotificationContract.PACKAGE_NAME,
                TransitNotificationContract.GO_CHANNEL_ID
            )
        )
        assertFalse(
            TransitGoLogic.isTransitNotification(
                "another.package",
                TransitNotificationContract.GO_CHANNEL_ID
            )
        )
        assertFalse(
            TransitGoLogic.isTransitNotification(
                TransitNotificationContract.PACKAGE_NAME,
                "another_channel"
            )
        )
        assertFalse(
            TransitGoLogic.isTransitNotification(
                TransitNotificationContract.PACKAGE_NAME,
                null
            )
        )
    }

    @Test
    fun foregroundServiceWithoutGroupSummaryIsLiveTrip() {
        assertTrue(TransitGoLogic.isLiveTrip(Notification.FLAG_FOREGROUND_SERVICE))
    }

    @Test
    fun groupSummaryIsNotLiveTrip() {
        assertFalse(
            TransitGoLogic.isLiveTrip(
                Notification.FLAG_FOREGROUND_SERVICE or Notification.FLAG_GROUP_SUMMARY
            )
        )
    }

    @Test
    fun nonForegroundNotificationIsNotLiveTrip() {
        assertFalse(TransitGoLogic.isLiveTrip(0))
        assertFalse(TransitGoLogic.isLiveTrip(Notification.FLAG_AUTO_CANCEL))
    }

    @Test
    fun bikeCountsUseEnglishWordingAndCautionThreshold() {
        assertEquals("⚠︎ 0 bikes left", TransitGoLogic.withBikesLeft("0 bikes"))
        assertEquals("⚠︎ 2 bikes left", TransitGoLogic.withBikesLeft("2 bikes"))
        assertEquals("3 bikes left", TransitGoLogic.withBikesLeft("3 bikes"))
        assertEquals("10 bikes left", TransitGoLogic.withBikesLeft("10 bikes"))
    }

    @Test
    fun bikeCountsUseSpanishSingularAndPlural() {
        assertEquals("⚠︎ 0 bicis restantes", TransitGoLogic.withBikesLeft("0 bicis"))
        assertEquals("⚠︎ 1 bici restante", TransitGoLogic.withBikesLeft("1 bici"))
        assertEquals("3 bicis restantes", TransitGoLogic.withBikesLeft("3 bicis"))
    }

    @Test
    fun bikeCountParsingIsCaseInsensitive() {
        assertEquals("4 BIKES left", TransitGoLogic.withBikesLeft("4 BIKES"))
        assertEquals("4 bikes left", TransitGoLogic.withBikesLeft("4 bikes"))
    }

    @Test
    fun unknownOrMalformedBikeTextIsPreserved() {
        listOf("bikes", "4 bicycles", "4 bicicletas", "4", "unknown", "").forEach { input ->
            assertEquals(input, TransitGoLogic.withBikesLeft(input))
        }
    }

    @Test
    fun onlyRemovalOfCurrentNotificationClearsTrip() {
        assertTrue(TransitGoLogic.shouldClearTrip("active-key", "active-key"))
        assertFalse(TransitGoLogic.shouldClearTrip("active-key", "another-key"))
        assertFalse(TransitGoLogic.shouldClearTrip(null, "active-key"))
    }

    @Test
    fun visibleTripContentComparisonDetectsRealChanges() {
        val original = content()
        assertTrue(TransitGoLogic.hasSameVisibleContent(original, content()))
        assertFalse(
            TransitGoLogic.hasSameVisibleContent(
                original,
                content(text = "Next stop changed")
            )
        )
        assertFalse(
            TransitGoLogic.hasSameVisibleContent(
                original,
                content(hasChip = false)
            )
        )
    }

    private fun content(
        text: String? = "in 12 minutes",
        hasChip: Boolean = true
    ) = TransitGoLogic.VisibleTripContent(
        title = "Leave in 5 minutes",
        text = text,
        previous = null,
        routeName = "57-A",
        arrival = "6:12 PM",
        timeLeft = "20 min",
        notificationKey = "active-key",
        hasChip = hasChip,
        hasStepIcon = true
    )
}
