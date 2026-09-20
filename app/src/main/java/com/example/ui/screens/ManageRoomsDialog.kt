
package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.RoomEntity
import com.example.ui.theme.*
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ─────────────────────────────────────────────────────────────
   Property type catalogue
   ───────────────────────────────────────────────────────────── */
private data class PropertyType(
    val key: String,
    val label: String,
    val icon: ImageVector
)

private val PROPERTY_TYPES = listOf(
    PropertyType("ROOM",      "Room",      Icons.Default.MeetingRoom),
    PropertyType("APARTMENT", "Apartment", Icons.Default.Apartment),
    PropertyType("HOUSE",     "House",     Icons.Default.Home),
    PropertyType("SHOP",      "Shop",      Icons.Default.Storefront),
    PropertyType("OFFICE",    "Office",    Icons.Default.Business),
    PropertyType("OTHER",     "Other",     Icons.Default.Category)
)

private data class FormErrors(
    val name: String? = null,
    val rent: String? = null,
    val elecRate: String? = null,
    val startElecUnit: String? = null,
    val waterRate: String? = null,
    val directWaterAmount: String? = null,
    val startWaterUnit: String? = null,
    val wasteCharge: String? = null,
    val tenantName: String? = null,
    val tenantPhone: String? = null,
    val tenantEmail: String? = null,
    val securityDeposit: String? = null
) {
    val isEmpty: Boolean
        get() = listOf(
            name, rent, elecRate, startElecUnit, waterRate, directWaterAmount,
            startWaterUnit, wasteCharge, tenantName, tenantPhone, tenantEmail,
            securityDeposit
        ).all { it == null }
}

private val EMAIL_REGEX = Regex(
    "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$"
)

private fun parseStrict(raw: String): Double? {
    val t = raw.trim()
    if (t.isEmpty()) return null
    return t.toDoubleOrNull()
}

private fun validateForm(
    name: String,
    rent: String,
    elecRate: String,
    startElecUnit: String,
    waterBillingMode: String,
    waterRate: String,
    directWaterAmount: String,
    startWaterUnit: String,
    wasteCharge: String,
    tenantName: String,
    tenantPhone: String,
    tenantEmail: String,
    securityDeposit: String
): FormErrors {
    var nameErr: String? = null
    val n = name.trim()
    if (n.isEmpty()) nameErr = "Name is required"
    else if (n.length < 2) nameErr = "Name must be at least 2 characters"
    else if (n.length > 60) nameErr = "Name must be 60 characters or fewer"

    var rentErr: String? = null
    val r = parseStrict(rent)
    when {
        rent.isBlank() -> rentErr = "Rent is required"
        r == null -> rentErr = "Rent must be a number"
        r < 0 -> rentErr = "Rent cannot be negative"
        r > 10_000_000 -> rentErr = "Rent looks too large"
    }

    var elecErr: String? = null
    val e = parseStrict(elecRate)
    when {
        elecRate.isBlank() -> elecErr = "Electricity rate is required"
        e == null -> elecErr = "Must be a number"
        e < 0 -> elecErr = "Cannot be negative"
        e > 10_000 -> elecErr = "Rate looks too large"
    }

    var startElecErr: String? = null
    if (startElecUnit.isNotBlank()) {
        val sv = parseStrict(startElecUnit)
        when {
            sv == null -> startElecErr = "Must be a number"
            sv < 0 -> startElecErr = "Cannot be negative"
            sv > 10_000_000 -> startElecErr = "Reading looks too large"
        }
    }

    var waterErr: String? = null
    var directErr: String? = null
    if (waterBillingMode == "DIRECT") {
        val d = parseStrict(directWaterAmount)
        when {
            directWaterAmount.isBlank() -> directErr = "Amount is required"
            d == null -> directErr = "Must be a number"
            d < 0 -> directErr = "Cannot be negative"
            d > 1_000_000 -> directErr = "Amount looks too large"
        }
    } else {
        val w = parseStrict(waterRate)
        when {
            waterRate.isBlank() -> waterErr = "Rate is required"
            w == null -> waterErr = "Must be a number"
            w < 0 -> waterErr = "Cannot be negative"
            w > 10_000 -> waterErr = "Rate looks too large"
        }
    }

    var startWaterErr: String? = null
    if (waterBillingMode == "UNIT" && startWaterUnit.isNotBlank()) {
        val sv = parseStrict(startWaterUnit)
        when {
            sv == null -> startWaterErr = "Must be a number"
            sv < 0 -> startWaterErr = "Cannot be negative"
            sv > 10_000_000 -> startWaterErr = "Reading looks too large"
        }
    }

    var wasteErr: String? = null
    if (wasteCharge.isNotBlank()) {
        val wc = parseStrict(wasteCharge)
        when {
            wc == null -> wasteErr = "Must be a number"
            wc < 0 -> wasteErr = "Cannot be negative"
            wc > 1_000_000 -> wasteErr = "Amount looks too large"
        }
    }

    var tenantNameErr: String? = null
    if (tenantName.isNotBlank()) {
        val tn = tenantName.trim()
        when {
            tn.length < 2 -> tenantNameErr = "Name must be at least 2 characters"
            tn.length > 60 -> tenantNameErr = "Name must be 60 characters or fewer"
        }
    }

    var phoneErr: String? = null
    if (tenantPhone.isNotBlank()) {
        val digits = tenantPhone.filter { it.isDigit() }
        when {
            digits.length != 10 -> phoneErr = "Phone must be exactly 10 digits"
            !(digits.startsWith("98") || digits.startsWith("97")) ->
                phoneErr = "Must start with 98 or 97"
        }
    }

    var emailErr: String? = null
    if (tenantEmail.isNotBlank() && !EMAIL_REGEX.matches(tenantEmail.trim())) {
        emailErr = "Enter a valid email"
    }

    var depositErr: String? = null
    if (securityDeposit.isNotBlank()) {
        val sd = parseStrict(securityDeposit)
        when {
            sd == null -> depositErr = "Must be a number"
            sd < 0 -> depositErr = "Cannot be negative"
            sd > 10_000_000 -> depositErr = "Amount looks too large"
        }
    }

    return FormErrors(
        name = nameErr,
        rent = rentErr,
        elecRate = elecErr,
        startElecUnit = startElecErr,
        waterRate = waterErr,
        directWaterAmount = directErr,
        startWaterUnit = startWaterErr,
        wasteCharge = wasteErr,
        tenantName = tenantNameErr,
        tenantPhone = phoneErr,
        tenantEmail = emailErr,
        securityDeposit = depositErr
    )
}

