package com.phonecleaner.aicleaner.presentation.dialogues.timer

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

private val timerSeconds = listOf(0, 5, 10, 15, 20)

@Composable
fun TimerDialog(
    show: Boolean,
    initialStartSec: Int = 0,
    initialEndSec: Int = 0,
    onDismiss: () -> Unit,
    onApply: (startSec: Int, endSec: Int) -> Unit
) {
    if (!show) return

    fun indexOfSeconds(seconds: Int): Int {
        val index = timerSeconds.indexOf(seconds)
        return if (index >= 0) index else 0
    }

    var isStartTime by remember(show) { mutableStateOf(true) }
    var startIndex by remember(show) { mutableIntStateOf(indexOfSeconds(initialStartSec)) }
    var endIndex by remember(show) { mutableIntStateOf(indexOfSeconds(initialEndSec)) }

    val currentIndex = if (isStartTime) startIndex else endIndex

    val options = listOf(
        stringResource(R.string.off),
        stringResource(R.string.timer_5),
        stringResource(R.string.timer_10),
        stringResource(R.string.timer_15),
        stringResource(R.string.timer_20)
    )

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        context.postAnalytic("timer_dialog_appear")
    }

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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
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
                        text = stringResource(R.string.timer),
                        fontSize = 18.ssp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.sdp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.sdp))
                            .background(MaterialTheme.colorScheme.onTertiaryFixed)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.sdp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.onTertiaryFixed
                            ),
                            border = BorderStroke(
                                0.8.sdp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.sdp))
                                        .background(
                                            if (isStartTime) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onTertiaryFixed
                                        )
                                        .clickable {
                                            isStartTime = true
                                            context.postAnalytic("timer_start_time_toggle")
                                        }
                                        .padding(vertical = 10.sdp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.start_time),
                                        color = if (isStartTime) Color.White
                                        else MaterialTheme.colorScheme.outline,
                                        fontSize = 14.ssp,
                                        fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.sdp))
                                        .background(
                                            if (!isStartTime) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onTertiaryFixed
                                        )
                                        .clickable {
                                            isStartTime = false
                                            context.postAnalytic("timer_end_time_toggle")
                                        }
                                        .padding(vertical = 10.sdp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.end_time),
                                        color = if (!isStartTime) Color.White
                                        else MaterialTheme.colorScheme.outline,
                                        fontSize = 14.ssp,
                                        fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.sdp))

                    options.forEachIndexed { index, item ->
                        TimerItem(
                            title = item,
                            isSelected = currentIndex == index
                        ) {
                            if (isStartTime) startIndex = index else endIndex = index
                            context.postAnalytic(
                                "timer_option_click_${
                                    item.replace(" ", "_").lowercase()
                                }"
                            )
                        }
                        Spacer(modifier = Modifier.height(10.sdp))
                    }

                    Spacer(modifier = Modifier.height(16.sdp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.sdp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                context.postAnalytic("timer_dialog_cancel")
                                onDismiss()
                            },
                            shape = RoundedCornerShape(22.sdp),
                            border = BorderStroke(
                                0.7.sdp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.onTertiaryFixed
                            ),
                            contentPadding = PaddingValues(vertical = 12.sdp)
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(vertical = 12.sdp),
                            onClick = {
                                context.postAnalytic("timer_dialog_apply_start_${timerSeconds[startIndex]}_end_${timerSeconds[endIndex]}")
                                onApply(timerSeconds[startIndex], timerSeconds[endIndex])
                            }
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
private fun TimerItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.sdp)
            .clip(RoundedCornerShape(16.sdp))
            .background(MaterialTheme.colorScheme.onTertiaryFixed)
            .border(
                width = if (isSelected) 1.sdp else 0.6.sdp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.sdp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 16.sdp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.tertiary,
            fontSize = 15.ssp,
            fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
            fontWeight = FontWeight.Medium
        )

        Box(
            modifier = Modifier
                .size(22.sdp)
                .border(
                    width = 2.sdp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(10.sdp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}
