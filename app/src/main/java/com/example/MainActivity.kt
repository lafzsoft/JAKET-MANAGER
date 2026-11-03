package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.StockStatusLevel
import com.example.ui.AddJacketScreen
import com.example.ui.AppScreen
import com.example.ui.DailyProductionScreen
import com.example.ui.HomeScreen
import com.example.ui.InventoryScreen
import com.example.ui.JacketListScreen
import com.example.ui.JacketViewModel
import com.example.ui.ReportsScreen
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.DrawerContent
import com.example.ui.dialogs.ExportCsvBottomSheet
import com.example.ui.dialogs.SkuImagePickerModal
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: JacketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val isIndonesian by viewModel.isIndonesian.collectAsStateWithLifecycle()
                val allJackets by viewModel.allJackets.collectAsStateWithLifecycle()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()

                val totalUnits = allJackets.sumOf { it.quantity }
                val lowStockCount = allJackets.count { it.status == StockStatusLevel.LOW_STOCK || it.status == StockStatusLevel.OUT_OF_STOCK }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(
                            currentScreen = currentScreen,
                            totalJackets = totalUnits,
                            lowStockCount = lowStockCount,
                            isIndonesian = isIndonesian,
                            onNavigate = { screen ->
                                viewModel.selectScreen(screen)
                            },
                            onSeasonalClick = {
                                viewModel.openFilterSeasonal()
                            },
                            onExportClick = {
                                viewModel.openExportSheet()
                            },
                            onLanguageToggle = {
                                viewModel.toggleLanguage()
                            },
                            onCloseDrawer = {
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    val isExportSheetOpen by viewModel.isExportSheetOpen.collectAsStateWithLifecycle()
                    if (isExportSheetOpen) {
                        ExportCsvBottomSheet(
                            allJackets = allJackets,
                            currentFilteredJackets = allJackets,
                            isIndonesian = isIndonesian,
                            onDismiss = { viewModel.closeExportSheet() }
                        )
                    }

                    val selectedJacketForImage by viewModel.selectedJacketForImage.collectAsStateWithLifecycle()
                    selectedJacketForImage?.let { targetJacket ->
                        SkuImagePickerModal(
                            jacket = targetJacket,
                            isIndonesian = isIndonesian,
                            onDismiss = { viewModel.closeImagePicker() },
                            onSave = { newImageUrl, updateAllWithSameSku ->
                                if (updateAllWithSameSku) {
                                    viewModel.updateJacketImageBySku(targetJacket.sku, newImageUrl)
                                } else {
                                    viewModel.updateJacketImage(targetJacket.id, newImageUrl)
                                }
                                viewModel.closeImagePicker()
                            }
                        )
                    }
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            AppBottomNavigationBar(
                                currentScreen = currentScreen,
                                isIndonesian = isIndonesian,
                                onScreenSelected = { screen ->
                                    viewModel.selectScreen(screen)
                                }
                            )
                        }
                    ) { scaffoldPadding ->
                        when (currentScreen) {
                            AppScreen.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onOpenDrawer = {
                                    coroutineScope.launch { drawerState.open() }
                                },
                                modifier = Modifier.padding(scaffoldPadding)
                            )
                            AppScreen.INVENTORY -> InventoryScreen(
                                viewModel = viewModel,
                                onOpenDrawer = {
                                    coroutineScope.launch { drawerState.open() }
                                },
                                onNavigateToAdd = {
                                    viewModel.navigateToAddJacket()
                                },
                                modifier = Modifier.padding(scaffoldPadding)
                            )
                            AppScreen.ADD_JACKET -> AddJacketScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    viewModel.navigateToHome()
                                },
                                modifier = Modifier.padding(scaffoldPadding)
                            )
                            AppScreen.PRODUCTION -> DailyProductionScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    viewModel.navigateToHome()
                                },
                                modifier = Modifier.padding(scaffoldPadding)
                            )
                            AppScreen.REPORTS -> ReportsScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    viewModel.navigateToHome()
                                },
                                modifier = Modifier.padding(scaffoldPadding)
                            )
                            AppScreen.JACKET_LIST -> JacketListScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    viewModel.navigateToHome()
                                },
                                onNavigateToAdd = {
                                    viewModel.navigateToAddJacket()
                                },
                                modifier = Modifier.padding(scaffoldPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
