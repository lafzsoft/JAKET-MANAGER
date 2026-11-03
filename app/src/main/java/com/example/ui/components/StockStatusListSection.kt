package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CatalogData
import com.example.model.StockStatusCategory
import com.example.model.StockStatusLevel

@Composable
fun StockStatusListSection(
    isIndonesian: Boolean,
    onStatusClick: (StockStatusCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val sectionHeader = if (isIndonesian) "Status Stok" else "Stock Status"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 16.dp)
    ) {
        // Section Header
        Text(
            text = sectionHeader,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Vertical List of Landscape Cards
        CatalogData.STOCK_STATUSES.forEach { statusCat ->
            val title = if (isIndonesian) statusCat.titleId else statusCat.titleEn
            val desc = if (isIndonesian) statusCat.descId else statusCat.descEn

            val badgeColor = when (statusCat.status) {
                StockStatusLevel.IN_STOCK -> Color(0xFF16A34A)
                StockStatusLevel.LOW_STOCK -> Color(0xFFD97706)
                StockStatusLevel.OUT_OF_STOCK -> Color(0xFFDC2626)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { onStatusClick(statusCat) }
                    .padding(12.dp)
                    .testTag("status_card_${statusCat.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fixed Image 100x80 dp with rounded corners
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    NetworkImageWithFallback(
                        imageUrl = statusCat.imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Info Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor)
                        )
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A),
                            maxLines = 1
                        )
                    }

                    Text(
                        text = desc,
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        maxLines = 2,
                        lineHeight = 17.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
