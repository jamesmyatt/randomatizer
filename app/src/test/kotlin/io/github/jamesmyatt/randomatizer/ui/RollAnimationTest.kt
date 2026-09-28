package io.github.jamesmyatt.randomatizer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.jamesmyatt.randomatizer.dice.DieResult
import io.github.jamesmyatt.randomatizer.dice.Roll
import io.github.jamesmyatt.randomatizer.dice.StandardDie
import io.github.jamesmyatt.randomatizer.history.HistoryEntry
import io.github.jamesmyatt.randomatizer.settings.AppSettings
import io.github.jamesmyatt.randomatizer.settings.Mode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// Android 16: Espresso (via Compose UI test) does not yet support Robolectric's Android 17 sandbox.
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class RollAnimationTest {
    @get:Rule
    val compose = createComposeRule()

    private fun entry(id: Long, vararg values: Int) =
        HistoryEntry(id, Roll(values.map { DieResult(StandardDie.D6, it) }), Mode.Basic)

    @Test
    fun newRollJoinsHistoryOnlyAfterAnimation() {
        val first = entry(1, 1, 2, 3)
        val second = entry(2, 6, 5, 4)
        val settings = AppSettings(mode = Mode.Basic, basicCount = 3, selectionExpanded = false)
        var state by mutableStateOf(RollerUiState(settings, first, listOf(first)))
        compose.setContent { RollerScreen(state, onRoll = {}, onClearHistory = {}, onUpdateSettings = {}) }
        compose.waitForIdle()
        compose.onNodeWithText("1 · 2 · 3").assertExists()

        compose.mainClock.autoAdvance = false
        state = RollerUiState(settings, second, listOf(second, first))
        // Every frame of the animation, including the first, hides the new row.
        repeat(6) {
            compose.mainClock.advanceTimeByFrame()
            compose.onNodeWithText("6 · 5 · 4").assertDoesNotExist()
            compose.onNodeWithText("1 · 2 · 3").assertExists()
        }

        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithText("6 · 5 · 4").assertExists()
    }
}
