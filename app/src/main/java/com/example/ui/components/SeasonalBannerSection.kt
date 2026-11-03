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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CatalogData

@Composable
fun SeasonalBannerSection(
    isIndonesian: Boolean,
    onBannerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bannerData = CatalogData.SEASONAL_BANNER
    val title = if (isIndonesian) bannerData.titleId else bannerData.titleEn
    val subtitle = if (isIndonesian) bannerData.subtitleId else bannerData.subtitleEn

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onBannerClick() }
            .padding(bottom = 16.dp)
            .testTag("seasonal_collection_banner")
    ) {
        // Banner Image
        NetworkImageWithFallback(
            imageUrl = bannerData.imageUrl,
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        // Title
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(top = 16.dp, start = 14.dp, end = 14.dp)
        )

        // Subtitle
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            lineHeight = 20.sp,
            maxLines = 3,
            modifier = Modifier.padding(top = 6.dp, start = 14.dp, end = 14.dp)
        )
    }
}
