package com.hotaro.duckystore.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    waveAmplitude: Dp = 3.dp,
    waveFrequency: Float = 0.08f,
    strokeWidth: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerY = h / 2f
        val amplitude = waveAmplitude.toPx()
        val freq = waveFrequency
        
        val filledWidth = w * progress

        val lastY = if (filledWidth > 0f) centerY + amplitude * Math.sin((filledWidth * freq - phase).toDouble()).toFloat() else centerY

        // Draw track connected to the end of the wave and tapered to center
        if (filledWidth < w) {
            val trackPath = Path()
            trackPath.moveTo(filledWidth, lastY)
            val distanceToCenter = Math.min(40f, w - filledWidth)
            if (distanceToCenter > 0f) {
                trackPath.cubicTo(
                    filledWidth + distanceToCenter / 2, lastY,
                    filledWidth + distanceToCenter / 2, centerY,
                    filledWidth + distanceToCenter, centerY
                )
            }
            trackPath.lineTo(w, centerY)

            drawPath(
                path = trackPath,
                color = trackColor,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Draw wavy filled part
        if (filledWidth > 0f) {
            val path = Path()
            var first = true
            for (x in 0..filledWidth.toInt() step 2) {
                val y = centerY + amplitude * Math.sin((x * freq - phase).toDouble()).toFloat()
                if (first) {
                    path.moveTo(x.toFloat(), y)
                    first = false
                } else {
                    path.lineTo(x.toFloat(), y)
                }
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokeWidth.toPx(), 
                    cap = StrokeCap.Round, 
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
