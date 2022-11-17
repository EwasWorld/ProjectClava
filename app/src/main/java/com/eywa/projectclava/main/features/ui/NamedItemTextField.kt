package com.eywa.projectclava.main.features.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.features.ui.editNameDialog.NamedItem

@Composable
fun <T : NamedItem> NamedItemTextField(
        typeContentDescription: String,
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        proposedItemName: String,
        fieldIsDirty: Boolean,
        onValueChangedListener: (String) -> Unit,
        onClearPressedListener: () -> Unit,
        onDoneListener: () -> Unit,
        modifier: Modifier = Modifier,
        textFieldModifier: Modifier = Modifier,
        itemBeingEdited: T? = null,
) {
    val isDuplicate = nameIsDuplicate(proposedItemName, itemBeingEdited?.name)
    val isEmpty = proposedItemName.isBlank()
    val errorMessage = when {
        !fieldIsDirty -> null
        isDuplicate -> "A $typeContentDescription with the name $proposedItemName already exists"
        isEmpty -> "Cannot be empty"
        else -> null
    }
    val label: @Composable () -> Unit = { Text("Add $typeContentDescription") }
    val onDone = {
        if (errorMessage == null && !isEmpty) {
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
                    IconButton(
                            onClick = onClearPressedListener,
                            modifier = Modifier.clearAndSetSemantics { },
                    ) {
                        Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear"
                        )
                    }
                },
                isError = errorMessage != null,
                keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Words,
                ),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = textFieldModifier
                        .onKeyEvent {
                            if (it.nativeKeyEvent.keyCode != KeyEvent.KEYCODE_ENTER) return@onKeyEvent false

                            onDone()
                            true
                        }
                        .semantics {
                            errorMessage?.let {
                                error(errorMessage)
                            }
                            customActions = listOf(
                                    CustomAccessibilityAction(
                                            label = "Add player $proposedItemName",
                                            action = { onDone(); true },
                                    ),
                                    CustomAccessibilityAction(
                                            label = "Clear",
                                            action = { onClearPressedListener(); true },
                                    ),
                            )
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