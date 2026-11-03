package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

/**
 * Grid card component for displaying an individual jacket item in the main inventory grid.
 * Displays product image, name, current stock level indicator, SKU, size, category, and quick actions.
 */
@Composable
fun JacketGridCard(
    jacket: JacketItem,
    isIndonesian: Boolean,
    onAdjustQuantity: (delta: Int) -> Unit,
    onRestock: () -> Unit,
    onEditImage: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val stockColor = when (jacket.status) {
        StockStatusLevel.IN_STOCK -> Color(0xFF059669) // Emerald Green
        StockStatusLevel.LOW_STOCK -> Color(0xFFD97706) // Amber
        StockStatusLevel.OUT_OF_STOCK -> Color(0xFFDC2626) // Crimson Red
    }

    val stockBgColor = when (jacket.status) {
        StockStatusLevel.IN_STOCK -> Color(0xFFECFDF5)
        StockStatusLevel.LOW_STOCK -> Color(0xFFFFFBEB)
        StockStatusLevel.OUT_OF_STOCK -> Color(0xFFFEF2F2)
    }

    val stockStatusText = when (jacket.status) {
        StockStatusLevel.IN_STOCK -> if (isIndonesian) "Tersedia" else "In Stock"
        StockStatusLevel.LOW_STOCK -> if (isIndonesian) "Menipis" else "Low Stock"
        StockStatusLevel.OUT_OF_STOCK -> if (isIndonesian) "Habis" else "Out of Stock"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("jacket_grid_card_${jacket.id}")
            .testTag("inventory_full_item_${jacket.sku}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image Header with Status Pill and Edit Photo Trigger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
                    .background(Color(0xFFF1F5F9))
                    .testTag("thumbnail_sku_${jacket.sku}")
            ) {
                NetworkImageWithFallback(
                    imageUrl = jacket.imageUrl,
                    contentDescription = jacket.name,
                    modifier = Modifier.fillMaxSize()
                )

                // Stock Status Badge Overlay (Top Start)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = stockBgColor.copy(alpha = 0.95f),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(stockColor)
                        )
                        Text(
                            text = stockStatusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = stockColor
                        )
                    }
                }

                // Edit Photo Button (Top End)
                IconButton(
                    onClick = onEditImage,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(30.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0xD90F172A))
                        .testTag("btn_edit_image_${jacket.sku}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = if (isIndonesian) "Ubah Foto SKU" else "Change SKU Photo",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Storage Rack Location Badge (Bottom End)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xD90F172A),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = jacket.locationRack,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            // Card Body: Details & Stock Level
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // SKU Code & Size Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = jacket.sku,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = jacket.size,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Product Name
                Text(
                    text = jacket.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("jacket_grid_name_${jacket.sku}")
                )

                // Category & Style
                Text(
                    text = "${jacket.genderCategory} • ${jacket.style}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Prominent Current Stock Level Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = stockBgColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("jacket_grid_stock_${jacket.sku}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIndonesian) "Stok Saat Ini:" else "Current Stock:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )

                        Text(
                            text = if (jacket.quantity > 0) {
                                "${jacket.quantity} Unit"
                            } else {
                                if (isIndonesian) "Kosong" else "0 Units"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = stockColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Stock Adjustment Stepper & Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stepper: Minus, Quantity, Plus
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onAdjustQuantity(-1) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = if (isIndonesian) "Kurangi Stok" else "Decrease Stock",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        Text(
                            text = "${jacket.quantity}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        IconButton(
                            onClick = { onAdjustQuantity(1) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = if (isIndonesian) "Tambah Stok" else "Increase Stock",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Delete or Restock Quick Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (jacket.status != StockStatusLevel.IN_STOCK) {
                            IconButton(
                                onClick = onRestock,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = if (isIndonesian) "Restock 15 Unit" else "Restock 15 Units",
                                    tint = Color(0xFF166534),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = if (isIndonesian) "Hapus Jaket" else "Delete Jacket",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
