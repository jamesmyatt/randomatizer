package io.github.jamesmyatt.randomatizer.history

import io.github.jamesmyatt.randomatizer.dice.DieResult
import io.github.jamesmyatt.randomatizer.dice.Roll
import io.github.jamesmyatt.randomatizer.dice.StandardDie
import io.github.jamesmyatt.randomatizer.settings.Mode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollHistoryTest {
    private fun entry(id: Long, vararg results: DieResult, mode: Mode = Mode.Advanced) =
        HistoryEntry(id, Roll(results.toList()), mode)

    @Test
    fun `newest entry comes first`() {
        val history = RollHistory() + entry(1, DieResult(StandardDie.D6, 1)) + entry(2, DieResult(StandardDie.D6, 2))
        assertEquals(listOf(2L, 1L), history.entries.map { it.id })
    }

    @Test
    fun `oldest entries drop off at capacity`() {
        val history = (1L..150L).fold(RollHistory()) { h, id -> h + entry(id, DieResult(StandardDie.D4, 1)) }
        assertEquals(RollHistory.DEFAULT_CAPACITY, history.entries.size)
        assertEquals(150L, history.entries.first().id)
        assertEquals(51L, history.entries.last().id)
    }

    @Test
    fun `clear empties the history`() {
        val history = RollHistory() + entry(1, DieResult(StandardDie.D6, 3))
        assertTrue(history.cleared().entries.isEmpty())
    }

    @Test
    fun `basic entries list values only`() {
        val e =
            entry(
                1,
                DieResult(StandardDie.D6, 4),
                DieResult(StandardDie.D6, 2),
                DieResult(StandardDie.D6, 5),
                mode = Mode.Basic,
            )
        assertEquals("4 · 2 · 5", e.describe())
    }

    @Test
    fun `advanced entries group by die type`() {
        val e = entry(
            1,
            DieResult(StandardDie.D20, 14),
            DieResult(StandardDie.D6, 3),
            DieResult(StandardDie.D8, 7),
            DieResult(StandardDie.D6, 5),
        )
        assertEquals("[2d6] 3 5 · [d8] 7 · [d20] 14", e.describe())
    }
}
