package io.github.jamesmyatt.randomatizer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import io.github.jamesmyatt.randomatizer.R
import io.github.jamesmyatt.randomatizer.colour.DieStyle
import io.github.jamesmyatt.randomatizer.dice.StandardDie

private const val CORNER_FRACTION = 0.19f
private const val OUTLINE_FRACTION = 0.03f
private const val PIP_RADIUS_FRACTION = 0.09f

private const val LOW = 0.28f
private const val MID = 0.5f
private const val HIGH = 0.72f

/** Pip centres as fractions of the die size, by face value. */
private val PIPS = mapOf(
    1 to listOf(MID to MID),
    2 to listOf(LOW to LOW, HIGH to HIGH),
    3 to listOf(LOW to LOW, MID to MID, HIGH to HIGH),
    4 to listOf(LOW to LOW, HIGH to LOW, LOW to HIGH, HIGH to HIGH),
    5 to listOf(LOW to LOW, HIGH to LOW, MID to MID, LOW to HIGH, HIGH to HIGH),
    6 to listOf(LOW to LOW, HIGH to LOW, LOW to MID, HIGH to MID, LOW to HIGH, HIGH to HIGH),
)

/** A d6 drawn with pips. */
@Composable
fun PipDie(value: Int, style: DieStyle, size: Dp, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.die_showing, StandardDie.D6.label, value)
    Canvas(modifier.size(size).semantics { contentDescription = description }) {
        val corner = this.size.minDimension * CORNER_FRACTION
        drawRoundRect(Color(style.face), cornerRadius = CornerRadius(corner))
        style.outline?.let { outline ->
            val width = this.size.minDimension * OUTLINE_FRACTION
            drawRoundRect(
                color = Color(outline),
                topLeft = Offset(width / 2, width / 2),
                size = Size(this.size.width - width, this.size.height - width),
                cornerRadius = CornerRadius(corner - width / 2),
                style = Stroke(width),
            )
        }
        PIPS.getValue(value).forEach { (x, y) ->
            drawCircle(
                color = Color(style.pips),
                radius = this.size.minDimension * PIP_RADIUS_FRACTION,
                center = Offset(x * this.size.width, y * this.size.height),
            )
        }
    }
}

/** A die of any type showing its value as a number. */
@Composable
fun NumberDie(die: StandardDie, value: Int, style: DieStyle, size: Dp, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.die_showing, die.label, value)
    val shape = RoundedCornerShape(size * CORNER_FRACTION)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(Color(style.face))
            .then(style.outline?.let { Modifier.border(size * OUTLINE_FRACTION, Color(it), shape) } ?: Modifier)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value.toString(),
            color = Color(style.pips),
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
