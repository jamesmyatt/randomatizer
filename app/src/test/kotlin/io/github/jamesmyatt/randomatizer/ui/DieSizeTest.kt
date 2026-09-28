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
    fun `three and four dice fill the width`() {
        assertEquals(110.dp, dieSize(phone, 3)) // (364 - 32 - 1) / 3 = 110.3
        assertEquals(78.dp, dieSize(phone, 4)) // (364 - 48 - 1) / 4 = 78.75
    }

    @Test
    fun `five dice just fit and more use the minimum size`() {
        assertEquals(59.dp, dieSize(phone, 5)) // (364 - 64 - 1) / 5 = 59.8
        assertEquals(MIN_DIE_SIZE, dieSize(phone, 6))
        assertEquals(MIN_DIE_SIZE, dieSize(phone, 70))
    }

    @Test
    fun `wider screens fit more dice before reaching the minimum`() {
        assertEquals(93.dp, dieSize(752.dp, 7)) // (752 - 96 - 1) / 7 = 93.6
    }

    @Test
    fun `sizes always fit in one row`() {
        for (count in 1..5) {
            val size = dieSize(phone, count)
            assert(size * count + DIE_SPACING * (count - 1) < phone) { "$count dice at $size overflow" }
        }
    }

    @Test
    fun `needs at least one die`() {
        assertFailsWith<IllegalArgumentException> { dieSize(phone, 0) }
    }
}
