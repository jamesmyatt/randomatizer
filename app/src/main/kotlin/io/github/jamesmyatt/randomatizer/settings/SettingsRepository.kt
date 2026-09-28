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

    /** Applies [transform] and returns the settings as saved. */
    suspend fun update(transform: (AppSettings) -> AppSettings): AppSettings =
        dataStore.edit { prefs -> prefs.write(transform(prefs.toAppSettings())) }.toAppSettings()
}

private object Keys {
    val mode = stringPreferencesKey("mode")
    val diceFace = stringPreferencesKey("dice_face")
    val customFace = intPreferencesKey("custom_face")
    val basicCount = intPreferencesKey("basic_count")
    val selectionExpanded = booleanPreferencesKey("selection_expanded")
    val historyExpanded = booleanPreferencesKey("history_expanded")
    val showTotal = booleanPreferencesKey("show_total")
    val historyEnabled = booleanPreferencesKey("history_enabled")
    val themeMode = stringPreferencesKey("theme_mode")
    val advancedCounts = StandardDie.entries.associateWith {
        intPreferencesKey("advanced_count_${it.name.lowercase()}")
    }
}

/** Reads settings, replacing missing or invalid values with defaults. */
internal fun Preferences.toAppSettings(): AppSettings {
    val defaults = AppSettings()
    val hasAdvanced = Keys.advancedCounts.values.any { it in this }
    return AppSettings(
        mode = enumOrNull<Mode>(this[Keys.mode]) ?: defaults.mode,
        diceFace = enumOrNull<DiceFace>(this[Keys.diceFace]) ?: defaults.diceFace,
        // Opaque only: the face is drawn over the background.
        customFace = this[Keys.customFace]?.takeIf { it ushr 24 == 0xFF } ?: defaults.customFace,
        basicCount = (this[Keys.basicCount] ?: defaults.basicCount)
            .coerceIn(DiceLimits.BASIC_MIN, DiceLimits.BASIC_MAX),
        advancedSelection = if (hasAdvanced) {
            AdvancedSelection.sanitized(Keys.advancedCounts.mapValues { (_, key) -> this[key] ?: 0 })
        } else {
            defaults.advancedSelection
        },
        selectionExpanded = this[Keys.selectionExpanded] ?: defaults.selectionExpanded,
        historyExpanded = this[Keys.historyExpanded] ?: defaults.historyExpanded,
        showTotal = this[Keys.showTotal] ?: defaults.showTotal,
        historyEnabled = this[Keys.historyEnabled] ?: defaults.historyEnabled,
        themeMode = enumOrNull<ThemeMode>(this[Keys.themeMode]) ?: defaults.themeMode,
    )
}

internal fun MutablePreferences.write(settings: AppSettings) {
    this[Keys.mode] = settings.mode.name
    this[Keys.diceFace] = settings.diceFace.name
    this[Keys.customFace] = settings.customFace
    this[Keys.basicCount] = settings.basicCount
    this[Keys.selectionExpanded] = settings.selectionExpanded
    this[Keys.historyExpanded] = settings.historyExpanded
    this[Keys.showTotal] = settings.showTotal
    this[Keys.historyEnabled] = settings.historyEnabled
    this[Keys.themeMode] = settings.themeMode.name
    Keys.advancedCounts.forEach { (die, key) -> this[key] = settings.advancedSelection.count(die) }
}

private inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? = enumValues<T>().firstOrNull { it.name == name }
