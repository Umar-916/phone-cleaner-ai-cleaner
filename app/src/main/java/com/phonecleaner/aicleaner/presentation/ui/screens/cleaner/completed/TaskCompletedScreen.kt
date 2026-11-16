package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.completed

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
import androidx.compose.material.icons.rounded.BatteryChargingFull
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))

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

/** Shown after a task finishes: celebrates the result and offers the care tasks that are still open. */
@Composable
fun TaskCompletedScreen(
    summary: String,
    onSkip: () -> Unit,
    onOpenTask: (MainTask) -> Unit
) {
    val done by TaskTracker.done.collectAsState()
    val remaining = suggestions.filter { it.task !in done }
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
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onSkip), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ArrowBack, null, tint = MaterialTheme.colorScheme.tertiary)
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.task_skip),
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onSkip).padding(horizontal = 14.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Box(
                    modifier = Modifier.size(82.dp).scale(checkScale).clip(CircleShape).background(Brand.Good),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.Done, null, tint = Color.White, modifier = Modifier.size(46.dp)) }
                Spacer(Modifier.size(16.dp))
                Column {
                    Text(
                        stringResource(R.string.task_completed),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = TextStyle(fontSize = 18.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        stringResource(R.string.task_progress, done.size, MainTask.entries.size),
                        color = Brand.Good,
                        style = TextStyle(fontSize = 24.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                }
            }
            if (summary.isNotBlank()) {
                Text(
                    summary,
                    color = MaterialTheme.colorScheme.surface,
                    textAlign = TextAlign.Start,
                    style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontFamily = medium)
                )
            }

            if (remaining.isEmpty()) {
                Text(
                    stringResource(R.string.task_all_done),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
            }
            remaining.forEach { suggestion -> SuggestionCard(suggestion) { onOpenTask(suggestion.task) } }
            Spacer(Modifier.height(16.dp + getBannerOverlayHeight()))
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: Suggestion, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(suggestion.tint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) { Icon(suggestion.icon, null, tint = suggestion.tint, modifier = Modifier.size(28.dp)) }
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(stringResource(suggestion.title), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                    Text(stringResource(suggestion.body), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 13.sp, fontFamily = medium))
                }
            }
            Spacer(Modifier.height(14.dp))
            GradientButton(stringResource(suggestion.action), onClick)
        }
        Text(
            stringResource(suggestion.badge),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 14.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(suggestion.tint)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            color = Color.White,
            style = TextStyle(fontSize = 11.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
    }
}
