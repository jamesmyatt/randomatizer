package io.github.jamesmyatt.randomatizer.history

import io.github.jamesmyatt.randomatizer.dice.Roll
import io.github.jamesmyatt.randomatizer.settings.Mode

/** One roll in the session history. [id] is unique within the session. */
data class HistoryEntry(val id: Long, val roll: Roll, val mode: Mode)

/**
 * In-memory roll history for the current session, newest first.
 *
 * Never persist this: history must not be written to storage.
 */
data class RollHistory(val entries: List<HistoryEntry> = emptyList(), val capacity: Int = DEFAULT_CAPACITY) {
    init {
        require(capacity > 0)
    }

    operator fun plus(entry: HistoryEntry): RollHistory = copy(entries = (listOf(entry) + entries).take(capacity))

    fun cleared(): RollHistory = copy(entries = emptyList())

    companion object {
        const val DEFAULT_CAPACITY = 100
    }
}
