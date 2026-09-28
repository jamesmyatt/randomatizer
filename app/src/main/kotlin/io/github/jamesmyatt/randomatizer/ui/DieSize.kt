package io.github.jamesmyatt.randomatizer.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor

/** Gap between dice, horizontally and vertically. */
internal val DIE_SPACING = 16.dp

/** Smallest die: about 5 across a typical phone (~412 dp wide). More dice wrap onto further rows. */
internal val MIN_DIE_SIZE = 56.dp

/** Largest die: about 2 across a typical phone. */
internal val MAX_DIE_SIZE = 168.dp

/**
 * Size of each die so that [count] dice fill [availableWidth] in one row, clamped to
 * [MIN_DIE_SIZE]..[MAX_DIE_SIZE]. Depends only on the number of dice, not the mode.
 */
internal fun dieSize(availableWidth: Dp, count: Int): Dp {
    require(count > 0) { "Need at least one die" }
    // Whole dp, with 1 dp of slack, so pixel rounding never pushes the last die onto a new row.
    val fit = floor(((availableWidth - DIE_SPACING * (count - 1) - 1.dp) / count).value).dp
    return fit.coerceIn(MIN_DIE_SIZE, MAX_DIE_SIZE)
}
