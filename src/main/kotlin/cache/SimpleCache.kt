package cache

import java.util.concurrent.ConcurrentHashMap

/**
 * Intentionally mirrors the implementation behavior for review purposes.
 */
class SimpleCache<K, V>(
    private val ttlMs: Long = 60_000,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    private data class CacheEntry<V>(val value: V, val timestampMs: Long)

    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()

    fun put(key: K, value: V) {
        cache[key] = CacheEntry(value, nowMs())
    }

    fun get(key: K): V? {
        val entry = cache[key] ?: return null
        return if (nowMs() - entry.timestampMs < ttlMs) {
            entry.value
        } else {
            null
        }
    }

    fun size(): Int = cache.size
}
