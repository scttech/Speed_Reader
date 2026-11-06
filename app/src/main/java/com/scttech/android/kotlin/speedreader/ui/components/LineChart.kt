package com.scttech.android.kotlin.speedreader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scttech.android.kotlin.speedreader.ui.theme.RaceBlue
import com.scttech.android.kotlin.speedreader.ui.theme.RaceOrange
import com.scttech.android.kotlin.speedreader.ui.theme.RaceYellow

/** A labelled horizontal guide line on a chart, e.g. the user's goal. */
data class ChartReference(val value: Int, val label: String)

/**
 * Minimal line chart for [values] (oldest first). The y axis starts at 0 and always has room for the
 * highest value and every reference line (or [yMax] if given).
 */
@Composable
fun LineChart(
    values: List<Int>,
    references: List<ChartReference> = emptyList(),
    yMax: Int? = null,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
    val labelStyle = TextStyle(color = labelColor, fontSize = 11.sp)

    Canvas(modifier = modifier.fillMaxWidth().height(170.dp)) {
        val left = 44.dp.toPx(); val right = 12.dp.toPx(); val top = 12.dp.toPx(); val bottom = 12.dp.toPx()
        val w = size.width - left - right; val h = size.height - top - bottom
        val ceiling = (yMax ?: (maxOf(values.maxOrNull() ?: 0, references.maxOfOrNull { it.value } ?: 0) * 115 / 100)).coerceAtLeast(10)
        fun y(v: Int) = top + h * (1f - v.coerceIn(0, ceiling) / ceiling.toFloat())
        fun x(i: Int) = if (values.size <= 1) left + w / 2 else left + w * i / (values.size - 1).toFloat()

        // axis labels and baseline
        drawLine(gridColor, Offset(left, y(0)), Offset(left + w, y(0)), strokeWidth = 2f)
        drawText(measurer, "0", Offset(4.dp.toPx(), y(0) - 8.dp.toPx()), labelStyle)
        drawText(measurer, "$ceiling", Offset(4.dp.toPx(), y(ceiling) - 2.dp.toPx()), labelStyle)

        references.forEach { r ->
            drawLine(
                RaceBlue.copy(alpha = 0.8f), Offset(left, y(r.value)), Offset(left + w, y(r.value)),
                strokeWidth = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f)),
            )
            drawText(measurer, r.label, Offset(left + 4.dp.toPx(), y(r.value) - 15.dp.toPx()), labelStyle.copy(color = RaceBlue))
        }

        if (values.isEmpty()) return@Canvas
        val line = Path().apply { values.forEachIndexed { i, v -> if (i == 0) moveTo(x(i), y(v)) else lineTo(x(i), y(v)) } }
        if (values.size > 1) {
            val area = Path().apply { addPath(line); lineTo(x(values.lastIndex), y(0)); lineTo(x(0), y(0)); close() }
            drawPath(area, Brush.verticalGradient(listOf(RaceOrange.copy(alpha = 0.35f), Color.Transparent), startY = top, endY = y(0)))
            drawPath(line, RaceOrange, style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        values.forEachIndexed { i, v -> drawCircle(RaceYellow, radius = 5.dp.toPx(), center = Offset(x(i), y(v))) }
        // latest value label
        drawText(measurer, "${values.last()}", Offset((x(values.lastIndex) - 14.dp.toPx()).coerceAtLeast(left), y(values.last()) - 22.dp.toPx()), labelStyle.copy(color = RaceYellow))
    }
}
