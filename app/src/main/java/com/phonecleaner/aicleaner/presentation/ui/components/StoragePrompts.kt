package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.phonecleaner.aicleaner.R

/** Prompt for file access (All files access on Android 11+, storage permission before). */
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

/** Prompt for photo and video library access; after a denial the button leads to app settings. */
@Composable
fun MediaPermissionPrompt(
    showSettings: Boolean,
    onDismiss: () -> Unit,
    onAllow: () -> Unit
) {
    PermissionPrompt(
        icon = Icons.Rounded.PhotoLibrary,
        title = stringResource(R.string.perm_media_title),
        body = stringResource(if (showSettings) R.string.perm_media_denied_body else R.string.perm_media_body),
        bullets = listOf(
            stringResource(R.string.perm_bullet_device),
            stringResource(R.string.perm_bullet_confirm),
            stringResource(R.string.perm_bullet_revoke)
        ),
        allowLabel = stringResource(if (showSettings) R.string.perm_open_settings else R.string.perm_allow),
        notNowLabel = stringResource(R.string.perm_not_now),
        onAllow = onAllow,
        onDismiss = onDismiss
    )
}
