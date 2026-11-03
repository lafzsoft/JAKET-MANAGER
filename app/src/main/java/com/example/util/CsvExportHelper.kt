package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportHelper {

    /**
     * Converts a list of JacketItems into an RFC 4180-compliant CSV string.
     * Includes UTF-8 BOM (\uFEFF) so Excel, Google Sheets, and other spreadsheet apps
     * open it with proper character encoding.
     */
    fun generateCsv(jackets: List<JacketItem>, isIndonesian: Boolean = true): String {
        val sb = StringBuilder()

        // UTF-8 Byte Order Mark for Excel compatibility
        sb.append('\uFEFF')

        // CSV Header
        val headers = if (isIndonesian) {
            listOf(
                "ID",
                "SKU",
                "Nama Produk",
                "Kategori Demografis",
                "Model Jaket",
                "Ukuran",
                "Bahan",
                "Warna",
                "Jumlah Stok",
                "Batas Minimum",
                "Lokasi Rak",
                "Status Stok",
                "Koleksi Musiman",
                "Harga Satuan (USD)",
                "Estimasi Nilai Total (USD)"
            )
        } else {
            listOf(
                "ID",
                "SKU",
                "Product Name",
                "Demographic Category",
                "Jacket Style",
                "Size",
                "Material",
                "Color",
                "Stock Quantity",
                "Min Threshold",
                "Location Rack",
                "Stock Status",
                "Seasonal Collection",
                "Unit Price (USD)",
                "Estimated Total Value (USD)"
            )
        }
        sb.append(headers.joinToString(separator = ",", transform = { escapeCsv(it) })).append("\r\n")

        // Rows
        for (jacket in jackets) {
            val statusLabel = when (jacket.status) {
                StockStatusLevel.IN_STOCK -> if (isIndonesian) "Stok Tersedia" else "In Stock"
                StockStatusLevel.LOW_STOCK -> if (isIndonesian) "Stok Sedikit" else "Low Stock"
                StockStatusLevel.OUT_OF_STOCK -> if (isIndonesian) "Habis Stok" else "Out of Stock"
            }
            val seasonalLabel = if (jacket.isSeasonal) {
                if (isIndonesian) "Ya" else "Yes"
            } else {
                if (isIndonesian) "Tidak" else "No"
            }
            val totalValue = jacket.quantity * jacket.unitPrice

            val row = listOf(
                jacket.id,
                jacket.sku,
                jacket.name,
                jacket.genderCategory,
                jacket.style,
                jacket.size,
                jacket.material,
                jacket.color,
                jacket.quantity.toString(),
                jacket.minThreshold.toString(),
                jacket.locationRack,
                statusLabel,
                seasonalLabel,
                String.format(Locale.US, "%.2f", jacket.unitPrice),
                String.format(Locale.US, "%.2f", totalValue)
            )
            sb.append(row.joinToString(separator = ",", transform = { escapeCsv(it) })).append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Escapes individual field values according to RFC 4180 CSV specifications.
     */
    fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    /**
     * Generates a unique, timestamped filename for the CSV backup.
     */
    fun generateFilename(prefix: String = "inventaris_jaket"): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${prefix}_$timestamp.csv"
    }

    /**
     * Saves CSV content to a cache file in the exports directory.
     */
    fun writeCsvToCache(context: Context, csvContent: String, filename: String = generateFilename()): File {
        val exportDir = File(context.cacheDir, "exports").apply {
            if (!exists()) mkdirs()
        }
        val file = File(exportDir, filename)
        FileOutputStream(file).use { out ->
            out.write(csvContent.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    /**
     * Builds an Android Share Intent for the generated CSV file using FileProvider.
     */
    fun createShareIntent(context: Context, file: File, chooserTitle: String = "Bagikan Data Inventaris CSV"): Intent {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            putExtra(Intent.EXTRA_TEXT, "Data inventaris jaket berhasil diekspor dari Jacket Manager (${file.name}).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(shareIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Copies the CSV content to the system clipboard.
     */
    fun copyToClipboard(context: Context, csvContent: String, label: String = "Data Inventaris Jaket (CSV)") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, csvContent)
        clipboard.setPrimaryClip(clip)
    }
}
