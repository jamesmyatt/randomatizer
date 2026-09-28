package io.github.jamesmyatt.randomatizer.color

import io.github.jamesmyatt.randomatizer.settings.DiceFace

/** Colors (ARGB) used to draw a die. [outline] is null when the die needs no outline. */
data class DieStyle(val face: Int, val pips: Int, val outline: Int?) {
    /** Face vs pips contrast ratio. */
    val pipsContrast: Double get() = Contrast.ratio(face, pips)
    val lowPipsContrast: Boolean get() = Contrast.isLow(face, pips)
}

/**
 * Resolves the die colors against the app's [background] and [foreground].
 *
 * Pips are whichever of [foreground] and [background] contrasts more with the face.
 * The die gets an outline in the pips color when the face is too close to the background.
 */
fun dieStyle(face: DiceFace, customFace: Int, background: Int, foreground: Int): DieStyle {
    val faceColor = when (face) {
        DiceFace.Background -> background
        DiceFace.Foreground -> foreground
        DiceFace.Custom -> customFace
    }
    val pips = if (Contrast.ratio(faceColor, foreground) >= Contrast.ratio(faceColor, background)) {
        foreground
    } else {
        background
    }
    return DieStyle(
        face = faceColor,
        pips = pips,
        outline = if (Contrast.isLow(faceColor, background)) pips else null,
    )
}
