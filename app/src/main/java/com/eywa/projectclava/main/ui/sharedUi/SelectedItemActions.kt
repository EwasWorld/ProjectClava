package com.eywa.projectclava.main.ui.sharedUi

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.ui.theme.Typography

data class SelectedItemAction(
        val icon: SelectedItemActionIcon,
        val contentDescription: String?,
        val enabled: Boolean,
        val onClick: () -> Unit,
)

sealed class SelectedItemActionIcon {
    data class VectorIcon(val imageVector: ImageVector) : SelectedItemActionIcon()
    data class PainterIcon(@DrawableRes val drawable: Int) : SelectedItemActionIcon()
}

@Composable
fun SelectedItemActions(
        text: String,
        buttons: List<SelectedItemAction>,
) {
    SelectedItemActions(
            buttons = buttons,
    ) {
        Text(
                text = text,
                style = Typography.h4,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 15.dp)
        )
    }
}

@Composable
fun SelectedItemActions(
        buttons: List<SelectedItemAction>,
        content: @Composable RowScope.() -> Unit
) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        content()

        buttons.forEach {
            IconButton(
                    enabled = it.enabled,
                    onClick = it.onClick
            ) {
                if (it.icon is SelectedItemActionIcon.VectorIcon) {
                    Icon(imageVector = it.icon.imageVector, contentDescription = it.contentDescription)
                }
                else if (it.icon is SelectedItemActionIcon.PainterIcon) {
                    Icon(painter = painterResource(it.icon.drawable), contentDescription = it.contentDescription)
                }
            }
        }
    }
}