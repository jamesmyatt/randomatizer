package io.github.jamesmyatt.randomatizer

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import io.github.jamesmyatt.randomatizer.settings.SettingsRepository

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class RandomatizerApp : Application() {
    val settingsRepository by lazy { SettingsRepository(settingsDataStore) }
}
