package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.asColor
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.TimeRemaining
import com.eywa.projectclava.ui.theme.ClavaColor

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
        content: @Composable () -> Unit
) = Surface(
        shape = RoundedCornerShape(5.dp),
        color = if (enabled) {
            match?.asColor(getTimeRemaining) ?: ClavaColor.ItemBackground
        }
        else {
            ClavaColor.DisabledItemBackground
        },
        border = BorderStroke(
                width = if (isSelected) 4.dp else 1.dp,
                color = if (isSelected) ClavaColor.SelectedBorder else ClavaColor.GeneralBorder
        ),
        content = content
)
