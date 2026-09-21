package com.rentutilitymanager.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rentutilitymanager.data.MonthlyRecordEntity
import com.rentutilitymanager.data.RoomEntity
import com.rentutilitymanager.ui.components.RoomPickerDialog
import com.rentutilitymanager.ui.components.RoomSwitcherCard
import com.rentutilitymanager.ui.components.SectionHeader
import com.rentutilitymanager.ui.components.SimpleBarChart
import com.rentutilitymanager.ui.components.StatCard
import com.rentutilitymanager.ui.theme.*
import com.rentutilitymanager.util.FormatUtils
import kotlin.math.cos
import kotlin.math.sin

/* ════════════════════════════════════════════════════════════
   FILTER ENUM — added CUSTOM
   ════════════════════════════════════════════════════════════ */
enum class StatFilter(val title: String, val count: Int) {
    LAST_3("3 Months", 3),
    LAST_6("6 Months", 6),
    LAST_12("12 Months", 12),
    ALL("All History", Int.MAX_VALUE),
    CUSTOM("Custom…", -1)
}

/* ════════════════════════════════════════════════════════════
   MONTH SORT HELPER — newest first
   ════════════════════════════════════════════════════════════ */
private val MONTH_ORDER = mapOf(
    "january" to 1, "jan" to 1,
    "february" to 2, "feb" to 2,
    "march" to 3, "mar" to 3,
    "april" to 4, "apr" to 4,
    "may" to 5,
    "june" to 6, "jun" to 6,
    "july" to 7, "jul" to 7,
    "august" to 8, "aug" to 8,
    "september" to 9, "sep" to 9, "sept" to 9,
    "october" to 10, "oct" to 10,
    "november" to 11, "nov" to 11,
    "december" to 12, "dec" to 12
)

private fun sortMonthsDesc(months: List<String>): List<String> =
    months.sortedWith(
        compareByDescending<String> {
            it.trim().substringAfterLast(" ").toIntOrNull() ?: 0
        }.thenByDescending {
            MONTH_ORDER[it.trim().substringBefore(" ").lowercase()] ?: 0
        }
    )

/* ════════════════════════════════════════════════════════════
   STATISTICS SCREEN
   ════════════════════════════════════════════════════════════ */
