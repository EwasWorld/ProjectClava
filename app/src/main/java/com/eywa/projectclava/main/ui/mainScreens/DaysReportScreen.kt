package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.ui.sharedUi.TabSwitcher
import com.eywa.projectclava.ui.theme.Typography

@Composable
fun DaysReportScreen(
        selectedTab: HistoryTabSwitcherItem,
        onTabSelectedListener: (HistoryTabSwitcherItem) -> Unit,
) {
    /*
     * TODO For each day,
     *  - who showed up and how many games they played
     *  - total matches played
     *  - first match, last match times
     */

    Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
    ) {
        TabSwitcher(
                items = HistoryTabSwitcherItem.values().toList(),
                selectedItem = selectedTab,
                onItemClicked = onTabSelectedListener,
                modifier = Modifier.padding(20.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
                text = "Not yet implemented",
                style = Typography.h4,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun DaysReportScreen_Preview() {
    DaysReportScreen(
            selectedTab = HistoryTabSwitcherItem.SUMMARY,
            onTabSelectedListener = {},
    )
}