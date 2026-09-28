package io.github.jamesmyatt.randomatizer.colour

import io.github.jamesmyatt.randomatizer.settings.CustomDiceColours
import io.github.jamesmyatt.randomatizer.settings.DiceColourMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiceColoursTest {
    private val black = 0xFF000000.toInt()
    private val white = 0xFFFFFFFF.toInt()
    private val red = 0xFFB71C1C.toInt()
    private val orange = 0xFFE65100.toInt()
    private val darkSurface = 0xFF111318.toInt()
    private val lightSurface = 0xFFF9F9FF.toInt()
    private val navy = 0xFF0D1B2A.toInt()
    private val darkGrey = 0xFF424242.toInt()

    @Test
    fun `contrast ratio matches WCAG reference values`() {
        assertEquals(21.0, Contrast.ratio(black, white), 1e-9)
        assertEquals(1.0, Contrast.ratio(red, red), 1e-9)
        assertEquals(Contrast.ratio(red, orange), Contrast.ratio(orange, red), 1e-12)
        assertEquals(1.7, Contrast.ratio(red, orange), 0.05)
        assertEquals(2.8, Contrast.ratio(red, darkSurface), 0.05)
    }

    @Test
    fun `system dice use the background face with a foreground outline`() {
        assertEquals(DieStyle(face = white, pips = black, outline = black), dieStyle(DiceColourMode.System, CustomDiceColours.Default, white, black))
    }

    @Test
    fun `system inverted dice swap face and pips without an outline`() {
        assertEquals(DieStyle(face = black, pips = white, outline = null), dieStyle(DiceColourMode.SystemInverted, CustomDiceColours.Default, white, black))
    }

    @Test
    fun `custom dice get an outline in the pips colour only when the face is close to the background`() {
        val custom = CustomDiceColours(face = red, pips = orange)
        assertEquals(orange, dieStyle(DiceColourMode.Custom, custom, darkSurface, white).outline)
        assertNull(dieStyle(DiceColourMode.Custom, custom, lightSurface, black).outline)
    }

    @Test
    fun `warns about low face and pips contrast`() {
        assertTrue(checkDiceColours(CustomDiceColours(red, orange), lightSurface).facePipsLow)
        assertFalse(checkDiceColours(CustomDiceColours(red, white), lightSurface).facePipsLow)
    }

    @Test
    fun `warns only when face and pips are both close to the background`() {
        // Face close to the dark background but pips are not: outline only, no warning.
        assertFalse(checkDiceColours(CustomDiceColours(red, orange), darkSurface).hardToSeeOnBackground)
        // Both close to the dark background.
        assertTrue(checkDiceColours(CustomDiceColours(navy, darkGrey), darkSurface).hardToSeeOnBackground)
        assertFalse(checkDiceColours(CustomDiceColours(navy, darkGrey), lightSurface).hardToSeeOnBackground)
    }
}
