package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen

@Composable
fun DrawerContent(
    currentScreen: AppScreen,
    totalJackets: Int,
    lowStockCount: Int,
    isIndonesian: Boolean,
    onNavigate: (AppScreen) -> Unit,
    onSeasonalClick: () -> Unit,
    onExportClick: () -> Unit = {},
    onLanguageToggle: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp)
            .testTag("app_navigation_drawer"),
        drawerContainerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Drawer Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2D3748)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checkroom,
                            contentDescription = "App Icon",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Jacket Manager",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = if (isIndonesian) "Sistem Logistik Gudang" else "Warehouse Inventory System",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            // Menu Section: Primary Destinations
            Text(
                text = if (isIndonesian) "MENU UTAMA" else "MAIN MENU",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            // 1. Home
            NavigationDrawerItem(
                label = { Text(if (isIndonesian) "Katalog Beranda" else "Catalog & Home", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                selected = currentScreen == AppScreen.HOME,
                onClick = {
                    onNavigate(AppScreen.HOME)
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            // 2. Inventory
            NavigationDrawerItem(
                label = { Text(if (isIndonesian) "Inventaris Gudang" else "Warehouse Inventory", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                badge = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "$totalJackets", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3748))
                    }
                },
                selected = currentScreen == AppScreen.INVENTORY,
                onClick = {
                    onNavigate(AppScreen.INVENTORY)
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            // 3. Produksi Harian (Daily Production)
            NavigationDrawerItem(
                label = { Text(if (isIndonesian) "Produksi Harian" else "Daily Production", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.PrecisionManufacturing, contentDescription = null) },
                selected = currentScreen == AppScreen.PRODUCTION,
                onClick = {
                    onNavigate(AppScreen.PRODUCTION)
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            // 4. Add Jacket
            NavigationDrawerItem(
                label = { Text(if (isIndonesian) "Tambah Jaket Baru" else "Add New Jacket", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = null) },
                selected = currentScreen == AppScreen.ADD_JACKET,
                onClick = {
                    onNavigate(AppScreen.ADD_JACKET)
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            // 4. Reports & Analytics / Dashboard
            NavigationDrawerItem(
                label = { Text(if (isIndonesian) "Dashboard & Analitik" else "Dashboard & Analytics", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                selected = currentScreen == AppScreen.REPORTS,
                onClick = {
                    onNavigate(AppScreen.REPORTS)
                    onCloseDrawer()
                },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .testTag("drawer_menu_reports")
                    .testTag("drawer_menu_dashboard")
            )

            // 5. Room Database Stock Status LazyColumn
            NavigationDrawerItem(
                label = { Text(if (isIndonesian) "Status Stok (Room DB)" else "Stock Status (Room DB)", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null) },
                badge = {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (lowStockCount > 0) Color(0xFFFFFBEB) else Color(0xFFECFDF5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (lowStockCount > 0) Color(0xFFFDE68A) else Color(0xFFA7F3D0))
                    ) {
                        Text(
                            text = if (lowStockCount > 0) "! $lowStockCount" else "OK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lowStockCount > 0) Color(0xFFB45309) else Color(0xFF047857),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                },
                selected = currentScreen == AppScreen.JACKET_LIST,
                onClick = {
                    onNavigate(AppScreen.JACKET_LIST)
                    onCloseDrawer()
                },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .testTag("drawer_menu_jacket_list")
            )

            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            // Quick Actions Section
            Text(
                text = if (isIndonesian) "PINTASAN & PENGATURAN" else "SHORTCUTS & SETTINGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            // Seasonal Collection Shortcut
            NavigationDrawerItem(
                label = { Text(if (isIndonesian) "Koleksi Musim Dingin" else "Seasonal Winter Collection", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.AcUnit, contentDescription = null, tint = Color(0xFF0284C7)) },
                selected = false,
                onClick = {
                    onSeasonalClick()
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            // Export CSV Shortcut
            NavigationDrawerItem(
                label = { Text(if (isIndonesian) "Ekspor Inventaris (CSV)" else "Export Inventory (CSV)", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF047857)) },
                selected = false,
                badge = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "CSV", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    }
                },
                onClick = {
                    onExportClick()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .testTag("drawer_export_csv_item")
            )

            // Stock Alerts item
            NavigationDrawerItem(
                label = { Text(if (isIndonesian) "Peringatan Stok Rendah" else "Low Stock Alerts", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFD97706)) },
                badge = {
                    if (lowStockCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "$lowStockCount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }
                    }
                },
                selected = false,
                onClick = {
                    onNavigate(AppScreen.REPORTS)
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            // Language Toggle
            NavigationDrawerItem(
                label = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isIndonesian) "Bahasa Aplikasi" else "App Language", fontWeight = FontWeight.Medium)
                        Text(if (isIndonesian) "Indonesia" else "English", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }
                },
                icon = { Icon(Icons.Default.Translate, contentDescription = null) },
                selected = false,
                onClick = {
                    onLanguageToggle()
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer of Drawer: Room Database indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                    )
                    Text(
                        text = if (isIndonesian) "Database Lokal Room: Aktif & Sinkron" else "Room Local DB: Online & Synced",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
