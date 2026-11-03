package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CatalogData
import com.example.model.JacketItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJacketSheet(
    isIndonesian: Boolean,
    existingJackets: List<JacketItem> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        sku: String,
        name: String,
        style: String,
        size: String,
        material: String,
        color: String,
        quantity: Int,
        locationRack: String,
        isSeasonal: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form inputs: name, style, size, quantity (core required fields)
    var name by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(CatalogData.STYLES.first().titleEn) }
    var selectedSize by remember { mutableStateOf(CatalogData.SIZES[2].title) } // M
    var quantity by remember { mutableIntStateOf(10) }
    var quantityInputText by remember { mutableStateOf("10") }

    // Additional metadata
    var sku by remember { mutableStateOf("") }
    var isManualSku by remember { mutableStateOf(false) }
    var manualSkuText by remember { mutableStateOf("") }
    var selectedMaterial by remember { mutableStateOf(CatalogData.MATERIALS.first().titleEn) }
    var selectedColor by remember { mutableStateOf(CatalogData.COLORS.first().titleEn) }
    var locationRack by remember { mutableStateOf("Rack A-01") }
    var isSeasonal by remember { mutableStateOf(false) }
    var showAdditionalDetails by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Active SKU and duplicate check
    val activeSku = if (isManualSku) manualSkuText.trim() else sku.trim()
    val duplicateJacket = remember(activeSku, existingJackets) {
        if (activeSku.isNotBlank()) {
            existingJackets.firstOrNull { it.sku.equals(activeSku, ignoreCase = true) }
        } else null
    }
    val isDuplicateSku = duplicateJacket != null

    fun generateAutoSkuCandidate(): String {
        val styleCode = selectedStyle.take(3).uppercase()
        val nameCode = if (name.isNotBlank()) name.take(3).uppercase().replace(" ", "") else "NEW"
        val base = "JKT-$styleCode-$nameCode-$selectedSize"
        return if (existingJackets.any { it.sku.equals(base, ignoreCase = true) }) {
            "$base-${(10..99).random()}"
        } else {
            base
        }
    }

    LaunchedEffect(name, selectedStyle, selectedSize, isManualSku) {
        if (!isManualSku) {
            sku = generateAutoSkuCandidate()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("add_jacket_modal_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = "ROOM DB",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = if (isIndonesian) "Input Jaket Baru" else "Add New Jacket",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(
                        text = if (isIndonesian)
                            "Simpan langsung ke Room database lokal"
                        else
                            "Save directly to local Room database",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_close_add_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. FIELD: NAMA JAKET (NAME)
            Text(
                text = if (isIndonesian) "1. Nama Jaket *" else "1. Jacket Name *",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                placeholder = {
                    Text(if (isIndonesian) "cth. Bomber Pilot Kulit Premium" else "e.g. Premium Leather Bomber")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = Color(0xFF475569)
                    )
                },
                trailingIcon = {
                    if (name.isNotEmpty()) {
                        IconButton(onClick = { name = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jacket_name_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E293B),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. FIELD: MODEL / STYLE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isIndonesian) "2. Model / Gaya Jaket *" else "2. Jacket Style *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = selectedStyle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("jacket_style_row")
            ) {
                items(CatalogData.STYLES) { style ->
                    val isSelected = selectedStyle.equals(style.titleEn, ignoreCase = true)
                    val label = if (isIndonesian) style.titleId else style.titleEn
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedStyle = style.titleEn
                            errorMessage = null
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E293B),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .testTag("style_chip_${style.titleEn.lowercase()}")
                            .heightIn(min = 40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. FIELD: UKURAN / SIZE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isIndonesian) "3. Ukuran / Size *" else "3. Jacket Size *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Size $selectedSize",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jacket_size_row"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CatalogData.SIZES.forEach { sizeItem ->
                    val isSelected = selectedSize.equals(sizeItem.title, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("size_btn_${sizeItem.title}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        ),
                        onClick = {
                            selectedSize = sizeItem.title
                            errorMessage = null
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = sizeItem.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. FIELD: JUMLAH KUANTITAS STOK (QUANTITY)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jacket_quantity_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIndonesian) "4. Jumlah Kuantitas Stok *" else "4. Stock Quantity *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )

                        // Status preview badge
                        val (statusText, statusBg, statusColor) = when {
                            quantity == 0 -> Triple(
                                if (isIndonesian) "Habis (0)" else "Out of Stock (0)",
                                Color(0xFFFEE2E2),
                                Color(0xFFDC2626)
                            )
                            quantity <= 5 -> Triple(
                                if (isIndonesian) "Menipis (≤5)" else "Low Stock (≤5)",
                                Color(0xFFFEF3C7),
                                Color(0xFFD97706)
                            )
                            else -> Triple(
                                if (isIndonesian) "Tersedia (>5)" else "In Stock (>5)",
                                Color(0xFFDCFCE7),
                                Color(0xFF16A34A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = statusBg
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stepper row with direct number input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Minus button (48dp touch target)
                        IconButton(
                            onClick = {
                                if (quantity > 0) {
                                    val newQty = quantity - 1
                                    quantity = newQty
                                    quantityInputText = newQty.toString()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0))
                                .testTag("jacket_quantity_decrease")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease Quantity",
                                tint = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Direct number input
                        OutlinedTextField(
                            value = quantityInputText,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(4)
                                quantityInputText = clean
                                val parsed = clean.toIntOrNull() ?: 0
                                quantity = parsed
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            ),
                            modifier = Modifier
                                .width(100.dp)
                                .testTag("jacket_quantity_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E293B),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Plus button (48dp touch target)
                        IconButton(
                            onClick = {
                                val newQty = quantity + 1
                                quantity = newQty
                                quantityInputText = newQty.toString()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0))
                                .testTag("jacket_quantity_increase")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase Quantity",
                                tint = Color(0xFF1E293B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Add Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 25, 50).forEach { amount ->
                            AssistChip(
                                onClick = {
                                    val newQty = quantity + amount
                                    quantity = newQty
                                    quantityInputText = newQty.toString()
                                },
                                label = {
                                    Text(
                                        text = "+$amount",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_add_qty_$amount")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable: Informasi Tambahan (SKU, Rak, Bahan, Warna)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdditionalDetails = !showAdditionalDetails },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isIndonesian) "Informasi Tambahan (SKU, Rak, Bahan, Warna)" else "Additional Details (SKU, Rack, Material, Color)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                    Icon(
                        imageVector = if (showAdditionalDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF475569)
                    )
                }
            }

            AnimatedVisibility(visible = showAdditionalDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    // SKU Section
                    Text(
                        text = if (isIndonesian) "Kode SKU / Barcode" else "SKU / Barcode Code",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = if (isManualSku) manualSkuText else sku,
                        onValueChange = {
                            isManualSku = true
                            manualSkuText = it.uppercase()
                            sku = manualSkuText
                        },
                        label = { Text(if (isManualSku) "SKU Manual" else "SKU Otomatis") },
                        placeholder = { Text("cth. JKT-BOM-NEW-M") },
                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    isManualSku = !isManualSku
                                    if (!isManualSku) sku = generateAutoSkuCandidate()
                                }
                            ) {
                                Text(
                                    text = if (isManualSku) (if (isIndonesian) "Otomatis" else "Auto") else (if (isIndonesian) "Manual" else "Edit"),
                                    fontSize = 11.sp
                                )
                            }
                        },
                        isError = isDuplicateSku,
                        supportingText = {
                            if (isDuplicateSku) {
                                Text(
                                    text = if (isIndonesian) "⚠️ SKU sudah digunakan oleh '${duplicateJacket?.name}'" else "⚠️ SKU already used by '${duplicateJacket?.name}'",
                                    color = Color(0xFFDC2626),
                                    fontSize = 11.sp
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jacket_sku_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rack Location
                    Text(
                        text = if (isIndonesian) "Lokasi Rak Gudang" else "Warehouse Rack Location",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = locationRack,
                        onValueChange = { locationRack = it },
                        placeholder = { Text("Rack A-01") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jacket_rack_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Material Selector
                    Text(
                        text = if (isIndonesian) "Bahan / Material" else "Material",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CatalogData.MATERIALS) { mat ->
                            val isSelected = selectedMaterial.equals(mat.titleEn, ignoreCase = true)
                            val label = if (isIndonesian) mat.titleId else mat.titleEn
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMaterial = mat.titleEn },
                                label = { Text(label, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Color Selector
                    Text(
                        text = if (isIndonesian) "Warna / Color" else "Color",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CatalogData.COLORS) { col ->
                            val isSelected = selectedColor.equals(col.titleEn, ignoreCase = true)
                            val label = if (isIndonesian) col.titleId else col.titleEn
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedColor = col.titleEn },
                                label = { Text(label, fontSize = 12.sp) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(col.hexColor))
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Seasonal switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIndonesian) "Koleksi Musim Dingin (Seasonal)" else "Winter Seasonal Collection",
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )
                        Switch(
                            checked = isSeasonal,
                            onCheckedChange = { isSeasonal = it }
                        )
                    }
                }
            }

            // Error banner if any
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFDC2626),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Save CTA Button
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF334155))
            )

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = if (isIndonesian) "Mohon masukkan nama jaket!" else "Please enter jacket name!"
                        return@Button
                    }
                    val resolvedSku = if (isManualSku) manualSkuText.trim() else sku.trim()
                    if (isManualSku && resolvedSku.isBlank()) {
                        errorMessage = if (isIndonesian) "Mohon masukkan kode SKU manual!" else "Please enter manual SKU code!"
                        return@Button
                    }
                    if (isDuplicateSku) {
                        errorMessage = if (isIndonesian)
                            "SKU '$resolvedSku' sudah digunakan oleh '${duplicateJacket?.name}'. Harap gunakan SKU unik!"
                        else
                            "SKU '$resolvedSku' is already used by '${duplicateJacket?.name}'. Please enter a unique SKU!"
                        return@Button
                    }
                    val finalSku = resolvedSku.ifBlank { generateAutoSkuCandidate() }

                    onSave(
                        finalSku,
                        name.trim(),
                        selectedStyle,
                        selectedSize,
                        selectedMaterial,
                        selectedColor,
                        quantity,
                        locationRack.trim().ifBlank { "Rack A-01" },
                        isSeasonal
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_jacket_to_room")
                    .testTag("confirm_add_jacket_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isIndonesian) "Simpan ke Room Database" else "Save to Room Database",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
