package io.github.jamesmyatt.randomatizer.dice

/** The face shown by one [die] after rolling. */
data class DieResult(val die: StandardDie, val value: Int) {
    init {
        require(value in 1..die.sides) { "$value is not a face of ${die.label}" }
    }
}

/** The results of rolling one or more dice together. */
data class Roll(val results: List<DieResult>) {
    init {
        require(results.isNotEmpty()) { "A roll needs at least one die" }
    }

    val total: Int = results.sumOf { it.value }
}
