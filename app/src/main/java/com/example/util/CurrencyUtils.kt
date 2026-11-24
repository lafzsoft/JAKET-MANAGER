package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Utility for formatting and parsing prices in Indonesian Rupiah (Rp / IDR).
 * Includes backward compatibility normalization for legacy seed data stored in USD.
 */
object CurrencyUtils {

    private val idSymbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID")).apply {
        groupingSeparator = '.'
        monetaryDecimalSeparator = ','
    }

    private val rupiahFormatter = DecimalFormat("#,###", idSymbols)

    /**
     * Common quick-select jacket price points in Indonesian Rupiah.
     */
    val QUICK_PRICE_OPTIONS = listOf(
        199_000.0,
        299_000.0,
        349_000.0,
        499_000.0,
        699_000.0
    )

    /**
     * Normalizes a price stored in database. If a legacy price in USD (< 2000.0) is encountered,
     * it is automatically converted to a realistic Rupiah price using the baseline exchange rate (~15,000 IDR/USD).
     */
    fun normalizeRupiah(price: Double): Double {
        return when {
            price <= 0.0 -> 0.0
            price in 0.01..2000.0 -> (price * 15_000.0).roundToLong().toDouble()
            else -> price
        }
    }

    /**
     * Formats a double value as Indonesian Rupiah (e.g. 350000.0 -> "Rp 350.000").
     */
    fun formatRupiah(price: Double, includePrefix: Boolean = true): String {
        val normalized = normalizeRupiah(price)
        val rounded = normalized.roundToLong()
        val formatted = rupiahFormatter.format(rounded)
        return if (includePrefix) "Rp $formatted" else formatted
    }

    /**
     * Formats raw number string without currency symbol for text field editing (e.g. 350000.0 -> "350000").
     */
    fun formatRupiahRaw(price: Double): String {
        val normalized = normalizeRupiah(price)
        return normalized.roundToLong().toString()
    }

    /**
     * Formats large Rupiah figures in a compact, human-readable format (e.g. Rp 12,5 Jt or Rp 850 Rb).
     */
    fun formatRupiahCompact(price: Double): String {
        val normalized = normalizeRupiah(price)
        val idLocale = Locale.forLanguageTag("id-ID")
        return when {
            normalized >= 1_000_000_000.0 -> {
                String.format(idLocale, "Rp %.1f M", normalized / 1_000_000_000.0)
            }
            normalized >= 1_000_000.0 -> {
                val value = normalized / 1_000_000.0
                if (value % 1.0 == 0.0) {
                    "Rp ${value.toLong()} Jt"
                } else {
                    String.format(idLocale, "Rp %.1f Jt", value)
                }
            }
            normalized >= 1_000.0 -> {
                val value = normalized / 1_000.0
                if (value % 1.0 == 0.0) {
                    "Rp ${value.toLong()} Rb"
                } else {
                    String.format(idLocale, "Rp %.0f Rb", value)
                }
            }
            else -> formatRupiah(normalized)
        }
    }

    /**
     * Parses user input text into a Double representing Rupiah value.
     * Handles inputs like "350000", "Rp 350.000", "350.000", etc.
     */
    fun parseRupiah(input: String, defaultPrice: Double = 350_000.0): Double {
        if (input.isBlank()) return defaultPrice
        // Extract only digits
        val cleanDigits = input.filter { it.isDigit() }
        if (cleanDigits.isEmpty()) return defaultPrice
        return cleanDigits.toDoubleOrNull() ?: defaultPrice
    }
}
