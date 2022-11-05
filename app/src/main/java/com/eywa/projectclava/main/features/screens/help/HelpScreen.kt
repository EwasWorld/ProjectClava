package com.eywa.projectclava.main.features.screens.help

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eywa.projectclava.main.features.ui.ClavaDialog
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.theme.ClavaColor
import com.eywa.projectclava.main.theme.Typography
import com.eywa.projectclava.main.theme.asClickableStyle

/*
 * Help numbers in pictures:
 * - Colour: Black on #00FF00 with shadow
 * - Font: Microsoft Sans Serif
 * - Size: 50(pt?)
 */


@Composable
fun HelpScreen(
        state: HelpState,
        listener: (HelpIntent) -> Unit,
) {
    val horizontalPadding = 20.dp
    val textPadding = 10.dp
    val helpInfo = state.screen?.getHelpInfo() ?: DEFAULT_HELP_INFO

    HelpNavigationDialog(
            isShown = state.isHelpNavigationDialogShown,
            onCancelListener = { listener(HelpIntent.CloseNavDialog) },
            goToHelpScreen = { listener(HelpIntent.GoToHelpScreen(it)) },
    )

    Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                    .background(ClavaColor.HelpScreenBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 15.dp, bottom = 30.dp)
                    .fillMaxSize()
    ) {
        Text(
                text = "Help: " + helpInfo.title,
                style = Typography.h3,
                modifier = Modifier
                        .padding(horizontal = horizontalPadding + textPadding)
                        .padding(top = 5.dp)
        )

        helpInfo.body?.let {
            Text(
                    text = helpInfo.body,
                    style = Typography.body1,
                    modifier = Modifier.padding(horizontal = horizontalPadding + textPadding)
            )
        }

        helpInfo.helpContents.forEach { content ->
            Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .padding(top = 15.dp)
                            .fillMaxWidth()
            ) {
                content.content(this) { listener(HelpIntent.Navigate(it)) }

                content.contentDescription.forEach { description ->
                    Text(
                            text = description,
                            style = Typography.body1.copy(lineHeight = 25.sp),
                            modifier = Modifier.padding(horizontal = textPadding)
                    )
                }
            }
        }

        if (helpInfo.showColourHelp) {
            Divider(modifier = Modifier.padding(vertical = 5.dp))
            ColourHelp(
                    modifier = Modifier
                            .padding(horizontal = horizontalPadding + textPadding)
                            .padding(bottom = 5.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 10.dp))
        Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
        ) {
            state.screen?.let { screen ->
                Text(
                        text = "Take me to this screen",
                        style = Typography.h4.asClickableStyle(),
                        modifier = Modifier.clickable { listener(HelpIntent.Navigate(screen)) }
                )
            }
            Text(
                    text = "Help me with something else",
                    style = Typography.h4.asClickableStyle(),
                    modifier = Modifier.clickable { listener(HelpIntent.OpenNavDialog) }
            )
        }
    }
}

@Composable
private fun ColourHelp(modifier: Modifier = Modifier) {
    Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = modifier
    ) {
        Text(
                text = "Colours indicate a player or game's status:",
                style = Typography.body1,
                modifier = Modifier.padding(vertical = 5.dp)
        )

        // TODO Colour blind mode? Add symbols? Colour picker?
        listOf(
                ClavaColor.MatchQueued to "Queued",
                ClavaColor.MatchInProgress to "In progress",
                ClavaColor.MatchFinishingSoon to "Finishing soon",
                ClavaColor.MatchOverrun to "Overrunning",
                ClavaColor.MatchPaused to "Paused",
        ).forEach { (colour, description) ->
            Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = colour,
                    modifier = Modifier.padding(horizontal = 35.dp)
            ) {
                Text(
                        text = description,
                        style = Typography.body1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                )
            }
        }
    }
}

@Composable
private fun HelpNavigationDialog(
        isShown: Boolean,
        onCancelListener: () -> Unit,
        goToHelpScreen: (NavRoute?) -> Unit,
) {
    ClavaDialog(
            isShown = isShown,
            title = "Help",
            onCancelListener = onCancelListener,
    ) {
        Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            listOf(null).plus(MainNavRoute.values().filter { it.getHelpInfo() != null }).forEach {
                Text(
                        text = (it?.getHelpInfo() ?: DEFAULT_HELP_INFO).title,
                        style = Typography.body1.asClickableStyle(),
                        modifier = Modifier
                                .clickable { goToHelpScreen(it) }
                                .padding(vertical = 10.dp)
                                .fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavBar_HelpScreen_Preview() {
    HelpScreen(
            state = HelpState(),
            listener = {},
    )
}

@Preview(showBackground = true)
@Composable
fun ColoursHelp_Preview() {
    Box(modifier = Modifier.padding(10.dp)) {
        ColourHelp()
    }
}

@Preview(showBackground = true)
@Composable
fun HelpNavigationDialog_Preview() {
    HelpScreen(
            state = HelpState(isHelpNavigationDialogShown = true),
            listener = {},
    )
}

@Preview(
        showBackground = true,
        heightDp = 1900
)
@Composable
fun AddPlayer_HelpScreen_Preview() {
    HelpScreen(
            state = HelpState(screen = MainNavRoute.ADD_PLAYER),
            listener = {},
    )
}
