package io.github.jamesmyatt.randomatizer.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.jamesmyatt.randomatizer.dice.AdvancedSelection
import io.github.jamesmyatt.randomatizer.dice.DiceLimits
import io.github.jamesmyatt.randomatizer.dice.StandardDie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Persists [AppSettings] with DataStore. Roll history is never stored here. */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    val settings: Flow<AppSettings> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it.toAppSettings() }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toAppSettings())) }
    }
}

private object Keys {
    val mode = stringPreferencesKey("mode")
    val diceColorMode = stringPreferencesKey("dice_color_mode")
    val customFace = intPreferencesKey("custom_face")
    val customPips = intPreferencesKey("custom_pips")
    val basicCount = intPreferencesKey("basic_count")
    val selectionExpanded = booleanPreferencesKey("selection_expanded")
    val showTotal = booleanPreferencesKey("show_total")
    val themeMode = stringPreferencesKey("theme_mode")
    val advancedCounts = StandardDie.entries.associateWith {
        intPreferencesKey("advanced_count_${it.name.lowercase()}")
    }
}

/** Reads settings, replacing missing or invalid values with defaults. */
internal fun Preferences.toAppSettings(): AppSettings {
    val defaults = AppSettings()
    val face = this[Keys.customFace]
    val pips = this[Keys.customPips]
    val hasAdvanced = Keys.advancedCounts.values.any { it in this }
    return AppSettings(
        mode = enumOrNull<Mode>(this[Keys.mode]) ?: defaults.mode,
        diceColorMode = enumOrNull<DiceColorMode>(this[Keys.diceColorMode]) ?: defaults.diceColorMode,
        customColors = if (face != null && pips != null && face != pips) {
            CustomDiceColors(face, pips)
        } else {
            defaults.customColors
        },
        basicCount = (this[Keys.basicCount] ?: defaults.basicCount)
            .coerceIn(DiceLimits.BASIC_MIN, DiceLimits.BASIC_MAX),
        advancedSelection = if (hasAdvanced) {
            AdvancedSelection.sanitized(Keys.advancedCounts.mapValues { (_, key) -> this[key] ?: 0 })
        } else {
            defaults.advancedSelection
        },
        selectionExpanded = this[Keys.selectionExpanded] ?: defaults.selectionExpanded,
        showTotal = this[Keys.showTotal] ?: defaults.showTotal,
        themeMode = enumOrNull<ThemeMode>(this[Keys.themeMode]) ?: defaults.themeMode,
    )
}

internal fun MutablePreferences.write(settings: AppSettings) {
    this[Keys.mode] = settings.mode.name
    this[Keys.diceColorMode] = settings.diceColorMode.name
    this[Keys.customFace] = settings.customColors.face
    this[Keys.customPips] = settings.customColors.pips
    this[Keys.basicCount] = settings.basicCount
    this[Keys.selectionExpanded] = settings.selectionExpanded
    this[Keys.showTotal] = settings.showTotal
    this[Keys.themeMode] = settings.themeMode.name
    Keys.advancedCounts.forEach { (die, key) -> this[key] = settings.advancedSelection.count(die) }
}

private inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? = enumValues<T>().firstOrNull { it.name == name }
