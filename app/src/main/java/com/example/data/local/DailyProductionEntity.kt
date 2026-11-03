package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DailyProductionItem
import com.example.model.ProductionShift
import com.example.model.ProductionStatus

@Entity(tableName = "daily_productions")
data class DailyProductionEntity(
    @PrimaryKey
    val id: String,
    val date: Long,
    val dateString: String,
    val jacketId: String,
    val jacketName: String,
    val jacketSku: String,
    val batchNumber: String,
    val shiftName: String,
    val operatorName: String,
    val quantityProduced: Int,
    val defectCount: Int,
    val statusName: String,
    val notes: String,
    val addedToStock: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toModel(): DailyProductionItem {
        val shiftEnum = try {
            ProductionShift.valueOf(shiftName)
        } catch (e: Exception) {
            ProductionShift.MORNING
        }

        val statusEnum = try {
            ProductionStatus.valueOf(statusName)
        } catch (e: Exception) {
            ProductionStatus.COMPLETED
        }

        return DailyProductionItem(
            id = id,
            date = date,
            dateString = dateString,
            jacketId = jacketId,
            jacketName = jacketName,
            jacketSku = jacketSku,
            batchNumber = batchNumber,
            shift = shiftEnum,
            operatorName = operatorName,
            quantityProduced = quantityProduced,
            defectCount = defectCount,
            status = statusEnum,
            notes = notes,
            addedToStock = addedToStock,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromModel(model: DailyProductionItem): DailyProductionEntity = DailyProductionEntity(
            id = model.id,
            date = model.date,
            dateString = model.dateString,
            jacketId = model.jacketId,
            jacketName = model.jacketName,
            jacketSku = model.jacketSku,
            batchNumber = model.batchNumber,
            shiftName = model.shift.name,
            operatorName = model.operatorName,
            quantityProduced = model.quantityProduced,
            defectCount = model.defectCount,
            statusName = model.status.name,
            notes = model.notes,
            addedToStock = model.addedToStock,
            createdAt = model.createdAt
        )
    }
}
