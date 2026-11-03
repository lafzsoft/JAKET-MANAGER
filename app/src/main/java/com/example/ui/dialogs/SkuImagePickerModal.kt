package com.example.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CatalogData
import com.example.model.JacketItem
import com.example.ui.components.NetworkImageWithFallback

data class PresetImageOption(
    val id: String,
    val nameId: String,
    val nameEn: String,
    val imageUrl: String
)

val CURATED_PRESETS = listOf(
    PresetImageOption(
        id = "bomber",
        nameId = "Bomber Hitam",
        nameEn = "Black Bomber",
        imageUrl = "https://mediadirhub.com/vibe/black-leather-bomber-jacket-with-ribbed-cuffs-and-zipper-front.png"
    ),
    PresetImageOption(
        id = "trench",
        nameId = "Trench Beige",
        nameEn = "Beige Trench",
        imageUrl = "https://mediadirhub.com/vibe/beige-cotton-trench-coat-with-double-breasted-buttons-and-belt.png"
    ),
    PresetImageOption(
        id = "denim",
        nameId = "Denim Klasik",
        nameEn = "Classic Denim",
        imageUrl = "https://mediadirhub.com/vibe/medium-blue-denim-jacket-with-patch-pockets-and-button-front.png"
    ),
    PresetImageOption(
        id = "puffer",
        nameId = "Puffer Tebal",
        nameEn = "Quilted Puffer",
        imageUrl = "https://mediadirhub.com/vibe/dark-gray-quilted-puffer-jacket-with-hood-and-zip-closure.png"
    ),
    PresetImageOption(
        id = "beige_casual",
        nameId = "Beige Lembut",
        nameEn = "Soft Beige",
        imageUrl = "https://mediadirhub.com/vibe/light-beige-jacket-with-soft-texture-and-neutral-tone.png"
    ),
    PresetImageOption(
        id = "olive_parka",
        nameId = "Parka Zaitun",
        nameEn = "Olive Parka",
        imageUrl = "https://mediadirhub.com/vibe/olive-green-jacket-with-matte-finish-and-structured-silhouette.png"
    ),
    PresetImageOption(
        id = "navy_suit",
        nameId = "Navy Elegan",
        nameEn = "Deep Navy",
        imageUrl = "https://mediadirhub.com/vibe/deep-navy-blue-jacket-with-clean-lines-and-no-patterns.png"
    ),
    PresetImageOption(
        id = "leather_brown",
        nameId = "Kulit Coklat",
        nameEn = "Brown Leather",
        imageUrl = "https://mediadirhub.com/vibe/close-up-of-smooth-brown-leather-with-natural-grain-pattern.png"
    ),
    PresetImageOption(
        id = "hero_showcase",
        nameId = "Showcase Utama",
        nameEn = "Showcase Hero",
        imageUrl = CatalogData.HERO_IMAGE_URL
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkuImagePickerModal(
    jacket: JacketItem,
    isIndonesian: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (newImageUrl: String, updateAllWithSameSku: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var selectedImageUrl by remember(jacket.imageUrl) { mutableStateOf(jacket.imageUrl) }
    var customUrlInput by remember { mutableStateOf("") }
    var applyToAllWithSameSku by remember { mutableStateOf(true) }
    var showUrlInputCard by remember { mutableStateOf(false) }

    // Google Play Policy compliant Android Photo Picker (zero broad storage permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUrl = uri.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("modal_sku_image_picker")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header: Title & SKU Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isIndonesian) "Atur Gambar SKU Produk" else "Set Product SKU Image",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = if (isIndonesian) "Kustomisasi visual produk untuk kode SKU ini" else "Customize visual asset for this SKU code",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_close_image_modal")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Preview Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sku_image_preview")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                    ) {
                        NetworkImageWithFallback(
                            imageUrl = selectedImageUrl,
                            contentDescription = jacket.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = jacket.sku,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = jacket.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${jacket.genderCategory} • ${jacket.style} • ${jacket.size}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )

                        Text(
                            text = if (isIndonesian) "Stok: ${jacket.quantity} unit di ${jacket.locationRack}" else "Stock: ${jacket.quantity} units at ${jacket.locationRack}",
                            fontSize = 11.sp,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Option 1: Android Photo Picker
            Text(
                text = if (isIndonesian) "1. Pilih Foto dari Galeri Perangkat" else "1. Pick Photo from Device Gallery",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_pick_photo_gallery"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isIndonesian) "Buka Galeri Foto (Photo Picker)" else "Open Photo Gallery",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Option 2: Curated Catalog Presets
            Text(
                text = if (isIndonesian) "2. Atau Pilih dari Preset Katalog Jaket" else "2. Or Choose from Preset Jacket Catalog",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CURATED_PRESETS, key = { it.id }) { preset ->
                    val isSelected = selectedImageUrl == preset.imageUrl
                    Column(
                        modifier = Modifier
                            .width(86.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedImageUrl = preset.imageUrl
                            }
                            .padding(6.dp)
                            .testTag("preset_image_${preset.id}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        ) {
                            NetworkImageWithFallback(
                                imageUrl = preset.imageUrl,
                                contentDescription = preset.nameEn,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isIndonesian) preset.nameId else preset.nameEn,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Option 3: Custom Web URL input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showUrlInputCard = !showUrlInputCard }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isIndonesian) "3. Masukkan Tautan URL Gambar Web" else "3. Enter Web Image URL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Icon(
                    imageVector = if (showUrlInputCard) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF64748B)
                )
            }

            if (showUrlInputCard) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_image_url"),
                        placeholder = {
                            Text("https://domain.com/gambar-jaket.png", fontSize = 12.sp)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (customUrlInput.isNotBlank()) {
                                    selectedImageUrl = customUrlInput.trim()
                                }
                                focusManager.clearFocus()
                            }
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (customUrlInput.isNotBlank()) {
                                selectedImageUrl = customUrlInput.trim()
                            }
                            focusManager.clearFocus()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                    ) {
                        Text(if (isIndonesian) "Pakai" else "Apply", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Checkbox: Apply to all jackets sharing this SKU
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { applyToAllWithSameSku = !applyToAllWithSameSku }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = applyToAllWithSameSku,
                        onCheckedChange = { applyToAllWithSameSku = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isIndonesian) "Terapkan ke seluruh jaket dengan SKU ${jacket.sku}" else "Apply to all jackets with SKU ${jacket.sku}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isIndonesian) "Menyelaraskan foto katalog untuk semua item dengan kode SKU yang sama" else "Keep catalog image uniform for every item with identical SKU",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF1E293B), Color(0xFF334155))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569))
                ) {
                    Text(if (isIndonesian) "Batal" else "Cancel", fontWeight = FontWeight.Medium)
                }

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(gradientBrush)
                        .clickable {
                            onSave(selectedImageUrl, applyToAllWithSameSku)
                        }
                        .testTag("btn_save_sku_image"),
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
                            text = if (isIndonesian) "Simpan Gambar SKU" else "Save SKU Image",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
