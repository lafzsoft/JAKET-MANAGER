package com.example.model

import java.util.UUID

/**
 * Data class representing an inventory item for a jacket.
 *
 * @property id Unique identifier for the jacket (default generates UUID)
 * @property name Name or title of the jacket (e.g. "Bomber Classic", "Parka Heavy")
 * @property category Category or style classification (e.g. "Bomber", "Parka", "Denim", "Windbreaker")
 * @property size Garment size (e.g. "XS", "S", "M", "L", "XL", "XXL", "3XL")
 * @property quantity Stock quantity available in inventory
 * @property sku Stock Keeping Unit identifier (e.g. "JKT-BMB-BLK-M")
 * @property material Fabric/material composition (e.g. "Leather", "Cotton", "Nylon")
 * @property color Color variant description (e.g. "Black", "Navy", "Olive")
 * @property locationRack Warehouse storage rack location (e.g. "Rack A-01")
 * @property minThreshold Minimum stock threshold before alert
 * @property unitPrice Unit price for the jacket item
 * @property imageUrl Image URL or asset reference
 * @property isSeasonal Whether this item belongs to a seasonal collection
 */
data class Jacket(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val size: String,
    val quantity: Int,
    val sku: String = "",
    val material: String = "Cotton",
    val color: String = "Black",
    val locationRack: String = "Rack A-01",
    val minThreshold: Int = 5,
    val unitPrice: Double = 89.99,
    val imageUrl: String = "",
    val isSeasonal: Boolean = false,
    val genderCategory: String = "Jaket Pria"
) {
    /**
     * Stock status level computed from the current inventory quantity and threshold.
     */
    val status: StockStatusLevel
        get() = when {
            quantity <= 0 -> StockStatusLevel.OUT_OF_STOCK
            quantity <= minThreshold -> StockStatusLevel.LOW_STOCK
            else -> StockStatusLevel.IN_STOCK
        }

    /**
     * Helper to map this Jacket instance to [JacketItem] for UI and Room entity compatibility.
     */
    fun toJacketItem(): JacketItem = JacketItem(
        id = id,
        sku = sku.ifBlank { "JKT-${category.take(3).uppercase()}-${size.uppercase()}" },
        name = name,
        style = category,
        size = size,
        material = material,
        color = color,
        quantity = quantity,
        minThreshold = minThreshold,
        locationRack = locationRack,
        imageUrl = imageUrl,
        isSeasonal = isSeasonal,
        unitPrice = unitPrice,
        genderCategory = genderCategory
    )

    companion object {
        /**
         * Creates a [Jacket] instance from a [JacketItem].
         */
        fun fromJacketItem(item: JacketItem): Jacket = Jacket(
            id = item.id,
            name = item.name,
            category = item.style,
            size = item.size,
            quantity = item.quantity,
            sku = item.sku,
            material = item.material,
            color = item.color,
            locationRack = item.locationRack,
            minThreshold = item.minThreshold,
            unitPrice = item.unitPrice,
            imageUrl = item.imageUrl,
            isSeasonal = item.isSeasonal,
            genderCategory = item.genderCategory
        )
    }
}

/**
 * Extension function to map a [JacketItem] to [Jacket].
 */
fun JacketItem.toJacket(): Jacket = Jacket.fromJacketItem(this)
