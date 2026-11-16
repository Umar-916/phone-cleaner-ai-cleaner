package com.phonecleaner.aicleaner.presentation.ui.screens.files

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.components.FloatingSheet
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand

/** What the sheet needs to know about a file, so it works for MediaStore entries and plain paths alike. */
class SheetFile(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val modifiedMs: Long,
    val mime: String,
    val canOpen: Boolean,
    val canShare: Boolean = true
)

/** Prank-style bottom sheet with the actions available for one file. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileItemSheet(
    file: SheetFile,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDetails by remember { mutableStateOf(false) }

    FloatingSheet(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                file.name,
                maxLines = 2,
                overflow = TextOverflow.MiddleEllipsis,
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
            )
            Text(
                Formatter.formatFileSize(context, file.sizeBytes) + " · " + formatDay(context, file.modifiedMs),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
                style = TextStyle(fontSize = 13.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)))
            )

            if (showDetails) {
                DetailRow(stringResource(R.string.files_detail_path), file.path)
                DetailRow(stringResource(R.string.files_detail_size), Formatter.formatFileSize(context, file.sizeBytes))
                DetailRow(stringResource(R.string.files_detail_modified), formatDateTime(context, file.modifiedMs))
                DetailRow(stringResource(R.string.files_detail_type), file.mime.ifBlank { mimeFor(file.name) })
            } else {
                if (file.canOpen) SheetAction(Icons.Rounded.OpenInNew, stringResource(R.string.files_action_open), Brand.Blue) { onOpen(); onDismiss() }
                if (file.canShare) {
                    SheetAction(Icons.Rounded.Share, stringResource(R.string.files_action_share), Brand.Good) { onShare(); onDismiss() }
                }
                SheetAction(Icons.Rounded.Info, stringResource(R.string.files_action_details), Brand.Photo) { showDetails = true }
                SheetAction(Icons.Rounded.DeleteOutline, stringResource(R.string.files_action_delete), Brand.Danger) { onDelete(); onDismiss() }
            }
        }
    }
}

@Composable
private fun SheetAction(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.14f)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.size(14.dp))
        Text(
            label,
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 16.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)), fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 12.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium))))
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)), fontWeight = FontWeight.SemiBold)
        )
    }
}
