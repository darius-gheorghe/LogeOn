package com.sapphyx.logeon.ui.composable.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sapphyx.logeon.navigation.bottomNavigation.BottomNavItem

@Composable
fun LogeOnBottomNavigationBar(
    navController: NavController,
    action: (BottomNavItem) -> Unit,
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Pulse,
        BottomNavItem.Add,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = Color.Transparent
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val itemTitle = stringResource(item.title)
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = itemTitle
                    )
                },
                label = {
                    Text(itemTitle)
                },
                selected = item.route == currentRoute,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                onClick = {
                    action(item)
                }
            )
        }
    }
}