package rx.dagger.internet.tester

import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun SpeedMeterPreview() {
    SpeedMeterWidget(
        modifier = Modifier.size(400.dp),
        currentValue = 50f
    )
}

@Composable
fun SpeedMeterWidget(
    modifier: Modifier = Modifier,
    dialValues: List<Float> = listOf(
        0f, 1f, 5f, 10f, 15f, 20f, 30f, 40f, 60f, 80f, 100f
    ),
    currentValue: Float = 0f,
    color: Color = Color.Blue,
    colorLight: Color = Color.LightGray,
    backgroundColor: Color = Color(0xfff3f5f9)
) {
    DialModerator(dialValues).moderate()
    val normalizedCurrentValue =
        CurrentValueNormalizer(currentValue, dialValues).normalize()

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
    ) {
        val radius = 42f
        val shadowRadius = radius * 1.4f
        val center = size.center
        val emptyAngle = 90f
        val startAngle = 90f + emptyAngle / 2
        val sweepAngle = 360f - emptyAngle

        drawBackground(color = backgroundColor)
        drawBackgroundCircle()
        drawArrowPinShadow(shadowRadius = radius * 1.4f)
        
        val arrowAngle = getAngle(
            dials = dialValues,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            currentValue = currentValue,
        )
        drawArrow(
            angle = arrowAngle,
            center = center
        )

        drawArrowPin(radius)
        drawDials(
            textMeasurer = textMeasurer
        )
        drawSpiralProgressBar(
            angle = arrowAngle,
            startAngle = startAngle
        )
    }
}

private class CurrentValueNormalizer(
    val currentValue: Float, val dialValues: List<Float>
) {
    fun normalize(): Float {
        val minimum = dialValues.first()
        val maximum = dialValues.last()

        return currentValue.coerceIn(minimum, maximum)
    }
}

private class DialModerator(val dialValues: List<Float>) {
    fun moderate() {
        require(dialValues.size in 2..11)

        repeat(dialValues.lastIndex - 1) { index ->
            require(dialValues[index] < dialValues[index + 1])
        }
    }
}

private fun DrawScope.drawBackground(
    color: Color
) {
    drawRect(color)
}

private fun Path.buildArrow(
    center: Offset,
    length: Float = 280f,
    startWidth: Float = 42f,
    endWidth: Float = 8f
): Path {
    moveTo(
        x = center.x,
        y = center.y
    )

    lineTo(
        x = center.x,
        y = center.y - startWidth / 2
    )

    lineTo(
        x = center.x + length,
        y = center.y - endWidth / 2
    )

    lineTo(
        x = center.x + length,
        y = center.y + endWidth / 2
    )

    lineTo(
        x = center.x,
        y = center.y + startWidth / 2
    )

    return this
}

private fun DrawScope.drawBackgroundCircle(
    radius: Float = 240f,
    width: Float = 32f
) {
    val linearGradientStart = pointOnCircle(
        center = center,
        radius = radius + width / 2,
        angleDegrees = 180 + 45f
    )

    val linearGradientEnd = pointOnCircle(
        center = center,
        radius = radius + width / 2,
        angleDegrees = 45f
    )

    drawCircle(
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0xffc8d6f8),
                1.0f to Color(0xffc6e2f8)
            ),
            start = linearGradientStart,
            end = linearGradientEnd
        ),
        radius = radius,
        style = Stroke(width = width)
    )
}

private fun DrawScope.drawDials(
    textMeasurer: TextMeasurer,
    dials: List<Float> = listOf(
        0f, 1f, 5f, 10f, 15f, 20f, 30f, 40f, 60f, 80f, 100f
    ),
    radius: Float = 320f,
    emptyAngle: Float = 90f,
) {
    val sweepAngle = 360f - emptyAngle
    val startAngle = 90f + emptyAngle / 2

    dials.forEachIndexed { i, n ->
        val deltaAngle = sweepAngle / (dials.lastIndex)
        val angle = startAngle + i * deltaAngle
        val pos = pointOnCircle(center, radius, angle)
        val textLayoutResult = textMeasurer.measure(
            text = n.toInt().toString(),
            style = TextStyle.Default.copy(
                fontWeight = FontWeight.SemiBold
            )
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                pos.x - textLayoutResult.size.width / 2,
                pos.y - textLayoutResult.size.height / 2
            )
        )
    }
}

private fun DrawScope.drawArrowPin(
    radius: Float
) {
    drawCircle(
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0xff3491e6),
                1.0f to Color(0xff185ef6)
            ),
            start = Offset(
                x = this.center.x,
                y = this.center.y - radius
            ),
            end = Offset(
                x = this.center.x,
                y = this.center.y + radius
            )
        ),
        radius = radius
    )
}

private fun DrawScope.drawArrowPinShadow(
    shadowRadius: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0xff185ef6),
                1.0f to Color.White
            ),
            radius = shadowRadius
        ),
        radius = shadowRadius
    )
}

