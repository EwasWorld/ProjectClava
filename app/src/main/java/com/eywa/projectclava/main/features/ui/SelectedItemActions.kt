package com.eywa.projectclava.main.features.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.theme.Typography

data class SelectedItemAction(
        val icon: ClavaIconInfo,
        val enabled: Boolean = true,
        val onClick: () -> Unit,
)

@Composable
fun SelectedItemActions(
        text: String,
        extraText: String? = null,
        color: Color? = null,
        buttons: List<SelectedItemAction>,
) {
    SelectedItemActions(
            color = color,
            buttons = buttons,
    ) {
        Column(
                modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {
            Text(
                    text = text,
                    style = Typography.h4,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
            )
            if (extraText != null) {
                Text(
                        text = extraText,
                        style = Typography.body1,
                )
            }
        }
    }
}

@Composable
fun SelectedItemActions(
        buttons: List<SelectedItemAction>,
        color: Color? = null,
        content: @Composable RowScope.() -> Unit
) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                    .background(color ?: Color.Transparent)
                    .padding(horizontal = 10.dp)
    ) {
        content()

        buttons.forEach {
            IconButton(
                    enabled = it.enabled,
                    onClick = it.onClick
            ) {
                it.icon.ClavaIcon()
            }
        }
    }
}