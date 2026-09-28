package io.github.jamesmyatt.randomatizer.settings

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.jamesmyatt.randomatizer.dice.AdvancedSelection
import io.github.jamesmyatt.randomatizer.dice.DiceLimits
import io.github.jamesmyatt.randomatizer.dice.StandardDie
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppSettingsTest {
    @Test
    fun `face and pips must differ`() {
        assertFailsWith<IllegalArgumentException> { CustomDiceColours(face = 0xFF123456.toInt(), pips = 0xFF123456.toInt()) }
    }

    @Test
    fun `missing preferences give defaults`() {
        assertEquals(AppSettings(), emptyPreferences().toAppSettings())
    }

    @Test
    fun `settings survive a round trip`() {
        val settings = AppSettings(
            mode = Mode.Advanced,
            diceColourMode = DiceColourMode.Custom,
            customColours = CustomDiceColours(face = 0xFFB71C1C.toInt(), pips = 0xFFFFFFFF.toInt()),
            basicCount = 7,
            advancedSelection = AdvancedSelection(mapOf(StandardDie.D20 to 1, StandardDie.D100 to 3)),
            selectionExpanded = false,
        )
        val prefs = mutablePreferencesOf().apply { write(settings) }
        assertEquals(settings, prefs.toAppSettings())
    }

    @Test
    fun `invalid stored values fall back to safe values`() {
        val prefs = mutablePreferencesOf(
            stringPreferencesKey("mode") to "Nonsense",
            intPreferencesKey("basic_count") to 99,
            intPreferencesKey("custom_face") to 0xFF000000.toInt(),
            intPreferencesKey("custom_pips") to 0xFF000000.toInt(),
            intPreferencesKey("advanced_count_d6") to 0,
        )
        val settings = prefs.toAppSettings()
        assertEquals(Mode.Basic, settings.mode)
        assertEquals(DiceLimits.BASIC_MAX, settings.basicCount)
        assertEquals(CustomDiceColours.Default, settings.customColours)
        assertEquals(AdvancedSelection.Default, settings.advancedSelection)
    }
}
