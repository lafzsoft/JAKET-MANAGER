package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacketItem
import com.example.model.ProductionShift
import com.example.model.ProductionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductionDialog(
    jackets: List<JacketItem>,
    isIndonesian: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        jacketId: String,
        jacketName: String,
        jacketSku: String,
        batchNumber: String,
        shift: ProductionShift,
        operatorName: String,
        quantityProduced: Int,
        defectCount: Int,
        status: ProductionStatus,
        notes: String,
        addedToStock: Boolean
    ) -> Unit
) {
    var selectedJacket by remember { mutableStateOf(jackets.firstOrNull()) }
    var batchNumber by remember {
        mutableStateOf("BATCH-${(System.currentTimeMillis() % 10000)}")
    }
    var selectedShift by remember { mutableStateOf(ProductionShift.MORNING) }
    var operatorName by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("20") }
    var defectText by remember { mutableStateOf("0") }
    var selectedStatus by remember { mutableStateOf(ProductionStatus.COMPLETED) }
    var notes by remember { mutableStateOf("") }
    var addedToStock by remember { mutableStateOf(true) }

    var jacketDropdownExpanded by remember { mutableStateOf(false) }

    val qty = quantityText.toIntOrNull() ?: 0
    val defects = defectText.toIntOrNull() ?: 0
    val netAccepted = (qty - defects).coerceAtLeast(0)

    val isFormValid = qty > 0 && operatorName.isNotBlank() && selectedJacket != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.testTag("add_production_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isIndonesian) "Catat Produksi Harian" else "Log Daily Production",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = if (isIndonesian) "Input hasil pengerjaan batch jahit & perakitan" else "Record output from tailoring & assembly batch",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF475569))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Jacket Selection Dropdown
            Text(
                text = if (isIndonesian) "Pilih Model Jaket *" else "Select Jacket Model *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = jacketDropdownExpanded,
                onExpandedChange = { jacketDropdownExpanded = !jacketDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedJacket?.let { "${it.name} (${it.sku})" } ?: if (isIndonesian) "Pilih Jaket" else "Choose Jacket",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jacketDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("production_jacket_dropdown"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2D3748),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                ExposedDropdownMenu(
                    expanded = jacketDropdownExpanded,
                    onDismissRequest = { jacketDropdownExpanded = false }
                ) {
                    jackets.forEach { jacket ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(jacket.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(
                                        "${jacket.sku} • ${jacket.style} • ${jacket.size} • Stok: ${jacket.quantity}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            },
                            onClick = {
                                selectedJacket = jacket
                                jacketDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Batch Number and Shift
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Batch Number
                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text(if (isIndonesian) "Nomor Batch" else "Batch Number") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("production_batch_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2D3748),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                // Operator Name
                OutlinedTextField(
                    value = operatorName,
                    onValueChange = { operatorName = it },
                    label = { Text(if (isIndonesian) "Nama Operator *" else "Operator Name *") },
                    placeholder = { Text(if (isIndonesian) "cth: Budi / Tim A" else "e.g. John / Team A") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("production_operator_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2D3748),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Shift Selection Chips
            Text(
                text = if (isIndonesian) "Pilih Shift Kerja" else "Select Work Shift",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProductionShift.values().forEach { shift ->
                    val isSelected = selectedShift == shift
                    val label = when (shift) {
                        ProductionShift.MORNING -> if (isIndonesian) "Pagi" else "Morning"
                        ProductionShift.AFTERNOON -> if (isIndonesian) "Siang" else "Afternoon"
                        ProductionShift.NIGHT -> if (isIndonesian) "Malam" else "Night"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF2D3748) else Color(0xFFF1F5F9))
                            .clickable { selectedShift = shift }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Quantity Produced & Defect Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quantity Produced
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                    label = { Text(if (isIndonesian) "Jumlah Selesai (pcs) *" else "Total Produced (pcs) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("production_qty_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2D3748),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                // Defect Count
                OutlinedTextField(
                    value = defectText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) defectText = it },
                    label = { Text(if (isIndonesian) "Cacat / Reject (pcs)" else "Defects (pcs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("production_defect_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFDC2626),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
            }

            // Quality preview chip
            if (qty > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isIndonesian) "Unit Lolos QC: $netAccepted pcs" else "QC Passed Units: $netAccepted pcs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                    val defectRate = if (qty > 0) (defects.toFloat() / qty.toFloat() * 100f) else 0f
                    Text(
                        text = if (isIndonesian) "Tingkat Cacat: ${"%.1f".format(defectRate)}%" else "Defect Rate: ${"%.1f".format(defectRate)}%",
                        fontSize = 11.sp,
                        color = if (defectRate > 5f) Color(0xFFDC2626) else Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Status Selection
            Text(
                text = if (isIndonesian) "Status Batch" else "Batch Status",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProductionStatus.values().forEach { st ->
                    val isSelected = selectedStatus == st
                    val label = if (isIndonesian) st.labelId else st.labelEn
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatus = st },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Automatically add to Warehouse Stock Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0FDF4))
                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isIndonesian) "Tambahkan ke Stok Gudang" else "Add Output to Warehouse Stock",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                    Text(
                        text = if (isIndonesian) {
                            "Otomatis menambah +$netAccepted pcs ke stok '${selectedJacket?.name ?: ""}'"
                        } else {
                            "Automatically add +$netAccepted pcs to '${selectedJacket?.name ?: ""}' inventory"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF15803D)
                    )
                }

                Switch(
                    checked = addedToStock,
                    onCheckedChange = { addedToStock = it },
                    modifier = Modifier.testTag("production_add_to_stock_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7. Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(if (isIndonesian) "Catatan Produksi (Opsional)" else "Production Notes (Optional)") },
                placeholder = { Text(if (isIndonesian) "Catatan kain, mesin, kendala jahit..." else "Fabric, machine, or quality notes...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("production_notes_input"),
                shape = RoundedCornerShape(10.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2D3748),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isIndonesian) "Batal" else "Cancel")
                }

                Button(
                    onClick = {
                        val jkt = selectedJacket ?: return@Button
                        onSave(
                            jkt.id,
                            jkt.name,
                            jkt.sku,
                            batchNumber,
                            selectedShift,
                            operatorName,
                            qty,
                            defects,
                            selectedStatus,
                            notes,
                            addedToStock
                        )
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_production_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3748))
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isIndonesian) "Simpan Batch" else "Save Batch", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
