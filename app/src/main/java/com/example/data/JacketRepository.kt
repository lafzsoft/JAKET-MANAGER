package com.example.data

import com.example.data.local.DailyProductionDao
import com.example.data.local.DailyProductionEntity
import com.example.data.local.JacketDao
import com.example.data.local.JacketEntity
import com.example.model.CatalogData
import com.example.model.DailyProductionItem
import com.example.model.JacketItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class JacketRepository(
    private val dao: JacketDao,
    private val productionDao: DailyProductionDao? = null
) {

    val jackets: Flow<List<JacketItem>> = dao.getAllJackets().map { list ->
        list.map { it.toModel() }
    }

    val jacketList: Flow<List<com.example.model.Jacket>> = dao.getAllJackets().map { list ->
        list.map { it.toJacket() }
    }

    val jacketsList: Flow<List<com.example.model.Jacket>> = jacketList

    val productions: Flow<List<DailyProductionItem>> = productionDao?.getAllProductions()?.map { list ->
        list.map { it.toModel() }
    } ?: emptyFlow()

    suspend fun addJacket(
        sku: String,
        name: String,
        style: String,
        size: String,
        material: String,
        color: String,
        quantity: Int,
        locationRack: String,
        imageUrl: String = "",
        isSeasonal: Boolean = false,
        unitPrice: Double = 89.99,
        genderCategory: String = "Jaket Pria"
    ) {
        val resolvedImage = if (imageUrl.isNotBlank()) imageUrl else {
            CatalogData.STYLES.firstOrNull { it.titleEn.equals(style, ignoreCase = true) }?.imageUrl
                ?: CatalogData.HERO_IMAGE_URL
        }
        val entity = JacketEntity(
            id = UUID.randomUUID().toString(),
            sku = sku.ifBlank { "JKT-${System.currentTimeMillis() % 10000}" },
            name = name,
            style = style,
            size = size,
            material = material,
            color = color,
            quantity = quantity,
            locationRack = locationRack.ifBlank { "Rack A-01" },
            imageUrl = resolvedImage,
            isSeasonal = isSeasonal,
            unitPrice = unitPrice,
            genderCategory = genderCategory,
            createdAt = System.currentTimeMillis()
        )
        dao.insertJacket(entity)
    }

    suspend fun addJacket(jacket: com.example.model.Jacket) {
        val resolvedImage = if (jacket.imageUrl.isNotBlank()) jacket.imageUrl else {
            CatalogData.STYLES.firstOrNull { it.titleEn.equals(jacket.category, ignoreCase = true) }?.imageUrl
                ?: CatalogData.HERO_IMAGE_URL
        }
        val entity = JacketEntity.fromJacket(
            jacket.copy(imageUrl = resolvedImage)
        )
        dao.insertJacket(entity)
    }

    suspend fun updateJacket(jacket: com.example.model.Jacket) {
        val resolvedImage = if (jacket.imageUrl.isNotBlank()) jacket.imageUrl else {
            CatalogData.STYLES.firstOrNull { it.titleEn.equals(jacket.category, ignoreCase = true) }?.imageUrl
                ?: CatalogData.HERO_IMAGE_URL
        }
        val entity = JacketEntity.fromJacket(jacket.copy(imageUrl = resolvedImage))
        dao.insertJacket(entity)
    }

    suspend fun updateJacketDetails(
        id: String,
        sku: String,
        name: String,
        style: String,
        size: String,
        material: String,
        color: String,
        quantity: Int,
        locationRack: String,
        isSeasonal: Boolean = false,
        imageUrl: String = "",
        unitPrice: Double = 89.99,
        genderCategory: String = "Jaket Pria",
        minThreshold: Int = 5
    ) {
        val existingEntity = dao.getJacketById(id)
        val resolvedImage = when {
            imageUrl.isNotBlank() -> imageUrl
            existingEntity?.imageUrl?.isNotBlank() == true -> existingEntity.imageUrl
            else -> CatalogData.STYLES.firstOrNull { it.titleEn.equals(style, ignoreCase = true) }?.imageUrl ?: CatalogData.HERO_IMAGE_URL
        }
        val entity = JacketEntity(
            id = id,
            sku = sku,
            name = name,
            style = style,
            size = size,
            material = material,
            color = color,
            quantity = quantity.coerceAtLeast(0),
            minThreshold = existingEntity?.minThreshold ?: minThreshold,
            locationRack = locationRack.ifBlank { "Rack A-01" },
            imageUrl = resolvedImage,
            isSeasonal = isSeasonal,
            unitPrice = existingEntity?.unitPrice ?: unitPrice,
            genderCategory = existingEntity?.genderCategory ?: genderCategory,
            createdAt = existingEntity?.createdAt ?: System.currentTimeMillis()
        )
        dao.insertJacket(entity)
    }

    suspend fun getJacketById(id: String): com.example.model.Jacket? {
        return dao.getJacketById(id)?.toJacket()
    }

    suspend fun adjustQuantity(id: String, currentQuantity: Int, delta: Int) {
        val newQty = (currentQuantity + delta).coerceAtLeast(0)
        dao.updateQuantity(id, newQty)
    }

    suspend fun setQuantity(id: String, newQuantity: Int) {
        dao.updateQuantity(id, newQuantity.coerceAtLeast(0))
    }

    suspend fun restock(id: String, currentQuantity: Int, amount: Int = 15) {
        dao.updateQuantity(id, currentQuantity + amount)
    }

    suspend fun updateJacketImage(id: String, imageUrl: String) {
        dao.updateImageUrl(id, imageUrl)
    }

    suspend fun updateJacketImageBySku(sku: String, imageUrl: String) {
        dao.updateImageUrlBySku(sku, imageUrl)
    }

    suspend fun deleteJacket(id: String) {
        dao.deleteJacket(id)
    }

    suspend fun deleteJacket(jacket: com.example.model.Jacket) {
        dao.deleteJacket(jacket.id)
    }

    suspend fun addDailyProduction(
        production: DailyProductionItem,
        currentJacketQuantity: Int? = null
    ) {
        productionDao?.insertProduction(DailyProductionEntity.fromModel(production))

        // If addedToStock is true and linked to an inventory jacket, increment inventory stock with net accepted count!
        if (production.addedToStock && production.jacketId.isNotBlank() && currentJacketQuantity != null) {
            val netQuantityToAdd = production.netAcceptedQuantity
            if (netQuantityToAdd > 0) {
                dao.updateQuantity(production.jacketId, currentJacketQuantity + netQuantityToAdd)
            }
        }
    }

    suspend fun deleteDailyProduction(id: String) {
        productionDao?.deleteProduction(id)
    }
}