@Composable
fun StatisticsScreen(
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity>,
    records: List<MonthlyRecordEntity>,
    currencySymbol: String,
    onSelectRoom: (RoomEntity) -> Unit,
    onManageRooms: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(StatFilter.ALL) }
    var customMonths by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showRoomPicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val sortedRecords = remember(records) {
        records.sortedWith(
            compareByDescending<MonthlyRecordEntity> {
                it.billingMonth.trim().substringAfterLast(" ").toIntOrNull() ?: 0
            }.thenByDescending {
                MONTH_ORDER[it.billingMonth.trim().substringBefore(" ").lowercase()] ?: 0
            }.thenByDescending { it.id }
        )
    }

    // ✅ Available months for the custom picker
    val availableMonths = remember(sortedRecords) {
        sortMonthsDesc(sortedRecords.map { it.billingMonth }.distinct())
    }

    // ✅ Filtered records based on selection
    val filteredRecords = remember(sortedRecords, selectedFilter, customMonths) {
        when (selectedFilter) {
            StatFilter.ALL -> sortedRecords
            StatFilter.CUSTOM -> sortedRecords.filter { it.billingMonth in customMonths }
            else -> sortedRecords.take(selectedFilter.count)
        }
    }

    val recordCount = filteredRecords.size

    val totalElecUnits = remember(filteredRecords) { filteredRecords.sumOf { it.electricityUnits } }
    val totalElecCost = remember(filteredRecords) { filteredRecords.sumOf { it.electricityCost } }
    val totalWaterUnits = remember(filteredRecords) { filteredRecords.sumOf { it.waterUnits } }
    val totalWaterCost = remember(filteredRecords) { filteredRecords.sumOf { it.waterCost } }
    val totalRoomRent = remember(filteredRecords) { filteredRecords.sumOf { it.roomRent } }
    val totalWasteCharge = remember(filteredRecords) { filteredRecords.sumOf { it.wasteCharge } }
    val totalAmountPaid = remember(filteredRecords) { filteredRecords.sumOf { it.amountPaid } }
    val totalOutstanding = remember(filteredRecords) { filteredRecords.sumOf { it.remainingAmount } }
    val totalBillAmount = remember(filteredRecords) { filteredRecords.sumOf { it.totalAmount } }

    val avgMonthlyElecUnits = if (recordCount > 0) totalElecUnits / recordCount else 0.0
    val avgMonthlyBill = if (recordCount > 0) totalBillAmount / recordCount else 0.0

    val highestElecUsage = remember(filteredRecords) {
        filteredRecords.maxOfOrNull { it.electricityUnits } ?: 0.0
    }
    val lowestElecUsage = remember(filteredRecords) {
        filteredRecords.minOfOrNull { it.electricityUnits } ?: 0.0
    }

    val elecChartData = remember(filteredRecords) {
        filteredRecords.take(12).reversed().map { it.billingMonth to it.electricityUnits }
    }
    val waterChartData = remember(filteredRecords) {
        filteredRecords.take(12).reversed().map { it.billingMonth to it.waterUnits }
    }
    val billChartData = remember(filteredRecords) {
        filteredRecords.take(12).reversed().map { it.billingMonth to it.totalAmount }
    }

    // ✅ Display label for the filter (shows custom month count)
    val filterLabel = when (selectedFilter) {
        StatFilter.CUSTOM -> "CUSTOM (${customMonths.size})"
        else -> selectedFilter.title.uppercase()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
    ) {
        RoomSwitcherCard(
            activeRoom = activeRoom,
            allRooms = allRooms,
            currencySymbol = currencySymbol,
            onSwitchClick = { showRoomPicker = true },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ═══════════════════════════════════════════════════════
        //  FILTER CHIPS (scrollable row — fits on small screens)
        // ═══════════════════════════════════════════════════════
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = {
                        if (filter == StatFilter.CUSTOM) {
                            showMonthPicker = true
                        } else {
                            selectedFilter = filter
                        }
                    },
                    label = {
                        Text(
                            text = if (filter == StatFilter.CUSTOM && customMonths.isNotEmpty())
                                "Custom (${customMonths.size})"
                            else filter.title,
                            fontSize = 12.sp
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selectedFilter == filter) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ═══════════════════════════════════════════════════════
        //  OVERVIEW SUMMARY CARD
        // ═══════════════════════════════════════════════════════
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "TOTAL EXPENDITURE ($filterLabel)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = FormatUtils.formatMoney(totalBillAmount, currencySymbol),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Paid",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatMoney(totalAmountPaid, currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StatusPaid
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Outstanding",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatMoney(totalOutstanding, currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (totalOutstanding > 0) StatusUnpaid else StatusPaid
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ═══════════════════════════════════════════════════════
        //  GRID OF STAT CARDS
        // ═══════════════════════════════════════════════════════
        SectionHeader(title = "Averages & Records")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Average Monthly Bill",
                value = FormatUtils.formatMoney(avgMonthlyBill, currencySymbol),
                subtitle = "Across $recordCount records",
                icon = Icons.Default.Receipt,
                iconColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Avg Monthly Elec",
                value = "${FormatUtils.formatUnits(avgMonthlyElecUnits)} units",
                subtitle = "Power usage",
                icon = Icons.Default.Bolt,
                iconColor = ElectricityAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Peak Elec Usage",
                value = "${FormatUtils.formatUnits(highestElecUsage)} units",
                subtitle = "Highest recorded month",
                icon = Icons.Default.TrendingUp,
                iconColor = StatusUnpaid,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Lowest Elec Usage",
                value = "${FormatUtils.formatUnits(lowestElecUsage)} units",
                subtitle = "Lowest recorded month",
                icon = Icons.Default.TrendingDown,
                iconColor = StatusPaid,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ═══════════════════════════════════════════════════════
        //  💰 COST BREAKDOWN DONUT
        // ═══════════════════════════════════════════════════════
        SectionHeader(
            title = "Cost Breakdown",
            subtitle = "Where your money goes"
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CostBreakdownDonut(
                        rent = totalRoomRent,
                        electricity = totalElecCost,
                        water = totalWaterCost,
                        waste = totalWasteCharge,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatMoney(
                                totalRoomRent + totalElecCost + totalWaterCost + totalWasteCharge,
                                currencySymbol
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DonutLegendItem("Rent", totalRoomRent, RentAccent)
                    DonutLegendItem("Electricity", totalElecCost, ElectricityAccent)
                    DonutLegendItem("Water", totalWaterCost, WaterAccent)
                    if (totalWasteCharge > 0) {
                        DonutLegendItem("Waste", totalWasteCharge, WasteAccent)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ═══════════════════════════════════════════════════════
        //  ✅ PAYMENT PROGRESS RING
        // ═══════════════════════════════════════════════════════
        SectionHeader(
            title = "Payment Progress",
            subtitle = "Paid vs outstanding across all bills"
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PaymentProgressRing(
                        paid = totalAmountPaid,
                        total = totalBillAmount,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val pct = if (totalBillAmount > 0)
                            ((totalAmountPaid / totalBillAmount) * 100).toInt()
                        else 0
                        Text(
                            text = "$pct%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "paid",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LegendBar(
                        label = "Paid",
                        value = FormatUtils.formatMoney(totalAmountPaid, currencySymbol),
                        color = StatusPaid
                    )
                    LegendBar(
                        label = "Outstanding",
                        value = FormatUtils.formatMoney(totalOutstanding, currencySymbol),
                        color = if (totalOutstanding > 0) StatusUnpaid else StatusPaid
                    )
                    LegendBar(
                        label = "Total Billed",
                        value = FormatUtils.formatMoney(totalBillAmount, currencySymbol),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ═══════════════════════════════════════════════════════
        //  UTILITY TOTALS BREAKDOWN
        // ═══════════════════════════════════════════════════════
        SectionHeader(title = "Utility & Rent Totals")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatRow(
                    label = "Total Room Rent",
                    value = FormatUtils.formatMoney(totalRoomRent, currencySymbol),
                    icon = Icons.Default.Bed,
                    color = RentAccent
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                StatRow(
                    label = "Total Electricity Cost",
                    value = "${FormatUtils.formatUnits(totalElecUnits)} u = ${FormatUtils.formatMoney(totalElecCost, currencySymbol)}",
                    icon = Icons.Default.Bolt,
                    color = ElectricityAccent
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                StatRow(
                    label = "Total Water Cost",
                    value = "${FormatUtils.formatUnits(totalWaterUnits)} u = ${FormatUtils.formatMoney(totalWaterCost, currencySymbol)}",
                    icon = Icons.Default.WaterDrop,
                    color = WaterAccent
                )
                if (totalWasteCharge > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    StatRow(
                        label = "Total Waste / Minimum",
                        value = FormatUtils.formatMoney(totalWasteCharge, currencySymbol),
                        icon = Icons.Default.Delete,
                        color = WasteAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ═══════════════════════════════════════════════════════
        //  MONTHLY ELECTRICITY TREND
        // ═══════════════════════════════════════════════════════
        SectionHeader(
            title = "Monthly Electricity Trend",
            subtitle = "Electricity units consumption by month"
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SimpleBarChart(
                    data = elecChartData,
                    barColor = ElectricityAccent,
                    unitLabel = "units"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ═══════════════════════════════════════════════════════
        //  MONTHLY WATER TREND
        // ═══════════════════════════════════════════════════════
        SectionHeader(
            title = "Monthly Water Trend",
            subtitle = "Water units consumption by month"
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SimpleBarChart(
                    data = waterChartData,
                    barColor = WaterAccent,
                    unitLabel = "units"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ═══════════════════════════════════════════════════════
        //  MONTHLY BILL TREND
        // ═══════════════════════════════════════════════════════
        SectionHeader(
            title = "Monthly Bill Trend",
            subtitle = "Total amount billed per month"
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SimpleBarChart(
                    data = billChartData,
                    barColor = MaterialTheme.colorScheme.primary,
                    unitLabel = currencySymbol
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ═══════════════════════════════════════════════════════════
    //  📅 MONTH PICKER DIALOG
    // ═══════════════════════════════════════════════════════════
    if (showMonthPicker) {
        MonthPickerDialog(
            availableMonths = availableMonths,
            initiallySelected = customMonths,
            onDismiss = { showMonthPicker = false },
            onConfirm = { picked ->
                customMonths = picked
                if (picked.isEmpty()) {
                    // Nothing picked → fall back to All
                    selectedFilter = StatFilter.ALL
                } else {
                    selectedFilter = StatFilter.CUSTOM
                }
                showMonthPicker = false
            }
        )
    }

    // ═══════════════════════════════════════════════════════════
    //  🏠 ROOM PICKER DIALOG
    // ═══════════════════════════════════════════════════════════
    if (showRoomPicker) {
        RoomPickerDialog(
            activeRoom = activeRoom,
            allRooms = allRooms,
            onSelect = { onSelectRoom(it) },
            onManage = { onManageRooms() },
            onDismiss = { showRoomPicker = false }
        )
    }
}

/* ════════════════════════════════════════════════════════════
   📅 MONTH PICKER DIALOG
   ════════════════════════════════════════════════════════════ */
@Composable
private fun MonthPickerDialog(
    availableMonths: List<String>,
    initiallySelected: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(initiallySelected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Pick Months",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Select any months to include in statistics",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                // Select All / Clear quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { tempSelected = availableMonths.toSet() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Select All", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { tempSelected = emptySet() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Clear", fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Month chips list (scrollable)
                if (availableMonths.isEmpty()) {
                    Text(
                        text = "No records yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableMonths.forEach { month ->
                            val isSelected = month in tempSelected
                            Surface(
                                onClick = {
                                    tempSelected = if (isSelected)
                                        tempSelected - month
                                    else
                                        tempSelected + month
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected)
                                            Icons.Default.CheckCircle
                                        else
                                            Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = month,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected)
                                            FontWeight.SemiBold
                                        else
                                            FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSelected) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (tempSelected.isEmpty())
                        "Show All"
                    else
                        "Apply (${tempSelected.size})",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/* ════════════════════════════════════════════════════════════
   STAT ROW
   ════════════════════════════════════════════════════════════ */
@Composable
private fun StatRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/* ════════════════════════════════════════════════════════════
   💰 COST BREAKDOWN DONUT CHART
   ════════════════════════════════════════════════════════════ */
@Composable
private fun CostBreakdownDonut(
    rent: Double,
    electricity: Double,
    water: Double,
    waste: Double,
    modifier: Modifier = Modifier
) {
    val total = (rent + electricity + water + waste).coerceAtLeast(0.0001)
    val segments = listOf(
        rent to RentAccent,
        electricity to ElectricityAccent,
        water to WaterAccent,
        waste to WasteAccent
    ).filter { it.first > 0 }

    Canvas(modifier = modifier) {
        val strokeW = 22.dp.toPx()
        val radius = size.minDimension / 2f - strokeW / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeW)
        )

        var startAngle = -90f
        segments.forEach { (value, color) ->
            val sweep = (value / total * 360f).toFloat()
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep - 3f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun DonutLegendItem(label: String, value: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = FormatUtils.formatMoney(value, ""),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/* ════════════════════════════════════════════════════════════
   ✅ PAYMENT PROGRESS RING
   ════════════════════════════════════════════════════════════ */
@Composable
private fun PaymentProgressRing(
    paid: Double,
    total: Double,
    modifier: Modifier = Modifier
) {
    val fraction = if (total > 0) (paid / total).coerceIn(0.0, 1.0) else 0.0
    // ✅ Read theme color BEFORE Canvas
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val strokeW = 12.dp.toPx()
        val radius = size.minDimension / 2f - strokeW / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            color = Color.White.copy(alpha = 0.10f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeW)
        )

        if (fraction > 0f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(StatusPaid, primaryColor, StatusPaid)
                ),
                startAngle = -90f,
                sweepAngle = (360f * fraction).toFloat(),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }

        if (fraction > 0.01 && fraction < 0.99) {
            val angleRad = Math.toRadians(-90.0 + 360.0 * fraction)
            val tipX = center.x + (radius * cos(angleRad)).toFloat()
            val tipY = center.y + (radius * sin(angleRad)).toFloat()
            drawCircle(
                color = StatusPaid,
                radius = strokeW * 0.55f,
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = Color.White,
                radius = strokeW * 0.22f,
                center = Offset(tipX, tipY)
            )
        }
    }
}

/* ════════════════════════════════════════════════════════════
   LEGEND BAR
   ════════════════════════════════════════════════════════════ */
@Composable
private fun LegendBar(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}