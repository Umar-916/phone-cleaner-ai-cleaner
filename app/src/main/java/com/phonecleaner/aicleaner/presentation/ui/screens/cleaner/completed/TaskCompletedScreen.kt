package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.completed

import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapClickable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.phonecleaner.aicleaner.core.utils.AppBackButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.domain.MainTask
import com.phonecleaner.aicleaner.domain.TaskTracker
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.GradientButton
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import com.phonecleaner.aicleaner.presentation.ui.components.featheredEdges

private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val bold = AppFonts.Bold

enum class OtherFeature { AI, WHATSAPP, DUPLICATES }

private fun OtherFeature.toExtra(): com.phonecleaner.aicleaner.domain.ExtraTask = when (this) {
    OtherFeature.AI -> com.phonecleaner.aicleaner.domain.ExtraTask.AI
    OtherFeature.WHATSAPP -> com.phonecleaner.aicleaner.domain.ExtraTask.WHATSAPP
    OtherFeature.DUPLICATES -> com.phonecleaner.aicleaner.domain.ExtraTask.DUPLICATES
}

private class Other(
    val feature: OtherFeature,
    val title: Int,
    val body: Int,
    val action: Int,
    val icon: ImageVector,
    val tint: Color,
    val recommended: Boolean = false
)

private val others = listOf(
    Other(OtherFeature.AI, R.string.feature_ai_cleanup, R.string.hint_ai, R.string.task_try, Icons.Rounded.AutoAwesome, Brand.Ai, recommended = true),
    Other(OtherFeature.WHATSAPP, R.string.feature_whatsapp, R.string.hint_whatsapp, R.string.task_try, Icons.Rounded.Forum, Brand.WhatsApp),
    Other(OtherFeature.DUPLICATES, R.string.feature_duplicates, R.string.hint_duplicates, R.string.home_clean, Icons.Rounded.ContentCopy, Brand.Duplicates)
)

private class Suggestion(
    val task: MainTask,
    val title: Int,
    val body: Int,
    val badge: Int,
    val action: Int,
    val icon: ImageVector,
    val tint: Color
)

private val suggestions = listOf(
    Suggestion(MainTask.JUNK, R.string.feature_junk_cleaner, R.string.task_junk_body, R.string.task_badge_space, R.string.home_clean, Icons.Rounded.CleaningServices, Brand.Junk),
    Suggestion(MainTask.PROCESS, R.string.feature_process_manager, R.string.task_process_body, R.string.task_badge_speed, R.string.task_go, Icons.Rounded.RocketLaunch, Brand.Process),
    Suggestion(MainTask.BATTERY, R.string.feature_battery, R.string.task_battery_body, R.string.task_badge_power, R.string.task_turn_on, Icons.Rounded.BatteryChargingFull, Brand.Battery)
)

@Composable
fun TaskCompletedScreen(
    summary: String,
    onSkip: () -> Unit,
    onOpenTask: (MainTask) -> Unit,
    onOpenOther: (OtherFeature) -> Unit = {}
) {
    val done by TaskTracker.done.collectAsState()
    val doneExtras by TaskTracker.extras.collectAsState()
    val hooray = MainTask.PROCESS in done && MainTask.BATTERY in done
    val remainingOthers = others.filter { it.feature.toExtra() !in doneExtras }
    val remaining = if (hooray) emptyList() else suggestions.filter { it.task !in done }
    var shown by remember { mutableStateOf(false) }
    val checkScale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.2f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "checkScale"
    )
    LaunchedEffect(Unit) { shown = true }
    BackHandler { onSkip() }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            AppBackButton(onClick = onSkip)
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.task_skip),
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).tapClickable("task_completed_screen_click_2", onClick = onSkip).padding(horizontal = 14.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (hooray) HoorayHeader() else Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Box(
                    modifier = Modifier.size(68.dp).scale(checkScale).clip(CircleShape).background(Brand.Good),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.Done, null, tint = Color.White, modifier = Modifier.size(40.dp)) }
                Spacer(Modifier.size(16.dp))
                Column {
                    Text(
                        stringResource(R.string.task_completed),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        stringResource(R.string.task_progress, done.size, MainTask.entries.size),
                        color = Brand.Good,
                        style = TextStyle(fontSize = 24.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                }
            }
            if (!hooray && summary.isNotBlank()) {
                Text(
                    summary,
                    color = MaterialTheme.colorScheme.surface,
                    textAlign = TextAlign.Start,
                    style = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontFamily = medium)
                )
            }

            if (hooray) {
                remainingOthers.forEach { o ->
                    TaskCard(
                        icon = o.icon, tint = o.tint, title = stringResource(o.title), body = stringResource(o.body),
                        badge = if (o.recommended) stringResource(R.string.task_recommended) else null,
                        badgeColor = Color(0xFFFF4D67), badgeText = Color.White,
                        action = stringResource(o.action)
                    ) { onOpenOther(o.feature) }
                }
            }
            remaining.forEach { suggestion -> SuggestionCard(suggestion) { onOpenTask(suggestion.task) } }
            Spacer(Modifier.height(16.dp + getBannerOverlayHeight()))
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: Suggestion, onClick: () -> Unit) {
    TaskCard(
        icon = suggestion.icon, tint = suggestion.tint, title = stringResource(suggestion.title), body = stringResource(suggestion.body),
        badge = stringResource(suggestion.badge),
        badgeColor = if (suggestion.task == MainTask.BATTERY) Color(0xFFFFD6AA) else Color(0xFFB8F0CB), badgeText = Color(0xFF250B3B),
        action = stringResource(suggestion.action), onClick = onClick
    )
}

@Composable
private fun TaskCard(
    icon: ImageVector, tint: Color, title: String, body: String, badge: String?,
    badgeColor: Color, badgeText: Color, action: String, onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, shape, ambientColor = Color(0x337A2ACB), spotColor = Color(0x337A2ACB))
            .clip(shape)
            .background(com.phonecleaner.aicleaner.presentation.ui.theme.softCardColor())
            .tapClickable("task_card_click", onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.size(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, modifier = Modifier.padding(end = if (badge != null) 78.dp else 0.dp), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 15.sp, lineHeight = 19.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                    Text(body, color = Color(0xFF8B6BC0), style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontFamily = medium))
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(19.dp)).background(Color(0xFF7425CF)),
                contentAlignment = Alignment.Center
            ) {
                Text(action, color = Color.White, style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
            }
        }
        if (badge != null) Text(
            badge,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomStart = 14.dp))
                .background(badgeColor)
                .padding(horizontal = 14.dp, vertical = 5.dp),
            color = badgeText,
            style = TextStyle(fontSize = 11.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun HoorayHeader() {
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "hooray")
    val wave by t.animateFloat(-1f, 1f, androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(1200), androidx.compose.animation.core.RepeatMode.Reverse), label = "wave")
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.files_mascot),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier.size(92.dp).graphicsLayer { translationY = wave * 3.dp.toPx(); rotationZ = wave * 2f }
        )
        Spacer(Modifier.size(10.dp))
        Column {
            Text(
                stringResource(R.string.task_hooray),
                modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = 14.dp, vertical = 7.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = TextStyle(fontSize = 15.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.task_completed), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
        }
    }
}
