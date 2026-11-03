package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Friendly Empty State view for the Jacket Inventory app.
 *
 * Handles both:
 * 1. Global empty state: When no jackets exist in the database (shows friendly illustration,
 *    warm message, and action buttons to add jacket or load sample items).
 * 2. Filter empty state: When jackets exist in the DB, but none match the current filter or search.
 */
@Composable
fun EmptyInventoryView(
    isDatabaseEmpty: Boolean,
    isIndonesian: Boolean = false,
    activeFilterName: String = "",
    modifier: Modifier = Modifier,
    onAddJacketClick: () -> Unit = {},
    onLoadSampleStock: () -> Unit = {},
    onResetFilter: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(if (isDatabaseEmpty) "empty_inventory_card" else "empty_filtered_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE2E8F0), Color(0xFFEDF2F7))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isDatabaseEmpty) {
                // ==================== GLOBAL EMPTY STATE ====================
                // Friendly Illustration with rounded corners & subtle shadow/glow
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF8FAFC)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_empty_closet),
                        contentDescription = if (isIndonesian) "Ilustrasi lemari pakaian kosong yang ramah" else "Friendly empty wardrobe illustration",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Friendly Status Pill
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEF3C7),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFFFDE68A), Color(0xFFFCD34D)))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD97706))
                        )
                        Text(
                            text = if (isIndonesian) "Inventaris Kosong" else "Closet Waiting",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Welcoming Headline
                Text(
                    text = if (isIndonesian) "Lemari Jaket Masih Kosong" else "Your Wardrobe is Waiting",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Warm Explanatory Body
                Text(
                    text = if (isIndonesian)
                        "Belum ada jaket yang tersimpan. Tambahkan jaket pertamamu untuk mulai melacak model, ukuran, rak gudang, dan jumlah stok secara rapi!"
                    else
                        "No jackets are currently logged in your inventory. Add your first jacket to track styles, sizes, warehouse racks, and stock quantities!",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Action Button: Add First Jacket
                Button(
                    onClick = onAddJacketClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("empty_state_add_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isIndonesian) "Tambah Jaket Pertama" else "Add First Jacket",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Action Button: Load Sample Stock
                OutlinedButton(
                    onClick = onLoadSampleStock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("empty_state_sample_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF334155)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isIndonesian) "Muat Stok Sampel (Default)" else "Load Sample Jackets",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // ==================== FILTERED EMPTY STATE ====================
                // Illustrated empty filter badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = if (isIndonesian) "Tidak Ada Jaket yang Sesuai" else "No Matching Jackets Found",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Explanation
                val explanation = if (activeFilterName.isNotBlank() && activeFilterName != "ALL") {
                    if (isIndonesian) {
                        "Tidak ditemukan jaket dengan filter '$activeFilterName'. Coba pilih model jaket lain atau reset filter."
                    } else {
                        "No jackets match the '$activeFilterName' filter. Try selecting a different style or resetting your filters."
                    }
                } else {
                    if (isIndonesian) {
                        "Kriteria pencarian tidak cocok dengan jaket manapun. Silakan coba kata kunci lain."
                    } else {
                        "No jackets match your current search criteria. Try a different keyword."
                    }
                }

                Text(
                    text = explanation,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Reset filter button
                Button(
                    onClick = onResetFilter,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("empty_state_reset_filter_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2D3748),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isIndonesian) "Tampilkan Semua Jaket" else "Show All Jackets",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
