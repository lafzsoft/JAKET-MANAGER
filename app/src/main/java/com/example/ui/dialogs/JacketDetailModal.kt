package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CatalogData
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import com.example.ui.components.NetworkImageWithFallback

/**
 * Modal bottom sheet presenting full jacket product details:
 * - High-resolution product image and pricing
 * - Detailed contextual description
 * - Interactive color variants with live warehouse stock indicators
 * - Comprehensive stock breakdown (size matrix, safety threshold, rack location, health gauge)
 * - Real-time stock adjustment stepper and quick action controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JacketDetailModal(
    jacket: JacketItem,
    allJackets: List<JacketItem>,
    isIndonesian: Boolean,
    onDismiss: () -> Unit,
    onAdjustQuantity: (jacketId: String, delta: Int) -> Unit,
    onRestock: (jacketId: String) -> Unit,
    onEditImage: (jacket: JacketItem) -> Unit,
    onDelete: (jacket: JacketItem) -> Unit,
    onSelectVariant: (jacket: JacketItem) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    // Determine currently displayed jacket (can switch if user clicks color variant)
    var currentJacket by remember(jacket) { mutableStateOf(jacket) }

    // Keep synced if list changes
    LaunchedEffect(allJackets, currentJacket.id) {
        val updated = allJackets.find { it.id == currentJacket.id }
        if (updated != null) {
            currentJacket = updated
        }
    }

    val stockColor = when (currentJacket.status) {
        StockStatusLevel.IN_STOCK -> Color(0xFF059669) // Emerald Green
        StockStatusLevel.LOW_STOCK -> Color(0xFFD97706) // Amber
        StockStatusLevel.OUT_OF_STOCK -> Color(0xFFDC2626) // Red
    }

    val stockBgColor = when (currentJacket.status) {
        StockStatusLevel.IN_STOCK -> Color(0xFFECFDF5)
        StockStatusLevel.LOW_STOCK -> Color(0xFFFFFBEB)
        StockStatusLevel.OUT_OF_STOCK -> Color(0xFFFEF2F2)
    }

    val statusLabel = when (currentJacket.status) {
        StockStatusLevel.IN_STOCK -> if (isIndonesian) "Stok Tersedia (Aman)" else "In Stock (Safe)"
        StockStatusLevel.LOW_STOCK -> if (isIndonesian) "Stok Menipis (Perlu Restock)" else "Low Stock (Restock Needed)"
        StockStatusLevel.OUT_OF_STOCK -> if (isIndonesian) "Habis Stok (Kritis)" else "Out of Stock (Critical)"
    }

    // Dynamic description generation based on jacket properties
    val productDescription = remember(currentJacket, isIndonesian) {
        generateDetailedDescription(currentJacket, isIndonesian)
    }

    // Related color variants matching this jacket style/model name
    val relatedVariants = remember(allJackets, currentJacket) {
        allJackets.filter {
            it.style.equals(currentJacket.style, ignoreCase = true) ||
            it.name.split(" ").take(2).joinToString(" ").equals(
                currentJacket.name.split(" ").take(2).joinToString(" "),
                ignoreCase = true
            )
        }
    }

    // All available sizes for this product family in warehouse
    val sizeBreakdown = remember(allJackets, currentJacket) {
        val sizes = listOf("XS", "S", "M", "L", "XL", "XXL", "3XL")
        sizes.map { size ->
            val matchingItems = relatedVariants.filter { it.size.equals(size, ignoreCase = true) }
            val count = matchingItems.sumOf { it.quantity }
            size to count
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("inventory_detail_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header Row: SKU Tag & Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = currentJacket.sku,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("detail_modal_sku")
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_close_detail_modal")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isIndonesian) "Tutup" else "Close",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            // Hero Image Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FAFC))
            ) {
                NetworkImageWithFallback(
                    imageUrl = currentJacket.imageUrl,
                    contentDescription = currentJacket.name,
                    modifier = Modifier.fillMaxSize()
                )

                // Stock Status Badge Overlay
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = stockBgColor.copy(alpha = 0.95f),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(stockColor)
                        )
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = stockColor
                        )
                    }
                }

                // Price Tag (Top End)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xD90F172A),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "$${String.format("%.2f", currentJacket.unitPrice)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Change Photo Quick Button (Bottom End)
                FilledTonalButton(
                    onClick = { onEditImage(currentJacket) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xD90F172A),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.BottomEnd)
                        .testTag("btn_edit_image_detail")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isIndonesian) "Ubah Foto" else "Edit Photo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Product Title & Demographic Tag
            Text(
                text = currentJacket.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                modifier = Modifier.testTag("detail_modal_product_name")
            )

            // Chips Row: Style, Category, Size, Rack
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE2E8F0)) {
                    Text(
                        text = currentJacket.genderCategory,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE2E8F0)) {
                    Text(
                        text = currentJacket.style,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1E293B)) {
                    Text(
                        text = "Ukuran: ${currentJacket.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                    Text(
                        text = currentJacket.locationRack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: Product Description
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_modal_description"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isIndonesian) "Deskripsi & Karakteristik Produk" else "Product Description & Specifications",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = productDescription,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Key Specs Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isIndonesian) "Bahan Material:" else "Material:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = currentJacket.material,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Column {
                            Text(
                                text = if (isIndonesian) "Warna Utama:" else "Primary Color:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = currentJacket.color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Column {
                            Text(
                                text = if (isIndonesian) "Koleksi:" else "Collection:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = if (currentJacket.isSeasonal) {
                                    if (isIndonesian) "Musiman (Winter/Rain)" else "Seasonal (Winter/Rain)"
                                } else {
                                    if (isIndonesian) "Koleksi Reguler" else "Regular Stock"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentJacket.isSeasonal) Color(0xFF2563EB) else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: Color Variants
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_modal_color_variants"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
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
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isIndonesian) "Varian Warna Produk" else "Color Variants",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Text(
                            text = if (isIndonesian) "Warna aktif: ${currentJacket.color}" else "Active: ${currentJacket.color}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Curated color list merging catalog palette and available warehouse items
                    val colorOptions = remember(allJackets, currentJacket) {
                        getColorVariantOptions(currentJacket, allJackets)
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(colorOptions) { option ->
                            val isSelected = option.colorName.equals(currentJacket.color, ignoreCase = true)
                            val optionJacket = option.existingJacket

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(68.dp)
                                    .clickable {
                                        if (optionJacket != null) {
                                            currentJacket = optionJacket
                                            onSelectVariant(optionJacket)
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(option.hexColor))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = if (option.isDark) Color.White else Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = option.colorName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = if (optionJacket != null) {
                                        "${optionJacket.quantity} unit"
                                    } else {
                                        if (isIndonesian) "Tersedia" else "Sample"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (optionJacket != null && optionJacket.quantity > 0) Color(0xFF059669) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 3: Detailed Stock Breakdown
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_modal_stock_breakdown"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
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
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = stockColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isIndonesian) "Rincian & Analisis Stok Gudang" else "Detailed Stock Breakdown & Health",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Surface(shape = RoundedCornerShape(6.dp), color = stockBgColor) {
                            Text(
                                text = statusLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = stockColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Metric Stat Boxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat 1: Current Stock
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isIndonesian) "Stok Gudang" else "Available Stock",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currentJacket.quantity}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = stockColor
                                )
                                Text(
                                    text = if (isIndonesian) "Unit siap kirim" else "Ready units",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Stat 2: Safety Threshold
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isIndonesian) "Batas Minimum" else "Min Threshold",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currentJacket.minThreshold}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = if (isIndonesian) "Batas peringatan" else "Alert trigger",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Stat 3: Rack Location
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isIndonesian) "Lokasi Rak" else "Rack Bin",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentJacket.locationRack,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isIndonesian) "Zona Gudang Utama" else "Main Zone",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stock Capacity Bar
                    val capacity = 50f
                    val progress = (currentJacket.quantity / capacity).coerceIn(0f, 1f)
                    Text(
                        text = if (isIndonesian) "Kapasitas Penyimpanan Rak (Target 50 Unit):" else "Rack Storage Capacity (50 Unit target):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = stockColor,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Size Matrix Breakdown in Warehouse
                    Text(
                        text = if (isIndonesian) "Distribusi Stok per Ukuran Model Ini:" else "Stock Distribution by Size (This Model):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        sizeBreakdown.forEach { (size, qty) ->
                            val isCurrentSize = size.equals(currentJacket.size, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCurrentSize) Color(0xFF1E293B) else Color.White,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = size,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrentSize) Color.White else Color(0xFF475569)
                                    )
                                    Text(
                                        text = "$qty",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isCurrentSize) Color(0xFF34D399) else if (qty > 0) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real-Time Stock Adjustment Stepper inside Modal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isIndonesian) "Penyesuaian Cepat Stok" else "Quick Stock Adjuster",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isIndonesian) "Tambah / kurangi jumlah unit" else "Add or subtract inventory",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { onAdjustQuantity(currentJacket.id, -1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = "${currentJacket.quantity}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            IconButton(
                                onClick = { onAdjustQuantity(currentJacket.id, 1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
                            }

                            if (currentJacket.status != StockStatusLevel.IN_STOCK) {
                                Spacer(modifier = Modifier.width(4.dp))
                                FilledTonalButton(
                                    onClick = { onRestock(currentJacket.id) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFDCFCE7),
                                        contentColor = Color(0xFF166534)
                                    )
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("+15", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Actions: Delete & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDelete(currentJacket)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isIndonesian) "Hapus Jaket" else "Delete Item", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text(if (isIndonesian) "Selesai" else "Done", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * Data class representing a color variant swatch option.
 */
