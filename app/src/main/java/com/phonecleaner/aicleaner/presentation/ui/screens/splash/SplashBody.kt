package com.phonecleaner.aicleaner.presentation.ui.screens.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import kotlin.math.min

/**
 * Splash content: the logo with the app name and tagline, a soft background that hints at cleaning
 * (faint bubbles and sparkles), and the loader with its status text at the bottom.
 */
@Composable
fun SplashBody(
    logoScale: () -> Float = { 1f },
    onLogoPositioned: (Offset) -> Unit = {}
) {
    val background = MaterialTheme.colorScheme.background
    val dark = background.red < 0.3f
    val tintTop = if (dark) Color(0xFF0E2A55) else Color(0xFFDCEBFF)

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(background, tintTop)))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val bubble = Brand.Blue.copy(alpha = if (dark) 0.10f else 0.07f)
            drawCircle(bubble, radius = w * 0.34f, center = Offset(w * 0.04f, h * 0.88f))
            drawCircle(bubble, radius = w * 0.24f, center = Offset(w * 0.98f, h * 0.18f))
            drawCircle(bubble, radius = w * 0.12f, center = Offset(w * 0.86f, h * 0.76f))
            drawCircle(bubble, radius = w * 0.07f, center = Offset(w * 0.12f, h * 0.22f))

            fun sparkle(cx: Float, cy: Float, r: Float, color: Color) {
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    quadraticTo(cx, cy, cx + r, cy)
                    quadraticTo(cx, cy, cx, cy + r)
                    quadraticTo(cx, cy, cx - r, cy)
                    quadraticTo(cx, cy, cx, cy - r)
                    close()
                }
                drawPath(path, color)
            }
            val sparkleColor = Brand.Blue.copy(alpha = if (dark) 0.28f else 0.20f)
            sparkle(w * 0.20f, h * 0.34f, min(w, h) * 0.030f, sparkleColor)
            sparkle(w * 0.80f, h * 0.44f, min(w, h) * 0.022f, sparkleColor)
            sparkle(w * 0.70f, h * 0.30f, min(w, h) * 0.016f, Brand.Gold.copy(alpha = 0.45f))
            sparkle(w * 0.14f, h * 0.52f, min(w, h) * 0.018f, Brand.Gold.copy(alpha = 0.40f))
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.splash_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(148.dp)
                    .onGloballyPositioned { onLogoPositioned(it.positionInWindow() + it.size.center.toOffset()) }
                    .graphicsLayer {
                        val s = logoScale()
                        scaleX = s
                        scaleY = s
                    }
            )
            Text(
                text = stringResource(R.string.app_name_short),
                modifier = Modifier.padding(top = 28.dp),
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 30.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
            )
            Text(
                text = stringResource(R.string.splash_tagline),
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)), fontWeight = FontWeight.Medium)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((-34).dp)
        ) {
            Text(
                text = stringResource(R.string.starting_up),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 12.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)), fontWeight = FontWeight.Medium)
            )
            LottieSplashLoader()
        }
    }
}
