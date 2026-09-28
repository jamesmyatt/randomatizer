package io.github.jamesmyatt.randomatizer.colour

import io.github.jamesmyatt.randomatizer.settings.CustomDiceColours
import io.github.jamesmyatt.randomatizer.settings.DiceColourMode

/** Colours (ARGB) used to draw a die. [outline] is null when the die needs no outline. */
data class DieStyle(val face: Int, val pips: Int, val outline: Int?)

/**
 * Resolves the die colours against the app's [background] and [foreground].
 *
 * Custom dice get an outline in the pips colour when the face is too close to the background.
 */
fun dieStyle(mode: DiceColourMode, custom: CustomDiceColours, background: Int, foreground: Int): DieStyle = when (mode) {
    DiceColourMode.System -> DieStyle(face = background, pips = foreground, outline = foreground)
    DiceColourMode.SystemInverted -> DieStyle(face = foreground, pips = background, outline = null)
    DiceColourMode.Custom -> DieStyle(
        face = custom.face,
        pips = custom.pips,
        outline = if (Contrast.isLow(custom.face, background)) custom.pips else null,
    )
}

/** Problems with custom dice colours on one background. */
data class DiceColourCheck(
    /** Face vs pips ratio. */
    val facePipsRatio: Double,
    /** True when both the face and the pips are too close to the background, so the die may be hard to see. */
    val hardToSeeOnBackground: Boolean,
) {
    val facePipsLow: Boolean get() = facePipsRatio < Contrast.MINIMUM
}

fun checkDiceColours(custom: CustomDiceColours, background: Int): DiceColourCheck = DiceColourCheck(
    facePipsRatio = Contrast.ratio(custom.face, custom.pips),
    hardToSeeOnBackground = Contrast.isLow(custom.face, background) && Contrast.isLow(custom.pips, background),
)
