package com.phonecleaner.aicleaner.presentation.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.animation.core.LinearEasing
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.presentation.ui.components.featheredEdges
import com.phonecleaner.aicleaner.presentation.ui.theme.OnboardingPalette
import kotlinx.coroutines.launch

private class ArtLayer(
    @DrawableRes val res: Int,
    val x: Float, val y: Float, val w: Float, val h: Float,
    val rotate: Boolean, val amount: Float, val phase: Float,
    val pivotX: Float = 0f, val pivotY: Float = 0f
)

private class OnboardingPage(
    @DrawableRes val image: Int,
    val layers: List<ArtLayer>,
    @StringRes val title: Int,
    @StringRes val body: Int
)

private val pages = listOf(
    OnboardingPage(R.drawable.onboarding_junk_base, listOf(
        ArtLayer(R.drawable.ob_junk_1, 594f, 176f, 133f, 219f, false, 7f, 0f),
        ArtLayer(R.drawable.ob_junk_2, 428f, 226f, 185f, 125f, true, 5f, 0.25f, 432f, 305f)
    ), R.string.onboarding_junk_title, R.string.onboarding_junk_body),
    OnboardingPage(R.drawable.onboarding_duplicates_base, listOf(
        ArtLayer(R.drawable.ob_duplicates_1, 490f, 146f, 245f, 309f, false, 6f, 0.5f),
        ArtLayer(R.drawable.ob_duplicates_2, 390f, 204f, 165f, 193f, true, 5f, 0f, 425f, 352f)
    ), R.string.onboarding_duplicates_title, R.string.onboarding_duplicates_body),
    OnboardingPage(R.drawable.onboarding_storage_base, listOf(
        ArtLayer(R.drawable.ob_storage_1, 468f, 156f, 289f, 279f, false, 6f, 0.3f),
        ArtLayer(R.drawable.ob_storage_2, 392f, 238f, 113f, 89f, true, 6f, 0f, 395f, 320f)
    ), R.string.onboarding_storage_title, R.string.onboarding_storage_body)
)

private val bold = FontFamily(Font(R.font.google_sans_flex_bold))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))

/** Three-page introduction shown once after the language screen: junk cleaner, duplicates, storage. */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val state = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val last = state.currentPage == pages.lastIndex
    val context = androidx.compose.ui.platform.LocalContext.current

    BackHandler(enabled = state.currentPage > 0) {
        scope.launch { state.animateScrollToPage(state.currentPage - 1) }
    }

    fun finish(event: String) {
        context.postAnalytic(event)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OnboardingPalette.top, OnboardingPalette.bottom)))
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Box(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                if (!last) {
                    Text(
                        stringResource(R.string.onboarding_skip),
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { finish("onboarding_skip") }
                            .padding(8.dp),
                        color = OnboardingPalette.purple,
                        style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                }
            }

            HorizontalPager(state = state, modifier = Modifier.weight(1f).fillMaxWidth()) { index ->
                PageContent(pages[index])
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { i ->
                    val selected = i == state.currentPage
                    val width by animateFloatAsState(if (selected) 26f else 8f, tween(250), label = "dot")
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(width.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (selected) OnboardingPalette.purple else OnboardingPalette.dot)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 24.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(29.dp))
                    .background(OnboardingPalette.purple)
                    .clickable {
                        if (last) finish("onboarding_done")
                        else scope.launch { state.animateScrollToPage(state.currentPage + 1) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(if (last) R.string.onboarding_get_started else R.string.onboarding_next),
                    color = Color.White,
                    style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage) {
    val transition = rememberInfiniteTransition(label = "onboardingFloat")
    val float by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float")

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val base = ImageBitmap.imageResource(page.image)
            val layers = page.layers.map { it to ImageBitmap.imageResource(it.res) }
            val wave by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "wave")
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(844f / 578f)
                    .graphicsLayer { translationY = float * 6.dp.toPx() }
                    .featheredEdges(0.10f)
            ) {
                val k = size.width / 844f
                drawImage(base, dstSize = IntSize(size.width.toInt(), size.height.toInt()))
                layers.forEach { (l, bmp) ->
                    val s = sin(2f * PI.toFloat() * (wave + l.phase))
                    val draw = {
                        drawImage(bmp, dstOffset = IntOffset((l.x * k).toInt(), (l.y * k).toInt()), dstSize = IntSize((l.w * k).toInt(), (l.h * k).toInt()))
                    }
                    if (l.rotate) rotate(s * l.amount, pivot = Offset(l.pivotX * k, l.pivotY * k)) { draw() }
                    else translate(top = s * l.amount * k) { draw() }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(page.title),
            textAlign = TextAlign.Center,
            color = OnboardingPalette.ink,
            style = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(page.body),
            textAlign = TextAlign.Center,
            color = OnboardingPalette.muted,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = TextStyle(fontSize = 16.sp, lineHeight = 23.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(18.dp))
    }
}