private fun DrawScope.drawArrow(
    angle: Float,
    center: Offset,
    length: Float = 280f,
    startWidth: Float = 42f,
    endWidth: Float = 8f
) {
    rotate(
        degrees = angle,
        pivot = center
    ) {
        drawPath(
            path = Path().buildArrow(
                center,
                length,
                startWidth,
                endWidth
            ),
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xff3491e6),
                    1.0f to Color(0xff185ef6)
                ),
                start = Offset(
                    x = center.x + length,
                    y = center.y
                ),
                end = Offset(
                    x = center.x,
                    y = center.y
                )
            )
        )
    }
}

private fun DrawScope.drawSpiralProgressBar(
    angle: Float,
    startAngle: Float,
    progressBarRadius: Float = 380f,
) {
    val progressBarStart = pointOnCircle(
        center = center,
        radius = progressBarRadius,
        angleDegrees = startAngle
    )

    val delta = angle - startAngle
    val fullParts = (delta / 90f).toInt()
    val remainPart = delta % 90f

    val fullSpiralParts = (delta / 30f).toInt()
    val remainSpiralPart = delta % 30f

    val circleParts = mutableListOf<CubicBezier>()

    repeat(fullParts) {
        circleParts.add(arcToBezier(
            center,
            progressBarRadius,
            startAngle + 90 * it,
            startAngle + 90 * it + 90
        ))
    }

    if (remainPart > 0f) {
        circleParts.add(arcToBezier(
            center,
            progressBarRadius,
            startAngle + 90 * fullParts,
            startAngle + 90 * fullParts + remainPart
        ))
    }

    val progressBarPath = Path().apply {
        moveTo(
            x = progressBarStart.x,
            y = progressBarStart.y
        )

        var lastInnerPoint = Offset(
            x = progressBarStart.x,
            y = progressBarStart.y
        )

        var lastOuterPoint = Offset(
            x = progressBarStart.x,
            y = progressBarStart.y
        )

        // https://dribbble.com/shots/26150271-Internet-Speed-Test-Mobile-App

        repeat(fullSpiralParts) { i ->
            val cubicBezier = spiralArcToBezier(
                center,
                progressBarRadius,
                128f,
                135f,
                0f + 30 * i,
                0f + 30 * i + 30
            )

            cubicTo(
                x1 = cubicBezier.p1.x,
                y1 = cubicBezier.p1.y,
                x2 = cubicBezier.p2.x,
                y2 = cubicBezier.p2.y,
                x3 = cubicBezier.p3.x,
                y3 = cubicBezier.p3.y
            )

            lastOuterPoint = Offset(
                x = cubicBezier.p3.x,
                y = cubicBezier.p3.y
            )
        }

        if (remainSpiralPart > 0f) {
            val remainCubicBezier = spiralArcToBezier(
                center,
                progressBarRadius, 128f,
                135f,
                0f + 30 * fullSpiralParts,
                0f + 30 * fullSpiralParts + remainSpiralPart
            )

            cubicTo(
                x1 = remainCubicBezier.p1.x,
                y1 = remainCubicBezier.p1.y,
                x2 = remainCubicBezier.p2.x,
                y2 = remainCubicBezier.p2.y,
                x3 = remainCubicBezier.p3.x,
                y3 = remainCubicBezier.p3.y
            )

            lastOuterPoint = Offset(
                x = remainCubicBezier.p3.x,
                y = remainCubicBezier.p3.y
            )
        }

        val lastArc = circleParts.popOrNull()

        lastArc?.let {
            lastInnerPoint = lastArc.p3
            val normal = (lastArc.p3 - center).normalize()

            val tangent = Offset(
                -normal.y,
                normal.x
            )

            val distance = (lastInnerPoint - lastOuterPoint).length()
            val handle = distance * 0.5f

            cubicTo(
                x1 = lastOuterPoint.x + tangent.x * handle,
                y1 = lastOuterPoint.y + tangent.y * handle,

                x2 = lastInnerPoint.x + tangent.x * handle,
                y2 = lastInnerPoint.y + tangent.y * handle,

                x3 = lastInnerPoint.x,
                y3 = lastInnerPoint.y
            )

            cubicTo(
                x1 = it.p2.x,
                y1 = it.p2.y,
                x2 = it.p1.x,
                y2 = it.p1.y,
                x3 = it.p0.x,
                y3 = it.p0.y
            )
        }

        while (true) {
            val arc = circleParts.popOrNull()
            if (arc == null) break
            cubicTo(
                x1 = arc.p2.x,
                y1 = arc.p2.y,
                x2 = arc.p1.x,
                y2 = arc.p1.y,
                x3 = arc.p0.x,
                y3 = arc.p0.y
            )
        }
    }

    val shader = SweepGradient(
        center.x,
        center.y,
        intArrayOf(
            Color(0xff1650f5).toArgb(),
            Color(0xff1592f5).toArgb()
        ),
        null
    )

    val matrix = Matrix().apply {
        postRotate(
            90f,           // сюда ставишь угол, куда хочешь спрятать шов
            center.x,
            center.y
        )
    }

    shader.setLocalMatrix(matrix)

    val brush = ShaderBrush(shader)

    drawPath(
        path = progressBarPath,
        brush = brush
    )
}