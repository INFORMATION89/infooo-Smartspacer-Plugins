package dev.marco.smartspacer.ubereats

/** Pure notification-text/channel heuristics used only for the Uber app's mixed feed. */
internal object UberFoodNotificationClassifier {
    private val FOOD_CHANNEL = Regex("eats|order|deliver|food", RegexOption.IGNORE_CASE)
    private val FOOD_WORDS = Regex(
        "\\b(order|food|restaurant|courier|delivery|delivered|pedido|comida|restaurante|repartidor|entrega)",
        RegexOption.IGNORE_CASE
    )
    private val RIDE_WORDS = Regex("\\b(driver|ride|trip|conductor|viaje)", RegexOption.IGNORE_CASE)

    /**
     * Treat a food-oriented channel as sufficient evidence. Otherwise require food wording and
     * reject wording that also looks like an ordinary ride/trip.
     *
     * This is a candidate filter, not final order validation: OrderReader's notification score
     * still decides whether the notification is sufficiently like live order tracking.
     */
    fun looksLikeFoodOrder(channelId: String?, title: String, body: String?): Boolean {
        if (channelId?.let(FOOD_CHANNEL::containsMatchIn) == true) return true
        val words = listOfNotNull(title, body).joinToString(" ")
        return FOOD_WORDS.containsMatchIn(words) && !RIDE_WORDS.containsMatchIn(words)
    }
}

/** Scoring inputs extracted from Android notifications so the decision is easy to unit-test. */
internal object OrderNotificationScoring {
    fun score(
        isProgressStyle: Boolean,
        isPromoted: Boolean,
        hasProgress: Boolean,
        isOngoing: Boolean,
        hasOrderCategory: Boolean,
        hasEtaOrCriticalText: Boolean
    ): Int =
        (if (isProgressStyle) 3 else 0) +
            (if (isPromoted) 2 else 0) +
            (if (hasProgress) 2 else 0) +
            (if (isOngoing) 1 else 0) +
            (if (hasOrderCategory) 1 else 0) +
            (if (hasEtaOrCriticalText) 1 else 0)
}
