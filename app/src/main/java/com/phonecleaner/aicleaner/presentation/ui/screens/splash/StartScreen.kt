package com.phonecleaner.aicleaner.presentation.ui.screens.splash

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.toOffset
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.toArgb
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.phonecleaner.aicleaner.R
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.phonecleaner.aicleaner.core.advertisement.AppOpenAdManager
import com.phonecleaner.aicleaner.core.analytics.AnalyticsEvents
import com.phonecleaner.aicleaner.core.common.HideSystemBars
import com.phonecleaner.aicleaner.core.common.debug
import com.phonecleaner.aicleaner.core.common.isAppInDarkTheme
import com.phonecleaner.aicleaner.core.common.isNetworkAvailable
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.utils.PostAnalytic
import com.phonecleaner.aicleaner.presentation.ui.screens.activities.MainActivity
import com.phonecleaner.aicleaner.presentation.ui.theme.textColorGray
import com.phonecleaner.aicleaner.core.remote.SplashDesign
import com.phonecleaner.aicleaner.domain.viewmodel.SplashVM
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StartScreen(
    splashVM: SplashVM = koinViewModel(),
    onNavigate: () -> Unit

) {

    val activity = LocalActivity.current

    val context = LocalContext.current

    val isLoadingComplete by splashVM.isLoadingComplete.collectAsStateWithLifecycle()
    val shouldExitApp by splashVM.shouldExitApp.collectAsStateWithLifecycle()
    val splashDesign by splashVM.splashDesign.collectAsStateWithLifecycle()

    val splashCount by splashVM.splashCount.collectAsStateWithLifecycle()
    val dayAnalyticsEvent by splashVM.dayAnalyticsEvent.collectAsStateWithLifecycle()

    PostAnalytic(AnalyticsEvents.SPLASH_SCREEN_DISPLAY)

    LaunchedEffect(splashCount) {
        if (splashCount > 0) {
            if (context.isNetworkAvailable()) context.postAnalytic(AnalyticsEvents.SESSION_START_WITH_INTERNET_ON)
            context.postAnalytic("splash_count_$splashCount")
            "App opened $splashCount times".debug("SplashScreen")
        }
    }

    LaunchedEffect(dayAnalyticsEvent) {
        dayAnalyticsEvent?.let { context.postAnalytic(it) }
    }

    LaunchedEffect(activity) {
        splashVM.initializeAppAndSetupProgress(activity)
    }

    LaunchedEffect(Unit) {

        AppOpenAdManager.canLoadAppOpen = false

        splashVM.forceUpdateEvent.collect {
            (activity as? MainActivity)?.forceUpdate()
        }
    }

    LaunchedEffect(shouldExitApp) {
        if (shouldExitApp) {
            splashVM.exitApp(activity)
        }
    }

    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(isLoadingComplete) {
        if (isLoadingComplete && !hasNavigated) {
            hasNavigated = true
            splashVM.goToNextScreen(onNavigate)
        }
    }

    BackHandler(enabled = true) {}

    HideSystemBars()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    )
    { innerPadding ->

        val isDark = isAppInDarkTheme()

        SplashDesignSwitcher(
            design = splashDesign,
            isDark = isDark,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }

}

