////package com.example.ui.screens
////
////import android.content.Context
////import androidx.compose.animation.AnimatedVisibility
////import androidx.compose.foundation.BorderStroke
////import androidx.compose.foundation.background
////import androidx.compose.foundation.border
////import androidx.compose.foundation.clickable
////import androidx.compose.foundation.layout.*
////import androidx.compose.foundation.rememberScrollState
////import androidx.compose.foundation.shape.CircleShape
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.foundation.verticalScroll
////import androidx.compose.material.icons.Icons
////import androidx.compose.material.icons.filled.*
////import androidx.compose.material3.*
////import androidx.compose.runtime.*
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.draw.clip
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.platform.LocalContext
////import androidx.compose.ui.platform.testTag
////import androidx.compose.ui.text.font.FontWeight
////import androidx.compose.ui.text.style.TextAlign
////import androidx.compose.ui.unit.dp
////import androidx.compose.ui.unit.sp
////import com.example.data.MonthlyRecordEntity
////import com.example.data.RoomEntity
////import com.example.ui.components.PaymentStatusBadge
////import com.example.ui.components.RoomSelectorBar
////import com.example.ui.components.SectionHeader
////import com.example.ui.components.SimpleBarChart
////import com.example.ui.theme.*
////import com.example.util.FormatUtils
////
////@Composable
////fun DashboardScreen(
////    activeRoom: RoomEntity?,
////    allRooms: List<RoomEntity>,
////    records: List<MonthlyRecordEntity>,
////    latestRecord: MonthlyRecordEntity?,
////    currencySymbol: String,
////    onSelectRoom: (RoomEntity) -> Unit,
////    onManageRooms: () -> Unit,
////    onAddRecord: () -> Unit,
////    onViewHistory: () -> Unit,
////    onOpenRecordDetail: (MonthlyRecordEntity) -> Unit,
////    onExportPdf: (MonthlyRecordEntity) -> Unit,
////    modifier: Modifier = Modifier
////) {
////    val scrollState = rememberScrollState()
////    val context = LocalContext.current
////
////    // Historical electricity and water usage series for mini charts
////    val electricityHistory = remember(records) {
////        records.take(6).reversed().map { it.billingMonth to it.electricityUnits }
////    }
////    val waterHistory = remember(records) {
////        records.take(6).reversed().map { it.billingMonth to it.waterUnits }
////    }
////
////    val totalHistoryElecUnits = remember(records) {
////        records.sumOf { it.electricityUnits }
////    }
////    val totalHistoryWaterUnits = remember(records) {
////        records.sumOf { it.waterUnits }
////    }
////
////    Column(
////        modifier = modifier
////            .fillMaxSize()
////            .verticalScroll(scrollState)
////            .padding(bottom = 24.dp)
////    ) {
////        // Room Switcher
////        RoomSelectorBar(
////            activeRoom = activeRoom,
////            allRooms = allRooms,
////            onSelectRoom = onSelectRoom,
////            onManageRooms = onManageRooms
////        )
////
////        Spacer(modifier = Modifier.height(10.dp))
////
////        if (latestRecord == null) {
////            // Empty State
////            Card(
////                modifier = Modifier
////                    .fillMaxWidth()
////                    .padding(horizontal = 16.dp, vertical = 16.dp),
////                shape = RoundedCornerShape(16.dp),
////                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
////                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
////                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
////            ) {
////                Column(
////                    modifier = Modifier
////                        .fillMaxWidth()
////                        .padding(28.dp),
////                    horizontalAlignment = Alignment.CenterHorizontally
////                ) {
////                    Box(
////                        modifier = Modifier
////                            .size(56.dp)
////                            .clip(RoundedCornerShape(12.dp))
////                            .background(MaterialTheme.colorScheme.primaryContainer),
////                        contentAlignment = Alignment.Center
////                    ) {
////                        Icon(
////                            imageVector = Icons.Default.ReceiptLong,
////                            contentDescription = null,
////                            tint = MaterialTheme.colorScheme.primary,
////                            modifier = Modifier.size(28.dp)
////                        )
////                    }
////                    Spacer(modifier = Modifier.height(16.dp))
////                    Text(
////                        text = "No Records for ${activeRoom?.name ?: "this room"}",
////                        style = MaterialTheme.typography.titleMedium,
////                        fontWeight = FontWeight.Bold
////                    )
////                    Spacer(modifier = Modifier.height(6.dp))
////                    Text(
////                        text = "Start by adding your first monthly bill with electricity and water meter readings.",
////                        style = MaterialTheme.typography.bodyMedium,
////                        color = MaterialTheme.colorScheme.onSurfaceVariant,
////                        textAlign = TextAlign.Center
////                    )
////                    Spacer(modifier = Modifier.height(20.dp))
////                    Button(
////                        onClick = onAddRecord,
////                        modifier = Modifier.testTag("add_first_record_button"),
////                        shape = RoundedCornerShape(8.dp)
////                    ) {
////                        Icon(Icons.Default.Add, contentDescription = null)
////                        Spacer(modifier = Modifier.width(8.dp))
////                        Text("Add Monthly Record")
////                    }
////                }
////            }
////        } else {
////            // MAIN CURRENT BILL CARD (Geometric Balance high-impact display)
////            Card(
////                modifier = Modifier
////                    .fillMaxWidth()
////                    .padding(horizontal = 16.dp)
////                    .clickable { onOpenRecordDetail(latestRecord) },
////                shape = RoundedCornerShape(16.dp),
////                colors = CardDefaults.cardColors(
////                    containerColor = MaterialTheme.colorScheme.surface
////                ),
////                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
////                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
////            ) {
////                Column(
////                    modifier = Modifier
////                        .fillMaxWidth()
////                        .padding(20.dp)
////                ) {
////                    Row(
////                        modifier = Modifier.fillMaxWidth(),
////                        horizontalArrangement = Arrangement.SpaceBetween,
////                        verticalAlignment = Alignment.CenterVertically
////                    ) {
////                        Column {
////                            Text(
////                                text = "CURRENT BILL",
////                                style = MaterialTheme.typography.labelSmall,
////                                fontWeight = FontWeight.Bold,
////                                color = MaterialTheme.colorScheme.primary,
////                                letterSpacing = 1.2.sp
////                            )
////                            Spacer(modifier = Modifier.height(2.dp))
////                            Text(
////                                text = latestRecord.billingMonth,
////                                style = MaterialTheme.typography.titleLarge,
////                                fontWeight = FontWeight.Bold,
////                                color = MaterialTheme.colorScheme.onSurface
////                            )
////                        }
////                        PaymentStatusBadge(status = latestRecord.paymentStatus)
////                    }
////
////                    Spacer(modifier = Modifier.height(16.dp))
////
////                    // Big Total Bill Amount
////                    Row(
////                        modifier = Modifier.fillMaxWidth(),
////                        horizontalArrangement = Arrangement.SpaceBetween,
////                        verticalAlignment = Alignment.Bottom
////                    ) {
////                        Column {
////                            Text(
////                                text = "Total Amount",
////                                style = MaterialTheme.typography.bodySmall,
////                                color = MaterialTheme.colorScheme.onSurfaceVariant
////                            )
////                            Text(
////                                text = FormatUtils.formatMoney(latestRecord.totalAmount, currencySymbol),
////                                style = MaterialTheme.typography.headlineMedium,
////                                fontWeight = FontWeight.Bold,
////                                color = MaterialTheme.colorScheme.onSurface
////                            )
////                        }
////                        if (latestRecord.remainingAmount > 0) {
////                            Column(horizontalAlignment = Alignment.End) {
////                                Text(
////                                    text = "Due / Remaining",
////                                    style = MaterialTheme.typography.bodySmall,
////                                    color = StatusUnpaid
////                                )
////                                Text(
////                                    text = FormatUtils.formatMoney(latestRecord.remainingAmount, currencySymbol),
////                                    style = MaterialTheme.typography.titleMedium,
////                                    fontWeight = FontWeight.Bold,
////                                    color = StatusUnpaid
////                                )
////                            }
////                        } else {
////                            Column(horizontalAlignment = Alignment.End) {
////                                Text(
////                                    text = "Paid in Full",
////                                    style = MaterialTheme.typography.bodySmall,
////                                    color = StatusPaid
////                                )
////                                Text(
////                                    text = FormatUtils.formatMoney(latestRecord.amountPaid, currencySymbol),
////                                    style = MaterialTheme.typography.titleMedium,
////                                    fontWeight = FontWeight.Bold,
////                                    color = StatusPaid
////                                )
////                            }
////                        }
////                    }
////
////                    Spacer(modifier = Modifier.height(18.dp))
////                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
////                    Spacer(modifier = Modifier.height(14.dp))
////
////                    // Itemized breakdown rows
////                    BillBreakdownRow(
////                        label = "Room Rent",
////                        calculation = "",
////                        amount = FormatUtils.formatMoney(latestRecord.roomRent, currencySymbol),
////                        icon = Icons.Default.Bed,
////                        accentColor = RentAccent
////                    )
////
////                    Spacer(modifier = Modifier.height(10.dp))
////
////                    BillBreakdownRow(
////                        label = "Electricity",
////                        calculation = "${FormatUtils.formatUnits(latestRecord.electricityUnits)} units × ${FormatUtils.formatMoney(latestRecord.electricityRate, currencySymbol)}",
////                        amount = FormatUtils.formatMoney(latestRecord.electricityCost, currencySymbol),
////                        icon = Icons.Default.Bolt,
////                        accentColor = ElectricityAccent,
////                        details = "Meter: ${FormatUtils.formatUnits(latestRecord.previousElectricityReading)} → ${FormatUtils.formatUnits(latestRecord.currentElectricityReading)}"
////                    )
////
////                    if (latestRecord.waterUnits > 0 || latestRecord.waterCost > 0) {
////                        Spacer(modifier = Modifier.height(10.dp))
////                        BillBreakdownRow(
////                            label = "Water",
////                            calculation = if (latestRecord.waterUnits > 0 && latestRecord.waterRate > 0) {
////                                "${FormatUtils.formatUnits(latestRecord.waterUnits)} units × ${FormatUtils.formatMoney(latestRecord.waterRate, currencySymbol)}"
////                            } else "",
////                            amount = FormatUtils.formatMoney(latestRecord.waterCost, currencySymbol),
////                            icon = Icons.Default.WaterDrop,
////                            accentColor = WaterAccent,
////                            details = if (latestRecord.waterUnits > 0) "Meter: ${FormatUtils.formatUnits(latestRecord.previousWaterReading)} → ${FormatUtils.formatUnits(latestRecord.currentWaterReading)}" else null
////                        )
////                    }
////
////                    if (latestRecord.wasteCharge > 0) {
////                        Spacer(modifier = Modifier.height(10.dp))
////                        BillBreakdownRow(
////                            label = "Waste / Minimum",
////                            calculation = "",
////                            amount = FormatUtils.formatMoney(latestRecord.wasteCharge, currencySymbol),
////                            icon = Icons.Default.Delete,
////                            accentColor = WasteAccent
////                        )
////                    }
////
////                    if (latestRecord.otherCharges > 0) {
////                        Spacer(modifier = Modifier.height(10.dp))
////                        BillBreakdownRow(
////                            label = "Other Charges",
////                            calculation = "",
////                            amount = FormatUtils.formatMoney(latestRecord.otherCharges, currencySymbol),
////                            icon = Icons.Default.Receipt,
////                            accentColor = MaterialTheme.colorScheme.secondary
////                        )
////                    }
////
////                    if (latestRecord.discount > 0) {
////                        Spacer(modifier = Modifier.height(10.dp))
////                        BillBreakdownRow(
////                            label = "Discount",
////                            calculation = "",
////                            amount = "- " + FormatUtils.formatMoney(latestRecord.discount, currencySymbol),
////                            icon = Icons.Default.LocalOffer,
////                            accentColor = StatusPaid
////                        )
////                    }
////
////                    if (latestRecord.paymentDate.isNotBlank() || latestRecord.amountPaid > 0) {
////                        Spacer(modifier = Modifier.height(14.dp))
////                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
////                        Spacer(modifier = Modifier.height(12.dp))
////
////                        Row(
////                            modifier = Modifier.fillMaxWidth(),
////                            horizontalArrangement = Arrangement.SpaceBetween
////                        ) {
////                            Text(
////                                text = if (latestRecord.paymentDate.isNotBlank()) "Last Payment: ${latestRecord.paymentDate}" else "Paid: ${FormatUtils.formatMoney(latestRecord.amountPaid, currencySymbol)}",
////                                style = MaterialTheme.typography.bodySmall,
////                                color = MaterialTheme.colorScheme.onSurfaceVariant
////                            )
////                            Text(
////                                text = "Amount Paid: ${FormatUtils.formatMoney(latestRecord.amountPaid, currencySymbol)}",
////                                style = MaterialTheme.typography.bodySmall,
////                                fontWeight = FontWeight.SemiBold,
////                                color = MaterialTheme.colorScheme.onSurface
////                            )
////                        }
////                    }
////
////                    Spacer(modifier = Modifier.height(16.dp))
////
////                    // Action Buttons inside Bill Card
////                    Row(
////                        modifier = Modifier.fillMaxWidth(),
////                        horizontalArrangement = Arrangement.spacedBy(8.dp)
////                    ) {
////                        OutlinedButton(
////                            onClick = { onExportPdf(latestRecord) },
////                            modifier = Modifier
////                                .weight(1f)
////                                .testTag("export_pdf_button"),
////                            shape = RoundedCornerShape(8.dp),
////                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
////                        ) {
////                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
////                            Spacer(modifier = Modifier.width(6.dp))
////                            Text("PDF Bill", fontSize = 12.sp)
////                        }
////                        Button(
////                            onClick = onAddRecord,
////                            modifier = Modifier
////                                .weight(1.3f)
////                                .testTag("add_record_button"),
////                            shape = RoundedCornerShape(8.dp)
////                        ) {
////                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
////                            Spacer(modifier = Modifier.width(6.dp))
////                            Text("New Record", fontSize = 12.sp)
////                        }
////                    }
////                }
////            }
////
////            Spacer(modifier = Modifier.height(18.dp))
////
////            // SECTION 6: ELECTRICITY TRACKING
////            SectionHeader(
////                title = "Electricity Tracking",
////                subtitle = "Meter readings and monthly usage"
////            )
////
////            Card(
////                modifier = Modifier
////                    .fillMaxWidth()
////                    .padding(horizontal = 16.dp),
////                shape = RoundedCornerShape(16.dp),
////                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
////                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
////                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
////            ) {
////                Column(
////                    modifier = Modifier
////                        .fillMaxWidth()
////                        .padding(18.dp)
////                ) {
////                    Row(
////                        modifier = Modifier.fillMaxWidth(),
////                        horizontalArrangement = Arrangement.SpaceBetween,
////                        verticalAlignment = Alignment.CenterVertically
////                    ) {
////                        Row(verticalAlignment = Alignment.CenterVertically) {
////                            Box(
////                                modifier = Modifier
////                                    .size(34.dp)
////                                    .clip(RoundedCornerShape(8.dp))
////                                    .background(ElectricityAccent.copy(alpha = 0.12f)),
////                                contentAlignment = Alignment.Center
////                            ) {
////                                Icon(Icons.Default.Bolt, contentDescription = null, tint = ElectricityAccent, modifier = Modifier.size(18.dp))
////                            }
////                            Spacer(modifier = Modifier.width(10.dp))
////                            Column {
////                                Text(
////                                    text = "This Month's Electricity",
////                                    style = MaterialTheme.typography.titleSmall,
////                                    fontWeight = FontWeight.Bold
////                                )
////                                Text(
////                                    text = "${FormatUtils.formatUnits(latestRecord.electricityUnits)} units used",
////                                    style = MaterialTheme.typography.bodySmall,
////                                    color = MaterialTheme.colorScheme.onSurfaceVariant
////                                )
////                            }
////                        }
////                        Text(
////                            text = FormatUtils.formatMoney(latestRecord.electricityCost, currencySymbol),
////                            style = MaterialTheme.typography.titleMedium,
////                            fontWeight = FontWeight.Bold,
////                            color = ElectricityAccent
////                        )
////                    }
////
////                    Spacer(modifier = Modifier.height(14.dp))
////
////                    // Meter Reading Comparison Grid
////                    Row(
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .clip(RoundedCornerShape(10.dp))
////                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)), RoundedCornerShape(10.dp))
////                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
////                            .padding(12.dp),
////                        horizontalArrangement = Arrangement.SpaceBetween
////                    ) {
////                        MeterColumn(
////                            label = "Last Meter",
////                            value = FormatUtils.formatUnits(latestRecord.previousElectricityReading)
////                        )
////                        MeterColumn(
////                            label = "Current Meter",
////                            value = FormatUtils.formatUnits(latestRecord.currentElectricityReading)
////                        )
////                        MeterColumn(
////                            label = "Rate / Unit",
////                            value = FormatUtils.formatMoney(latestRecord.electricityRate, currencySymbol)
////                        )
////                    }
////
////                    Spacer(modifier = Modifier.height(14.dp))
////
////                    Row(
////                        modifier = Modifier.fillMaxWidth(),
////                        horizontalArrangement = Arrangement.SpaceBetween
////                    ) {
////                        Text(
////                            text = "Historical Total: ${FormatUtils.formatUnits(totalHistoryElecUnits)} units",
////                            style = MaterialTheme.typography.labelMedium,
////                            color = MaterialTheme.colorScheme.onSurfaceVariant
////                        )
////                    }
////
////                    Spacer(modifier = Modifier.height(8.dp))
////                    SimpleBarChart(
////                        data = electricityHistory,
////                        barColor = ElectricityAccent,
////                        unitLabel = "units"
////                    )
////                }
////            }
////
////            Spacer(modifier = Modifier.height(18.dp))
////
////            // SECTION 7: WATER TRACKING
////            SectionHeader(
////                title = "Water Tracking",
////                subtitle = "Meter readings and monthly consumption"
////            )
////
////            Card(
////                modifier = Modifier
////                    .fillMaxWidth()
////                    .padding(horizontal = 16.dp),
////                shape = RoundedCornerShape(16.dp),
////                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
////                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
////                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
////            ) {
////                Column(
////                    modifier = Modifier
////                        .fillMaxWidth()
////                        .padding(18.dp)
////                ) {
////                    Row(
////                        modifier = Modifier.fillMaxWidth(),
////                        horizontalArrangement = Arrangement.SpaceBetween,
////                        verticalAlignment = Alignment.CenterVertically
////                    ) {
////                        Row(verticalAlignment = Alignment.CenterVertically) {
////                            Box(
////                                modifier = Modifier
////                                    .size(34.dp)
////                                    .clip(RoundedCornerShape(8.dp))
////                                    .background(WaterAccent.copy(alpha = 0.12f)),
////                                contentAlignment = Alignment.Center
////                            ) {
////                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WaterAccent, modifier = Modifier.size(18.dp))
////                            }
////                            Spacer(modifier = Modifier.width(10.dp))
////                            Column {
////                                Text(
////                                    text = "This Month's Water",
////                                    style = MaterialTheme.typography.titleSmall,
////                                    fontWeight = FontWeight.Bold
////                                )
////                                Text(
////                                    text = "${FormatUtils.formatUnits(latestRecord.waterUnits)} units used",
////                                    style = MaterialTheme.typography.bodySmall,
////                                    color = MaterialTheme.colorScheme.onSurfaceVariant
////                                )
////                            }
////                        }
////                        Text(
////                            text = FormatUtils.formatMoney(latestRecord.waterCost, currencySymbol),
////                            style = MaterialTheme.typography.titleMedium,
////                            fontWeight = FontWeight.Bold,
////                            color = WaterAccent
////                        )
////                    }
////
////                    Spacer(modifier = Modifier.height(14.dp))
////
////                    // Water Meter Reading Grid
////                    Row(
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .clip(RoundedCornerShape(10.dp))
////                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)), RoundedCornerShape(10.dp))
////                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
////                            .padding(12.dp),
////                        horizontalArrangement = Arrangement.SpaceBetween
////                    ) {
////                        MeterColumn(
////                            label = "Last Meter",
////                            value = FormatUtils.formatUnits(latestRecord.previousWaterReading)
////                        )
////                        MeterColumn(
////                            label = "Current Meter",
////                            value = FormatUtils.formatUnits(latestRecord.currentWaterReading)
////                        )
////                        MeterColumn(
////                            label = "Rate / Unit",
////                            value = FormatUtils.formatMoney(latestRecord.waterRate, currencySymbol)
////                        )
////                    }
////
////                    Spacer(modifier = Modifier.height(14.dp))
////
////                    Row(
////                        modifier = Modifier.fillMaxWidth(),
////                        horizontalArrangement = Arrangement.SpaceBetween
////                    ) {
////                        Text(
////                            text = "Historical Total: ${FormatUtils.formatUnits(totalHistoryWaterUnits)} units",
////                            style = MaterialTheme.typography.labelMedium,
////                            color = MaterialTheme.colorScheme.onSurfaceVariant
////                        )
////                    }
////
////                    Spacer(modifier = Modifier.height(8.dp))
////                    SimpleBarChart(
////                        data = waterHistory,
////                        barColor = WaterAccent,
////                        unitLabel = "units"
////                    )
////                }
////            }
////        }
////    }
////}
////
////@Composable
////private fun BillBreakdownRow(
////    label: String,
////    calculation: String,
////    amount: String,
////    icon: androidx.compose.ui.graphics.vector.ImageVector,
////    accentColor: Color,
////    details: String? = null
////) {
////    Row(
////        modifier = Modifier.fillMaxWidth(),
////        verticalAlignment = Alignment.CenterVertically,
////        horizontalArrangement = Arrangement.SpaceBetween
////    ) {
////        Row(
////            verticalAlignment = Alignment.CenterVertically,
////            modifier = Modifier.weight(1f)
////        ) {
////            Box(
////                modifier = Modifier
////                    .size(26.dp)
////                    .clip(RoundedCornerShape(6.dp))
////                    .background(accentColor.copy(alpha = 0.15f)),
////                contentAlignment = Alignment.Center
////            ) {
////                Icon(
////                    imageVector = icon,
////                    contentDescription = null,
////                    tint = accentColor,
////                    modifier = Modifier.size(15.dp)
////                )
////            }
////            Spacer(modifier = Modifier.width(10.dp))
////            Column {
////                Text(
////                    text = label,
////                    style = MaterialTheme.typography.bodyMedium,
////                    fontWeight = FontWeight.Medium,
////                    color = MaterialTheme.colorScheme.onSurface
////                )
////                if (calculation.isNotBlank()) {
////                    Text(
////                        text = calculation,
////                        style = MaterialTheme.typography.labelSmall,
////                        color = MaterialTheme.colorScheme.onSurfaceVariant
////                    )
////                }
////                if (details != null) {
////                    Text(
////                        text = details,
////                        style = MaterialTheme.typography.labelSmall,
////                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
////                    )
////                }
////            }
////        }
////        Text(
////            text = amount,
////            style = MaterialTheme.typography.bodyLarge,
////            fontWeight = FontWeight.SemiBold,
////            color = MaterialTheme.colorScheme.onSurface
////        )
////    }
////}
////
////@Composable
////private fun MeterColumn(label: String, value: String) {
////    Column(horizontalAlignment = Alignment.CenterHorizontally) {
////        Text(
////            text = label,
////            style = MaterialTheme.typography.labelSmall,
////            color = MaterialTheme.colorScheme.onSurfaceVariant
////        )
////        Spacer(modifier = Modifier.height(2.dp))
////        Text(
////            text = value,
////            style = MaterialTheme.typography.bodyMedium,
////            fontWeight = FontWeight.Bold,
////            color = MaterialTheme.colorScheme.onSurface
////        )
////    }
////}
//
//
//
//
//package com.example.ui.screens
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.data.MonthlyRecordEntity
//import com.example.data.RoomEntity
//import com.example.ui.components.PaymentStatusBadge
//import com.example.ui.components.RoomSelectorBar
//import com.example.ui.components.SectionHeader
//import com.example.ui.components.SimpleBarChart
//import com.example.ui.theme.*
//import com.example.util.FormatUtils
//
//@Composable
//fun DashboardScreen(
//    activeRoom: RoomEntity?,
//    allRooms: List<RoomEntity>,
//    records: List<MonthlyRecordEntity>,
//    latestRecord: MonthlyRecordEntity?,
//    currencySymbol: String,
//    onSelectRoom: (RoomEntity) -> Unit,
//    onManageRooms: () -> Unit,
//    onAddRecord: () -> Unit,
//    onViewHistory: () -> Unit,
//    onOpenRecordDetail: (MonthlyRecordEntity) -> Unit,
//    onExportPdf: (MonthlyRecordEntity) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val scrollState = rememberScrollState()
//
//    // ── charts data ────────────────────────────────────────────
//    val electricityHistory = remember(records) {
//        records.take(6).reversed().map { it.billingMonth to it.electricityUnits }
//    }
//    val waterHistory = remember(records) {
//        records.take(6).reversed().map { it.billingMonth to it.waterUnits }
//    }
//    val rentHistory = remember(records) {
//        records.take(6).reversed().map { it.billingMonth to it.totalAmount }
//    }
//
//    // ── occupancy simulation ───────────────────────────────────
//    // A room is "occupied" if it has at least one record.
//    // A room is "vacant" if no records exist for it.
//    val occupiedCount = remember(allRooms) { allRooms.size }
//    val vacantCount = remember(allRooms) { 0 }
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .verticalScroll(scrollState)
//            .padding(bottom = 24.dp)
//    ) {
//        RoomSelectorBar(
//            activeRoom = activeRoom,
//            allRooms = allRooms,
//            onSelectRoom = onSelectRoom,
//            onManageRooms = onManageRooms
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // ═══════════════════════════════════════════════════════
//        // OCCUPANCY OVERVIEW
//        // ═══════════════════════════════════════════════════════
//        SectionHeader(
//            title = "Occupancy",
//            subtitle = "Rooms and their current status"
//        )
//
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            OccupancyStat(
//                label = "Total Rooms",
//                value = allRooms.size.toString(),
//                icon = Icons.Default.Apartment,
//                accent = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.weight(1f)
//            )
//            OccupancyStat(
//                label = "Occupied",
//                value = allRooms.size.toString(),
//                icon = Icons.Default.Person,
//                accent = StatusPaid,
//                modifier = Modifier.weight(1f)
//            )
//            OccupancyStat(
//                label = "Vacant",
//                value = "0",
//                icon = Icons.Default.MeetingRoom,
//                accent = StatusUnpaid,
//                modifier = Modifier.weight(1f)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(14.dp))
//
//        // ── horizontal room cards ──────────────────────────────
//        if (allRooms.isNotEmpty()) {
//            LazyRow(
//                contentPadding = PaddingValues(horizontal = 16.dp),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(allRooms, key = { it.id }) { room ->
//                    RoomOccupancyCard(
//                        room = room,
//                        isActive = room.id == activeRoom?.id,
//                        hasRecord = room.id == activeRoom?.id && latestRecord != null,
//                        monthlyAmount = if (room.id == activeRoom?.id && latestRecord != null)
//                            latestRecord.totalAmount else 0.0,
//                        currencySymbol = currencySymbol,
//                        onClick = { onSelectRoom(room) }
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(22.dp))
//
//        // ═══════════════════════════════════════════════════════
//        // QUICK ACTION TILES
//        // ═══════════════════════════════════════════════════════
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            QuickActionTile(
//                icon = Icons.Default.Add,
//                label = "New Bill",
//                accent = MaterialTheme.colorScheme.primary,
//                onClick = onAddRecord,
//                modifier = Modifier.weight(1f)
//            )
//            QuickActionTile(
//                icon = Icons.Default.History,
//                label = "History",
//                accent = ElectricityAccent,
//                onClick = onViewHistory,
//                modifier = Modifier.weight(1f)
//            )
//            QuickActionTile(
//                icon = Icons.Default.PictureAsPdf,
//                label = "Export",
//                accent = WaterAccent,
//                onClick = {
//                    latestRecord?.let { onExportPdf(it) }
//                },
//                enabled = latestRecord != null,
//                modifier = Modifier.weight(1f)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(22.dp))
//
//        // ═══════════════════════════════════════════════════════
//        // CHARTS
//        // ═══════════════════════════════════════════════════════
//        SectionHeader(
//            title = "Usage Trends",
//            subtitle = "Last 6 months at a glance"
//        )
//
//        // electricity chart card
//        ChartCard(
//            title = "Electricity",
//            subtitle = "Monthly consumption",
//            totalLabel = FormatUtils.formatUnits(records.sumOf { it.electricityUnits }) + " units total",
//            accent = ElectricityAccent,
//            icon = Icons.Default.Bolt
//        ) {
//            SimpleBarChart(
//                data = electricityHistory,
//                barColor = ElectricityAccent,
//                unitLabel = "units"
//            )
//        }
//
//        Spacer(modifier = Modifier.height(14.dp))
//
//        // water chart card
//        ChartCard(
//            title = "Water",
//            subtitle = "Monthly consumption",
//            totalLabel = FormatUtils.formatUnits(records.sumOf { it.waterUnits }) + " units total",
//            accent = WaterAccent,
//            icon = Icons.Default.WaterDrop
//        ) {
//            SimpleBarChart(
//                data = waterHistory,
//                barColor = WaterAccent,
//                unitLabel = "units"
//            )
//        }
//
//        Spacer(modifier = Modifier.height(14.dp))
//
//        // rent / bill amount chart
//        ChartCard(
//            title = "Total Billed",
//            subtitle = "Monthly bill amounts",
//            totalLabel = FormatUtils.formatMoney(
//                records.sumOf { it.totalAmount }, currencySymbol
//            ) + " total",
//            accent = RentAccent,
//            icon = Icons.Default.Payments
//        ) {
//            SimpleBarChart(
//                data = rentHistory,
//                barColor = RentAccent,
//                unitLabel = currencySymbol
//            )
//        }
//
//        Spacer(modifier = Modifier.height(22.dp))
//
//        // ═══════════════════════════════════════════════════════
//        // CURRENT ROOM SNAPSHOT (small summary, not full bill)
//        // ═══════════════════════════════════════════════════════
//        if (latestRecord != null) {
//            SectionHeader(
//                title = "Latest Bill",
//                subtitle = activeRoom?.name ?: "Current room"
//            )
//
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp)
//                    .clickable { onOpenRecordDetail(latestRecord) },
//                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
//                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(48.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            Icons.Default.ReceiptLong,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                    Spacer(modifier = Modifier.width(14.dp))
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(
//                            text = latestRecord.billingMonth,
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Spacer(modifier = Modifier.height(2.dp))
//                        Text(
//                            text = FormatUtils.formatMoney(latestRecord.totalAmount, currencySymbol),
//                            style = MaterialTheme.typography.headlineSmall,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                    Column(horizontalAlignment = Alignment.End) {
//                        PaymentStatusBadge(status = latestRecord.paymentStatus)
//                        Spacer(modifier = Modifier.height(6.dp))
//                        Text(
//                            text = if (latestRecord.remainingAmount > 0)
//                                "Due ${FormatUtils.formatMoney(latestRecord.remainingAmount, currencySymbol)}"
//                            else
//                                "Paid in full",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = if (latestRecord.remainingAmount > 0) StatusUnpaid else StatusPaid,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//            }
//        } else if (allRooms.isNotEmpty()) {
//            // no records yet for this room
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp),
//                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
//                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(24.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Icon(
//                        Icons.Default.ReceiptLong,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.outline,
//                        modifier = Modifier.size(40.dp)
//                    )
//                    Spacer(modifier = Modifier.height(10.dp))
//                    Text(
//                        text = "No records for ${activeRoom?.name ?: "this room"}",
//                        style = MaterialTheme.typography.titleSmall,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        text = "Add your first monthly bill to see it here.",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        textAlign = TextAlign.Center
//                    )
//                    Spacer(modifier = Modifier.height(14.dp))
//                    Button(
//                        onClick = onAddRecord,
//                        shape = RoundedCornerShape(10.dp)
//                    ) {
//                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
//                        Spacer(modifier = Modifier.width(6.dp))
//                        Text("Add Record")
//                    }
//                }
//            }
//        }
//    }
//}
//
///* ═══════════════════════════════════════════════════════════════
//   Occupancy stat tile
//   ═══════════════════════════════════════════════════════════════ */
//@Composable
//private fun OccupancyStat(
//    label: String,
//    value: String,
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    accent: Color,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier,
//        shape = RoundedCornerShape(14.dp),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
//        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(30.dp)
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(accent.copy(alpha = 0.14f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = value,
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//            Text(
//                text = label,
//                style = MaterialTheme.typography.labelSmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//}
//
///* ═══════════════════════════════════════════════════════════════
//   Room occupancy card (horizontal scroll)
//   ═══════════════════════════════════════════════════════════════ */
//@Composable
//private fun RoomOccupancyCard(
//    room: RoomEntity,
//    isActive: Boolean,
//    hasRecord: Boolean,
//    monthlyAmount: Double,
//    currencySymbol: String,
//    onClick: () -> Unit
//) {
//    val occupied = hasRecord || isActive
//    val statusColor = if (occupied) StatusPaid else StatusUnpaid
//    val statusLabel = if (occupied) "Occupied" else "Vacant"
//
//    Card(
//        modifier = Modifier
//            .width(190.dp)
//            .clickable { onClick() },
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isActive)
//                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
//            else MaterialTheme.colorScheme.surface
//        ),
//        border = BorderStroke(
//            width = if (isActive) 1.5.dp else 1.dp,
//            color = if (isActive) MaterialTheme.colorScheme.primary
//            else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(14.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(36.dp)
//                        .clip(RoundedCornerShape(10.dp))
//                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        Icons.Default.Apartment,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//                // status pill
//                Surface(
//                    shape = RoundedCornerShape(20.dp),
//                    color = statusColor.copy(alpha = 0.14f)
//                ) {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(6.dp)
//                                .clip(CircleShape)
//                                .background(statusColor)
//                        )
//                        Spacer(modifier = Modifier.width(5.dp))
//                        Text(
//                            text = statusLabel,
//                            style = MaterialTheme.typography.labelSmall,
//                            color = statusColor,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Text(
//                text = room.name,
//                style = MaterialTheme.typography.titleSmall,
//                fontWeight = FontWeight.Bold,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            if (room.address.isNotBlank()) {
//                Text(
//                    text = room.address,
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            Spacer(modifier = Modifier.height(10.dp))
//            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
//            Spacer(modifier = Modifier.height(10.dp))
//
//            Text(
//                text = if (monthlyAmount > 0) "This month" else "Rent rate",
//                style = MaterialTheme.typography.labelSmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//            Text(
//                text = FormatUtils.formatMoney(
//                    if (monthlyAmount > 0) monthlyAmount else room.defaultRent,
//                    currencySymbol
//                ),
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//        }
//    }
//}
//
///* ═══════════════════════════════════════════════════════════════
//   Quick action tile
//   ═══════════════════════════════════════════════════════════════ */
//@Composable
//private fun QuickActionTile(
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    label: String,
//    accent: Color,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true
//) {
//    Card(
//        modifier = modifier
//            .clickable(enabled = enabled) { onClick() },
//        shape = RoundedCornerShape(14.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (enabled) MaterialTheme.colorScheme.surface
//            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//        ),
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
//        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 14.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(36.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(
//                        if (enabled) accent.copy(alpha = 0.14f)
//                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = null,
//                    tint = if (enabled) accent else MaterialTheme.colorScheme.outline,
//                    modifier = Modifier.size(18.dp)
//                )
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = label,
//                style = MaterialTheme.typography.labelMedium,
//                fontWeight = FontWeight.SemiBold,
//                color = if (enabled) MaterialTheme.colorScheme.onSurface
//                else MaterialTheme.colorScheme.outline
//            )
//        }
//    }
//}
//
///* ═══════════════════════════════════════════════════════════════
//   Chart card wrapper
//   ═══════════════════════════════════════════════════════════════ */
//@Composable
//private fun ChartCard(
//    title: String,
//    subtitle: String,
//    totalLabel: String,
//    accent: Color,
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    content: @Composable () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
//        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Box(
//                        modifier = Modifier
//                            .size(34.dp)
//                            .clip(RoundedCornerShape(9.dp))
//                            .background(accent.copy(alpha = 0.14f)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
//                    }
//                    Spacer(modifier = Modifier.width(10.dp))
//                    Column {
//                        Text(
//                            text = title,
//                            style = MaterialTheme.typography.titleSmall,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Text(
//                            text = subtitle,
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Text(
//                text = totalLabel,
//                style = MaterialTheme.typography.labelMedium,
//                color = accent,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            Spacer(modifier = Modifier.height(10.dp))
//
//            content()
//        }
//    }
//}



package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity>,
    records: List<MonthlyRecordEntity>,
    allRecords: List<MonthlyRecordEntity>,
    latestRecord: MonthlyRecordEntity?,
    currencySymbol: String,
    onSelectRoom: (RoomEntity) -> Unit,
    onManageRooms: () -> Unit,
    onAddRecord: () -> Unit,
    onViewHistory: () -> Unit,
    onOpenRecordDetail: (MonthlyRecordEntity) -> Unit,
    onExportPdf: (MonthlyRecordEntity) -> Unit,
    onViewStatistics: () -> Unit = onViewHistory,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // ── charts data ────────────────────────────────────────────
    val electricityHistory = remember(records) {
        records.take(6).reversed().map { it.billingMonth to it.electricityUnits }
    }
    val waterHistory = remember(records) {
        records.take(6).reversed().map { it.billingMonth to it.waterUnits }
    }
    val rentHistory = remember(records) {
        records.take(6).reversed().map { it.billingMonth to it.totalAmount }
    }

    // ── real occupancy ─────────────────────────────────────────
    val roomIdsWithRecords = remember(allRecords) {
        allRecords.map { it.roomId }.toSet()
    }
    val occupiedCount = remember(allRooms, roomIdsWithRecords) {
        allRooms.count { it.id in roomIdsWithRecords }
    }
    val vacantCount = remember(allRooms, roomIdsWithRecords) {
        allRooms.count { it.id !in roomIdsWithRecords }
    }

    // ── this-month vs last-month ───────────────────────────────
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val thisMonth = remember { monthFormat.format(Date()) }
    val lastMonth = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        monthFormat.format(cal.time)
    }

    val thisMonthRecords = remember(allRecords, thisMonth) {
        allRecords.filter { it.billingMonth.equals(thisMonth, ignoreCase = true) }
    }
    val lastMonthRecords = remember(allRecords, lastMonth) {
        allRecords.filter { it.billingMonth.equals(lastMonth, ignoreCase = true) }
    }

    val thisMonthBilled = remember(thisMonthRecords) { thisMonthRecords.sumOf { it.totalAmount } }
    val thisMonthPaid   = remember(thisMonthRecords) { thisMonthRecords.sumOf { it.amountPaid } }
    val thisMonthDue    = remember(thisMonthRecords) { thisMonthRecords.sumOf { it.remainingAmount } }
    val lastMonthBilled = remember(lastMonthRecords) { lastMonthRecords.sumOf { it.totalAmount } }

    val collectionRate = if (thisMonthBilled > 0)
        (thisMonthPaid / thisMonthBilled).coerceIn(0.0, 1.0) else 0.0

    // ── pending dues ───────────────────────────────────────────
    val pendingDues = remember(allRecords, allRooms) {
        allRecords
            .filter { it.remainingAmount > 0 }
            .groupBy { it.roomId }
            .map { (roomId, recs) ->
                val room = allRooms.firstOrNull { it.id == roomId }
                Triple(room, recs.sumOf { it.remainingAmount }, recs.size)
            }
            .filter { it.first != null }
            .sortedByDescending { it.second }
    }
    val totalOutstanding = remember(pendingDues) { pendingDues.sumOf { it.second } }

    // ── top consumers ──────────────────────────────────────────
    val topElectricityRooms = remember(allRecords, allRooms) {
        allRooms.map { room ->
            val total = allRecords
                .filter { it.roomId == room.id }
                .sumOf { it.electricityUnits }
            room to total
        }.sortedByDescending { it.second }.take(3)
    }
    val topWaterRooms = remember(allRecords, allRooms) {
        allRooms.map { room ->
            val total = allRecords
                .filter { it.roomId == room.id }
                .sumOf { it.waterUnits }
            room to total
        }.sortedByDescending { it.second }.take(3)
    }

    // ── recent activity ────────────────────────────────────────
    val recentRecords = remember(allRecords, allRooms) {
        allRecords
            .sortedByDescending { it.updatedAt }
            .take(4)
            .mapNotNull { rec ->
                val room = allRooms.firstOrNull { it.id == rec.roomId }
                if (room != null) rec to room else null
            }
    }

    // ── yearly totals ──────────────────────────────────────────
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR).toString() }
    val yearRecords = remember(allRecords, currentYear) {
        allRecords.filter { it.billingMonth.endsWith(currentYear) }
    }
    val yearTotalBilled = remember(yearRecords) { yearRecords.sumOf { it.totalAmount } }
    val yearTotalPaid   = remember(yearRecords) { yearRecords.sumOf { it.amountPaid } }
    val yearTotalDue    = remember(yearRecords) { yearRecords.sumOf { it.remainingAmount } }

    // ── averages ───────────────────────────────────────────────
    val avgBill = remember(allRecords) {
        if (allRecords.isEmpty()) 0.0
        else allRecords.sumOf { it.totalAmount } / allRecords.size
    }
    val highestBill = remember(allRecords) {
        allRecords.maxByOrNull { it.totalAmount }?.totalAmount ?: 0.0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // ── room selector ──────────────────────────────────────
        RoomSelectorBar(
            activeRoom = activeRoom,
            allRooms = allRooms,
            onSelectRoom = onSelectRoom,
            onManageRooms = onManageRooms
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ═══════════════════════════════════════════════════════
        // OCCUPANCY
        // ═══════════════════════════════════════════════════════
        SectionHeader(
            title = "Occupancy",
            subtitle = "Rooms and their current status"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OccupancyStat(
                label = "Total Rooms",
                value = allRooms.size.toString(),
                icon = Icons.Default.Apartment,
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            OccupancyStat(
                label = "Occupied",
                value = occupiedCount.toString(),
                icon = Icons.Default.Person,
                accent = StatusPaid,
                modifier = Modifier.weight(1f)
            )
            OccupancyStat(
                label = "Vacant",
                value = vacantCount.toString(),
                icon = Icons.Default.MeetingRoom,
                accent = StatusUnpaid,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── room cards ─────────────────────────────────────────
        if (allRooms.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allRooms, key = { it.id }) { room ->
                    val roomRecords = allRecords.filter { it.roomId == room.id }
                    val hasAnyRecord = roomRecords.isNotEmpty()
                    val latestRoomRecord = roomRecords.maxByOrNull { it.updatedAt }

                    RoomOccupancyCard(
                        room = room,
                        isActive = room.id == activeRoom?.id,
                        isOccupied = hasAnyRecord,
                        monthlyAmount = latestRoomRecord?.totalAmount ?: 0.0,
                        currencySymbol = currencySymbol,
                        onClick = { onSelectRoom(room) }
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // ALERTS
        // ═══════════════════════════════════════════════════════
        if (totalOutstanding > 0 || thisMonthDue > 0) {
            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (totalOutstanding > 0) {
                    AlertBanner(
                        icon = Icons.Default.Warning,
                        title = "Outstanding dues",
                        subtitle = "${pendingDues.size} room(s) · " +
                                FormatUtils.formatMoney(totalOutstanding, currencySymbol),
                        accent = StatusUnpaid,
                        onClick = onViewHistory
                    )
                }
                if (thisMonthDue > 0) {
                    AlertBanner(
                        icon = Icons.Default.Schedule,
                        title = "Unpaid this month",
                        subtitle = FormatUtils.formatMoney(thisMonthDue, currencySymbol) + " still due",
                        accent = StatusPartiallyPaid,
                        onClick = onViewHistory
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // QUICK ACTIONS
        // ═══════════════════════════════════════════════════════
        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionTile(
                icon = Icons.Default.Add,
                label = "New Bill",
                accent = MaterialTheme.colorScheme.primary,
                onClick = onAddRecord,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                icon = Icons.Default.History,
                label = "History",
                accent = ElectricityAccent,
                onClick = onViewHistory,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                icon = Icons.Default.PictureAsPdf,
                label = "Export",
                accent = WaterAccent,
                onClick = { latestRecord?.let { onExportPdf(it) } },
                enabled = latestRecord != null,
                modifier = Modifier.weight(1f)
            )
        }

        // ═══════════════════════════════════════════════════════
        // COLLECTION PROGRESS
        // ═══════════════════════════════════════════════════════
        if (thisMonthBilled > 0) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionHeader(
                title = "Collection Progress",
                subtitle = "This month · $thisMonth"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CollectionRing(
                        progress = collectionRate.toFloat(),
                        modifier = Modifier.size(86.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        CollectionRow(
                            label = "Billed",
                            value = FormatUtils.formatMoney(thisMonthBilled, currencySymbol),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        CollectionRow(
                            label = "Collected",
                            value = FormatUtils.formatMoney(thisMonthPaid, currencySymbol),
                            color = StatusPaid
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        CollectionRow(
                            label = "Due",
                            value = FormatUtils.formatMoney(thisMonthDue, currencySymbol),
                            color = if (thisMonthDue > 0) StatusUnpaid else StatusPaid
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // MONTH OVER MONTH
        // ═══════════════════════════════════════════════════════
        if (thisMonthBilled > 0 || lastMonthBilled > 0) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionHeader(
                title = "Month over Month",
                subtitle = "Compare with previous month"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ComparisonStat(
                    label = lastMonth,
                    value = FormatUtils.formatMoney(lastMonthBilled, currencySymbol),
                    accent = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                ComparisonStat(
                    label = thisMonth,
                    value = FormatUtils.formatMoney(thisMonthBilled, currencySymbol),
                    accent = MaterialTheme.colorScheme.primary,
                    previousValue = lastMonthBilled,
                    currentValue = thisMonthBilled,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ═══════════════════════════════════════════════════════
        // YEAR TO DATE
        // ═══════════════════════════════════════════════════════
        if (yearRecords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionHeader(
                title = "Year to Date",
                subtitle = "Totals for $currentYear"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        YearStat(
                            label = "Billed",
                            value = FormatUtils.formatMoney(yearTotalBilled, currencySymbol),
                            accent = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        YearStat(
                            label = "Collected",
                            value = FormatUtils.formatMoney(yearTotalPaid, currencySymbol),
                            accent = StatusPaid,
                            modifier = Modifier.weight(1f)
                        )
                        YearStat(
                            label = "Due",
                            value = FormatUtils.formatMoney(yearTotalDue, currencySymbol),
                            accent = StatusUnpaid,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${yearRecords.size} bills recorded",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Avg ${FormatUtils.formatMoney(avgBill, currencySymbol)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // PENDING DUES
        // ═══════════════════════════════════════════════════════
        if (pendingDues.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionHeader(
                title = "Pending Dues",
                subtitle = "Rooms with unpaid balances"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    pendingDues.forEachIndexed { index, (room, due, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { room?.let { onSelectRoom(it) } }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StatusUnpaid.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = StatusUnpaid,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = room?.name ?: "Unknown",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$count unpaid bill(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = FormatUtils.formatMoney(due, currencySymbol),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusUnpaid
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (index < pendingDues.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // USAGE TRENDS
        // ═══════════════════════════════════════════════════════
        Spacer(modifier = Modifier.height(22.dp))

        SectionHeader(
            title = "Usage Trends",
            subtitle = "Last 6 months at a glance"
        )

        ChartCard(
            title = "Electricity",
            subtitle = "Monthly consumption",
            totalLabel = FormatUtils.formatUnits(records.sumOf { it.electricityUnits }) + " units total",
            accent = ElectricityAccent,
            icon = Icons.Default.Bolt
        ) {
            SimpleBarChart(
                data = electricityHistory,
                barColor = ElectricityAccent,
                unitLabel = "units"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        ChartCard(
            title = "Water",
            subtitle = "Monthly consumption",
            totalLabel = FormatUtils.formatUnits(records.sumOf { it.waterUnits }) + " units total",
            accent = WaterAccent,
            icon = Icons.Default.WaterDrop
        ) {
            SimpleBarChart(
                data = waterHistory,
                barColor = WaterAccent,
                unitLabel = "units"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        ChartCard(
            title = "Total Billed",
            subtitle = "Monthly bill amounts",
            totalLabel = FormatUtils.formatMoney(
                records.sumOf { it.totalAmount }, currencySymbol
            ) + " total",
            accent = RentAccent,
            icon = Icons.Default.Payments
        ) {
            SimpleBarChart(
                data = rentHistory,
                barColor = RentAccent,
                unitLabel = currencySymbol
            )
        }

        // ═══════════════════════════════════════════════════════
        // TOP CONSUMERS
        // ═══════════════════════════════════════════════════════
        if (topElectricityRooms.any { it.second > 0 }) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionHeader(
                title = "Top Consumers",
                subtitle = "Highest cumulative usage"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TopConsumerCard(
                    title = "Electricity",
                    icon = Icons.Default.Bolt,
                    accent = ElectricityAccent,
                    entries = topElectricityRooms,
                    currencySymbol = currencySymbol,
                    unitLabel = "units",
                    modifier = Modifier.weight(1f)
                )
                TopConsumerCard(
                    title = "Water",
                    icon = Icons.Default.WaterDrop,
                    accent = WaterAccent,
                    entries = topWaterRooms,
                    currencySymbol = currencySymbol,
                    unitLabel = "units",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ═══════════════════════════════════════════════════════
        // RECENT ACTIVITY
        // ═══════════════════════════════════════════════════════
        if (recentRecords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionHeader(
                title = "Recent Activity",
                subtitle = "Latest bills you've added"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    recentRecords.forEachIndexed { index, (rec, room) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenRecordDetail(rec) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = room.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = rec.billingMonth,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FormatUtils.formatMoney(rec.totalAmount, currencySymbol),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                PaymentStatusBadge(status = rec.paymentStatus)
                            }
                        }
                        if (index < recentRecords.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // LATEST BILL
        // ═══════════════════════════════════════════════════════
        if (latestRecord != null) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionHeader(
                title = "Latest Bill",
                subtitle = activeRoom?.name ?: "Current room"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onOpenRecordDetail(latestRecord) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = latestRecord.billingMonth,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FormatUtils.formatMoney(latestRecord.totalAmount, currencySymbol),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        PaymentStatusBadge(status = latestRecord.paymentStatus)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (latestRecord.remainingAmount > 0)
                                "Due ${FormatUtils.formatMoney(latestRecord.remainingAmount, currencySymbol)}"
                            else
                                "Paid in full",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (latestRecord.remainingAmount > 0) StatusUnpaid else StatusPaid,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else if (allRooms.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No records for ${activeRoom?.name ?: "this room"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add your first monthly bill to see it here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onAddRecord,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Record")
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // QUICK STATS
        // ═══════════════════════════════════════════════════════
        if (allRecords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionHeader(
                title = "Quick Stats",
                subtitle = "All-time summary"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OccupancyStat(
                    label = "Avg Bill",
                    value = FormatUtils.formatMoney(avgBill, currencySymbol),
                    icon = Icons.Default.TrendingUp,
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                OccupancyStat(
                    label = "Highest Bill",
                    value = FormatUtils.formatMoney(highestBill, currencySymbol),
                    icon = Icons.Default.TrendingUp,
                    accent = StatusUnpaid,
                    modifier = Modifier.weight(1f)
                )
                OccupancyStat(
                    label = "Bills Total",
                    value = allRecords.size.toString(),
                    icon = Icons.Default.ReceiptLong,
                    accent = StatusPaid,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Occupancy stat tile
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun OccupancyStat(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Room occupancy card
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun RoomOccupancyCard(
    room: RoomEntity,
    isActive: Boolean,
    isOccupied: Boolean,
    monthlyAmount: Double,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val statusColor = if (isOccupied) StatusPaid else StatusUnpaid
    val statusLabel = if (isOccupied) "Occupied" else "Vacant"

    Card(
        modifier = Modifier
            .width(190.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Apartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = room.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (room.address.isNotBlank()) {
                Text(
                    text = room.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (monthlyAmount > 0) "Latest bill" else "Rent rate",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = FormatUtils.formatMoney(
                    if (monthlyAmount > 0) monthlyAmount else room.defaultRent,
                    currencySymbol
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Quick action tile
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (enabled) accent.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) accent else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.outline
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Chart card wrapper
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    totalLabel: String,
    accent: Color,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                            .clip(RoundedCornerShape(9.dp))
                            .background(accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = totalLabel,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            content()
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Alert banner
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun AlertBanner(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Collection ring
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun CollectionRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(900),
        label = "ringProgress"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        val progressColor = if (animated >= 0.99f) StatusPaid else MaterialTheme.colorScheme.primary

        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.11f
            drawCircle(color = trackColor, style = Stroke(width = stroke, cap = StrokeCap.Round))
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animated * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animated * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "collected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Collection row
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun CollectionRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   Comparison stat
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun ComparisonStat(
    label: String,
    value: String,
    accent: Color,
    previousValue: Double = 0.0,
    currentValue: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val delta = when {
        previousValue == 0.0 || currentValue == 0.0 -> null
        else -> (currentValue - previousValue) / previousValue
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            delta?.let {
                Spacer(modifier = Modifier.height(4.dp))
                val up = it > 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (up) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (up) StatusUnpaid else StatusPaid,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${if (up) "+" else ""}${(it * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (up) StatusUnpaid else StatusPaid,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Year stat
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun YearStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   Top consumer card
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun TopConsumerCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    entries: List<Pair<RoomEntity, Double>>,
    currencySymbol: String,
    unitLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            val max = entries.maxOfOrNull { it.second } ?: 0.0
            entries.forEachIndexed { index, (room, value) ->
                if (value > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(18.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = room.name,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(
                                            if (max > 0) (value / max).toFloat() else 0f
                                        )
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(accent)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = FormatUtils.formatUnits(value),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = accent
                        )
                    }
                }
            }
        }
    }
}