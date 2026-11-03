package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacketItem

enum class ChartDistributionMode {
    BY_STYLE,
    BY_SIZE,
    BY_CATEGORY
}

data class ChartBarData(
    val label: String,
    val value: Int,
    val color: Color,
    val secondaryLabel: String = ""
)

/**
 * Interactive Material 3 Bar Chart component visualizing jacket inventory distribution
 * across different jacket styles, sizes, and demographic categories.
 */
@Composable
fun InventoryDistributionCharts(
    allJackets: List<JacketItem>,
    isIndonesian: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(ChartDistributionMode.BY_STYLE) }
    var selectedBarItem by remember { mutableStateOf<ChartBarData?>(null) }

    val totalStock = remember(allJackets) { allJackets.sumOf { it.quantity } }

    // Prepare chart data based on active distribution mode
    val chartData = remember(allJackets, selectedMode) {
        when (selectedMode) {
            ChartDistributionMode.BY_STYLE -> {
                val styles = listOf("Bomber Jacket", "Parka", "Denim Jacket", "Trench Coat", "Puffer Jacket")
                val styleColors = listOf(
                    Color(0xFF2563EB), // Blue
                    Color(0xFF0D9488), // Teal
                    Color(0xFF6366F1), // Indigo
                    Color(0xFFEA580C), // Orange
                    Color(0xFF8B5CF6)  // Purple
                )
                styles.mapIndexed { index, styleName ->
                    val matching = allJackets.filter { it.style.equals(styleName, ignoreCase = true) }
                    val qty = matching.sumOf { it.quantity }
                    val shortName = when {
                        styleName.contains("Bomber") -> "Bomber"
                        styleName.contains("Parka") -> "Parka"
                        styleName.contains("Denim") -> "Denim"
                        styleName.contains("Trench") -> "Trench"
                        styleName.contains("Puffer") -> "Puffer"
                        else -> styleName.take(6)
                    }
                    ChartBarData(
                        label = shortName,
                        value = qty,
                        color = styleColors[index % styleColors.size],
                        secondaryLabel = styleName
                    )
                }
            }
            ChartDistributionMode.BY_SIZE -> {
                val sizes = listOf("XS", "S", "M", "L", "XL", "XXL", "3XL")
                val sizeColors = listOf(
                    Color(0xFF38BDF8),
                    Color(0xFF0284C7),
                    Color(0xFF2563EB),
                    Color(0xFF1D4ED8),
                    Color(0xFF4F46E5),
                    Color(0xFF7C3AED),
                    Color(0xFF9333EA)
                )
                sizes.mapIndexed { index, sizeCode ->
                    val matching = allJackets.filter { it.size.equals(sizeCode, ignoreCase = true) }
                    val qty = matching.sumOf { it.quantity }
                    ChartBarData(
                        label = sizeCode,
                        value = qty,
                        color = sizeColors[index % sizeColors.size],
                        secondaryLabel = if (isIndonesian) "Ukuran $sizeCode" else "Size $sizeCode"
                    )
                }
            }
            ChartDistributionMode.BY_CATEGORY -> {
                val categories = listOf("Jaket Pria", "Jaket Wanita", "Jaket Anak", "Jaket Unisex")
                val catColors = listOf(
                    Color(0xFF1E40AF),
                    Color(0xFFBE185D),
                    Color(0xFF047857),
                    Color(0xFF7C2D12)
                )
                categories.mapIndexed { index, catName ->
                    val matching = allJackets.filter { it.genderCategory.equals(catName, ignoreCase = true) }
                    val qty = matching.sumOf { it.quantity }
                    val shortName = when (catName) {
                        "Jaket Pria" -> if (isIndonesian) "Pria" else "Men"
                        "Jaket Wanita" -> if (isIndonesian) "Wanita" else "Women"
                        "Jaket Anak" -> if (isIndonesian) "Anak" else "Kids"
                        else -> "Unisex"
                    }
                    ChartBarData(
                        label = shortName,
                        value = qty,
                        color = catColors[index % catColors.size],
                        secondaryLabel = catName
                    )
                }
            }
        }
    }

    val maxVal = remember(chartData) { (chartData.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("inventory_distribution_dashboard_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title & Recharts/Chart Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isIndonesian) "Distribusi Stok Jaket" else "Jacket Stock Distribution",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isIndonesian) "Visualisasi Diagram Batang Stok" else "Bar Chart Inventory Analytics",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Total stock pill
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$totalStock unit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Mode Selector Tabs: By Style / By Size / By Category
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    ChartDistributionMode.BY_STYLE to (if (isIndonesian) "Model" else "Styles"),
                    ChartDistributionMode.BY_SIZE to (if (isIndonesian) "Ukuran" else "Sizes"),
                    ChartDistributionMode.BY_CATEGORY to (if (isIndonesian) "Kategori" else "Category")
                )

                tabs.forEach { (mode, label) ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable {
                                selectedMode = mode
                                selectedBarItem = null
                            }
                            .padding(vertical = 7.dp)
                            .testTag("tab_chart_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Interactive Bar Chart Graphic Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("inventory_bar_chart_area")
            ) {
                // Background grid lines
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE2E8F0))
                        )
                    }
                }

                // Vertical Bars
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, bottom = 26.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    chartData.forEach { barItem ->
                        val barHeightFraction = (barItem.value.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)
                        val animatedFraction by animateFloatAsState(
                            targetValue = barHeightFraction,
                            animationSpec = tween(durationMillis = 600),
                            label = "bar_anim_${barItem.label}"
                        )
                        val isSelected = selectedBarItem?.label == barItem.label

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    selectedBarItem = if (isSelected) null else barItem
                                }
                                .testTag("bar_chart_item_${barItem.label.lowercase()}")
                        ) {
                            // Value text on top of bar
                            Text(
                                text = "${barItem.value}",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF64748B),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )

                            // Animated Bar with rounded top
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (chartData.size > 5) 0.55f else 0.45f)
                                    .fillMaxHeight(animatedFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = if (isSelected) {
                                                listOf(barItem.color, barItem.color.copy(alpha = 0.8f))
                                            } else {
                                                listOf(barItem.color.copy(alpha = 0.85f), barItem.color.copy(alpha = 0.6f))
                                            }
                                        )
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color(0xFF0F172A) else Color.Transparent,
                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    )
                            )
                        }
                    }
                }

                // X-Axis Labels Row below bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    chartData.forEach { barItem ->
                        val isSelected = selectedBarItem?.label == barItem.label
                        Text(
                            text = barItem.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selected Bar Interactive Tooltip/Detail Card
            AnimatedVisibility(
                visible = selectedBarItem != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedBarItem?.let { selected ->
                    val percentage = if (totalStock > 0) {
                        (selected.value.toFloat() / totalStock.toFloat()) * 100f
                    } else 0f

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, selected.color.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("selected_chart_bar_details")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(selected.color)
                                )
                                Column {
                                    Text(
                                        text = selected.secondaryLabel.ifBlank { selected.label },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = if (isIndonesian)
                                            "${selected.value} unit tersedia (${String.format("%.1f", percentage)}% dari total gudang)"
                                        else
                                            "${selected.value} units in stock (${String.format("%.1f", percentage)}% of total warehouse)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedBarItem = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Summary legend items row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                items(chartData) { item ->
                    val isSelected = selectedBarItem?.label == item.label
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) item.color.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, item.color) else null,
                        modifier = Modifier.clickable {
                            selectedBarItem = if (isSelected) null else item
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                            )
                            Text(
                                text = "${item.label}: ${item.value}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }
    }
}
