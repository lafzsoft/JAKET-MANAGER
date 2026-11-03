package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.*
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
import com.example.model.DailyProductionItem
import com.example.model.ProductionShift
import com.example.model.ProductionStatus
import com.example.ui.dialogs.AddProductionDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyProductionScreen(
    viewModel: JacketViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    val allJackets by viewModel.allJackets.collectAsStateWithLifecycle()
    val filteredProductions by viewModel.filteredProductions.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val currentShiftFilter by viewModel.productionShiftFilter.collectAsStateWithLifecycle()
    val isAddSheetOpen by viewModel.isAddProductionSheetOpen.collectAsStateWithLifecycle()
    val isIndonesian by viewModel.isIndonesian.collectAsStateWithLifecycle()

    var productionToDelete by remember { mutableStateOf<DailyProductionItem?>(null) }
    var isEditTargetDialogOpen by remember { mutableStateOf(false) }
    var newTargetInput by remember { mutableStateOf("${dailySummary.dailyTarget}") }

    val formattedToday = remember(isIndonesian) {
        val locale = if (isIndonesian) Locale("id", "ID") else Locale.US
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
        formatter.format(Date())
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_production_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isIndonesian) "Produksi Harian Jaket" else "Daily Jacket Production",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = formattedToday,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("production_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1A1A1A)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isEditTargetDialogOpen = true },
                        modifier = Modifier.testTag("edit_daily_target_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = "Edit Target",
                            tint = Color(0xFF2D3748)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("production_language_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = Color(0xFF2D3748)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddProductionSheet() },
                containerColor = Color(0xFF2D3748),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (isIndonesian) "Catat Produksi" else "Log Production", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("fab_add_production")
                    .padding(bottom = 8.dp)
            )
        },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            // KPI Summary Card 1: Today's Target & Output
            item(key = "production_kpi_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF334155)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PrecisionManufacturing,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isIndonesian) "Output Hari Ini" else "Today's Output",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${dailySummary.totalAcceptedToday} / ${dailySummary.dailyTarget} pcs",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Percentage Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (dailySummary.targetFulfillmentPercentage >= 100f) Color(0xFF16A34A)
                                        else Color(0xFF0284C7)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${dailySummary.targetFulfillmentPercentage.toInt()}% " +
                                            if (dailySummary.targetFulfillmentPercentage >= 100f) {
                                                if (isIndonesian) "Tercapai" else "Done"
                                            } else {
                                                if (isIndonesian) "Target" else "Target"
                                            },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Target Progress Bar
                        LinearProgressIndicator(
                            progress = { (dailySummary.targetFulfillmentPercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (dailySummary.targetFulfillmentPercentage >= 100f) Color(0xFF10B981) else Color(0xFF38BDF8),
                            trackColor = Color(0xFF334155)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color(0xFF334155), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Mini metrics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isIndonesian) "Total Dihasilkan" else "Total Made",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "${dailySummary.totalProducedToday} pcs",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column {
                                Text(
                                    text = if (isIndonesian) "Lolos QC (Bagus)" else "Passed QC",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "${dailySummary.totalAcceptedToday} pcs",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4ADE80)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isIndonesian) "Cacat / Reject" else "Defects",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "${dailySummary.totalDefectsToday} pcs (${"%.1f".format(dailySummary.averageDefectRate)}%)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dailySummary.totalDefectsToday > 0) Color(0xFFF87171) else Color.White
                                )
                            }

                            Column {
                                Text(
                                    text = if (isIndonesian) "Total Batch" else "Total Batches",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "${dailySummary.totalBatchesToday} lot",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Quick Info Banner for Stock Sync
            item(key = "stock_sync_banner") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBBF7D0)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isIndonesian) "Sinkronisasi Stok Otomatis" else "Automatic Stock Sync",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = if (isIndonesian) {
                                    "Setiap batch jaket yang selesai diproduksi otomatis menambah stok fisik di gudang."
                                } else {
                                    "Finished batches directly increment available warehouse inventory levels."
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }

            // Shift Filter Bar
            item(key = "shift_filter_bar") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIndonesian) "Daftar Batch Produksi" else "Production Batch Logs",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        Text(
                            text = "${filteredProductions.size} " + if (isIndonesian) "Batch" else "Batches",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val shiftFilters = listOf(
                            "ALL" to (if (isIndonesian) "Semua Shift" else "All Shifts"),
                            "MORNING" to (if (isIndonesian) "Shift Pagi" else "Morning Shift"),
                            "AFTERNOON" to (if (isIndonesian) "Shift Siang" else "Afternoon Shift"),
                            "NIGHT" to (if (isIndonesian) "Shift Malam" else "Night Shift")
                        )

                        items(shiftFilters) { (key, label) ->
                            val isSelected = currentShiftFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setProductionShiftFilter(key) },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2D3748),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_shift_$key")
                            )
                        }
                    }
                }
            }

            // Empty state if no productions
            if (filteredProductions.isEmpty()) {
                item(key = "empty_production_state") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PrecisionManufacturing,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (isIndonesian) "Belum Ada Catatan Produksi" else "No Production Batches Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isIndonesian) {
                                    "Catat hasil penjahitan jaket hari ini untuk memonitor kuota harian dan mutu pengerjaan."
                                } else {
                                    "Log finished jacket tailoring batches to monitor daily quotas and quality."
                                },
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.openAddProductionSheet() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3748))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isIndonesian) "Catat Batch Pertama" else "Log First Batch")
                            }
                        }
                    }
                }
            } else {
                // Production Batch Items List
                items(
                    items = filteredProductions,
                    key = { it.id }
                ) { item ->
                    ProductionBatchCard(
                        production = item,
                        isIndonesian = isIndonesian,
                        onDelete = { productionToDelete = item }
                    )
                }
            }
        }
    }

    // Add Production Bottom Sheet
    if (isAddSheetOpen) {
        AddProductionDialog(
            jackets = allJackets,
            isIndonesian = isIndonesian,
            onDismiss = { viewModel.closeAddProductionSheet() },
            onSave = { jacketId, jacketName, jacketSku, batchNumber, shift, operatorName, quantityProduced, defectCount, status, notes, addedToStock ->
                viewModel.addDailyProduction(
                    jacketId = jacketId,
                    jacketName = jacketName,
                    jacketSku = jacketSku,
                    batchNumber = batchNumber,
                    shift = shift,
                    operatorName = operatorName,
                    quantityProduced = quantityProduced,
                    defectCount = defectCount,
                    status = status,
                    notes = notes,
                    addedToStock = addedToStock
                )
            }
        )
    }

    // Edit Target Dialog
    if (isEditTargetDialogOpen) {
        AlertDialog(
            onDismissRequest = { isEditTargetDialogOpen = false },
            title = {
                Text(
                    text = if (isIndonesian) "Ubah Target Produksi Harian" else "Edit Daily Production Target",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isIndonesian) "Masukkan kuota target jaket yang harus diselesaikan dalam sehari:" else "Enter the target quota of jackets to finish today:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newTargetInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) newTargetInput = it },
                        label = { Text(if (isIndonesian) "Target Harian (pcs)" else "Daily Target (pcs)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_input_field"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = newTargetInput.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            viewModel.updateDailyTarget(parsed)
                        }
                        isEditTargetDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3748)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_target_button")
                ) {
                    Text(if (isIndonesian) "Simpan" else "Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { isEditTargetDialogOpen = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isIndonesian) "Batal" else "Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete Confirmation Dialog
    if (productionToDelete != null) {
        val target = productionToDelete!!
        AlertDialog(
            onDismissRequest = { productionToDelete = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isIndonesian) "Hapus Catatan Batch?" else "Delete Batch Record?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isIndonesian) {
                        "Apakah Anda yakin ingin menghapus catatan batch '${target.batchNumber}' (${target.jacketName})?"
                    } else {
                        "Are you sure you want to delete batch record '${target.batchNumber}' (${target.jacketName})?"
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDailyProduction(target.id)
                        productionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_production_button")
                ) {
                    Text(if (isIndonesian) "Hapus" else "Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { productionToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isIndonesian) "Batal" else "Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ProductionBatchCard(
    production: DailyProductionItem,
    isIndonesian: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusLabel, statusColor, statusBg) = when (production.status) {
        ProductionStatus.COMPLETED -> Triple(
            if (isIndonesian) "Selesai" else "Finished",
            Color(0xFF16A34A),
            Color(0xFFDCFCE7)
        )
        ProductionStatus.IN_PROGRESS -> Triple(
            if (isIndonesian) "Sedang Jahit" else "In Progress",
            Color(0xFF0284C7),
            Color(0xFFE0F2FE)
        )
        ProductionStatus.QUALITY_CHECK -> Triple(
            if (isIndonesian) "Pemeriksaan QC" else "QC Review",
            Color(0xFFD97706),
            Color(0xFFFEF3C7)
        )
    }

    val shiftLabel = when (production.shift) {
        ProductionShift.MORNING -> if (isIndonesian) "Shift Pagi" else "Morning"
        ProductionShift.AFTERNOON -> if (isIndonesian) "Shift Siang" else "Afternoon"
        ProductionShift.NIGHT -> if (isIndonesian) "Shift Malam" else "Night"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("production_card_${production.batchNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Batch Number, Shift, Status, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = production.batchNumber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    // Shift Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = shiftLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                    }

                    // Status Pill
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
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2))
                        .testTag("delete_production_btn_${production.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Jacket Model & SKU
            Text(
                text = production.jacketName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (production.jacketSku.isNotBlank()) {
                Text(
                    text = "SKU: ${production.jacketSku}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Production numbers and operator row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Operator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = production.operatorName,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Output metrics
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Produced
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "+${production.quantityProduced} pcs",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }

                    // Defect if any
                    if (production.defectCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "-${production.defectCount} defect",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            // Notes if any
            if (production.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"${production.notes}\"",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
