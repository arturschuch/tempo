# Hierarchy Exercise Notes

## Original Requirement

From the provided take-home file, the requested work was:

1. Understand the DFS-array hierarchy representation (`nodeIds` + `depths`).
2. Implement `Hierarchy.filter(nodeIdPredicate)`.
3. Add more tests beyond the provided single sample test.

The filtering rule from the prompt is:

- A node is kept **iff** the node passes the predicate **and** all its ancestors pass.

## What Was Implemented

### 1) `filter()` implementation

Implemented in:

- `src/main/kotlin/hierarchy/Hierarchy.kt`

Behavior:

- Iterates once through hierarchy nodes in DFS order.
- Tracks whether parent at each depth is kept (`keepAtDepth`).
- Keeps a node only when:
  - parent is kept (or node is root), and
  - `nodeIdPredicate(nodeId)` is true.
- Preserves original order and depth values for kept nodes.
- Returns a new `ArrayBasedHierarchy`.

Complexity:

- Time: `O(n)`
- Space: `O(n)` for output + `O(maxDepth)` tracking array (allocated as `O(n)` worst case).

### 2) Additional tests

Implemented in:

- `src/test/kotlin/hierarchy/HierarchyFilterTest.kt`

Covered scenarios:

- Empty hierarchy input.
- Predicate always true (no filtering).
- Predicate always false (empty result).
- Ancestor failure removes descendants.
- Multiple roots with selective root/child inclusion.
- Provided example from the assignment prompt.

## Summary

- The solution favors clarity and linear-time behavior.
- Tests are intentionally scenario-focused (`given/when/then`) to make rule verification easy to follow.
