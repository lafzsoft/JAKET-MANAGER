package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.model.ColorCategory

@Composable
fun ColorsCarouselSection(
    isIndonesian: Boolean,
    onColorClick: (ColorCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val sectionHeader = if (isIndonesian) "Warna Jaket" else "Jacket Colors"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        // Section Header
        Text(
            text = sectionHeader,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Horizontal Carousel
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(CatalogData.COLORS, key = { it.id }) { colorItem ->
                val title = if (isIndonesian) colorItem.titleId else colorItem.titleEn
                val desc = if (isIndonesian) colorItem.descId else colorItem.descEn

                Column(
                    modifier = Modifier
                        .width(133.dp)
                        .wrapContentHeight()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable { onColorClick(colorItem) }
                        .padding(bottom = 12.dp)
                        .testTag("color_card_${colorItem.id}")
                ) {
                    // Card Image
                    NetworkImageWithFallback(
                        imageUrl = colorItem.imageUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp)
                    )

                    // Color title with swatch dot
                    Row(
                        modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(colorItem.hexColor))
                                .border(0.6.dp, Color(0xFFCBD5E1), CircleShape)
                        )
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Description
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        maxLines = 2,
                        lineHeight = 16.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp, start = 10.dp, end = 10.dp)
                    )
                }
            }
        }
    }
}
