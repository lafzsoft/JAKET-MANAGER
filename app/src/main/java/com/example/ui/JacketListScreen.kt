package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.ui.components.NetworkImageWithFallback
import com.example.ui.dialogs.AddJacketSheet
import com.example.ui.dialogs.EditJacketSheet
import com.example.ui.dialogs.JacketDetailModal
import kotlinx.coroutines.launch

/**
 * Jetpack Compose screen that observes the Room database and displays all jackets
 * in a LazyColumn with real-time stock status, quantities, safety thresholds, and quick stock adjusters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JacketListScreen(
    viewModel: JacketViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    // Observing Room Database via ViewModel StateFlow
    val allJackets by viewModel.allJackets.collectAsStateWithLifecycle()
    val isIndonesian by viewModel.isIndonesian.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedJacketForDetails by remember { mutableStateOf<JacketItem?>(null) }
    var isAddModalBottomSheetOpen by remember { mutableStateOf(false) }
    var jacketToDelete by remember { mutableStateOf<JacketItem?>(null) }
    var jacketToEdit by remember { mutableStateOf<JacketItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Summary calculations from Room database
    val totalStock = remember(allJackets) { allJackets.sumOf { it.quantity } }
    val inStockList = remember(allJackets) { allJackets.filter { it.status == StockStatusLevel.IN_STOCK } }
    val lowStockList = remember(allJackets) { allJackets.filter { it.status == StockStatusLevel.LOW_STOCK } }
    val outOfStockList = remember(allJackets) { allJackets.filter { it.status == StockStatusLevel.OUT_OF_STOCK } }

    // Filter jackets based on status and search query
    val displayedJackets = remember(allJackets, selectedStatusFilter, searchQuery) {
        var list = when (selectedStatusFilter) {
            "IN_STOCK" -> inStockList
            "LOW_STOCK" -> lowStockList
            "OUT_OF_STOCK" -> outOfStockList
            else -> allJackets
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.style.lowercase().contains(q) ||
                it.sku.lowercase().contains(q) ||
                it.locationRack.lowercase().contains(q) ||
                it.color.lowercase().contains(q)
            }
        }
        list
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("jacket_list_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isIndonesian) "Status Stok Jaket (Room)" else "Jacket Stock Status (Room)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                text = if (isIndonesian)
                                    "Mengamati Room DB • ${allJackets.size} jaket aktif"
                                else
                                    "Observing Room DB • ${allJackets.size} jackets active",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_from_jacket_list")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isAddModalBottomSheetOpen = true },
                        modifier = Modifier.testTag("btn_add_from_jacket_list")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Jacket",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isAddModalBottomSheetOpen = true },
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
                    .testTag("fab_add_jacket")
                    .navigationBarsPadding()
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Room Metrics Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("room_stock_metrics_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
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
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isIndonesian) "Database Lokal Room" else "Room Local Database",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF34D399))
                                )
                                Text(
                                    text = "Room Flow Active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$totalStock Unit",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = if (isIndonesian) "Total seluruh stok fisik di gudang" else "Total physical units in warehouse",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Batch restock button if low stock items exist
                        if (lowStockList.isNotEmpty() || outOfStockList.isNotEmpty()) {
                            FilledTonalButton(
                                onClick = { viewModel.restockAllLowStock() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF1E293B),
                                    contentColor = Color(0xFF38BDF8)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_restock_all_room")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isIndonesian) "Restock Semua" else "Restock All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Breakdown Mini Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StockMiniMetric(
                            label = if (isIndonesian) "Tersedia" else "In Stock",
                            count = inStockList.size,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        StockMiniMetric(
                            label = if (isIndonesian) "Menipis" else "Low Stock",
                            count = lowStockList.size,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        StockMiniMetric(
                            label = if (isIndonesian) "Habis" else "Out of Stock",
                            count = outOfStockList.size,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Search Bar & Filter Chips Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (isIndonesian) "Cari nama, SKU, atau model jaket..." else "Filter jackets by name, SKU...",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
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
                        .height(48.dp)
                        .testTag("jacket_list_search_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Filter Tabs: ALL, IN_STOCK, LOW_STOCK, OUT_OF_STOCK
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filterOptions = listOf(
                        "ALL" to (if (isIndonesian) "Semua (${allJackets.size})" else "All (${allJackets.size})"),
                        "IN_STOCK" to (if (isIndonesian) "Tersedia (${inStockList.size})" else "In Stock (${inStockList.size})"),
                        "LOW_STOCK" to (if (isIndonesian) "Menipis (${lowStockList.size})" else "Low Stock (${lowStockList.size})"),
                        "OUT_OF_STOCK" to (if (isIndonesian) "Habis (${outOfStockList.size})" else "Out of Stock (${outOfStockList.size})")
                    )

                    items(filterOptions) { (key, label) ->
                        val isSelected = selectedStatusFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatusFilter = key },
                            label = { Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E293B),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF475569)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF1E293B) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.testTag("filter_chip_${key.lowercase()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // THE CORE LAZYCOLUMN OBSERVING ROOM DATABASE
            if (displayedJackets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = if (isIndonesian) "Tidak ada jaket ditemukan" else "No jackets found in Room DB",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = if (isIndonesian) "Coba sesuaikan kata kunci pencarian atau filter status stok" else "Try adjusting search query or stock filter",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("room_jacket_lazy_column"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 48.dp)
                ) {
                    items(
                        items = displayedJackets,
                        key = { it.id }
                    ) { jacket ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    jacketToDelete = jacket
                                    false
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
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(color)
                                        .padding(end = 20.dp)
                                        .testTag("swipe_background_${jacket.id}"),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isIndonesian) "Hapus dari Room" else "Delete from Room",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("swipe_to_delete_box_${jacket.id}")
                        ) {
                            JacketStockRowCard(
                                jacket = jacket,
                                isIndonesian = isIndonesian,
                                onClick = { selectedJacketForDetails = jacket },
                                onAdjustQuantity = { delta ->
                                    viewModel.adjustQuantity(jacket.id, delta)
                                },
                                onRestock = {
                                    viewModel.restock(jacket.id)
                                },
                                onDelete = {
                                    jacketToDelete = jacket
                                },
                                onEdit = {
                                    jacketToEdit = jacket
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Details when item is clicked
    selectedJacketForDetails?.let { targetJacket ->
        JacketDetailModal(
            jacket = targetJacket,
            allJackets = allJackets,
            isIndonesian = isIndonesian,
            onDismiss = { selectedJacketForDetails = null },
            onAdjustQuantity = { id, delta -> viewModel.adjustQuantity(id, delta) },
            onRestock = { id -> viewModel.restock(id) },
            onEditImage = { item -> viewModel.openImagePickerForJacket(item) },
            onDelete = { item -> viewModel.deleteJacket(item.id) },
            onSelectVariant = { newVariant -> selectedJacketForDetails = newVariant }
        )
    }

    // Modal Bottom Sheet to input new jacket details and save to Room DB
    if (isAddModalBottomSheetOpen) {
        AddJacketSheet(
            isIndonesian = isIndonesian,
            existingJackets = allJackets,
            onDismiss = { isAddModalBottomSheetOpen = false },
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
                isAddModalBottomSheetOpen = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = if (isIndonesian)
                            "✅ Berhasil menyimpan '$name' ($style, Size $size, $quantity pcs) ke Room database!"
                        else
                            "✅ Successfully saved '$name' ($style, Size $size, $quantity pcs) to Room database!",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    // Confirmation Alert Dialog for Deletion from Room Database
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
                    text = if (isIndonesian) "Hapus dari Room DB?" else "Delete from Room DB?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A)
                )
            },
            text = {
                Text(
                    text = if (isIndonesian) {
                        "Apakah Anda yakin ingin menghapus '${target.name}' (${target.sku}, ${target.quantity} unit) dari database Room?"
                    } else {
                        "Are you sure you want to delete '${target.name}' (${target.sku}, ${target.quantity} units) from the Room database?"
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
                                message = if (isIndonesian)
                                    "🗑️ '${removed.name}' dihapus dari Room DB"
                                else
                                    "🗑️ '${removed.name}' removed from Room DB",
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

/**
 * Individual jacket item card inside the LazyColumn displaying stock status clearly.
 */
