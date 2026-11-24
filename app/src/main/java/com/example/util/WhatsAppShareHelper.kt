package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import java.net.URLEncoder

/**
 * Utility for formatting and sharing jacket details (Model, Size, Warna, Nama, SKU)
 * via WhatsApp or standard Android Share Sheet.
 */
object WhatsAppShareHelper {

    /**
     * Builds a structured, professional message in Indonesian for WhatsApp sharing.
     * Highlights model, size, warna, nama, and SKU as requested.
     */
    fun buildJacketShareText(jacket: JacketItem, isIndonesian: Boolean = true): String {
        val stockLabel = when (jacket.status) {
            StockStatusLevel.IN_STOCK -> "✅ Stok Aman (${jacket.quantity} unit)"
            StockStatusLevel.LOW_STOCK -> "⚠️ Stok Menipis (${jacket.quantity} unit)"
            StockStatusLevel.OUT_OF_STOCK -> "❌ Habis Stok (0 unit)"
        }
        val formattedPrice = CurrencyUtils.formatRupiah(jacket.unitPrice)

        return buildString {
            appendLine("🧥 *RINCIAN PRODUK JAKET*")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("🏷️ *Nama*: ${jacket.name}")
            appendLine("🔖 *SKU*: ${jacket.sku}")
            appendLine("🎨 *Model / Gaya*: ${jacket.style}")
            appendLine("📏 *Ukuran (Size)*: ${jacket.size}")
            appendLine("🌈 *Warna*: ${jacket.color}")
            appendLine("🧵 *Bahan*: ${jacket.material}")
            appendLine("📍 *Lokasi Rak*: ${jacket.locationRack}")
            appendLine("💰 *Harga*: $formattedPrice")
            appendLine("📦 *Status*: $stockLabel")
            if (jacket.genderCategory.isNotBlank()) {
                appendLine("👥 *Kategori*: ${jacket.genderCategory}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("_Informasi dari Sistem Manajemen Inventaris Gudang Jaket_")
        }
    }

    /**
     * Attempts to open WhatsApp directly (via package or direct whatsapp:// scheme),
     * and seamlessly falls back to the standard Android system share sheet with a chooser
     * so that the user can pick WhatsApp, WhatsApp Business, Telegram, or any messaging app.
     */
    fun shareJacketToWhatsApp(context: Context, jacket: JacketItem, isIndonesian: Boolean = true) {
        val message = buildJacketShareText(jacket, isIndonesian)
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (_: Exception) {
            message
        }

        // 1. Try direct WhatsApp URI intent
        val directUriIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?text=$encodedMessage")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(directUriIntent)
        } catch (_: Exception) {
            // 2. Fallback to generic ACTION_SEND intent with chooser
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Rincian Jaket: ${jacket.name} (${jacket.sku})")
                putExtra(Intent.EXTRA_TEXT, message)
            }
            val chooserTitle = if (isIndonesian) "Bagikan Rincian Jaket via WhatsApp" else "Share Jacket Details via WhatsApp"
            val chooserIntent = Intent.createChooser(sendIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(chooserIntent)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (isIndonesian) "Gagal membuka aplikasi WhatsApp" else "Failed to open WhatsApp",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Builds a clean, ready-to-process single-unit order message (Format Order Satuan).
     * Highlights SKU, Warna, Size, Nama, Jumlah, Harga, and optional Buyer/Shipping info.
     */
    fun buildSingleUnitOrderText(
        jacket: JacketItem,
        buyerName: String = "",
        buyerPhone: String = "",
        orderQty: Int = 1,
        notes: String = "",
        shippingAddress: String = "",
        isIndonesian: Boolean = true
    ): String {
        val totalPrice = jacket.unitPrice * orderQty
        val formattedUnitPrice = CurrencyUtils.formatRupiah(jacket.unitPrice)
        val formattedTotalPrice = CurrencyUtils.formatRupiah(totalPrice)

        return buildString {
            appendLine("📋 *FORMAT ORDER SATUAN (JAKET)*")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("🔖 *SKU*: ${jacket.sku}")
            appendLine("🎨 *Warna*: ${jacket.color}")
            appendLine("📏 *Size*: ${jacket.size}")
            appendLine("🏷️ *Nama Produk*: ${jacket.name} (${jacket.style})")
            appendLine("🔢 *Jumlah Pesanan*: $orderQty pcs")
            appendLine("💵 *Harga Satuan*: $formattedUnitPrice")
            if (orderQty > 1) {
                appendLine("💰 *Total Harga*: $formattedTotalPrice")
            }
            if (buyerName.isNotBlank()) {
                appendLine("👤 *Nama Pembeli*: $buyerName")
            }
            if (buyerPhone.isNotBlank()) {
                appendLine("📱 *No. HP / WA*: $buyerPhone")
            }
            if (shippingAddress.isNotBlank()) {
                appendLine("📍 *Alamat Kirim*: $shippingAddress")
            }
            if (notes.isNotBlank()) {
                appendLine("📝 *Catatan Khusus*: $notes")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("✅ _Mohon konfirmasi ketersediaan stok & nomor resi pengiriman._")
            appendLine("_Dikirim via Sistem Gudang Inventaris Jaket_")
        }
    }

    /**
     * Sends the single-unit order message directly to a specific WhatsApp phone number
     * (or open WhatsApp / chooser if recipient phone is not specified).
     */
    fun sendSingleUnitOrderToWhatsApp(
        context: Context,
        jacket: JacketItem,
        buyerName: String = "",
        buyerPhone: String = "",
        orderQty: Int = 1,
        notes: String = "",
        shippingAddress: String = "",
        targetWhatsAppNumber: String = "",
        isIndonesian: Boolean = true
    ) {
        val message = buildSingleUnitOrderText(
            jacket = jacket,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            orderQty = orderQty,
            notes = notes,
            shippingAddress = shippingAddress,
            isIndonesian = isIndonesian
        )
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (_: Exception) {
            message
        }

        // Clean phone number: remove +, -, spaces and convert leading 0 to country code 62 (Indonesia)
        val cleanPhone = targetWhatsAppNumber.filter { it.isDigit() }.let { num ->
            when {
                num.startsWith("0") -> "62" + num.substring(1)
                num.startsWith("8") -> "62$num"
                else -> num
            }
        }

        val directUriString = if (cleanPhone.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
        } else {
            "https://api.whatsapp.com/send?text=$encodedMessage"
        }

        val directUriIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(directUriString)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(directUriIntent)
        } catch (_: Exception) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Order Satuan: ${jacket.sku} - ${jacket.name}")
                putExtra(Intent.EXTRA_TEXT, message)
            }
            val chooserTitle = if (isIndonesian) "Kirim Order Satuan via WhatsApp" else "Send Unit Order via WhatsApp"
            val chooserIntent = Intent.createChooser(sendIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(chooserIntent)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (isIndonesian) "Gagal membuka aplikasi WhatsApp" else "Failed to open WhatsApp",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
