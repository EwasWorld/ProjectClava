package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eywa.projectclava.ui.theme.Typography

@Composable
fun PreviousMatchesScreen() {
    Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
    ) {
        Text(
                text = "I haven't made this screen yet xD",
                style = Typography.h4,
        )
    }

    // TODO PreviousMatchesScreen

    // TODO Continue game
}

@Preview
@Composable
fun PreviousMatchesScreen_Preview() {
    PreviousMatchesScreen()
}