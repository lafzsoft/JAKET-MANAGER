package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import com.example.ui.theme.IosSystemGreen
import com.example.ui.theme.IosSystemOrange
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.LocalAppThemeSpec

data class SkuOption(
    val sku: String,
    val name: String,
    val style: String,
    val size: String,
    val locationRack: String,
    val totalQuantity: Int,
    val status: StockStatusLevel,
    val variantCount: Int = 1
)

object SkuDropdownHelper {

    private val KNOWN_SIZE_SUFFIXES = setOf("XS", "S", "M", "L", "XL", "XXL", "3XL", "4XL", "STD")

    /**
     * Builds deduplicated, alphabetically sorted SKU options from the inventory list.
     */
    fun buildSkuOptions(jackets: List<JacketItem>): List<SkuOption> {
        return jackets
            .filter { it.sku.isNotBlank() }
            .groupBy { it.sku.trim().uppercase() }
            .map { (_, group) ->
                val primary = group.first()
                val totalQty = group.sumOf { it.quantity }
                val minThreshold = group.minOfOrNull { it.minThreshold } ?: 5
                val status = when {
                    totalQty <= 0 -> StockStatusLevel.OUT_OF_STOCK
                    totalQty <= minThreshold -> StockStatusLevel.LOW_STOCK
                    else -> StockStatusLevel.IN_STOCK
                }
                SkuOption(
                    sku = primary.sku.trim(),
                    name = primary.name,
                    style = primary.style,
                    size = primary.size,
                    locationRack = primary.locationRack,
                    totalQuantity = totalQty,
                    status = status,
                    variantCount = group.size
                )
            }
            .sortedBy { it.sku.uppercase() }
    }

    /**
     * Filters SKU options by query text against SKU, jacket name, style, or size.
     */
    fun filterSkuOptions(options: List<SkuOption>, query: String): List<SkuOption> {
        val clean = query.trim()
        if (clean.isBlank()) return options
        return options.filter { opt ->
            opt.sku.contains(clean, ignoreCase = true) ||
                opt.name.contains(clean, ignoreCase = true) ||
                opt.style.contains(clean, ignoreCase = true) ||
                opt.size.contains(clean, ignoreCase = true)
        }
    }

    /**
     * Filters jackets by selected SKU ("ALL" or blank returns the full list).
     */
    fun filterJacketsBySku(jackets: List<JacketItem>, selectedSku: String): List<JacketItem> {
        val clean = selectedSku.trim()
        if (clean.isBlank() || clean.equals("ALL", ignoreCase = true)) {
            return jackets
        }
        return jackets.filter { it.sku.trim().equals(clean, ignoreCase = true) }
    }

    /**
     * Generates a clean, unique variant SKU derived from an existing SKU template and target size.
     */
    fun buildVariantSkuFromTemplate(
        baseSku: String,
        targetSize: String,
        existingSkus: Set<String>
    ): String {
        val normalizedBase = baseSku.trim().uppercase()
        val normalizedSize = targetSize.trim().uppercase().ifBlank { "STD" }
        val existingUpper = existingSkus.map { it.trim().uppercase() }.toSet()

        if (normalizedBase.isBlank()) {
            return "JKT-NEW-$normalizedSize"
        }

        val parts = normalizedBase.split("-").filter { it.isNotBlank() }
        val rootPrefix = if (parts.size >= 2 && parts.last() in KNOWN_SIZE_SUFFIXES) {
            parts.dropLast(1).joinToString("-")
        } else {
            normalizedBase
        }

        val primaryCandidate = "$rootPrefix-$normalizedSize"
        if (primaryCandidate !in existingUpper) {
            return primaryCandidate
        }

        var counter = 2
        while (counter <= 999) {
            val numbered = "$primaryCandidate-$counter"
            if (numbered !in existingUpper) return numbered
            counter++
        }
        return "$primaryCandidate-${System.currentTimeMillis() % 1000}"
    }
}

