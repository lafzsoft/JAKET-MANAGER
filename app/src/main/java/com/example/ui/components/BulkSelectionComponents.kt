package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import com.example.ui.theme.IosLabelPrimary
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosSystemGreen
import com.example.ui.theme.IosSystemOrange
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.LocalAppThemeSpec

/**
 * Contextual top bar displayed while checkbox multi-selection mode is active.
 */
@Composable
fun BulkSelectionTopBar(
    selectedCount: Int,
    totalVisibleCount: Int,
    allVisibleSelected: Boolean,
    isIndonesian: Boolean,
    onExitSelectionMode: () -> Unit,
    onToggleSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalAppThemeSpec.current
    CompactAppTopBar(
        title = if (selectedCount > 0) {
            if (isIndonesian) "$selectedCount Dipilih" else "$selectedCount Selected"
        } else {
            if (isIndonesian) "Pilih Jaket" else "Select Jackets"
        },
        subtitle = if (isIndonesian) {
            "dari $totalVisibleCount item ditampilkan"
        } else {
            "of $totalVisibleCount items shown"
        },
        containerColor = themeSpec.accentPrimary.copy(alpha = if (themeSpec.isDark) 0.24f else 0.12f),
        showBottomDivider = true,
        navigationIcon = {
            IconButton(
                onClick = onExitSelectionMode,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("btn_exit_selection_mode")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = if (isIndonesian) "Keluar Mode Pilih" else "Exit Selection Mode",
                    tint = themeSpec.labelPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        actions = {
            TextButton(
                onClick = onToggleSelectAll,
                enabled = totalVisibleCount > 0,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .testTag("btn_topbar_select_all")
            ) {
                Icon(
                    imageVector = if (allVisibleSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                    contentDescription = null,
                    tint = themeSpec.accentPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (allVisibleSelected) {
                        if (isIndonesian) "Batal Semua" else "Clear All"
                    } else {
                        if (isIndonesian) "Pilih Semua" else "Select All"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeSpec.accentPrimary
                )
            }
        },
        modifier = modifier.testTag("bulk_selection_top_bar")
    )
}

/**
 * Floating/docked bulk action bar allowing 'Update Status' and 'Delete' on selected jackets in one operation.
 */
@Composable
fun BulkSelectionActionBar(
    selectedCount: Int,
    isIndonesian: Boolean,
    onUpdateStatusClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalAppThemeSpec.current
    val hasSelection = selectedCount > 0

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = themeSpec.dockBackground,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, themeSpec.dockBorder),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("bulk_selection_action_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Count badge
            Surface(
                shape = CircleShape,
                color = if (hasSelection) themeSpec.accentPrimary else themeSpec.chipContainer
            ) {
                Text(
                    text = "$selectedCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (hasSelection) Color.White else themeSpec.labelSecondary,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                )
            }

            // Bulk Update Status button
            Button(
                onClick = onUpdateStatusClick,
                enabled = hasSelection,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeSpec.accentPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = themeSpec.chipContainer,
                    disabledContentColor = themeSpec.labelSecondary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .weight(1.25f)
                    .heightIn(min = 48.dp)
                    .testTag("btn_bulk_update_status")
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isIndonesian) "Ubah Status" else "Update Status",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bulk Delete button
            FilledTonalButton(
                onClick = onDeleteClick,
                enabled = hasSelection,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = IosSystemRed.copy(alpha = 0.14f),
                    contentColor = IosSystemRed,
                    disabledContainerColor = themeSpec.chipContainer,
                    disabledContentColor = themeSpec.labelSecondary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .weight(0.95f)
                    .heightIn(min = 48.dp)
                    .testTag("btn_bulk_delete")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isIndonesian) "Hapus" else "Delete",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Dialog to choose a target [StockStatusLevel] and apply it to all selected jackets at once.
 */
@Composable
fun BulkStatusUpdateDialog(
    selectedCount: Int,
    isIndonesian: Boolean,
    onDismiss: () -> Unit,
    onConfirmStatus: (StockStatusLevel) -> Unit
) {
    val themeSpec = LocalAppThemeSpec.current
    var chosenStatus by remember { mutableStateOf(StockStatusLevel.IN_STOCK) }

    val options = listOf(
        Triple(
            StockStatusLevel.IN_STOCK,
            if (isIndonesian) "Stok Tersedia (In Stock)" else "In Stock",
            if (isIndonesian) "Atur stok aman (+15 di atas batas minimum)" else "Restores stock above threshold (+15 pcs)"
        ),
        Triple(
            StockStatusLevel.LOW_STOCK,
            if (isIndonesian) "Stok Sedikit (Low Stock)" else "Low Stock",
            if (isIndonesian) "Tandai stok menipis (3 unit / batas minimum)" else "Marks inventory as running low (3 pcs)"
        ),
        Triple(
            StockStatusLevel.OUT_OF_STOCK,
            if (isIndonesian) "Habis Stok (Out of Stock)" else "Out of Stock",
            if (isIndonesian) "Atur jumlah stok menjadi 0 unit" else "Sets stock quantity to 0 units"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(themeSpec.accentPrimary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = themeSpec.accentPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = if (isIndonesian) {
                    "Ubah Status ($selectedCount Jaket)"
                } else {
                    "Update Status ($selectedCount Jackets)"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = IosLabelPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isIndonesian) {
                        "Pilih status stok baru untuk diterapkan ke semua jaket yang dipilih:"
                    } else {
                        "Choose the stock status to apply to all selected jackets:"
                    },
                    fontSize = 13.sp,
                    color = IosLabelSecondary
                )

                options.forEach { (status, title, subtitle) ->
                    val isSelected = chosenStatus == status
                    val statusColor = when (status) {
                        StockStatusLevel.IN_STOCK -> IosSystemGreen
                        StockStatusLevel.LOW_STOCK -> IosSystemOrange
                        StockStatusLevel.OUT_OF_STOCK -> IosSystemRed
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) statusColor.copy(alpha = 0.10f) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) statusColor else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { chosenStatus = status }
                            .testTag("bulk_status_option_${status.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { chosenStatus = status },
                                colors = RadioButtonDefaults.colors(selectedColor = statusColor)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Text(
                                        text = title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IosLabelPrimary
                                    )
                                }
                                Text(
                                    text = subtitle,
                                    fontSize = 11.sp,
                                    color = IosLabelSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmStatus(chosenStatus) },
                colors = ButtonDefaults.buttonColors(containerColor = themeSpec.accentPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_confirm_bulk_status")
            ) {
                Text(
                    text = if (isIndonesian) "Terapkan" else "Apply",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_cancel_bulk_status")
            ) {
                Text(text = if (isIndonesian) "Batal" else "Cancel")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.testTag("bulk_status_update_dialog")
    )
}

/**
 * Confirmation dialog before permanently deleting multiple selected jackets from the Room database.
 */
@Composable
fun BulkDeleteConfirmationDialog(
    selectedJackets: List<JacketItem>,
    isIndonesian: Boolean,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val count = selectedJackets.size
    val previewNames = selectedJackets.take(3)
    val remainingCount = (count - previewNames.size).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(IosSystemRed.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = IosSystemRed,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = if (isIndonesian) "Hapus $count Jaket?" else "Delete $count Jackets?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = IosLabelPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isIndonesian) {
                        "Yakin ingin menghapus $count jaket terpilih secara permanen dari database Room?"
                    } else {
                        "Are you sure you want to remove $count selected jackets from the Room database?"
                    },
                    fontSize = 13.sp,
                    color = IosLabelSecondary
                )

                if (previewNames.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            previewNames.forEach { jacket ->
                                Text(
                                    text = "• ${jacket.name} (${jacket.sku})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = IosLabelPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (remainingCount > 0) {
                                Text(
                                    text = if (isIndonesian) {
                                        "+ $remainingCount jaket lainnya"
                                    } else {
                                        "+ $remainingCount more jackets"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IosLabelSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = IosSystemRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_confirm_bulk_delete")
            ) {
                Text(
                    text = if (isIndonesian) "Hapus ($count)" else "Delete ($count)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_cancel_bulk_delete")
            ) {
                Text(text = if (isIndonesian) "Batal" else "Cancel")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.testTag("bulk_delete_confirmation_dialog")
    )
}
