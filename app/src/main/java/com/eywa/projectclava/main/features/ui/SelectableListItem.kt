package com.eywa.projectclava.main.features.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.asColor
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.TimeRemaining
import com.eywa.projectclava.main.theme.ClavaColor

/**
 * @param match used to decide the color of the component
 * @param enabled changes the colour of the component to a disabled color and ignores [match]
 */
@Composable
fun SelectableListItem(
        getTimeRemaining: Match.() -> TimeRemaining? = { null },
        enabled: Boolean = true,
        match: Match? = null,
        isSelected: Boolean = false,
        contentDescription: String,
        onClickActionLabel: String? = null,
        onClick: (() -> Unit)? = null,
        actions: List<CustomAccessibilityAction>? = null,
        content: @Composable () -> Unit
) {
    val colour = when {
        enabled -> match?.asColor(getTimeRemaining) ?: ClavaColor.ItemBackground
        else -> ClavaColor.DisabledItemBackground
    }
    val selectableModifier = onClick?.let {
        Modifier.selectable(selected = isSelected, onClick = onClick)
    } ?: Modifier

    Surface(
            shape = RoundedCornerShape(5.dp),
            color = colour,
            border = BorderStroke(
                    width = if (isSelected) 4.dp else 1.dp,
                    color = if (isSelected) ClavaColor.SelectedBorder else ClavaColor.GeneralBorder
            ),
            content = content,
            modifier = Modifier
                    .clearAndSetSemantics {
                        this.contentDescription = (if (isSelected) "selected " else "") + contentDescription
                        actions?.let {
                            customActions = actions
                        }
                        if (onClick != null) {
                            this.onClick(
                                    label = onClickActionLabel ?: if (isSelected) "deselect" else "select",
                                    action = { onClick(); true },
                            )
                        }
                    }
                    .then(selectableModifier)
    )
}
