package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.KasbonItem
import com.example.model.KasbonStatus

@Entity(tableName = "kasbon_table")
data class KasbonEntity(
    @PrimaryKey
    val id: String,
    val employeeName: String,
    val role: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val date: Long,
    val dateString: String,
    val dueDate: String,
    val statusName: String, // BELUM_LUNAS, CICILAN, LUNAS
    val purpose: String,
    val repaymentMethod: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): KasbonItem {
        val parsedStatus = try {
            KasbonStatus.valueOf(statusName)
        } catch (_: Exception) {
            if (paidAmount >= amount) KasbonStatus.LUNAS
            else if (paidAmount > 0) KasbonStatus.CICILAN
            else KasbonStatus.BELUM_LUNAS
        }

        return KasbonItem(
            id = id,
            employeeName = employeeName,
            role = role,
            amount = amount,
            paidAmount = paidAmount,
            date = date,
            dateString = dateString,
            dueDate = dueDate,
            status = parsedStatus,
            purpose = purpose,
            repaymentMethod = repaymentMethod,
            notes = notes,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(item: KasbonItem): KasbonEntity {
            return KasbonEntity(
                id = item.id,
                employeeName = item.employeeName,
                role = item.role,
                amount = item.amount,
                paidAmount = item.paidAmount,
                date = item.date,
                dateString = item.dateString,
                dueDate = item.dueDate,
                statusName = item.status.name,
                purpose = item.purpose,
                repaymentMethod = item.repaymentMethod,
                notes = item.notes,
                createdAt = item.createdAt
            )
        }
    }
}
