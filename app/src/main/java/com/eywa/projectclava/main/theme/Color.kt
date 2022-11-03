package com.eywa.projectclava.main.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

object ClavaColor {
    val Background = Color(0xAAD1C4E9)
    val HeaderFooterBackground = Color.White
    private val Primary = Color(0xFF6200EE)

    val ItemBackground = Color.White
    val DisabledItemBackground = Color(0xFFAAA4B4)
    val MatchQueued = Color(0xFFA5D6A7)
    val MatchPaused = Color(0xFF4FC3F7)
    val MatchInProgress = Color(0xFF4CAF50)
    val MatchOverrun = Color(0xFFF44336)
    val MatchFinishingSoon = Color(0xFFFFC107)

    val SelectedBorder = Color(0xFF3F51B5)
    val GeneralBorder = Color.LightGray

    val BottomNavBackground = Primary
    val BottomNavIcon = Color.White

    val FabBackground = Primary
    val FabIcon = Color.White

    val TabSwitcherSelected = Primary
    val OnTabSwitcherSelected = Color.White
    val TabSwitcherNotSelected = Color.White
    val OnTabSwitcherNotSelected = Color.Black
    val TabSwitcherBorder = Color.LightGray

    val HelpScreenBackground = Color.White

    val Divider = Color.Black.copy(alpha = 0.12f)
}

@Preview
@Composable
fun ClavaColor_Preview() {
    Column {
        listOf(
                ClavaColor.MatchQueued,
                ClavaColor.MatchPaused,
                ClavaColor.MatchInProgress,
                ClavaColor.MatchOverrun,
                ClavaColor.MatchFinishingSoon,
                ClavaColor.DisabledItemBackground,
                ClavaColor.ItemBackground,
        ).forEach {
            Surface(
                    color = it,
                    modifier = Modifier
                            .width(300.dp)
            ) {
                Box(contentAlignment = Alignment.CenterEnd) {
                    FloatingActionButton(
                            onClick = { },
                            backgroundColor = Color(0xFF512DA8),
                            contentColor = Color.White,
                            modifier = Modifier.padding(15.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}