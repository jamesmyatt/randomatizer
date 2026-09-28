package io.github.jamesmyatt.randomatizer.color

import io.github.jamesmyatt.randomatizer.settings.CustomDiceColors
import io.github.jamesmyatt.randomatizer.settings.DiceColorMode

/** Colors (ARGB) used to draw a die. [outline] is null when the die needs no outline. */
data class DieStyle(val face: Int, val pips: Int, val outline: Int?)

/**
 * Resolves the die colors against the app's [background] and [foreground].
 *
 * Custom dice get an outline in the pips color when the face is too close to the background.
 */
fun dieStyle(mode: DiceColorMode, custom: CustomDiceColors, background: Int, foreground: Int): DieStyle = when (mode) {
    DiceColorMode.System -> DieStyle(face = background, pips = foreground, outline = foreground)
    DiceColorMode.SystemInverted -> DieStyle(face = foreground, pips = background, outline = null)
    DiceColorMode.Custom -> DieStyle(
        face = custom.face,
        pips = custom.pips,
        outline = if (Contrast.isLow(custom.face, background)) custom.pips else null,
    )
}

/** Problems with custom dice colors on one background. */
data class DiceColorCheck(
    /** Face vs pips ratio. */
    val facePipsRatio: Double,
    /** True when both the face and the pips are too close to the background, so the die may be hard to see. */
    val hardToSeeOnBackground: Boolean,
) {
    val facePipsLow: Boolean get() = facePipsRatio < Contrast.MINIMUM
}

fun checkDiceColors(custom: CustomDiceColors, background: Int): DiceColorCheck = DiceColorCheck(
    facePipsRatio = Contrast.ratio(custom.face, custom.pips),
    hardToSeeOnBackground = Contrast.isLow(custom.face, background) && Contrast.isLow(custom.pips, background),
)
