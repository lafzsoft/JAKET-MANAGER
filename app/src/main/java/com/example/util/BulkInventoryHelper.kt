package com.example.util

import com.example.model.JacketItem
import com.example.model.StockStatusLevel

/**
 * Pure helper utilities for bulk inventory selection and stock status calculation.
 */
object BulkInventoryHelper {

    /**
     * Computes the target stock quantity for a [JacketItem] when applying a bulk [StockStatusLevel] update.
     * - [StockStatusLevel.OUT_OF_STOCK] -> 0 units
     * - [StockStatusLevel.LOW_STOCK] -> safe low-stock level within 1..minThreshold
     * - [StockStatusLevel.IN_STOCK] -> preserves existing healthy stock or bumps to minThreshold + 15
     */
    fun targetQuantityForStatus(
        currentQuantity: Int,
        minThreshold: Int,
        targetStatus: StockStatusLevel
    ): Int {
        val safeThreshold = minThreshold.coerceAtLeast(1)
        return when (targetStatus) {
            StockStatusLevel.OUT_OF_STOCK -> 0
            StockStatusLevel.LOW_STOCK -> minOf(3, safeThreshold).coerceAtLeast(1)
            StockStatusLevel.IN_STOCK -> if (currentQuantity > safeThreshold) {
                currentQuantity
            } else {
                safeThreshold + 15
            }
        }
    }

    /**
     * Toggles a single item ID in the selected set.
     */
    fun toggleItemSelection(selectedIds: Set<String>, itemId: String): Set<String> {
        return if (itemId in selectedIds) selectedIds - itemId else selectedIds + itemId
    }

    /**
     * Selects all visible items if not all visible items are currently selected;
     * otherwise clears the visible items from the selection.
     */
    fun toggleSelectAllVisible(selectedIds: Set<String>, visibleItems: List<JacketItem>): Set<String> {
        val visibleIds = visibleItems.map { it.id }.toSet()
        if (visibleIds.isEmpty()) return emptySet()
        val allVisibleSelected = visibleIds.all { it in selectedIds }
        return if (allVisibleSelected) {
            selectedIds - visibleIds
        } else {
            selectedIds + visibleIds
        }
    }
}
