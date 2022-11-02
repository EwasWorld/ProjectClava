package com.eywa.projectclava.main.mainActivity.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.eywa.projectclava.R
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.ui.sharedUi.ClavaIconInfo
import com.eywa.projectclava.ui.theme.ClavaColor

@Composable
fun ClavaBottomNav(
        navController: NavController,
        hasOverrunningMatch: Boolean,
) {
    val current by navController.currentBackStackEntryAsState()
    ClavaBottomNav(
            currentRoute = current?.destination?.route,
            hasOverrunningMatch = hasOverrunningMatch,
            onClick = { navController.navigate(it.route) }
    )
}

@Composable
fun ClavaBottomNav(
        currentRoute: String?,
        hasOverrunningMatch: Boolean,
        onClick: (destination: NavRoute) -> Unit,
) {
    BottomNavigation(
            backgroundColor = ClavaColor.BottomNavBackground,
            contentColor = ClavaColor.BottomNavIcon,
    ) {
        ClavaBottomNavItem(
                icon = ClavaIconInfo.PainterIcon(R.drawable.outline_assignment_24),
                selectedIcon = ClavaIconInfo.PainterIcon(R.drawable.baseline_assignment_24),
                label = "Manage",
                contentDescription = "Manage players and courts",
                destinations = listOf(NavRoute.ADD_PLAYER, NavRoute.ADD_COURT),
                currentRoute = currentRoute,
                onClick = onClick,
        )
        ClavaBottomNavItem(
                icon = ClavaIconInfo.PainterIcon(R.drawable.outline_groups_24),
                selectedIcon = ClavaIconInfo.PainterIcon(R.drawable.baseline_groups_24),
                label = "Match up",
                contentDescription = "Match up players",
                destination = NavRoute.CREATE_MATCH,
                currentRoute = currentRoute,
                onClick = onClick,
        )
        ClavaBottomNavItem(
                icon = ClavaIconInfo.PainterIcon(R.drawable.outline_pending_24),
                selectedIcon = ClavaIconInfo.PainterIcon(R.drawable.baseline_pending_24),
                label = "Queue",
                contentDescription = "Match queue",
                destination = NavRoute.MATCH_QUEUE,
                currentRoute = currentRoute,
                onClick = onClick,
        )
        ClavaBottomNavItem(
                icon = ClavaIconInfo.PainterIcon(R.drawable.baseline_timelapse_24),
                label = "Ongoing",
                contentDescription = "Ongoing matches",
                badgeContent = if (hasOverrunningMatch) "" else null,
                destination = NavRoute.ONGOING_MATCHES,
                currentRoute = currentRoute,
                onClick = onClick,
        )
        ClavaBottomNavItem(
                icon = ClavaIconInfo.PainterIcon(R.drawable.baseline_history_24),
                label = "History",
                contentDescription = "Match history",
                destinations = listOf(NavRoute.MATCH_HISTORY, NavRoute.HISTORY_SUMMARY),
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
        badgeContent: String? = null,
        destination: NavRoute,
        currentRoute: String?,
        onClick: (destination: NavRoute) -> Unit,
) = ClavaBottomNavItem(
        icon = icon,
        selectedIcon = selectedIcon,
        label = label,
        contentDescription = contentDescription,
        badgeContent = badgeContent,
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
        badgeContent: String? = null,
        destinations: Iterable<NavRoute>,
        currentRoute: String?,
        onClick: (destination: NavRoute) -> Unit,
) {
    require(destinations.any()) { "No destinations for nav button" }
    val isSelected = destinations.map { it.route }.contains(currentRoute)
    BottomNavigationItem(
            selected = isSelected,
            onClick = { onClick(destinations.first()) },
            icon = {
                val displayIcon = if (isSelected) selectedIcon else icon
                if (badgeContent != null) {
                    BadgedBox(
                            badge = {
                                Badge(content = badgeContent.takeIf { it.isNotBlank() }?.let { { Text(badgeContent) } })
                            },
                            content = { displayIcon.ClavaIcon() },
                    )
                }
                else {
                    displayIcon.ClavaIcon()
                }
            },
            label = {
                Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
            hasOverrunningMatch = false,
    )
}

@Preview
@Composable
fun B_ClavaBottomNav_Preview() {
    ClavaBottomNav(
            currentRoute = NavRoute.ADD_COURT.route,
            onClick = {},
            hasOverrunningMatch = true,
    )
}