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
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun SpeedMeterPreview400() {
    SpeedMeterWidget(
        modifier = Modifier.size(400.dp),
        currentValue = 8.3f,
        color = Color.Green,
        colorLight = Color.Cyan,
        dialValues = listOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f),
        backgroundColor = Color.Black,
        backgroundCircleLinearGradientStart = Color.Magenta,
        backgroundCircleLinearGradientEnd = Color.Red,
        textColor = Color.White
    )
}

@Preview
@Composable
fun SpeedMeterPreview300() {
    SpeedMeterWidget(
        modifier = Modifier.size(300.dp),
        currentValue = 80f
    )
}

@Preview
@Composable
fun SpeedMeterPreview200() {
    SpeedMeterWidget(
        modifier = Modifier.size(200.dp),
        currentValue = 60f
    )
}

@Preview
@Composable
fun SpeedMeterPreview100() {
    SpeedMeterWidget(
        modifier = Modifier.size(100.dp),
        currentValue = 20f
    )
}

@Composable
fun SpeedMeterWidget(
    modifier: Modifier = Modifier,
    dialValues: List<Float> = listOf(
        0f, 1f, 5f, 10f, 15f, 20f, 30f, 40f, 60f, 80f, 100f
    ),
    currentValue: Float = 0f,
    color: Color = Color(0xff1650f5),
    colorLight: Color = Color(0xff1592f5),
    backgroundCircleLinearGradientStart: Color = Color(0xffc8d6f8),
    backgroundCircleLinearGradientEnd: Color = Color(0xffc6e2f8),
    backgroundColor: Color = Color(0xfff3f5f9),
    textColor: Color = Color.Black
) {
    DialModerator(dialValues).moderate()
    val normalizedCurrentValue =
        CurrentValueNormalizer(currentValue, dialValues).normalize()

    val textMeasurer = rememberTextMeasurer()

    val shadowFraction = 1.4f
    Canvas(
        modifier = modifier
    ) {
        val radius = 0.04f * size.minDimension
        val center = size.center

        val emptyAngle = 90f
        val startAngle = 90f + emptyAngle / 2
        val sweepAngle = 360f - emptyAngle

        drawBackground(color = backgroundColor)
        drawBackgroundCircle(
            linearGradientColorStart = backgroundCircleLinearGradientStart,
            linearGradientColorEnd = backgroundCircleLinearGradientEnd
        )
        drawArrowPinShadow(
            shadowRadius = radius * shadowFraction,
            color = color,
            backgroundColor = backgroundColor
        )
        
        val arrowAngle = getAngle(
            dials = dialValues,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            currentValue = normalizedCurrentValue,
        )
        drawArrow(
            angle = arrowAngle,
            center = center,
            linearGradientColorStart = colorLight,
            linearGradientColorEnd = color
        )

        drawArrowPin(
            radius = radius,
            linearGradientColorStart = colorLight,
            linearGradientColorEnd = color
        )
        drawDials(
            textMeasurer = textMeasurer,
            dials = dialValues,
            textColor = textColor
        )
        drawSpiralProgressBar(
            angle = arrowAngle,
            startAngle = startAngle,
            sweepGradientStartColor = color,
            sweepGradientEndColor = colorLight
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
    radiusFraction: Float = 0.57f,
    widthFraction: Float = 0.05f,
    linearGradientColorStart: Color,
    linearGradientColorEnd: Color
) {
    val diameter = size.minDimension

    val width = diameter * widthFraction
    val radius = diameter * radiusFraction / 2f - width / 2f

    val linearGradientStart = pointOnCircle(
        center = center,
        radius = radius + width / 2,
        angleDegrees = 225f
    )

    val linearGradientEnd = pointOnCircle(
        center = center,
        radius = radius + width / 2,
        angleDegrees = 45f
    )

    drawCircle(
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to linearGradientColorStart,
                1.0f to linearGradientColorEnd
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
    textColor: Color,
    dials: List<Float> = listOf(
        0f, 1f, 5f, 10f, 15f, 20f, 30f, 40f, 60f, 80f, 100f
    ),
    radiusFraction: Float = 0.35f,
    fontSizeFraction: Float = 0.013f,
    emptyAngle: Float = 90f,
) {
    val sweepAngle = 360f - emptyAngle
    val startAngle = 90f + emptyAngle / 2
    val radius = size.minDimension * radiusFraction

    dials.forEachIndexed { i, n ->
        val deltaAngle = sweepAngle / (dials.lastIndex)
        val angle = startAngle + i * deltaAngle
        val pos = pointOnCircle(center, radius, angle)
        val textLayoutResult = textMeasurer.measure(
            text = n.toInt().toString(),
            style = TextStyle.Default.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.minDimension * fontSizeFraction).sp
            )
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                pos.x - textLayoutResult.size.width / 2,
                pos.y - textLayoutResult.size.height / 2
            ),
            color = textColor
        )
    }
}

private fun DrawScope.drawArrowPin(
    radius: Float,
    linearGradientColorStart: Color,
    linearGradientColorEnd: Color
) {
    drawCircle(
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to linearGradientColorStart,
                1.0f to linearGradientColorEnd
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
    shadowRadius: Float,
    color: Color,
    backgroundColor: Color
) {
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to color,
                1.0f to backgroundColor
            ),
            radius = shadowRadius
        ),
        radius = shadowRadius
    )
}

private fun DrawScope.drawArrow(
    angle: Float,
    center: Offset,
    linearGradientColorStart: Color,
    linearGradientColorEnd: Color,
    lengthFraction: Float = 0.31f,
    startWidthFraction: Float = 0.042f,
    endWidthFraction: Float = 0.008f
) {
    val length = size.minDimension * lengthFraction
    val startWidth = size.minDimension * startWidthFraction
    val endWidth = size.minDimension * endWidthFraction

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
                    0.0f to linearGradientColorStart,
                    1.0f to linearGradientColorEnd
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
    sweepGradientStartColor: Color,
    sweepGradientEndColor: Color,
    progressBarRadiusFraction: Float = 0.4f,
    pitchFraction: Float = 0.12f,
) {
    val progressBarRadius = size.minDimension * progressBarRadiusFraction

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

        var lastInnerPoint: Offset

        var lastOuterPoint = Offset(
            x = progressBarStart.x,
            y = progressBarStart.y
        )

        // https://dribbble.com/shots/26150271-Internet-Speed-Test-Mobile-App

        repeat(fullSpiralParts) { i ->
            val cubicBezier = spiralArcToBezier(
                center,
                progressBarRadius,
                size.minDimension * pitchFraction,
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
                progressBarRadius,
                size.minDimension * pitchFraction,
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
            val arc = circleParts.popOrNull() ?: break
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
            sweepGradientStartColor.toArgb(),
            sweepGradientEndColor.toArgb()
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