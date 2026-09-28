package io.github.jamesmyatt.randomatizer.dice

import kotlin.random.Random

/** Deterministic [RandomSource] for tests. `Random.nextInt(until)` is unbiased. */
class SeededRandomSource(seed: Long) : RandomSource {
    private val random = Random(seed)

    override fun nextInt(bound: Int): Int = random.nextInt(bound)
}
