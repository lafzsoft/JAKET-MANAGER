package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveFilter
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import com.example.ui.components.NetworkImageWithFallback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListSheet(
    filter: ActiveFilter,
    items: List<JacketItem>,
    searchQuery: String,
    isIndonesian: Boolean,
    onSearchChange: (String) -> Unit,
    onAdjustQty: (String, Int) -> Unit,
    onRestock: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filterTitle = when (filter) {
        is ActiveFilter.None -> if (isIndonesian) "Semua Inventaris" else "All Inventory"
        is ActiveFilter.ByStyle -> filter.title
        is ActiveFilter.BySize -> filter.title
        is ActiveFilter.ByStatus -> filter.title
        is ActiveFilter.ByMaterial -> filter.title
        is ActiveFilter.ByColor -> filter.title
        is ActiveFilter.BySeasonal -> filter.title
        is ActiveFilter.ByCategory -> filter.title
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = filterTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = if (isIndonesian) "${items.size} jaket ditemukan" else "${items.size} jackets found",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF4A5568))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        if (isIndonesian) "Cari SKU, nama, rak..." else "Search SKU, name, rack...",
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inventory_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2D3748),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isIndonesian) "Tidak ada jaket di kategori ini" else "No jackets found in this category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }) { jacket ->
                        JacketInventoryItemCard(
                            jacket = jacket,
                            isIndonesian = isIndonesian,
                            onAdjustQty = { delta -> onAdjustQty(jacket.id, delta) },
                            onRestock = { onRestock(jacket.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JacketInventoryItemCard(
    jacket: JacketItem,
    isIndonesian: Boolean,
    onAdjustQty: (Int) -> Unit,
    onRestock: () -> Unit
) {
    val (statusLabel, statusColor, statusBg) = when (jacket.status) {
        StockStatusLevel.IN_STOCK -> Triple(
            if (isIndonesian) "Tersedia" else "In Stock",
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
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${jacket.sku}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Jacket Thumbnail
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    NetworkImageWithFallback(
                        imageUrl = jacket.imageUrl,
                        contentDescription = jacket.name,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Details
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

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
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

                    // Specs row: Size, Style, Rack
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpecChip(text = jacket.size)
                        SpecChip(text = jacket.color)
                        SpecChip(text = jacket.material)
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

            // Action row: Quantity Stepper & Quick Restock
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
                        text = if (isIndonesian) "Stok saat ini:" else "Current Stock:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    IconButton(
                        onClick = { onAdjustQty(-1) },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "${jacket.quantity}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.widthIn(min = 28.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    IconButton(
                        onClick = { onAdjustQty(1) },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
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
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isIndonesian) "+15 Restock" else "+15 Restock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecChip(text: String) {
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
