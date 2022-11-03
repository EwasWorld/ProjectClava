package com.eywa.projectclava.main.features.ui.timePicker

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.theme.Typography
import kotlin.math.roundToInt

@Composable
fun TimePicker(
        timePickerState: TimePickerState,
        timeChangedListener: (TimePickerState) -> Unit,
        modifier: Modifier = Modifier,
        showError: Boolean = true,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(horizontal = 10.dp)
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        ) {
            NumericTextField(
                    value = timePickerState.minutes,
                    label = "min",
                    placeholderText = timePickerState.initialMinutes,
                    isError = (timePickerState.minutesError ?: timePickerState.generalError) != null,
                    goNext = true,
                    onValueChange = { timeChangedListener(timePickerState.copy(minutes = it, minutesIsDirty = true)) },
            )
            Text(":")
            NumericTextField(
                    value = timePickerState.seconds,
                    label = "sec",
                    placeholderText = timePickerState.initialSeconds,
                    isError = (timePickerState.secondsError ?: timePickerState.generalError) != null,
                    goNext = false,
                    onValueChange = { timeChangedListener(timePickerState.copy(seconds = it, secondsIsDirty = true)) },
            )
        }
        if (showError && timePickerState.error != null) {
            Text(
                    text = timePickerState.error!!,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}

/**
 * @param goNext true if the keyboard's done action should be NEXT instead of DONE
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun NumericTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String?,
        placeholderText: String?,
        isError: Boolean,
        modifier: Modifier = Modifier,
        goNext: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.trim().filter { char -> char.isDigit() }) },
            modifier = modifier.width(50.dp),
            interactionSource = interactionSource,
            textStyle = Typography.body1.copy(textAlign = TextAlign.Center),
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (goNext) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                    onDone = { if (!goNext) keyboardController?.hide() },
                    onNext = { focusManager.moveFocus(FocusDirection.Right) }
            ),
    ) { innerTextField ->
        TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = value,
                innerTextField = innerTextField,
                contentPadding = PaddingValues(10.dp),
                interactionSource = interactionSource,
                enabled = true,
                singleLine = true,
                isError = isError,
                visualTransformation = VisualTransformation.None,
                label = label?.let {
                    {
                        Text(
                                text = label,
                        )
                    }
                },
                placeholder = placeholderText?.let {
                    {
                        Text(
                                text = placeholderText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimePicker_Preview() {
    TimePicker(
            timePickerState = TimePickerState((60 * 15.5).roundToInt()),
            timeChangedListener = {},
    )
}

@Preview(showBackground = true)
@Composable
fun Error_TimePicker_Preview() {
    TimePicker(
            timePickerState = TimePickerState(
                    minutes = "-5",
                    seconds = "00"
            ),
            timeChangedListener = {},
    )
}