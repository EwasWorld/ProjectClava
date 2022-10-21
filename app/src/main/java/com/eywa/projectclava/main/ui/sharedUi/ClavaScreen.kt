package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.MissingContentNextStep
import com.eywa.projectclava.main.model.MissingContentNextStep.Companion.getFirstStep
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography

@Composable
fun ClavaScreen(
        noContentText: String,
        missingContentNextStep: Iterable<MissingContentNextStep>?,
        modifier: Modifier = Modifier,
        showMissingContentNextStep: Boolean = true,
        navigateListener: (NavRoute) -> Unit,
        headerContent: @Composable (() -> Unit)? = null,
        footerContent: @Composable (() -> Unit)? = null,
        listArrangement: Arrangement.Vertical = Arrangement.spacedBy(10.dp),
        listModifier: Modifier = Modifier,
        listContent: LazyListScope.() -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
    ) {
        headerContent?.let {
            Surface(
                    color = ClavaColor.HeaderFooterBackground,
                    modifier = Modifier.fillMaxWidth()
            ) {
                headerContent()
            }
            Divider(thickness = DividerThickness)
        }

        Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
        ) {
            val firstMissingContent = missingContentNextStep.getFirstStep()
            if (firstMissingContent != null) {
                // TODO Force this to always be at the same height
                Text(
                        text = noContentText,
                        style = Typography.h4,
                        textAlign = TextAlign.Center,
                )
                if (showMissingContentNextStep) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                            text = firstMissingContent.nextStepsText,
                            style = Typography.h4,
                            textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { navigateListener(firstMissingContent.buttonRoute) }) {
                        Text(
                                text = "Let's do it!",
                        )
                    }
                }
            }
            else {
                LazyColumn(
                        verticalArrangement = listArrangement,
                        contentPadding = PaddingValues(vertical = 20.dp),
                        modifier = listModifier
                                .fillMaxWidth()
                                .weight(1f)
                ) {
                    listContent()
                }
            }
        }

        footerContent?.let {
            Divider(thickness = DividerThickness)
            Surface(
                    color = ClavaColor.HeaderFooterBackground,
                    modifier = Modifier.fillMaxWidth()
            ) {
                footerContent()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun Empty_ClavaScreen_Preview() {
    ClavaScreen(
            noContentText = "No queued matches",
            missingContentNextStep = listOf(MissingContentNextStep.ADD_PLAYERS),
            navigateListener = {},
            listContent = {},
    )
}