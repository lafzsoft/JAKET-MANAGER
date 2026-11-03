package com.example.model

enum class ProductionShift(val titleEn: String, val titleId: String) {
    MORNING("Morning Shift (08:00 - 16:00)", "Shift Pagi (08:00 - 16:00)"),
    AFTERNOON("Afternoon Shift (16:00 - 24:00)", "Shift Siang (16:00 - 24:00)"),
    NIGHT("Night Shift (00:00 - 08:00)", "Shift Malam (00:00 - 08:00)")
}

enum class ProductionStatus(val labelEn: String, val labelId: String) {
    COMPLETED("Finished", "Selesai"),
    IN_PROGRESS("In Progress", "Proses"),
    QUALITY_CHECK("QC Review", "Pemeriksaan QC")
}

data class DailyProductionItem(
    val id: String,
    val date: Long,
    val dateString: String,
    val jacketId: String,
    val jacketName: String,
    val jacketSku: String,
    val batchNumber: String,
    val shift: ProductionShift,
    val operatorName: String,
    val quantityProduced: Int,
    val defectCount: Int,
    val status: ProductionStatus,
    val notes: String,
    val addedToStock: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val netAcceptedQuantity: Int
        get() = (quantityProduced - defectCount).coerceAtLeast(0)

    val defectRatePercentage: Float
        get() = if (quantityProduced > 0) {
            (defectCount.toFloat() / quantityProduced.toFloat()) * 100f
        } else 0f
}

data class DailyProductionSummary(
    val totalProducedToday: Int,
    val totalAcceptedToday: Int,
    val totalDefectsToday: Int,
    val totalBatchesToday: Int,
    val dailyTarget: Int,
    val targetFulfillmentPercentage: Float,
    val averageDefectRate: Float
)