data class ColorVariantOption(
    val colorName: String,
    val hexColor: Long,
    val isDark: Boolean,
    val existingJacket: JacketItem?
)

/**
 * Helper to build rich color variant list merging warehouse records and catalog defaults.
 */
private fun getColorVariantOptions(currentJacket: JacketItem, allJackets: List<JacketItem>): List<ColorVariantOption> {
    val catalogColors = listOf(
        "Black" to 0xFF18181BL,
        "Navy" to 0xFF1E3A8AL,
        "Olive" to 0xFF365314L,
        "Beige" to 0xFFE5D5C5L,
        "Maroon" to 0xFF800020L,
        "Red" to 0xFFDC2626L,
        "Gray" to 0xFF6B7280L,
        "Caramel" to 0xFFC27835L,
        "White" to 0xFFFFFFFFL,
        "Pink" to 0xFFF472B6L
    )

    // Existing jackets with same style
    val sameStyleJackets = allJackets.filter { it.style.equals(currentJacket.style, ignoreCase = true) }

    return catalogColors.map { (name, hex) ->
        val matchedJacket = sameStyleJackets.find { it.color.equals(name, ignoreCase = true) }
        val isDark = hex != 0xFFFFFFFFL && hex != 0xFFE5D5C5L && hex != 0xFFF472B6L
        ColorVariantOption(
            colorName = name,
            hexColor = hex,
            isDark = isDark,
            existingJacket = matchedJacket ?: if (name.equals(currentJacket.color, ignoreCase = true)) currentJacket else null
        )
    }
}

