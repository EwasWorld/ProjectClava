package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.Typography

interface TabSwitcherItem {
    val label: String
}

@Composable
fun <T : TabSwitcherItem> TabSwitcher(
        items: Iterable<T>,
        selectedItem: T,
        onItemClicked: (T) -> Unit,
        modifier: Modifier = Modifier,
) {
    require(items.count() >= 2) { "Must have at least two items" }

    Surface(
            shape = RoundedCornerShape(100),
            border = BorderStroke(1.dp, ClavaColor.TabSwitcherBorder),
            modifier = modifier.height(IntrinsicSize.Min)
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
        ) {
            items.forEachIndexed { index, item ->
                val textColour: Color
                val backgroundColour: Color
                if (selectedItem == item) {
                    backgroundColour = ClavaColor.TabSwitcherSelected
                    textColour = ClavaColor.OnTabSwitcherSelected
                }
                else {
                    backgroundColour = ClavaColor.TabSwitcherNotSelected
                    textColour = ClavaColor.OnTabSwitcherNotSelected
                }

                Text(
                        text = item.label,
                        style = Typography.body1.copy(
                                color = textColour,
                                fontWeight = FontWeight.Bold,
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .clickable { onItemClicked(item) }
                                .weight(1f)
                                .background(backgroundColour)
                                .fillMaxHeight()
                                .padding(10.dp)
                )
                if (index != items.count() - 1) {
                    Divider(
                            color = ClavaColor.TabSwitcherBorder,
                            modifier = Modifier
                                    .fillMaxHeight()
                                    .width(2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TabSwitcher_Preview() {
    TabSwitcher(
            items = SetupListTabSwitcherItem.values().toList(),
            selectedItem = SetupListTabSwitcherItem.PLAYERS,
            onItemClicked = {},
    )
}