package com.phonecleaner.aicleaner.presentation.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.StoragePermission
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.data.storage.FolderAccess

@Composable
fun FolderAccessPrompt(
    onDismiss: () -> Unit,
    onGranted: () -> Unit,
    accent: Color = MaterialTheme.colorScheme.primary,
    @Suppress("UNUSED_PARAMETER") hint: Uri? = null,
    body: String = stringResource(R.string.perm_storage_body)
) {
    val context = LocalContext.current
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (FolderAccess.hasAllFiles(context)) onGranted()
    }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && FolderAccess.hasAllFiles(context)) onGranted()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val legacyLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (FolderAccess.hasAllFiles(context)) onGranted()
    }
    PermissionPrompt(
        icon = Icons.Rounded.Folder,
        title = stringResource(R.string.perm_storage_title),
        body = body,
        bullets = listOf(
            stringResource(R.string.perm_bullet_device),
            stringResource(R.string.perm_bullet_confirm),
            stringResource(R.string.perm_bullet_revoke)
        ),
        allowLabel = stringResource(R.string.perm_allow),
        notNowLabel = stringResource(R.string.perm_not_now),
        onAllow = {
            if (StoragePermission.usesSettingsScreen) tryCatch { settingsLauncher.launch(StoragePermission.settingsIntent(context)) }
            else legacyLauncher.launch(StoragePermission.LEGACY_PERMISSION)
        },
        onDismiss = onDismiss,
        accent = accent
    )
}

@Composable
fun StoragePermissionPrompt(
    onDismiss: () -> Unit,
    accent: Color = MaterialTheme.colorScheme.primary,
    body: String = stringResource(R.string.perm_storage_body),
    onAllow: () -> Unit
) {
    PermissionPrompt(
        icon = Icons.Rounded.Folder,
        title = stringResource(R.string.perm_storage_title),
        body = body,
        bullets = listOf(
            stringResource(R.string.perm_bullet_device),
            stringResource(R.string.perm_bullet_confirm),
            stringResource(R.string.perm_bullet_revoke)
        ),
        allowLabel = stringResource(R.string.perm_allow),
        notNowLabel = stringResource(R.string.perm_not_now),
        onAllow = onAllow,
        onDismiss = onDismiss,
        accent = accent
    )
}

@Composable
fun OnResumeEffect(action: () -> Unit) {
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val current = androidx.compose.runtime.rememberUpdatedState(action)
    androidx.compose.runtime.DisposableEffect(owner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) current.value()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
