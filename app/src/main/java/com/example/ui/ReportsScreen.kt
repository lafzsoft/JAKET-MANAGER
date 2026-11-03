package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.StockStatusLevel
import com.example.ui.components.InventoryDistributionCharts
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: JacketViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    val allJackets by viewModel.allJackets.collectAsStateWithLifecycle()
    val isIndonesian by viewModel.isIndonesian.collectAsStateWithLifecycle()

    val totalUnits = allJackets.sumOf { it.quantity }
    val totalModels = allJackets.size
    val inStockJackets = allJackets.filter { it.status == StockStatusLevel.IN_STOCK }
    val lowStockJackets = allJackets.filter { it.status == StockStatusLevel.LOW_STOCK }
    val outOfStockJackets = allJackets.filter { it.status == StockStatusLevel.OUT_OF_STOCK }

    val totalValuation = allJackets.sumOf { it.quantity * it.unitPrice }
    val healthRatio = if (totalModels > 0) inStockJackets.size.toFloat() / totalModels.toFloat() else 1f

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen")
            .testTag("dashboard_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isIndonesian) "Dashboard & Analisis Gudang" else "Warehouse Dashboard & Analytics",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = if (isIndonesian) "Visualisasi distribusi stok & valuasi" else "Inventory distribution & asset analytics",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1A1A1A))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openExportSheet() },
                        modifier = Modifier.testTag("reports_export_csv_button")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Export CSV", tint = Color(0xFF047857))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
        ) {
            // Valuation Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isIndonesian) "Total Estimasi Valuasi Stok" else "Total Estimated Stock Value",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = currencyFormatter.format(totalValuation),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFF334155), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isIndonesian) "Total Unit: $totalUnits pcs" else "Total Units: $totalUnits pcs",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                            Text(
                                text = if (isIndonesian) "Total Model: $totalModels model" else "Total Models: $totalModels models",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            // Interactive Inventory Distribution Bar Chart (Styles, Sizes, Categories)
            item {
                InventoryDistributionCharts(
                    allJackets = allJackets,
                    isIndonesian = isIndonesian
                )
            }

            // Stock Health Progress Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isIndonesian) "Kesehatan Stok Inventaris" else "Inventory Stock Health",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = "${(healthRatio * 100).toInt()}% Sehat",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (healthRatio > 0.7f) Color(0xFF16A34A) else Color(0xFFD97706)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { healthRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF16A34A),
                            trackColor = Color(0xFFF1F5F9)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusMetricChip(
                                label = if (isIndonesian) "Tersedia" else "In Stock",
                                count = inStockJackets.size,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.weight(1f)
                            )
                            StatusMetricChip(
                                label = if (isIndonesian) "Stok Sedikit" else "Low Stock",
                                count = lowStockJackets.size,
                                color = Color(0xFFD97706),
                                modifier = Modifier.weight(1f)
                            )
                            StatusMetricChip(
                                label = if (isIndonesian) "Habis" else "Out of Stock",
                                count = outOfStockJackets.size,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Alerts & Batch Restock Action
            val alertJackets = lowStockJackets + outOfStockJackets
            if (alertJackets.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIndonesian) "Perlu Perhatian Segera (${alertJackets.size})" else "Needs Immediate Restock (${alertJackets.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        TextButton(
                            onClick = { viewModel.restockAllLowStock() }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isIndonesian) "Restock Semua" else "Restock All")
                        }
                    }
                }

                items(alertJackets, key = { it.id }) { jacket ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = jacket.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text(
                                    text = "${jacket.sku} • ${jacket.locationRack} • Sisa: ${jacket.quantity}",
                                    fontSize = 12.sp,
                                    color = if (jacket.quantity == 0) Color(0xFFDC2626) else Color(0xFFD97706),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            FilledTonalButton(
                                onClick = { viewModel.restock(jacket.id) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF2D3748),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("+15", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Breakdown by Style
            item {
                Text(
                    text = if (isIndonesian) "Distribusi Model Jaket" else "Style Distribution",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            val stylesGroup = allJackets.groupBy { it.style }
            items(stylesGroup.toList(), key = { it.first }) { (styleName, list) ->
                val styleUnits = list.sumOf { it.quantity }
                val stylePercentage = if (totalUnits > 0) styleUnits.toFloat() / totalUnits.toFloat() else 0f

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = styleName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(text = "$styleUnits pcs (${(stylePercentage * 100).toInt()}%)", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { stylePercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF2D3748),
                            trackColor = Color(0xFFF1F5F9)
                        )
                    }
                }
            }

            // CSV Export Card Item
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF047857)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isIndonesian) "Ekspor Laporan ke CSV" else "Export Report to CSV",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                                Text(
                                    text = if (isIndonesian) "Backup seluruh data inventaris & valuasi" else "Backup all inventory & asset values",
                                    fontSize = 11.sp,
                                    color = Color(0xFF166534)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.openExportSheet() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_export_from_reports")
                        ) {
                            Text(if (isIndonesian) "Ekspor" else "Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusMetricChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
