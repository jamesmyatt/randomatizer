package io.github.jamesmyatt.randomatizer.dice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DiceRollerTest {
    /** Records every bound requested and returns a scripted value. */
    private class RecordingSource(private val answer: (Int) -> Int) : RandomSource {
        val bounds = mutableListOf<Int>()

        override fun nextInt(bound: Int): Int {
            bounds += bound
            return answer(bound)
        }
    }

    @Test
    fun `every die stays within 1 to sides`() {
        val roller = DiceRoller(SeededRandomSource(42))
        StandardDie.entries.forEach { die ->
            repeat(10_000) {
                val value = roller.roll(die).value
                assertTrue(value in 1..die.sides, "${die.label} rolled $value")
            }
        }
    }

    @Test
    fun `asks the source for exactly one value per die using the side count as the bound`() {
        val source = RecordingSource { 0 }
        DiceRoller(source).roll(StandardDie.entries)
        assertEquals(StandardDie.entries.map { it.sides }, source.bounds)
    }

    @Test
    fun `maps source extremes to the lowest and highest faces`() {
        StandardDie.entries.forEach { die ->
            assertEquals(1, DiceRoller(RecordingSource { 0 }).roll(die).value)
            assertEquals(die.sides, DiceRoller(RecordingSource { it - 1 }).roll(die).value)
        }
    }

    @Test
    fun `every face is reachable`() {
        StandardDie.entries.forEach { die ->
            val faces = (0 until die.sides).map { n -> DiceRoller(RecordingSource { n }).roll(die).value }
            assertEquals((1..die.sides).toList(), faces)
        }
    }

    @Test
    fun `same seed gives the same rolls`() {
        val dice = List(20) { StandardDie.D20 } + List(20) { StandardDie.D6 }
        assertEquals(
            DiceRoller(SeededRandomSource(7)).roll(dice),
            DiceRoller(SeededRandomSource(7)).roll(dice),
        )
    }

    @Test
    fun `mixed roll keeps order and totals the results`() {
        val dice = listOf(StandardDie.D4, StandardDie.D100, StandardDie.D6, StandardDie.D6)
        val roll = DiceRoller(SeededRandomSource(1)).roll(dice)
        assertEquals(dice, roll.results.map { it.die })
        assertEquals(roll.results.sumOf { it.value }, roll.total)
    }

    @Test
    fun `empty roll is rejected`() {
        assertFailsWith<IllegalArgumentException> { DiceRoller(SeededRandomSource(1)).roll(emptyList()) }
    }

    @Test
    fun `impossible die result is rejected`() {
        assertFailsWith<IllegalArgumentException> { DieResult(StandardDie.D6, 7) }
        assertFailsWith<IllegalArgumentException> { DieResult(StandardDie.D6, 0) }
    }

    @Test
    fun `secure source stays within bounds`() {
        val roller = DiceRoller(SecureRandomSource())
        StandardDie.entries.forEach { die ->
            repeat(1_000) { assertTrue(roller.roll(die).value in 1..die.sides) }
        }
    }
}
