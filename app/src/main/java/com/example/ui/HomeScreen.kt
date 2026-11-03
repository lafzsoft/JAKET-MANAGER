package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import com.example.ui.components.*
import com.example.ui.dialogs.AddJacketSheet
import com.example.ui.dialogs.EditJacketSheet
import com.example.ui.dialogs.InventoryListSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JacketViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allJackets by viewModel.allJackets.collectAsStateWithLifecycle()
    val filteredJackets by viewModel.filteredJackets.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val isAddSheetOpen by viewModel.isAddSheetOpen.collectAsStateWithLifecycle()
    val isFilterSheetOpen by viewModel.isFilterSheetOpen.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isIndonesian by viewModel.isIndonesian.collectAsStateWithLifecycle()

    var jacketToDelete by remember { mutableStateOf<JacketItem?>(null) }
    var jacketToEdit by remember { mutableStateOf<JacketItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedStyleFilter by remember { mutableStateOf("ALL") }
    var selectedSortOrder by remember { mutableStateOf(JacketSortOrder.STYLE_ASC) }
    var homeSearchQuery by remember { mutableStateOf("") }

    val displayedJackets = remember(allJackets, selectedCategoryFilter, selectedStyleFilter, selectedSortOrder, homeSearchQuery) {
        var filtered = allJackets
        if (selectedCategoryFilter != "ALL") {
            filtered = filtered.filter { it.genderCategory.equals(selectedCategoryFilter, ignoreCase = true) }
        }
        if (selectedStyleFilter != "ALL") {
            filtered = filtered.filter { it.style.equals(selectedStyleFilter, ignoreCase = true) }
        }
        if (homeSearchQuery.isNotBlank()) {
            val q = homeSearchQuery.trim()
            filtered = filtered.filter {
                it.name.contains(q, ignoreCase = true) ||
                it.sku.contains(q, ignoreCase = true) ||
                it.style.contains(q, ignoreCase = true) ||
                it.material.contains(q, ignoreCase = true) ||
                it.color.contains(q, ignoreCase = true) ||
                it.locationRack.contains(q, ignoreCase = true)
            }
        }
        when (selectedSortOrder) {
            JacketSortOrder.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            JacketSortOrder.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            JacketSortOrder.QTY_DESC -> filtered.sortedByDescending { it.quantity }
            JacketSortOrder.QTY_ASC -> filtered.sortedBy { it.quantity }
            JacketSortOrder.STYLE_ASC -> filtered.sortedWith(compareBy({ it.style }, { it.name }))
            JacketSortOrder.STYLE_DESC -> filtered.sortedWith(compareByDescending<JacketItem> { it.style }.thenBy { it.name })
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isIndonesian) "Tambah Jaket Baru" else "Add New Jacket"
                    )
                },
                text = {
                    Text(
                        text = if (isIndonesian) "Tambah Jaket" else "Add Jacket",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("home_fab_add_jacket")
                    .navigationBarsPadding()
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        // Main LazyColumn displaying catalog sections and the list of added jackets from database
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .testTag("added_jackets_lazy_column")
        ) {
            // Summary Card at the top calculating and displaying total jackets in stock
            item(key = "inventory_summary_section") {
                InventorySummaryCard(
                    totalUnits = allJackets.sumOf { it.quantity },
                    totalModels = allJackets.size,
                    uniqueVariantsCount = allJackets.map { it.sku.ifBlank { it.id } }.distinct().size,
                    inStockCount = allJackets.count { it.status == StockStatusLevel.IN_STOCK },
                    lowStockCount = allJackets.count { it.status == StockStatusLevel.LOW_STOCK },
                    outOfStockCount = allJackets.count { it.status == StockStatusLevel.OUT_OF_STOCK },
                    isIndonesian = isIndonesian,
                    onAddClick = { viewModel.navigateToAddJacket() },
                    onProductionClick = { viewModel.navigateToProduction() },
                    onExportClick = { viewModel.openExportSheet() },
                    onLanguageToggle = { viewModel.toggleLanguage() },
                    onMenuClick = onOpenDrawer
                )
            }

            // [HERO: SPLIT] Opens directly with hero area, no top app bar or header
            item(key = "hero_section") {
                HeroSplitSection(
                    isIndonesian = isIndonesian,
                    totalJackets = allJackets.sumOf { it.quantity },
                    onAddClick = { viewModel.navigateToAddJacket() },
                    onLanguageToggle = { viewModel.toggleLanguage() }
                )
            }

            // [DEMOGRAPHIC CATEGORIES] Jaket Pria, Jaket Wanita, Jaket Anak, Jaket Unisex
            item(key = "demographic_categories_section") {
                DemographicCategoriesSection(
                    allJackets = allJackets,
                    selectedCategory = selectedCategoryFilter,
                    isIndonesian = isIndonesian,
                    onCategoryClick = { cat ->
                        selectedCategoryFilter = if (cat.titleId == "ALL" || selectedCategoryFilter.equals(cat.titleId, ignoreCase = true)) {
                            "ALL"
                        } else {
                            cat.titleId
                        }
                    }
                )
            }

            // [LAYOUT: CAROUSEL] [CARD_SHAPE: SQUARE] Jacket Styles
            item(key = "styles_section") {
                StylesCarouselSection(
                    isIndonesian = isIndonesian,
                    onStyleClick = { style -> viewModel.openFilterByStyle(style) }
                )
            }

            // [LAYOUT: GRID_2COL] [CARD_SHAPE: SQUARE] Jacket Sizes
            item(key = "sizes_section") {
                SizesGridSection(
                    isIndonesian = isIndonesian,
                    onSizeClick = { size -> viewModel.openFilterBySize(size) }
                )
            }

            // [LAYOUT: VERTICAL_LIST] [CARD_SHAPE: LANDSCAPE] Stock Status
            item(key = "stock_status_section") {
                StockStatusListSection(
                    isIndonesian = isIndonesian,
                    onStatusClick = { statusCat -> viewModel.openFilterByStatus(statusCat) }
                )
            }

            // [LAYOUT: FEATURE_BANNER] [CARD_SHAPE: BANNER_FULL] Seasonal Collection
            item(key = "seasonal_banner_section") {
                SeasonalBannerSection(
                    isIndonesian = isIndonesian,
                    onBannerClick = { viewModel.openFilterSeasonal() }
                )
            }

            // [LAYOUT: GRID_3COL] [CARD_SHAPE: CIRCLE] Jacket Materials
            item(key = "materials_section") {
                MaterialsGridSection(
                    isIndonesian = isIndonesian,
                    onMaterialClick = { mat -> viewModel.openFilterByMaterial(mat) }
                )
            }

            // [LAYOUT: CAROUSEL] [CARD_SHAPE: PORTRAIT] Jacket Colors
            item(key = "colors_section") {
                ColorsCarouselSection(
                    isIndonesian = isIndonesian,
                    onColorClick = { color -> viewModel.openFilterByColor(color) }
                )
            }

            // SECTION HEADER: Added Jackets from Database & Search Bar
            item(key = "db_inventory_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isIndonesian) "Inventaris Jaket (Database)" else "Added Jackets Inventory (Database)",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = if (isIndonesian) "Cari nama atau kode SKU produk di bawah" else "Search by product name or SKU code below",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Badge showing total models count
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2D3748))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (selectedStyleFilter == "ALL" && selectedCategoryFilter == "ALL" && homeSearchQuery.isBlank()) {
                                    if (isIndonesian) "${allJackets.size} Model" else "${allJackets.size} Items"
                                } else {
                                    "${displayedJackets.size} / ${allJackets.size}"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Kolom Pencarian (Search Bar) di bagian atas daftar jaket
                    OutlinedTextField(
                        value = homeSearchQuery,
                        onValueChange = { homeSearchQuery = it },
                        placeholder = {
                            Text(
                                text = if (isIndonesian) "Cari berdasarkan nama jaket atau kode SKU..." else "Search by jacket name or SKU code...",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Pencarian",
                                tint = Color(0xFF475569)
                            )
                        },
                        trailingIcon = {
                            if (homeSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { homeSearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Hapus pencarian",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_bar_input")
                    )
                }
            }

            // Style & Category Filter Tab Bar & Dropdowns
            item(key = "style_filter_section") {
                StyleFilterBar(
                    allJackets = allJackets,
                    selectedStyle = selectedStyleFilter,
                    selectedCategory = selectedCategoryFilter,
                    sortOrder = selectedSortOrder,
                    isIndonesian = isIndonesian,
                    onStyleSelected = { selectedStyleFilter = it },
                    onCategorySelected = { selectedCategoryFilter = it },
                    onSortSelected = { selectedSortOrder = it }
                )
            }

            // Empty state if database is empty
            if (allJackets.isEmpty()) {
                item(key = "empty_inventory") {
                    EmptyInventoryView(
                        isDatabaseEmpty = true,
                        isIndonesian = isIndonesian,
                        onAddJacketClick = { viewModel.navigateToAddJacket() },
                        onLoadSampleStock = { viewModel.seedSampleStock() }
                    )
                }
            } else if (displayedJackets.isEmpty()) {
                item(key = "empty_filtered_inventory") {
                    val filterName = when {
                        homeSearchQuery.isNotBlank() && selectedCategoryFilter != "ALL" -> "\"$homeSearchQuery\" ($selectedCategoryFilter)"
                        homeSearchQuery.isNotBlank() -> "\"$homeSearchQuery\""
                        selectedCategoryFilter != "ALL" && selectedStyleFilter != "ALL" -> "$selectedCategoryFilter • $selectedStyleFilter"
                        selectedCategoryFilter != "ALL" -> selectedCategoryFilter
                        else -> selectedStyleFilter
                    }
                    EmptyInventoryView(
                        isDatabaseEmpty = false,
                        isIndonesian = isIndonesian,
                        activeFilterName = filterName,
                        onResetFilter = {
                            homeSearchQuery = ""
                            selectedCategoryFilter = "ALL"
                            selectedStyleFilter = "ALL"
                        }
                    )
                }
            } else {
                // LAZY LIST ITEMS: Each jacket pulled from database with Swipe-to-Delete and Delete Button
                items(
                    items = displayedJackets,
                    key = { it.id }
                ) { jacket ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                jacketToDelete = jacket
                                false // Handled via confirmation dialog or delete action
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                Color(0xFFDC2626)
                            } else {
                                Color(0xFFEF4444).copy(alpha = 0.85f)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color)
                                    .padding(end = 22.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isIndonesian) "Hapus" else "Delete",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Jacket",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("swipe_to_delete_${jacket.sku}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            DatabaseJacketCard(
                                jacket = jacket,
                                isIndonesian = isIndonesian,
                                onAdjustQty = { delta -> viewModel.adjustQuantity(jacket.id, delta) },
                                onRestock = { viewModel.restock(jacket.id) },
                                onDelete = { jacketToDelete = jacket },
                                onEdit = { jacketToEdit = jacket },
                                onEditImage = { viewModel.openImagePickerForJacket(jacket) }
                            )
                        }
                    }
                }
            }

            // Bottom Spacers to prevent touching bottom edge of viewport
            item(key = "bottom_spacing") {
                Spacer(modifier = Modifier.height(36.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }

    // Confirmation Alert Dialog for Deletion
    if (jacketToDelete != null) {
        val target = jacketToDelete!!
        AlertDialog(
            onDismissRequest = { jacketToDelete = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isIndonesian) "Hapus dari Inventaris?" else "Remove from Inventory?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A)
                )
            },
            text = {
                Text(
                    text = if (isIndonesian) {
                        "Apakah Anda yakin ingin menghapus '${target.name}' (${target.sku}) dari database inventaris?"
                    } else {
                        "Are you sure you want to remove '${target.name}' (${target.sku}) from the warehouse inventory?"
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF4A5568)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val removed = target
                        viewModel.deleteJacket(removed.id)
                        jacketToDelete = null
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = if (isIndonesian) "'${removed.name}' dihapus dari gudang" else "'${removed.name}' removed from inventory",
                                actionLabel = if (isIndonesian) "Batal" else "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.addNewJacket(
                                    sku = removed.sku,
                                    name = removed.name,
                                    style = removed.style,
                                    size = removed.size,
                                    material = removed.material,
                                    color = removed.color,
                                    quantity = removed.quantity,
                                    locationRack = removed.locationRack,
                                    isSeasonal = removed.isSeasonal
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(
                        text = if (isIndonesian) "Hapus" else "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { jacketToDelete = null },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A5568)),
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text(text = if (isIndonesian) "Batal" else "Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal Sheet for adding a new jacket (quick fallback)
    if (isAddSheetOpen) {
        AddJacketSheet(
            isIndonesian = isIndonesian,
            existingJackets = allJackets,
            onDismiss = { viewModel.closeAddSheet() },
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
            }
        )
    }

    // Modal Sheet for category-filtered warehouse list
    if (isFilterSheetOpen) {
        InventoryListSheet(
            filter = activeFilter,
            items = filteredJackets,
            searchQuery = searchQuery,
            isIndonesian = isIndonesian,
            onSearchChange = { viewModel.setSearchQuery(it) },
            onAdjustQty = { id, delta -> viewModel.adjustQuantity(id, delta) },
            onRestock = { id -> viewModel.restock(id) },
            onDismiss = { viewModel.closeFilterSheet() }
        )
    }

    // Modal Bottom Sheet to edit jacket pre-filled with existing details
    jacketToEdit?.let { targetJacket ->
        EditJacketSheet(
            jacket = targetJacket,
            isIndonesian = isIndonesian,
            existingJackets = allJackets,
            onDismiss = { jacketToEdit = null },
            onSaveUpdate = { id, sku, name, style, size, material, color, quantity, locationRack, isSeasonal ->
                viewModel.updateJacketDetails(
                    id = id,
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
                jacketToEdit = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = if (isIndonesian)
                            "✅ Data jaket '$name' berhasil diperbarui di Room DB!"
                        else
                            "✅ Jacket '$name' updated in Room DB!",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }
}

@Composable
private fun DatabaseJacketCard(
    jacket: JacketItem,
    isIndonesian: Boolean,
    onAdjustQty: (Int) -> Unit,
    onRestock: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onEditImage: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (statusLabel, statusColor, statusBg) = when (jacket.status) {
        StockStatusLevel.IN_STOCK -> Triple(
            if (isIndonesian) "Stok Tersedia" else "In Stock",
            Color(0xFF16A34A),
            Color(0xFFDCFCE7)
        )
        StockStatusLevel.LOW_STOCK -> Triple(
            if (isIndonesian) "Stok Sedikit" else "Low Stock",
            Color(0xFFD97706),
            Color(0xFFFEF3C7)
        )
        StockStatusLevel.OUT_OF_STOCK -> Triple(
            if (isIndonesian) "Habis Stok" else "Out of Stock",
            Color(0xFFDC2626),
            Color(0xFFFEE2E2)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("database_jacket_card_${jacket.sku}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Jacket Thumbnail with photo edit trigger
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (onEditImage != null) Modifier.clickable { onEditImage() } else Modifier
                        )
                ) {
                    NetworkImageWithFallback(
                        imageUrl = jacket.imageUrl,
                        contentDescription = jacket.name,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (onEditImage != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
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
                }

                // Info Column
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (onEditImage != null) {
                                IconButton(
                                    onClick = onEditImage,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF))
                                        .testTag("btn_edit_image_home_${jacket.sku}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Ubah Gambar",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }

                            // Edit Button to open EditJacketSheet
                            if (onEdit != null) {
                                IconButton(
                                    onClick = onEdit,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF))
                                        .testTag("edit_jacket_button_${jacket.sku}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Jacket",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            // Explicit Delete Button
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2))
                                    .testTag("delete_jacket_button_${jacket.sku}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Jacket",
                                    tint = Color(0xFFDC2626),
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
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Specs row: Style, Size, Color, Location Rack
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpecTag(jacket.style)
                        SpecTag(jacket.size)
                        SpecTag(jacket.color)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = jacket.locationRack,
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Stock Controls: - / + stepper and Restock button
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )

                    IconButton(
                        onClick = { onAdjustQty(-1) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${jacket.quantity}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.widthIn(min = 28.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    IconButton(
                        onClick = { onAdjustQty(1) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (jacket.status != StockStatusLevel.IN_STOCK) {
                    FilledTonalButton(
                        onClick = onRestock,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF2D3748),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isIndonesian) "+15 Restock" else "+15 Restock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF475569)
        )
    }
}
