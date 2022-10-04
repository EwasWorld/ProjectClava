package com.eywa.projectclava.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eywa.projectclava.main.ui.mainScreens.SetupPlayersScreen
import com.eywa.projectclava.ui.theme.ProjectClavaTheme

/*
 * Time spent: 18 hrs
 */

class MainActivity : ComponentActivity() {
    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectClavaTheme {
                Navigation(viewModel)
            }
        }
    }
}

@Composable
fun Navigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    val players by viewModel.players.collectAsState(initial = listOf())
    val matches by viewModel.matches.collectAsState(initial = listOf())
    val courts by viewModel.courts.collectAsState(initial = listOf())

    NavHost(navController = navController, startDestination = NavRoute.AddPlayer.route) {
        composable(NavRoute.AddPlayer.route) {
            SetupPlayersScreen(
                    items = players,
                    matches = matches,
                    itemAddedListener = { viewModel.addPlayer(it) },
                    itemNameEditedListener = { player, newName ->
                        viewModel.updatePlayer(player.copy(name = newName))
                    },
                    itemDeletedListener = { viewModel.deletePlayer(it) },
                    toggleIsPresentListener = { viewModel.updatePlayer(it.copy(isPresent = !it.isPresent)) },
            )
        }
    }
}

sealed class NavRoute(val route: String) {
    object AddPlayer : NavRoute("add_player")
}