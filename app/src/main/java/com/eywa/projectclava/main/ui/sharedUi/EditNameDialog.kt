package com.eywa.projectclava.main.ui.sharedUi

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

interface NamedItem {
    val name: String
}

@Composable
fun <T : NamedItem> EditNameDialog(
        typeContentDescription: String,
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        editDialogOpenFor: T?,
        itemEditedListener: (T, String) -> Unit,
        itemEditCancelledListener: () -> Unit,
) {
    val editName = rememberSaveable(editDialogOpenFor) { mutableStateOf(editDialogOpenFor?.name ?: "") }
    val fieldTouched = rememberSaveable(editDialogOpenFor) { mutableStateOf(false) }
    val isDuplicate = nameIsDuplicate(editName.value, editDialogOpenFor?.name)
    val okListener = { itemEditedListener(editDialogOpenFor!!, editName.value.trim()) }

    ClavaDialog(
            isShown = editDialogOpenFor != null,
            title = "Edit ${editDialogOpenFor?.name}",
            okButtonText = "Edit",
            okButtonEnabled = !isDuplicate && editName.value.isNotBlank(),
            onCancelListener = itemEditCancelledListener,
            onOkListener = okListener
    ) {
        NamedItemTextField(
                typeContentDescription = typeContentDescription,
                textPlaceholder = textPlaceholder,
                nameIsDuplicate = nameIsDuplicate,
                proposedItemName = editName.value,
                onValueChangedListener = {
                    fieldTouched.value = true
                    editName.value = it
                },
                onClearPressedListener = {
                    fieldTouched.value = false
                    editName.value = ""
                },
                showBlankError = fieldTouched.value,
                onDoneListener = okListener,
                itemBeingEdited = editDialogOpenFor,
        )
    }
}


@Composable
fun <T : NamedItem> NamedItemTextField(
        typeContentDescription: String,
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        proposedItemName: String,
        showBlankError: Boolean,
        onValueChangedListener: (String) -> Unit,
        onClearPressedListener: () -> Unit,
        onDoneListener: () -> Unit,
        modifier: Modifier = Modifier,
        textFieldModifier: Modifier = Modifier,
        itemBeingEdited: T? = null,
) {
    val isDuplicate = nameIsDuplicate(proposedItemName, itemBeingEdited?.name)
    val errorMessage = when {
        isDuplicate -> "A $typeContentDescription with already exists"
        showBlankError && proposedItemName.isBlank() -> "Cannot be empty"
        else -> null
    }
    val label: @Composable () -> Unit = { Text("Add $typeContentDescription") }
    val onDone = {
        if (errorMessage == null && proposedItemName.isNotBlank()) {
            onDoneListener()
        }
        else {
            // Force dirty state
            onValueChangedListener(proposedItemName)
        }
    }

    Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = modifier
    ) {
        // TODO Use a basic text field to get around the weird top padding?
        OutlinedTextField(
                value = proposedItemName,
                onValueChange = onValueChangedListener,
                label = label.takeIf { itemBeingEdited == null },
                placeholder = { Text(textPlaceholder) },
                trailingIcon = {
                    IconButton(onClick = onClearPressedListener) {
                        Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear"
                        )
                    }
                },
                isError = isDuplicate,
                keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Words,
                ),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = textFieldModifier.onKeyEvent {
                    if (it.nativeKeyEvent.keyCode != KeyEvent.KEYCODE_ENTER) return@onKeyEvent false

                    onDone()
                    true
                }
        )
        errorMessage?.let {
            Text(
                    text = errorMessage,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}