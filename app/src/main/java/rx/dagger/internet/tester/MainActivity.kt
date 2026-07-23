package rx.dagger.internet.tester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import rx.dagger.internet.tester.ui.theme.InternetTesterTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InternetTesterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    SpeedMeter()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    InternetTesterTheme {
//        Greeting("Android")
//    }
//}

data class CubicBezier(
    val p0: Offset,
    val p1: Offset,
    val p2: Offset,
    val p3: Offset
)

fun arcToBezier(
    center: Offset,
    radius: Float,
    startAngleDeg: Float,
    endAngleDeg: Float
): CubicBezier {

    val a0 = Math.toRadians(startAngleDeg.toDouble())
    val a1 = Math.toRadians(endAngleDeg.toDouble())

    val theta = a1 - a0
    val k = (4.0 / 3.0 * tan(theta / 4.0)).toFloat()

    fun point(a: Double) = Offset(
        center.x + radius * cos(a).toFloat(),
        center.y + radius * sin(a).toFloat()
    )

    fun tangent(a: Double) = Offset(
        -sin(a).toFloat(),
        cos(a).toFloat()
    )

    val p0 = point(a0)
    val p3 = point(a1)

    val t0 = tangent(a0)
    val t1 = tangent(a1)

    val p1 = p0 + t0 * (radius * k)
    val p2 = p3 - t1 * (radius * k)

    return CubicBezier(p0, p1, p2, p3)
}

fun spiralArcToBezier(
    center: Offset,
    startRadius: Float,
    pitch: Float,
    rotationDeg: Float,
    startSweepDeg: Float,
    endSweepDeg: Float
): CubicBezier {

    val k = pitch / (2f * PI.toFloat())

    val rotation = Math.toRadians(rotationDeg.toDouble()).toFloat()

    val t0 = Math.toRadians(startSweepDeg.toDouble()).toFloat()
    val t1 = Math.toRadians(endSweepDeg.toDouble()).toFloat()

    val h = t1 - t0

    fun radius(t: Float) =
        startRadius + k * t

    fun point(t: Float): Offset {
        val angle = rotation + t
        val r = radius(t)

        return Offset(
            center.x + r * cos(angle),
            center.y + r * sin(angle)
        )
    }

    fun derivative(t: Float): Offset {
        val angle = rotation + t
        val r = radius(t)

        return Offset(
            k * cos(angle) - r * sin(angle),
            k * sin(angle) + r * cos(angle)
        )
    }

    val p0 = point(t0)
    val p3 = point(t1)

    val d0 = derivative(t0)
    val d1 = derivative(t1)

    val p1 = p0 + d0 * (h / 3f)
    val p2 = p3 - d1 * (h / 3f)

    return CubicBezier(
        p0 = p0,
        p1 = p1,
        p2 = p2,
        p3 = p3
    )
}

data class ArrowDrawOptions(
    val length: Float,
    val startWidth: Float,
    val endWidth: Float
)

data class BackgroundCircleOptions(
    val radius: Float,
    val width: Float
)

fun pointOnCircle(
    center: Offset,
    radius: Float,
    angleDegrees: Float
): Offset {
    val angle = Math.toRadians(angleDegrees.toDouble())

    return Offset(
        x = center.x + radius * cos(angle).toFloat(),
        y = center.y + radius * sin(angle).toFloat()
    )
}

data class NumbersOptions(
    val radius: Float,
    val emptyAngle: Float,
    val steps: List<Float>
)

fun kappa(angleDegrees: Float): Float {
    val theta = angleDegrees * PI / 180.0
    return (4.0 / 3.0 * tan(theta / 4.0)).toFloat()
}

@Preview
@Composable
fun SpeedMeter() {
    val textMeasurer = rememberTextMeasurer()

    val infiniteTransition = rememberInfiniteTransition()
    val currentValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 10000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

//    val currentValue = 3.4f

    Canvas(
        modifier = Modifier
            .size(480.dp)
    ) {
        drawRect(
            color = Color(0xfff3f5f9)
        )

        val radius = 42f
        val shadowRadius = radius * 1.4f

        val center = size.center

        val backgroundCircleOptions = BackgroundCircleOptions(
            radius = 240f,
            width = 32f
        )

        val linearGradientStart = pointOnCircle(
            center = center,
            radius = backgroundCircleOptions.radius +
                backgroundCircleOptions.width / 2,
            angleDegrees = 180 + 45f
        )

        val linearGradientEnd = pointOnCircle(
            center = center,
            radius = backgroundCircleOptions.radius +
                    backgroundCircleOptions.width / 2,
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
            radius = backgroundCircleOptions.radius,
            style = Stroke(width = backgroundCircleOptions.width)
        )

        val numbersOptions = NumbersOptions(
            radius = 320f,
            emptyAngle = 90f,
            steps = listOf(
                0f, 1f, 5f, 10f, 15f, 20f, 30f, 40f, 60f, 80f, 100f
            )
        )

        val sweepAngle = 360f - numbersOptions.emptyAngle
        val startAngle = 90f + numbersOptions.emptyAngle / 2

        numbersOptions.steps.forEachIndexed { i, n ->
            val deltaAngle = sweepAngle / (numbersOptions.steps.lastIndex)
            val angle = startAngle + i * deltaAngle
            val pos = pointOnCircle(center, numbersOptions.radius, angle)
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

        val progressBarRadius = 380f
        val progressBarStart = pointOnCircle(
            center = center,
            radius = progressBarRadius,
            angleDegrees = startAngle
        )

        val arrowOptions = ArrowDrawOptions(
            length = 280f,
            startWidth = 42f,
            endWidth = 8f
        )

        val minValue = numbersOptions.steps.min()
        val maxValue = numbersOptions.steps.max()

        val angle = getAngle(
            numbersOptions.steps,
            startAngle,
            sweepAngle,
            currentValue
        )

        val arrow = Path().apply {
            moveTo(
                x = center.x,
                y = center.y
            )

            lineTo(
                x = center.x,
                y = center.y - arrowOptions.startWidth / 2
            )

            lineTo(
                x = center.x + arrowOptions.length,
                y = center.y - arrowOptions.endWidth / 2
            )

            lineTo(
                x = center.x + arrowOptions.length,
                y = center.y + arrowOptions.endWidth / 2
            )

            lineTo(
                x = center.x,
                y = center.y + arrowOptions.startWidth / 2
            )
        }

        rotate(
            degrees = angle,
            pivot = center
        ) {
            drawPath(
                path = arrow,
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xff3491e6),
                        1.0f to Color(0xff185ef6)
                    ),
                    start = Offset(
                        x = center.x + arrowOptions.length,
                        y = center.y
                    ),
                    end = Offset(
                        x = center.x,
                        y = center.y
                    )
                )
            )
        }

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

        val progressBarMin = 0f
        val progressBarMax = 100

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

        val shader = android.graphics.SweepGradient(
            center.x,
            center.y,
            intArrayOf(
                Color(0xff1650f5).toArgb(),
                Color(0xff1592f5).toArgb()
            ),
            null
        )

        val matrix = android.graphics.Matrix().apply {
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
}

fun getAngle(
    dials: List<Float>,
    startAngle: Float,
    sweepAngle: Float,
    currentValue: Float
): Float {
    val deltaAngle = sweepAngle / (dials.lastIndex)
    val exactIndex = dials.indexOf(currentValue)

    if (exactIndex >= 0) {
        return startAngle + exactIndex * deltaAngle
    }

    val fromStep = dials.last { currentValue > it }
    val toStep = dials.first { it > fromStep }

    val fromStepIndex = dials.indexOf(fromStep)
    val toStepIndex = dials.indexOf(toStep)

    val fromStepAngle = startAngle + fromStepIndex * deltaAngle
    val toStepAngle = startAngle + toStepIndex * deltaAngle

    val betweenLen = toStep - fromStep

    return fromStepAngle + ((currentValue - fromStep) / betweenLen) * deltaAngle
}

fun Offset.length(): Float =
    sqrt(x * x + y * y)


fun Offset.normalize(): Offset {
    val len = length()
    return if (len == 0f) Offset.Zero else this * (1f / len)
}

operator fun Offset.plus(other: Offset) =
    Offset(x + other.x, y + other.y)

operator fun Offset.minus(other: Offset) =
    Offset(x - other.x, y - other.y)

operator fun Offset.times(scale: Float) =
    Offset(x * scale, y * scale)

operator fun Float.times(offset: Offset) =
    Offset(offset.x * this, offset.y * this)

fun <T> MutableList<T>.popOrNull(): T? {
    if (this.isEmpty()) return null
    return this.removeAt(this.lastIndex)
}

fun <T> MutableList<T>.pop(): T {
    if (this.isEmpty()) {
        throw NoSuchElementException("List is empty.")
    }
    return this.removeAt(this.lastIndex)
}