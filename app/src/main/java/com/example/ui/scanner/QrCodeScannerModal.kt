package com.example.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import com.example.ui.components.NetworkImageWithFallback
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * CameraX and ML Kit powered QR Code & Barcode Scanner Modal.
 * Scans jacket product labels to instantly identify existing items or pre-fill new jacket additions.
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeScannerModal(
    allJackets: List<JacketItem>,
    isIndonesian: Boolean,
    onDismiss: () -> Unit,
    onJacketIdentified: (JacketItem) -> Unit,
    onAddNewWithSku: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    // Permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Torch / Flashlight state
    var isTorchEnabled by remember { mutableStateOf(false) }
    var cameraControlInstance by remember { mutableStateOf<Camera?>(null) }

    // Scan result state
    var detectedCode by remember { mutableStateOf<String?>(null) }
    var matchedJacket by remember { mutableStateOf<JacketItem?>(null) }
    var isProcessingScan by remember { mutableStateOf(false) }

    // Manual test / simulation input
    var manualInputText by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    // Scanning laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_beam"
    )

    // Helper function to resolve scanned barcode content
    fun handleScannedCode(rawCode: String) {
        if (isProcessingScan) return
        val cleanCode = rawCode.trim()
        if (cleanCode.isEmpty()) return

        isProcessingScan = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        detectedCode = cleanCode

        // Search in current inventory by SKU, ID, or QR payload
        val found = allJackets.firstOrNull { jacket ->
            jacket.sku.equals(cleanCode, ignoreCase = true) ||
            jacket.id.equals(cleanCode, ignoreCase = true) ||
            cleanCode.contains(jacket.sku, ignoreCase = true) ||
            jacket.name.equals(cleanCode, ignoreCase = true)
        }
        matchedJacket = found
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("qr_scanner_modal")
        ) {
            // Camera Preview View or Permission Request
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val cameraExecutor = Executors.newSingleThreadExecutor()

                        val barcodeScanner = BarcodeScanning.getClient(
                            BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(
                                    Barcode.FORMAT_QR_CODE,
                                    Barcode.FORMAT_CODE_128,
                                    Barcode.FORMAT_CODE_39,
                                    Barcode.FORMAT_EAN_13,
                                    Barcode.FORMAT_UPC_A
                                )
                                .build()
                        )

                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null && !isProcessingScan) {
                                        val inputImage = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees
                                        )
                                        barcodeScanner.process(inputImage)
                                            .addOnSuccessListener { barcodes ->
                                                for (barcode in barcodes) {
                                                    val raw = barcode.rawValue
                                                    if (!raw.isNullOrBlank()) {
                                                        handleScannedCode(raw)
                                                        break
                                                    }
                                                }
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        imageProxy.close()
                                    }
                                }

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.unbindAll()
                                val boundCamera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                                cameraControlInstance = boundCamera
                            } catch (_: Exception) {
                                // Fallback handled gracefully in UI
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Viewfinder Darkened Overlay with Cutout Reticle & Laser
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val boxSize = size.minDimension * 0.70f
                    val left = (size.width - boxSize) / 2f
                    val top = (size.height - boxSize) / 2f - 40.dp.toPx()

                    // Semi-transparent background
                    drawRect(
                        color = Color.Black.copy(alpha = 0.55f),
                        size = size
                    )

                    // Transparent cutout window for viewfinder
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(left, top),
                        size = Size(boxSize, boxSize),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        blendMode = BlendMode.Clear
                    )

                    // Viewfinder border
                    drawRoundRect(
                        color = Color(0xFF38BDF8).copy(alpha = 0.8f),
                        topLeft = Offset(left, top),
                        size = Size(boxSize, boxSize),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Animated laser scan beam (sweeping vertically inside viewfinder)
                    if (!isProcessingScan) {
                        val laserY = top + (boxSize * laserOffsetY)
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF38BDF8),
                                    Color(0xFF00E5FF),
                                    Color(0xFF38BDF8),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(left + 16.dp.toPx(), laserY),
                            end = Offset(left + boxSize - 16.dp.toPx(), laserY),
                            strokeWidth = 3.5.dp.toPx()
                        )
                    }
                }
            } else {
                // Camera Permission Denied / Rationale View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("camera_permission_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (isIndonesian) "Izin Kamera Diperlukan" else "Camera Permission Required",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isIndonesian)
                                    "Aplikasi memerlukan akses kamera untuk memindai kode QR dan Barcode pada label jaket secara instan."
                                else
                                    "The app requires camera access to scan QR codes and Barcodes on jacket product labels instantly.",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_grant_camera_permission")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isIndonesian) "Beri Izin Kamera" else "Grant Camera Access",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = { showManualInput = true },
                                modifier = Modifier.testTag("btn_use_manual_input_fallback")
                            ) {
                                Text(
                                    text = if (isIndonesian) "Gunakan Input SKU Manual" else "Use Manual SKU Input",
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }
            }

            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("btn_close_scanner")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // Title pill
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isIndonesian) "Scan Label Jaket" else "Scan Jacket Label",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Actions: Torch and Manual Input toggle
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasCameraPermission) {
                        IconButton(
                            onClick = {
                                val nextTorch = !isTorchEnabled
                                cameraControlInstance?.cameraControl?.enableTorch(nextTorch)
                                isTorchEnabled = nextTorch
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isTorchEnabled) Color(0xFFEAB308) else Color.Black.copy(alpha = 0.5f))
                                .testTag("btn_toggle_torch")
                        ) {
                            Icon(
                                imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Torch",
                                tint = if (isTorchEnabled) Color.Black else Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = { showManualInput = !showManualInput },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (showManualInput) Color(0xFF2563EB) else Color.Black.copy(alpha = 0.5f))
                            .testTag("btn_toggle_manual_sku_input")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Manual Input",
                            tint = Color.White
                        )
                    }
                }
            }

            // Target Guide Instruction Text
            if (!isProcessingScan && !showManualInput) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .offset(y = 150.dp)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = if (isIndonesian)
                                "Arahkan kotak ke kode QR atau Barcode label jaket"
                            else
                                "Align frame with jacket label QR code or barcode",
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Quick Preset / Manual Input Panel (Always visible at bottom when toggled or for fast testing)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                // Manual SKU testing & preset chips row (allows direct emulation and instant testing)
                AnimatedVisibility(
                    visible = showManualInput,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("manual_sku_scanner_panel")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isIndonesian) "Input / Simulasi Barcode Label" else "Manual Barcode / SKU Input",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8)
                                )
                                IconButton(
                                    onClick = { showManualInput = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = manualInputText,
                                    onValueChange = { manualInputText = it },
                                    placeholder = {
                                        Text("e.g. JKT-BMB-BLK-L", fontSize = 13.sp, color = Color(0xFF64748B))
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF475569),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF1E293B),
                                        unfocusedContainerColor = Color(0xFF1E293B)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_manual_sku_scanner")
                                )

                                Button(
                                    onClick = {
                                        if (manualInputText.isNotBlank()) {
                                            handleScannedCode(manualInputText)
                                        }
                                    },
                                    enabled = manualInputText.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("btn_simulate_scan")
                                ) {
                                    Text(if (isIndonesian) "Cek" else "Check", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (isIndonesian) "Label Contoh Gudang:" else "Sample Label Codes:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Sample preset chips for existing and new jackets
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val sampleSkus = allJackets.map { it.sku }.filter { it.isNotBlank() }.take(4) +
                                    listOf("JKT-NEW-WHT-XL")
                                items(sampleSkus) { sample ->
                                    val isKnown = allJackets.any { it.sku.equals(sample, ignoreCase = true) }
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            manualInputText = sample
                                            handleScannedCode(sample)
                                        },
                                        label = {
                                            Text(
                                                text = if (isKnown) sample else "$sample (+)",
                                                fontSize = 11.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (isKnown) Icons.Default.Checkroom else Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color(0xFF1E293B),
                                            labelColor = if (isKnown) Color(0xFF93C5FD) else Color(0xFFFDE047),
                                            iconColor = if (isKnown) Color(0xFF93C5FD) else Color(0xFFFDE047)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = false,
                                            borderColor = Color(0xFF334155)
                                        ),
                                        modifier = Modifier.testTag("chip_sample_sku_$sample")
                                    )
                                }
                            }
                        }
                    }
                }

                // Scan Result Bottom Card (Appears whenever a barcode is scanned or simulated)
                AnimatedVisibility(
                    visible = detectedCode != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    detectedCode?.let { rawCode ->
                        val jacket = matchedJacket
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (jacket != null) Color(0xFF0F172A) else Color(0xFF1C1917)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                if (jacket != null) Color(0xFF10B981) else Color(0xFFF59E0B)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("scan_result_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                // Status Header Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (jacket != null)
                                                        Color(0xFF10B981).copy(alpha = 0.2f)
                                                    else
                                                        Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (jacket != null) Icons.Default.CheckCircle else Icons.Default.Info,
                                                contentDescription = null,
                                                tint = if (jacket != null) Color(0xFF34D399) else Color(0xFFFBBF24),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Text(
                                            text = if (jacket != null) {
                                                if (isIndonesian) "Jaket Teridentifikasi!" else "Jacket Identified!"
                                            } else {
                                                if (isIndonesian) "Label Baru Terdeteksi" else "New Label Scanned"
                                            },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Retake / Scan Again small button
                                    TextButton(
                                        onClick = {
                                            detectedCode = null
                                            matchedJacket = null
                                            isProcessingScan = false
                                        },
                                        modifier = Modifier.testTag("btn_scan_again")
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isIndonesian) "Scan Ulang" else "Scan Again",
                                            fontSize = 12.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (jacket != null) {
                                    // MATCHED JACKET PRESENTATION
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Product Thumbnail
                                        NetworkImageWithFallback(
                                            imageUrl = jacket.imageUrl,
                                            contentDescription = jacket.name,
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = jacket.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "SKU: ${jacket.sku} • ${jacket.style}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8),
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Stock count badge
                                                Surface(
                                                    color = when (jacket.status) {
                                                        StockStatusLevel.IN_STOCK -> Color(0xFF065F46)
                                                        StockStatusLevel.LOW_STOCK -> Color(0xFF92400E)
                                                        StockStatusLevel.OUT_OF_STOCK -> Color(0xFF991B1B)
                                                    },
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "${jacket.quantity} unit (${jacket.size})",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }

                                                Text(
                                                    text = jacket.locationRack,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFCBD5E1)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Action: Open Details Modal
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onJacketIdentified(jacket)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_open_identified_jacket_details")
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isIndonesian) "Buka Rincian Produk" else "View Product Details",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    // UNKNOWN / NEW SKU PRESENTATION
                                    Surface(
                                        color = Color(0xFF292524),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = if (isIndonesian) "Kode Label Terbaca:" else "Scanned Label Code:",
                                                fontSize = 11.sp,
                                                color = Color(0xFFA8A29E)
                                            )
                                            Text(
                                                text = rawCode,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFDE047)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (isIndonesian)
                                                    "Label ini belum ada di inventaris gudang. Anda dapat langsung menambahkannya sebagai jaket baru."
                                                else
                                                    "This label is not currently in warehouse inventory. You can add it as a new jacket right away.",
                                                fontSize = 12.sp,
                                                color = Color(0xFFD6D3D1)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Action: Add new jacket with scanned SKU
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onAddNewWithSku(rawCode)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_add_new_jacket_with_scanned_sku")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isIndonesian) "+ Tambah Jaket Baru dengan SKU Ini" else "+ Add New Jacket with this SKU",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
