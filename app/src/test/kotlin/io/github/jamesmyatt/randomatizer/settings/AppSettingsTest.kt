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

class AppSettingsTest {
    @Test
    fun `missing preferences give defaults`() {
        assertEquals(AppSettings(), emptyPreferences().toAppSettings())
    }

    @Test
    fun `settings survive a round trip`() {
        val settings = AppSettings(
            mode = Mode.Advanced,
            diceFace = DiceFace.Custom,
            customFace = 0xFF0D47A1.toInt(),
            basicCount = 7,
            advancedSelection = AdvancedSelection(mapOf(StandardDie.D20 to 1, StandardDie.D100 to 3)),
            selectionExpanded = false,
            showTotal = false,
            themeMode = ThemeMode.Dark,
        )
        val prefs = mutablePreferencesOf().apply { write(settings) }
        assertEquals(settings, prefs.toAppSettings())
    }

    @Test
    fun `invalid stored values fall back to safe values`() {
        val prefs = mutablePreferencesOf(
            stringPreferencesKey("mode") to "Nonsense",
            stringPreferencesKey("theme_mode") to "Nonsense",
            intPreferencesKey("basic_count") to 99,
            stringPreferencesKey("dice_face") to "Nonsense",
            intPreferencesKey("custom_face") to 0x80FF0000.toInt(),
            intPreferencesKey("advanced_count_d6") to 0,
        )
        val settings = prefs.toAppSettings()
        assertEquals(Mode.Basic, settings.mode)
        assertEquals(ThemeMode.System, settings.themeMode)
        assertEquals(DiceLimits.BASIC_MAX, settings.basicCount)
        assertEquals(DiceFace.Background, settings.diceFace)
        assertEquals(AppSettings.DEFAULT_CUSTOM_FACE, settings.customFace)
        assertEquals(AdvancedSelection.Default, settings.advancedSelection)
    }

    @Test
    fun `basic to advanced keeps the same d6s`() {
        val advanced = AppSettings(mode = Mode.Basic, basicCount = 4).withMode(Mode.Advanced)
        assertEquals(Mode.Advanced, advanced.mode)
        assertEquals(AdvancedSelection(mapOf(StandardDie.D6 to 4)), advanced.advancedSelection)
    }

    @Test
    fun `advanced to basic keeps the number of dice up to the basic maximum`() {
        val mixed = AdvancedSelection(mapOf(StandardDie.D4 to 1, StandardDie.D20 to 2))
        assertEquals(3, AppSettings(mode = Mode.Advanced, advancedSelection = mixed).withMode(Mode.Basic).basicCount)
        val many = AdvancedSelection(mapOf(StandardDie.D6 to 10, StandardDie.D8 to 5))
        assertEquals(
            DiceLimits.BASIC_MAX,
            AppSettings(mode = Mode.Advanced, advancedSelection = many).withMode(Mode.Basic).basicCount,
        )
    }

    @Test
    fun `switching to the same mode changes nothing`() {
        val settings = AppSettings(mode = Mode.Basic, basicCount = 5)
        assertEquals(settings, settings.withMode(Mode.Basic))
    }
}
