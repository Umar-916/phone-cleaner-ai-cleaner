package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.utils.ComponentHelper.debounceClickable
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun PremiumOutlinedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(10.sdp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondary, shape)
            .border(
                1.sdp,
                Color.Black.copy(alpha = 0.2f),
                shape
            )
            .padding(horizontal = 12.sdp, vertical = 11.sdp)
    ) {
        content()
    }
}

@Composable
fun PremiumFeatureRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 12.ssp,
                fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary
            )
        )
        Image(
            painter = painterResource(R.drawable.premium_check),
            contentDescription = null,
            modifier = Modifier.size(20.sdp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun PremiumTrialSwitch(
    checked: Boolean
) {
    val trackColor = if (checked) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    } else {
        Color(0xFF666666).copy(alpha = 0.25f)
    }
    val thumbColor = if (checked) {
        MaterialTheme.colorScheme.primary
    } else {
        Color(0xFF666666)
    }

    Box(
        modifier = Modifier
            .width(35.sdp)
            .height(26.sdp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(30.sdp)
                .height(14.sdp)
                .clip(RoundedCornerShape(7.sdp))
                .background(trackColor)
        )
        Box(
            modifier = Modifier
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .size(18.sdp)
                .dropShadow(
                    CircleShape,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        radius = 3.sdp,
                        spread = 0.sdp,
                        offset = DpOffset(0.sdp, 1.sdp)
                    )
                )
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
fun PremiumPlanCard(
    isTrialEnabled: Boolean,
    weeklyPrice: String
) {
    PremiumOutlinedCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.premium_weekly_access),
                    style = TextStyle(
                        fontSize = 12.ssp,
                        fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                )
                Text(
                    text = if (weeklyPrice == "null") "…" else stringResource(R.string.premium_price_per_week_format, weeklyPrice),
                    style = TextStyle(
                        fontSize = 10.ssp,
                        fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.surface
                    )
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (isTrialEnabled) {
                    Text(
                        text = stringResource(R.string.premium_trial_status_enabled),
                        style = TextStyle(
                            fontSize = 12.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                            textAlign = TextAlign.End
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.premium_verified),
                            contentDescription = null,
                            modifier = Modifier.size(16.sdp)
                        )
                        Text(
                            text = stringResource(R.string.premium_auto_renewal),
                            modifier = Modifier.padding(start = 4.sdp),
                            style = TextStyle(
                                fontSize = 10.ssp,
                                fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.premium_auto_renewal),
                        style = TextStyle(
                            fontSize = 10.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface,
                            textAlign = TextAlign.End
                        )
                    )
                }
            }
        }
    }
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun PremiumContinueButton(
    isTrialEnabled: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "premium_arrow")
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "premium_arrow_offset"
    )
    val buttonShape = RoundedCornerShape(30.sdp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.sdp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .dropShadow(
                    buttonShape,
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                        radius = 12.sdp,
                        spread = 0.sdp,
                        offset = DpOffset(0.sdp, 4.sdp)
                    )
                )
                .clip(buttonShape)
                .background(MaterialTheme.colorScheme.primary)
                .height(54.sdp)
                .debounceClickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(
                        if (isTrialEnabled) R.string.premium_continue_for_free
                        else R.string.premium_continue
                    ),
                    style = TextStyle(
                        fontSize = 15.ssp,
                        fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                )
                Image(
                    painter = painterResource(R.drawable.premium_continue_arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = arrowOffset.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun PremiumCloseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(16.sdp)
            .size(30.sdp)
            .clip(RoundedCornerShape(12.sdp))
            .background(Color.White.copy(alpha = 0.9f))
            .debounceClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.premium_close),
            contentDescription = stringResource(R.string.close),
            modifier = Modifier.size(20.sdp),
            colorFilter = ColorFilter.tint(Color(0xFF7A2ACB).copy(alpha = 0.85f))
        )
    }
}

@Composable
fun PremiumNoPaymentFooter() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.premium_verified),
            contentDescription = null,
            modifier = Modifier.size(16.sdp)
        )
        Text(
            text = stringResource(R.string.premium_no_payment_now),
            modifier = Modifier.padding(start = 4.sdp),
            style = TextStyle(
                fontSize = 10.ssp,
                fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary
            )
        )
    }
}

@Preview(
    name = "Premium Helpers Preview",
    showBackground = true
)
@Composable
private fun PremiumHelpersPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.sdp),
        verticalArrangement = Arrangement.spacedBy(12.sdp)
    ) {
        PremiumOutlinedCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.sdp)
            ) {
                PremiumFeatureRow("Unlock all prank sounds")
                PremiumFeatureRow("Exclusive Pro sounds")
                PremiumFeatureRow("Vibration effects")
                PremiumFeatureRow("Timer & loop")
                PremiumFeatureRow("Ad-free experience")
            }
        }

        PremiumTrialSwitch(checked = true)

        PremiumPlanCard(
            isTrialEnabled = true,
            weeklyPrice = "$4.99"
        )

        PremiumContinueButton(
            isTrialEnabled = true,
            onClick = {}
        )

        PremiumNoPaymentFooter()
    }
}
