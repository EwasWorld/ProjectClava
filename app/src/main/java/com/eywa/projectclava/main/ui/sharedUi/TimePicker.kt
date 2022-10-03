package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.ui.theme.Typography
import kotlin.math.roundToInt

@Composable
fun TimePicker(
        totalSeconds: Int,
        timeChangedListener: (Int) -> Unit,
        modifier: Modifier = Modifier,
) {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
            modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
    ) {
        NumericTextField(
                value = minutes.toString(),
                placeholderText = "min",
                onValueChange = { timeChangedListener(Integer.parseInt(it) * 60 + seconds) }
        )
        Text(":")
        NumericTextField(
                value = seconds.toString().padStart(2, '0'),
                placeholderText = "sec",
                onValueChange = { timeChangedListener(Integer.parseInt(it) + minutes * 60) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NumericTextField(
        value: String,
        onValueChange: (String) -> Unit,
        placeholderText: String?,
        modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.width(IntrinsicSize.Min),
            interactionSource = interactionSource,
            textStyle = Typography.body1,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                    onDone = {
//                                        keyboardController?.hide()
                        // TODO Keyboard actions?
                    }
            ),
    ) { innerTextField ->
        TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = value,
                innerTextField = innerTextField,
                contentPadding = PaddingValues(10.dp),
                interactionSource = interactionSource,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                placeholder = placeholderText?.let {
                    {
                        Text(text = placeholderText)
                    }
                },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimePicker_Preview() {
    TimePicker(
            totalSeconds = (60 * 15.5).roundToInt(),
            timeChangedListener = {},
    )
}