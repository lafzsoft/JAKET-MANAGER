package com.example.util

import com.example.model.JacketItem
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Utility helpers for normalizing scanned 1D/2D jacket barcode & QR label values
 * and ranking matching inventory items from the Room database.
 */
object BarcodeSearchUtils {

    private val structuredSkuRegex = Regex(
        pattern = """(?i)(?:sku|code|barcode|item|id)\s*[:=]\s*([A-Za-z0-9\-_./]+)"""
    )
    private val jsonSkuRegex = Regex(
        pattern = """(?i)"(?:sku|code|barcode|id)"\s*:\s*"([^"]+)""""
    )

    /**
     * Extracts the core SKU / barcode token from raw scanner output.
     * Supports raw 1D barcodes (EAN-13, UPC-A, Code 128, Code 39),
     * key-value QR payloads (e.g. "SKU:JKT-BMB-BLK-L"), JSON payloads,
     * and product URLs whose final path segment is a SKU/code.
     */
    fun extractLookupToken(rawCode: String): String {
        val trimmed = rawCode.trim()
        if (trimmed.isEmpty()) return ""

        jsonSkuRegex.find(trimmed)?.groupValues?.getOrNull(1)?.trim()?.let { extracted ->
            if (extracted.isNotEmpty()) return extracted
        }

        structuredSkuRegex.find(trimmed)?.groupValues?.getOrNull(1)?.trim()?.let { extracted ->
            if (extracted.isNotEmpty()) return extracted
        }

        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            val withoutQuery = trimmed.substringBefore("?").substringBefore("#").trimEnd('/')
            val lastSegment = withoutQuery.substringAfterLast('/')
            if (lastSegment.isNotBlank() && lastSegment != withoutQuery) {
                return lastSegment.trim()
            }
        }

        return trimmed
    }

    /**
     * Strips separators (dashes, spaces, underscores) and lowercases for tolerant matching
     * across barcode symbologies that omit hyphens.
     */
    fun normalizeBarcodeToken(code: String): String {
        return code.trim()
            .replace("-", "")
            .replace("_", "")
            .replace(" ", "")
            .lowercase()
    }

    /**
     * Finds and ranks matching [JacketItem]s for a scanned barcode or QR payload.
     * Exact SKU matches appear first, followed by exact ID, separator-insensitive SKU,
     * partial SKU matches, and product name / rack matches.
     */
    fun findRankedMatches(rawCode: String, candidates: List<JacketItem>): List<JacketItem> {
        val token = extractLookupToken(rawCode)
        if (token.isBlank() || candidates.isEmpty()) return emptyList()

        val normalizedToken = normalizeBarcodeToken(token)
        val allowPartial = token.length >= 3

        val scored = candidates.mapNotNull { item ->
            val skuNorm = normalizeBarcodeToken(item.sku)
            val rank = when {
                item.sku.equals(token, ignoreCase = true) -> 0
                item.id.equals(token, ignoreCase = true) -> 1
                normalizedToken.isNotEmpty() && skuNorm == normalizedToken -> 2
                allowPartial && (
                    item.sku.contains(token, ignoreCase = true) ||
                        (item.sku.length >= 3 && token.contains(item.sku, ignoreCase = true)) ||
                        (normalizedToken.length >= 3 && skuNorm.contains(normalizedToken))
                    ) -> 3
                allowPartial && (
                    item.name.contains(token, ignoreCase = true) ||
                        item.locationRack.equals(token, ignoreCase = true)
                    ) -> 4
                else -> null
            }
            rank?.let { it to item }
        }

        return scored
            .sortedWith(compareBy<Pair<Int, JacketItem>> { it.first }.thenBy { it.second.name.lowercase() })
            .map { it.second }
            .distinctBy { it.id }
    }

    /**
     * Returns a human-readable label for ML Kit [Barcode] format constants.
     */
    fun formatBarcodeSymbology(format: Int?, isIndonesian: Boolean): String {
        return when (format) {
            Barcode.FORMAT_EAN_13 -> "EAN-13"
            Barcode.FORMAT_EAN_8 -> "EAN-8"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_CODE_39 -> "Code 39"
            Barcode.FORMAT_CODE_93 -> "Code 93"
            Barcode.FORMAT_ITF -> "ITF"
            Barcode.FORMAT_CODABAR -> "Codabar"
            Barcode.FORMAT_QR_CODE -> "QR Code"
            Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
            Barcode.FORMAT_PDF417 -> "PDF417"
            Barcode.FORMAT_AZTEC -> "Aztec"
            else -> if (isIndonesian) "Kode Manual" else "Manual Code"
        }
    }
}