@Composable
private fun JacketStockRowCard(
    jacket: JacketItem,
    isIndonesian: Boolean,
    onClick: () -> Unit,
    onAdjustQuantity: (delta: Int) -> Unit,
    onRestock: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("jacket_stock_item_${jacket.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image
                Box(modifier = Modifier.size(72.dp)) {
                    NetworkImageWithFallback(
                        imageUrl = jacket.imageUrl,
                        contentDescription = jacket.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                    )

                    // Size Pill on image
                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(3.dp)
                    ) {
                        Text(
                            text = jacket.size,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Middle Details: Name, SKU, Category, Style, Location
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = jacket.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${jacket.sku} • ${jacket.style}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = jacket.locationRack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "${jacket.color} • ${jacket.material}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    }
                }

                // Right Column: STOCK STATUS BADGE & Quantity
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StockStatusBadge(
                        status = jacket.status,
                        isIndonesian = isIndonesian,
                        modifier = Modifier.testTag("stock_status_badge_${jacket.id}")
                    )

                    Text(
                        text = "${jacket.quantity} unit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (jacket.status) {
                            StockStatusLevel.IN_STOCK -> Color(0xFF047857)
                            StockStatusLevel.LOW_STOCK -> Color(0xFFB45309)
                            StockStatusLevel.OUT_OF_STOCK -> Color(0xFFB91C1C)
                        }
                    )

                    Text(
                        text = if (isIndonesian) "Min: ${jacket.minThreshold}" else "Safe: ${jacket.minThreshold}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Bar: Stock Health Progress Bar + Stepper Controls (+ / - / Restock)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock health bar
                val safeRatio = if (jacket.minThreshold > 0) {
                    (jacket.quantity.toFloat() / (jacket.minThreshold * 2).toFloat()).coerceIn(0f, 1f)
                } else 1f

                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    LinearProgressIndicator(
                        progress = { safeRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when (jacket.status) {
                            StockStatusLevel.IN_STOCK -> Color(0xFF10B981)
                            StockStatusLevel.LOW_STOCK -> Color(0xFFF59E0B)
                            StockStatusLevel.OUT_OF_STOCK -> Color(0xFFEF4444)
                        },
                        trackColor = Color(0xFFF1F5F9)
                    )
                }

                // Quick Increment / Decrement Stepper buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Restock action if low/out
                    if (jacket.status != StockStatusLevel.IN_STOCK) {
                        FilledTonalButton(
                            onClick = onRestock,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFECFDF5),
                                contentColor = Color(0xFF047857)
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_restock_${jacket.id}")
                        ) {
                            Text("+15", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Decrement button
                    OutlinedIconButton(
                        onClick = { onAdjustQuantity(-1) },
                        shape = RoundedCornerShape(8.dp),
                        enabled = jacket.quantity > 0,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_decrement_${jacket.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Increment button
                    OutlinedIconButton(
                        onClick = { onAdjustQuantity(1) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_increment_${jacket.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Edit button to open EditJacketSheet (48dp minimum touch target)
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_edit_${jacket.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isIndonesian) "Edit Jaket di Room DB" else "Edit Jacket in Room DB",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Delete button from Room database (48dp minimum touch target)
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_delete_${jacket.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = if (isIndonesian) "Hapus Jaket dari Room DB" else "Delete Jacket from Room DB",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Material 3 badge presenting the stock status of a jacket.
 */
@Composable
fun StockStatusBadge(
    status: StockStatusLevel,
    isIndonesian: Boolean,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, icon, text) = when (status) {
        StockStatusLevel.IN_STOCK -> Tuple5(
            Color(0xFFECFDF5),
            Color(0xFF047857),
            Color(0xFFA7F3D0),
            Icons.Default.CheckCircle,
            if (isIndonesian) "Tersedia" else "In Stock"
        )
        StockStatusLevel.LOW_STOCK -> Tuple5(
            Color(0xFFFFFBEB),
            Color(0xFFB45309),
            Color(0xFFFDE68A),
            Icons.Default.Warning,
            if (isIndonesian) "Menipis" else "Low Stock"
        )
        StockStatusLevel.OUT_OF_STOCK -> Tuple5(
            Color(0xFFFEF2F2),
            Color(0xFFB91C1C),
            Color(0xFFFECACA),
            Icons.Default.Cancel,
            if (isIndonesian) "Habis" else "Out of Stock"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun StockMiniMetric(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = "$count",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
