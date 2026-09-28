package io.github.jamesmyatt.randomatizer.ui

import androidx.annotation.StringRes
import io.github.jamesmyatt.randomatizer.R

data class Swatch(val argb: Int, @StringRes val name: Int)

/** Preset custom face colors, in rows of six: strong, light, then neutrals and extras. */
val DicePalette = listOf(
    Swatch(0xFFD32F2F.toInt(), R.string.color_red),
    Swatch(0xFFF57C00.toInt(), R.string.color_orange),
    Swatch(0xFFFFD600.toInt(), R.string.color_yellow),
    Swatch(0xFF388E3C.toInt(), R.string.color_green),
    Swatch(0xFF1976D2.toInt(), R.string.color_blue),
    Swatch(0xFF7B1FA2.toInt(), R.string.color_purple),

    Swatch(0xFFF48FB1.toInt(), R.string.color_pink),
    Swatch(0xFFFFCC80.toInt(), R.string.color_peach),
    Swatch(0xFFFFF59D.toInt(), R.string.color_lemon),
    Swatch(0xFFA5D6A7.toInt(), R.string.color_mint),
    Swatch(0xFF90CAF9.toInt(), R.string.color_sky_blue),
    Swatch(0xFFCE93D8.toInt(), R.string.color_lavender),

    Swatch(0xFFFFFFFF.toInt(), R.string.color_white),
    Swatch(0xFF9E9E9E.toInt(), R.string.color_gray),
    Swatch(0xFF000000.toInt(), R.string.color_black),
    Swatch(0xFF6D4C41.toInt(), R.string.color_brown),
    Swatch(0xFF00897B.toInt(), R.string.color_teal),
    Swatch(0xFFC2185B.toInt(), R.string.color_magenta),
)
