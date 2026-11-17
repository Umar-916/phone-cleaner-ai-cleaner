package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.feedback

import com.phonecleaner.aicleaner.core.common.tapEvent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.getSafeString
import com.phonecleaner.aicleaner.core.common.keyboardPadding
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.showToast
import com.phonecleaner.aicleaner.domain.FeedbackViewModel
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitleStyle
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.feedback.helpers.DescriptionField
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.feedback.helpers.FeedbackChip
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.feedback.helpers.FeedbackToggle
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.feedback.helpers.ReportBanner
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit = {},
    feedbackViewModel: FeedbackViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val activity = LocalActivity.current

    val selectedTab by feedbackViewModel.selectedTab.collectAsStateWithLifecycle()
    val description by feedbackViewModel.description.collectAsStateWithLifecycle()
    val selectedItems by feedbackViewModel.selectedItems.collectAsStateWithLifecycle()
    val showMaxSelectionToast by feedbackViewModel.showMaxSelectionToast.collectAsStateWithLifecycle()
    val showReportBanner by feedbackViewModel.showReportBanner.collectAsStateWithLifecycle()

    val issuesItems = listOf(
        stringResource(R.string.bug_crashes),
        stringResource(R.string.bug_page_not_loading),
        stringResource(R.string.bug_app_not_responding),
        stringResource(R.string.bug_function_disable),
        stringResource(R.string.bug_multiple_ads),
        stringResource(R.string.bug_premium_not_working),
        stringResource(R.string.bug_dont_know_how_to_use),
        stringResource(R.string.bug_others)
    )

    val suggestionItems = listOf(
        stringResource(R.string.add_new_feature),
        stringResource(R.string.change_interface),
        stringResource(R.string.add_multiple_themes),
        stringResource(R.string.create_dark_mode),
        stringResource(R.string.change_app_color),
        stringResource(R.string.improve_quality),
        stringResource(R.string.optimize_performance),
        stringResource(R.string.others)
    )

    val currentItems = if (selectedTab == 0) issuesItems else suggestionItems

    LaunchedEffect(showMaxSelectionToast) {
        if (showMaxSelectionToast) {

            context.showToast(
                context.getSafeString(R.string.maximum_3_selections_allowed)
            )

            feedbackViewModel.onMaxSelectionToastShown()
        }
    }

    LaunchedEffect(Unit) {
        context.postAnalytic("feedback_screen_appear")
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = {
                    AppTopBarTitle(
                        text = stringResource(R.string.feedback),
                        style = AppTopBarTitleStyle.DetailSemi
                    )
                },
                onBack = onBackClick
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 10.sdp)
                .keyboardPadding()
        ) {

            Spacer(modifier = Modifier.height(6.sdp))

            if (showReportBanner) {
                ReportBanner(
                    onClose = feedbackViewModel::closeReportBanner
                )
            }

            Spacer(modifier = Modifier.height(10.sdp))

            FeedbackToggle(
                selectedTab = selectedTab,
                onTabSelected = feedbackViewModel::onTabSelected
            )

            Spacer(modifier = Modifier.height(14.dp))

            DescriptionField(
                value = description,
                onValueChange = feedbackViewModel::onDescriptionChange, selectedTab = selectedTab
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.select_item),
                color = MaterialTheme.colorScheme.surface,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentItems) { item ->
                    FeedbackChip(
                        text = item,
                        selected = item in selectedItems
                    ) {
                        context.postAnalytic(
                            "feedback_chip_click_${
                                item.replace(" ", "_").lowercase()
                            }"
                        )
                        feedbackViewModel.onItemSelected(item)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { tapEvent("feedback_screen_tap");
                    context.postAnalytic("feedback_submit_tab_${if (selectedTab == 0) "issues" else "suggestions"}")
                    activity?.let { currentActivity ->

                        feedbackViewModel.prepareFeedbackEmail()?.let { email ->
                            feedbackViewModel.sendFeedBack(activity = currentActivity, email.body)
                        }
                    }
                },
                enabled = selectedItems.isNotEmpty(),
                modifier = Modifier
                    .then(
                        if (selectedItems.isNotEmpty()) {
                            Modifier.dropShadow(
                                RoundedCornerShape(30.sdp),
                                shadow = Shadow(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                    radius = 12.sdp,
                                    spread = 0.sdp,
                                    offset = DpOffset(0.sdp, 4.sdp)
                                ),
                            )
                        } else {
                            Modifier
                        }
                    )
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.sdp))
                    .height(52.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
            ) {
                Text(
                    stringResource(R.string.submit),
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
fun FeedbackScreenPreview() {
    MaterialTheme {
        FeedbackScreen(onBackClick = {})
    }
}
