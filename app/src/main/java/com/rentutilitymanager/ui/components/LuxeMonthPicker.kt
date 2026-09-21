package com.rentutilitymanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class LuxeDatePickerMode {
    DATE,
    MONTH_YEAR,
    MONTH,
    YEAR
}

@Composable
fun LuxeDatePicker(
    visible: Boolean,
    mode: LuxeDatePickerMode = LuxeDatePickerMode.DATE,
    initialValue: String? = null,
    minYear: Int = 2000,
    maxYear: Int = 2100,
    title: String? = null,
    onDismiss: () -> Unit,
    onValueSelected: (String) -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            when (mode) {

                LuxeDatePickerMode.DATE -> {
                    LuxeExactDatePicker(
                        initialValue = initialValue,
                        minYear = minYear,
                        maxYear = maxYear,
                        title = title ?: "Select Date",
                        onDismiss = onDismiss,
                        onValueSelected = onValueSelected
                    )
                }

                LuxeDatePickerMode.MONTH_YEAR -> {
                    LuxeMonthYearPicker(
                        initialValue = initialValue,
                        minYear = minYear,
                        maxYear = maxYear,
                        title = title ?: "Select Month",
                        onDismiss = onDismiss,
                        onValueSelected = onValueSelected
                    )
                }

                LuxeDatePickerMode.MONTH -> {
                    LuxeMonthOnlyPicker(
                        initialValue = initialValue,
                        title = title ?: "Select Month",
                        onDismiss = onDismiss,
                        onValueSelected = onValueSelected
                    )
                }

                LuxeDatePickerMode.YEAR -> {
                    LuxeYearPicker(
                        initialValue = initialValue,
                        minYear = minYear,
                        maxYear = maxYear,
                        title = title ?: "Select Year",
                        onDismiss = onDismiss,
                        onValueSelected = onValueSelected
                    )
                }
            }
        }
    }
}

/* ============================================================
   EXACT DATE PICKER
   Output: yyyy-MM-dd
   Example: 2026-10-10
   ============================================================ */

