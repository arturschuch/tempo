package hierarchy

import kotlin.test.Test
import kotlin.test.assertEquals

class HierarchyFilterTest {

    @Test
    fun returnsEmptyForEmptyHierarchy() {
        // given
        val empty = hierarchyOf(intArrayOf(), intArrayOf())

        // when
        val filtered = empty.filter { true }

        // then
        assertHierarchyEquals("[]", filtered)
    }

    @Test
    fun keepsEverythingWhenPredicateAlwaysTrue() {
        // given
        val hierarchy = hierarchyOf(
            intArrayOf(1, 2, 3, 4),
            intArrayOf(0, 1, 1, 2),
        )

        // when
        val filtered = hierarchy.filter { true }

        // then
        assertHierarchyEquals(hierarchy.formatString(), filtered)
    }

    @Test
    fun returnsEmptyWhenPredicateAlwaysFalse() {
        // given
        val hierarchy = hierarchyOf(
            intArrayOf(1, 2, 3),
            intArrayOf(0, 1, 0),
        )

        // when
        val filtered = hierarchy.filter { false }

        // then
        assertHierarchyEquals("[]", filtered)
    }

    @Test
    fun removesDescendantsWhenAncestorFailsPredicate() {
        // given
        val hierarchy = hierarchyOf(
            intArrayOf(1, 2, 3, 4, 5, 6),
            intArrayOf(0, 1, 2, 1, 0, 1),
        )

        // when
        val filtered = hierarchy.filter { id -> id != 2 }

        // then
        assertHierarchyEquals("[1:0, 4:1, 5:0, 6:1]", filtered)
    }

    @Test
    fun keepsOnlyPassingRootsAndTheirPassingDescendants() {
        // given
        val hierarchy = hierarchyOf(
            intArrayOf(1, 2, 3, 4, 5),
            intArrayOf(0, 1, 0, 1, 1),
        )

        // when
        val filtered = hierarchy.filter { id -> id >= 3 }

        // then
        assertHierarchyEquals("[3:0, 4:1, 5:1]", filtered)
    }

    @Test
    fun matchesExpectedResultFromPrompt() {
        // given
        val unfiltered = hierarchyOf(
            intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
            intArrayOf(0, 1, 2, 3, 1, 0, 1, 0, 1, 1, 2),
        )

        // when
        val filtered = unfiltered.filter { nodeId -> nodeId % 3 != 0 }

        // then
        assertHierarchyEquals("[1:0, 2:1, 5:1, 8:0, 10:1, 11:2]", filtered)
    }

    private fun hierarchyOf(nodeIds: IntArray, depths: IntArray): Hierarchy =
        ArrayBasedHierarchy(nodeIds, depths)

    private fun assertHierarchyEquals(expectedFormat: String, actual: Hierarchy) {
        assertEquals(expectedFormat, actual.formatString())
    }
}
