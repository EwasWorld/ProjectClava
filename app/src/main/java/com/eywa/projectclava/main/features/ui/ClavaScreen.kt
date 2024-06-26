package com.eywa.projectclava.main.features.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import com.eywa.projectclava.main.common.IMissingContentNextStep
import com.eywa.projectclava.main.common.MissingContentNextStep
import com.eywa.projectclava.main.common.MissingContentNextStep.Companion.getFirstStep
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.theme.ClavaColor
import com.eywa.projectclava.main.theme.DividerThickness
import com.eywa.projectclava.main.theme.Typography

// TODO Test accessibility
/**
 * @param navigateListener only required if [missingContentNextStep] is given
 */
@SuppressLint("ModifierParameter") // It's not a modifier on the whole ClavaScreen so don't want to call it modifier
@Composable
fun ClavaScreen(
        showNoContentPlaceholder: Boolean,
        noContentText: String,
        missingContentNextStep: Iterable<IMissingContentNextStep>? = null,
        navigateListener: ((NavRoute) -> Unit)? = null,
        headerContent: @Composable (() -> Unit)? = null,
        footerContent: @Composable (() -> Unit)? = null,
        footerIsVisible: Boolean = true,
        fabs: @Composable ((Modifier) -> Unit)? = null,
        listArrangement: Arrangement.Vertical = Arrangement.spacedBy(10.dp),
        listModifier: Modifier = Modifier,
        listContent: LazyListScope.() -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
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

        Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
        ) {
            if (showNoContentPlaceholder) {
                NoContentPlaceholder(
                        noContentText = noContentText,
                        missingContentNextStep = missingContentNextStep,
                        navigateListener = navigateListener,
                )
            }
            else {
                LazyColumn(
                        verticalArrangement = listArrangement,
                        contentPadding = PaddingValues(vertical = 20.dp),
                        content = listContent,
                        modifier = listModifier.padding(horizontal = 20.dp)
                )
            }
            fabs?.invoke(
                    Modifier
                            .padding(bottom = 30.dp, start = 30.dp)
                            .align(Alignment.BottomEnd)
            )
        }

        AnimatedVisibility(
                visible = footerContent != null && footerIsVisible,
                enter = expandVertically(),
                exit = shrinkVertically(),
        ) {
            Column {
                Divider(thickness = DividerThickness)
                Surface(
                        color = ClavaColor.HeaderFooterBackground,
                        modifier = Modifier.fillMaxWidth()
                ) {
                    footerContent?.invoke()
                }
            }
        }
    }
}

@Composable
private fun NoContentPlaceholder(
        noContentText: String,
        missingContentNextStep: Iterable<IMissingContentNextStep>?,
        navigateListener: ((NavRoute) -> Unit)? = null,
) {
    val firstMissingContent = missingContentNextStep.getFirstStep()
    Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp)
    ) {
        Text(
                text = noContentText,
                style = Typography.h4,
                textAlign = TextAlign.Center,
        )
        if (firstMissingContent != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                    text = firstMissingContent.nextStepsText,
                    style = Typography.h4,
                    textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { navigateListener!!(firstMissingContent.buttonRoute) }) {
                Text(
                        text = firstMissingContent.buttonText,
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun Empty_ClavaScreen_Preview() {
    ClavaScreen(
            showNoContentPlaceholder = true,
            noContentText = "No queued matches",
            missingContentNextStep = listOf(MissingContentNextStep.ADD_PLAYERS),
            navigateListener = {},
            listContent = {},
    )
}
