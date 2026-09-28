package io.github.jamesmyatt.randomatizer.settings

import io.github.jamesmyatt.randomatizer.dice.AdvancedSelection
import io.github.jamesmyatt.randomatizer.dice.DiceLimits

enum class Mode { Basic, Advanced }

/** Where the dice colors come from. The rest of the UI always uses the system (dynamic) colors. */
enum class DiceColorMode { System, SystemInverted, Custom }

/** User-chosen dice colors as ARGB. Face and pips must differ. */
data class CustomDiceColors(val face: Int, val pips: Int) {
    init {
        require(face != pips) { "Face and pips colors must differ" }
    }

    companion object {
        val Default = CustomDiceColors(face = 0xFFFFFFFF.toInt(), pips = 0xFF000000.toInt())
    }
}

/** Persisted settings, including the last dice selection. Never holds roll results. */
data class AppSettings(
    val mode: Mode = Mode.Basic,
    val diceColorMode: DiceColorMode = DiceColorMode.System,
    val customColors: CustomDiceColors = CustomDiceColors.Default,
    val basicCount: Int = DEFAULT_BASIC_COUNT,
    val advancedSelection: AdvancedSelection = AdvancedSelection.Default,
    val selectionExpanded: Boolean = true,
    /** Show the total on the main screen and in the history. */
    val showTotal: Boolean = true,
) {
    init {
        require(basicCount in DiceLimits.BASIC_MIN..DiceLimits.BASIC_MAX) { "Basic count out of range: $basicCount" }
    }

    companion object {
        const val DEFAULT_BASIC_COUNT = 2
    }
}
