package com.eywa.projectclava.main.ui.sharedUi

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
import com.eywa.projectclava.ui.theme.Typography

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ClavaDialog(
        isShown: Boolean,
        title: String,
        okButtonText: String,
        okButtonEnabled: Boolean = true,
        onCancelListener: () -> Unit,
        onOkListener: () -> Unit,
        content: @Composable () -> Unit
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
            title =
            {
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
                    Button(
                            enabled = okButtonEnabled,
                            onClick = {
                                onOkListener()
                                onCancelListener()
                            },
                    ) {
                        Text(okButtonText)
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