/**
 * Helper generating contextual product description based on garment material, style, and category.
 */
private fun generateDetailedDescription(jacket: JacketItem, isIndonesian: Boolean): String {
    val styleDesc = when (jacket.style.lowercase()) {
        "bomber jacket", "bomber" -> if (isIndonesian) {
            "Jaket model bomber klasik berinsulasi dengan rib elastis pada bagian kerah, pergelangan tangan, dan pinggang. Menawarkan siluet atletis dan ketahanan tinggi."
        } else {
            "Classic insulated bomber silhouette featuring stretch ribbed collar, cuffs, and hem. Delivers an athletic fit and rugged wind resistance."
        }
        "trench coat", "trench" -> if (isIndonesian) {
            "Mantel model trench coat elegan berdesain double-breasted dengan sabuk pinggang yang dapat disesuaikan. Melindungi dari angin dan cipratan air hujan."
        } else {
            "Sophisticated double-breasted trench coat tailored with an adjustable waist belt. Engineered for rain repellent performance and timeless fashion."
        }
        "denim jacket", "denim" -> if (isIndonesian) {
            "Jaket berbahan denim premium berkancing logam dengan saku ganda di dada. Memiliki jahitan kokoh khas pakaian kerja vintage yang tahan lama."
        } else {
            "Rugged cotton denim outerwear featuring branded metal hardware and twin chest pockets. Reinforced stitching built for versatile daily wear."
        }
        "puffer jacket", "puffer" -> if (isIndonesian) {
            "Jaket puffer quilted berbobot ringan dengan insulasi termal sintetis. Sangat efektif menjaga suhu tubuh tetap hangat saat cuaca dingin ekstrem."
        } else {
            "Ultra-light quilted puffer construction with synthetic thermal fill. Provides exceptional warmth retention in freezing conditions."
        }
        "parka" -> if (isIndonesian) {
            "Jaket parka panjang tahan cuaca dengan penutup kepala (hoodie) dan saku kargo multifungsi. Dirancang untuk perlindungan maksimal aktivitas luar ruangan."
        } else {
            "Heavy-duty weather resistant long parka jacket with drawstring hood and utility cargo pockets. Built for maximum outdoor shielding."
        }
        else -> if (isIndonesian) {
            "Pakaian luar berkualitas tinggi dengan konstruksi jahitan presisi untuk kenyamanan sepanjang hari dan daya tahan optimal."
        } else {
            "Premium outerwear built with reinforced precision tailoring for maximum comfort and durability."
        }
    }

    val materialDesc = when (jacket.material.lowercase()) {
        "leather", "kulit" -> if (isIndonesian) "Dibuat dari bahan kulit premium dengan tekstur halus dan lapisan dalam lembut." else "Crafted with supple full-grain leather and breathable interior lining."
        "cotton", "katun" -> if (isIndonesian) "Terbuat dari katun combed alami yang sejuk, ramah kulit, dan menyerap keringat." else "Constructed from 100% breathable organic cotton twill."
        "nylon", "nilon" -> if (isIndonesian) "Menggunakan serat nilon tahan air dengan finishing matte anti robek." else "Constructed with ripstop water-resistant nylon weave."
        "wool", "wol" -> if (isIndonesian) "Komposisi serat wol alami yang tebal dan memberikan kehangatan istimewa." else "Heavyweight natural wool blend providing superior thermal insulation."
        else -> if (isIndonesian) "Material kain komposit ${jacket.material} yang kuat dan mudah dirawat." else "Durable ${jacket.material} composite blend, easy to clean."
    }

    val fitDesc = if (isIndonesian) {
        "Kategori: ${jacket.genderCategory}. Ukuran: ${jacket.size}. Disimpan di ${jacket.locationRack} siap untuk pemenuhan pesanan gudang."
    } else {
        "Category: ${jacket.genderCategory}. Size: ${jacket.size}. Stored at ${jacket.locationRack} ready for warehouse dispatch."
    }

    return "$styleDesc $materialDesc $fitDesc"
}
