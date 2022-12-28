package com.eywa.projectclava.main.features.ui

import android.view.KeyEvent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.ifThen
import com.eywa.projectclava.main.features.ui.NamedItemTextFieldError.*
import com.eywa.projectclava.main.features.ui.editNameDialog.NamedItem

@Composable
fun <T : NamedItem> NamedItemTextField(
        typeContentDescription: String,
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        nameIsArchived: (newName: String, editItemName: String?) -> T?,
        proposedItemName: String,
        fieldIsDirty: Boolean,
        onValueChangedListener: (String) -> Unit,
        onClearPressedListener: () -> Unit,
        onDoneListener: () -> Unit,
        onUnarchiveListener: (T) -> Unit,
        modifier: Modifier = Modifier,
        textFieldModifier: Modifier = Modifier,
        itemBeingEdited: T? = null,
) {
    val duplicateArchivedItem = nameIsArchived(proposedItemName, itemBeingEdited?.name)
    val isEmpty = proposedItemName.isBlank()
    val textFieldError = when {
        !fieldIsDirty -> null
        duplicateArchivedItem != null -> ARCHIVED
        nameIsDuplicate(proposedItemName, itemBeingEdited?.name) -> DUPLICATE
        isEmpty -> EMPTY
        else -> null
    }

    val onDone = {
        if (textFieldError == null && !isEmpty) {
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
                label = (@Composable { Text("Add $typeContentDescription") })
                        .takeIf { itemBeingEdited == null },
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
                isError = textFieldError != null,
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
                            textFieldError?.let {
                                error(textFieldError.getMessage(typeContentDescription, proposedItemName))
                            }
                            customActions = listOf(
                                    CustomAccessibilityAction(
                                            label = "Add $typeContentDescription $proposedItemName",
                                            action = { onDone(); true },
                                    ),
                                    CustomAccessibilityAction(
                                            label = "Clear",
                                            action = { onClearPressedListener(); true },
                                    ),
                            )
                        }
        )
        textFieldError?.let {
            Text(
                    text = textFieldError.getMessage(typeContentDescription, proposedItemName),
                    color = MaterialTheme.colors.error,
                    textDecoration = if (it.clickable) TextDecoration.Underline else TextDecoration.None,
                    fontWeight = if (it.clickable) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                            .padding(start = 5.dp)
                            .ifThen(
                                    textFieldError == ARCHIVED,
                                    Modifier.clickable {
                                        onUnarchiveListener(duplicateArchivedItem!!)
                                        onClearPressedListener()
                                    },
                            )
            )
        }
    }
}

private enum class NamedItemTextFieldError(val clickable: Boolean = false) {
    DUPLICATE {
        override fun getMessage(typeContentDescription: String, proposedItemName: String) =
                "A $typeContentDescription with the name $proposedItemName already exists"
    },
    ARCHIVED(clickable = true) {
        override fun getMessage(typeContentDescription: String, proposedItemName: String) =
                "$proposedItemName already exists but is archived. Click here to unarchive."
    },
    EMPTY {
        override fun getMessage(typeContentDescription: String, proposedItemName: String): String =
                "Cannot be empty"
    }
    ;

    abstract fun getMessage(
            typeContentDescription: String,
            proposedItemName: String,
    ): String
}
