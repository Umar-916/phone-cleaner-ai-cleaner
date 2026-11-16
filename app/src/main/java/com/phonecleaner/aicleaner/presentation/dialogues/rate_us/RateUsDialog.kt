package com.phonecleaner.aicleaner.presentation.dialogues.rate_us

import RatingStars
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.presentation.ui.theme.colorWhite
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun RateUsDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Int) -> Unit
) {

    if (!show) return

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        context.postAnalytic("rate_us_dialog_appear")
    }

    var rating by remember {
        mutableIntStateOf(0)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 8.sdp,
                        end = 8.sdp,
                        bottom = 4.sdp
                    )
            ) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 55.sdp),
                    shape = RoundedCornerShape(22.sdp),
                    color = MaterialTheme.colorScheme.secondary,
                    shadowElevation = 2.sdp
                ) {

                    Box {

                        Image(
                            painter = painterResource(R.drawable.rate_us_close),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(
                                    top = 18.sdp,
                                    end = 18.sdp
                                )
                                .clickable(
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    },
                                    indication = null
                                ) {
                                    context.postAnalytic("rate_us_cancel")
                                    onDismiss()
                                }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 76.sdp,
                                    start = 12.sdp,
                                    end = 12.sdp,
                                    bottom = 18.sdp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = stringResource(R.string.rate_us),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.ssp,
                                fontFamily = FontFamily(
                                    Font(R.font.google_sans_flex_bold)
                                ),
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(10.sdp))

                            Text(
                                text = stringResource(R.string.your_opinion_matters_to_us),
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 16.ssp,
                                fontFamily = FontFamily(
                                    Font(R.font.google_sans_flex_semi)
                                )
                            )

                            Spacer(Modifier.height(8.sdp))

                            Text(
                                text = stringResource(R.string.we_work_super_hard_to_make_application),
                                color = MaterialTheme.colorScheme.surface,
                                fontSize = 12.ssp,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily(
                                    Font(R.font.google_sans_flex_medium)
                                )
                            )

                            Spacer(Modifier.height(22.sdp))

                            RatingStars(
                                rating = rating,
                                onRatingSelected = {
                                    rating = it
                                    context.postAnalytic("rate_us_star_selected_$it")
                                    if (it == 5) {
                                        context.postAnalytic("rate_us_submit_$it")
                                        onSubmit(rating)
                                        onDismiss()
                                    }
                                }
                            )

                            Spacer(Modifier.height(18.sdp))

                            Text(
                                text = stringResource(R.string.thank_you_for_rate_us),
                                color = MaterialTheme.colorScheme.surface,
                                fontSize = 12.ssp,
                                fontFamily = FontFamily(
                                    Font(R.font.google_sans_flex_medium)
                                )
                            )

                            Spacer(Modifier.height(24.sdp))

                            Button(
                                modifier = Modifier
                                    .dropShadow(
                                        RoundedCornerShape(40.sdp),
                                        shadow = Shadow(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                            radius = 12.sdp,
                                            spread = 0.sdp,
                                            offset = DpOffset(0.sdp, 4.sdp)
                                        ),
                                    )
                                    .fillMaxWidth()
                                    .height(50.sdp),
                                enabled = rating > 0,
                                onClick = {
                                    context.postAnalytic("rate_us_submit_$rating")
                                    onSubmit(rating)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(40.sdp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.submit),
                                    color = colorWhite,
                                    fontSize = 16.ssp,
                                    fontFamily = FontFamily(
                                        Font(R.font.google_sans_flex_semi)
                                    )
                                )
                            }

                        }

                    }

                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(110.sdp)
                        .shadow(
                            elevation = 10.sdp,
                            shape = CircleShape
                        )
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Image(
                        painter = painterResource(R.drawable.rate_us_icon),
                        contentDescription = null
                    )

                }

            }

        }

    }

}

@Preview(showBackground = true)
@Composable
fun RateUsPreview() {

    MaterialTheme {

        RateUsDialog(
            show = true,
            onDismiss = {},
            onSubmit = {}
        )

    }

}