/* ─────────────────────────────────────────────────────────────
   Result / confirm state
   ───────────────────────────────────────────────────────────── */
private sealed interface SaveResult {
    data object Idle : SaveResult
    data class Success(val roomName: String, val wasEdit: Boolean) : SaveResult
    data class Deleted(val roomName: String) : SaveResult
    data class Error(val message: String) : SaveResult
}

private enum class ResultKind { SUCCESS, ERROR, DELETED }

private data class PendingSave(
    val entity: RoomEntity,
    val wasEdit: Boolean,
    val name: String
)

/** Red used for the required-field asterisk. */
private val RequiredMarkRed = Color(0xFFD32F2F)

/* ═══════════════════════════════════════════════════════════════
   REQUIRED FIELD LABEL
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun RequiredFieldLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.4.sp
        )
        Text(
            text = " *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = RequiredMarkRed
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   REUSABLE DATE PICKER FIELD
   ═══════════════════════════════════════════════════════════════ */
private val DATE_DISPLAY_FMT = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
private val DATE_FALLBACK_FMT = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

internal fun formatDateMillis(millis: Long): String = DATE_DISPLAY_FMT.format(Date(millis))

internal fun parseDateToMillis(raw: String): Long? {
    val t = raw.trim()
    if (t.isEmpty()) return null
    return try {
        DATE_DISPLAY_FMT.parse(t)?.time
    } catch (_: Exception) {
        try { DATE_FALLBACK_FMT.parse(t)?.time } catch (_: Exception) { null }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuxeDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Date",
    placeholder: String = "Pick a date",
    leadingIcon: ImageVector = Icons.Default.Event,
    isError: Boolean = false,
    supportingText: String? = null,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val initialMillis = remember(value) {
        parseDateToMillis(value) ?: System.currentTimeMillis()
    }

    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    Box(modifier = modifier) {
        LuxeTextField(
            value = value,
            onValueChange = { /* read-only — picker only */ },
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            isError = isError,
            supportingText = supportingText,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showPicker = true }
        )
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onValueChange(formatDateMillis(millis))
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/* ─────────────────────────────────────────────────────────────
   ManageRoomsDialog
   ───────────────────────────────────────────────────────────── */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRoomsDialog(
    allRooms: List<RoomEntity>,
    activeRoom: RoomEntity?,
    editingRoom: RoomEntity?,
    currencySymbol: String,
    deleteProtectionEnabled: Boolean = false,
    hasDeletePin: Boolean = false,
    onVerifyDeletePin: (String) -> Boolean = { false },
    onSelectRoom: (RoomEntity) -> Unit,
    onSaveRoom: (RoomEntity) -> Unit,
    onDeleteRoom: (RoomEntity) -> Unit,
    onClose: () -> Unit
) {
    var isEditing by remember { mutableStateOf(editingRoom != null) }
    var currentEditRoom by remember { mutableStateOf(editingRoom) }

    val editKey = currentEditRoom?.id
    val isNewRoom = currentEditRoom == null

    var name by remember(editKey) { mutableStateOf(currentEditRoom?.name ?: "") }
    var address by remember(editKey) { mutableStateOf(currentEditRoom?.address ?: "") }
    var propertyType by remember(editKey) {
        mutableStateOf(currentEditRoom?.propertyType ?: "ROOM")
    }
    var rent by remember(editKey) {
        mutableStateOf(currentEditRoom?.defaultRent?.toString() ?: "10000")
    }
    var elecRate by remember(editKey) {
        mutableStateOf(currentEditRoom?.electricityRate?.toString() ?: "15")
    }

    var startElecUnit by remember(editKey) {
        mutableStateOf(
            currentEditRoom?.startElectricityUnit?.takeIf { it > 0.0 }?.toString() ?: ""
        )
    }
    var startWaterUnit by remember(editKey) {
        mutableStateOf(
            currentEditRoom?.startWaterUnit?.takeIf { it > 0.0 }?.toString() ?: ""
        )
    }

    var waterBillingMode by remember(editKey) {
        mutableStateOf(
            currentEditRoom?.waterBillingMode?.takeIf { it.isNotBlank() } ?: "UNIT"
        )
    }
    var waterRate by remember(editKey) {
        mutableStateOf(currentEditRoom?.waterRate?.toString() ?: "30")
    }
    var directWaterAmount by remember(editKey) {
        mutableStateOf(
            currentEditRoom?.directWaterAmount?.takeIf { it > 0.0 }?.toString() ?: ""
        )
    }
    var wasteCharge by remember(editKey) {
        mutableStateOf(currentEditRoom?.wasteCharge?.toString() ?: "100")
    }

    var tenantName by remember(editKey) { mutableStateOf(currentEditRoom?.tenantName ?: "") }
    var tenantPhone by remember(editKey) { mutableStateOf(currentEditRoom?.tenantPhone ?: "") }
    var tenantEmail by remember(editKey) { mutableStateOf(currentEditRoom?.tenantEmail ?: "") }
    var tenantIdNumber by remember(editKey) { mutableStateOf(currentEditRoom?.tenantIdNumber ?: "") }
    var emergencyContact by remember(editKey) { mutableStateOf(currentEditRoom?.emergencyContact ?: "") }
    var moveInDate by remember(editKey) { mutableStateOf(currentEditRoom?.moveInDate ?: "") }
    var securityDeposit by remember(editKey) {
        mutableStateOf(currentEditRoom?.securityDeposit?.toString() ?: "")
    }
    var tenantNotes by remember(editKey) { mutableStateOf(currentEditRoom?.tenantNotes ?: "") }

    var showValidation by remember(editKey) { mutableStateOf(false) }
    val errors = remember(
        showValidation, name, rent, elecRate, startElecUnit, waterBillingMode, waterRate,
        directWaterAmount, startWaterUnit, wasteCharge, tenantName, tenantPhone,
        tenantEmail, securityDeposit
    ) {
        if (!showValidation) FormErrors()
        else validateForm(
            name, rent, elecRate, startElecUnit, waterBillingMode, waterRate,
            directWaterAmount, startWaterUnit, wasteCharge, tenantName, tenantPhone,
            tenantEmail, securityDeposit
        )
    }

    var saveResult by remember { mutableStateOf<SaveResult>(SaveResult.Idle) }
    var pendingDeleteRoom by remember { mutableStateOf<RoomEntity?>(null) }
    var pendingSave by remember { mutableStateOf<PendingSave?>(null) }

    // Auto-focus the Name field whenever the form opens for a new room
    val nameFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isEditing, isNewRoom) {
        if (isEditing && isNewRoom) {
            kotlinx.coroutines.delay(180)
            runCatching { nameFocusRequester.requestFocus() }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 12.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            BeautifulCard(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                elevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // ── Header ─────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LeadingIconTile(
                            icon = when {
                                isEditing && isNewRoom -> Icons.Default.Add
                                isEditing -> Icons.Default.Edit
                                else -> Icons.Default.Domain
                            },
                            accent = MaterialTheme.colorScheme.primary,
                            size = 46.dp,
                            iconSize = 22.dp,
                            corner = 14.dp
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEditing)
                                    (if (!isNewRoom) "Edit Room" else "Add New Room")
                                else "Manage Rooms",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEditing)
                                    "Fields marked * are required"
                                else "${allRooms.size} ${if (allRooms.size == 1) "room" else "rooms"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            onClick = onClose,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close, "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (isEditing) {
                        // ── FORM branch ─────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            LuxeSectionLabel(
                                text = "Property Identity",
                                caption = "Name and type help you identify this room"
                            )

                            Spacer(Modifier.height(12.dp))

                            RequiredFieldLabel("Name")
                            Spacer(Modifier.height(6.dp))
                            LuxeTextField(
                                value = name,
                                onValueChange = { if (it.length <= 60) name = it },
                                label = "",
                                placeholder = "e.g. Room 102, Sunrise Apartment",
                                leadingIcon = Icons.Default.Apartment,
                                isError = errors.name != null,
                                supportingText = errors.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(nameFocusRequester)
                                    .testTag("room_name_input")
                            )

                            Spacer(Modifier.height(10.dp))

                            LuxeTextField(
                                value = address,
                                onValueChange = { if (it.length <= 200) address = it },
                                label = "Address / Description",
                                placeholder = "e.g. Floor 2, East Wing",
                                leadingIcon = Icons.Default.LocationOn,
                                modifier = Modifier.testTag("room_address_input")
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                "Property Type",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.4.sp
                            )
                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PROPERTY_TYPES.forEach { pt ->
                                    PropertyTypeChip(
                                        propertyType = pt,
                                        selected = propertyType == pt.key,
                                        onSelect = { propertyType = pt.key }
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            LuxeSectionLabel(
                                text = "Billing",
                                caption = "How rent and utilities are charged"
                            )

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    RequiredFieldLabel("Rent ($currencySymbol)")
                                    Spacer(Modifier.height(6.dp))
                                    LuxeTextField(
                                        value = rent,
                                        onValueChange = { rent = it },
                                        label = "",
                                        placeholder = "10000",
                                        leadingIcon = Icons.Default.Payments,
                                        keyboardType = KeyboardType.Decimal,
                                        isError = errors.rent != null,
                                        supportingText = errors.rent,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    RequiredFieldLabel("Electricity / unit")
                                    Spacer(Modifier.height(6.dp))
                                    LuxeTextField(
                                        value = elecRate,
                                        onValueChange = { elecRate = it },
                                        label = "",
                                        placeholder = "15",
                                        leadingIcon = Icons.Default.Bolt,
                                        keyboardType = KeyboardType.Decimal,
                                        isError = errors.elecRate != null,
                                        supportingText = errors.elecRate,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            if (isNewRoom) {
                                Spacer(Modifier.height(16.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 10.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Start electricity unit is the meter " +
                                                    "reading at move-in. Used only for the " +
                                                    "first bill. Cannot be changed later.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                LuxeTextField(
                                    value = startElecUnit,
                                    onValueChange = { input ->
                                        startElecUnit = input.filter { c ->
                                            c.isDigit() || c == '.'
                                        }
                                    },
                                    label = "Start electricity unit",
                                    placeholder = "e.g. 1250",
                                    leadingIcon = Icons.Default.Bolt,
                                    keyboardType = KeyboardType.Decimal,
                                    isError = errors.startElecUnit != null,
                                    supportingText = errors.startElecUnit
                                        ?: "Reading on move-in day",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("start_elec_input")
                                )
                            } else {
                                val hasStartElec = currentEditRoom?.startElectricityUnit
                                    ?.let { it > 0.0 } == true
                                val hasStartWater = currentEditRoom?.startWaterUnit
                                    ?.let { it > 0.0 } == true

                                if (hasStartElec || hasStartWater) {
                                    Spacer(Modifier.height(16.dp))

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.06f),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 10.dp
                                            )
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = "START UNITS (LOCKED)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    letterSpacing = 0.8.sp,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Spacer(Modifier.height(6.dp))

                                            if (hasStartElec) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Electricity",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = fmtUnit(
                                                            currentEditRoom?.startElectricityUnit
                                                                ?: 0.0
                                                        ),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }

                                            if (hasStartWater) {
                                                if (hasStartElec) Spacer(Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Water",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = fmtUnit(
                                                            currentEditRoom?.startWaterUnit
                                                                ?: 0.0
                                                        ),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // ── Water billing ─────────────────────
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GradientIconBadge(
                                    icon = Icons.Default.WaterDrop,
                                    accent = WaterAccent,
                                    size = 30.dp,
                                    iconSize = 16.dp,
                                    corner = 9.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Water Billing",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = WaterAccent.copy(alpha = 0.14f),
                                    border = BorderStroke(
                                        1.dp,
                                        WaterAccent.copy(alpha = 0.30f)
                                    )
                                ) {
                                    Text(
                                        text = if (waterBillingMode == "DIRECT")
                                            "DIRECT" else "UNIT-WISE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WaterAccent,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.6.sp,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                SegmentedButton(
                                    selected = waterBillingMode == "UNIT",
                                    onClick = { waterBillingMode = "UNIT" },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                    icon = { Icon(Icons.Default.Speed, null, Modifier.size(16.dp)) },
                                    label = { Text("Unit-wise", fontSize = 12.sp) }
                                )
                                SegmentedButton(
                                    selected = waterBillingMode == "DIRECT",
                                    onClick = {
                                        waterBillingMode = "DIRECT"
                                        startWaterUnit = ""
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                    icon = { Icon(Icons.Default.Payments, null, Modifier.size(16.dp)) },
                                    label = { Text("Direct", fontSize = 12.sp) }
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WaterAccent.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, WaterAccent.copy(alpha = 0.20f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (waterBillingMode) {
                                        "UNIT" -> "Billed by meter reading × water rate per unit"
                                        else -> "Fixed flat amount each month (e.g. ${currencySymbol}300)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WaterAccent,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            if (isNewRoom && waterBillingMode == "UNIT") {
                                LuxeTextField(
                                    value = startWaterUnit,
                                    onValueChange = { input ->
                                        startWaterUnit = input.filter { c ->
                                            c.isDigit() || c == '.'
                                        }
                                    },
                                    label = "Start water unit",
                                    placeholder = "e.g. 100",
                                    leadingIcon = Icons.Default.WaterDrop,
                                    keyboardType = KeyboardType.Decimal,
                                    isError = errors.startWaterUnit != null,
                                    supportingText = errors.startWaterUnit
                                        ?: "Meter reading on move-in day",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("start_water_input")
                                )

                                Spacer(Modifier.height(10.dp))
                            }

                            if (waterBillingMode == "UNIT") {
                                LuxeTextField(
                                    value = waterRate,
                                    onValueChange = { waterRate = it },
                                    label = "Water / unit",
                                    leadingIcon = Icons.Default.WaterDrop,
                                    keyboardType = KeyboardType.Decimal,
                                    isError = errors.waterRate != null,
                                    supportingText = errors.waterRate,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                LuxeTextField(
                                    value = directWaterAmount,
                                    onValueChange = {
                                        directWaterAmount = it.filter { c ->
                                            c.isDigit() || c == '.'
                                        }
                                    },
                                    label = "Water bill ($currencySymbol)",
                                    placeholder = "e.g. 300",
                                    leadingIcon = Icons.Default.Payments,
                                    keyboardType = KeyboardType.Decimal,
                                    isError = errors.directWaterAmount != null,
                                    supportingText = errors.directWaterAmount,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            LuxeTextField(
                                value = wasteCharge,
                                onValueChange = { wasteCharge = it },
                                label = "Waste ($currencySymbol)",
                                leadingIcon = Icons.Default.Delete,
                                keyboardType = KeyboardType.Decimal,
                                isError = errors.wasteCharge != null,
                                supportingText = errors.wasteCharge,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            LuxeSectionLabel(
                                text = "Tenant Details",
                                caption = "Optional — leave blank if vacant"
                            )

                            Spacer(Modifier.height(12.dp))

                            LuxeTextField(
                                value = tenantName,
                                onValueChange = { if (it.length <= 60) tenantName = it },
                                label = "Tenant name",
                                leadingIcon = Icons.Default.Person,
                                isError = errors.tenantName != null,
                                supportingText = errors.tenantName
                            )

                            Spacer(Modifier.height(10.dp))

                            LuxeTextField(
                                value = tenantPhone,
                                onValueChange = { input ->
                                    tenantPhone = input.filter { c ->
                                        c.isDigit() || c == '+' || c == '-' || c == ' ' ||
                                                c == '(' || c == ')'
                                    }
                                },
                                label = "Phone",
                                placeholder = "98XXXXXXXX or 97XXXXXXXX",
                                leadingIcon = Icons.Default.Phone,
                                keyboardType = KeyboardType.Phone,
                                isError = errors.tenantPhone != null,
                                supportingText = errors.tenantPhone
                                    ?: "10 digits, starts with 98 or 97"
                            )

                            Spacer(Modifier.height(10.dp))

                            LuxeTextField(
                                value = tenantEmail,
                                onValueChange = { tenantEmail = it },
                                label = "Email",
                                leadingIcon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email,
                                isError = errors.tenantEmail != null,
                                supportingText = errors.tenantEmail
                            )

                            Spacer(Modifier.height(10.dp))

                            LuxeTextField(
                                value = tenantIdNumber,
                                onValueChange = { if (it.length <= 40) tenantIdNumber = it },
                                label = "ID / Citizenship number",
                                leadingIcon = Icons.Default.Badge
                            )

                            Spacer(Modifier.height(10.dp))

                            LuxeTextField(
                                value = emergencyContact,
                                onValueChange = { emergencyContact = it },
                                label = "Emergency contact",
                                leadingIcon = Icons.Default.ContactPhone
                            )

                            Spacer(Modifier.height(10.dp))

                            LuxeDatePickerField(
                                value = moveInDate,
                                onValueChange = { moveInDate = it },
                                label = "Move-in date",
                                placeholder = "Pick a date",
                                leadingIcon = Icons.Default.Event,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("move_in_date_input")
                            )

                            Spacer(Modifier.height(10.dp))

                            LuxeTextField(
                                value = securityDeposit,
                                onValueChange = { input ->
                                    securityDeposit = input.filter { c ->
                                        c.isDigit() || c == '.'
                                    }
                                },
                                label = "Security deposit ($currencySymbol)",
                                leadingIcon = Icons.Default.Savings,
                                keyboardType = KeyboardType.Decimal,
                                isError = errors.securityDeposit != null,
                                supportingText = errors.securityDeposit
                            )

                            Spacer(Modifier.height(10.dp))

                            LuxeTextField(
                                value = tenantNotes,
                                onValueChange = { if (it.length <= 500) tenantNotes = it },
                                label = "Notes",
                                leadingIcon = Icons.Default.Notes,
                                singleLine = false,
                                minLines = 2
                            )

                            Spacer(Modifier.height(24.dp))
                        }

                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LuxeSecondaryButton(
                                text = "Back",
                                icon = Icons.Default.ArrowBack,
                                onClick = {
                                    isEditing = false
                                    currentEditRoom = null
                                    showValidation = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                            LuxePrimaryButton(
                                text = "Save Room",
                                icon = Icons.Default.Check,
                                onClick = {
                                    showValidation = true
                                    val errs = validateForm(
                                        name, rent, elecRate, startElecUnit,
                                        waterBillingMode, waterRate, directWaterAmount,
                                        startWaterUnit, wasteCharge, tenantName,
                                        tenantPhone, tenantEmail, securityDeposit
                                    )
                                    if (!errs.isEmpty) return@LuxePrimaryButton

                                    val wasEdit = currentEditRoom != null
                                    val savedName = name.trim()

                                    val savedStartElec = if (wasEdit) {
                                        currentEditRoom?.startElectricityUnit ?: 0.0
                                    } else {
                                        FormatUtils.parseDoubleOrZero(startElecUnit)
                                    }
                                    val savedStartWater = if (wasEdit) {
                                        currentEditRoom?.startWaterUnit ?: 0.0
                                    } else {
                                        if (waterBillingMode == "UNIT")
                                            FormatUtils.parseDoubleOrZero(startWaterUnit)
                                        else 0.0
                                    }

                                    val entity = RoomEntity(
                                        id = currentEditRoom?.id ?: 0,
                                        name = savedName,
                                        address = address.trim(),
                                        propertyType = propertyType,
                                        defaultRent = FormatUtils.parseDoubleOrZero(rent),
                                        electricityRate = FormatUtils.parseDoubleOrZero(elecRate),
                                        startElectricityUnit = savedStartElec,
                                        waterRate = FormatUtils.parseDoubleOrZero(waterRate),
                                        wasteCharge = FormatUtils.parseDoubleOrZero(wasteCharge),
                                        waterBillingMode = waterBillingMode,
                                        directWaterAmount =
                                            FormatUtils.parseDoubleOrZero(directWaterAmount),
                                        startWaterUnit = savedStartWater,
                                        tenantName = tenantName.trim(),
                                        tenantPhone = tenantPhone.trim(),
                                        tenantEmail = tenantEmail.trim(),
                                        tenantIdNumber = tenantIdNumber.trim(),
                                        emergencyContact = emergencyContact.trim(),
                                        moveInDate = moveInDate.trim(),
                                        securityDeposit =
                                            FormatUtils.parseDoubleOrZero(securityDeposit),
                                        tenantNotes = tenantNotes.trim()
                                    )

                                    pendingSave = PendingSave(entity, wasEdit, savedName)
                                    saveResult = SaveResult.Success(savedName, wasEdit)
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("save_room_button")
                            )
                        }
                    } else {
                        // ── LIST branch ────────────────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (allRooms.isEmpty()) {
                                EmptyStateCard(
                                    icon = Icons.Default.Domain,
                                    message = "No rooms yet — add your first one below"
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.TopStart),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 4.dp)
                                ) {
                                    items(allRooms, key = { it.id }) { room ->
                                        val isActiveRoom = room.id == activeRoom?.id
                                        ManageRoomListRow(
                                            room = room,
                                            isActive = isActiveRoom,
                                            currencySymbol = currencySymbol,
                                            onSelect = { onSelectRoom(room) },
                                            onEdit = {
                                                currentEditRoom = room
                                                isEditing = true
                                                showValidation = false
                                            },
                                            onDelete = { pendingDeleteRoom = room },
                                            showDelete = allRooms.size > 1 && !isActiveRoom
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        LuxePrimaryButton(
                            text = "Add New Room",
                            icon = Icons.Default.Add,
                            onClick = {
                                currentEditRoom = null
                                name = ""
                                address = ""
                                propertyType = "ROOM"
                                rent = ""
                                elecRate = ""
                                startElecUnit = ""
                                startWaterUnit = ""
                                waterRate = ""
                                wasteCharge = ""
                                waterBillingMode = "UNIT"
                                directWaterAmount = ""
                                tenantName = ""
                                tenantPhone = ""
                                tenantEmail = ""
                                tenantIdNumber = ""
                                emergencyContact = ""
                                moveInDate = ""
                                securityDeposit = ""
                                tenantNotes = ""
                                showValidation = false
                                isEditing = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_new_room_button")
                        )
                    }
                }
            }

            // ── Delete confirm overlay ─────────────────────────
            val deleting = pendingDeleteRoom
            if (deleting != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f))
                        .clickable { /* swallow taps outside card */ }
                )

                DeleteConfirmOverlay(
                    roomName = deleting.name,
                    deleteProtectionEnabled = deleteProtectionEnabled,
                    hasDeletePin = hasDeletePin,
                    onVerifyDeletePin = onVerifyDeletePin,
                    onCancel = { pendingDeleteRoom = null },
                    onConfirm = {
                        val nameToDelete = deleting.name
                        try {
                            onDeleteRoom(deleting)
                            pendingDeleteRoom = null
                            saveResult = SaveResult.Deleted(nameToDelete)
                        } catch (t: Throwable) {
                            pendingDeleteRoom = null
                            saveResult = SaveResult.Error(
                                t.message ?: "Couldn't delete this room."
                            )
                        }
                    }
                )
            }

            // ── Save / delete result overlay ───────────────────
            val result = saveResult
            if (result !is SaveResult.Idle && deleting == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f))
                        .clickable { }
                )

                SaveResultOverlay(
                    kind = when (result) {
                        is SaveResult.Success -> ResultKind.SUCCESS
                        is SaveResult.Deleted -> ResultKind.DELETED
                        is SaveResult.Error   -> ResultKind.ERROR
                        else -> ResultKind.SUCCESS
                    },
                    title = when (result) {
                        is SaveResult.Success ->
                            if (result.wasEdit) "Room Updated" else "Room Added"
                        is SaveResult.Deleted -> "Room Deleted"
                        is SaveResult.Error -> "Couldn't Save"
                        else -> ""
                    },
                    message = when (result) {
                        is SaveResult.Success ->
                            if (result.wasEdit)
                                "\"${result.roomName}\" was updated successfully."
                            else
                                "\"${result.roomName}\" was added successfully."
                        is SaveResult.Deleted ->
                            "\"${result.roomName}\" was deleted permanently."
                        is SaveResult.Error -> result.message
                        else -> ""
                    },
                    primaryLabel = when (result) {
                        is SaveResult.Error -> "Try Again"
                        else -> "OK"
                    },
                    onPrimary = {
                        when (result) {
                            is SaveResult.Success -> {
                                val toSave = pendingSave
                                if (toSave != null) {
                                    try {
                                        onSaveRoom(toSave.entity)
                                    } catch (t: Throwable) {
                                        saveResult = SaveResult.Error(
                                            t.message ?: "Something went wrong while saving."
                                        )
                                        return@SaveResultOverlay
                                    }
                                }
                                pendingSave = null
                                saveResult = SaveResult.Idle
                                isEditing = false
                                currentEditRoom = null
                                showValidation = false
                            }
                            is SaveResult.Deleted -> saveResult = SaveResult.Idle
                            is SaveResult.Error -> saveResult = SaveResult.Idle
                            else -> saveResult = SaveResult.Idle
                        }
                    },
                    secondaryLabel = if (result is SaveResult.Error) "Cancel" else null,
                    onSecondary = if (result is SaveResult.Error) {
                        {
                            saveResult = SaveResult.Idle
                            isEditing = false
                            currentEditRoom = null
                            showValidation = false
                            pendingSave = null
                        }
                    } else null
                )
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   DELETE CONFIRM OVERLAY
   ─────────────────────────────────────────────────────────────
   • PIN mode: numeric field, checked LIVE as you type. A green
     check / red x icon shows instantly — no submit needed.
   • Fallback: type the room name to confirm.
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun DeleteConfirmOverlay(
    roomName: String,
    deleteProtectionEnabled: Boolean,
    hasDeletePin: Boolean,
    onVerifyDeletePin: (String) -> Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val pinEnabled = deleteProtectionEnabled && hasDeletePin

    var typed by remember(roomName, pinEnabled) { mutableStateOf("") }

    // Live check — recomputed on every keystroke.
    val nameMatched = typed.trim() == roomName.trim()
    val pinMatched  = pinEnabled && typed.isNotEmpty() && onVerifyDeletePin(typed)
    val canConfirm  = if (pinEnabled) pinMatched else nameMatched

    // Auto-focus the input when the dialog opens.
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(pinEnabled, roomName) {
        kotlinx.coroutines.delay(120)
        runCatching { focusRequester.requestFocus() }
    }

    val accent = StatusUnpaid

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BeautifulCard(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(22.dp)
                ),
            accent = accent,
            shape = RoundedCornerShape(22.dp),
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accent.copy(alpha = 0.28f),
                                    accent.copy(alpha = 0.08f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Delete Room?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (pinEnabled)
                        "Enter your PIN to permanently delete \"$roomName\". This cannot be undone."
                    else
                        "This will permanently delete \"$roomName\" and all of its bills. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                if (pinEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            LuxeTextField(
                                value = typed,
                                onValueChange = { input ->
                                    typed = input.filter { it.isDigit() }.take(8)
                                },
                                label = "Enter PIN",
                                placeholder = "••••",
                                leadingIcon = Icons.Default.Lock,
                                keyboardType = KeyboardType.NumberPassword,
                                isError = typed.isNotEmpty() && !pinMatched,
                                supportingText = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .testTag("delete_pin_input")
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        // Live status indicator
                        if (typed.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pinMatched) StatusPaid.copy(alpha = 0.14f)
                                        else StatusUnpaid.copy(alpha = 0.14f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (pinMatched)
                                        Icons.Default.CheckCircle
                                    else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (pinMatched) StatusPaid else StatusUnpaid,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    LuxeTextField(
                        value = typed,
                        onValueChange = { typed = it },
                        label = "Type the room name to confirm",
                        placeholder = roomName,
                        leadingIcon = Icons.Default.Edit,
                        isError = typed.isNotEmpty() && !nameMatched,
                        supportingText = when {
                            typed.isEmpty() -> "Type \"$roomName\" exactly"
                            !nameMatched -> "Name doesn't match"
                            else -> "Name matches"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("delete_name_input")
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LuxeSecondaryButton(
                        text = "Cancel",
                        icon = Icons.Default.Close,
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )
                    LuxePrimaryButton(
                        text = "Delete",
                        icon = Icons.Default.Delete,
                        accent = accent,
                        enabled = canConfirm,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   SAVE / DELETE RESULT OVERLAY
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun SaveResultOverlay(
    kind: ResultKind,
    title: String,
    message: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    val accent = when (kind) {
        ResultKind.SUCCESS -> StatusPaid
        ResultKind.DELETED -> StatusUnpaid
        ResultKind.ERROR   -> StatusUnpaid
    }
    val icon = when (kind) {
        ResultKind.SUCCESS -> Icons.Default.Check
        ResultKind.DELETED -> Icons.Default.DeleteForever
        ResultKind.ERROR   -> Icons.Default.Error
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BeautifulCard(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(22.dp)
                ),
            accent = accent,
            shape = RoundedCornerShape(22.dp),
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (kind == ResultKind.SUCCESS) {
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            accent.copy(alpha = 0.14f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            accent.copy(alpha = 0.22f),
                                            accent.copy(alpha = 0.06f)
                                        )
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            accent,
                                            lerp(accent, Color.White, 0.35f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = accent.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.32f))
                    ) {
                        Text(
                            text = "SUCCESS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            fontSize = 10.sp,
                            letterSpacing = 1.4.sp,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 6.dp
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        accent.copy(alpha = 0.28f),
                                        accent.copy(alpha = 0.08f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                if (secondaryLabel != null && onSecondary != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LuxeSecondaryButton(
                            text = secondaryLabel,
                            onClick = onSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        LuxePrimaryButton(
                            text = primaryLabel,
                            icon = if (kind == ResultKind.ERROR)
                                Icons.Default.Refresh else Icons.Default.Check,
                            accent = accent,
                            onClick = onPrimary,
                            modifier = Modifier.weight(1.4f)
                        )
                    }
                } else {
                    LuxePrimaryButton(
                        text = primaryLabel,
                        icon = if (kind == ResultKind.ERROR)
                            Icons.Default.Refresh else Icons.Default.Check,
                        accent = accent,
                        onClick = onPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyTypeChip(
    propertyType: PropertyType,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) accent.copy(alpha = 0.14f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) accent.copy(alpha = 0.65f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = propertyType.icon,
            contentDescription = null,
            tint = if (selected) accent
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = propertyType.label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) accent
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   MANAGE ROOM LIST ROW
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun ManageRoomListRow(
    room: RoomEntity,
    isActive: Boolean,
    currencySymbol: String,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    showDelete: Boolean
) {
    val pt = PROPERTY_TYPES.firstOrNull { it.key == room.propertyType }
        ?: PROPERTY_TYPES.first()

    val accent = MaterialTheme.colorScheme.primary
    val borderColor = if (isActive) accent.copy(alpha = 0.75f)
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accent.copy(alpha = if (isActive) 0.10f else 0.04f),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GradientIconBadge(
                icon = pt.icon,
                accent = accent,
                size = 42.dp,
                iconSize = 20.dp,
                corner = 12.dp,
                topAmount = if (isActive) 0.28f else 0.20f,
                bottomAmount = if (isActive) 0.12f else 0.06f
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = accent,
                            shape = RoundedCornerShape(6.dp),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                letterSpacing = 0.6.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (room.address.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = room.address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                if (room.tenantName.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person, null,
                            tint = accent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = room.tenantName,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = buildString {
                                append("Rent ")
                                append(FormatUtils.formatMoney(room.defaultRent, currencySymbol))
                                append("  ·  Elec ")
                                append(FormatUtils.formatMoney(room.electricityRate, currencySymbol))
                                append("/u  ·  Water ")
                                if (room.waterBillingMode == "DIRECT") {
                                    append(FormatUtils.formatMoney(
                                        room.directWaterAmount, currencySymbol
                                    ))
                                    append(" flat")
                                } else {
                                    append(FormatUtils.formatMoney(room.waterRate, currencySymbol))
                                    append("/u")
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (room.startElectricityUnit > 0.0 || room.startWaterUnit > 0.0) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = buildString {
                                    append("Start: ")
                                    if (room.startElectricityUnit > 0.0) {
                                        append("Elec ${fmtUnit(room.startElectricityUnit)}")
                                    }
                                    if (room.startWaterUnit > 0.0) {
                                        if (room.startElectricityUnit > 0.0) append("  ·  ")
                                        append("Water ${fmtUnit(room.startWaterUnit)}")
                                    }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Column {
                Surface(
                    onClick = onEdit,
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Edit, "Edit Room",
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (showDelete) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        onClick = onDelete,
                        shape = CircleShape,
                        color = StatusUnpaid.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, StatusUnpaid.copy(alpha = 0.22f)),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Delete, "Delete Room",
                                tint = StatusUnpaid,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun fmtUnit(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()