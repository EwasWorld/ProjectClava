package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eywa.projectclava.ui.theme.Typography

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

    Dialog(onDismissRequest = onCancelListener) {
        Surface(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                        text = title,
                        style = Typography.h4,
                )
                content()

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            }
        }
    }
}