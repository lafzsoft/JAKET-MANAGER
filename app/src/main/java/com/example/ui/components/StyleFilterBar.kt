package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.JacketItem

enum class JacketSortOrder {
    NAME_ASC,
    NAME_DESC,
    QTY_DESC,
    QTY_ASC,
    STYLE_ASC,
    STYLE_DESC
}

@Composable
fun StyleFilterBar(
    allJackets: List<JacketItem>,
    selectedStyle: String, // "ALL" or specific style name
    selectedCategory: String = "ALL", // "ALL" or demographic category (Jaket Pria, Jaket Wanita, Jaket Anak, Jaket Unisex)
    sortOrder: JacketSortOrder,
    isIndonesian: Boolean,
    onStyleSelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit = {},
    onSortSelected: (JacketSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFilterDropdownExpanded by remember { mutableStateOf(false) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isSortDropdownExpanded by remember { mutableStateOf(false) }

    val demographicCategories = remember {
        listOf("Jaket Pria", "Jaket Wanita", "Jaket Anak", "Jaket Unisex")
    }

    // Collect all unique styles dynamically from jackets, preserving order and including standard styles
    val availableStyles = remember(allJackets) {
        val standard = listOf("Bomber Jacket", "Parka", "Denim Jacket", "Trench Coat", "Puffer Jacket")
        val fromJackets = allJackets.map { it.style }.distinct()
        (standard + fromJackets).distinct()
    }

    val activeSortLabel = remember(sortOrder, isIndonesian) {
        when (sortOrder) {
            JacketSortOrder.NAME_ASC -> if (isIndonesian) "Nama: A-Z" else "Name: A-Z"
            JacketSortOrder.NAME_DESC -> if (isIndonesian) "Nama: Z-A" else "Name: Z-A"
            JacketSortOrder.QTY_DESC -> if (isIndonesian) "Stok: Terbanyak" else "Stock: High-Low"
            JacketSortOrder.QTY_ASC -> if (isIndonesian) "Stok: Tersedikit" else "Stock: Low-High"
            JacketSortOrder.STYLE_ASC -> if (isIndonesian) "Model: A-Z" else "Style: A-Z"
            JacketSortOrder.STYLE_DESC -> if (isIndonesian) "Model: Z-A" else "Style: Z-A"
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("style_filter_bar")
    ) {
        // Controls Row: Title, Filter Dropdown, and Sort Dropdown Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color(0xFF4A5568),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isIndonesian) "Filter & Urutan Jaket" else "Filter & Jacket Sorting",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Sort Dropdown Menu Button
                Box {
                    FilledTonalButton(
                        onClick = { isSortDropdownExpanded = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("jacket_sort_dropdown_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort Menu",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeSortLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isSortDropdownExpanded,
                        onDismissRequest = { isSortDropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        Text(
                            text = if (isIndonesian) "Urutkan Berdasarkan Stok:" else "Sort by Stock Quantity:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isIndonesian) "Stok: Terbanyak (Tinggi ke Rendah)" else "Stock: High to Low",
                                    fontWeight = if (sortOrder == JacketSortOrder.QTY_DESC) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortSelected(JacketSortOrder.QTY_DESC)
                                isSortDropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF2D3748))
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isIndonesian) "Stok: Tersedikit (Rendah ke Tinggi)" else "Stock: Low to High",
                                    fontWeight = if (sortOrder == JacketSortOrder.QTY_ASC) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortSelected(JacketSortOrder.QTY_ASC)
                                isSortDropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF2D3748))
                            }
                        )

                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = if (isIndonesian) "Urutkan Abjad Nama:" else "Sort Alphabetically by Name:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isIndonesian) "Nama: A ke Z" else "Name: A to Z",
                                    fontWeight = if (sortOrder == JacketSortOrder.NAME_ASC) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortSelected(JacketSortOrder.NAME_ASC)
                                isSortDropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.TextFields, contentDescription = null, tint = Color(0xFF2D3748))
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isIndonesian) "Nama: Z ke A" else "Name: Z to A",
                                    fontWeight = if (sortOrder == JacketSortOrder.NAME_DESC) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortSelected(JacketSortOrder.NAME_DESC)
                                isSortDropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.TextFields, contentDescription = null, tint = Color(0xFF2D3748))
                            }
                        )

                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = if (isIndonesian) "Urutkan Model:" else "Sort by Style:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isIndonesian) "Model: A ke Z" else "Style: A to Z",
                                    fontWeight = if (sortOrder == JacketSortOrder.STYLE_ASC) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortSelected(JacketSortOrder.STYLE_ASC)
                                isSortDropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SortByAlpha, contentDescription = null, tint = Color(0xFF2D3748))
                            }
                        )
                    }
                }

                // 2. Style Filter Dropdown Menu Button
                Box {
                    FilledTonalButton(
                        onClick = { isFilterDropdownExpanded = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.testTag("style_filter_dropdown_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter Dropdown",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (selectedStyle == "ALL") {
                                if (isIndonesian) "Model" else "Styles"
                            } else {
                                selectedStyle.take(10)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isFilterDropdownExpanded,
                        onDismissRequest = { isFilterDropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        Text(
                            text = if (isIndonesian) "Saring Berdasarkan Model:" else "Filter by Style:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )

                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isIndonesian) "Semua Model" else "All Styles",
                                        fontWeight = if (selectedStyle == "ALL") FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedStyle == "ALL") Color(0xFF2D3748) else Color(0xFF1A1A1A)
                                    )
                                    Text(
                                        text = "${allJackets.size}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            },
                            onClick = {
                                onStyleSelected("ALL")
                                isFilterDropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Apps, contentDescription = null, tint = Color(0xFF4A5568))
                            }
                        )

                        availableStyles.forEach { style ->
                            val count = allJackets.count { it.style.equals(style, ignoreCase = true) }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = style,
                                            fontWeight = if (selectedStyle.equals(style, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedStyle.equals(style, ignoreCase = true)) Color(0xFF2D3748) else Color(0xFF1A1A1A)
                                        )
                                        Text(
                                            text = "$count",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                },
                                onClick = {
                                    onStyleSelected(style)
                                    isFilterDropdownExpanded = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Checkroom, contentDescription = null, tint = Color(0xFF4A5568))
                                }
                            )
                        }
                    }
                }

                // 3. Category Filter Dropdown Menu Button
                Box {
                    FilledTonalButton(
                        onClick = { isCategoryDropdownExpanded = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedCategory != "ALL") Color(0xFF1E293B) else Color.White,
                            contentColor = if (selectedCategory != "ALL") Color.White else Color(0xFF4A5568)
                        ),
                        modifier = Modifier.testTag("category_filter_dropdown_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Category Dropdown",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (selectedCategory == "ALL") {
                                if (isIndonesian) "Kategori" else "Category"
                            } else {
                                selectedCategory.replace("Jaket ", "")
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        Text(
                            text = if (isIndonesian) "Saring Berdasarkan Kategori:" else "Filter by Category:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )

                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isIndonesian) "Semua Kategori" else "All Categories",
                                        fontWeight = if (selectedCategory == "ALL") FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedCategory == "ALL") Color(0xFF2D3748) else Color(0xFF1A1A1A)
                                    )
                                    Text(
                                        text = "${allJackets.size}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            },
                            onClick = {
                                onCategorySelected("ALL")
                                isCategoryDropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.AllInclusive, contentDescription = null, tint = Color(0xFF4A5568))
                            }
                        )

                        demographicCategories.forEach { cat ->
                            val count = allJackets.count { it.genderCategory.equals(cat, ignoreCase = true) }
                            val icon = when (cat) {
                                "Jaket Pria" -> Icons.Default.Male
                                "Jaket Wanita" -> Icons.Default.Female
                                "Jaket Anak" -> Icons.Default.ChildCare
                                else -> Icons.Default.Group
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cat,
                                            fontWeight = if (selectedCategory.equals(cat, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedCategory.equals(cat, ignoreCase = true)) Color(0xFF2D3748) else Color(0xFF1A1A1A)
                                        )
                                        Text(
                                            text = "$count",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                },
                                onClick = {
                                    onCategorySelected(cat)
                                    isCategoryDropdownExpanded = false
                                },
                                leadingIcon = {
                                    Icon(icon, contentDescription = null, tint = Color(0xFF4A5568))
                                }
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Category Filter Chips Row (Demographic Categories: Jaket Pria, Jaket Wanita, Jaket Anak, Jaket Unisex)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val isAllSelected = selectedCategory == "ALL"
                FilterChip(
                    selected = isAllSelected,
                    onClick = { onCategorySelected("ALL") },
                    label = {
                        Text(
                            text = if (isIndonesian) "Semua Kategori (${allJackets.size})" else "All Categories (${allJackets.size})",
                            fontSize = 12.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1E293B),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("category_chip_all")
                )
            }

            items(demographicCategories) { cat ->
                val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                val count = allJackets.count { it.genderCategory.equals(cat, ignoreCase = true) }
                val icon = when (cat) {
                    "Jaket Pria" -> Icons.Default.Male
                    "Jaket Wanita" -> Icons.Default.Female
                    "Jaket Anak" -> Icons.Default.ChildCare
                    else -> Icons.Default.Group
                }
                val tagSlug = when (cat) {
                    "Jaket Pria" -> "pria"
                    "Jaket Wanita" -> "wanita"
                    "Jaket Anak" -> "anak"
                    else -> "unisex"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) onCategorySelected("ALL") else onCategorySelected(cat)
                    },
                    label = {
                        Text(
                            text = "$cat ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1E293B),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("category_chip_$tagSlug")
                )
            }
        }

        // Horizontal Quick Sort Chips Row (Stock Quantity & Alphabetical Name)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = if (isIndonesian) "Urutkan:" else "Sort:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // 1. Stock High to Low
            item {
                val isSelected = sortOrder == JacketSortOrder.QTY_DESC
                FilterChip(
                    selected = isSelected,
                    onClick = { onSortSelected(JacketSortOrder.QTY_DESC) },
                    label = {
                        Text(
                            text = if (isIndonesian) "Stok Terbanyak" else "Stock: High-Low",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2D3748),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("sort_chip_qty_desc")
                )
            }

            // 2. Stock Low to High
            item {
                val isSelected = sortOrder == JacketSortOrder.QTY_ASC
                FilterChip(
                    selected = isSelected,
                    onClick = { onSortSelected(JacketSortOrder.QTY_ASC) },
                    label = {
                        Text(
                            text = if (isIndonesian) "Stok Tersedikit" else "Stock: Low-High",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2D3748),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("sort_chip_qty_asc")
                )
            }

            // 3. Name A to Z
            item {
                val isSelected = sortOrder == JacketSortOrder.NAME_ASC
                FilterChip(
                    selected = isSelected,
                    onClick = { onSortSelected(JacketSortOrder.NAME_ASC) },
                    label = {
                        Text(
                            text = if (isIndonesian) "Nama: A-Z" else "Name: A-Z",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.SortByAlpha,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2D3748),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("sort_chip_name_asc")
                )
            }

            // 4. Name Z to A
            item {
                val isSelected = sortOrder == JacketSortOrder.NAME_DESC
                FilterChip(
                    selected = isSelected,
                    onClick = { onSortSelected(JacketSortOrder.NAME_DESC) },
                    label = {
                        Text(
                            text = if (isIndonesian) "Nama: Z-A" else "Name: Z-A",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.SortByAlpha,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2D3748),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("sort_chip_name_desc")
                )
            }

            // 5. Style A to Z
            item {
                val isSelected = sortOrder == JacketSortOrder.STYLE_ASC
                FilterChip(
                    selected = isSelected,
                    onClick = { onSortSelected(JacketSortOrder.STYLE_ASC) },
                    label = {
                        Text(
                            text = if (isIndonesian) "Model: A-Z" else "Style: A-Z",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2D3748),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("sort_chip_style_asc")
                )
            }
        }

        // Horizontal Tab / FilterChip Row for Styles
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" Tab
            item {
                val isAllSelected = selectedStyle == "ALL"
                FilterChip(
                    selected = isAllSelected,
                    onClick = { onStyleSelected("ALL") },
                    label = {
                        Text(
                            text = if (isIndonesian) "Semua (${allJackets.size})" else "All Styles (${allJackets.size})",
                            fontSize = 12.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AllInclusive,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2D3748),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("style_tab_all")
                )
            }

            // Each Style Tab with live count
            items(availableStyles) { style ->
                val isSelected = selectedStyle.equals(style, ignoreCase = true)
                val count = allJackets.count { it.style.equals(style, ignoreCase = true) }

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) onStyleSelected("ALL") else onStyleSelected(style)
                    },
                    label = {
                        Text(
                            text = "$style ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2D3748),
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("style_tab_${style.replace(" ", "_").lowercase()}")
                )
            }
        }

        // Active Category Filter Banner (if a specific category is active)
        if (selectedCategory != "ALL") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E7FF))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Color(0xFF3730A3),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isIndonesian) "Menampilkan kategori: $selectedCategory" else "Filtered by category: $selectedCategory",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E1B4B)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onCategorySelected("ALL") }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Category Filter",
                        tint = Color(0xFF3730A3),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Active Filter Banner (if a specific style is active)
        if (selectedStyle != "ALL") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        tint = Color(0xFF2D3748),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isIndonesian) "Menampilkan model: $selectedStyle" else "Filtered by style: $selectedStyle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onStyleSelected("ALL") }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Style Filter",
                        tint = Color(0xFF4A5568),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
