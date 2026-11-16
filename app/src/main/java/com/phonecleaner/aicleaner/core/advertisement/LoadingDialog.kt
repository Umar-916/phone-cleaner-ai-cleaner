package com.phonecleaner.aicleaner.core.advertisement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.debugRelease


@Composable
fun LoadingDialog(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.loading),
    onDismiss: () -> Unit = {}
) {

    "dialog".debugRelease("inter_ad_dialog")

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.new_loading))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    val strokeColorProperty = rememberLottieDynamicProperty(
        property = LottieProperty.STROKE_COLOR,
        value = MaterialTheme.colorScheme.tertiary.toArgb(),
        keyPath = arrayOf("**")
    )

    val dynamicProperties = rememberLottieDynamicProperties(
        strokeColorProperty
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = modifier
                .wrapContentSize()
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(top = 18.dp, bottom = 18.dp)
                .padding(horizontal = 24.dp),
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(80.dp),
                    dynamicProperties = dynamicProperties
                )

                Text(
                    modifier = Modifier,
                    text = text,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }
        }
    }
}