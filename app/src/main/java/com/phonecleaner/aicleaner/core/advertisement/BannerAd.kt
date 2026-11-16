package com.phonecleaner.aicleaner.core.advertisement

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.phonecleaner.aicleaner.R

@Composable
fun BannerAdCompose(
    bannerAd: BannerAd?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    loadingHeight: Dp = 50.dp,
    verticalInset: Dp = 0.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary
) {
    val activity = LocalActivity.current

    if (bannerAd != null && activity != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(backgroundColor)
                .padding(vertical = verticalInset),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = loadingHeight),
                factory = { context -> FrameLayout(context) },
                update = { container ->
                    container.removeAllViews()
                    @Suppress("DEPRECATION")
                    val adView = bannerAd.getView(activity)
                    (adView.parent as? ViewGroup)?.removeView(adView)
                    container.addView(
                        adView,
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    )
                }
            )
        }
    } else if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = verticalInset)
                .height(loadingHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.loading_ad),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
