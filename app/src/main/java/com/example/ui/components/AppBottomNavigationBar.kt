package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.AppScreen

@Composable
fun AppBottomNavigationBar(
    currentScreen: AppScreen,
    isIndonesian: Boolean,
    onScreenSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("app_bottom_navigation_bar"),
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        // Tab 1: Home
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { onScreenSelected(AppScreen.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text(
                    text = if (isIndonesian) "Beranda" else "Home",
                    maxLines = 1
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color(0xFF1E293B),
                indicatorColor = Color(0xFF2D3748),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("bottom_tab_home")
        )

        // Tab 2: Inventory
        NavigationBarItem(
            selected = currentScreen == AppScreen.INVENTORY,
            onClick = { onScreenSelected(AppScreen.INVENTORY) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.INVENTORY) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                    contentDescription = "Inventory"
                )
            },
            label = {
                Text(
                    text = if (isIndonesian) "Inventaris" else "Inventory",
                    maxLines = 1
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color(0xFF1E293B),
                indicatorColor = Color(0xFF2D3748),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("bottom_tab_inventory")
        )

        // Tab 3: Produksi Harian (Daily Production)
        NavigationBarItem(
            selected = currentScreen == AppScreen.PRODUCTION,
            onClick = { onScreenSelected(AppScreen.PRODUCTION) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.PRODUCTION) Icons.Filled.PrecisionManufacturing else Icons.Outlined.PrecisionManufacturing,
                    contentDescription = "Production"
                )
            },
            label = {
                Text(
                    text = if (isIndonesian) "Produksi" else "Production",
                    maxLines = 1
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color(0xFF1E293B),
                indicatorColor = Color(0xFF2D3748),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("bottom_tab_production")
        )

        // Tab 4: Add Jacket
        NavigationBarItem(
            selected = currentScreen == AppScreen.ADD_JACKET,
            onClick = { onScreenSelected(AppScreen.ADD_JACKET) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.ADD_JACKET) Icons.Filled.AddCircle else Icons.Outlined.AddCircleOutline,
                    contentDescription = "Add Jacket"
                )
            },
            label = {
                Text(
                    text = if (isIndonesian) "Tambah" else "Add New",
                    maxLines = 1
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color(0xFF1E293B),
                indicatorColor = Color(0xFF2D3748),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("bottom_tab_add")
        )

        // Tab 4: Reports
        NavigationBarItem(
            selected = currentScreen == AppScreen.REPORTS,
            onClick = { onScreenSelected(AppScreen.REPORTS) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.REPORTS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    contentDescription = "Reports"
                )
            },
            label = {
                Text(
                    text = if (isIndonesian) "Dashboard" else "Dashboard",
                    maxLines = 1
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color(0xFF1E293B),
                indicatorColor = Color(0xFF2D3748),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier
                .testTag("bottom_tab_reports")
                .testTag("bottom_tab_dashboard")
        )
    }
}
