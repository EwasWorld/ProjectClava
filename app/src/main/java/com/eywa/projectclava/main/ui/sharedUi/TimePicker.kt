package com.eywa.projectclava.main.ui.sharedUi

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
    // TODO Work on null display - it's a bit awkward
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    fun String.parseInt() =
            try {
                if (isNullOrBlank()) 0 else Integer.parseInt(this)
            }
            catch (e: NumberFormatException) {
                0
            }

    Column {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                modifier = modifier.padding(horizontal = 10.dp)
        ) {
            NumericTextField(
                    value = minutes.takeIf { it != 0 }?.toString() ?: "",
                    placeholderText = "min",
                    isError = minutes < 0,
                    goNext = true,
                    onValueChange = { timeChangedListener(it.parseInt() * 60 + seconds) }
            )
            Text(":")
            NumericTextField(
                    value = seconds.toString().padStart(2, '0'),
                    placeholderText = "sec",
                    isError = seconds < 0,
                    goNext = false,
                    onValueChange = { timeChangedListener(it.parseInt() + minutes * 60) }
            )
        }
        if (minutes < 0 || seconds < 0) {
            Text(
                    text = "Cannot be less than 0",
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
            onValueChange = onValueChange,
            modifier = modifier.width(IntrinsicSize.Min),
            interactionSource = interactionSource,
            textStyle = Typography.body1,
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