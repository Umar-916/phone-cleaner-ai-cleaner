package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import com.phonecleaner.aicleaner.core.common.isAppInDarkTheme
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.utils.AppBackButton
import com.phonecleaner.aicleaner.core.utils.ComponentHelper.debounceClickable
import com.phonecleaner.aicleaner.presentation.ui.theme.colorPrimaryBoth
import com.phonecleaner.aicleaner.presentation.ui.theme.premIconColor
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

enum class AppTopBarTitleStyle {
    MainTab,
    Detail,
    DetailSemi,
    Song
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        modifier = modifier,
        title = title,
        navigationIcon = {
            if (onBack != null) {
                AppBackButton(onClick = onBack)
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun AppTopBarTitle(
    text: String,
    style: AppTopBarTitleStyle = AppTopBarTitleStyle.MainTab
) {
    val fontSize: TextUnit
    val font: Font
    val weight: FontWeight

    when (style) {
        AppTopBarTitleStyle.MainTab -> {
            fontSize = 24.ssp
            font = Font(R.font.google_sans_flex_bold)
            weight = FontWeight.Bold
        }

        AppTopBarTitleStyle.Detail -> {
            fontSize = 20.ssp
            font = Font(R.font.google_sans_flex_bold)
            weight = FontWeight.Bold
        }

        AppTopBarTitleStyle.DetailSemi -> {
            fontSize = 20.ssp
            font = Font(R.font.google_sans_flex_semi)
            weight = FontWeight.SemiBold
        }

        AppTopBarTitleStyle.Song -> {
            fontSize = 18.ssp
            font = Font(R.font.google_sans_flex_semi)
            weight = FontWeight.SemiBold
        }
    }

    Text(
        text = text,
        style = TextStyle(
            fontSize = fontSize,
            fontFamily = FontFamily(font),
            fontWeight = weight,
            color = MaterialTheme.colorScheme.tertiary
        )
    )
}

@Composable
fun PremiumTopBarAction(
    onClick: () -> Unit
) {
    if (!isPremiumUser()) {
        val pillShape = RoundedCornerShape(19.sdp)
        val premDescription = stringResource(id = R.string.prem_description)
        Row(
            modifier = Modifier
                .padding(end = 6.sdp)
                .width(62.sdp)
                .height(28.sdp)
                .clip(pillShape)
                .background(Brush.verticalGradient(listOf(Brand.Gold.copy(alpha = 0.55f), Brand.GoldDeep.copy(alpha = 0.45f))))
                .border(1.sdp, Brand.Gold.copy(alpha = 0.85f), pillShape)
                .semantics { contentDescription = premDescription }
                .debounceClickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.sdp, Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .size(22.sdp)
                    .clip(CircleShape)
                    .background(Brand.Gold.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.home_pro_crown),
                    contentDescription = null,
                    modifier = Modifier.size(14.sdp)
                )
            }
            Text(
                text = stringResource(R.string.pro),
                style = TextStyle(
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun ShareTopBarAction(
    onClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    Image(
        painter = painterResource(
            id = if (isDark) R.drawable.share_image_icon_dark else R.drawable.share_icon
        ),
        contentDescription = null,
        modifier = Modifier
            .padding(end = 6.sdp)
            .debounceClickable(onClick = onClick)
    )
}
