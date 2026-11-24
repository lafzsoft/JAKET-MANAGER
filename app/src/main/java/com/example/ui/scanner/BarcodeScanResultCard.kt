package com.example.ui.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import com.example.ui.components.NetworkImageWithFallback
import com.example.ui.theme.LocalAppThemeSpec
import com.example.util.BarcodeSearchUtils
import com.example.util.CurrencyUtils

/**
 * Displays the Room database lookup result for a scanned jacket barcode or QR tag.
 * Supports single exact match, multi-item partial/family matches, and unrecognized barcode creation.
 */
@Composable
internal fun BarcodeScanResultCard(
    rawCode: String,
    lookupToken: String,
    detectedFormat: Int?,
    isSearchingDatabase: Boolean,
    matchedJackets: List<JacketItem>,
    latestInventory: List<JacketItem>,
    isIndonesian: Boolean,
    onScanAgain: () -> Unit,
    onJacketIdentified: (JacketItem) -> Unit,
    onAddNewWithSku: (String) -> Unit,
    onApplyBarcodeToSearch: ((String) -> Unit)? = null,
    onQuickAdjustStock: ((JacketItem, Int) -> Unit)? = null
) {
    val themeSpec = LocalAppThemeSpec.current
    val resolvedMatches = matchedJackets.map { match ->
        latestInventory.firstOrNull { it.id == match.id } ?: match
    }
    val hasMatches = resolvedMatches.isNotEmpty()
    val borderColor = when {
        isSearchingDatabase -> themeSpec.accentPrimary
        hasMatches -> Color(0xFF10B981)
        else -> Color(0xFFF59E0B)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasMatches || isSearchingDatabase) Color(0xF50F172A) else Color(0xF51C1917)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("scan_result_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: status icon, title, and Scan Again button
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
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(borderColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSearchingDatabase) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = themeSpec.accentSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (hasMatches) Icons.Default.CheckCircle else Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = if (hasMatches) Color(0xFF34D399) else Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = when {
                            isSearchingDatabase -> if (isIndonesian) "Mencari di Database..." else "Searching Database..."
                            resolvedMatches.size == 1 -> if (isIndonesian) "Jaket Ditemukan!" else "Jacket Found!"
                            resolvedMatches.size > 1 -> if (isIndonesian) {
                                "${resolvedMatches.size} Jaket Cocok"
                            } else {
                                "${resolvedMatches.size} Matching Jackets"
                            }
                            else -> if (isIndonesian) "Barcode Belum Terdaftar" else "Barcode Not in Inventory"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TextButton(
                    onClick = onScanAgain,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .testTag("btn_scan_again")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = if (isIndonesian) "Pindai Ulang" else "Scan Again",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isIndonesian) "Ulang" else "Rescan",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Scanned barcode format & code chip
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = themeSpec.accentSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${BarcodeSearchUtils.formatBarcodeSymbology(detectedFormat, isIndonesian)}: $rawCode",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFBAE6FD),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isSearchingDatabase -> {
                    Text(
                        text = if (isIndonesian) {
                            "Memeriksa SKU dan katalog inventaris lokal..."
                        } else {
                            "Checking local inventory database by barcode..."
                        },
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                resolvedMatches.size == 1 -> {
                    val jacket = resolvedMatches.first()
                    SingleMatchedJacketContent(
                        jacket = jacket,
                        lookupToken = lookupToken,
                        isIndonesian = isIndonesian,
                        accentColor = themeSpec.accentPrimary,
                        onJacketIdentified = onJacketIdentified,
                        onApplyBarcodeToSearch = onApplyBarcodeToSearch,
                        onQuickAdjustStock = onQuickAdjustStock
                    )
                }

                resolvedMatches.size > 1 -> {
                    MultipleMatchedJacketsContent(
                        jackets = resolvedMatches,
                        lookupToken = lookupToken,
                        isIndonesian = isIndonesian,
                        accentColor = themeSpec.accentPrimary,
                        onJacketIdentified = onJacketIdentified,
                        onApplyBarcodeToSearch = onApplyBarcodeToSearch
                    )
                }

                else -> {
                    UnrecognizedBarcodeContent(
                        lookupToken = lookupToken,
                        isIndonesian = isIndonesian,
                        accentColor = themeSpec.accentPrimary,
                        onAddNewWithSku = onAddNewWithSku,
                        onApplyBarcodeToSearch = onApplyBarcodeToSearch
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleMatchedJacketContent(
    jacket: JacketItem,
    lookupToken: String,
    isIndonesian: Boolean,
    accentColor: Color,
    onJacketIdentified: (JacketItem) -> Unit,
    onApplyBarcodeToSearch: ((String) -> Unit)?,
    onQuickAdjustStock: ((JacketItem, Int) -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NetworkImageWithFallback(
            imageUrl = jacket.imageUrl,
            contentDescription = jacket.name,
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = jacket.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${jacket.sku} • ${jacket.style}",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${jacket.material} • ${CurrencyUtils.formatRupiah(jacket.unitPrice)}",
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StockCountPill(jacket = jacket)
                Text(
                    text = jacket.locationRack,
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    if (onQuickAdjustStock != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isIndonesian) "Ubah Stok Cepat" else "Quick Stock Update",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFCBD5E1)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { onQuickAdjustStock(jacket, -1) },
                    enabled = jacket.quantity > 0,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFF334155),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_scanner_stock_minus")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = if (isIndonesian) "Kurangi Stok 1" else "Decrease stock by 1"
                    )
                }
                Text(
                    text = "${jacket.quantity}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                FilledTonalIconButton(
                    onClick = { onQuickAdjustStock(jacket, 1) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFF065F46),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_scanner_stock_plus")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isIndonesian) "Tambah Stok 1" else "Increase stock by 1"
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (onApplyBarcodeToSearch != null) {
            OutlinedButton(
                onClick = { onApplyBarcodeToSearch(jacket.sku.ifBlank { lookupToken }) },
                border = BorderStroke(1.dp, accentColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("btn_apply_barcode_to_search")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isIndonesian) "Filter Daftar" else "Filter List",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }

        Button(
            onClick = { onJacketIdentified(jacket) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1.25f)
                .heightIn(min = 48.dp)
                .testTag("btn_open_identified_jacket_details")
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isIndonesian) "Lihat Detail" else "View Details",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MultipleMatchedJacketsContent(
    jackets: List<JacketItem>,
    lookupToken: String,
    isIndonesian: Boolean,
    accentColor: Color,
    onJacketIdentified: (JacketItem) -> Unit,
    onApplyBarcodeToSearch: ((String) -> Unit)?
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 210.dp)
            .testTag("scanner_multi_match_list")
    ) {
        items(jackets, key = { it.id }) { item ->
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onJacketIdentified(item) }
                    .testTag("scanner_match_item_${item.id}")
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NetworkImageWithFallback(
                        imageUrl = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.sku} • ${item.size} • ${item.locationRack}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    StockCountPill(jacket = item)
                }
            }
        }
    }

    if (onApplyBarcodeToSearch != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onApplyBarcodeToSearch(lookupToken) },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("btn_show_all_matches_in_inventory")
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isIndonesian) {
                    "Tampilkan Semua di Inventaris (${jackets.size})"
                } else {
                    "Show All in Inventory (${jackets.size})"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun UnrecognizedBarcodeContent(
    lookupToken: String,
    isIndonesian: Boolean,
    accentColor: Color,
    onAddNewWithSku: (String) -> Unit,
    onApplyBarcodeToSearch: ((String) -> Unit)?
) {
    Surface(
        color = Color(0xFF292524),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (isIndonesian) "Kode Terbaca:" else "Decoded Value:",
                fontSize = 11.sp,
                color = Color(0xFFA8A29E)
            )
            Text(
                text = lookupToken,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFDE047)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isIndonesian) {
                    "Tidak ada jaket yang cocok di database inventaris. Anda dapat menambahkannya sebagai item baru atau menerapkannya ke pencarian."
                } else {
                    "No matching jacket was found in the inventory database. You can register it as a new item or apply the code to search."
                },
                fontSize = 12.sp,
                color = Color(0xFFD6D3D1)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (onApplyBarcodeToSearch != null) {
            OutlinedButton(
                onClick = { onApplyBarcodeToSearch(lookupToken) },
                border = BorderStroke(1.dp, Color(0xFF78716C)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("btn_search_unmatched_barcode")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isIndonesian) "Cari" else "Search",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Button(
            onClick = { onAddNewWithSku(lookupToken) },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1.4f)
                .heightIn(min = 48.dp)
                .testTag("btn_add_new_jacket_with_scanned_sku")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isIndonesian) "Tambah Jaket Baru" else "Add New Jacket",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StockCountPill(jacket: JacketItem) {
    Surface(
        color = when (jacket.status) {
            StockStatusLevel.IN_STOCK -> Color(0xFF065F46)
            StockStatusLevel.LOW_STOCK -> Color(0xFF92400E)
            StockStatusLevel.OUT_OF_STOCK -> Color(0xFF991B1B)
        },
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = "${jacket.quantity} unit (${jacket.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}
