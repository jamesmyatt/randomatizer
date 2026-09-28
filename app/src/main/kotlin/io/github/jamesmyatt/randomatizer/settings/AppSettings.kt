package io.github.jamesmyatt.randomatizer.settings

import io.github.jamesmyatt.randomatizer.dice.AdvancedSelection
import io.github.jamesmyatt.randomatizer.dice.DiceLimits
import io.github.jamesmyatt.randomatizer.dice.StandardDie

enum class Mode { Basic, Advanced }

/**
 * The dice face color: the app's background or foreground, or a custom color.
 * Pips, numbers and outline are derived from it. The rest of the UI always uses the system (dynamic) colors.
 */
enum class DiceFace { Background, Foreground, Custom }

/** Light or dark app theme. System follows the device setting. */
enum class ThemeMode { System, Light, Dark }

/** Persisted settings, including the last dice selection. Never holds roll results. */
data class AppSettings(
    val mode: Mode = Mode.Basic,
    val diceFace: DiceFace = DiceFace.Background,
    /** Custom face color as ARGB, used when [diceFace] is [DiceFace.Custom]. */
    val customFace: Int = DEFAULT_CUSTOM_FACE,
    val basicCount: Int = DEFAULT_BASIC_COUNT,
    val advancedSelection: AdvancedSelection = AdvancedSelection.Default,
    val selectionExpanded: Boolean = true,
    /** Show the total on the main screen and in the history. */
    val showTotal: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
) {
    init {
        require(basicCount in DiceLimits.BASIC_MIN..DiceLimits.BASIC_MAX) { "Basic count out of range: $basicCount" }
    }

    /**
     * Switches mode, carrying the dice over: Basic to Advanced keeps the same d6s;
     * Advanced to Basic keeps the number of dice, up to the Basic maximum.
     */
    fun withMode(newMode: Mode): AppSettings = when {
        newMode == mode -> this

        newMode == Mode.Advanced -> copy(
            mode = newMode,
            advancedSelection = AdvancedSelection(mapOf(StandardDie.D6 to basicCount)),
        )

        else -> copy(
            mode = newMode,
            basicCount = advancedSelection.total.coerceIn(DiceLimits.BASIC_MIN, DiceLimits.BASIC_MAX),
        )
    }

    companion object {
        const val DEFAULT_BASIC_COUNT = 2
        const val DEFAULT_CUSTOM_FACE = 0xFFD32F2F.toInt()
    }
}
