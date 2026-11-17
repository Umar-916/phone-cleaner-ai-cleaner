package com.phonecleaner.aicleaner.presentation.navigation

import com.phonecleaner.aicleaner.core.common.tapClickable
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.advertisement.InterstitialAdManager
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.domain.viewmodel.SharedNativeAdVM
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.atan2
import kotlin.math.sqrt

var actualBottomHeight by mutableStateOf(0.dp)
var actualBannerOverlayHeight by mutableStateOf(0.dp)

private val sideTabs = listOf(BottomNavItem.Cleaner, BottomNavItem.Settings)

val bottomBarDestinations: Set<AppDestination> = setOf(
    BottomNavItem.Cleaner.destination,
    BottomNavItem.Files.destination,
    BottomNavItem.Settings.destination
)

private class NotchedBarShape(
    private val notchRadius: Dp,
    private val centerDrop: Dp,
    private val fillet: Dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val nr = with(density) { notchRadius.toPx() }
        val dy = with(density) { centerDrop.toPx() }
        val f = with(density) { fillet.toPx() }
        val r = size.height / 2f
        val cx = size.width / 2f

        val dx = sqrt((nr + f) * (nr + f) - (f - dy) * (f - dy))
        val shoulderSweep = Math.toDegrees(atan2((dy - f).toDouble(), dx.toDouble())).toFloat()
        val xLeft = cx - dx
        val xRight = cx + dx
        val contactAngle = 180f - Math.toDegrees(atan2((f - dy).toDouble(), dx.toDouble())).toFloat()

        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(xLeft, 0f)
            arcTo(Rect(xLeft - f, 0f, xLeft + f, 2 * f), -90f, 90f + shoulderSweep, false)
            arcTo(Rect(cx - nr, dy - nr, cx + nr, dy + nr), contactAngle, -(2f * contactAngle - 180f), false)
            arcTo(Rect(xRight - f, 0f, xRight + f, 2 * f), 180f - shoulderSweep, 90f + shoulderSweep, false)
            lineTo(size.width - r, 0f)
            arcTo(Rect(size.width - 2 * r, 0f, size.width, size.height), -90f, 180f, false)
            lineTo(r, size.height)
            arcTo(Rect(0f, 0f, 2 * r, size.height), 90f, 180f, false)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun BottomNavigationBar(
    backStack: NavBackStack<NavKey>? = null,
    modifier: Modifier = Modifier,
    sharedNativeAdVM: SharedNativeAdVM = koinViewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity),
) {
    val activity = LocalActivity.current
    val density = LocalDensity.current
    val currentDestination = backStack?.lastOrNull()

    val barHeight = 60.dp + 3.sdp
    val fabSize = 56.dp
    val fabOverBar = 24.dp
    val notchRadius = 39.dp
    val notchFillet = 30.dp
    val barShape = remember { NotchedBarShape(notchRadius, 0.dp, notchFillet) }

    fun openTab(item: BottomNavItem) {
        activity?.postAnalytic("select_tab_${item.analyticsName}")
        backStack?.clear()
        if (item.destination is AppDestination.Home) {
            backStack?.add(AppDestination.Home)
        } else {
            backStack?.addAll(listOf(AppDestination.Home, item.destination))
        }
        sharedNativeAdVM.clearAd()
    }

    fun onItemClick(item: BottomNavItem) {
        if (currentDestination == item.destination) return
        InterstitialAdManager.showInnerThen(activity = activity, screenName = "BottomNav") { openTab(item) }
    }

    val filesSelected = currentDestination == BottomNavItem.Files.destination

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val top = coordinates.positionInRoot().y
                val rootHeight = coordinates.findRootCoordinates().size.height.toFloat()
                actualBottomHeight = with(density) { (rootHeight - top).coerceAtLeast(0f).toDp() }
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = fabOverBar, bottom = 12.dp),
            shape = barShape,
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.primary, 0.10f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            val barModifier = if (isPremiumUser()) Modifier.navigationBarsPadding() else Modifier
            Box(modifier = barModifier.fillMaxWidth().height(barHeight)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(barHeight).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sideTabs.forEachIndexed { index, item ->
                        if (index == 1) Spacer(Modifier.weight(1f))
                        val selected = currentDestination == item.destination
                        val tint by animateColorAsState(
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            tween(220), label = "tabTint"
                        )
                        val container by animateColorAsState(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            tween(220), label = "tabContainer"
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(barHeight - 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(container)
                                .tapClickable("on_item_click_click") { onItemClick(item) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                            Text(
                                stringResource(item.title),
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp,
                                    fontFamily = FontFamily(Font(if (selected) R.font.google_sans_flex_semi else R.font.google_sans_flex_medium)),
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = tint
                                )
                            )
                        }
                    }
                }
                Text(
                    stringResource(BottomNavItem.Files.title),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 7.dp),
                    style = TextStyle(
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontFamily = FontFamily(Font(if (filesSelected) R.font.google_sans_flex_semi else R.font.google_sans_flex_medium)),
                        fontWeight = if (filesSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (filesSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(fabSize)
                .drawBehind {
                    val radius = size.width / 2f
                    val outer = radius + 14.dp.toPx()
                    val center = Offset(size.width / 2f, size.height / 2f + 8.dp.toPx())
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to Brand.Blue.copy(alpha = 0.24f),
                                (radius / outer) to Brand.Blue.copy(alpha = 0.16f),
                                1f to Brand.Blue.copy(alpha = 0f)
                            ),
                            center = center,
                            radius = outer
                        ),
                        radius = outer,
                        center = center
                    )
                }
                .clip(CircleShape)
                .background(Brand.Primary)
                .then(if (filesSelected) Modifier.border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape) else Modifier)
                .tapClickable("on_item_click_click_2") { onItemClick(BottomNavItem.Files) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.FolderOpen, contentDescription = stringResource(BottomNavItem.Files.title), tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun rememberBottomHeight(): State<Dp> {
    return remember { derivedStateOf { actualBottomHeight } }
}

@Composable
fun getTotalBottomHeight(): Dp {
    val heightState = rememberBottomHeight()
    val systemNavHeight = with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() }
    return if (heightState.value > 0.dp) heightState.value else 62.sdp + 10.sdp + systemNavHeight
}

@Composable
fun getBannerOverlayHeight(): Dp {
    return remember { derivedStateOf { actualBannerOverlayHeight } }.value
}