/**
 * Interactive Material 3 ExposedDropdownMenu for filtering inventory lists by SKU.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkuFilterDropdown(
    jackets: List<JacketItem>,
    selectedSku: String,
    isIndonesian: Boolean,
    onSkuSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "sku_filter"
) {
    val themeSpec = LocalAppThemeSpec.current
    val skuOptions = remember(jackets) { SkuDropdownHelper.buildSkuOptions(jackets) }
    var expanded by remember { mutableStateOf(false) }

    val selectedOption = remember(skuOptions, selectedSku) {
        if (selectedSku == "ALL" || selectedSku.isBlank()) null
        else skuOptions.firstOrNull { it.sku.equals(selectedSku, ignoreCase = true) }
    }

    val isFilterActive = selectedSku != "ALL" && selectedSku.isNotBlank()
    val displayText = when {
        selectedOption != null -> "${selectedOption.sku} • ${selectedOption.name}"
        isFilterActive -> selectedSku
        else -> if (isIndonesian) {
            "Semua SKU (${skuOptions.size} kode)"
        } else {
            "All SKUs (${skuOptions.size} codes)"
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = {
                Text(
                    text = if (isIndonesian) "Filter Dropdown SKU" else "SKU Dropdown Filter",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isFilterActive) themeSpec.accentPrimary.copy(alpha = 0.14f)
                            else themeSpec.labelSecondary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = if (isIndonesian) "Pilih SKU" else "Select SKU",
                        tint = if (isFilterActive) themeSpec.accentPrimary else themeSpec.labelSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (isFilterActive) {
                        IconButton(
                            onClick = {
                                onSkuSelected("ALL")
                                expanded = false
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("${testTagPrefix}_clear_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isIndonesian) "Reset Filter SKU" else "Clear SKU Filter",
                                tint = themeSpec.labelSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 13.sp,
                fontWeight = if (isFilterActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isFilterActive) themeSpec.labelPrimary else themeSpec.labelSecondary
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeSpec.accentPrimary,
                unfocusedBorderColor = if (isFilterActive) themeSpec.accentPrimary else themeSpec.cardBorder,
                focusedContainerColor = themeSpec.cardBackground,
                unfocusedContainerColor = themeSpec.cardBackground
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .menuAnchor()
                .testTag("${testTagPrefix}_dropdown")
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .heightIn(max = 320.dp)
        ) {
            // Option 0: All SKUs
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = null,
                                tint = if (!isFilterActive) themeSpec.accentPrimary else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isIndonesian) "Semua SKU" else "All SKUs",
                                fontSize = 13.sp,
                                fontWeight = if (!isFilterActive) FontWeight.Bold else FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "${skuOptions.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                onClick = {
                    onSkuSelected("ALL")
                    expanded = false
                },
                trailingIcon = if (!isFilterActive) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = themeSpec.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                modifier = Modifier.testTag("${testTagPrefix}_option_all")
            )

            HorizontalDivider(color = Color(0xFFE2E8F0))

            if (skuOptions.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isIndonesian) "Belum ada kode SKU terdaftar" else "No SKU codes registered yet",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                skuOptions.forEach { option ->
                    val isCurrent = selectedSku.equals(option.sku, ignoreCase = true)
                    val statusColor = when (option.status) {
                        StockStatusLevel.IN_STOCK -> IosSystemGreen
                        StockStatusLevel.LOW_STOCK -> IosSystemOrange
                        StockStatusLevel.OUT_OF_STOCK -> IosSystemRed
                    }

                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(statusColor)
                                        )
                                        Text(
                                            text = option.sku,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isCurrent) themeSpec.accentPrimary else Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${option.name} • ${option.size} • ${option.locationRack}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusColor.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "${option.totalQuantity} pcs",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSkuSelected(option.sku)
                            expanded = false
                        },
                        trailingIcon = if (isCurrent) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = themeSpec.accentPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier.testTag("${testTagPrefix}_option_${option.sku}")
                    )
                }
            }
        }
    }
}

/**
 * Dropdown in AddJacketScreen allowing the user to pick an existing SKU from the database
 * as a template to quickly generate a new unique variant SKU.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkuTemplateDropdown(
    existingJackets: List<JacketItem>,
    targetSize: String,
    isIndonesian: Boolean,
    onTemplateSelected: (sourceJacket: JacketItem, suggestedVariantSku: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val existingSkus = remember(existingJackets) {
        existingJackets.map { it.sku.trim() }.filter { it.isNotBlank() }.toSet()
    }
    val sortedJackets = remember(existingJackets) {
        existingJackets
            .filter { it.sku.isNotBlank() }
            .distinctBy { it.sku.trim().uppercase() }
            .sortedBy { it.sku.uppercase() }
    }
    var expanded by remember { mutableStateOf(false) }
    var lastChosenSku by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = lastChosenSku.ifBlank {
                if (isIndonesian) {
                    "Pilih dari daftar SKU terdaftar (${sortedJackets.size})..."
                } else {
                    "Pick from registered SKUs (${sortedJackets.size})..."
                }
            },
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = {
                Text(
                    text = if (isIndonesian) "Dropdown Referensi / Template SKU" else "SKU Reference / Template Dropdown",
                    fontSize = 11.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = if (isIndonesian) "Dropdown SKU" else "SKU Dropdown",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 12.sp,
                fontWeight = if (lastChosenSku.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                color = if (lastChosenSku.isNotBlank()) Color(0xFF0F172A) else Color(0xFF64748B)
            ),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .menuAnchor()
                .testTag("add_jacket_sku_dropdown")
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .heightIn(max = 280.dp)
        ) {
            if (sortedJackets.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isIndonesian) "Belum ada SKU di inventaris" else "No SKUs in inventory yet",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                sortedJackets.forEach { jacket ->
                    val suggested = SkuDropdownHelper.buildVariantSkuFromTemplate(
                        baseSku = jacket.sku,
                        targetSize = targetSize,
                        existingSkus = existingSkus
                    )
                    DropdownMenuItem(
                        text = {
                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = jacket.sku,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "→ $suggested",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                                Text(
                                    text = "${jacket.name} • ${jacket.style} (${jacket.size})",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        onClick = {
                            lastChosenSku = "${jacket.sku} → $suggested"
                            onTemplateSelected(jacket, suggested)
                            expanded = false
                        },
                        modifier = Modifier.testTag("add_sku_template_${jacket.sku}")
                    )
                }
            }
        }
    }
}
