package cache

import java.util.concurrent.ConcurrentHashMap

class ExpiringSimpleCache<K, V>(
    private val ttlMs: Long,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    init {
        require(ttlMs > 0) { "ttlMs must be > 0" }
    }

    private data class CacheEntry<V>(val value: V, val timestampMs: Long)

    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()

    fun put(key: K, value: V) {
        cache[key] = CacheEntry(value, nowMs())
    }

    fun get(key: K): V? {
        val entry = cache[key] ?: return null
        return if (isExpired(entry)) {
            cache.remove(key, entry)
            null
        } else {
            entry.value
        }
    }

    fun size(): Int {
        cleanupExpired()
        return cache.size
    }

    fun cleanupExpired() {
        val now = nowMs()
        val iterator = cache.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value.timestampMs >= ttlMs) {
                iterator.remove()
            }
        }
    }

    private fun isExpired(entry: CacheEntry<V>): Boolean {
        return nowMs() - entry.timestampMs >= ttlMs
    }
}
