package com.phonecleaner.aicleaner.core.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.tapped

/** Round back button that follows the theme: a soft surface circle with a themed arrow, in light and dark. */
@Composable
fun AppBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick.tapped("back_click"),
        modifier = modifier.size(44.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.back),
            modifier = Modifier.size(24.dp)
        )
    }
}