@Composable
private fun SplashDesignSwitcher(
    design: SplashDesign,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val reveal = remember { Animatable(if (design == SplashDesign.New) 1f else 0f) }
    var containerPos by remember { mutableStateOf(Offset.Zero) }
    var logoCenter by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(design) {
        when (design) {
            SplashDesign.New -> if (reveal.value < 1f) {
                reveal.animateTo(1f, tween(REVEAL_DURATION_MS, easing = REVEAL_EASING))
            }
            SplashDesign.Classic -> reveal.snapTo(0f)
        }
    }

    val glowColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.onGloballyPositioned { containerPos = it.positionInWindow() }
    ) {

        if (design == SplashDesign.Classic || reveal.value < 1f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 1f - 0.6f * smoothStep(0.3f, 1f, reveal.value) }
            ) {
                ClassicSplashContent(
                    isDark = isDark,
                    logoScale = { 1f + 0.10f * sin(PI.toFloat() * (reveal.value / 0.6f).coerceIn(0f, 1f)) },
                    onLogoPositioned = { logoCenter = it }
                )
            }
        }

        if (design == SplashDesign.New) {
            NewSplashContent(
                modifier = Modifier
                    .graphicsLayer {
                        val p = reveal.value
                        if (p < 1f) {
                            compositingStrategy = CompositingStrategy.Offscreen
                            alpha = smoothStep(0.1f, 0.55f, p)
                            val s = 1.03f - 0.03f * p
                            scaleX = s
                            scaleY = s
                        }
                    }
                    .drawWithContent {
                        drawContent()
                        val p = spread(((reveal.value - 0.12f) / 0.88f).coerceIn(0f, 1f))
                        if (reveal.value < 1f) {
                            val center = logoCenter?.minus(containerPos) ?: this.center
                            val reach = maxRevealRadius(center, size.width, size.height)
                            val radius = (reach / (1f - REVEAL_FEATHER) * p).coerceAtLeast(1f)
                            drawRect(
                                brush = Brush.radialGradient(
                                    0f to Color.Black,
                                    (1f - REVEAL_FEATHER) to Color.Black,
                                    1f to Color.Transparent,
                                    center = center,
                                    radius = radius
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                    }
            )
        }

        if (design == SplashDesign.New && reveal.value in 0.001f..0.999f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val p = reveal.value
                        val center = logoCenter?.minus(containerPos) ?: this.center
                        val reach = maxRevealRadius(center, size.width, size.height)
                        val travel = spread(p)
                        val mid = size.minDimension * 0.08f + reach * travel
                        val halfWidth = size.minDimension * 0.06f + mid * 0.45f
                        val outer = mid + halfWidth
                        val inner = (mid - halfWidth).coerceAtLeast(0f)
                        val strength = 0.40f * (1f - smoothStep(0.35f, 1f, p)) * smoothStep(0f, 0.12f, p)
                        drawCircle(
                            brush = Brush.radialGradient(
                                (inner / outer) to Color.Transparent,
                                (mid / outer) to glowColor.copy(alpha = strength),
                                1f to Color.Transparent,
                                center = center,
                                radius = outer
                            ),
                            radius = outer,
                            center = center
                        )
                    }
            )
        }
    }
}

private const val REVEAL_DURATION_MS = 2800

private const val REVEAL_FEATHER = 0.55f
private val REVEAL_EASING = LinearEasing

private fun spread(t: Float): Float = 1f - (1f - t).pow(1.8f)

private fun smoothStep(from: Float, to: Float, value: Float): Float {
    val t = ((value - from) / (to - from)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun maxRevealRadius(center: Offset, width: Float, height: Float): Float {
    val dx = max(center.x, width - center.x)
    val dy = max(center.y, height - center.y)
    return hypot(dx, dy)
}

@Composable
private fun ClassicSplashContent(
    isDark: Boolean,
    logoScale: () -> Float = { 1f },
    onLogoPositioned: (Offset) -> Unit = {}
) {
    SplashBody(logoScale = logoScale, onLogoPositioned = onLogoPositioned)
}

@Composable
fun LottieSplashLoader(
    modifier: Modifier = Modifier,
    animationRes: Int = R.raw.new_loading
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val tint = MaterialTheme.colorScheme.primary.toArgb()
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.COLOR, tint, "**"),
        rememberLottieDynamicProperty(LottieProperty.STROKE_COLOR, tint, "**")
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        dynamicProperties = dynamicProperties,
        modifier = modifier
            .width(108.sdp)
            .height(108.sdp),
        contentScale = ContentScale.FillWidth
    )
}
