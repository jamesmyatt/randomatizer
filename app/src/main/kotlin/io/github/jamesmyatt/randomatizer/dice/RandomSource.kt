package io.github.jamesmyatt.randomatizer.dice

import java.security.SecureRandom

/** Source of uniformly distributed random integers. */
fun interface RandomSource {
    /** Returns a uniformly distributed value in `0 until bound`. [bound] must be positive. */
    fun nextInt(bound: Int): Int
}

/**
 * Production [RandomSource] backed by [SecureRandom].
 *
 * `SecureRandom.nextInt(bound)` uses rejection sampling, so results have no modulo bias.
 */
class SecureRandomSource(private val random: SecureRandom = SecureRandom()) : RandomSource {
    override fun nextInt(bound: Int): Int = random.nextInt(bound)
}
