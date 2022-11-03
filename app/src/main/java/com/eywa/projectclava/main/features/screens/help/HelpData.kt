package com.eywa.projectclava.main.features.screens.help

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.mainActivity.NavRoute


/**
 * Help data for a particular screen
 */
data class HelpData(
        val title: String,
        val body: String? = null,
        val showColourHelp: Boolean = false,
        val helpContents: List<HelpContent>,
) {
    constructor(
            title: String,
            body: String? = null,
            showColourHelp: Boolean = false,
            helpContent: HelpContent,
    ) : this(
            title,
            body,
            showColourHelp,
            listOf(helpContent),
    )
}

/**
 * An image (usually with labels) and a description of the image
 */
data class HelpImage(
        @DrawableRes val imageId: Int,
        val imageDescription: List<AnnotatedString>,
) : HelpContent(
        content = {
            Surface(
                    modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .border(1.dp, Color.Black, RoundedCornerShape(8))
            ) {
                Image(
                        painter = painterResource(imageId),
                        contentDescription = "Image of screen",
                        modifier = Modifier.sizeIn(maxHeight = 600.dp)
                )
            }
        },
        contentDescription = imageDescription,
)

/**
 * Help content and a description of the content
 */
open class HelpContent(
        val content: @Composable ColumnScope.(navListener: (NavRoute) -> Unit) -> Unit,
        val contentDescription: List<AnnotatedString>,
)
