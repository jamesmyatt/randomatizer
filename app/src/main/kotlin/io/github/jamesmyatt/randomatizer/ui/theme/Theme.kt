package io.github.jamesmyatt.randomatizer.ui.theme

import android.app.UiModeManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import io.github.jamesmyatt.randomatizer.settings.ThemeMode

/** Always uses the system dynamic colors (available from API 31), in the app's light/dark mode. */
@Composable
fun RandomatizerTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    MaterialTheme(colorScheme = colorScheme, content = content)
}

/**
 * Sets the app's light/dark mode. The system keeps it and applies it from the first frame of the next
 * launch, so the app never flashes the wrong theme while settings load. Null (not loaded yet) does nothing.
 */
@Composable
fun ApplyThemeMode(mode: ThemeMode?) {
    val context = LocalContext.current
    LaunchedEffect(mode) {
        if (mode == null) return@LaunchedEffect
        val nightMode = when (mode) {
            ThemeMode.System -> UiModeManager.MODE_NIGHT_AUTO
            ThemeMode.Light -> UiModeManager.MODE_NIGHT_NO
            ThemeMode.Dark -> UiModeManager.MODE_NIGHT_YES
        }
        context.getSystemService(UiModeManager::class.java).setApplicationNightMode(nightMode)
    }
}
