package dev.marco.smartspacer.ubereats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UberFoodNotificationClassifierTest {

    @Test
    fun foodOrientedChannelIsSufficient() {
        assertTrue(
            UberFoodNotificationClassifier.looksLikeFoodOrder(
                channelId = "active_order_status",
                title = "Arriving soon",
                body = null
            )
        )
    }

    @Test
    fun englishFoodWordingIsRecognized() {
        assertTrue(
            UberFoodNotificationClassifier.looksLikeFoodOrder(
                channelId = "miscellaneous",
                title = "Your order is being delivered",
                body = "The courier is on the way"
            )
        )
    }

    @Test
    fun spanishFoodWordingIsRecognized() {
        assertTrue(
            UberFoodNotificationClassifier.looksLikeFoodOrder(
                channelId = null,
                title = "Tu pedido está en camino",
                body = "El repartidor llegará pronto"
            )
        )
    }

    @Test
    fun ordinaryRideWordingIsRejectedWhenChannelIsNotFoodSpecific() {
        assertFalse(
            UberFoodNotificationClassifier.looksLikeFoodOrder(
                channelId = "trip_updates",
                title = "Your driver is arriving",
                body = "Your ride is on the way"
            )
        )
    }

    @Test
    fun mixedFoodAndRideWordingIsRejectedFromUberAppFeed() {
        assertFalse(
            UberFoodNotificationClassifier.looksLikeFoodOrder(
                channelId = "miscellaneous",
                title = "Your food order is ready",
                body = "Your driver is arriving for your ride"
            )
        )
    }

    @Test
    fun unrelatedTextWithoutFoodSignalsIsRejected() {
        assertFalse(
            UberFoodNotificationClassifier.looksLikeFoodOrder(
                channelId = null,
                title = "Your account is ready",
                body = "Thanks for using the app"
            )
        )
    }
}

class OrderNotificationScoringTest {

    @Test
    fun progressStyleIsStrongEnoughToPassTheCurrentThreshold() {
        val score = OrderNotificationScoring.score(
            isProgressStyle = true,
            isPromoted = false,
            hasProgress = false,
            isOngoing = false,
            hasOrderCategory = false,
            hasEtaOrCriticalText = false
        )
        assertEquals(3, score)
        assertTrue(score >= 2)
    }

    @Test
    fun independentSignalsAccumulateTheirCurrentWeights() {
        val score = OrderNotificationScoring.score(
            isProgressStyle = false,
            isPromoted = true,
            hasProgress = true,
            isOngoing = true,
            hasOrderCategory = true,
            hasEtaOrCriticalText = true
        )
        assertEquals(7, score)
    }

    @Test
    fun aNotificationWithoutLiveTrackingSignalsScoresZero() {
        val score = OrderNotificationScoring.score(
            isProgressStyle = false,
            isPromoted = false,
            hasProgress = false,
            isOngoing = false,
            hasOrderCategory = false,
            hasEtaOrCriticalText = false
        )
        assertEquals(0, score)
        assertFalse(score >= 2)
    }
}
