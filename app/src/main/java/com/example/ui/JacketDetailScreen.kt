package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.CatalogData
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import com.example.ui.components.CompactAppTopBar
import com.example.ui.components.NetworkImageWithFallback
import com.example.ui.dialogs.EditJacketSheet
import com.example.ui.dialogs.SingleUnitOrderDialog
import com.example.ui.theme.*
import com.example.util.CurrencyUtils
import com.example.util.WhatsAppShareHelper
import kotlinx.coroutines.launch

/**
 * Full-screen detail view for a selected jacket from the Room database.
 * Displays hero imagery, prominent Description, Material, and Price fields with inline editing
 * saved directly to Room, plus stock adjustments, color/size variants, WhatsApp ordering, and deletion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JacketDetailScreen(
    viewModel: JacketViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedJacket by viewModel.selectedJacket.collectAsStateWithLifecycle()
    val allJackets by viewModel.allJackets.collectAsStateWithLifecycle()
    val isIndonesian by viewModel.isIndonesian.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isEditingSpecs by remember { mutableStateOf(false) }
    var isFullEditSheetOpen by remember { mutableStateOf(false) }
    var isOrderDialogOpen by remember { mutableStateOf(false) }
    var isDeleteConfirmOpen by remember { mutableStateOf(false) }

    BackHandler {
        if (isEditingSpecs) {
            isEditingSpecs = false
        } else {
            onNavigateBack()
        }
    }

    val jacket = selectedJacket

    // Local editable state synchronized whenever the selected jacket changes
    var editedDescription by remember(jacket?.id, jacket?.description) {
        mutableStateOf(
            jacket?.let { item ->
                item.description.ifBlank { buildDefaultJacketDescription(item, isIndonesian) }
            } ?: ""
        )
    }
    var editedMaterial by remember(jacket?.id, jacket?.material) {
        mutableStateOf(jacket?.material ?: "")
    }
    var editedPriceText by remember(jacket?.id, jacket?.unitPrice) {
        mutableStateOf(jacket?.let { CurrencyUtils.formatRupiahRaw(it.unitPrice) } ?: "")
    }
    var priceError by remember { mutableStateOf<String?>(null) }

    val themeSpec = LocalAppThemeSpec.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("jacket_detail_screen"),
        containerColor = themeSpec.screenBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CompactAppTopBar(
                title = if (isIndonesian) {
                    stringResource(R.string.jacket_detail_title_id)
                } else {
                    stringResource(R.string.jacket_detail_title)
                },
                subtitle = jacket?.sku,
                containerColor = themeSpec.cardBackground,
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_detail_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isIndonesian) "Kembali" else "Back",
                            tint = themeSpec.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    if (jacket != null) {
                        IconButton(
                            onClick = {
                                WhatsAppShareHelper.shareJacketToWhatsApp(context, jacket, isIndonesian)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_detail_share_wa")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = if (isIndonesian) "Bagikan via WhatsApp" else "Share via WhatsApp",
                                tint = IosSystemGreen,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        IconButton(
                            onClick = { isFullEditSheetOpen = true },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_detail_full_edit")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isIndonesian) "Edit Semua Data" else "Edit All Details",
                                tint = themeSpec.accentPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (jacket == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.widthIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checkroom,
                            contentDescription = null,
                            tint = IosLabelSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isIndonesian) "Jaket tidak ditemukan" else "Jacket not found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosLabelPrimary
                        )
                        Text(
                            text = if (isIndonesian) {
                                "Item ini mungkin telah dihapus dari database inventaris."
                            } else {
                                "This jacket may have been removed from your inventory database."
                            },
                            fontSize = 14.sp,
                            color = IosLabelSecondary
                        )
                        Button(
                            onClick = onNavigateBack,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_detail_not_found_back")
                        ) {
                            Text(
                                text = if (isIndonesian) "Kembali ke Daftar" else "Back to List",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            return@Scaffold
        }

        val displayDescription = remember(jacket.description, jacket.id, isIndonesian) {
            jacket.description.ifBlank { buildDefaultJacketDescription(jacket, isIndonesian) }
        }

        val siblingVariants = remember(allJackets, jacket.id, jacket.name, jacket.style) {
            allJackets.filter {
                it.name.equals(jacket.name, ignoreCase = true) ||
                    (it.style.equals(jacket.style, ignoreCase = true) &&
                        it.genderCategory.equals(jacket.genderCategory, ignoreCase = true))
            }.distinctBy { it.id }.take(12)
        }

        val sizeBreakdown = remember(allJackets, jacket.name, jacket.style) {
            val matching = allJackets.filter {
                it.name.equals(jacket.name, ignoreCase = true) ||
                    it.style.equals(jacket.style, ignoreCase = true)
            }
            listOf("XS", "S", "M", "L", "XL", "XXL", "3XL").map { sz ->
                sz to matching.filter { it.size.equals(sz, ignoreCase = true) }.sumOf { it.quantity }
            }
        }

        val statusColors = when (jacket.status) {
            StockStatusLevel.IN_STOCK -> Triple(
                StockInGreenBg,
                StockInGreen,
                if (isIndonesian) "Stok Aman" else "In Stock"
            )
            StockStatusLevel.LOW_STOCK -> Triple(
                StockLowAmberBg,
                StockLowAmber,
                if (isIndonesian) "Stok Menipis" else "Low Stock"
            )
            StockStatusLevel.OUT_OF_STOCK -> Triple(
                StockOutRedBg,
                StockOutRed,
                if (isIndonesian) "Habis Stok" else "Out of Stock"
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themeSpec.backgroundBrush)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hero Product Image Card
                HeroJacketImageCard(
                    jacket = jacket,
                    statusBg = statusColors.first,
                    statusFg = statusColors.second,
                    statusLabel = statusColors.third,
                    isIndonesian = isIndonesian,
                    onEditPhotoClick = { viewModel.openImagePickerForJacket(jacket) }
                )

                // 2. Title, Style, Category & Price Highlight Card
                HeaderOverviewCard(
                    jacket = jacket,
                    isIndonesian = isIndonesian
                )

                // 3. Dedicated Description, Material & Price Card (with inline editing & Room persistence)
                DescriptionMaterialPriceCard(
                    jacket = jacket,
                    displayDescription = displayDescription,
                    isIndonesian = isIndonesian,
                    isEditing = isEditingSpecs,
                    editedDescription = editedDescription,
                    editedMaterial = editedMaterial,
                    editedPriceText = editedPriceText,
                    priceError = priceError,
                    onToggleEdit = {
                        if (!isEditingSpecs) {
                            editedDescription = displayDescription
                            editedMaterial = jacket.material
                            editedPriceText = CurrencyUtils.formatRupiahRaw(jacket.unitPrice)
                            priceError = null
                        }
                        isEditingSpecs = !isEditingSpecs
                    },
                    onDescriptionChange = { editedDescription = it },
                    onMaterialChange = { editedMaterial = it },
                    onPriceChange = { input ->
                        editedPriceText = input.filter { it.isDigit() || it == '.' || it == ',' }
                        priceError = null
                    },
                    onQuickPriceSelect = { quickPrice ->
                        editedPriceText = CurrencyUtils.formatRupiahRaw(quickPrice)
                        priceError = null
                    },
                    onSave = {
                        val parsedPrice = CurrencyUtils.parseRupiah(editedPriceText, defaultPrice = -1.0)
                        if (parsedPrice <= 0.0) {
                            priceError = if (isIndonesian) {
                                "Masukkan harga yang valid"
                            } else {
                                "Please enter a valid price"
                            }
                        } else {
                            val cleanMaterial = editedMaterial.trim().ifBlank { jacket.material }
                            val cleanDesc = editedDescription.trim()
                            viewModel.updateJacketDescriptionMaterialPrice(
                                id = jacket.id,
                                description = cleanDesc,
                                material = cleanMaterial,
                                unitPrice = parsedPrice
                            )
                            isEditingSpecs = false
                            priceError = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (isIndonesian) {
                                        "Deskripsi, bahan & harga disimpan ke database"
                                    } else {
                                        "Description, material & price saved"
                                    },
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )

                // 4. Stock Control & Warehouse Rack Card
                StockControlCard(
                    jacket = jacket,
                    isIndonesian = isIndonesian,
                    onAdjustQuantity = { delta -> viewModel.adjustQuantity(jacket.id, delta) },
                    onRestock = {
                        viewModel.restock(jacket.id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (isIndonesian) "+15 unit ditambahkan ke stok" else "+15 units restocked",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )

                // 5. Color Variants & Size Distribution Card
                if (siblingVariants.size > 1) {
                    VariantsAndSizesCard(
                        currentJacket = jacket,
                        variants = siblingVariants,
                        sizeBreakdown = sizeBreakdown,
                        isIndonesian = isIndonesian,
                        onSelectVariant = { variant ->
                            isEditingSpecs = false
                            viewModel.navigateToJacketDetail(variant.id)
                        }
                    )
                }

                // 6. Primary Actions Footer (WhatsApp Order, Full Edit, Delete)
                DetailActionsCard(
                    isIndonesian = isIndonesian,
                    onOrderWhatsApp = { isOrderDialogOpen = true },
                    onFullEdit = { isFullEditSheetOpen = true },
                    onDeleteClick = { isDeleteConfirmOpen = true }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }

    // Full Edit Bottom Sheet
    if (isFullEditSheetOpen && jacket != null) {
        EditJacketSheet(
            jacket = jacket,
            isIndonesian = isIndonesian,
            existingJackets = allJackets,
            onDismiss = { isFullEditSheetOpen = false },
            onSaveUpdate = { id, sku, name, style, size, material, color, quantity, locationRack, isSeasonal, unitPrice ->
                viewModel.updateJacketDetails(
                    id = id,
                    sku = sku,
                    name = name,
                    style = style,
                    size = size,
                    material = material,
                    color = color,
                    quantity = quantity,
                    locationRack = locationRack,
                    isSeasonal = isSeasonal,
                    unitPrice = unitPrice
                )
                isFullEditSheetOpen = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = if (isIndonesian) "Perubahan jaket disimpan" else "Jacket details updated",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    // WhatsApp Single Unit Order Sheet
    if (isOrderDialogOpen && jacket != null) {
        SingleUnitOrderDialog(
            jacket = jacket,
            isIndonesian = isIndonesian,
            onDismiss = { isOrderDialogOpen = false }
        )
    }

    // Delete Confirmation Dialog
    if (isDeleteConfirmOpen && jacket != null) {
        AlertDialog(
            onDismissRequest = { isDeleteConfirmOpen = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = IosSystemRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isIndonesian) "Hapus Jaket?" else "Delete Jacket?",
                    fontWeight = FontWeight.Bold,
                    color = IosLabelPrimary
                )
            },
            text = {
                Text(
                    text = if (isIndonesian) {
                        "Apakah Anda yakin ingin menghapus \"${jacket.name}\" (${jacket.sku}) secara permanen dari database?"
                    } else {
                        "Are you sure you want to permanently remove \"${jacket.name}\" (${jacket.sku}) from the database?"
                    },
                    color = IosLabelSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleteConfirmOpen = false
                        viewModel.deleteJacket(jacket.id)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IosSystemRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_confirm_delete_detail")
                ) {
                    Text(
                        text = if (isIndonesian) "Hapus" else "Delete",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isDeleteConfirmOpen = false },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_cancel_delete_detail")
                ) {
                    Text(
                        text = if (isIndonesian) "Batal" else "Cancel",
                        color = IosLabelSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun HeroJacketImageCard(
    jacket: JacketItem,
    statusBg: Color,
    statusFg: Color,
    statusLabel: String,
    isIndonesian: Boolean,
    onEditPhotoClick: () -> Unit
) {
    val heroShape = RoundedCornerShape(26.dp)
    Surface(
        shape = heroShape,
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .testTag("detail_hero_image_card")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NetworkImageWithFallback(
                imageUrl = jacket.imageUrl,
                contentDescription = jacket.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Soft bottom scrim for legible overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                        )
                    )
            )

            // Status Badge (Top Start)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusBg.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusFg)
                    )
                    Text(
                        text = "$statusLabel • ${jacket.quantity}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusFg
                    )
                }
            }

            // Size Badge (Top End)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.94f),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
            ) {
                Text(
                    text = "Size ${jacket.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosLabelPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Color & Style tag (Bottom Start)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val colorHex = resolveColorDot(jacket.color)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colorHex)
                            .border(1.dp, Color.White, CircleShape)
                    )
                    Text(
                        text = "${jacket.color} • ${jacket.style}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Change Photo Pill Button (Bottom End)
            Surface(
                onClick = onEditPhotoClick,
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.94f),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .heightIn(min = 48.dp)
                    .testTag("btn_detail_change_photo")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = if (isIndonesian) "Ganti Foto" else "Change Photo",
                        tint = IosSystemBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isIndonesian) "Foto" else "Photo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosSystemBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderOverviewCard(
    jacket: JacketItem,
    isIndonesian: Boolean
) {
    val themeSpec = LocalAppThemeSpec.current
    val totalValue = remember(jacket.unitPrice, jacket.quantity) {
        CurrencyUtils.normalizeRupiah(jacket.unitPrice) * jacket.quantity
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = themeSpec.cardBackground,
        border = BorderStroke(1.dp, themeSpec.cardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detail_header_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailBadgeChip(
                    label = jacket.style,
                    containerColor = themeSpec.accentPrimary.copy(alpha = 0.14f),
                    contentColor = themeSpec.accentPrimary
                )
                if (jacket.genderCategory.isNotBlank()) {
                    DetailBadgeChip(
                        label = jacket.genderCategory,
                        containerColor = themeSpec.accentSecondary.copy(alpha = 0.14f),
                        contentColor = themeSpec.accentSecondary
                    )
                }
                if (jacket.isSeasonal) {
                    DetailBadgeChip(
                        label = if (isIndonesian) "Musiman" else "Seasonal",
                        containerColor = IosSystemOrange.copy(alpha = 0.14f),
                        contentColor = IosSystemOrange
                    )
                }
            }

            Text(
                text = jacket.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = themeSpec.labelPrimary,
                lineHeight = 28.sp,
                modifier = Modifier.testTag("detail_jacket_name")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (isIndonesian) "Harga Satuan" else "Unit Price",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeSpec.labelSecondary
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(jacket.unitPrice),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = themeSpec.accentPrimary,
                        modifier = Modifier.testTag("detail_price_display")
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = themeSpec.chipContainer
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (isIndonesian) "Total Nilai Stok" else "Total Stock Value",
                            fontSize = 11.sp,
                            color = themeSpec.labelSecondary
                        )
                        Text(
                            text = CurrencyUtils.formatRupiahCompact(totalValue),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeSpec.labelPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DescriptionMaterialPriceCard(
    jacket: JacketItem,
    displayDescription: String,
    isIndonesian: Boolean,
    isEditing: Boolean,
    editedDescription: String,
    editedMaterial: String,
    editedPriceText: String,
    priceError: String?,
    onToggleEdit: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onQuickPriceSelect: (Double) -> Unit,
    onSave: () -> Unit
) {
    val themeSpec = LocalAppThemeSpec.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = themeSpec.cardBackground,
        border = BorderStroke(1.dp, themeSpec.cardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detail_specs_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(themeSpec.accentPrimary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = themeSpec.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isIndonesian) "Deskripsi, Bahan & Harga" else "Description, Material & Price",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeSpec.labelPrimary
                        )
                        Text(
                            text = if (isIndonesian) "Informasi utama produk jaket" else "Key jacket product fields",
                            fontSize = 12.sp,
                            color = themeSpec.labelSecondary
                        )
                    }
                }

                TextButton(
                    onClick = onToggleEdit,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_toggle_edit_fields")
                ) {
                    Text(
                        text = if (isEditing) {
                            if (isIndonesian) "Batal" else "Cancel"
                        } else {
                            if (isIndonesian) "Ubah" else "Edit"
                        },
                        color = if (isEditing) IosSystemRed else themeSpec.accentPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            HorizontalDivider(color = themeSpec.separator.copy(alpha = 0.5f))

            AnimatedContent(
                targetState = isEditing,
                label = "SpecsEditModeTransition"
            ) { editing ->
                if (!editing) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Description Field Display
                        DetailFieldRow(
                            icon = Icons.Default.Notes,
                            iconTint = IosSystemIndigo,
                            label = if (isIndonesian) {
                                stringResource(R.string.jacket_detail_description_id)
                            } else {
                                stringResource(R.string.jacket_detail_description)
                            },
                            value = displayDescription,
                            valueTestTag = "detail_description_value",
                            isMultiline = true
                        )

                        HorizontalDivider(color = IosSeparator.copy(alpha = 0.4f))

                        // Material Field Display
                        DetailFieldRow(
                            icon = Icons.Default.Layers,
                            iconTint = IosSystemTeal,
                            label = if (isIndonesian) {
                                stringResource(R.string.jacket_detail_material_id)
                            } else {
                                stringResource(R.string.jacket_detail_material)
                            },
                            value = jacket.material,
                            valueTestTag = "detail_material_value"
                        )

                        HorizontalDivider(color = IosSeparator.copy(alpha = 0.4f))

                        // Price Field Display
                        DetailFieldRow(
                            icon = Icons.Default.Sell,
                            iconTint = IosSystemGreen,
                            label = if (isIndonesian) {
                                stringResource(R.string.jacket_detail_price_id)
                            } else {
                                stringResource(R.string.jacket_detail_price)
                            },
                            value = CurrencyUtils.formatRupiah(jacket.unitPrice),
                            valueColor = themeSpec.accentPrimary,
                            valueTestTag = "detail_price_value"
                        )

                        // Quick Edit CTA
                        OutlinedButton(
                            onClick = onToggleEdit,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, themeSpec.accentPrimary.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_inline_edit_specs")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = themeSpec.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isIndonesian) {
                                    "Edit Deskripsi, Bahan & Harga"
                                } else {
                                    "Edit Description, Material & Price"
                                },
                                color = themeSpec.accentPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Description Input
                        OutlinedTextField(
                            value = editedDescription,
                            onValueChange = onDescriptionChange,
                            label = {
                                Text(
                                    if (isIndonesian) {
                                        stringResource(R.string.jacket_detail_description_id)
                                    } else {
                                        stringResource(R.string.jacket_detail_description)
                                    }
                                )
                            },
                            placeholder = {
                                Text(
                                    if (isIndonesian) {
                                        "Tambahkan catatan bahan, potongan, fitur jaket..."
                                    } else {
                                        "Add jacket fit, fabric finish, and key features..."
                                    }
                                )
                            },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("detail_description_input")
                        )

                        // Material Input + Quick Material Chips
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editedMaterial,
                                onValueChange = onMaterialChange,
                                label = {
                                    Text(
                                        if (isIndonesian) {
                                            stringResource(R.string.jacket_detail_material_id)
                                        } else {
                                            stringResource(R.string.jacket_detail_material)
                                        }
                                    )
                                },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = IosSystemTeal
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("detail_material_input")
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(CatalogData.MATERIALS, key = { it.id }) { mat ->
                                    val isSelected = editedMaterial.equals(mat.titleEn, ignoreCase = true) ||
                                        editedMaterial.equals(mat.titleId, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onMaterialChange(mat.titleEn) },
                                        label = {
                                            Text(
                                                text = if (isIndonesian) mat.titleId else mat.titleEn,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        // Price Input + Quick Price Chips
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editedPriceText,
                                onValueChange = onPriceChange,
                                label = {
                                    Text(
                                        if (isIndonesian) {
                                            "${stringResource(R.string.jacket_detail_price_id)} (Rp)"
                                        } else {
                                            "${stringResource(R.string.jacket_detail_price)} (Rp)"
                                        }
                                    )
                                },
                                prefix = {
                                    Text(
                                        text = "Rp ",
                                        fontWeight = FontWeight.Bold,
                                        color = IosSystemBlue
                                    )
                                },
                                supportingText = {
                                    val preview = CurrencyUtils.parseRupiah(editedPriceText, 0.0)
                                    Text(
                                        text = priceError ?: if (preview > 0.0) {
                                            CurrencyUtils.formatRupiah(preview)
                                        } else {
                                            ""
                                        },
                                        color = if (priceError != null) IosSystemRed else IosLabelSecondary
                                    )
                                },
                                isError = priceError != null,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("detail_price_input")
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(CurrencyUtils.QUICK_PRICE_OPTIONS) { quickPrice ->
                                    SuggestionChip(
                                        onClick = { onQuickPriceSelect(quickPrice) },
                                        label = {
                                            Text(
                                                text = CurrencyUtils.formatRupiahCompact(quickPrice),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        // Save Button
                        Button(
                            onClick = onSave,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeSpec.accentPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .testTag("btn_save_detail_fields")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isIndonesian) {
                                    stringResource(R.string.jacket_detail_save_changes_id)
                                } else {
                                    stringResource(R.string.jacket_detail_save_changes)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailFieldRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    valueTestTag: String,
    valueColor: Color = LocalAppThemeSpec.current.labelPrimary,
    isMultiline: Boolean = false
) {
    val themeSpec = LocalAppThemeSpec.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = if (isMultiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeSpec.labelSecondary
            )
            Text(
                text = value,
                fontSize = if (isMultiline) 14.sp else 16.sp,
                fontWeight = if (isMultiline) FontWeight.Normal else FontWeight.Bold,
                color = valueColor,
                lineHeight = if (isMultiline) 21.sp else 22.sp,
                modifier = Modifier.testTag(valueTestTag)
            )
        }
    }
}

@Composable
private fun StockControlCard(
    jacket: JacketItem,
    isIndonesian: Boolean,
    onAdjustQuantity: (Int) -> Unit,
    onRestock: () -> Unit
) {
    val themeSpec = LocalAppThemeSpec.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = themeSpec.cardBackground,
        border = BorderStroke(1.dp, themeSpec.cardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detail_stock_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(IosSystemOrange.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = IosSystemOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isIndonesian) "Stok & Lokasi Gudang" else "Stock & Warehouse Rack",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeSpec.labelPrimary
                        )
                        Text(
                            text = "${jacket.locationRack} • Min ${jacket.minThreshold} unit",
                            fontSize = 12.sp,
                            color = themeSpec.labelSecondary
                        )
                    }
                }

                Text(
                    text = "${jacket.quantity}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeSpec.labelPrimary,
                    modifier = Modifier.testTag("detail_stock_quantity")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { onAdjustQuantity(-1) },
                    enabled = jacket.quantity > 0,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_detail_qty_minus")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = if (isIndonesian) "Kurangi Stok" else "Decrease Stock"
                    )
                }

                FilledTonalIconButton(
                    onClick = { onAdjustQuantity(1) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_detail_qty_plus")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isIndonesian) "Tambah Stok" else "Increase Stock"
                    )
                }

                Button(
                    onClick = onRestock,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeSpec.accentPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_detail_restock")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isIndonesian) "Restok +15" else "Restock +15",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VariantsAndSizesCard(
    currentJacket: JacketItem,
    variants: List<JacketItem>,
    sizeBreakdown: List<Pair<String, Int>>,
    isIndonesian: Boolean,
    onSelectVariant: (JacketItem) -> Unit
) {
    val themeSpec = LocalAppThemeSpec.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = themeSpec.cardBackground,
        border = BorderStroke(1.dp, themeSpec.cardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detail_variants_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isIndonesian) "Varian Warna & Ukuran" else "Color Variants & Sizes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeSpec.labelPrimary
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(variants, key = { it.id }) { variant ->
                    val isCurrent = variant.id == currentJacket.id
                    Surface(
                        onClick = { if (!isCurrent) onSelectVariant(variant) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrent) themeSpec.accentPrimary.copy(alpha = 0.14f) else themeSpec.chipContainer,
                        border = BorderStroke(
                            width = if (isCurrent) 1.5.dp else 1.dp,
                            color = if (isCurrent) themeSpec.accentPrimary else themeSpec.separator.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("detail_variant_${variant.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(resolveColorDot(variant.color))
                                    .border(0.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                            )
                            Text(
                                text = "${variant.color} (${variant.size})",
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) themeSpec.accentPrimary else themeSpec.labelPrimary
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sizeBreakdown.forEach { (sz, qty) ->
                    val isSelectedSize = sz.equals(currentJacket.size, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isSelectedSize -> themeSpec.accentPrimary
                            qty > 0 -> themeSpec.chipContainer
                            else -> themeSpec.chipContainer.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = sz,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelectedSize) Color.White else themeSpec.labelPrimary
                            )
                            Text(
                                text = "$qty",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelectedSize) Color.White.copy(alpha = 0.9f) else themeSpec.labelSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailActionsCard(
    isIndonesian: Boolean,
    onOrderWhatsApp: () -> Unit,
    onFullEdit: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val themeSpec = LocalAppThemeSpec.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = themeSpec.cardBackground,
        border = BorderStroke(1.dp, themeSpec.cardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detail_actions_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOrderWhatsApp,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IosSystemGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_detail_order_wa")
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isIndonesian) "Order Satuan via WhatsApp" else "Single-Unit WhatsApp Order",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onFullEdit,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, themeSpec.separator),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_detail_edit_all")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = themeSpec.labelPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isIndonesian) "Edit Lengkap" else "Full Edit",
                        color = themeSpec.labelPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onDeleteClick,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, IosSystemRed.copy(alpha = 0.45f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IosSystemRed),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_detail_delete")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isIndonesian) "Hapus Jaket" else "Delete Jacket",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailBadgeChip(
    label: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

private fun resolveColorDot(colorName: String): Color {
    val matched = CatalogData.COLORS.firstOrNull {
        it.titleEn.equals(colorName, ignoreCase = true) ||
            it.titleId.equals(colorName, ignoreCase = true)
    }
    return if (matched != null) Color(matched.hexColor) else IosLabelSecondary
}

private fun buildDefaultJacketDescription(jacket: JacketItem, isIndonesian: Boolean): String {
    return if (isIndonesian) {
        "${jacket.name} bergaya ${jacket.style} untuk kategori ${jacket.genderCategory}, dibuat menggunakan bahan ${jacket.material} berkualitas tinggi dengan warna ${jacket.color}. Tersedia ukuran ${jacket.size} di ${jacket.locationRack} untuk kenyamanan harian dan daya tahan jangka panjang."
    } else {
        "${jacket.name} crafted in a ${jacket.style} silhouette (${jacket.genderCategory}), tailored from durable ${jacket.material} in ${jacket.color}. Stocked in size ${jacket.size} at ${jacket.locationRack} for everyday comfort and lasting wear."
    }
}
