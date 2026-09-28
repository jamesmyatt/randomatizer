package io.github.jamesmyatt.randomatizer.history

import io.github.jamesmyatt.randomatizer.dice.StandardDie
import io.github.jamesmyatt.randomatizer.settings.Mode

/**
 * Compact description of the dice in an entry, without the total.
 *
 * Basic: "4 · 2 · 5". Advanced: "2d6 3 5 · d8 7 · d20 14".
 */
fun HistoryEntry.describe(): String = when (mode) {
    Mode.Basic -> roll.results.joinToString(" · ") { it.value.toString() }

    Mode.Advanced -> roll.results.groupBy { it.die }
        .toSortedMap(compareBy(StandardDie::ordinal))
        .map { (die, results) ->
            val prefix = if (results.size == 1) die.label else "${results.size}${die.label}"
            "$prefix ${results.joinToString(" ") { it.value.toString() }}"
        }
        .joinToString(" · ")
}
