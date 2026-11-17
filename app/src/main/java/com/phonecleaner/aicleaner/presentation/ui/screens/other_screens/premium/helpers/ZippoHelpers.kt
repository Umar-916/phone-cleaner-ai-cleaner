package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.utils.ComponentHelper.debounceClickable
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun ZippoFeatureRow(
    iconRes: Int,
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.sdp)
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.sdp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 13.ssp,
                fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary
            )
        )
        Box(
            modifier = Modifier.width(40.sdp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Image(
                painter = painterResource(R.drawable.zippo_tick),
                contentDescription = null,
                modifier = Modifier.size(16.sdp),
                contentScale = ContentScale.Fit
            )
        }
        Box(
            modifier = Modifier.width(40.sdp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Image(
                painter = painterResource(R.drawable.zippo_dash),
                contentDescription = null,
                modifier = Modifier.size(16.sdp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun ZippoCtaButton(
    isTrialEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.sdp)
            .clip(RoundedCornerShape(30.sdp))
            .background(MaterialTheme.colorScheme.primary)
            .debounceClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(
                if (isTrialEnabled) R.string.zippo_cta_trial
                else R.string.premium_continue
            ),
            style = TextStyle(
                fontSize = 14.ssp,
                fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun ZippoLegalText(
    isTrialEnabled: Boolean,
    onPolicyClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val prefix = stringResource(R.string.zippo_legal_prefix)
    val terms = stringResource(R.string.zippo_terms)
    val and = stringResource(R.string.zippo_legal_and)
    val privacy = stringResource(R.string.zippo_privacy)
    val suffix = stringResource(R.string.zippo_legal_suffix)
    val body = stringResource(
        if (isTrialEnabled) R.string.zippo_legal_body_trial
        else R.string.zippo_legal_body_no_trial
    )
    val linkStyle = SpanStyle(
        color = primary,
        fontWeight = FontWeight.Medium,
        textDecoration = TextDecoration.None
    )
    val linkStyles = TextLinkStyles(
        style = linkStyle,
        focusedStyle = linkStyle,
        hoveredStyle = linkStyle,
        pressedStyle = linkStyle
    )
    val annotated = buildAnnotatedString {
        append(prefix)
        withLink(
            LinkAnnotation.Clickable(
                tag = "terms",
                styles = linkStyles,
                linkInteractionListener = { onPolicyClick() }
            )
        ) {
            withStyle(linkStyle) { append(" $terms ") }
        }
        append(and)
        withLink(
            LinkAnnotation.Clickable(
                tag = "privacy",
                styles = linkStyles,
                linkInteractionListener = { onPolicyClick() }
            )
        ) {
            withStyle(linkStyle) { append(" $privacy ") }
        }
        append(suffix)
        append("\n")
        append(body)
    }
    Text(
        text = annotated,
        style = TextStyle(
            fontSize = 10.ssp,
            fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center,
            lineHeight = 16.ssp
        )
    )
}
