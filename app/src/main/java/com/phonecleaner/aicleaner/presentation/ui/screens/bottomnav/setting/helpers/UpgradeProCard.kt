package com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.setting.helpers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import androidx.compose.foundation.background
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun UpgradeProCard(
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.sdp),
        shape = RoundedCornerShape(20.sdp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Brand.BlueLight, Brand.Blue, Brand.BlueDark)))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.sdp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.sdp)
                        .shadow(
                            elevation = 3.sdp,
                            shape = RoundedCornerShape(14.sdp),
                            ambientColor = Color(0xFFFFD588),
                            spotColor = Color(0xFFFFD588)
                        )
                        .clip(RoundedCornerShape(14.sdp))
                        .background(Brand.ProBadge)
                        .border(width = 1.5.sdp, color = Color.White.copy(alpha = 0.55f), shape = RoundedCornerShape(14.sdp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.crown_icon),
                        contentDescription = null,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF4A2A00))
                    )
                }

                Spacer(modifier = Modifier.width(12.sdp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(R.string.upgrade_to_pro),
                        style = TextStyle(
                            fontSize = 16.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(2.sdp))

                    Text(
                        stringResource(R.string.get_access_of_all_premium_features),
                        style = TextStyle(
                            fontSize = 10.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Brand.ProBadge)
                        .border(width = 1.5.sdp, color = Color.White.copy(alpha = 0.55f), shape = CircleShape)
                        .padding(
                            horizontal = 16.sdp,
                            vertical = 7.sdp
                        )
                    ,
                ) {
                    Text(
                        stringResource(R.string.continue_setting),
                        style = TextStyle(
                            fontSize = 10.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A2A00)
                        )
                    )
                }
            }
        }
    }
}
