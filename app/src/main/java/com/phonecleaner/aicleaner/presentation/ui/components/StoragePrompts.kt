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
import com.phonecleaner.aicleaner.data.storage.FolderAccess

@Composable
fun FolderAccessPrompt(
    onDismiss: () -> Unit,
    onGranted: () -> Unit,
    accent: Color = MaterialTheme.colorScheme.primary,
    hint: Uri? = FolderAccess.mediaHint(),
    body: String = stringResource(R.string.folder_body)
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            FolderAccess.persist(context, uri)
            onGranted()
        }
    }
    PermissionPrompt(
        icon = Icons.Rounded.Folder,
        title = stringResource(R.string.folder_title),
        body = body,
        bullets = listOf(
            stringResource(R.string.folder_bullet_only),
            stringResource(R.string.folder_bullet_local),
            stringResource(R.string.perm_bullet_revoke)
        ),
        allowLabel = stringResource(R.string.folder_choose),
        notNowLabel = stringResource(R.string.perm_not_now),
        onAllow = { picker.launch(hint) },
        onDismiss = onDismiss,
        accent = accent
    )
}
