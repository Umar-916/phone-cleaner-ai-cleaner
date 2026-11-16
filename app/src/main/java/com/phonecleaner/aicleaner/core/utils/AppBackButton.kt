package com.phonecleaner.aicleaner.core.utils

import androidx.compose.foundation.Image
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.isAppInDarkTheme

@Composable
fun AppBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val isDark = isAppInDarkTheme()

    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {

        Image(
            painter = painterResource(
                id = if (isDark)
                    R.drawable.back_image_dark
                else
                    R.drawable.back_press_icon
            ),
            contentDescription = stringResource(R.string.back)
        )
    }
}