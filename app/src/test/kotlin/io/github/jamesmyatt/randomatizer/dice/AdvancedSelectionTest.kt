package io.github.jamesmyatt.randomatizer.dice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdvancedSelectionTest {
    private val selection = AdvancedSelection(mapOf(StandardDie.D20 to 1, StandardDie.D6 to 2, StandardDie.D8 to 1))

    @Test
    fun `summary lists dice in standard order`() {
        assertEquals("2d6 + d8 + d20", selection.summary())
    }

    @Test
    fun `dice are grouped in standard order`() {
        assertEquals(
            listOf(StandardDie.D6, StandardDie.D6, StandardDie.D8, StandardDie.D20),
            selection.dice(),
        )
        assertEquals(4, selection.total)
    }

    @Test
    fun `cannot remove the last die`() {
        val single = AdvancedSelection(mapOf(StandardDie.D12 to 1))
        assertFalse(single.canDecrement(StandardDie.D12))
        assertFalse(single.canDecrement(StandardDie.D4))
        assertFailsWith<IllegalArgumentException> { single.withCount(StandardDie.D12, 0) }
    }

    @Test
    fun `cannot exceed the per-die limit`() {
        val full = AdvancedSelection(mapOf(StandardDie.D6 to DiceLimits.ADVANCED_MAX_PER_DIE))
        assertFalse(full.canIncrement(StandardDie.D6))
        assertTrue(full.canIncrement(StandardDie.D8))
        assertFailsWith<IllegalArgumentException> {
            full.withCount(StandardDie.D6, DiceLimits.ADVANCED_MAX_PER_DIE + 1)
        }
    }

    @Test
    fun `sanitized clamps counts and falls back to the default when empty`() {
        assertEquals(
            AdvancedSelection(mapOf(StandardDie.D4 to DiceLimits.ADVANCED_MAX_PER_DIE)),
            AdvancedSelection.sanitized(mapOf(StandardDie.D4 to 99, StandardDie.D6 to -3)),
        )
        assertEquals(AdvancedSelection.Default, AdvancedSelection.sanitized(emptyMap()))
    }
}
