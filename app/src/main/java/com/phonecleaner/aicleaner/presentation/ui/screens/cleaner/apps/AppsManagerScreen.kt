package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.apps

import android.content.Intent
import android.provider.Settings
import android.text.format.DateFormat
import android.text.format.Formatter
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.components.SegmentedTabs
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.domain.AppsViewModel
import com.phonecleaner.aicleaner.domain.model.AppFilter
import com.phonecleaner.aicleaner.domain.model.AppSort
import com.phonecleaner.aicleaner.domain.model.InstalledApp
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppIcon
import com.phonecleaner.aicleaner.presentation.ui.components.FloatingSheet
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import java.util.Date

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsManagerScreen(
    onBack: () -> Unit,
    viewModel: AppsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var selected by remember { mutableStateOf<InstalledApp?>(null) }

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = { AppTopBarTitle(stringResource(R.string.feature_apps_manager)) },
                onBack = onBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                placeholder = { Text(stringResource(R.string.apps_search_hint)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.secondary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                    focusedTextColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedTextColor = MaterialTheme.colorScheme.tertiary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.surface,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.surface,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            val filters = listOf(AppFilter.USER, AppFilter.SYSTEM, AppFilter.ALL)
            val filterLabels = listOf(
                if (state.isLoading) stringResource(R.string.apps_filter_user_plain) else stringResource(R.string.apps_filter_user, state.userCount),
                if (state.isLoading) stringResource(R.string.apps_filter_system_plain) else stringResource(R.string.apps_filter_system, state.systemCount),
                if (state.isLoading) stringResource(R.string.apps_filter_all_plain) else stringResource(R.string.apps_filter_all, state.totalCount)
            )
            SegmentedTabs(
                options = filterLabels,
                selectedIndex = filters.indexOf(state.filter),
                onSelect = { viewModel.setFilter(filters[it]) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            val sorts = AppSort.entries.toList()
            SegmentedTabs(
                options = listOf(stringResource(R.string.apps_sort_name), stringResource(R.string.apps_sort_size), stringResource(R.string.apps_sort_recent)),
                selectedIndex = sorts.indexOf(state.sort),
                onSelect = { viewModel.setSort(sorts[it]) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )

                    state.apps.isEmpty() -> EmptyApps()
                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + getBannerOverlayHeight()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.apps, key = { it.packageName }) { app ->
                            AppRow(app) {
                                context.postAnalytic("apps_item_click")
                                selected = app
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { app ->
        FloatingSheet(onDismiss = { selected = null }) {
            AppDetailsSheet(app = app, onDone = { selected = null })
        }
    }
}

@Composable
private fun AppRow(app: InstalledApp, onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(app.packageName, Modifier.size(46.dp))
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                app.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
            Text(
                app.packageName,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 12.sp, fontFamily = medium)
            )
        }
        Text(
            Formatter.formatShortFileSize(context, app.apkSizeBytes),
            color = MaterialTheme.colorScheme.primary,
            style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
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

@Composable
private fun AppDetailsSheet(app: InstalledApp, onDone: () -> Unit) {
    val context = LocalContext.current
    val installed = remember(app.installedAt) {
        DateFormat.getMediumDateFormat(context).format(Date(app.installedAt))
    }

    Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIcon(app.packageName, Modifier.size(72.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            app.label,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Text(
            app.packageName,
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 12.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailRow(stringResource(R.string.apps_detail_version), app.versionName.ifBlank { "-" })
            DetailRow(stringResource(R.string.apps_detail_size), Formatter.formatFileSize(context, app.apkSizeBytes))
            DetailRow(stringResource(R.string.apps_detail_installed), installed)
            DetailRow(
                stringResource(R.string.apps_detail_type),
                stringResource(if (app.isSystem) R.string.apps_type_system else R.string.apps_type_user)
            )
        }
        Spacer(Modifier.height(16.dp))

        if (app.canLaunch) {
            ActionButton(Icons.Rounded.OpenInNew, stringResource(R.string.apps_action_open), filled = true) {
                context.packageManager.getLaunchIntentForPackage(app.packageName)?.let { intent ->
                    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                }
                onDone()
            }
            Spacer(Modifier.height(10.dp))
        }
        ActionButton(Icons.Rounded.Settings, stringResource(R.string.apps_action_settings), filled = false) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${app.packageName}".toUri())
            tryCatch { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            onDone()
        }
        if (!app.isSystem) {
            Spacer(Modifier.height(10.dp))
            ActionButton(Icons.Rounded.Delete, stringResource(R.string.apps_action_uninstall), filled = false, danger = true) {
                context.postAnalytic("apps_uninstall_click")
                val intent = Intent(Intent.ACTION_DELETE, "package:${app.packageName}".toUri())
                runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                onDone()
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            color = MaterialTheme.colorScheme.surface,
            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
        )
        Text(
            value,
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    filled: Boolean,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val accent = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(if (filled) accent else accent.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (filled) Color.White else accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(8.dp))
        Text(
            text,
            color = if (filled) Color.White else accent,
            style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
    }
}

