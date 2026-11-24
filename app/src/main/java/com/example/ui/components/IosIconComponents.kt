package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Signature iPhone / iOS Squircle App Icon with gradient fill, subtle top specular gloss,
 * spring press animation, and optional iOS red notification badge.
 */
@Composable
fun IosAppIconTile(
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    badgeText: String? = null,
    iconSize: Dp = 62.dp,
    glyphSize: Dp = 30.dp,
    labelColor: Color = LocalAppThemeSpec.current.labelPrimary,
    testTagName: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ios_icon_press_scale"
    )

    val cornerRadius = (iconSize.value * 0.24f).dp
    val squircleShape = RoundedCornerShape(cornerRadius)

    val clickableModifier = if (testTagName != null) {
        modifier.testTag(testTagName)
    } else {
        modifier
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = clickableModifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(iconSize + 8.dp)
        ) {
            // Squircle App Icon Surface
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .shadow(
                        elevation = 6.dp,
                        shape = squircleShape,
                        ambientColor = gradientColors.first().copy(alpha = 0.35f),
                        spotColor = gradientColors.last().copy(alpha = 0.45f)
                    )
                    .clip(squircleShape)
                    .background(Brush.linearGradient(gradientColors))
                    .border(
                        width = 0.75.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f),
                                Color.White.copy(alpha = 0.12f)
                            )
                        ),
                        shape = squircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subtle iOS specular shine on upper half
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.48f)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(glyphSize)
                )
            }

            // Signature iOS Red Notification Badge overlapping top-right corner
            val resolvedBadge = badgeText ?: if (badgeCount > 0) {
                if (badgeCount > 99) "99+" else badgeCount.toString()
            } else null

            if (resolvedBadge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-1).dp, y = 1.dp)
                        .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                        .clip(CircleShape)
                        .background(IosSystemRed)
                        .border(1.8.dp, Color.White, CircleShape)
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = resolvedBadge,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Compact 36dp iOS Settings-style squircle icon badge for list rows, drawers, and headers.
 */
@Composable
fun IosSmallSquircleIcon(
    icon: ImageVector,
    gradientColors: List<Color>,
    contentDescription: String? = null,
    size: Dp = 32.dp,
    glyphSize: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape((size.value * 0.25f).dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(Brush.linearGradient(gradientColors))
            .border(0.5.dp, Color.White.copy(alpha = 0.4f), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(glyphSize)
        )
    }
}

/**
 * iPhone Home Screen App Grid (4 columns x 2 rows) providing instant app-icon access
 * to every core module of Jacket Manager.
 */
@Composable
fun IosHomeAppGridSection(
    isIndonesian: Boolean,
    totalModels: Int,
    lowStockCount: Int,
    unpaidKasbonCount: Int,
    onInventoryClick: () -> Unit,
    onAddClick: () -> Unit,
    onScanClick: () -> Unit,
    onProductionClick: () -> Unit,
    onKasbonClick: () -> Unit,
    onReportsClick: () -> Unit,
    onRoomListClick: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalAppThemeSpec.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("ios_app_icon_grid_section")
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = themeSpec.glassSurface,
            shadowElevation = if (themeSpec.isDark) 6.dp else 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, themeSpec.glassBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isIndonesian) "Aplikasi Gudang" else "Warehouse Apps",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeSpec.labelSecondary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(themeSpec.accentPrimary)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(themeSpec.separator)
                        )
                    }
                }

                // Row 1: Inventaris, Tambah, Scan QR, Produksi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IosAppIconTile(
                        label = if (isIndonesian) "Inventaris" else "Inventory",
                        icon = Icons.Default.Inventory2,
                        gradientColors = listOf(Color(0xFF0A84FF), Color(0xFF5AC8FA)),
                        badgeCount = totalModels,
                        onClick = onInventoryClick,
                        modifier = Modifier.weight(1f),
                        testTagName = "ios_app_icon_inventory"
                    )
                    IosAppIconTile(
                        label = if (isIndonesian) "Tambah" else "Add New",
                        icon = Icons.Default.AddCircle,
                        gradientColors = listOf(Color(0xFF34C759), Color(0xFF30D158)),
                        onClick = onAddClick,
                        modifier = Modifier.weight(1f),
                        testTagName = "ios_app_icon_add"
                    )
                    IosAppIconTile(
                        label = if (isIndonesian) "Scan Tag" else "Scan Tag",
                        icon = Icons.Default.QrCodeScanner,
                        gradientColors = listOf(Color(0xFF5856D6), Color(0xFFAF52DE)),
                        onClick = onScanClick,
                        modifier = Modifier.weight(1f),
                        testTagName = "ios_app_icon_scan"
                    )
                    IosAppIconTile(
                        label = if (isIndonesian) "Produksi" else "Produce",
                        icon = Icons.Default.PrecisionManufacturing,
                        gradientColors = listOf(Color(0xFFFF9500), Color(0xFFFFCC00)),
                        onClick = onProductionClick,
                        modifier = Modifier.weight(1f),
                        testTagName = "ios_app_icon_production"
                    )
                }

                // Row 2: Kasbon, Dashboard, Room DB, Ekspor CSV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IosAppIconTile(
                        label = if (isIndonesian) "Kasbon" else "Advances",
                        icon = Icons.Default.Payments,
                        gradientColors = listOf(Color(0xFFFF2D55), Color(0xFFFF3B30)),
                        badgeCount = unpaidKasbonCount,
                        onClick = onKasbonClick,
                        modifier = Modifier.weight(1f),
                        testTagName = "ios_app_icon_kasbon"
                    )
                    IosAppIconTile(
                        label = "Dashboard",
                        icon = Icons.Default.BarChart,
                        gradientColors = listOf(Color(0xFF32ADE6), Color(0xFF007AFF)),
                        badgeCount = lowStockCount,
                        onClick = onReportsClick,
                        modifier = Modifier.weight(1f),
                        testTagName = "ios_app_icon_dashboard"
                    )
                    IosAppIconTile(
                        label = "Room DB",
                        icon = Icons.Default.Storage,
                        gradientColors = listOf(Color(0xFFAF52DE), Color(0xFF5856D6)),
                        onClick = onRoomListClick,
                        modifier = Modifier.weight(1f),
                        testTagName = "ios_app_icon_room"
                    )
                    IosAppIconTile(
                        label = if (isIndonesian) "Ekspor" else "Export",
                        icon = Icons.Default.IosShare,
                        gradientColors = listOf(Color(0xFF00C7BE), Color(0xFF34C759)),
                        onClick = onExportClick,
                        modifier = Modifier.weight(1f),
                        testTagName = "ios_app_icon_export"
                    )
                }
            }
        }
    }
}
