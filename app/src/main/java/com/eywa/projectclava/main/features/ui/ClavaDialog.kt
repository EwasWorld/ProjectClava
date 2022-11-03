package com.eywa.projectclava.main.features.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.eywa.projectclava.main.theme.Typography

private enum class ButtonStatus { ENABLED, DISABLED, GONE }

@Composable
fun ClavaDialog(
        isShown: Boolean,
        title: String,
        okButtonText: String,
        okButtonEnabled: Boolean = true,
        onCancelListener: () -> Unit,
        onOkListener: () -> Unit,
        content: @Composable ColumnScope.() -> Unit
) = ClavaDialog(
        isShown = isShown,
        title = title,
        okButtonText = okButtonText,
        okButtonStatus = if (okButtonEnabled) ButtonStatus.ENABLED else ButtonStatus.DISABLED,
        onCancelListener = onCancelListener,
        onOkListener = onOkListener,
        content = content,
)

@Composable
fun ClavaDialog(
        isShown: Boolean,
        title: String,
        onCancelListener: () -> Unit,
        content: @Composable ColumnScope.() -> Unit
) = ClavaDialog(
        isShown = isShown,
        title = title,
        okButtonText = "",
        okButtonStatus = ButtonStatus.GONE,
        onCancelListener = onCancelListener,
        onOkListener = { },
        content = content,
)


@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ClavaDialog(
        isShown: Boolean,
        title: String,
        okButtonText: String,
        okButtonStatus: ButtonStatus = ButtonStatus.ENABLED,
        onCancelListener: () -> Unit,
        onOkListener: () -> Unit,
        content: @Composable ColumnScope.() -> Unit
) {
    if (!isShown) return

    /*
     * Cannot use a normal Dialog because the lazy column that's sometimes used in content
     *      pushes the buttons off the bottom for reasons unknown
     */
    AlertDialog(
            shape = RoundedCornerShape(28.dp),
            onDismissRequest = onCancelListener,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                            text = title,
                            style = Typography.h4,
                    )
                    content()
                }
            },
            buttons = {
                Row(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                            onClick = onCancelListener,
                    ) {
                        Text("Cancel")
                    }
                    if (okButtonStatus != ButtonStatus.GONE) {
                        Button(
                                enabled = okButtonStatus == ButtonStatus.ENABLED,
                                onClick = {
                                    onOkListener()
                                    onCancelListener()
                                },
                        ) {
                            Text(okButtonText)
                        }
                    }
                }
            },
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
                    .sizeIn(
                            minWidth = 280.dp,
                            maxWidth = 560.dp,
                            maxHeight = 560.dp,
                    )
    )
}