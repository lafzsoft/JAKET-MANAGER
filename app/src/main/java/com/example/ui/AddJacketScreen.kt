package com.example.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CatalogData
import com.example.model.StockStatusLevel
import com.example.ui.components.NetworkImageWithFallback
import com.example.ui.dialogs.CURATED_PRESETS
import com.example.ui.scanner.QrCodeScannerModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJacketScreen(
    viewModel: JacketViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    val isIndonesian by viewModel.isIndonesian.collectAsStateWithLifecycle()
    val allJackets by viewModel.allJackets.collectAsStateWithLifecycle()
    val prefilledSku by viewModel.prefilledSkuForAdd.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Form states
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var isManualSku by remember { mutableStateOf(false) }
    var manualSkuText by remember { mutableStateOf("") }
    var locationRack by remember { mutableStateOf("Rack A-01") }
    var unitPriceText by remember { mutableStateOf("89.99") }
    var isSeasonal by remember { mutableStateOf(false) }
    var isScannerOpenInAdd by remember { mutableStateOf(false) }

    // Category state (Demographic Category)
    val categoryOptions = remember {
        listOf("Jaket Pria", "Jaket Wanita", "Jaket Anak", "Jaket Unisex")
    }
    var selectedCategory by remember { mutableStateOf(categoryOptions[0]) }

    // Dropdown state: Style
    val styleOptions = remember {
        listOf("Bomber Jacket", "Parka", "Denim Jacket", "Trench Coat", "Puffer Jacket")
    }
    var selectedStyle by remember { mutableStateOf(styleOptions[0]) }
    var isStyleDropdownExpanded by remember { mutableStateOf(false) }

    // Dropdown state: Size
    val sizeOptions = remember {
        listOf(
            "XS" to "Fits chest 76–81 cm",
            "S" to "Fits chest 81–86 cm",
            "M" to "Fits chest 86–91 cm",
            "L" to "Fits chest 91–96 cm",
            "XL" to "Fits chest 96–101 cm",
            "XXL" to "Fits chest 101–106 cm",
            "3XL" to "Fits chest 106–112 cm"
        )
    }
    var selectedSize by remember { mutableStateOf("M") }
    var isSizeDropdownExpanded by remember { mutableStateOf(false) }

    // Numeric input state: Stock Quantity
    var quantityText by remember { mutableStateOf("12") }
    val quantityInt = quantityText.toIntOrNull() ?: 0

    // Material and Color selection
    var selectedMaterial by remember { mutableStateOf(CatalogData.MATERIALS[0].titleEn) }
    var selectedColor by remember { mutableStateOf(CatalogData.COLORS[0].titleEn) }

    // Error feedback
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    // SKU Image state & photo picker launcher
    var customImageUrl by remember { mutableStateOf("") }
    var showUrlInputField by remember { mutableStateOf(false) }
    var customUrlText by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customImageUrl = uri.toString()
        }
    }

    LaunchedEffect(prefilledSku) {
        prefilledSku?.let { pSku ->
            if (pSku.isNotBlank()) {
                isManualSku = true
                manualSkuText = pSku
                sku = pSku
                val parts = pSku.split("-")
                if (parts.size >= 3) {
                    when (parts[1].uppercase()) {
                        "BMB" -> selectedStyle = "Bomber Jacket"
                        "PRK" -> selectedStyle = "Parka"
                        "DNM" -> selectedStyle = "Denim Jacket"
                        "TRN" -> selectedStyle = "Trench Coat"
                        "PUF" -> selectedStyle = "Puffer Jacket"
                    }
                    val lastPart = parts.last().uppercase()
                    if (lastPart in listOf("XS", "S", "M", "L", "XL", "XXL", "3XL")) {
                        selectedSize = lastPart
                    }
                }
            }
        }
    }

    // Active SKU resolution & duplicate checking
    val activeSku = if (isManualSku) manualSkuText.trim() else sku.trim()
    val duplicateJacket = remember(activeSku, allJackets) {
        if (activeSku.isNotBlank()) {
            allJackets.firstOrNull { it.sku.equals(activeSku, ignoreCase = true) }
        } else null
    }
    val isDuplicateSku = duplicateJacket != null

    // Compute live stock status level
    val calculatedStatus = when {
        quantityInt <= 0 -> StockStatusLevel.OUT_OF_STOCK
        quantityInt <= 5 -> StockStatusLevel.LOW_STOCK
        else -> StockStatusLevel.IN_STOCK
    }

    // Auto-generate suggested SKU when name or style changes (Auto mode only)
    LaunchedEffect(name, selectedStyle, selectedSize, isManualSku) {
        if (!isManualSku) {
            val autoSku = viewModel.generateAutoSku(selectedStyle, name, selectedSize)
            sku = autoSku
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("add_jacket_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isIndonesian) "Tambah Jaket Baru" else "Add New Jacket",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = if (isIndonesian) "Inventaris & Manajemen Gudang" else "Inventory & Warehouse Stocking",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = Color(0xFF1A1A1A)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            name = ""
                            sku = ""
                            manualSkuText = ""
                            isManualSku = false
                            quantityText = "10"
                            locationRack = "Rack A-01"
                            selectedCategory = categoryOptions[0]
                            errorMessage = null
                        }
                    ) {
                        Text(
                            text = if (isIndonesian) "Reset" else "Reset",
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Warehouse Location Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isIndonesian) "Pusat Logistik & Gudang Jaket" else "Jacket Warehouse Logistics Entry",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isIndonesian) "Lengkapi detail model, ukuran, dan jumlah stok" else "Specify jacket details, dropdown selections, and stock count",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Form Container Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 1. TEXT FIELD FOR JACKET NAME
                    Text(
                        text = if (isIndonesian) "Nama Jaket *" else "Jacket Name *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                if (isIndonesian) "cth. Tactical MA-1 Leather Bomber" else "e.g. Tactical MA-1 Leather Bomber",
                                color = Color(0xFF94A3B8)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Checkroom,
                                contentDescription = null,
                                tint = Color(0xFF4A5568)
                            )
                        },
                        trailingIcon = {
                            if (name.isNotEmpty()) {
                                IconButton(onClick = { name = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jacket_name_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2D3748),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1.5. DEMOGRAPHIC CATEGORY SELECTOR
                    Text(
                        text = if (isIndonesian) "Kategori Target (Demographic Category) *" else "Target Category *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        items(categoryOptions) { cat ->
                            val isSelected = selectedCategory == cat
                            val icon = when (cat) {
                                "Jaket Pria" -> Icons.Default.Male
                                "Jaket Wanita" -> Icons.Default.Female
                                "Jaket Anak" -> Icons.Default.ChildCare
                                else -> Icons.Default.Group
                            }
                            val tagSlug = when (cat) {
                                "Jaket Pria" -> "pria"
                                "Jaket Wanita" -> "wanita"
                                "Jaket Anak" -> "anak"
                                else -> "unisex"
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1E293B),
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier.testTag("add_category_chip_$tagSlug")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. DROPDOWN FOR STYLE
                    Text(
                        text = if (isIndonesian) "Model Jaket (Style) *" else "Jacket Style *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = isStyleDropdownExpanded,
                        onExpandedChange = { isStyleDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedStyle,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = Color(0xFF4A5568)
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStyleDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("jacket_style_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2D3748),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = isStyleDropdownExpanded,
                            onDismissRequest = { isStyleDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            styleOptions.forEach { style ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Checkroom,
                                                contentDescription = null,
                                                tint = Color(0xFF2D3748),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = style,
                                                fontWeight = if (style == selectedStyle) FontWeight.Bold else FontWeight.Normal,
                                                color = Color(0xFF1A1A1A)
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedStyle = style
                                        isStyleDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("style_option_$style")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. DROPDOWN FOR SIZE
                    Text(
                        text = if (isIndonesian) "Ukuran (Size) *" else "Jacket Size *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = isSizeDropdownExpanded,
                        onExpandedChange = { isSizeDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val displaySizeText = sizeOptions.firstOrNull { it.first == selectedSize }
                            ?.let { "${it.first} (${it.second})" } ?: selectedSize

                        OutlinedTextField(
                            value = displaySizeText,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Straighten,
                                    contentDescription = null,
                                    tint = Color(0xFF4A5568)
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSizeDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("jacket_size_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2D3748),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = isSizeDropdownExpanded,
                            onDismissRequest = { isSizeDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            sizeOptions.forEach { (sizeTag, sizeDesc) ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = sizeTag,
                                                fontSize = 15.sp,
                                                fontWeight = if (sizeTag == selectedSize) FontWeight.Bold else FontWeight.SemiBold,
                                                color = Color(0xFF1A1A1A)
                                            )
                                            Text(
                                                text = sizeDesc,
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedSize = sizeTag
                                        isSizeDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("size_option_$sizeTag")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. NUMERIC INPUT FOR STOCK QUANTITY
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIndonesian) "Jumlah Stok (Stock Quantity) *" else "Stock Quantity *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )

                        // Status Badge indicator based on quantity
                        val (statusText, statusColor, statusBg) = when (calculatedStatus) {
                            StockStatusLevel.IN_STOCK -> Triple(
                                if (isIndonesian) "Stok Tersedia" else "In Stock",
                                Color(0xFF16A34A),
                                Color(0xFFDCFCE7)
                            )
                            StockStatusLevel.LOW_STOCK -> Triple(
                                if (isIndonesian) "Stok Sedikit" else "Low Stock",
                                Color(0xFFD97706),
                                Color(0xFFFEF3C7)
                            )
                            StockStatusLevel.OUT_OF_STOCK -> Triple(
                                if (isIndonesian) "Habis Stok" else "Out of Stock",
                                Color(0xFFDC2626),
                                Color(0xFFFEE2E2)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Numeric Text Field
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() }) {
                                    quantityText = input
                                    errorMessage = null
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = Color(0xFF4A5568)
                                )
                            },
                            placeholder = { Text("0") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("jacket_quantity_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2D3748),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )

                        // Stepper Decrease Button (-)
                        FilledTonalIconButton(
                            onClick = {
                                val current = quantityText.toIntOrNull() ?: 0
                                if (current > 0) quantityText = (current - 1).toString()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("quantity_decrement_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF1E293B)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Quantity")
                        }

                        // Stepper Increase Button (+)
                        FilledTonalIconButton(
                            onClick = {
                                val current = quantityText.toIntOrNull() ?: 0
                                quantityText = (current + 1).toString()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("quantity_increment_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF1E293B)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Quantity")
                        }
                    }

                    // Quick Quantity Presets
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 25, 50).forEach { quickVal ->
                            SuggestionChip(
                                onClick = {
                                    val current = quantityText.toIntOrNull() ?: 0
                                    quantityText = (current + quickVal).toString()
                                },
                                label = { Text("+$quickVal", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color(0xFFF8FAFC),
                                    labelColor = Color(0xFF334155)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. SKU CODE & BARCODE (AUTO VS MANUAL INPUT)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(
                                1.dp,
                                if (isDuplicateSku) Color(0xFFEF4444) else Color(0xFFE2E8F0),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color(0xFF1E293B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isIndonesian) "Kode SKU & Barcode *" else "SKU Code & Barcode *",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            // Mode Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isManualSku) Color(0xFFEFF6FF) else Color(0xFFF0FDF4),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isManualSku) Color(0xFF93C5FD) else Color(0xFF86EFAC)
                                )
                            ) {
                                Text(
                                    text = if (isManualSku) {
                                        if (isIndonesian) "Mode: Manual" else "Mode: Manual"
                                    } else {
                                        if (isIndonesian) "Mode: Otomatis" else "Mode: Auto-Gen"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isManualSku) Color(0xFF1D4ED8) else Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mode Selector: Otomatis vs Manual
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Auto Button
                            OutlinedButton(
                                onClick = {
                                    isManualSku = false
                                    sku = viewModel.generateAutoSku(selectedStyle, name, selectedSize)
                                    errorMessage = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!isManualSku) Color(0xFF1E293B) else Color.White,
                                    contentColor = if (!isManualSku) Color.White else Color(0xFF475569)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (!isManualSku) Color(0xFF1E293B) else Color(0xFFCBD5E1)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sku_mode_auto_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isIndonesian) "⚡ Otomatis" else "⚡ Auto-Gen",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Manual Button
                            OutlinedButton(
                                onClick = {
                                    isManualSku = true
                                    if (manualSkuText.isBlank()) {
                                        manualSkuText = sku.ifBlank { viewModel.generateAutoSku(selectedStyle, name, selectedSize) }
                                    }
                                    sku = manualSkuText
                                    errorMessage = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isManualSku) Color(0xFF1E293B) else Color.White,
                                    contentColor = if (isManualSku) Color.White else Color(0xFF475569)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isManualSku) Color(0xFF1E293B) else Color(0xFFCBD5E1)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sku_mode_manual_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isIndonesian) "✏️ Input Manual" else "✏️ Manual SKU",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Scan Label Button
                            OutlinedButton(
                                onClick = {
                                    isScannerOpenInAdd = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF2563EB)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sku_mode_scan_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF2563EB)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isIndonesian) "📷 Scan QR" else "📷 Scan QR",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!isManualSku) {
                            // AUTO SKU DISPLAY CARD
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = if (isIndonesian) "SKU Tergenerate (Otomatis):" else "Generated SKU (Auto):",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = sku.ifBlank { "JKT-..." },
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                isManualSku = true
                                                manualSkuText = sku
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("btn_switch_to_manual_sku")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = Color(0xFF2563EB)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isIndonesian) "Ubah Manual" else "Edit Manually",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF2563EB)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isIndonesian) "Format sistem: JKT-[Model]-[Nama]-[Ukuran]" else "System format: JKT-[Style]-[Name]-[Size]",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        } else {
                            // MANUAL SKU INPUT FIELD
                            OutlinedTextField(
                                value = manualSkuText,
                                onValueChange = { input ->
                                    val cleaned = input.uppercase().filter { it.isLetterOrDigit() || it == '-' || it == '_' }
                                    manualSkuText = cleaned
                                    sku = cleaned
                                    errorMessage = null
                                },
                                label = { Text(if (isIndonesian) "Kode SKU Manual *" else "Manual SKU Code *") },
                                placeholder = { Text("cth. WHR-BOMBER-001") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Tag,
                                        contentDescription = null,
                                        tint = if (isDuplicateSku) Color(0xFFDC2626) else Color(0xFF475569)
                                    )
                                },
                                trailingIcon = {
                                    if (manualSkuText.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                manualSkuText = ""
                                                sku = ""
                                            },
                                            modifier = Modifier.testTag("clear_manual_sku_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear SKU",
                                                tint = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                },
                                isError = isDuplicateSku,
                                supportingText = {
                                    if (isDuplicateSku) {
                                        Text(
                                            text = if (isIndonesian) "⚠️ SKU '$manualSkuText' SUDAH DIGUNAKAN oleh '${duplicateJacket?.name}'!" else "⚠️ SKU '$manualSkuText' is ALREADY USED by '${duplicateJacket?.name}'!",
                                            color = Color(0xFFDC2626),
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else if (manualSkuText.isBlank()) {
                                        Text(
                                            text = if (isIndonesian) "Wajib diisi pada mode manual" else "Required in manual mode",
                                            color = Color(0xFF94A3B8)
                                        )
                                    } else {
                                        Text(
                                            text = if (isIndonesian) "✅ SKU unik & siap digunakan" else "✅ SKU is unique & ready",
                                            color = Color(0xFF16A34A),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isDuplicateSku) Color(0xFFDC2626) else Color(0xFF2563EB),
                                    unfocusedBorderColor = if (isDuplicateSku) Color(0xFFEF4444) else Color(0xFFCBD5E1),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("jacket_sku_field")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Prefix / Random Helper Chips
                            Text(
                                text = if (isIndonesian) "Template / Generator Cepat:" else "Quick Templates / Generator:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Randomizer chip
                                AssistChip(
                                    onClick = {
                                        val randomSku = viewModel.generateRandomCustomSku("WHR")
                                        manualSkuText = randomSku
                                        sku = randomSku
                                        errorMessage = null
                                    },
                                    label = {
                                        Text(
                                            text = if (isIndonesian) "🎲 Acak WHR" else "🎲 Random WHR",
                                            fontSize = 11.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("btn_random_sku")
                                )

                                // Prefix WHR-
                                AssistChip(
                                    onClick = {
                                        if (!manualSkuText.startsWith("WHR-")) {
                                            manualSkuText = "WHR-" + manualSkuText.removePrefix("JKT-").removePrefix("BAR-")
                                            sku = manualSkuText
                                        }
                                    },
                                    label = { Text("WHR-", fontSize = 11.sp) }
                                )

                                // Prefix JKT-
                                AssistChip(
                                    onClick = {
                                        if (!manualSkuText.startsWith("JKT-")) {
                                            manualSkuText = "JKT-" + manualSkuText.removePrefix("WHR-").removePrefix("BAR-")
                                            sku = manualSkuText
                                        }
                                    },
                                    label = { Text("JKT-", fontSize = 11.sp) }
                                )

                                // Prefix BAR-
                                AssistChip(
                                    onClick = {
                                        if (!manualSkuText.startsWith("BAR-")) {
                                            manualSkuText = "BAR-" + manualSkuText.removePrefix("WHR-").removePrefix("JKT-")
                                            sku = manualSkuText
                                        }
                                    },
                                    label = { Text("BAR-", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. RACK LOCATION
                    Text(
                        text = if (isIndonesian) "Lokasi Rak Gudang *" else "Warehouse Rack Location *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = locationRack,
                        onValueChange = { locationRack = it },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2D3748),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA)
                        ),
                        placeholder = { Text("cth. Rack A-01, Bin B-03") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jacket_rack_field")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. MATERIAL SELECTION
                    Text(
                        text = if (isIndonesian) "Bahan (Material)" else "Material",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CatalogData.MATERIALS) { mat ->
                            val isSelected = selectedMaterial == mat.titleEn
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMaterial = mat.titleEn },
                                label = { Text(if (isIndonesian) mat.titleId else mat.titleEn, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2D3748),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7. COLOR SELECTION
                    Text(
                        text = if (isIndonesian) "Warna (Color)" else "Color",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CatalogData.COLORS, key = { it.id }) { colorCat ->
                            val colorTitle = if (isIndonesian) colorCat.titleId else colorCat.titleEn
                            val isSelected = selectedColor.equals(colorCat.titleEn, ignoreCase = true) ||
                                             selectedColor.equals(colorCat.titleId, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFF2D3748) else Color(0xFFF1F5F9))
                                    .clickable { selectedColor = if (isIndonesian) colorCat.titleId else colorCat.titleEn }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorCat.hexColor))
                                        .border(0.6.dp, Color(0xFF94A3B8), CircleShape)
                                )
                                Text(
                                    text = colorTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF1A1A1A)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 8. SEASONAL COLLECTION TOGGLE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isIndonesian) "Koleksi Musiman Musim Dingin" else "Winter Seasonal Collection",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = if (isIndonesian) "Tandai untuk ditampilkan pada banner koleksi" else "Mark for seasonal promotional showcase",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Switch(
                            checked = isSeasonal,
                            onCheckedChange = { isSeasonal = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2D3748)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 9. SKU IMAGE SELECTION SECTION
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isIndonesian) "Gambar / Foto SKU Produk" else "Product SKU Image",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isIndonesian) "Pilih foto galeri, preset katalog, atau URL web" else "Choose gallery photo, catalog preset, or web URL",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        if (customImageUrl.isNotBlank()) {
                            TextButton(onClick = { customImageUrl = "" }) {
                                Text(
                                    text = if (isIndonesian) "Reset Preset" else "Reset Preset",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons: Photo Picker and URL input toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_pick_photo_add_screen"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isIndonesian) "Pilih dari Galeri" else "Pick from Gallery",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { showUrlInputField = !showUrlInputField },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isIndonesian) "Input URL" else "Enter URL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (showUrlInputField) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = customUrlText,
                                onValueChange = { customUrlText = it },
                                placeholder = { Text("https://domain.com/gambar.png", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Button(
                                onClick = {
                                    if (customUrlText.isNotBlank()) {
                                        customImageUrl = customUrlText.trim()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Text(if (isIndonesian) "Terapkan" else "Apply", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Quick Carousel
                    Text(
                        text = if (isIndonesian) "Preset Katalog Pilihan:" else "Curated Catalog Presets:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CURATED_PRESETS, key = { it.id }) { preset ->
                            val isSelected = (customImageUrl == preset.imageUrl) ||
                                (customImageUrl.isBlank() && preset.nameEn.contains(selectedStyle, ignoreCase = true))
                            Column(
                                modifier = Modifier
                                    .width(76.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { customImageUrl = preset.imageUrl }
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                ) {
                                    NetworkImageWithFallback(
                                        imageUrl = preset.imageUrl,
                                        contentDescription = preset.nameEn,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text(
                                    text = if (isIndonesian) preset.nameId else preset.nameEn,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Preview Card
            Text(
                text = if (isIndonesian) "Pratinjau Kartu Inventaris" else "Live Inventory Preview",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            val defaultStyleImage = remember(selectedStyle) {
                CatalogData.STYLES.firstOrNull { it.titleEn.equals(selectedStyle, ignoreCase = true) }?.imageUrl
                    ?: CatalogData.HERO_IMAGE_URL
            }
            val effectivePreviewImage = if (customImageUrl.isNotBlank()) customImageUrl else defaultStyleImage

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        NetworkImageWithFallback(
                            imageUrl = effectivePreviewImage,
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = sku.ifBlank { "JKT-SKU" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "$quantityInt units",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (calculatedStatus) {
                                    StockStatusLevel.IN_STOCK -> Color(0xFF16A34A)
                                    StockStatusLevel.LOW_STOCK -> Color(0xFFD97706)
                                    StockStatusLevel.OUT_OF_STOCK -> Color(0xFFDC2626)
                                }
                            )
                        }
                        Text(
                            text = name.ifBlank { "Sample Jacket Name" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$selectedCategory • $selectedStyle • $selectedSize • $selectedColor",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Error display
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626)
                            )
                            Text(
                                text = it,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Cancel and Gradient Submit
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF2D3748), Color(0xFF4A5568))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2D3748)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(
                        text = if (isIndonesian) "Batal" else "Cancel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(25.dp))
                        .clip(RoundedCornerShape(25.dp))
                        .background(gradientBrush)
                        .clickable {
                            if (name.isBlank()) {
                                errorMessage = if (isIndonesian) "Mohon isi nama jaket" else "Please enter jacket name"
                                return@clickable
                            }
                            val qty = quantityText.toIntOrNull()
                            if (qty == null || qty < 0) {
                                errorMessage = if (isIndonesian) "Jumlah stok harus berupa angka valid" else "Quantity must be a valid number"
                                return@clickable
                            }

                            val resolvedSku = if (isManualSku) manualSkuText.trim() else sku.trim()
                            if (isManualSku && resolvedSku.isBlank()) {
                                errorMessage = if (isIndonesian) "Mohon masukkan kode SKU manual" else "Please enter manual SKU code"
                                return@clickable
                            }
                            if (isDuplicateSku) {
                                errorMessage = if (isIndonesian) "SKU '$resolvedSku' sudah digunakan oleh '${duplicateJacket?.name}'. Harap gunakan SKU unik!" else "SKU '$resolvedSku' is already in use by '${duplicateJacket?.name}'. Please enter a unique SKU!"
                                return@clickable
                            }

                            val finalSku = resolvedSku.ifBlank { "JKT-${System.currentTimeMillis() % 10000}" }
                            viewModel.addNewJacket(
                                sku = finalSku,
                                name = name,
                                style = selectedStyle,
                                size = selectedSize,
                                material = selectedMaterial,
                                color = selectedColor,
                                quantity = qty,
                                locationRack = locationRack.ifBlank { "Rack A-01" },
                                isSeasonal = isSeasonal,
                                genderCategory = selectedCategory,
                                imageUrl = customImageUrl
                            )
                            onNavigateBack()
                        }
                        .testTag("save_jacket_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isIndonesian) "Simpan ke Inventaris" else "Add to Inventory",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }

    if (isScannerOpenInAdd) {
        QrCodeScannerModal(
            allJackets = allJackets,
            isIndonesian = isIndonesian,
            onDismiss = { isScannerOpenInAdd = false },
            onJacketIdentified = { identifiedJacket ->
                isScannerOpenInAdd = false
                isManualSku = true
                manualSkuText = identifiedJacket.sku
                sku = identifiedJacket.sku
                name = identifiedJacket.name
                selectedStyle = identifiedJacket.style
                selectedSize = identifiedJacket.size
                selectedMaterial = identifiedJacket.material
                selectedColor = identifiedJacket.color
                locationRack = identifiedJacket.locationRack
            },
            onAddNewWithSku = { scannedSku ->
                isScannerOpenInAdd = false
                isManualSku = true
                manualSkuText = scannedSku
                sku = scannedSku
                val parts = scannedSku.split("-")
                if (parts.size >= 3) {
                    when (parts[1].uppercase()) {
                        "BMB" -> selectedStyle = "Bomber Jacket"
                        "PRK" -> selectedStyle = "Parka"
                        "DNM" -> selectedStyle = "Denim Jacket"
                        "TRN" -> selectedStyle = "Trench Coat"
                        "PUF" -> selectedStyle = "Puffer Jacket"
                    }
                    val lastPart = parts.last().uppercase()
                    if (lastPart in listOf("XS", "S", "M", "L", "XL", "XXL", "3XL")) {
                        selectedSize = lastPart
                    }
                }
            }
        )
    }
}
