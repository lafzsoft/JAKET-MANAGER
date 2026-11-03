package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.JacketItem

@Entity(tableName = "jackets")
data class JacketEntity(
    @PrimaryKey
    val id: String,
    val sku: String,
    val name: String,
    val style: String,
    val size: String,
    val material: String,
    val color: String,
    val quantity: Int,
    val minThreshold: Int = 5,
    val locationRack: String,
    val imageUrl: String,
    val isSeasonal: Boolean = false,
    val unitPrice: Double = 89.99,
    val genderCategory: String = "Jaket Pria",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toModel(): JacketItem = JacketItem(
        id = id,
        sku = sku,
        name = name,
        style = style,
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

    fun toJacket(): com.example.model.Jacket = com.example.model.Jacket(
        id = id,
        name = name,
        category = style,
        size = size,
        quantity = quantity,
        sku = sku,
        material = material,
        color = color,
        locationRack = locationRack,
        minThreshold = minThreshold,
        unitPrice = unitPrice,
        imageUrl = imageUrl,
        isSeasonal = isSeasonal,
        genderCategory = genderCategory
    )

    companion object {
        fun fromModel(model: JacketItem, timestamp: Long = System.currentTimeMillis()): JacketEntity =
            JacketEntity(
                id = model.id,
                sku = model.sku,
                name = model.name,
                style = model.style,
                size = model.size,
                material = model.material,
                color = model.color,
                quantity = model.quantity,
                minThreshold = model.minThreshold,
                locationRack = model.locationRack,
                imageUrl = model.imageUrl,
                isSeasonal = model.isSeasonal,
                unitPrice = model.unitPrice,
                genderCategory = model.genderCategory,
                createdAt = timestamp
            )

        fun fromJacket(jacket: com.example.model.Jacket, timestamp: Long = System.currentTimeMillis()): JacketEntity =
            JacketEntity(
                id = jacket.id,
                sku = jacket.sku.ifBlank { "JKT-${jacket.category.take(3).uppercase()}-${jacket.size.uppercase()}" },
                name = jacket.name,
                style = jacket.category,
                size = jacket.size,
                material = jacket.material,
                color = jacket.color,
                quantity = jacket.quantity,
                minThreshold = jacket.minThreshold,
                locationRack = jacket.locationRack,
                imageUrl = jacket.imageUrl,
                isSeasonal = jacket.isSeasonal,
                unitPrice = jacket.unitPrice,
                genderCategory = jacket.genderCategory,
                createdAt = timestamp
            )
    }
}
