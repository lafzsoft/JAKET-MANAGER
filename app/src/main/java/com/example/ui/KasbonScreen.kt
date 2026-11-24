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
import com.example.model.KasbonItem
import com.example.model.KasbonStatus
import com.example.ui.components.CompactAppTopBar
import com.example.ui.dialogs.AddKasbonSheet
import com.example.ui.dialogs.RecordRepaymentDialog
import com.example.ui.theme.LocalAppThemeSpec
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasbonScreen(
    viewModel: JacketViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Hardware/gesture back handler to navigate back safely
    BackHandler {
        onNavigateBack()
    }

    val isIndonesian by viewModel.isIndonesian.collectAsStateWithLifecycle()
    val allKasbon by viewModel.allKasbon.collectAsStateWithLifecycle()
    val statusFilter by viewModel.kasbonStatusFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.kasbonSearchQuery.collectAsStateWithLifecycle()

    val isAddSheetOpen by viewModel.isAddKasbonSheetOpen.collectAsStateWithLifecycle()
    val selectedForRepayment by viewModel.selectedKasbonForRepayment.collectAsStateWithLifecycle()
    val selectedForEdit by viewModel.selectedKasbonForEdit.collectAsStateWithLifecycle()

    var kasbonToDelete by remember { mutableStateOf<KasbonItem?>(null) }

    // Summary calculations
    val totalActiveAdvance = remember(allKasbon) {
        allKasbon.filter { it.status != KasbonStatus.LUNAS }.sumOf { it.amount }
    }
    val totalRemainingDebt = remember(allKasbon) {
        allKasbon.sumOf { it.remainingAmount }
    }
    val totalPaidRepayments = remember(allKasbon) {
        allKasbon.sumOf { it.paidAmount }
    }
    val activeBorrowersCount = remember(allKasbon) {
        allKasbon.filter { it.status != KasbonStatus.LUNAS }.map { it.employeeName.trim().lowercase() }.distinct().size
    }

    // Filter & Search
    val filteredKasbon = remember(allKasbon, statusFilter, searchQuery) {
        var list = allKasbon
        if (statusFilter != "ALL") {
            list = list.filter { it.status.name == statusFilter }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.employeeName.lowercase().contains(q) ||
                it.role.lowercase().contains(q) ||
                it.purpose.lowercase().contains(q) ||
                it.repaymentMethod.lowercase().contains(q)
            }
        }
        list
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("kasbon_screen"),
        topBar = {
            val themeSpec = LocalAppThemeSpec.current
            CompactAppTopBar(
                title = if (isIndonesian) "Kasbon Karyawan" else "Employee Cash Advances",
                subtitle = if (isIndonesian) "Pencatatan pinjaman & potong gaji" else "Loans, advances & payroll",
                containerColor = themeSpec.cardBackground,
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_kasbon")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = themeSpec.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    if (onOpenDrawer != null) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu Drawer",
                                tint = themeSpec.labelPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = themeSpec.chipContainer
                        ) {
                            Text(
                                text = if (isIndonesian) "ID" else "EN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeSpec.labelPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddKasbonSheet() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        text = if (isIndonesian) "Catat Kasbon" else "New Advance",
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("fab_add_kasbon")
                    .navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SUMMARY CARDS (RUPIAH)
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sisa Piutang Kasbon
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDC2626)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    text = if (isIndonesian) "Sisa Piutang" else "Unpaid Balance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = CurrencyUtils.formatRupiah(totalRemainingDebt),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = if (isIndonesian) "$activeBorrowersCount karyawan aktif" else "$activeBorrowersCount active staff",
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    // Total Terlunasi
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    text = if (isIndonesian) "Total Terbayar" else "Total Paid",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = CurrencyUtils.formatRupiah(totalPaidRepayments),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF16A34A)
                            )
                            Text(
                                text = if (isIndonesian) "Total kasbon: ${CurrencyUtils.formatRupiah(totalActiveAdvance + totalPaidRepayments)}" else "Cumulative advances",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // SEARCH BAR
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setKasbonSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = if (isIndonesian) "Cari nama karyawan, jabatan, atau keperluan..." else "Search staff, role, or purpose...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setKasbonSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0F172A),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_kasbon_input")
                )
            }

            // FILTER TABS (STATUS CHIPS)
            item {
                val filters = listOf(
                    "ALL" to (if (isIndonesian) "Semua (${allKasbon.size})" else "All (${allKasbon.size})"),
                    KasbonStatus.BELUM_LUNAS.name to (if (isIndonesian) "Belum Lunas" else "Unpaid"),
                    KasbonStatus.CICILAN.name to (if (isIndonesian) "Sedang Dicicil" else "Partially Paid"),
                    KasbonStatus.LUNAS.name to (if (isIndonesian) "Lunas" else "Paid")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { (key, label) ->
                        val isSelected = statusFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setKasbonStatusFilter(key) },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0F172A),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF475569)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFFE2E8F0),
                                selectedBorderColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // LIST OF KASBON ITEMS
            if (filteredKasbon.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
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
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = if (isIndonesian) "Tidak Ada Catatan Kasbon" else "No Cash Advances Found",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isIndonesian) "Gunakan tombol '+ Catat Kasbon' di bawah untuk menambahkan pinjaman baru." else "Tap '+ New Advance' below to add a cash advance entry.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredKasbon, key = { it.id }) { item ->
                    KasbonCard(
                        item = item,
                        isIndonesian = isIndonesian,
                        onPayClick = { viewModel.openRepaymentDialog(item) },
                        onEditClick = { viewModel.openEditKasbonSheet(item) },
                        onDeleteClick = { kasbonToDelete = item }
                    )
                }
            }
        }
    }

    // MODAL BOTTOM SHEET: TAMBAH / EDIT KASBON
    if (isAddSheetOpen || selectedForEdit != null) {
        AddKasbonSheet(
            existingKasbon = selectedForEdit,
            isIndonesian = isIndonesian,
            onDismiss = {
                viewModel.closeAddKasbonSheet()
                viewModel.closeEditKasbonSheet()
            },
            onSave = { employeeName, role, amount, purpose, dueDate, repaymentMethod, notes ->
                if (selectedForEdit != null) {
                    val updated = selectedForEdit!!.copy(
                        employeeName = employeeName,
                        role = role,
                        amount = amount,
                        purpose = purpose,
                        dueDate = dueDate,
                        repaymentMethod = repaymentMethod,
                        notes = notes,
                        status = if (selectedForEdit!!.paidAmount >= amount) KasbonStatus.LUNAS
                                 else if (selectedForEdit!!.paidAmount > 0) KasbonStatus.CICILAN
                                 else KasbonStatus.BELUM_LUNAS
                    )
                    viewModel.updateKasbon(updated)
                } else {
                    viewModel.addKasbon(
                        employeeName = employeeName,
                        role = role,
                        amount = amount,
                        purpose = purpose,
                        dueDate = dueDate,
                        repaymentMethod = repaymentMethod,
                        notes = notes
                    )
                }
            }
        )
    }

    // DIALOG: BAYAR / CICIL KASBON
    selectedForRepayment?.let { kasbon ->
        RecordRepaymentDialog(
            kasbon = kasbon,
            isIndonesian = isIndonesian,
            onDismiss = { viewModel.closeRepaymentDialog() },
            onConfirmPayment = { amount ->
                viewModel.recordKasbonRepayment(kasbon.id, amount)
            }
        )
    }

    // DIALOG: KONFIRMASI HAPUS
    kasbonToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { kasbonToDelete = null },
            title = {
                Text(if (isIndonesian) "Hapus Catatan Kasbon?" else "Delete Cash Advance?")
            },
            text = {
                Text(
                    if (isIndonesian) {
                        "Apakah Anda yakin ingin menghapus data kasbon atas nama ${target.employeeName} sebesar ${target.amountFormatted}?"
                    } else {
                        "Are you sure you want to delete advance record for ${target.employeeName} (${target.amountFormatted})?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteKasbon(target.id)
                        kasbonToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isIndonesian) "Hapus" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { kasbonToDelete = null }) {
                    Text(if (isIndonesian) "Batal" else "Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun KasbonCard(
    item: KasbonItem,
    isIndonesian: Boolean,
    onPayClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (item.status) {
        KasbonStatus.LUNAS -> Color(0xFF16A34A)
        KasbonStatus.CICILAN -> Color(0xFF2563EB)
        KasbonStatus.BELUM_LUNAS -> Color(0xFFDC2626)
    }

    val statusBg = when (item.status) {
        KasbonStatus.LUNAS -> Color(0xFFDCFCE7)
        KasbonStatus.CICILAN -> Color(0xFFEFF6FF)
        KasbonStatus.BELUM_LUNAS -> Color(0xFFFEE2E2)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("kasbon_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Avatar, Name, Role, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.employeeName.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = item.employeeName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = item.role,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (isIndonesian) item.status.labelId else item.status.labelEn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF8FAFC))

            // Price & Remaining row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (isIndonesian) "Nominal Kasbon" else "Advance Amount",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = item.amountFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isIndonesian) "Sisa Pembayaran" else "Remaining Balance",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = if (item.remainingAmount == 0.0) {
                            if (isIndonesian) "LUNAS" else "PAID"
                        } else {
                            item.remainingAmountFormatted
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (item.remainingAmount == 0.0) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                }
            }

            // Repayment Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { item.progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (item.progressPercent >= 1f) Color(0xFF16A34A) else Color(0xFF2563EB),
                    trackColor = Color(0xFFF1F5F9)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isIndonesian) "Terbayar: ${item.paidAmountFormatted}" else "Paid: ${item.paidAmountFormatted}",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${(item.progressPercent * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.progressPercent >= 1f) Color(0xFF16A34A) else Color(0xFF2563EB)
                    )
                }
            }

            // Purpose & Due Date details
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(if (isIndonesian) "Keperluan:" else "Purpose:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(item.purpose, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(if (isIndonesian) "Metode:" else "Method:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(item.repaymentMethod, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                        }
                        Text(
                            text = if (isIndonesian) "Jatuh Tempo: ${item.dueDate}" else "Due: ${item.dueDate}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bayar / Cicil button
                Button(
                    onClick = onPayClick,
                    enabled = item.status != KasbonStatus.LUNAS,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A),
                        disabledContainerColor = Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.status == KasbonStatus.LUNAS) {
                            if (isIndonesian) "Sudah Lunas" else "Settled"
                        } else {
                            if (isIndonesian) "Bayar / Cicil" else "Pay"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Edit button
                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                }

                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
