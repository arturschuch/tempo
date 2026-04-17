package cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExpiringSimpleCacheTest {

    @Test
    fun returnsNullForExpiredEntry() {
        // given
        var now = 1_000L
        val cache = ExpiringSimpleCache<String, String>(ttlMs = 1, nowMs = { now })
        cache.put("k1", "v1")

        // when
        now = 1_001L
        val value = cache.get("k1")

        // then
        assertNull(value)
    }

    @Test
    fun returnsValueBeforeExpiration() {
        // given
        var now = 1_000L
        val cache = ExpiringSimpleCache<String, String>(ttlMs = 100, nowMs = { now })
        cache.put("k", "v")

        // when
        now = 1_050L
        val value = cache.get("k")

        // then
        assertEquals("v", value)
    }

    @Test
    fun evictsExpiredEntryAfterRead() {
        // given
        var now = 1_000L
        val cache = ExpiringSimpleCache<String, String>(ttlMs = 100, nowMs = { now })
        cache.put("k", "v")

        // when
        now = 1_100L
        val value = cache.get("k")

        // then
        assertNull(value)
        assertEquals(0, cache.size())
    }

    @Test
    fun sizeReflectsOnlyLiveEntries() {
        // given
        var now = 10_000L
        val cache = ExpiringSimpleCache<String, String>(ttlMs = 50, nowMs = { now })
        cache.put("a", "1")
        now = 10_060L
        cache.put("b", "2")

        // when
        val size = cache.size()

        // then
        assertEquals(1, size)
        assertEquals("2", cache.get("b"))
        assertNull(cache.get("a"))
    }

    @Test
    fun cleanupRemovesOnlyExpiredEntries() {
        // given
        var now = 5_000L
        val cache = ExpiringSimpleCache<String, String>(ttlMs = 100, nowMs = { now })
        cache.put("old", "x")
        now = 5_060L
        cache.put("fresh", "y")

        // when
        now = 5_120L
        cache.cleanupExpired()

        // then
        assertNull(cache.get("old"))
        assertEquals("y", cache.get("fresh"))
    }

    @Test
    fun evictsExpiredEntriesAfterBulkReads() {
        // given
        var now = 1_000L
        val cache = ExpiringSimpleCache<String, String>(ttlMs = 1, nowMs = { now })
        repeat(1_000) { index ->
            cache.put("k$index", "v$index")
        }

        // when
        now = 1_001L
        repeat(1_000) { index ->
            assertNull(cache.get("k$index"))
        }

        // then
        assertEquals(0, cache.size())
    }

    @Test
    fun doesNotAccumulateStaleKeysAcrossManyExpirationCycles() {
        // given
        var now = 10_000L
        val cache = ExpiringSimpleCache<String, String>(ttlMs = 1, nowMs = { now })

        // when
        repeat(50) { cycle ->
            repeat(100) { index ->
                val key = "c${cycle}_$index"
                cache.put(key, key)
            }
            now += 1
            repeat(100) { index ->
                assertNull(cache.get("c${cycle}_$index"))
            }
            now += 1
        }

        // then
        assertEquals(0, cache.size())
    }
}
