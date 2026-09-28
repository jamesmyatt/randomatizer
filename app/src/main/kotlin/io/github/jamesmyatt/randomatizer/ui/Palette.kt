package io.github.jamesmyatt.randomatizer.ui

import androidx.annotation.StringRes
import io.github.jamesmyatt.randomatizer.R

data class Swatch(val argb: Int, @StringRes val name: Int)

/** Preset colors offered for custom dice. */
val DicePalette = listOf(
    Swatch(0xFF000000.toInt(), R.string.color_black),
    Swatch(0xFFFFFFFF.toInt(), R.string.color_white),
    Swatch(0xFF424242.toInt(), R.string.color_dark_gray),
    Swatch(0xFFE0E0E0.toInt(), R.string.color_light_gray),
    Swatch(0xFF0D1B2A.toInt(), R.string.color_navy),
    Swatch(0xFFFFF8E1.toInt(), R.string.color_cream),
    Swatch(0xFFB71C1C.toInt(), R.string.color_red),
    Swatch(0xFFE65100.toInt(), R.string.color_orange),
    Swatch(0xFF1B5E20.toInt(), R.string.color_green),
    Swatch(0xFF006064.toInt(), R.string.color_teal),
    Swatch(0xFF0D47A1.toInt(), R.string.color_blue),
    Swatch(0xFF4A148C.toInt(), R.string.color_purple),
)
