package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.process

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.domain.BoostState
import com.phonecleaner.aicleaner.domain.MainTask
import com.phonecleaner.aicleaner.domain.ProcessViewModel
import com.phonecleaner.aicleaner.presentation.ui.components.StopProcessSheet
import com.phonecleaner.aicleaner.presentation.ui.components.TaskDoneCheck
import com.phonecleaner.aicleaner.presentation.ui.components.doneLabelFor
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProcessManagerScreen(
    onBack: () -> Unit,
    onTaskCompleted: (String) -> Unit = {},
    viewModel: ProcessViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var started by rememberSaveable { mutableStateOf(false) }
    var stopSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!started) {
            started = true
            context.postAnalytic("process_boost_click")
            viewModel.boost()
        }
    }

    val running = state !is BoostState.Done
    BackHandler(enabled = running) { stopSheet = true }
    if (stopSheet && running) {
        StopProcessSheet(
            onCancel = { stopSheet = false; viewModel.reset(); onBack() },
            onDismiss = { stopSheet = false }
        )
    }

    when (val current = state) {
        is BoostState.Done -> {
            val freed = current.result
            val summary = if (freed.freedBytes > 0) {
                stringResource(R.string.process_done_summary, Formatter.formatFileSize(context, freed.freedBytes), freed.appsCount)
            } else {
                stringResource(R.string.process_done_none, freed.appsCount)
            }
            TaskDoneCheck(label = doneLabelFor(main = MainTask.PROCESS)) {
                onTaskCompleted(summary)
                viewModel.reset()
            }
        }
        is BoostState.Running -> BoostRunningView(current.percent, current.label)
        BoostState.Idle -> BoostRunningView(0, "")
    }
}
