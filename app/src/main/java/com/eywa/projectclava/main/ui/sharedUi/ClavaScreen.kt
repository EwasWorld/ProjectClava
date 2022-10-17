package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography

@Composable
fun ClavaScreen(
        noContentText: String,
        hasContent: Boolean,
        modifier: Modifier = Modifier,
        headerContent: @Composable (() -> Unit)? = null,
        footerContent: @Composable (() -> Unit)? = null,
        listArrangement: Arrangement.Vertical = Arrangement.spacedBy(10.dp),
        listModifier: Modifier = Modifier,
        listContent: LazyListScope.() -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
    ) {
        headerContent?.let {
            Surface(
                    color = ClavaColor.HeaderFooterBackground,
                    modifier = Modifier.fillMaxWidth()
            ) {
                headerContent()
            }
            Divider(thickness = DividerThickness)
        }

        if (!hasContent) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                    text = noContentText,
                    style = Typography.h4
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        else {
            LazyColumn(
                    verticalArrangement = listArrangement,
                    contentPadding = PaddingValues(vertical = 20.dp),
                    modifier = listModifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp)
            ) {
                listContent()
            }
        }

        footerContent?.let {
            Divider(thickness = DividerThickness)
            Surface(
                    color = ClavaColor.HeaderFooterBackground,
                    modifier = Modifier.fillMaxWidth()
            ) {
                footerContent()
            }
        }
    }
}