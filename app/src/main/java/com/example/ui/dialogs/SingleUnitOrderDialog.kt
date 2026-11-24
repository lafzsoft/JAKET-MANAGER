package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacketItem
import com.example.ui.components.NetworkImageWithFallback
import com.example.util.CurrencyUtils
import com.example.util.WhatsAppShareHelper

/**
 * Modal BottomSheet for formatting and sending a single-unit order (Order Satuan)
 * directly via WhatsApp, featuring SKU, Warna, Size, Nama, Jumlah, Harga,
 * along with buyer details and target WhatsApp phone number.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleUnitOrderDialog(
    jacket: JacketItem,
    isIndonesian: Boolean = true,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var orderQty by remember { mutableIntStateOf(1) }
    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var shippingAddress by remember { mutableStateOf("") }
    var orderNotes by remember { mutableStateOf("") }
    var targetPhone by remember { mutableStateOf("") }

    val totalPrice = remember(jacket.unitPrice, orderQty) { jacket.unitPrice * orderQty }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("single_unit_order_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isIndonesian) "Rincian Order Satuan (WA)" else "Unit Order via WhatsApp",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (isIndonesian) "Kirim format pesanan cepat ke WhatsApp" else "Send fast structured order to WhatsApp",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Highlighted Product Card: SKU, Warna, Size, Nama, Harga
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_jacket_preview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                    ) {
                        NetworkImageWithFallback(
                            imageUrl = jacket.imageUrl,
                            contentDescription = jacket.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = jacket.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Highlights: SKU, Warna, Size
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0F172A)
                            ) {
                                Text(
                                    text = "SKU: ${jacket.sku}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = "Size: ${jacket.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = jacket.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${CurrencyUtils.formatRupiah(jacket.unitPrice)} / unit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stepper: Jumlah Pesanan (Order Quantity)
            Text(
                text = if (isIndonesian) "Jumlah Pesanan Satuan" else "Order Quantity",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$orderQty pcs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Total: ${CurrencyUtils.formatRupiah(totalPrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF047857)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (orderQty > 1) orderQty-- },
                        enabled = orderQty > 1,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurang", tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = { orderQty++ },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Form Inputs: Pembeli, No WA, Alamat, Catatan
            Text(
                text = if (isIndonesian) "Informasi Pembeli / Customer (Opsional)" else "Customer Information (Optional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = buyerName,
                onValueChange = { buyerName = it },
                label = { Text(if (isIndonesian) "Nama Pembeli / Pemesan" else "Buyer Name") },
                placeholder = { Text("Contoh: Kak Andre") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_order_buyer_name"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = targetPhone,
                onValueChange = { targetPhone = it },
                label = { Text(if (isIndonesian) "No. WhatsApp Tujuan (Admin / Supplier / Buyer)" else "Target WhatsApp Number") },
                placeholder = { Text("Contoh: 08123456789 atau kosongkan untuk pilih chat") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_order_phone"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = shippingAddress,
                onValueChange = { shippingAddress = it },
                label = { Text(if (isIndonesian) "Alamat Kirim / Ekspedisi" else "Shipping Address") },
                placeholder = { Text("Contoh: Jl. Sudirman No. 45, Bandung (J&T)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_order_address"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = orderNotes,
                onValueChange = { orderNotes = it },
                label = { Text(if (isIndonesian) "Catatan Tambahan (Opsional)" else "Special Notes") },
                placeholder = { Text("Contoh: Minta dipacking plastik ganda") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_order_notes"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Live Preview of Order Message
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isIndonesian) "Format Teks yang Akan Terkirim ke WA:" else "Text Message to be Sent:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val previewText = remember(jacket, buyerName, targetPhone, orderQty, orderNotes, shippingAddress) {
                        WhatsAppShareHelper.buildSingleUnitOrderText(
                            jacket = jacket,
                            buyerName = buyerName,
                            buyerPhone = targetPhone,
                            orderQty = orderQty,
                            notes = orderNotes,
                            shippingAddress = shippingAddress,
                            isIndonesian = isIndonesian
                        )
                    }

                    Text(
                        text = previewText,
                        fontSize = 11.sp,
                        color = Color(0xFF14532D),
                        lineHeight = 16.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isIndonesian) "Batal" else "Cancel")
                }

                Button(
                    onClick = {
                        WhatsAppShareHelper.sendSingleUnitOrderToWhatsApp(
                            context = context,
                            jacket = jacket,
                            buyerName = buyerName,
                            buyerPhone = buyerPhone,
                            orderQty = orderQty,
                            notes = orderNotes,
                            shippingAddress = shippingAddress,
                            targetWhatsAppNumber = targetPhone,
                            isIndonesian = isIndonesian
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("btn_confirm_send_order_wa"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isIndonesian) "Kirim Order ke WA" else "Send Order to WA",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
