package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.apps

import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.core.common.tapPlainClickable
import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapClickable
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.domain.AppsViewModel
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppIcon
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.RoundCheck
import com.phonecleaner.aicleaner.presentation.ui.theme.softCardColor
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val bold = AppFonts.Bold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsManagerScreen(
    onBack: () -> Unit,
    viewModel: AppsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var selected by remember { mutableStateOf(setOf<String>()) }
    var queue by remember { mutableStateOf(listOf<String>()) }

    val apps = state.apps
    val present = remember(apps) { apps.map { it.packageName }.toSet() }
    val chosen = selected intersect present

    val uninstallLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        queue = queue.drop(1)
    }
    LaunchedEffect(queue) {
        val next = queue.firstOrNull()
        if (next != null) {
            val intent = Intent(Intent.ACTION_DELETE, "package:$next".toUri()).putExtra(Intent.EXTRA_RETURN_RESULT, true)
            runCatching { uninstallLauncher.launch(intent) }.onFailure { queue = queue.drop(1) }
        } else if (selected.isNotEmpty()) {
            viewModel.refresh()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        var firstResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (firstResume) firstResume = false else viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val selecting = chosen.isNotEmpty()
    BackHandler(enabled = selecting) { selected = emptySet() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (selecting) {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.apps_selected_count, chosen.size),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        Box(Modifier.padding(start = 8.dp).size(44.dp).clip(CircleShape).tapClickable("apps_manager_screen_click") { selected = emptySet() }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.tertiary)
                        }
                    },
                    actions = {
                        Box(
                            Modifier.padding(end = 8.dp).size(44.dp).tapPlainClickable("apps_manager_screen_tap_1") { selected = if (chosen.size == apps.size) emptySet() else present },
                            contentAlignment = Alignment.Center
                        ) { RoundCheck(chosen.size == apps.size && apps.isNotEmpty()) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            } else {
                AppTopBar(title = { AppTopBarTitle(stringResource(R.string.feature_apps_manager)) }, onBack = onBack)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                    apps.isEmpty() -> EmptyApps()
                    else -> LazyColumn(contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp + getBannerOverlayHeight())) {
                        items(apps, key = { it.packageName }) { app ->
                            val isSelected = app.packageName in chosen
                            val bg by animateColorAsState(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                tween(200), label = "rowBg"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .tapClickable("apps_manager_screen_click_2") {
                                        context.postAnalytic("apps_item_click")
                                        selected = if (isSelected) chosen - app.packageName else chosen + app.packageName
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIcon(app.packageName, Modifier.size(46.dp))
                                Spacer(Modifier.size(16.dp))
                                Text(
                                    app.label,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                                )
                                RoundCheck(isSelected)
                            }
                        }
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = selecting,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically(tween(260)) { it } + fadeIn(tween(260)),
                    exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200))
                ) {
                    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 12.dp + getBannerOverlayHeight())) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(27.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .tapClickable("apps_manager_screen_click_3") {
                                    context.postAnalytic("apps_uninstall_click")
                                    queue = chosen.toList()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.apps_action_uninstall),
                                color = Color.White,
                                style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyApps() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.SearchOff, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.apps_empty),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
    }
}
