package io.github.jamesmyatt.randomatizer.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val THUMB_WIDTH = 4.dp
private val THUMB_MARGIN = 4.dp
private val THUMB_MIN_HEIGHT = 24.dp

/**
 * Draws a thin scroll thumb at the end edge of a lazy list whose rows are about the same height.
 * Hidden when everything fits.
 */
internal fun Modifier.verticalScrollIndicator(state: LazyListState, color: Color): Modifier = drawWithContent {
    drawContent()
    val visible = state.layoutInfo.visibleItemsInfo
    if (visible.isEmpty() || !(state.canScrollForward || state.canScrollBackward)) return@drawWithContent
    val rowHeight = visible.sumOf { it.size }.toFloat() / visible.size
    val contentHeight = rowHeight * state.layoutInfo.totalItemsCount
    val viewport = size.height
    val scrolled = state.firstVisibleItemIndex * rowHeight + state.firstVisibleItemScrollOffset
    val thumbHeight = (viewport * viewport / contentHeight).coerceIn(THUMB_MIN_HEIGHT.toPx(), viewport)
    val progress = (scrolled / (contentHeight - viewport)).coerceIn(0f, 1f)
    val width = THUMB_WIDTH.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width - width - THUMB_MARGIN.toPx(), progress * (viewport - thumbHeight)),
        size = Size(width, thumbHeight),
        cornerRadius = CornerRadius(width / 2),
    )
}
