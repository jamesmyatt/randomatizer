package io.github.jamesmyatt.randomatizer.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.jamesmyatt.randomatizer.R
import io.github.jamesmyatt.randomatizer.dice.AdvancedSelection
import io.github.jamesmyatt.randomatizer.dice.DieResult
import io.github.jamesmyatt.randomatizer.dice.Roll
import io.github.jamesmyatt.randomatizer.dice.StandardDie
import io.github.jamesmyatt.randomatizer.history.HistoryEntry
import io.github.jamesmyatt.randomatizer.settings.AppSettings
import io.github.jamesmyatt.randomatizer.settings.DiceFace
import io.github.jamesmyatt.randomatizer.settings.Mode
import io.github.jamesmyatt.randomatizer.settings.ThemeMode
import io.github.jamesmyatt.randomatizer.ui.theme.RandomatizerTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the main screens to PNGs. Images are only written when recording
 * (`-Proborazzi.test.record=true`); CI uploads them as an artifact.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Android 16: Espresso (via Compose UI test) does not yet support Robolectric's Android 17 sandbox.
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ScreenshotTest {
    private val advanced = AdvancedSelection(mapOf(StandardDie.D6 to 2, StandardDie.D8 to 1, StandardDie.D20 to 1))

    private fun entry(id: Long, mode: Mode, vararg results: Pair<StandardDie, Int>) =
        HistoryEntry(id, Roll(results.map { (die, value) -> DieResult(die, value) }), mode)

    private val basicHistory = listOf(
        entry(3, Mode.Basic, StandardDie.D6 to 4, StandardDie.D6 to 2, StandardDie.D6 to 5),
        entry(2, Mode.Basic, StandardDie.D6 to 6, StandardDie.D6 to 1, StandardDie.D6 to 3),
    )

    private val advancedHistory = listOf(
        entry(2, Mode.Advanced, StandardDie.D6 to 3, StandardDie.D6 to 5, StandardDie.D8 to 7, StandardDie.D20 to 14),
        entry(1, Mode.Advanced, StandardDie.D6 to 6, StandardDie.D6 to 2, StandardDie.D8 to 1, StandardDie.D20 to 20),
    )

    private fun capture(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        if (dark) RuntimeEnvironment.setQualifiers("+night")
        captureRoboImage("$DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH/$name.png") {
            RandomatizerTheme { content() }
        }
    }

    @Composable
    private fun Roller(settings: AppSettings, history: List<HistoryEntry>) {
        RollerScreen(
            state = RollerUiState(settings, history.first(), history),
            onRoll = {},
            onClearHistory = {},
            onUpdateSettings = {},
        )
    }

    /** The adaptive icon cropped as launchers show it (the middle 72 of 108 dp, as a circle), plus the themed icon. */
    @Test
    fun launcherIcon() = capture("launcher-icon") {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.background(Color(0xFF808080)).padding(24.dp),
        ) {
            LauncherIcon(R.drawable.ic_launcher_foreground, background = Color.White)
            LauncherIcon(
                R.drawable.ic_launcher_monochrome,
                background = Color(0xFFC8E6D4),
                tint = ColorFilter.tint(Color(0xFF1B3A2F)),
            )
        }
    }

    @Composable
    private fun LauncherIcon(@DrawableRes layer: Int, background: Color, tint: ColorFilter? = null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(144.dp).clip(CircleShape).background(background),
        ) {
            Image(
                painterResource(layer),
                contentDescription = null,
                colorFilter = tint,
                modifier = Modifier.requiredSize(216.dp),
            )
        }
    }

    @Test
    fun basicFour() = capture("basic-four") {
        val four = entry(
            1,
            Mode.Basic,
            StandardDie.D6 to 3,
            StandardDie.D6 to 5,
            StandardDie.D6 to 1,
            StandardDie.D6 to 6,
        )
        Roller(AppSettings(mode = Mode.Basic, basicCount = 4, selectionExpanded = false), listOf(four))
    }

    @Test
    fun basicSeven() = capture("basic-seven") {
        val values = listOf(1, 2, 3, 4, 5, 6, 3)
        val seven = entry(1, Mode.Basic, *values.map { StandardDie.D6 to it }.toTypedArray())
        Roller(AppSettings(mode = Mode.Basic, basicCount = 7, selectionExpanded = false), listOf(seven))
    }

    @Test
    fun basicLight() = capture("basic-light") {
        Roller(AppSettings(mode = Mode.Basic, basicCount = 3), basicHistory)
    }

    @Test
    fun basicLongHistory() = capture("basic-long-history") {
        val history = (30L downTo 1L).map { id ->
            entry(id, Mode.Basic, *List(3) { i -> StandardDie.D6 to ((id + i) % 6 + 1).toInt() }.toTypedArray())
        }
        Roller(AppSettings(mode = Mode.Basic, basicCount = 3), history)
    }

    @Test
    fun basicHistoryCollapsed() = capture("basic-history-collapsed") {
        Roller(AppSettings(mode = Mode.Basic, basicCount = 3, historyExpanded = false), basicHistory)
    }

    @Test
    fun basicDarkForeground() = capture("basic-dark-foreground", dark = true) {
        Roller(
            AppSettings(mode = Mode.Basic, basicCount = 3, diceFace = DiceFace.Foreground),
            basicHistory,
        )
    }

    @Test
    fun advancedDark() = capture("advanced-dark", dark = true) {
        Roller(AppSettings(mode = Mode.Advanced, advancedSelection = advanced), advancedHistory)
    }

    @Test
    fun advancedCustomCollapsedNoTotal() = capture("advanced-custom-collapsed-no-total") {
        Roller(
            AppSettings(
                mode = Mode.Advanced,
                advancedSelection = advanced,
                diceFace = DiceFace.Custom,
                selectionExpanded = false,
                showTotal = false,
            ),
            advancedHistory,
        )
    }

    @Test
    fun settingsCustomLemon() = capture("settings-custom-lemon") {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            SettingsContent(
                settings = AppSettings(
                    diceFace = DiceFace.Custom,
                    customFace = 0xFFFFF59D.toInt(),
                    themeMode = ThemeMode.Dark,
                ),
                onUpdate = {},
            )
        }
    }
}
