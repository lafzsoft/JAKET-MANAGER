package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CatalogData
import com.example.model.MaterialCategory

@Composable
fun MaterialsGridSection(
    isIndonesian: Boolean,
    onMaterialClick: (MaterialCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val sectionHeader = if (isIndonesian) "Bahan Jaket" else "Jacket Materials"

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

        // 3-Column Grid Layout (rows of 3)
        val materialTriplets = CatalogData.MATERIALS.chunked(3)
        materialTriplets.forEach { triplet ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                triplet.forEach { material ->
                    val title = if (isIndonesian) material.titleId else material.titleEn
                    val desc = if (isIndonesian) material.descId else material.descEn

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight()
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable { onMaterialClick(material) }
                            .padding(horizontal = 6.dp, vertical = 12.dp)
                            .testTag("material_card_${material.id}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Circular Image (80x80 dp)
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                        ) {
                            NetworkImageWithFallback(
                                imageUrl = material.imageUrl,
                                contentDescription = title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Centered Title
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        // Centered Description
                        Text(
                            text = desc,
                            fontSize = 11.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            lineHeight = 15.sp,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
