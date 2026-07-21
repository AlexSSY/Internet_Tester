package rx.dagger.internet.tester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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

    Canvas(
        modifier = Modifier
            .size(480.dp)
    ) {
        drawRect(
            color = Color(0xfff3f5f9)
        )

        val radius = 52f
        val shadowRadius = radius * 1.4f

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

        val arrowOptions = ArrowDrawOptions(
            length = 280f,
            startWidth = 42f,
            endWidth = 8f
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

        val progressBarRadius = 380f
        val progressBarStart = pointOnCircle(
            center = center,
            radius = progressBarRadius,
            angleDegrees = startAngle
        )

        val progressBarMin = 0f
        val progressBarMax = 100

        val progressBarPath = Path().apply {
            moveTo(
                x = progressBarStart.x,
                y = progressBarStart.y
            )

            // https://dribbble.com/shots/26150271-Internet-Speed-Test-Mobile-App

            cubicTo(
                x1 = TODO(),
                y1 = TODO(),
                x2 = TODO(),
                y2 = TODO(),
                x3 = TODO(),
                y3 = TODO()
            )
        }
    }
}