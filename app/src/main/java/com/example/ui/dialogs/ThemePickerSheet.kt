package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Interactive iPhone 18 Theme Picker ModalBottomSheet featuring live miniature
 * iPhone 18 device previews (Dynamic Island, Liquid Glass cards, Titanium bezel, and Dock).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerSheet(
    currentTheme: AppThemeMode,
    isIndonesian: Boolean,
    onSelectTheme: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val activeSpec = LocalAppThemeSpec.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = activeSpec.screenBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = activeSpec.separator)
        },
        modifier = Modifier.testTag("iphone18_theme_picker_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(activeSpec.accentPrimary, activeSpec.accentSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneIphone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isIndonesian) "Tema iPhone 18" else "iPhone 18 Themes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeSpec.labelPrimary
                        )
                        Text(
                            text = if (isIndonesian) {
                                "Pilih tampilan Liquid Glass & Titanium"
                            } else {
                                "Choose Liquid Glass & Titanium finish"
                            },
                            fontSize = 12.sp,
                            color = activeSpec.labelSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_close_theme_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isIndonesian) "Tutup" else "Close",
                        tint = activeSpec.labelSecondary
                    )
                }
            }

            // Theme Option Cards
            AppThemeMode.entries.forEach { mode ->
                val spec = getAppThemeSpec(mode)
                val isSelected = mode == currentTheme

                ThemePreviewCard(
                    spec = spec,
                    activeSpec = activeSpec,
                    isSelected = isSelected,
                    isIndonesian = isIndonesian,
                    onClick = { onSelectTheme(mode) }
                )
            }

            // Done Button
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeSpec.accentPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_apply_theme_done")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isIndonesian) "Terapkan Tema" else "Apply Theme",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    spec: AppThemeSpec,
    activeSpec: AppThemeSpec,
    isSelected: Boolean,
    isIndonesian: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = activeSpec.cardBackground,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) activeSpec.accentPrimary else activeSpec.cardBorder
        ),
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("theme_option_${spec.mode.storageKey}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Miniature iPhone 18 Mockup Frame
            Box(
                modifier = Modifier
                    .width(66.dp)
                    .height(104.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(spec.backgroundBrush)
                    .border(
                        width = 2.dp,
                        color = if (spec.isDark) Color(0xFF475069) else Color(0xFFCBD5E1),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(5.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mini Dynamic Island
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                    )

                    // Mini Hero Glass Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Brush.linearGradient(spec.heroGradient))
                    )

                    // Mini Squircle Icon Dots Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        spec.previewSwatches.forEach { swatch ->
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(swatch)
                                    .border(
                                        0.5.dp,
                                        Color.White.copy(alpha = 0.5f),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }

                    // Mini Floating Dock
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(spec.dockBackground)
                            .border(0.5.dp, spec.dockBorder, RoundedCornerShape(6.dp))
                    )
                }
            }

            // Text Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isIndonesian) spec.displayNameId else spec.displayNameEn,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeSpec.labelPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = spec.accentPrimary.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = spec.badgeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = spec.accentPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = if (isIndonesian) spec.taglineId else spec.taglineEn,
                    fontSize = 12.sp,
                    color = activeSpec.labelSecondary,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Color Swatch Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    spec.previewSwatches.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, activeSpec.separator, CircleShape)
                        )
                    }
                }
            }

            // Selection Indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) activeSpec.accentPrimary else activeSpec.chipContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = if (isIndonesian) "Dipilih" else "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
