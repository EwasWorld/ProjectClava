package com.eywa.projectclava.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.eywa.projectclava.main.model.MainState
import com.eywa.projectclava.ui.theme.ProjectClavaTheme

/*
 * Time spent: 11 hrs
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectClavaTheme {
                var state by remember { mutableStateOf(MainState()) }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ProjectClavaTheme {
    }
}