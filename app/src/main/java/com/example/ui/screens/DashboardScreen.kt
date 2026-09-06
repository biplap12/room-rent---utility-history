package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.RoomSelectorBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.SimpleBarChart
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun DashboardScreen(
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity>,
    records: List<MonthlyRecordEntity>,
    latestRecord: MonthlyRecordEntity?,
    currencySymbol: String,
    onSelectRoom: (RoomEntity) -> Unit,
    onManageRooms: () -> Unit,
    onAddRecord: () -> Unit,
    onViewHistory: () -> Unit,
    onOpenRecordDetail: (MonthlyRecordEntity) -> Unit,
    onExportPdf: (MonthlyRecordEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Historical electricity and water usage series for mini charts
    val electricityHistory = remember(records) {
        records.take(6).reversed().map { it.billingMonth to it.electricityUnits }
    }
    val waterHistory = remember(records) {
        records.take(6).reversed().map { it.billingMonth to it.waterUnits }
    }

    val totalHistoryElecUnits = remember(records) {
        records.sumOf { it.electricityUnits }
    }
    val totalHistoryWaterUnits = remember(records) {
        records.sumOf { it.waterUnits }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // Room Switcher
        RoomSelectorBar(
            activeRoom = activeRoom,
            allRooms = allRooms,
            onSelectRoom = onSelectRoom,
            onManageRooms = onManageRooms
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (latestRecord == null) {
            // Empty State
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Records for ${activeRoom?.name ?: "this room"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Start by adding your first monthly bill with electricity and water meter readings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onAddRecord,
                        modifier = Modifier.testTag("add_first_record_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Monthly Record")
                    }
                }
            }
        } else {
            // MAIN CURRENT BILL CARD (Geometric Balance high-impact display)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onOpenRecordDetail(latestRecord) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT BILL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = latestRecord.billingMonth,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        PaymentStatusBadge(status = latestRecord.paymentStatus)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Total Bill Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Total Amount",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormatUtils.formatMoney(latestRecord.totalAmount, currencySymbol),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (latestRecord.remainingAmount > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Due / Remaining",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusUnpaid
                                )
                                Text(
                                    text = FormatUtils.formatMoney(latestRecord.remainingAmount, currencySymbol),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusUnpaid
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Paid in Full",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusPaid
                                )
                                Text(
                                    text = FormatUtils.formatMoney(latestRecord.amountPaid, currencySymbol),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPaid
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Itemized breakdown rows
                    BillBreakdownRow(
                        label = "Room Rent",
                        calculation = "",
                        amount = FormatUtils.formatMoney(latestRecord.roomRent, currencySymbol),
                        icon = Icons.Default.Bed,
                        accentColor = RentAccent
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    BillBreakdownRow(
                        label = "Electricity",
                        calculation = "${FormatUtils.formatUnits(latestRecord.electricityUnits)} units × ${FormatUtils.formatMoney(latestRecord.electricityRate, currencySymbol)}",
                        amount = FormatUtils.formatMoney(latestRecord.electricityCost, currencySymbol),
                        icon = Icons.Default.Bolt,
                        accentColor = ElectricityAccent,
                        details = "Meter: ${FormatUtils.formatUnits(latestRecord.previousElectricityReading)} → ${FormatUtils.formatUnits(latestRecord.currentElectricityReading)}"
                    )

                    if (latestRecord.waterUnits > 0 || latestRecord.waterCost > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        BillBreakdownRow(
                            label = "Water",
                            calculation = if (latestRecord.waterUnits > 0 && latestRecord.waterRate > 0) {
                                "${FormatUtils.formatUnits(latestRecord.waterUnits)} units × ${FormatUtils.formatMoney(latestRecord.waterRate, currencySymbol)}"
                            } else "",
                            amount = FormatUtils.formatMoney(latestRecord.waterCost, currencySymbol),
                            icon = Icons.Default.WaterDrop,
                            accentColor = WaterAccent,
                            details = if (latestRecord.waterUnits > 0) "Meter: ${FormatUtils.formatUnits(latestRecord.previousWaterReading)} → ${FormatUtils.formatUnits(latestRecord.currentWaterReading)}" else null
                        )
                    }

                    if (latestRecord.wasteCharge > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        BillBreakdownRow(
                            label = "Waste / Minimum",
                            calculation = "",
                            amount = FormatUtils.formatMoney(latestRecord.wasteCharge, currencySymbol),
                            icon = Icons.Default.Delete,
                            accentColor = WasteAccent
                        )
                    }

                    if (latestRecord.otherCharges > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        BillBreakdownRow(
                            label = "Other Charges",
                            calculation = "",
                            amount = FormatUtils.formatMoney(latestRecord.otherCharges, currencySymbol),
                            icon = Icons.Default.Receipt,
                            accentColor = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (latestRecord.discount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        BillBreakdownRow(
                            label = "Discount",
                            calculation = "",
                            amount = "- " + FormatUtils.formatMoney(latestRecord.discount, currencySymbol),
                            icon = Icons.Default.LocalOffer,
                            accentColor = StatusPaid
                        )
                    }

                    if (latestRecord.paymentDate.isNotBlank() || latestRecord.amountPaid > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (latestRecord.paymentDate.isNotBlank()) "Last Payment: ${latestRecord.paymentDate}" else "Paid: ${FormatUtils.formatMoney(latestRecord.amountPaid, currencySymbol)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Amount Paid: ${FormatUtils.formatMoney(latestRecord.amountPaid, currencySymbol)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons inside Bill Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onExportPdf(latestRecord) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_button"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF Bill", fontSize = 12.sp)
                        }
                        Button(
                            onClick = onAddRecord,
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("add_record_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Record", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 6: ELECTRICITY TRACKING
            SectionHeader(
                title = "Electricity Tracking",
                subtitle = "Meter readings and monthly usage"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricityAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = ElectricityAccent, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "This Month's Electricity",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${FormatUtils.formatUnits(latestRecord.electricityUnits)} units used",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = FormatUtils.formatMoney(latestRecord.electricityCost, currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricityAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Meter Reading Comparison Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)), RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MeterColumn(
                            label = "Last Meter",
                            value = FormatUtils.formatUnits(latestRecord.previousElectricityReading)
                        )
                        MeterColumn(
                            label = "Current Meter",
                            value = FormatUtils.formatUnits(latestRecord.currentElectricityReading)
                        )
                        MeterColumn(
                            label = "Rate / Unit",
                            value = FormatUtils.formatMoney(latestRecord.electricityRate, currencySymbol)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Historical Total: ${FormatUtils.formatUnits(totalHistoryElecUnits)} units",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SimpleBarChart(
                        data = electricityHistory,
                        barColor = ElectricityAccent,
                        unitLabel = "units"
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 7: WATER TRACKING
            SectionHeader(
                title = "Water Tracking",
                subtitle = "Meter readings and monthly consumption"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(WaterAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WaterAccent, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "This Month's Water",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${FormatUtils.formatUnits(latestRecord.waterUnits)} units used",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = FormatUtils.formatMoney(latestRecord.waterCost, currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaterAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Water Meter Reading Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)), RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MeterColumn(
                            label = "Last Meter",
                            value = FormatUtils.formatUnits(latestRecord.previousWaterReading)
                        )
                        MeterColumn(
                            label = "Current Meter",
                            value = FormatUtils.formatUnits(latestRecord.currentWaterReading)
                        )
                        MeterColumn(
                            label = "Rate / Unit",
                            value = FormatUtils.formatMoney(latestRecord.waterRate, currencySymbol)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Historical Total: ${FormatUtils.formatUnits(totalHistoryWaterUnits)} units",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SimpleBarChart(
                        data = waterHistory,
                        barColor = WaterAccent,
                        unitLabel = "units"
                    )
                }
            }
        }
    }
}

@Composable
private fun BillBreakdownRow(
    label: String,
    calculation: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    details: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (calculation.isNotBlank()) {
                    Text(
                        text = calculation,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (details != null) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MeterColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
