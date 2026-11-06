package com.scttech.android.kotlin.speedreader.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scttech.android.kotlin.speedreader.ui.theme.RaceBlue
import com.scttech.android.kotlin.speedreader.ui.theme.RaceOrange
import com.scttech.android.kotlin.speedreader.ui.theme.RaceRed
import com.scttech.android.kotlin.speedreader.ui.theme.RaceYellow
import kotlin.math.cos
import kotlin.math.sin

private const val START_ANGLE = 150f
private const val SWEEP_ANGLE = 240f
private const val SEGMENTS = 40

private fun segmentColor(fraction: Float): Color =
    if (fraction < 0.5f) lerp(RaceYellow, RaceOrange, fraction / 0.5f)
    else lerp(RaceOrange, RaceRed, (fraction - 0.5f) / 0.5f)

/**
 * Racing-style gauge. [level] is 0..100 (progress toward the user's goal);
 * the centre shows [effectiveWpm] against [goalWpm].
 */
@Composable
fun SpeedGauge(
    level: Int,
    effectiveWpm: Int,
    goalWpm: Int,
    modifier: Modifier = Modifier,
) {
    val fraction by animateFloatAsState(
        targetValue = level.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "gaugeFraction",
    )

    Box(modifier = modifier.fillMaxWidth().aspectRatio(1.15f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize().padding(12.dp)) {
            val stroke = size.minDimension * 0.07f
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            val segSweep = SWEEP_ANGLE / SEGMENTS
            val gap = 1.6f

            // Segmented track: lit segments are coloured, unlit are dim.
            for (i in 0 until SEGMENTS) {
                val frac = (i + 0.5f) / SEGMENTS
                val lit = frac <= fraction
                drawArc(
                    color = if (lit) segmentColor(frac) else Color.White.copy(alpha = 0.10f),
                    startAngle = START_ANGLE + i * segSweep + gap / 2,
                    sweepAngle = segSweep - gap,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
            }

            // Outer blue rim, as in the logo.
            drawArc(
                color = RaceBlue,
                startAngle = START_ANGLE - 4f,
                sweepAngle = SWEEP_ANGLE + 8f,
                useCenter = false,
                topLeft = topLeft - Offset(stroke * 0.9f, stroke * 0.9f),
                size = Size(diameter + stroke * 1.8f, diameter + stroke * 1.8f),
                style = Stroke(width = stroke * 0.12f, cap = StrokeCap.Round),
            )

            // Needle.
            val center = Offset(size.width / 2f, size.height / 2f)
            val angle = Math.toRadians((START_ANGLE + SWEEP_ANGLE * fraction).toDouble())
            val length = diameter / 2f - stroke * 1.4f
            val tip = center + Offset((cos(angle) * length).toFloat(), (sin(angle) * length).toFloat())
            drawLine(
                color = RaceRed,
                start = center,
                end = tip,
                strokeWidth = stroke * 0.22f,
                cap = StrokeCap.Round,
            )
            drawCircle(color = Color.White, radius = stroke * 0.32f, center = center)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)) {
            Text(
                text = "$effectiveWpm",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "effective WPM",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Level $level / 100  ·  goal $goalWpm",
                style = MaterialTheme.typography.labelLarge,
                color = RaceOrange,
            )
        }
    }
}
