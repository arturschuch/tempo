# SimpleCache Review

## Problem Summary

The submitted cache uses a process-local `ConcurrentHashMap` with TTL checks on read.
For a highly concurrent production workload (thousands of reads/s, hundreds of writes/s, many threads),
the implementation has correctness, operability, and scalability risks.

## Findings (Ordered by Severity)

1. **Expired entries are never removed**
   - `get()` returns `null` for stale entries but does not evict them.
   - Impact: unbounded memory growth under sustained write traffic, eventually increasing GC pressure and causing latency spikes or OOM risk.

2. **Process-local in-memory cache does not scale across instances**
   - Each app instance maintains its own independent map.
   - Impact: low cross-instance hit rate, duplicated memory usage, inconsistent behavior between nodes, and no shared cache state.

3. **No durability or resilience across restarts**
   - Data is lost on process restart/deploy/crash.
   - Impact: cache cold starts, backend thundering herd, and unstable latency after deployments or incidents.

4. **No max-size/eviction policy**
   - Cache growth is only limited by process memory.
   - Impact: memory pressure and runtime instability under high cardinality workloads.

5. **`size()` reports misleading value**
   - `size()` includes expired entries because stale items remain in the map.
   - Impact: misleading telemetry and bad capacity decisions.

6. **Hard-coded TTL in the original snippet**
   - Fixed TTL reduces adaptability across workloads.
   - Impact: cannot tune freshness-vs-hit-rate tradeoff without code changes.

7. **Wall-clock dependency for expiration checks**
   - Expiration is based on `System.currentTimeMillis()`.
   - Impact: clock skew/jumps can cause early or late expiration behavior.

8. **Missing operational controls and observability**
   - No hit/miss/eviction counters, no invalidation APIs, no backpressure strategy.
   - Impact: difficult incident diagnosis and hard-to-tune behavior in production.

## Architecture Notes (In-Memory vs Redis)

For single-instance applications with low/medium load, an in-memory cache can be acceptable.
For horizontally scaled services and the workload described in the prompt, a distributed cache is typically safer.

### Why Redis (or equivalent) is often preferred

- Shared cache across all app instances (better hit ratio).
- Independent memory lifecycle from app process restarts.
- Built-in eviction policies and TTL management.
- Rich observability and operational tooling.
- Easier centralized invalidation patterns.

### Redis Tradeoffs in This Context

- A remote cache adds network hop latency and dependency management complexity.
- It requires timeout/retry/circuit-breaker behavior to avoid cascading failures.
- It needs capacity planning plus high-availability setup/operations.
- A hybrid model can be preferable in high-throughput systems (L1 local cache + L2 Redis).

## Test-Backed Demonstration

- Baseline behavior: `src/main/kotlin/cache/SimpleCache.kt`
- Problem-focused tests: `src/test/kotlin/cache/SimpleCacheBehaviorTest.kt`

These tests intentionally prove the problematic behavior, including growth across short-TTL expiration cycles.

## Optional Improved Implementation

- Improved cache: `src/main/kotlin/cache/ExpiringSimpleCache.kt`
- Comparison tests: `src/test/kotlin/cache/ExpiringSimpleCacheTest.kt`

These tests mirror churn scenarios and show stale entries are evicted (no accumulation after expiration cycles).

## Recommended Next Steps Before Production

1. Add bounded size + eviction policy and a deterministic cleanup strategy.
2. Externalize TTL and cache settings to configuration.
3. Add cache metrics: hit/miss rate, evictions, entry count, and memory estimates.
4. Define invalidation strategy and failure behavior.
5. Evaluate distributed cache adoption (Redis) for multi-instance deployments.
