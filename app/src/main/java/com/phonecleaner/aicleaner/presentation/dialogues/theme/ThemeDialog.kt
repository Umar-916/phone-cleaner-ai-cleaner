package com.phonecleaner.aicleaner.presentation.dialogues.theme

import com.phonecleaner.aicleaner.core.common.tapPlainClickable
import com.phonecleaner.aicleaner.core.common.tapEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun ThemeDialog(
    show: Boolean,
    selectedTheme: String,
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit = {},
    onApply: (String) -> Unit
) {

    if (!show) return

    val context = LocalContext.current

    var tempSelection by remember(selectedTheme) {
        mutableStateOf(selectedTheme)
    }

    val themes = listOf(
        Triple(stringResource(R.string.system_default), R.drawable.default_theme, ThemeKeys.SYSTEM),
        Triple(stringResource(R.string.light), R.drawable.light_theme, ThemeKeys.LIGHT),
        Triple(stringResource(R.string.dark), R.drawable.dark_theme, ThemeKeys.DARK)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.sdp, end = 8.sdp, bottom = 4.sdp),
                shape = RoundedCornerShape(22.sdp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 14.sdp, end = 14.sdp, top = 12.sdp, bottom = 12.sdp
                    )
                ) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(42.sdp)
                            .height(3.sdp)
                            .background(
                                MaterialTheme.colorScheme.onTertiaryFixed,
                                RoundedCornerShape(50)
                            )
                    )

                    Spacer(modifier = Modifier.height(16.sdp))

                    Text(
                        text = stringResource(R.string.app_theme),
                        fontSize = 18.ssp,
                        fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(18.sdp))

                    themes.forEach {
                        ThemeItem(
                            icon = it.second,
                            title = it.first,
                            isSelected = tempSelection == it.third
                        ) {
                            tempSelection = it.third
                            onThemeSelected(it.third)
                        }
                        Spacer(modifier = Modifier.height(10.sdp))
                    }

                    Spacer(modifier = Modifier.height(14.sdp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.sdp)
                    ) {

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { tapEvent("theme_dialog_tap");
                                context.postAnalytic("theme_dialog_cancel")
                                onDismiss()
                            },
                            shape = RoundedCornerShape(22.sdp),
                            border = BorderStroke(
                                0.7.sdp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = com.phonecleaner.aicleaner.presentation.ui.theme.softCardColor()),
                            contentPadding = PaddingValues(vertical = 13.sdp)
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                fontSize = 16.ssp,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }

                        Button(
                            modifier = Modifier
                                .dropShadow(
                                    RoundedCornerShape(22.sdp),
                                    shadow = Shadow(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                        radius = 12.sdp,
                                        spread = 0.sdp,
                                        offset = DpOffset(0.sdp, 4.sdp)
                                    ),
                                )
                                .weight(1f),
                            onClick = { tapEvent("theme_dialog_tap_2");
                                context.postAnalytic("theme_dialog_apply_$tempSelection")
                                onApply(tempSelection)
                            },
                            shape = RoundedCornerShape(22.sdp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(vertical = 13.sdp)
                        ) {
                            Text(
                                text = stringResource(R.string.apply),
                                color = Color.White,
                                fontSize = 16.ssp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeItem(
    icon: Int,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.sdp)
            .clip(RoundedCornerShape(16.sdp))
            .background(MaterialTheme.colorScheme.onTertiaryFixed)
            .border(
                width = if (isSelected) 0.9.sdp else 0.13.sdp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.23f),
                shape = RoundedCornerShape(16.sdp)
            )
            .tapPlainClickable("theme_dialog_tap_1") { onClick() }
            .padding(horizontal = 14.sdp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .height(30.sdp)
                .width(30.sdp)
                .clip(RoundedCornerShape(8.sdp))
                .border(
                    width = 0.5.sdp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.sdp)
                )
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(18.sdp)
            )
        }

        Spacer(modifier = Modifier.width(12.sdp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 14.ssp,
            fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.tertiary
        )

        Box(
            modifier = Modifier
                .size(18.sdp)
                .border(
                    width = 1.9.sdp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.sdp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF4A4A4A, showSystemUi = true)
@Composable
fun ThemeDialogPreview() {
    MaterialTheme {
        ThemeDialog(
            show = true,
            selectedTheme = ThemeKeys.SYSTEM,
            onDismiss = {},
            onApply = {}
        )
    }
}
