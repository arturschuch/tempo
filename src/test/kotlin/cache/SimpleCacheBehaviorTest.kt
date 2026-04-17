package cache

import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SimpleCacheBehaviorTest {

    @Test
    fun returnsNullForExpiredEntry() {
        // given
        var now = 1_000L
        val cache = SimpleCache<String, String>(ttlMs = 1, nowMs = { now })
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
        val cache = SimpleCache<String, String>(ttlMs = 100, nowMs = { now })
        cache.put("k", "v")

        // when
        now = 1_050L
        val value = cache.get("k")

        // then
        assertEquals("v", value)
    }

    @Test
    fun doesNotEvictExpiredEntryAfterRead_wrongCurrentBehavior() {
        // given
        var now = 1_000L
        val cache = SimpleCache<String, String>(ttlMs = 1, nowMs = { now })
        cache.put("k1", "v1")

        // when
        now = 1_001L
        assertNull(cache.get("k1"))

        // then
        assertEquals(1, cache.size())
    }

    @Test
    @Disabled("Desired behavior spec: baseline SimpleCache does not evict expired entries.")
    fun evictsExpiredEntryAfterRead_desiredBehavior() {
        // given
        var now = 1_000L
        val cache = SimpleCache<String, String>(ttlMs = 1, nowMs = { now })
        cache.put("k1", "v1")

        // when
        now = 1_001L
        assertNull(cache.get("k1"))

        // then
        assertEquals(0, cache.size())
    }

    @Test
    fun accumulatesStaleKeysAcrossManyExpirationCycles_wrongCurrentBehavior() {
        // given
        var now = 10_000L
        val cache = SimpleCache<String, String>(ttlMs = 1, nowMs = { now })

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
        assertEquals(5_000, cache.size())
    }

    @Test
    @Disabled("Desired behavior spec: baseline SimpleCache keeps stale keys and grows across cycles.")
    fun doesNotAccumulateStaleKeysAcrossManyExpirationCycles_desiredBehavior() {
        // given
        var now = 10_000L
        val cache = SimpleCache<String, String>(ttlMs = 1, nowMs = { now })

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
