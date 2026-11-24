package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KasbonItem
import com.example.util.CurrencyUtils

@Composable
fun RecordRepaymentDialog(
    kasbon: KasbonItem,
    isIndonesian: Boolean,
    onDismiss: () -> Unit,
    onConfirmPayment: (paymentAmount: Double) -> Unit
) {
    val remaining = kasbon.remainingAmount
    var paymentText by remember { mutableStateOf(Math.round(remaining).toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentInputAmount = paymentText.toDoubleOrNull() ?: 0.0
    val newRemaining = (remaining - currentInputAmount).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isIndonesian) "Bayar / Cicil Kasbon" else "Record Repayment",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = kasbon.employeeName + " (" + kasbon.role + ")",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Summary Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isIndonesian) "Total Pinjaman:" else "Total Advance:", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(kasbon.amountFormatted, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isIndonesian) "Sudah Terbayar:" else "Already Paid:", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(kasbon.paidAmountFormatted, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isIndonesian) "Sisa Tagihan:" else "Remaining Balance:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text(kasbon.remainingAmountFormatted, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))
                        }
                    }
                }

                // Payment input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isIndonesian) "Nominal Pembayaran (Rp) *" else "Payment Amount (Rp) *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    OutlinedTextField(
                        value = paymentText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                paymentText = input
                            }
                        },
                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_repayment_amount")
                    )

                    // Quick buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssistChip(
                            onClick = { paymentText = Math.round(remaining).toString() },
                            label = { Text(if (isIndonesian) "Bayar Lunas" else "Pay Full", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFDCFCE7), labelColor = Color(0xFF166534)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        )

                        if (remaining >= 100_000) {
                            AssistChip(
                                onClick = { paymentText = "100000" },
                                label = { Text("100rb", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (remaining >= 250_000) {
                            AssistChip(
                                onClick = { paymentText = "250000" },
                                label = { Text("250rb", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Sisa Setelah Pembayaran
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (newRemaining == 0.0) Color(0xFFECFDF5) else Color(0xFFF1F5F9))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isIndonesian) "Sisa Setelah Bayar:" else "Balance After:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = if (newRemaining == 0.0) {
                            if (isIndonesian) "LUNAS (Rp 0)" else "PAID IN FULL"
                        } else {
                            CurrencyUtils.formatRupiah(newRemaining)
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (newRemaining == 0.0) Color(0xFF16A34A) else Color(0xFF2563EB)
                    )
                }

                errorMessage?.let { err ->
                    Text(text = err, color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pAmt = paymentText.toDoubleOrNull() ?: 0.0
                    if (pAmt <= 0) {
                        errorMessage = if (isIndonesian) "Nominal bayar harus lebih dari Rp 0." else "Payment must be greater than 0."
                        return@Button
                    }
                    if (pAmt > remaining) {
                        errorMessage = if (isIndonesian) "Nominal melebihi sisa tagihan (${CurrencyUtils.formatRupiah(remaining)})." else "Amount exceeds remaining balance."
                        return@Button
                    }
                    onConfirmPayment(pAmt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_repayment")
            ) {
                Text(if (isIndonesian) "Konfirmasi Bayar" else "Confirm Payment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isIndonesian) "Batal" else "Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
