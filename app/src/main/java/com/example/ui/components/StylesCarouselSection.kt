package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.model.StyleCategory

@Composable
fun StylesCarouselSection(
    isIndonesian: Boolean,
    onStyleClick: (StyleCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(CatalogData.STYLES, key = { it.id }) { style ->
            val title = if (isIndonesian) style.titleId else style.titleEn
            val desc = if (isIndonesian) style.descId else style.descEn

            Column(
                modifier = Modifier
                    .width(133.dp)
                    .wrapContentHeight()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { onStyleClick(style) }
                    .padding(bottom = 12.dp)
                    .testTag("style_card_${style.id}")
            ) {
                // Card Image
                NetworkImageWithFallback(
                    imageUrl = style.imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                )

                // Title
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp)
                )

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
