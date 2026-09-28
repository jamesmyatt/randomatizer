package io.github.jamesmyatt.randomatizer.color

import kotlin.math.pow

/** WCAG 2 contrast helpers for opaque ARGB colors. */
object Contrast {
    /** Minimum ratio for graphical objects and large text (WCAG 1.4.11 / 1.4.3). */
    const val MINIMUM = 3.0

    fun relativeLuminance(argb: Int): Double {
        fun channel(shift: Int): Double {
            val c = ((argb shr shift) and 0xFF) / 255.0
            return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }

    /** Contrast ratio from 1.0 (identical) to 21.0 (black on white). */
    fun ratio(a: Int, b: Int): Double {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    fun isLow(a: Int, b: Int): Boolean = ratio(a, b) < MINIMUM
}
