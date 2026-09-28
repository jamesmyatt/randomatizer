package io.github.jamesmyatt.randomatizer.color

import io.github.jamesmyatt.randomatizer.settings.DiceFace

private const val BLACK = 0xFF000000.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()

/** Colors (ARGB) used to draw a die. [outline] is null when the die needs no outline. */
data class DieStyle(val face: Int, val pips: Int, val outline: Int?) {
    /** Face vs pips contrast ratio. */
    val pipsContrast: Double get() = Contrast.ratio(face, pips)
    val lowPipsContrast: Boolean get() = Contrast.isLow(face, pips)
}

/**
 * Resolves the die colors against the app's [background] and [foreground].
 *
 * Pips are the darker or lighter of [foreground] and [background]. Which one depends only on the face
 * (whichever of black and white contrasts more with it), so a face gets the same kind of pips in light and dark.
 * The die gets an outline in the pips color when the face is too close to the background.
 */
fun dieStyle(face: DiceFace, customFace: Int, background: Int, foreground: Int): DieStyle {
    val faceColor = when (face) {
        DiceFace.Background -> background
        DiceFace.Foreground -> foreground
        DiceFace.Custom -> customFace
    }
    val foregroundIsDarker = Contrast.relativeLuminance(foreground) < Contrast.relativeLuminance(background)
    val darker = if (foregroundIsDarker) foreground else background
    val lighter = if (foregroundIsDarker) background else foreground
    val pips = if (Contrast.ratio(faceColor, BLACK) >= Contrast.ratio(faceColor, WHITE)) darker else lighter
    return DieStyle(
        face = faceColor,
        pips = pips,
        outline = if (Contrast.isLow(faceColor, background)) pips else null,
    )
}
