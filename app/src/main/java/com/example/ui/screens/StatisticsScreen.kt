package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import com.example.ui.components.RoomSelectorBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.util.FormatUtils

enum class StatFilter(val title: String, val count: Int) {
    LAST_3("3 Months", 3),
    LAST_6("6 Months", 6),
    LAST_12("12 Months", 12),
    ALL("All History", Int.MAX_VALUE)
}

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
    val scrollState = rememberScrollState()

    val filteredRecords = remember(records, selectedFilter) {
        if (selectedFilter == StatFilter.ALL) records
        else records.take(selectedFilter.count)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
    ) {
        RoomSelectorBar(
            activeRoom = activeRoom,
            allRooms = allRooms,
            onSelectRoom = onSelectRoom,
            onManageRooms = onManageRooms
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Time Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.title, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (selectedFilter == filter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // OVERVIEW SUMMARY CARD
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
                    text = "TOTAL EXPENDITURE (${selectedFilter.title.uppercase()})",
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

        // GRID OF STAT CARDS (Averages & Extremes)
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

        // UTILITY TOTALS BREAKDOWN
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

        // ELECTRICITY USAGE CHART
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

        // WATER USAGE CHART
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
    }
}

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
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
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
