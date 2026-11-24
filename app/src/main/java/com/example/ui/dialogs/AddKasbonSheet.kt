package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KasbonItem
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKasbonSheet(
    existingKasbon: KasbonItem? = null,
    isIndonesian: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        employeeName: String,
        role: String,
        amount: Double,
        purpose: String,
        dueDate: String,
        repaymentMethod: String,
        notes: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val roles = remember {
        listOf(
            "Operator Jahit",
            "Tukang Potong (Cutting)",
            "Finishing & Packaging",
            "Quality Control (QC)",
            "Staff Gudang & Logistik",
            "Supervisor Produksi"
        )
    }

    val repaymentMethods = remember {
        listOf(
            "Potong Gaji Bulanan",
            "Cicil Mingguan",
            "Tunai Langsung",
            "Potong Uang Lembur"
        )
    }

    val commonEmployees = remember {
        listOf("Ahmad Santoso", "Siti Aminah", "Budi Santoso", "Rudi Hermawan", "Ahmad Zaki", "Dewi Sartika", "Hendra Gunawan")
    }

    var employeeName by remember { mutableStateOf(existingKasbon?.employeeName ?: "") }
    var selectedRole by remember { mutableStateOf(existingKasbon?.role ?: roles.first()) }
    var amountText by remember { mutableStateOf(existingKasbon?.let { Math.round(it.amount).toString() } ?: "300000") }
    var purpose by remember { mutableStateOf(existingKasbon?.purpose ?: "") }
    var dueDate by remember { mutableStateOf(existingKasbon?.dueDate ?: "2026-11-25 (Potong Gaji)") }
    var selectedRepaymentMethod by remember { mutableStateOf(existingKasbon?.repaymentMethod ?: repaymentMethods.first()) }
    var notes by remember { mutableStateOf(existingKasbon?.notes ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("add_kasbon_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (existingKasbon != null) {
                                if (isIndonesian) "Edit Kasbon Karyawan" else "Edit Cash Advance"
                            } else {
                                if (isIndonesian) "Catat Kasbon Baru" else "New Cash Advance"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (isIndonesian) "Form pinjaman & uang muka gaji karyawan" else "Employee loan & salary advance form",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF64748B))
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Nama Karyawan
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isIndonesian) "Nama Karyawan *" else "Employee Name *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                OutlinedTextField(
                    value = employeeName,
                    onValueChange = { employeeName = it },
                    placeholder = { Text("Contoh: Ahmad Santoso") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_kasbon_employee_name")
                )

                // Quick name suggestion chips
                Text(
                    text = if (isIndonesian) "Pilih Cepat Karyawan:" else "Quick Select:",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonEmployees.take(3).forEach { name ->
                        AssistChip(
                            onClick = { employeeName = name },
                            label = { Text(name, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Jabatan / Divisi
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isIndonesian) "Divisi / Bagian Pekerjaan" else "Department / Role",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.take(3).forEach { role ->
                        val isSelected = selectedRole == role
                        Surface(
                            onClick = { selectedRole = role },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = role.substringBefore(" ("),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Nominal Kasbon (Rp)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isIndonesian) "Nominal Kasbon (Rp) *" else "Advance Amount (Rp) *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            amountText = input
                        }
                    },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB)) },
                    placeholder = { Text("300000") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_kasbon_amount")
                )

                // Quick nominal chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(100_000.0, 250_000.0, 500_000.0, 1_000_000.0).forEach { nom ->
                        val label = when (nom) {
                            100_000.0 -> "100rb"
                            250_000.0 -> "250rb"
                            500_000.0 -> "500rb"
                            else -> "1 Juta"
                        }
                        Surface(
                            onClick = { amountText = Math.round(nom).toString() },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Keperluan / Alasan
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isIndonesian) "Keperluan / Alasan Pinjaman" else "Purpose of Advance",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    placeholder = { Text("Contoh: Biaya berobat keluarga, servis motor kerja, dll") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Metode Pelunasan & Tanggal Jatuh Tempo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isIndonesian) "Metode Pelunasan" else "Repayment Method",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    OutlinedTextField(
                        value = selectedRepaymentMethod,
                        onValueChange = { selectedRepaymentMethod = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isIndonesian) "Target Jatuh Tempo" else "Due Date",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Catatan Tambahan
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isIndonesian) "Catatan Supervisor / Admin (Opsional)" else "Admin Notes (Optional)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Disetujui mandor, bukti nota, dsb.") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error feedback
            errorMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isIndonesian) "Batal" else "Cancel", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (employeeName.isBlank()) {
                            errorMessage = if (isIndonesian) "Nama karyawan tidak boleh kosong." else "Employee name is required."
                            return@Button
                        }
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt <= 0) {
                            errorMessage = if (isIndonesian) "Nominal kasbon harus lebih dari Rp 0." else "Amount must be greater than 0."
                            return@Button
                        }

                        onSave(
                            employeeName.trim(),
                            selectedRole,
                            amt,
                            purpose.ifBlank { "Keperluan Pribadi" },
                            dueDate.ifBlank { "Akhir Bulan Ini" },
                            selectedRepaymentMethod,
                            notes.trim()
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("btn_submit_kasbon")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (existingKasbon != null) {
                            if (isIndonesian) "Simpan Perubahan" else "Save Changes"
                        } else {
                            if (isIndonesian) "Simpan Kasbon" else "Save Advance"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
