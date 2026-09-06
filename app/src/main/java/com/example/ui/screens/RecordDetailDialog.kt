package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun RecordDetailDialog(
    record: MonthlyRecordEntity,
    room: RoomEntity?,
    currencySymbol: String,
    onEdit: (MonthlyRecordEntity) -> Unit,
    onDuplicate: (MonthlyRecordEntity) -> Unit,
    onDelete: (MonthlyRecordEntity) -> Unit,
    onExportPdf: (MonthlyRecordEntity) -> Unit,
    onClose: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BILL STATEMENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = record.billingMonth,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = room?.name ?: "Room ${record.roomId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(vertical = 12.dp)
                ) {
                    // Status Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Payment Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (record.paymentDate.isNotBlank()) "Paid on: ${record.paymentDate}" else "No payment date recorded",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        PaymentStatusBadge(status = record.paymentStatus)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Itemized Details Table
                    Text(text = "Itemized Charges", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DetailRow(
                                title = "Room Rent",
                                subtitle = "Fixed monthly base rent",
                                value = FormatUtils.formatMoney(record.roomRent, currencySymbol)
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                            DetailRow(
                                title = "Electricity",
                                subtitle = "Meter: ${FormatUtils.formatUnits(record.previousElectricityReading)} → ${FormatUtils.formatUnits(record.currentElectricityReading)}\n(${FormatUtils.formatUnits(record.electricityUnits)} units × ${FormatUtils.formatMoney(record.electricityRate, currencySymbol)})",
                                value = FormatUtils.formatMoney(record.electricityCost, currencySymbol),
                                valueColor = ElectricityAccent
                            )

                            if (record.waterUnits > 0 || record.waterCost > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                DetailRow(
                                    title = "Water",
                                    subtitle = "Meter: ${FormatUtils.formatUnits(record.previousWaterReading)} → ${FormatUtils.formatUnits(record.currentWaterReading)}\n(${FormatUtils.formatUnits(record.waterUnits)} units × ${FormatUtils.formatMoney(record.waterRate, currencySymbol)})",
                                    value = FormatUtils.formatMoney(record.waterCost, currencySymbol),
                                    valueColor = WaterAccent
                                )
                            }

                            if (record.wasteCharge > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                DetailRow(
                                    title = "Waste / Minimum Charge",
                                    subtitle = "Municipal / sanitation fee",
                                    value = FormatUtils.formatMoney(record.wasteCharge, currencySymbol)
                                )
                            }

                            if (record.otherCharges > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                DetailRow(
                                    title = "Other Charges",
                                    subtitle = "Additional expenses",
                                    value = FormatUtils.formatMoney(record.otherCharges, currencySymbol)
                                )
                            }

                            if (record.discount > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                DetailRow(
                                    title = "Discount",
                                    subtitle = "Deductions applied",
                                    value = "- " + FormatUtils.formatMoney(record.discount, currencySymbol),
                                    valueColor = StatusPaid
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Totals
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Total Bill:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = FormatUtils.formatMoney(record.totalAmount, currencySymbol),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Amount Paid:", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = FormatUtils.formatMoney(record.amountPaid, currencySymbol),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StatusPaid
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Remaining Due:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = FormatUtils.formatMoney(record.remainingAmount, currencySymbol),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.remainingAmount > 0) StatusUnpaid else StatusPaid
                                )
                            }
                        }
                    }

                    if (record.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "Notes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = record.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onExportPdf(record) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_pdf_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onDuplicate(record)
                            onClose()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_duplicate_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Duplicate", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onEdit(record)
                            onClose()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_edit_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("detail_delete_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusUnpaid)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusUnpaid) },
            title = { Text("Delete This Record?") },
            text = { Text("Are you sure you want to permanently delete the bill for ${record.billingMonth}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(record)
                        showDeleteConfirm = false
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusUnpaid),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    title: String,
    subtitle: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
