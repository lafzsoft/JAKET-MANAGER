package com.example.model

import com.example.util.CurrencyUtils

enum class KasbonStatus(val labelId: String, val labelEn: String) {
    BELUM_LUNAS("Belum Lunas", "Unpaid"),
    CICILAN("Sedang Dicicil", "Partially Paid"),
    LUNAS("Lunas", "Paid in Full")
}

data class KasbonItem(
    val id: String,
    val employeeName: String,
    val role: String, // e.g. "Operator Jahit", "Tukang Potong", "Finishing", "Staff Gudang", "Quality Control"
    val amount: Double,
    val paidAmount: Double = 0.0,
    val date: Long,
    val dateString: String,
    val dueDate: String, // e.g. "2026-11-25 (Gaji Bulanan)"
    val status: KasbonStatus,
    val purpose: String,
    val repaymentMethod: String = "Potong Gaji Bulanan", // e.g. "Potong Gaji Bulanan", "Cicil Mingguan", "Tunai Langsung"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val remainingAmount: Double
        get() = (amount - paidAmount).coerceAtLeast(0.0)

    val progressPercent: Float
        get() = if (amount > 0) ((paidAmount / amount).coerceIn(0.0, 1.0)).toFloat() else 1f

    val amountFormatted: String
        get() = CurrencyUtils.formatRupiah(amount)

    val paidAmountFormatted: String
        get() = CurrencyUtils.formatRupiah(paidAmount)

    val remainingAmountFormatted: String
        get() = CurrencyUtils.formatRupiah(remainingAmount)
}
