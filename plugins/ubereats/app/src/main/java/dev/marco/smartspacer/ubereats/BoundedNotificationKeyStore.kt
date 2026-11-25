package dev.marco.smartspacer.ubereats

/**
 * Remembers notification keys that were classified as food orders so later ETA-only updates
 * from the Uber app can still be recognized. Entries expire and the store is capped to keep
 * stale notifications from growing process memory indefinitely.
 */
internal class BoundedNotificationKeyStore(
    private val maxEntries: Int,
    private val maxAgeMillis: Long
) {
    private val seenAt = LinkedHashMap<String, Long>()

    init {
        require(maxEntries > 0) { "maxEntries must be positive" }
        require(maxAgeMillis > 0) { "maxAgeMillis must be positive" }
    }

    @Synchronized
    fun containsAndRefresh(key: String, nowMillis: Long): Boolean {
        prune(nowMillis)
        if (seenAt.remove(key) == null) return false
        seenAt[key] = nowMillis
        return true
    }

    @Synchronized
    fun remember(key: String, nowMillis: Long) {
        prune(nowMillis)
        seenAt.remove(key)
        seenAt[key] = nowMillis
        trimToLimit()
    }

    @Synchronized
    fun remove(key: String) {
        seenAt.remove(key)
    }

    @Synchronized
    fun clear() {
        seenAt.clear()
    }

    @Synchronized
    fun prune(nowMillis: Long): Int {
        val cutoff = nowMillis - maxAgeMillis
        val iterator = seenAt.entries.iterator()
        var removed = 0
        while (iterator.hasNext()) {
            if (iterator.next().value < cutoff) {
                iterator.remove()
                removed++
            }
        }
        trimToLimit()
        return removed
    }

    @Synchronized
    internal fun size(): Int = seenAt.size

    private fun trimToLimit() {
        while (seenAt.size > maxEntries) {
            val oldestKey = seenAt.entries.firstOrNull()?.key ?: break
            seenAt.remove(oldestKey)
        }
    }
}
