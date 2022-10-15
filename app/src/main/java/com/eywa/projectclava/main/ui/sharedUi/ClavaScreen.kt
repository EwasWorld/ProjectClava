package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography

@Composable
fun ClavaScreen(
        noContentText: String,
        hasContent: Boolean,
        headerContent: @Composable (() -> Unit)? = null,
        footerContent: @Composable (() -> Unit)? = null,
        aboveListContent: @Composable (() -> Unit)? = null,
        listContent: LazyListScope.() -> Unit,
) {
    val lazyColumnPadding = if (aboveListContent == null) {
        PaddingValues(vertical = 20.dp)
    }
    else {
        PaddingValues(bottom = 20.dp)
    }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
    ) {
        headerContent?.let {
            headerContent()
            Divider(thickness = DividerThickness)
        }

        aboveListContent?.invoke()

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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = lazyColumnPadding,
                    modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp)
            ) {
                listContent()
            }
        }

        footerContent?.let {
            Divider(thickness = DividerThickness)
            footerContent()
        }
    }
}