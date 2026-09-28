package io.github.jamesmyatt.randomatizer.ui

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DieSizeTest {
    // A 412 dp wide phone with 24 dp padding each side.
    private val phone = 364.dp

    @Test
    fun `one or two dice use the maximum size`() {
        assertEquals(MAX_DIE_SIZE, dieSize(phone, 1))
        assertEquals(MAX_DIE_SIZE, dieSize(phone, 2))
    }

    @Test
    fun `three dice fill the width`() {
        assertEquals(107.dp, dieSize(phone, 3)) // (364 - 40 - 1) / 3 = 107.7
    }

    @Test
    fun `four dice just fit and more use the minimum size`() {
        assertEquals(MIN_DIE_SIZE, dieSize(phone, 4)) // (364 - 60 - 1) / 4 = 75.75
        assertEquals(MIN_DIE_SIZE, dieSize(phone, 5))
        assertEquals(MIN_DIE_SIZE, dieSize(phone, 84))
    }

    @Test
    fun `wider screens fit more dice before reaching the minimum`() {
        assertEquals(90.dp, dieSize(752.dp, 7)) // (752 - 120 - 1) / 7 = 90.1
    }

    @Test
    fun `sizes always fit in one row`() {
        for (count in 1..4) {
            val size = dieSize(phone, count)
            assert(size * count + DIE_SPACING * (count - 1) < phone) { "$count dice at $size overflow" }
        }
    }

    @Test
    fun `needs at least one die`() {
        assertFailsWith<IllegalArgumentException> { dieSize(phone, 0) }
    }
}
