package io.github.jamesmyatt.randomatizer.dice

/** Limits on how many dice can be selected. */
object DiceLimits {
    const val BASIC_MIN = 1
    const val BASIC_MAX = 10
    const val ADVANCED_MAX_PER_DIE = 10
}

/** Number of each [StandardDie] to roll in Advanced mode. Always holds at least one die. */
data class AdvancedSelection(val counts: Map<StandardDie, Int>) {
    init {
        require(counts.values.all { it in 0..DiceLimits.ADVANCED_MAX_PER_DIE }) { "Count out of range: $counts" }
        require(total > 0) { "Selection must contain at least one die" }
    }

    val total: Int get() = counts.values.sum()

    fun count(die: StandardDie): Int = counts[die] ?: 0

    fun canIncrement(die: StandardDie): Boolean = count(die) < DiceLimits.ADVANCED_MAX_PER_DIE

    fun canDecrement(die: StandardDie): Boolean = count(die) > 0 && total > 1

    fun withCount(die: StandardDie, count: Int): AdvancedSelection = copy(counts = counts + (die to count))

    /** The dice to roll, grouped by type in [StandardDie] order. */
    fun dice(): List<StandardDie> = StandardDie.entries.flatMap { die -> List(count(die)) { die } }

    /** Short description such as "2d6 + d8 + d20". */
    fun summary(): String = StandardDie.entries
        .filter { count(it) > 0 }
        .joinToString(" + ") { die -> count(die).let { if (it == 1) die.label else "$it${die.label}" } }

    companion object {
        val Default = AdvancedSelection(mapOf(StandardDie.D6 to 2))

        /** Clamps [counts] into range, falling back to [Default] if nothing is selected. */
        fun sanitized(counts: Map<StandardDie, Int>): AdvancedSelection {
            val clamped = StandardDie.entries.associateWith { (counts[it] ?: 0).coerceIn(0, DiceLimits.ADVANCED_MAX_PER_DIE) }
                .filterValues { it > 0 }
            return if (clamped.isEmpty()) Default else AdvancedSelection(clamped)
        }
    }
}
