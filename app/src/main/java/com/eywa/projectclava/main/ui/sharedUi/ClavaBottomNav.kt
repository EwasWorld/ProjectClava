package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.eywa.projectclava.R
import com.eywa.projectclava.main.NavRoute

@Composable
fun ClavaBottomNav(
        navController: NavController,
) {
    val current by navController.currentBackStackEntryAsState()
    ClavaBottomNav(
            currentRoute = current?.destination?.route,
            onClick = { navController.navigate(it) }
    )
}

@Composable
fun ClavaBottomNav(
        currentRoute: String?,
        onClick: (destination: String) -> Unit,
) {
    BottomNavigation {
        ClavaBottomNavItem(
                icon = ClavaIconInfo.PainterIcon(R.drawable.outline_assignment_24),
                selectedIcon = ClavaIconInfo.PainterIcon(R.drawable.baseline_assignment_24),
                label = "Manage",
                contentDescription = "Manage players and courts",
                destinations = listOf(NavRoute.ADD_PLAYER.route, NavRoute.ADD_COURT.route),
                currentRoute = currentRoute,
                onClick = onClick,
        )
        ClavaBottomNavItem(
                icon = ClavaIconInfo.PainterIcon(R.drawable.outline_groups_24),
                selectedIcon = ClavaIconInfo.PainterIcon(R.drawable.baseline_groups_24),
                label = "Match up",
                contentDescription = "Match up players",
                destination = NavRoute.CREATE_MATCH.route,
                currentRoute = currentRoute,
                onClick = onClick,
        )
        ClavaBottomNavItem(
                icon = ClavaIconInfo.PainterIcon(R.drawable.outline_pending_24),
                selectedIcon = ClavaIconInfo.PainterIcon(R.drawable.baseline_pending_24),
                label = "Queue",
                contentDescription = "Match queue",
                destination = NavRoute.UPCOMING_MATCHES.route,
                currentRoute = currentRoute,
                onClick = onClick,
        )
        // TODO Add an icon if a match has finished
        ClavaBottomNavItem(
                icon = ClavaIconInfo.PainterIcon(R.drawable.baseline_timelapse_24),
                label = "Ongoing",
                contentDescription = "Ongoing matches",
                destination = NavRoute.CURRENT_MATCHES.route,
                currentRoute = currentRoute,
                onClick = onClick,
        )
        ClavaBottomNavItem(
                icon = ClavaIconInfo.PainterIcon(R.drawable.baseline_history_24),
                label = "History",
                contentDescription = "Match history",
                destinations = listOf(NavRoute.PREVIOUS_MATCHES.route, NavRoute.DAYS_REPORT.route),
                currentRoute = currentRoute,
                onClick = onClick,
        )
    }
}

@Composable
fun RowScope.ClavaBottomNavItem(
        icon: ClavaIconInfo,
        selectedIcon: ClavaIconInfo = icon,
        label: String,
        contentDescription: String,
        destination: String,
        currentRoute: String?,
        onClick: (destination: String) -> Unit,
) = ClavaBottomNavItem(
        icon = icon,
        selectedIcon = selectedIcon,
        label = label,
        contentDescription = contentDescription,
        destinations = listOf(destination),
        currentRoute = currentRoute,
        onClick = onClick,
)

/**
 * @param destinations the first destination is where it will navigate to on click,
 * others are used to determine whether the item is selected
 */
@Composable
fun RowScope.ClavaBottomNavItem(
        icon: ClavaIconInfo,
        selectedIcon: ClavaIconInfo = icon,
        label: String,
        contentDescription: String,
        destinations: Iterable<String>,
        currentRoute: String?,
        onClick: (destination: String) -> Unit,
) {
    require(destinations.any()) { "No destinations for nav button" }
    val isSelected = destinations.contains(currentRoute)
    BottomNavigationItem(
            selected = isSelected,
            onClick = { onClick(destinations.first()) },
            icon = { (if (isSelected) selectedIcon else icon).ClavaIcon() },
            label = {
                Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            modifier = Modifier.semantics { this.contentDescription = contentDescription }
    )
}

@Preview
@Composable
fun A_ClavaBottomNav_Preview() {
    ClavaBottomNav(
            currentRoute = NavRoute.CREATE_MATCH.route,
            onClick = {},
    )
}

@Preview
@Composable
fun B_ClavaBottomNav_Preview() {
    ClavaBottomNav(
            currentRoute = NavRoute.ADD_COURT.route,
            onClick = {},
    )
}