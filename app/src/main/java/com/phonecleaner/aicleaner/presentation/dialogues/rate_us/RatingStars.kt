import com.phonecleaner.aicleaner.BuildConfig
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri
import com.phonecleaner.aicleaner.R
import network.chaintech.sdpcomposemultiplatform.sdp

@Composable
fun RatingStars(
    rating: Int,
    onRatingSelected: (Int) -> Unit
) {
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.sdp)
    ) {

        repeat(5) { index ->

            val selected = index < rating

            Image(
                painter = painterResource(
                    id = if (selected)
                        R.drawable.fill_star
                    else
                        R.drawable.without_fill_star
                ),
                contentDescription = null,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember {
                            MutableInteractionSource()
                        }
                    ) {
                        onRatingSelected(index + 1)
                        if (index + 1 == 5) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "market://details?id=${BuildConfig.APPLICATION_ID}".toUri()
                            )
                            context.startActivity(intent)
                        }
                    }
            )
        }
    }
}