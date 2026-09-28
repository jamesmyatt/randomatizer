package io.github.jamesmyatt.randomatizer.color

import io.github.jamesmyatt.randomatizer.settings.DiceFace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiceColorsTest {
    private val black = 0xFF000000.toInt()
    private val white = 0xFFFFFFFF.toInt()
    private val red = 0xFFB71C1C.toInt()
    private val orange = 0xFFE65100.toInt()
    private val darkSurface = 0xFF111318.toInt()
    private val lightSurface = 0xFFF9F9FF.toInt()
    private val cream = 0xFFFFF8E1.toInt()
    private val midGray = 0xFF757575.toInt()
    private val teal = 0xFF00897B.toInt()
    private val blue = 0xFF1976D2.toInt()
    private val lightGray = 0xFFB0B0B0.toInt()
    private val darkGray = 0xFF505050.toInt()

    @Test
    fun `contrast ratio matches WCAG reference values`() {
        assertEquals(21.0, Contrast.ratio(black, white), 1e-9)
        assertEquals(1.0, Contrast.ratio(red, red), 1e-9)
        assertEquals(Contrast.ratio(red, orange), Contrast.ratio(orange, red), 1e-12)
        assertEquals(1.7, Contrast.ratio(red, orange), 0.05)
        assertEquals(2.8, Contrast.ratio(red, darkSurface), 0.05)
    }

    @Test
    fun `background dice use the foreground for pips and outline`() {
        assertEquals(
            DieStyle(face = white, pips = black, outline = black),
            dieStyle(DiceFace.Background, red, background = white, foreground = black),
        )
    }

    @Test
    fun `foreground dice use the background for pips without an outline`() {
        assertEquals(
            DieStyle(face = black, pips = white, outline = null),
            dieStyle(DiceFace.Foreground, red, background = white, foreground = black),
        )
    }

    @Test
    fun `custom pips are the darker or lighter theme color, chosen by the face`() {
        // Light theme.
        assertEquals(lightSurface, dieStyle(DiceFace.Custom, red, lightSurface, darkSurface).pips)
        assertEquals(darkSurface, dieStyle(DiceFace.Custom, cream, lightSurface, darkSurface).pips)
        // Dark theme.
        assertEquals(lightSurface, dieStyle(DiceFace.Custom, red, darkSurface, lightSurface).pips)
        assertEquals(darkSurface, dieStyle(DiceFace.Custom, cream, darkSurface, lightSurface).pips)
    }

    @Test
    fun `mid-tone faces get the same kind of pips in light and dark`() {
        // Tinted theme colors where picking the higher contrast per theme would flip these faces.
        val light = 0xFFFFF8F6.toInt() to 0xFF231918.toInt()
        val dark = 0xFF1A110F.toInt() to 0xFFF1DFDA.toInt()
        // Teal: dark pips in both themes.
        assertEquals(light.second, dieStyle(DiceFace.Custom, teal, light.first, light.second).pips)
        assertEquals(dark.first, dieStyle(DiceFace.Custom, teal, dark.first, dark.second).pips)
        // Blue: light pips in both themes.
        assertEquals(light.first, dieStyle(DiceFace.Custom, blue, light.first, light.second).pips)
        assertEquals(dark.second, dieStyle(DiceFace.Custom, blue, dark.first, dark.second).pips)
    }

    @Test
    fun `custom dice get an outline in the pips color only when the face is close to the background`() {
        val onDark = dieStyle(DiceFace.Custom, red, darkSurface, lightSurface)
        assertEquals(onDark.pips, onDark.outline)
        assertNull(dieStyle(DiceFace.Custom, red, lightSurface, darkSurface).outline)
        val creamOnLight = dieStyle(DiceFace.Custom, cream, lightSurface, darkSurface)
        assertEquals(darkSurface, creamOnLight.outline)
    }

    @Test
    fun `warns about low pips contrast only when neither theme color contrasts with the face`() {
        // With near-white and near-black theme colors, one of them always contrasts enough.
        assertFalse(dieStyle(DiceFace.Custom, midGray, lightSurface, darkSurface).lowPipsContrast)
        assertFalse(dieStyle(DiceFace.Custom, midGray, darkSurface, lightSurface).lowPipsContrast)
        // A low-contrast theme cannot give the face readable pips.
        assertTrue(dieStyle(DiceFace.Custom, midGray, background = lightGray, foreground = darkGray).lowPipsContrast)
    }
}
