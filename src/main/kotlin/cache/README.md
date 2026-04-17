# SimpleCache Review

## Problem Summary

The provided `SimpleCache` implementation uses a `ConcurrentHashMap` with a fixed TTL and timestamp-based expiration checks.

## Findings (Ordered by Severity)

1. **Expired entries are never removed**
   - `get()` returns `null` for stale entries but does not evict them.
   - Impact: unbounded memory growth under sustained write traffic, eventually increasing GC pressure and causing latency spikes or OOM risk.

2. **`size()` reports misleading value**
   - `size()` includes expired entries because stale items remain in the map.
   - Impact: operational metrics and autoscaling decisions can be wrong; callers may trust an inflated cache size.

3. **Hard-coded TTL in the original snippet**
   - Fixed TTL reduces adaptability across workloads.
   - Impact: cannot tune freshness-vs-hit-rate tradeoff without code changes.

4. **No explicit cleanup strategy**
   - No background cleanup or bounded-size policy.
   - Impact: stale key accumulation and unpredictable memory usage under heavy load.

## Test-Backed Demonstration

- Baseline behavior: `src/main/kotlin/cache/SimpleCache.kt`
- Problem-focused tests: `src/test/kotlin/cache/SimpleCacheBehaviorTest.kt`

These tests intentionally prove the problematic behavior, including growth across short-TTL expiration cycles.

## Optional Improved Implementation

- Improved cache: `src/main/kotlin/cache/ExpiringSimpleCache.kt`
- Comparison tests: `src/test/kotlin/cache/ExpiringSimpleCacheTest.kt`

These tests mirror the churn scenarios and show stale entries are evicted (no accumulation after expiration cycles).
