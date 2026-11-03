package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import com.example.ui.components.EmptyInventoryView
import com.example.ui.components.JacketGridCard
import com.example.ui.components.NetworkImageWithFallback
import com.example.ui.dialogs.AddJacketSheet
import com.example.ui.dialogs.ExportCsvBottomSheet
import com.example.ui.dialogs.JacketDetailModal
import com.example.ui.scanner.QrCodeScannerModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: JacketViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateToHome()
    }

    val allJackets by viewModel.allJackets.collectAsStateWithLifecycle()
    val isIndonesian by viewModel.isIndonesian.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("ALL") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedStyleFilter by remember { mutableStateOf("ALL") }
    var selectedSort by remember { mutableStateOf("STYLE_ASC") }
    var jacketToDelete by remember { mutableStateOf<JacketItem?>(null) }
    var isExportSheetOpen by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    var selectedJacketForDetails by remember { mutableStateOf<JacketItem?>(null) }
    var isScannerOpen by remember { mutableStateOf(false) }
    var isAddSheetOpen by remember { mutableStateOf(false) }

    // Summary metrics: total available stock and unique registered variants
    val totalAvailableStock = remember(allJackets) { allJackets.sumOf { it.quantity } }
    val uniqueVariantsCount = remember(allJackets) { allJackets.map { it.sku.ifBlank { it.id } }.distinct().size }
    val inStockVariants = remember(allJackets) { allJackets.count { it.status == StockStatusLevel.IN_STOCK } }
    val lowStockVariants = remember(allJackets) { allJackets.count { it.status == StockStatusLevel.LOW_STOCK } }
    val outOfStockVariants = remember(allJackets) { allJackets.count { it.status == StockStatusLevel.OUT_OF_STOCK } }

    // Filter and Sort jackets
    val displayedJackets = remember(allJackets, searchQuery, selectedFilterTab, selectedCategoryFilter, selectedStyleFilter, selectedSort) {
        var list = allJackets

        // Filter by tab
        list = when (selectedFilterTab) {
            "IN_STOCK" -> list.filter { it.status == StockStatusLevel.IN_STOCK }
            "LOW_STOCK" -> list.filter { it.status == StockStatusLevel.LOW_STOCK }
            "OUT_OF_STOCK" -> list.filter { it.status == StockStatusLevel.OUT_OF_STOCK }
            "SEASONAL" -> list.filter { it.isSeasonal }
            else -> list
        }

        // Filter by demographic category (Jaket Pria, Jaket Wanita, Jaket Anak, Jaket Unisex)
        if (selectedCategoryFilter != "ALL") {
            list = list.filter { it.genderCategory.equals(selectedCategoryFilter, ignoreCase = true) }
        }

        // Filter by style
        if (selectedStyleFilter != "ALL") {
            list = list.filter { it.style.equals(selectedStyleFilter, ignoreCase = true) }
        }

        // Filter by search query (allows filtering by jacket name, style, SKU, etc.)
        if (searchQuery.isNotBlank()) {
            val tokens = searchQuery.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
            list = list.filter { jacket ->
                tokens.all { token ->
                    jacket.name.contains(token, ignoreCase = true) ||
                    jacket.style.contains(token, ignoreCase = true) ||
                    jacket.sku.contains(token, ignoreCase = true) ||
                    jacket.material.contains(token, ignoreCase = true) ||
                    jacket.color.contains(token, ignoreCase = true) ||
                    jacket.locationRack.contains(token, ignoreCase = true)
                }
            }
        }

        // Sort
        when (selectedSort) {
            "QTY_DESC" -> list.sortedByDescending { it.quantity }
            "QTY_ASC" -> list.sortedBy { it.quantity }
            "NAME_ASC" -> list.sortedBy { it.name.lowercase() }
            "NAME_DESC" -> list.sortedByDescending { it.name.lowercase() }
            "STYLE_ASC" -> list.sortedWith(compareBy({ it.style }, { it.name }))
            "STYLE_DESC" -> list.sortedWith(compareByDescending<JacketItem> { it.style }.thenBy { it.name })
            "RACK" -> list.sortedBy { it.locationRack }
            else -> list
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("inventory_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isIndonesian) "Inventaris Gudang" else "Warehouse Inventory",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = if (isIndonesian) "${displayedJackets.size} jaket terdata" else "${displayedJackets.size} jackets listed",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF1A1A1A))
                    }
                },
                actions = {
                    var isSortMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { isSortMenuExpanded = true }) {
                            Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort", tint = Color(0xFF1A1A1A))
                        }
                        DropdownMenu(
                            expanded = isSortMenuExpanded,
                            onDismissRequest = { isSortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isIndonesian) "Model: A ke Z" else "Style: A to Z") },
                                onClick = { selectedSort = "STYLE_ASC"; isSortMenuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isIndonesian) "Model: Z ke A" else "Style: Z to A") },
                                onClick = { selectedSort = "STYLE_DESC"; isSortMenuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isIndonesian) "Stok: Terbanyak" else "Stock: High to Low") },
                                onClick = { selectedSort = "QTY_DESC"; isSortMenuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isIndonesian) "Stok: Tersedikit" else "Stock: Low to High") },
                                onClick = { selectedSort = "QTY_ASC"; isSortMenuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isIndonesian) "Nama: A ke Z" else "Name: A to Z") },
                                onClick = { selectedSort = "NAME_ASC"; isSortMenuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isIndonesian) "Nama: Z ke A" else "Name: Z to A") },
                                onClick = { selectedSort = "NAME_DESC"; isSortMenuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) }
                            )
                        }
                    }
                    IconButton(
                        onClick = { isGridView = !isGridView },
                        modifier = Modifier.testTag("btn_toggle_grid_view")
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = if (isGridView) (if (isIndonesian) "Beralih ke Tampilan Daftar" else "Switch to List View") else (if (isIndonesian) "Beralih ke Tampilan Grid" else "Switch to Grid View"),
                            tint = Color(0xFF1A1A1A)
                        )
                    }
                    IconButton(
                        onClick = { isScannerOpen = true },
                        modifier = Modifier.testTag("btn_open_qr_scanner")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = if (isIndonesian) "Pindai Label QR" else "Scan QR Label",
                            tint = Color(0xFF2563EB)
                        )
                    }
                    IconButton(
                        onClick = { isExportSheetOpen = true },
                        modifier = Modifier.testTag("btn_export_csv_top")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Export CSV", tint = Color(0xFF047857))
                    }
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Jacket", tint = Color(0xFF1A1A1A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isAddSheetOpen = true },
                icon = {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Jacket")
                },
                text = {
                    Text(
                        text = if (isIndonesian) "Tambah Jaket" else "Add Jacket",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("fab_add_jacket")
                    .navigationBarsPadding()
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Kartu Ringkasan Stok & Varian Unik di bagian atas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("inventory_top_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = if (isIndonesian) "Ringkasan Gudang & Inventaris" else "Warehouse & Inventory Summary",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF334155)
                        ) {
                            Text(
                                text = if (isIndonesian) "Live Data" else "Live Data",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dual Hero Metrics: Total Stok & Varian Unik
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Metric 1: Total Stok Jaket Tersedia
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                                .padding(10.dp)
                                .testTag("card_metric_total_stock")
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isIndonesian) "Total Stok" else "Total Stock",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "$totalAvailableStock",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Text(
                                    text = if (isIndonesian) "Unit jaket tersedia" else "Units in stock",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        // Metric 2: Varian Unik Terdaftar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                                .padding(10.dp)
                                .testTag("card_metric_unique_variants")
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isIndonesian) "Varian Unik" else "Unique Variants",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "$uniqueVariantsCount",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Text(
                                    text = if (isIndonesian) "Kode SKU terdaftar" else "Registered SKUs",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Breakdown Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIndonesian) "Tersedia: $inStockVariants • Menipis: $lowStockVariants • Habis: $outOfStockVariants" else "In Stock: $inStockVariants • Low: $lowStockVariants • Out: $outOfStockVariants",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = if (isIndonesian) "${displayedJackets.size} ditampilkan" else "${displayedJackets.size} shown",
                            fontSize = 11.sp,
                            color = Color(0xFF60A5FA),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Search Bar & Filter Rows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search Bar at the top of the inventory grid to filter jackets by name or style
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (isIndonesian) "Cari nama jaket atau model gaya (contoh: Bomber, Parka)..." else "Filter jackets by name or style (e.g. Bomber, Parka)...",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = if (isIndonesian) "Cari" else "Search",
                            tint = if (searchQuery.isNotBlank()) Color(0xFF2563EB) else Color(0xFF64748B)
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.testTag("btn_clear_search")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = if (isIndonesian) "Hapus Pencarian" else "Clear Search",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { isScannerOpen = true },
                                modifier = Modifier.testTag("btn_search_bar_qr_scanner")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = if (isIndonesian) "Pindai Label QR" else "Scan QR Label",
                                    tint = Color(0xFF2563EB)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_bar_input")
                        .testTag("inventory_search_bar")
                        .testTag("inventory_grid_search_bar")
                )

                // Quick Style Search Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    item {
                        Text(
                            text = if (isIndonesian) "Cari Cepat:" else "Quick Search:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(end = 2.dp, top = 6.dp)
                        )
                    }
                    val quickStyles = listOf("Bomber", "Parka", "Denim", "Trench", "Puffer")
                    items(quickStyles) { styleName ->
                        val isMatched = searchQuery.contains(styleName, ignoreCase = true)
                        SuggestionChip(
                            onClick = {
                                searchQuery = if (isMatched) "" else styleName
                            },
                            label = { Text(styleName, fontSize = 11.sp) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Checkroom,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isMatched) Color(0xFF2D3748) else Color(0xFFF1F5F9),
                                labelColor = if (isMatched) Color.White else Color(0xFF334155),
                                iconContentColor = if (isMatched) Color.White else Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag("quick_search_style_${styleName.lowercase()}")
                        )
                    }
                }

                // Active Search Query Filter Feedback Pill
                if (searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_search_indicator")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isIndonesian) {
                                        "Menampilkan ${displayedJackets.size} jaket untuk: \"$searchQuery\""
                                    } else {
                                        "Showing ${displayedJackets.size} jackets for: \"$searchQuery\""
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E40AF)
                                )
                            }

                            Text(
                                text = if (isIndonesian) "Reset" else "Reset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB),
                                modifier = Modifier
                                    .clickable { searchQuery = "" }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Export & Filter Header Row with Grid/List Toggle Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isIndonesian) "Filter & Data Gudang:" else "Warehouse Filters & Data:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isGridView) Color(0xFF2D3748) else Color(0xFFE2E8F0),
                            modifier = Modifier
                                .clickable { isGridView = !isGridView }
                                .testTag("chip_toggle_view_mode")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isGridView) Icons.Default.GridView else Icons.Default.ViewList,
                                    contentDescription = null,
                                    tint = if (isGridView) Color.White else Color(0xFF475569),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isGridView) "Grid" else (if (isIndonesian) "Daftar" else "List"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGridView) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = { isExportSheetOpen = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFDCFCE7),
                            contentColor = Color(0xFF166534)
                        ),
                        modifier = Modifier.testTag("btn_export_csv_quick")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isIndonesian) "Ekspor CSV" else "Export CSV",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Stock Status Tabs (Chips)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tabs = listOf(
                        "ALL" to if (isIndonesian) "Semua Status" else "All Status",
                        "IN_STOCK" to if (isIndonesian) "Stok Tersedia" else "In Stock",
                        "LOW_STOCK" to if (isIndonesian) "Stok Sedikit" else "Low Stock",
                        "OUT_OF_STOCK" to if (isIndonesian) "Habis Stok" else "Out of Stock",
                        "SEASONAL" to if (isIndonesian) "Koleksi Musiman" else "Seasonal"
                    )

                    items(tabs) { (key, label) ->
                        val isSelected = selectedFilterTab == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilterTab = key },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2D3748),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Category Filter Tabs (Chips)
                val categories = remember {
                    listOf("Jaket Pria", "Jaket Wanita", "Jaket Anak", "Jaket Unisex")
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "ALL",
                            onClick = { selectedCategoryFilter = "ALL" },
                            label = { Text(if (isIndonesian) "Semua Kategori" else "All Categories", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E293B),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("inv_category_chip_all")
                        )
                    }
                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter.equals(cat, ignoreCase = true)
                        val count = allJackets.count { it.genderCategory.equals(cat, ignoreCase = true) }
                        val icon = when (cat) {
                            "Jaket Pria" -> Icons.Default.Male
                            "Jaket Wanita" -> Icons.Default.Female
                            "Jaket Anak" -> Icons.Default.ChildCare
                            else -> Icons.Default.Group
                        }
                        val tagSlug = when (cat) {
                            "Jaket Pria" -> "pria"
                            "Jaket Wanita" -> "wanita"
                            "Jaket Anak" -> "anak"
                            else -> "unisex"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = if (isSelected) "ALL" else cat },
                            label = { Text("$cat ($count)", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E293B),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("inv_category_chip_$tagSlug")
                        )
                    }
                }

                // Style Filter Tabs (Chips)
                val styles = remember(allJackets) {
                    val standard = listOf("Bomber Jacket", "Parka", "Denim Jacket", "Trench Coat", "Puffer Jacket")
                    (standard + allJackets.map { it.style }).distinct()
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedStyleFilter == "ALL",
                            onClick = { selectedStyleFilter = "ALL" },
                            label = { Text(if (isIndonesian) "Semua Model" else "All Styles", fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF475569),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(styles) { style ->
                        val isSelected = selectedStyleFilter.equals(style, ignoreCase = true)
                        val count = allJackets.count { it.style.equals(style, ignoreCase = true) }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStyleFilter = if (isSelected) "ALL" else style },
                            label = { Text("$style ($count)", fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF475569),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Quick Sort Chips (Stock Quantity & Alphabetical Name)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    item {
                        Text(
                            text = if (isIndonesian) "Urutkan:" else "Sort:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(end = 2.dp, top = 6.dp)
                        )
                    }
                    val sortChips = listOf(
                        "QTY_DESC" to (if (isIndonesian) "Stok Terbanyak" else "Stock: High-Low"),
                        "QTY_ASC" to (if (isIndonesian) "Stok Tersedikit" else "Stock: Low-High"),
                        "NAME_ASC" to (if (isIndonesian) "Nama A-Z" else "Name A-Z"),
                        "NAME_DESC" to (if (isIndonesian) "Nama Z-A" else "Name Z-A"),
                        "STYLE_ASC" to (if (isIndonesian) "Model A-Z" else "Style A-Z")
                    )
                    items(sortChips) { (key, label) ->
                        val isSelected = selectedSort == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSort = key },
                            label = { Text(label, fontSize = 11.sp) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2D3748),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Empty state or Lazy Column with Items
            if (displayedJackets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val activeName = when {
                        selectedCategoryFilter != "ALL" && selectedStyleFilter != "ALL" -> "$selectedCategoryFilter • $selectedStyleFilter"
                        selectedCategoryFilter != "ALL" -> selectedCategoryFilter
                        selectedStyleFilter != "ALL" -> selectedStyleFilter
                        selectedFilterTab != "ALL" -> selectedFilterTab
                        searchQuery.isNotBlank() -> searchQuery
                        else -> ""
                    }
                    EmptyInventoryView(
                        isDatabaseEmpty = allJackets.isEmpty(),
                        isIndonesian = isIndonesian,
                        activeFilterName = activeName,
                        onAddJacketClick = onNavigateToAdd,
                        onLoadSampleStock = { viewModel.seedSampleStock() },
                        onResetFilter = {
                            selectedFilterTab = "ALL"
                            selectedCategoryFilter = "ALL"
                            selectedStyleFilter = "ALL"
                            searchQuery = ""
                        }
                    )
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("inventory_grid_view"),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(displayedJackets, key = { it.id }) { jacket ->
                            JacketGridCard(
                                jacket = jacket,
                                isIndonesian = isIndonesian,
                                onAdjustQuantity = { delta -> viewModel.adjustQuantity(jacket.id, delta) },
                                onRestock = { viewModel.restock(jacket.id) },
                                onEditImage = { viewModel.openImagePickerForJacket(jacket) },
                                onDelete = { jacketToDelete = jacket },
                                onClick = { selectedJacketForDetails = jacket }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("inventory_list_view"),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(displayedJackets, key = { it.id }) { jacket ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedJacketForDetails = jacket }
                                .testTag("inventory_full_item_${jacket.sku}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail with interactive photo picker trigger
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.openImagePickerForJacket(jacket) }
                                            .testTag("thumbnail_sku_${jacket.sku}")
                                    ) {
                                        NetworkImageWithFallback(
                                            imageUrl = jacket.imageUrl,
                                            contentDescription = jacket.name,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(3.dp)
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xD91E293B)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = "Ubah Gambar SKU",
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = jacket.sku,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B)
                                            )

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { viewModel.openImagePickerForJacket(jacket) },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .testTag("btn_edit_image_${jacket.sku}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AddPhotoAlternate,
                                                        contentDescription = "Ubah Gambar SKU",
                                                        tint = Color(0xFF2563EB),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { jacketToDelete = jacket },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = jacket.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1A1A1A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            modifier = Modifier.padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${jacket.genderCategory} • ${jacket.style} • ${jacket.size} • ${jacket.locationRack}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }

                                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                                // Stock Adjustment Stepper Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isIndonesian) "Stok:" else "Stock:",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )

                                        IconButton(
                                            onClick = { viewModel.adjustQuantity(jacket.id, -1) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF1F5F9))
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFF1E293B), modifier = Modifier.size(14.dp))
                                        }

                                        Text(
                                            text = "${jacket.quantity}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )

                                        IconButton(
                                            onClick = { viewModel.adjustQuantity(jacket.id, 1) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF1F5F9))
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF1E293B), modifier = Modifier.size(14.dp))
                                        }
                                    }

                                    if (jacket.status != StockStatusLevel.IN_STOCK) {
                                        FilledTonalButton(
                                            onClick = { viewModel.restock(jacket.id) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = Color(0xFF2D3748),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+15 Restock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Delete Confirmation Dialog
    if (jacketToDelete != null) {
        val item = jacketToDelete!!
        AlertDialog(
            onDismissRequest = { jacketToDelete = null },
            title = {
                Text(
                    text = if (isIndonesian) "Hapus Jaket?" else "Delete Jacket?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isIndonesian) {
                        "Apakah Anda yakin ingin menghapus '${item.name}' (${item.sku}) dari database?"
                    } else {
                        "Are you sure you want to delete '${item.name}' (${item.sku}) from the database?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteJacket(item.id)
                        jacketToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isIndonesian) "Hapus" else "Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { jacketToDelete = null }) {
                    Text(if (isIndonesian) "Batal" else "Cancel")
                }
            }
        )
    }

    if (isExportSheetOpen) {
        ExportCsvBottomSheet(
            allJackets = allJackets,
            currentFilteredJackets = displayedJackets,
            isIndonesian = isIndonesian,
            onDismiss = { isExportSheetOpen = false }
        )
    }

    selectedJacketForDetails?.let { detailedJacket ->
        JacketDetailModal(
            jacket = detailedJacket,
            allJackets = allJackets,
            isIndonesian = isIndonesian,
            onDismiss = { selectedJacketForDetails = null },
            onAdjustQuantity = { id, delta -> viewModel.adjustQuantity(id, delta) },
            onRestock = { id -> viewModel.restock(id) },
            onEditImage = { item -> viewModel.openImagePickerForJacket(item) },
            onDelete = { item -> jacketToDelete = item },
            onSelectVariant = { newVariant -> selectedJacketForDetails = newVariant }
        )
    }

    if (isScannerOpen) {
        QrCodeScannerModal(
            allJackets = allJackets,
            isIndonesian = isIndonesian,
            onDismiss = { isScannerOpen = false },
            onJacketIdentified = { identifiedJacket ->
                isScannerOpen = false
                selectedJacketForDetails = identifiedJacket
            },
            onAddNewWithSku = { scannedSku ->
                isScannerOpen = false
                viewModel.navigateToAddJacketWithSku(scannedSku)
            }
        )
    }

    if (isAddSheetOpen) {
        AddJacketSheet(
            isIndonesian = isIndonesian,
            existingJackets = allJackets,
            onDismiss = { isAddSheetOpen = false },
            onSave = { sku, name, style, size, material, color, quantity, locationRack, isSeasonal ->
                viewModel.addNewJacket(
                    sku = sku,
                    name = name,
                    style = style,
                    size = size,
                    material = material,
                    color = color,
                    quantity = quantity,
                    locationRack = locationRack,
                    isSeasonal = isSeasonal
                )
                isAddSheetOpen = false
            }
        )
    }
}
