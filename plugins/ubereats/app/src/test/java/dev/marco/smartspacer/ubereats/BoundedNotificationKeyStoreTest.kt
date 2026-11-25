package dev.marco.smartspacer.ubereats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoundedNotificationKeyStoreTest {

    @Test
    fun remembersAndRefreshesKnownUberFoodKeys() {
        val store = BoundedNotificationKeyStore(maxEntries = 4, maxAgeMillis = 100)
        store.remember("food-order", nowMillis = 1_000)

        assertTrue(store.containsAndRefresh("food-order", nowMillis = 1_050))
        assertTrue(store.containsAndRefresh("food-order", nowMillis = 1_140))
        assertEquals(1, store.size())
    }

    @Test
    fun expiresKeysAfterTheConfiguredAge() {
        val store = BoundedNotificationKeyStore(maxEntries = 4, maxAgeMillis = 100)
        store.remember("food-order", nowMillis = 1_000)

        assertEquals(1, store.prune(nowMillis = 1_101))
        assertFalse(store.containsAndRefresh("food-order", nowMillis = 1_102))
        assertEquals(0, store.size())
    }

    @Test
    fun keepsOnlyTheMostRecentlyRememberedKeys() {
        val store = BoundedNotificationKeyStore(maxEntries = 2, maxAgeMillis = 1_000)
        store.remember("first", nowMillis = 1_000)
        store.remember("second", nowMillis = 1_001)
        store.remember("third", nowMillis = 1_002)

        assertFalse(store.containsAndRefresh("first", nowMillis = 1_003))
        assertTrue(store.containsAndRefresh("second", nowMillis = 1_003))
        assertTrue(store.containsAndRefresh("third", nowMillis = 1_003))
        assertEquals(2, store.size())
    }

    @Test
    fun removingAndClearingKeysIsSafe() {
        val store = BoundedNotificationKeyStore(maxEntries = 2, maxAgeMillis = 1_000)
        store.remember("first", nowMillis = 1_000)
        store.remember("second", nowMillis = 1_001)
        store.remove("first")
        assertFalse(store.containsAndRefresh("first", nowMillis = 1_002))

        store.clear()
        assertEquals(0, store.size())
    }
}
