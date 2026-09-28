package io.github.jamesmyatt.randomatizer.dice

import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertTrue

/** Chi-squared goodness-of-fit checks that each die is uniform. Seeded, so deterministic. */
class DistributionTest {
    private fun chiSquared(counts: IntArray, expected: Double): Double =
        counts.sumOf { (it - expected).pow(2) / expected }

    /** Wilson–Hilferty approximation of the chi-squared critical value at p = 0.001. */
    private fun criticalValue(degreesOfFreedom: Int): Double {
        val k = degreesOfFreedom.toDouble()
        val z = 3.090
        return k * (1 - 2 / (9 * k) + z * sqrt(2 / (9 * k))).pow(3)
    }

    @Test
    fun `every die is uniform`() {
        val roller = DiceRoller(SeededRandomSource(2026))
        StandardDie.entries.forEach { die ->
            val rollsPerFace = 1_000
            val counts = IntArray(die.sides)
            repeat(die.sides * rollsPerFace) { counts[roller.roll(die).value - 1]++ }
            val statistic = chiSquared(counts, rollsPerFace.toDouble())
            val critical = criticalValue(die.sides - 1)
            assertTrue(statistic < critical, "${die.label}: chi² = $statistic exceeds $critical")
        }
    }
}
