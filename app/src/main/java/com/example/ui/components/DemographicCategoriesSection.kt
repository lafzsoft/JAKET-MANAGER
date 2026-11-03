package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CatalogData
import com.example.model.DemographicCategory
import com.example.model.JacketItem

@Composable
fun DemographicCategoriesSection(
    allJackets: List<JacketItem>,
    selectedCategory: String,
    isIndonesian: Boolean,
    onCategoryClick: (DemographicCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("demographic_categories_section")
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isIndonesian) "Kategori Jaket" else "Jacket Categories",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = if (isIndonesian) "Pilih kategori untuk memfilter produk" else "Select category to filter products",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (selectedCategory != "ALL") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE2E8F0))
                        .clickable {
                            // Clear category filter
                            CatalogData.GENDER_CATEGORIES.firstOrNull()?.let {
                                onCategoryClick(it.copy(titleId = "ALL"))
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isIndonesian) "Reset Filter" else "Reset Filter",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }

        // Horizontal Carousel of Categories
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(CatalogData.GENDER_CATEGORIES, key = { it.id }) { cat ->
                val title = if (isIndonesian) cat.titleId else cat.titleEn
                val desc = if (isIndonesian) cat.descId else cat.descEn
                val isSelected = selectedCategory.equals(cat.titleId, ignoreCase = true)
                val count = allJackets.count { it.genderCategory.equals(cat.titleId, ignoreCase = true) }

                val (iconVector, accentColor, gradient) = when (cat.id) {
                    "cat_pria" -> Triple(
                        Icons.Default.Male,
                        Color(0xFF1E3A8A),
                        listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
                    )
                    "cat_wanita" -> Triple(
                        Icons.Default.Female,
                        Color(0xFF9D174D),
                        listOf(Color(0xFFBE185D), Color(0xFFF472B6))
                    )
                    "cat_anak" -> Triple(
                        Icons.Default.ChildCare,
                        Color(0xFF047857),
                        listOf(Color(0xFF059669), Color(0xFF34D399))
                    )
                    else -> Triple(
                        Icons.Default.Group,
                        Color(0xFF6D28D9),
                        listOf(Color(0xFF6D28D9), Color(0xFF8B5CF6))
                    )
                }

                val borderModifier = if (isSelected) {
                    Modifier.border(2.dp, accentColor, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }

                Column(
                    modifier = Modifier
                        .width(150.dp)
                        .shadow(elevation = if (isSelected) 4.dp else 2.dp, shape = RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .then(borderModifier)
                        .background(if (isSelected) Color(0xFFF8FAFC) else Color.White)
                        .clickable { onCategoryClick(cat) }
                        .padding(bottom = 12.dp)
                        .testTag("category_card_${cat.id}")
                ) {
                    // Category Thumbnail with Overlay Gradient & Icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        NetworkImageWithFallback(
                            imageUrl = cat.imageUrl,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top-left icon badge
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(gradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Top-right item count badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$count",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Category Title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 10.dp, end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accentColor else Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Category Description
                    Text(
                        text = desc,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 2,
                        lineHeight = 14.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp, start = 10.dp, end = 10.dp)
                    )
                }
            }
        }
    }
}
