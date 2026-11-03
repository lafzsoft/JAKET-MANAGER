package com.example.ui.dialogs

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacketItem
import com.example.util.CsvExportHelper
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportCsvBottomSheet(
    allJackets: List<JacketItem>,
    currentFilteredJackets: List<JacketItem>,
    isIndonesian: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Selection scope for export
    var selectedScope by remember { mutableStateOf("ALL") }

    val jacketsToExport = remember(allJackets, currentFilteredJackets, selectedScope) {
        when (selectedScope) {
            "FILTERED" -> currentFilteredJackets
            "PRIA" -> allJackets.filter { it.genderCategory.equals("Jaket Pria", ignoreCase = true) }
            "WANITA" -> allJackets.filter { it.genderCategory.equals("Jaket Wanita", ignoreCase = true) }
            "ANAK" -> allJackets.filter { it.genderCategory.equals("Jaket Anak", ignoreCase = true) }
            "UNISEX" -> allJackets.filter { it.genderCategory.equals("Jaket Unisex", ignoreCase = true) }
            else -> allJackets
        }
    }

    val totalUnits = remember(jacketsToExport) { jacketsToExport.sumOf { it.quantity } }
    val totalValuation = remember(jacketsToExport) { jacketsToExport.sumOf { it.quantity * it.unitPrice } }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    // Live CSV content preview
    val csvContent = remember(jacketsToExport, isIndonesian) {
        CsvExportHelper.generateCsv(jacketsToExport, isIndonesian)
    }

    val previewSnippet = remember(csvContent) {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        val header = lines.firstOrNull() ?: ""
        val samples = lines.drop(1).take(3)
        (listOf(header) + samples).joinToString("\n")
    }

    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("export_csv_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF047857), Color(0xFF10B981)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isIndonesian) "Ekspor Inventaris CSV" else "Export Inventory to CSV",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isIndonesian) "Backup data & bagikan ke spreadsheet" else "Backup data & share to spreadsheet apps",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_export_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scope Selector
            Text(
                text = if (isIndonesian) "Pilih Data yang Akan Diekspor:" else "Select Data Scope to Export:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(6.dp))

            val scopes = listOf(
                "ALL" to if (isIndonesian) "Semua Item (${allJackets.size})" else "All Items (${allJackets.size})",
                "FILTERED" to if (isIndonesian) "Tampilan Saat Ini (${currentFilteredJackets.size})" else "Current Filter (${currentFilteredJackets.size})",
                "PRIA" to "Jaket Pria",
                "WANITA" to "Jaket Wanita",
                "ANAK" to "Jaket Anak",
                "UNISEX" to "Jaket Unisex"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(scopes) { (key, label) ->
                    val isSelected = selectedScope == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedScope = key },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E293B),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("export_scope_$key")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Stats Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${jacketsToExport.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isIndonesian) "Model Produk" else "Products",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp),
                        color = Color(0xFFCBD5E1)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalUnits",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                        Text(
                            text = if (isIndonesian) "Total Unit Stok" else "Total Units",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp),
                        color = Color(0xFFCBD5E1)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currencyFormatter.format(totalValuation),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                        Text(
                            text = if (isIndonesian) "Nilai Valuasi" else "Asset Value",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CSV Preview Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isIndonesian) "Pratinjau Format CSV:" else "CSV Format Preview:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )

                Text(
                    text = if (isIndonesian) "Encoding: UTF-8 (Excel OK)" else "Encoding: UTF-8 (Excel OK)",
                    fontSize = 11.sp,
                    color = Color(0xFF059669),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${CsvExportHelper.generateFilename()}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF334155))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "RFC 4180",
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = previewSnippet,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF38BDF8),
                        lineHeight = 15.sp,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    )
                }
            }

            // Notification / Feedback banner
            if (exportSuccessMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A)
                        )
                        Text(
                            text = exportSuccessMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF14532D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Copy to Clipboard Button
                OutlinedButton(
                    onClick = {
                        CsvExportHelper.copyToClipboard(context, csvContent)
                        exportSuccessMessage = if (isIndonesian) {
                            "Berhasil menyalin data CSV (${jacketsToExport.size} item) ke papan klip!"
                        } else {
                            "Copied CSV data (${jacketsToExport.size} items) to clipboard!"
                        }
                        Toast.makeText(
                            context,
                            if (isIndonesian) "Data CSV disalin ke clipboard" else "CSV data copied to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_copy_csv_clipboard")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isIndonesian) "Salin Teks" else "Copy Text",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Share CSV File Button
                Button(
                    onClick = {
                        try {
                            val filename = CsvExportHelper.generateFilename()
                            val file = CsvExportHelper.writeCsvToCache(context, csvContent, filename)
                            val shareIntent = CsvExportHelper.createShareIntent(
                                context = context,
                                file = file,
                                chooserTitle = if (isIndonesian) "Bagikan File CSV Inventaris" else "Share Inventory CSV File"
                            )
                            context.startActivity(shareIntent)
                            exportSuccessMessage = if (isIndonesian) {
                                "File CSV '$filename' siap dibagikan ke aplikasi lain!"
                            } else {
                                "CSV file '$filename' ready to share!"
                            }
                        } catch (e: Exception) {
                            exportSuccessMessage = if (isIndonesian) {
                                "Gagal mengekspor: ${e.localizedMessage}"
                            } else {
                                "Export failed: ${e.localizedMessage}"
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF047857),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("btn_share_csv_file")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isIndonesian) "Bagikan File CSV" else "Share CSV File",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
