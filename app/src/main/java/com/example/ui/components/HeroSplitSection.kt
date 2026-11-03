package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CatalogData

@Composable
fun HeroSplitSection(
    isIndonesian: Boolean,
    totalJackets: Int,
    onAddClick: () -> Unit,
    onLanguageToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroText = if (isIndonesian) {
        "Kelola inventaris jaket Anda dengan presisi dan kejelasan."
    } else {
        "Manage your jacket inventory with precision and clarity."
    }

    val buttonText = if (isIndonesian) {
        "Tambah Jaket Baru"
    } else {
        "Add New Jacket"
    }

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2D3748), Color(0xFF4A5568))
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
    ) {
        // Subtle top utility bar with Language toggle and total stock badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isIndonesian) "$totalJackets Item di Gudang" else "$totalJackets Items in Stock",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A5568)
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { onLanguageToggle() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Language",
                    tint = Color(0xFF4A5568),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isIndonesian) "ID | EN" else "EN | ID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748)
                )
            }
        }

        // Split Hero Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Half: Hero warehouse image
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                NetworkImageWithFallback(
                    imageUrl = CatalogData.HERO_IMAGE_URL,
                    contentDescription = "Industrial warehouse with stacked jackets on metal racks",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Right Half: Title and CTA
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 6.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = heroText,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Centered Gradient CTA Button
                Box(
                    modifier = Modifier
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(25.dp))
                        .clip(RoundedCornerShape(25.dp))
                        .background(gradientBrush)
                        .clickable { onAddClick() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("add_new_jacket_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = buttonText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