@Composable
private fun LuxeExactDatePicker(
    initialValue: String?,
    minYear: Int,
    maxYear: Int,
    title: String,
    onDismiss: () -> Unit,
    onValueSelected: (String) -> Unit
) {
    val today = remember {
        Calendar.getInstance()
    }

    val initialCalendar = remember(initialValue) {
        parseDate(initialValue) ?: Calendar.getInstance()
    }

    var selectedYear by remember {
        mutableIntStateOf(initialCalendar.get(Calendar.YEAR))
    }

    var selectedMonth by remember {
        mutableIntStateOf(initialCalendar.get(Calendar.MONTH))
    }

    var selectedDay by remember {
        mutableIntStateOf(initialCalendar.get(Calendar.DAY_OF_MONTH))
    }

    val years = remember(minYear, maxYear) {
        (minYear..maxYear).toList()
    }

    val months = remember {
        (0..11).toList()
    }

    val days = remember(
        selectedYear,
        selectedMonth
    ) {
        val calendar = Calendar.getInstance()
        calendar.set(
            Calendar.YEAR,
            selectedYear
        )
        calendar.set(
            Calendar.MONTH,
            selectedMonth
        )

        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        (1..maxDay).toList()
    }

    LaunchedEffect(selectedYear, selectedMonth) {
        if (selectedDay > days.last()) {
            selectedDay = days.last()
        }
    }

    Column(
        modifier = Modifier.padding(22.dp)
    ) {

        PickerHeader(
            title = title,
            icon = Icons.Default.Event,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(18.dp))

        PickerLabel("Year")

        PickerSelector(
            selectedText = selectedYear.toString(),
            options = years,
            optionText = { it.toString() },
            onSelected = { selectedYear = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        PickerLabel("Month")

        PickerSelector(
            selectedText = monthName(selectedMonth),
            options = months,
            optionText = { monthName(it) },
            onSelected = { selectedMonth = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        PickerLabel("Day")

        PickerSelector(
            selectedText = selectedDay.toString(),
            options = days,
            optionText = { it.toString() },
            onSelected = { selectedDay = it }
        )

        Spacer(modifier = Modifier.height(22.dp))

        val selectedCalendar = Calendar.getInstance().apply {
            set(
                selectedYear,
                selectedMonth,
                selectedDay,
                12,
                0,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }

        val preview = formatMillis(
            selectedCalendar.timeInMillis,
            "EEEE, MMMM d, yyyy"
        )

        SelectedPreview(
            text = preview
        )

        Spacer(modifier = Modifier.height(20.dp))

        PickerButtons(
            onDismiss = onDismiss,
            onConfirm = {
                onValueSelected(
                    formatMillis(
                        selectedCalendar.timeInMillis,
                        "yyyy-MM-dd"
                    )
                )
            }
        )
    }
}

/* ============================================================
   MONTH + YEAR PICKER
   Output: yyyy-MM
   Example: 2026-10
   ============================================================ */

@Composable
private fun LuxeMonthYearPicker(
    initialValue: String?,
    minYear: Int,
    maxYear: Int,
    title: String,
    onDismiss: () -> Unit,
    onValueSelected: (String) -> Unit
) {
    val initialCalendar = remember(initialValue) {
        parseMonthYear(initialValue) ?: Calendar.getInstance()
    }

    var selectedYear by remember {
        mutableIntStateOf(
            initialCalendar.get(Calendar.YEAR)
        )
    }

    var selectedMonth by remember {
        mutableIntStateOf(
            initialCalendar.get(Calendar.MONTH)
        )
    }

    val years = remember(minYear, maxYear) {
        (minYear..maxYear).toList()
    }

    val months = remember {
        (0..11).toList()
    }

    Column(
        modifier = Modifier.padding(22.dp)
    ) {

        PickerHeader(
            title = title,
            icon = Icons.Default.CalendarMonth,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(20.dp))

        PickerLabel("Year")

        PickerSelector(
            selectedText = selectedYear.toString(),
            options = years,
            optionText = { it.toString() },
            onSelected = {
                selectedYear = it
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PickerLabel("Month")

        PickerSelector(
            selectedText = monthName(selectedMonth),
            options = months,
            optionText = { monthName(it) },
            onSelected = {
                selectedMonth = it
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        SelectedPreview(
            text = "${monthName(selectedMonth)} $selectedYear"
        )

        Spacer(modifier = Modifier.height(20.dp))

        PickerButtons(
            onDismiss = onDismiss,
            onConfirm = {

                val monthNumber = String.format(
                    Locale.US,
                    "%02d",
                    selectedMonth + 1
                )

                val result = "$selectedYear-$monthNumber"

                onValueSelected(result)
            }
        )
    }
}

/* ============================================================
   MONTH ONLY PICKER
   Output: MM
   Example: 10
   ============================================================ */

@Composable
private fun LuxeMonthOnlyPicker(
    initialValue: String?,
    title: String,
    onDismiss: () -> Unit,
    onValueSelected: (String) -> Unit
) {
    val initialMonth = remember(initialValue) {
        parseMonthOnly(initialValue)
    }

    var selectedMonth by remember {
        mutableIntStateOf(initialMonth)
    }

    val months = remember {
        (0..11).toList()
    }

    Column(
        modifier = Modifier.padding(22.dp)
    ) {

        PickerHeader(
            title = title,
            icon = Icons.Default.CalendarMonth,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(20.dp))

        PickerSelector(
            selectedText = monthName(selectedMonth),
            options = months,
            optionText = { monthName(it) },
            onSelected = {
                selectedMonth = it
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        SelectedPreview(
            text = monthName(selectedMonth)
        )

        Spacer(modifier = Modifier.height(20.dp))

        PickerButtons(
            onDismiss = onDismiss,
            onConfirm = {

                val result = String.format(
                    Locale.US,
                    "%02d",
                    selectedMonth + 1
                )

                onValueSelected(result)
            }
        )
    }
}

/* ============================================================
   YEAR ONLY PICKER
   Output: yyyy
   Example: 2026
   ============================================================ */

@Composable
private fun LuxeYearPicker(
    initialValue: String?,
    minYear: Int,
    maxYear: Int,
    title: String,
    onDismiss: () -> Unit,
    onValueSelected: (String) -> Unit
) {
    val currentYear = Calendar.getInstance()
        .get(Calendar.YEAR)

    val initialYear = remember(initialValue) {
        initialValue
            ?.filter { it.isDigit() }
            ?.toIntOrNull()
            ?.takeIf { it in minYear..maxYear }
            ?: currentYear.coerceIn(
                minYear,
                maxYear
            )
    }

    var selectedYear by remember {
        mutableIntStateOf(initialYear)
    }

    val years = remember(minYear, maxYear) {
        (minYear..maxYear).toList()
    }

    Column(
        modifier = Modifier.padding(22.dp)
    ) {

        PickerHeader(
            title = title,
            icon = Icons.Default.Today,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(20.dp))

        PickerSelector(
            selectedText = selectedYear.toString(),
            options = years,
            optionText = { it.toString() },
            onSelected = {
                selectedYear = it
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        SelectedPreview(
            text = selectedYear.toString()
        )

        Spacer(modifier = Modifier.height(20.dp))

        PickerButtons(
            onDismiss = onDismiss,
            onConfirm = {
                onValueSelected(
                    selectedYear.toString()
                )
            }
        )
    }
}

/* ============================================================
   HEADER
   ============================================================ */

@Composable
private fun PickerHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onDismiss
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close"
            )
        }
    }
}

/* ============================================================
   LABEL
   ============================================================ */

@Composable
private fun PickerLabel(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

/* ============================================================
   SELECTOR
   ============================================================ */

@Composable
private fun <T> PickerSelector(
    selectedText: String,
    options: List<T>,
    optionText: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = true
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 15.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "▼",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (expanded) {
            Dialog(
                onDismissRequest = {
                    expanded = false
                }
            ) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Select",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 8.dp
                            )
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(330.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {

                            items(options) { option ->

                                val text =
                                    optionText(option)

                                val isSelected =
                                    text == selectedText

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable {

                                            onSelected(option)
                                            expanded = false
                                        }
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme
                                                    .colorScheme
                                                    .primaryContainer
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .padding(
                                            horizontal = 14.dp,
                                            vertical = 13.dp
                                        ),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = text,
                                        style = MaterialTheme
                                            .typography
                                            .bodyLarge,
                                        fontWeight =
                                            if (isSelected) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isSelected) {
                                        Icon(
                                            imageVector =
                                                Icons.Default.Check,
                                            contentDescription =
                                                "Selected",
                                            tint =
                                                MaterialTheme
                                                    .colorScheme
                                                    .primary
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

/* ============================================================
   PREVIEW
   ============================================================ */

@Composable
private fun SelectedPreview(
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            )
        }
    }
}

/* ============================================================
   BUTTONS
   ============================================================ */

@Composable
private fun PickerButtons(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {

        TextButton(
            onClick = onDismiss
        ) {
            Text("Cancel")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onConfirm
        ) {
            Text(
                text = "Select",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/* ============================================================
   DATE PARSING
   API 24 COMPATIBLE
   ============================================================ */

private fun parseDate(
    value: String?
): Calendar? {
    if (value.isNullOrBlank()) return null

    return try {
        val formatter =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

        formatter.isLenient = false

        val date =
            formatter.parse(value)
                ?: return null

        Calendar.getInstance().apply {
            time = date
        }
    } catch (_: Exception) {
        null
    }
}

private fun parseMonthYear(
    value: String?
): Calendar? {
    if (value.isNullOrBlank()) return null

    return try {
        val formatter =
            SimpleDateFormat(
                "yyyy-MM",
                Locale.US
            )

        formatter.isLenient = false

        val date =
            formatter.parse(value)
                ?: return null

        Calendar.getInstance().apply {
            time = date
        }
    } catch (_: Exception) {

        // Support old format:
        // October 2026

        try {
            val formatter =
                SimpleDateFormat(
                    "MMMM yyyy",
                    Locale.US
                )

            formatter.isLenient = false

            val date =
                formatter.parse(value)
                    ?: return null

            Calendar.getInstance().apply {
                time = date
            }
        } catch (_: Exception) {
            null
        }
    }
}

private fun parseMonthOnly(
    value: String?
): Int {
    if (value.isNullOrBlank()) {
        return Calendar.getInstance()
            .get(Calendar.MONTH)
    }

    return try {

        val number =
            value.toIntOrNull()

        if (number != null) {
            return (number - 1)
                .coerceIn(0, 11)
        }

        val formatter =
            SimpleDateFormat(
                "MMMM",
                Locale.US
            )

        formatter.isLenient = false

        val date =
            formatter.parse(value)
                ?: return Calendar.getInstance()
                    .get(Calendar.MONTH)

        Calendar.getInstance()
            .apply {
                time = date
            }
            .get(Calendar.MONTH)

    } catch (_: Exception) {

        Calendar.getInstance()
            .get(Calendar.MONTH)
    }
}

/* ============================================================
   FORMATTERS
   ============================================================ */

private fun formatMillis(
    millis: Long,
    pattern: String
): String {
    val formatter =
        SimpleDateFormat(
            pattern,
            Locale.US
        )

    formatter.timeZone =
        TimeZone.getDefault()

    return formatter.format(
        Date(millis)
    )
}

private fun monthName(
    monthIndex: Int
): String {
    val formatter =
        SimpleDateFormat(
            "MMMM",
            Locale.US
        )

    val calendar =
        Calendar.getInstance()

    calendar.set(
        Calendar.MONTH,
        monthIndex
    )

    return formatter.format(
        calendar.time
    )
}
