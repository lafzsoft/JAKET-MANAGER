package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.SizeCategory

@Composable
fun SizesGridSection(
    isIndonesian: Boolean,
    onSizeClick: (SizeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val sectionHeader = if (isIndonesian) "Ukuran Jaket" else "Jacket Sizes"

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

        // 2-Column Grid Layout (pairs of items)
        val sizePairs = CatalogData.SIZES.chunked(2)
        sizePairs.forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { size ->
                    val desc = if (isIndonesian) size.descId else size.descEn

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight()
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable { onSizeClick(size) }
                            .padding(bottom = 12.dp)
                            .testTag("size_card_${size.id}")
                    ) {
                        NetworkImageWithFallback(
                            imageUrl = size.imageUrl,
                            contentDescription = "Size ${size.title}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(112.dp)
                        )

                        Text(
                            text = size.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A),
                            maxLines = 1,
                            modifier = Modifier.padding(top = 10.dp, start = 12.dp, end = 12.dp)
                        )

                        Text(
                            text = desc,
                            fontSize = 13.sp,
                            color = Color(0xFF666666),
                            maxLines = 2,
                            lineHeight = 17.sp,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp)
                        )
                    }
                }

                // If odd number in row (not applicable for 6, but good defensive practice)
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
