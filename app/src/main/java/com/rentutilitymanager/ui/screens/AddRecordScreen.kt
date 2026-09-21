package com.rentutilitymanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rentutilitymanager.data.MonthlyRecordEntity
import com.rentutilitymanager.data.RoomEntity
import com.rentutilitymanager.ui.components.LuxeDatePicker
import com.rentutilitymanager.ui.components.LuxeDatePickerMode
import com.rentutilitymanager.ui.components.PaymentStatusBadge
import com.rentutilitymanager.ui.components.RoomPickerDialog
import com.rentutilitymanager.ui.components.propertyTypeIcon
import com.rentutilitymanager.ui.theme.*
import com.rentutilitymanager.util.FormatUtils
import com.rentutilitymanager.viewmodel.RecordFormState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddRecordScreen(
    formState: RecordFormState,
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity> = emptyList(),
    currencySymbol: String,
    onUpdateField: ((RecordFormState) -> RecordFormState) -> Unit,
    onSave: (onSaved: (MonthlyRecordEntity) -> Unit) -> Boolean,
    onCancel: () -> Unit,
    onManageRooms: () -> Unit,
    onSelectRoom: (RoomEntity) -> Unit = {},
    onSwitchRoom: (RoomEntity) -> Unit = onSelectRoom,
    onDownloadPdf: (MonthlyRecordEntity) -> Unit = {},
    onShare: (MonthlyRecordEntity) -> Unit = onDownloadPdf,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var showReceiptPreview by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var savedIsUpdate by remember { mutableStateOf(false) }
    var savedEntity by remember { mutableStateOf<MonthlyRecordEntity?>(null) }

    var showRoomPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var saveAttempted by remember { mutableStateOf(false) }
    var forceShakeTick by remember { mutableStateOf(0) }

    val monthRequester = remember { BringIntoViewRequester() }
    val rentRequester = remember { BringIntoViewRequester() }
    val elecMeterRequester = remember { BringIntoViewRequester() }
    val waterMeterRequester = remember { BringIntoViewRequester() }
    val discountRequester = remember { BringIntoViewRequester() }
    val amountRequester = remember { BringIntoViewRequester() }
    val wasteRequester = remember { BringIntoViewRequester() }
    val remarksRequester = remember { BringIntoViewRequester() }

    val lockedFields = remember {
        mutableStateMapOf(
            "billingMonth" to true,
            "roomRent" to true,
            "prevElec" to true,
            "prevWater" to true,
            "wasteCharge" to true
        )
    }
    val tapCounts = remember { mutableStateMapOf<String, Int>() }


    fun handleLockTap(key: String) {
        val count = (tapCounts[key] ?: 0) + 1
        tapCounts[key] = count
        if (count >= 5) lockedFields[key] = false
    }

    LaunchedEffect(activeRoom?.id) {
        lockedFields["billingMonth"] = true
        lockedFields["roomRent"] = true
        lockedFields["prevElec"] = true
        lockedFields["prevWater"] = true
        lockedFields["wasteCharge"] = true
        tapCounts.clear()
        saveAttempted = false
    }

    var todaySet by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!todaySet && formState.paymentDate.isBlank()) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            onUpdateField { it.copy(paymentDate = today) }
            todaySet = true
        }
    }

    var amountIsUserEdited by remember { mutableStateOf(false) }

    LaunchedEffect(formState.editingRecordId, activeRoom?.id) {
        amountIsUserEdited = formState.editingRecordId != null &&
                (formState.amountPaid.trim().toDoubleOrNull() ?: 0.0) != 0.0
    }

    LaunchedEffect(formState.totalAmount, amountIsUserEdited) {
        if (!amountIsUserEdited && formState.totalAmount > 0) {
            val current = formState.amountPaid.trim().toDoubleOrNull() ?: 0.0
            if (current != formState.totalAmount) {
                onUpdateField { it.copy(amountPaid = formState.totalAmount.toString()) }
            }
        }
    }

    val amountPaidValue = FormatUtils.parseDoubleOrZero(formState.amountPaid)
    val liveDue = (formState.totalAmount - amountPaidValue).coerceAtLeast(0.0)

    val billingMonthError: String? =
        if (saveAttempted && formState.billingMonth.isBlank()) "Billing month is required"
        else null

    val roomRentValue = FormatUtils.parseDoubleOrZero(formState.roomRent)
    val defaultRent = activeRoom?.defaultRent ?: 0.0
    val roomRentError: String? = when {
        formState.roomRent.isBlank() && saveAttempted -> "Room rent is required"
        formState.roomRent.isBlank() -> null
        roomRentValue <= 0.0 -> "Must be greater than zero"
        defaultRent > 0 && roomRentValue < defaultRent ->
            "Below default ${FormatUtils.formatMoney(defaultRent, currencySymbol)}"
        else -> null
    }

    val remarksError: String? = when {
        formState.notes.isBlank() && saveAttempted -> "Remarks is required"
        formState.notes.isBlank() -> null
        else -> null
    }

    val elecRateValue = FormatUtils.parseDoubleOrZero(formState.electricityRate)
    val defaultElecRate = activeRoom?.electricityRate ?: 0.0
    val elecRateError: String? = if (
        defaultElecRate > 0 && elecRateValue < defaultElecRate
    ) "Below default ($currencySymbol$defaultElecRate/u)" else null

    val prevElecValue = FormatUtils.parseDoubleOrZero(formState.previousElectricityReading)
    val currentElecRaw = formState.currentElectricityReading.trim()
    val currentElecValue = FormatUtils.parseDoubleOrZero(currentElecRaw)
    val currentMeterError: String? = when {
        currentElecRaw.isBlank() && saveAttempted -> "Current meter is required"
        currentElecRaw.isBlank() -> null
        prevElecValue > 0 && currentElecValue < prevElecValue ->
            "Must be ≥ ${FormatUtils.formatUnits(prevElecValue)}"
        currentElecValue < 0 -> "Cannot be negative"
        else -> null
    }

    val isDirectWaterMode = formState.waterBillingMode == "DIRECT"
    val waterRateValue = FormatUtils.parseDoubleOrZero(formState.waterRate)
    val defaultWaterRate = activeRoom?.waterRate ?: 0.0
    val waterRateError: String? = if (
        !isDirectWaterMode && defaultWaterRate > 0 && waterRateValue < defaultWaterRate
    ) "Below default ($currencySymbol$defaultWaterRate/u)" else null

    val prevWaterValue = FormatUtils.parseDoubleOrZero(formState.previousWaterReading)
    val currentWaterRaw = formState.currentWaterReading.trim()
    val currentWaterValue = FormatUtils.parseDoubleOrZero(currentWaterRaw)
    val currentWaterError: String? = if (!isDirectWaterMode) when {
        currentWaterRaw.isBlank() -> null
        prevWaterValue > 0 && currentWaterValue < prevWaterValue ->
            "Must be ≥ ${FormatUtils.formatUnits(prevWaterValue)}"
        currentWaterValue < 0 -> "Cannot be negative"
        else -> null
    } else null

    val wasteChargeValue = FormatUtils.parseDoubleOrZero(formState.wasteCharge)
    val defaultWasteCharge = activeRoom?.wasteCharge ?: 0.0
    val wasteChargeError: String? = when {
        formState.wasteCharge.isBlank() -> null
        wasteChargeValue < 0 -> "Cannot be negative"
        defaultWasteCharge > 0 && wasteChargeValue < defaultWasteCharge ->
            "Below default ${FormatUtils.formatMoney(defaultWasteCharge, currencySymbol)}"
        else -> null
    }

    val discountInput = formState.discount.trim()
    val isPercentDiscount = discountInput.endsWith("%")

    val subtotalBeforeDiscount =
        FormatUtils.parseDoubleOrZero(formState.roomRent) +
                formState.electricityCost +
                formState.waterCost +
                FormatUtils.parseDoubleOrZero(formState.wasteCharge) +
                FormatUtils.parseDoubleOrZero(formState.otherCharges)

    val discountError: String? = run {
        if (discountInput.isBlank()) return@run null
        if (isPercentDiscount) {
            val pct = discountInput.dropLast(1).toDoubleOrNull() ?: return@run "Invalid percent"
            if (pct < 0) return@run "Cannot be negative"
            if (pct > 100) return@run "Max 100%"
            null
        } else {
            val amt = discountInput.toDoubleOrNull() ?: return@run "Invalid value"
            if (amt < 0) return@run "Cannot be negative"
            if (subtotalBeforeDiscount > 0 && amt > subtotalBeforeDiscount) {
                return@run "Exceeds subtotal"
            }
            null
        }
    }

    val discountPreview: String? = run {
        if (discountError != null || discountInput.isBlank()) return@run null
        if (isPercentDiscount) {
            val pct = discountInput.dropLast(1).toDoubleOrNull() ?: return@run null
            val amt = subtotalBeforeDiscount * pct / 100.0
            "− ${FormatUtils.formatMoney(amt, currencySymbol)}"
        } else {
            val amt = discountInput.toDoubleOrNull() ?: return@run null
            "− ${FormatUtils.formatMoney(amt, currencySymbol)}"
        }
    }

    val amountPaidError: String? = when {
        formState.amountPaid.isBlank() && saveAttempted -> "Amount paid is required"
        formState.amountPaid.isBlank() -> null
        amountPaidValue < 0 -> "Cannot be negative"
        amountPaidValue > formState.totalAmount && formState.totalAmount > 0 ->
            "Exceeds ${FormatUtils.formatMoney(formState.totalAmount, currencySymbol)}"
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 40.dp)
    ) {
        RoomSwitcherCard(
            activeRoom = activeRoom,
            allRooms = allRooms,
            currencySymbol = currencySymbol,
            onSwitchClick = { showRoomPicker = true }
        )

        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            GradientIconBadge(
                icon = if (formState.editingRecordId != null)
                    Icons.Default.Edit else Icons.Default.Add,
                accent = MaterialTheme.colorScheme.primary,
                size = 36.dp,
                iconSize = 18.dp,
                corner = 10.dp
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (formState.editingRecordId != null)
                    "Edit Monthly Record" else "New Monthly Record",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        LiveBillPreview(
            formState = formState,
            currencySymbol = currencySymbol,
            isDirectWaterMode = isDirectWaterMode
        )

        AnimatedVisibility(visible = formState.validationError != null) {
            Spacer(Modifier.height(10.dp))
            FormAlert(formState.validationError ?: "", StatusUnpaid)
        }
        AnimatedVisibility(
            visible = formState.highUsageWarning != null && formState.validationError == null
        ) {
            Spacer(Modifier.height(10.dp))
            FormAlert(formState.highUsageWarning ?: "", StatusPartiallyPaid)
        }

        Spacer(Modifier.height(22.dp))

        LuxeSectionLabel(text = "Billing Period")
        Spacer(Modifier.height(12.dp))

        LuxeField(
            value = formatBillingMonth(formState.billingMonth),
            onValueChange = { },
            label = "Billing Month *",
            placeholder = "Tap to pick month",
            leadingIcon = Icons.Default.DateRange,
            accent = MaterialTheme.colorScheme.primary,
            isLocked = lockedFields["billingMonth"] == true,
            onLockedTap = {
                handleLockTap("billingMonth")
            },
            errorMessage = billingMonthError,
            forceShakeTick = forceShakeTick,
            readOnlyAsPicker = true,
            onPickerClick = {
                if (lockedFields["billingMonth"] != true) {
                    showMonthPicker = true
                }
            },
            testTag = "billing_month_input",
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(monthRequester)
        )
        LuxeDatePicker(
            visible = showMonthPicker,
            mode = LuxeDatePickerMode.MONTH_YEAR,
            initialValue = formState.billingMonth,
            minYear = 2000,
            maxYear = 2100,
            title = "Select Billing Month",

            onDismiss = {
                showMonthPicker = false
            },

            onValueSelected = { selectedMonth ->
                onUpdateField {
                    it.copy(
                        billingMonth = selectedMonth
                    )
                }

                showMonthPicker = false
            }
        )
        Spacer(Modifier.height(12.dp))

        LuxeField(
            value = formState.roomRent,
            onValueChange = { value -> onUpdateField { it.copy(roomRent = value) } },
            label = "Room Rent ($currencySymbol) *",
            leadingIcon = Icons.Default.Home,
            accent = RentAccent,
            isLocked = lockedFields["roomRent"] == true,
            onLockedTap = { handleLockTap("roomRent") },
            keyboardType = KeyboardType.Number,
            errorMessage = roomRentError,
            forceShakeTick = forceShakeTick,
            testTag = "room_rent_input",
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(rentRequester)
        )

        Spacer(Modifier.height(24.dp))

        MeterSectionHeader(
            title = "Electricity",
            icon = Icons.Default.Bolt,
            accent = ElectricityAccent,
            rateLabel = "$currencySymbol${formState.electricityRate}/u"
        )
        if (elecRateError != null) {
            Spacer(Modifier.height(6.dp))
            InlineError(elecRateError)
        }
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LuxeField(
                value = formState.previousElectricityReading,
                onValueChange = { value ->
                    onUpdateField { it.copy(previousElectricityReading = value) }
                },
                label = "Previous",
                leadingIcon = Icons.Default.KeyboardArrowLeft,
                accent = ElectricityAccent,
                isLocked = lockedFields["prevElec"] == true,
                onLockedTap = { handleLockTap("prevElec") },
                keyboardType = KeyboardType.Number,
                forceShakeTick = forceShakeTick,
                testTag = "prev_electricity_input",
                modifier = Modifier.weight(1f)
            )
            LuxeField(
                value = formState.currentElectricityReading,
                onValueChange = { value ->
                    onUpdateField { it.copy(currentElectricityReading = value) }
                },
                label = "Current *",
                leadingIcon = Icons.Default.KeyboardArrowRight,
                accent = ElectricityAccent,
                keyboardType = KeyboardType.Number,
                errorMessage = currentMeterError,
                forceShakeTick = forceShakeTick,
                testTag = "current_electricity_input",
                modifier = Modifier
                    .weight(1f)
                    .bringIntoViewRequester(elecMeterRequester)
            )
        }

        Spacer(Modifier.height(10.dp))

        AutoCalcPreview(
            label = "${FormatUtils.formatUnits(formState.electricityUnits)} u  ·  " +
                    FormatUtils.formatMoney(formState.electricityCost, currencySymbol)
        )

        Spacer(Modifier.height(24.dp))

        MeterSectionHeader(
            title = "Water",
            icon = Icons.Default.WaterDrop,
            accent = WaterAccent,
            rateLabel = if (isDirectWaterMode)
                "$currencySymbol${formState.directWaterAmount}"
            else
                "$currencySymbol${formState.waterRate}/u"
        )
        if (!isDirectWaterMode && waterRateError != null) {
            Spacer(Modifier.height(6.dp))
            InlineError(waterRateError)
        }
        Spacer(Modifier.height(12.dp))

        if (isDirectWaterMode) {
            FlatWaterCard(
                amount = FormatUtils.parseDoubleOrZero(formState.directWaterAmount),
                currencySymbol = currencySymbol
            )
            Spacer(Modifier.height(10.dp))
            AutoCalcPreview(
                label = FormatUtils.formatMoney(formState.waterCost, currencySymbol)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LuxeField(
                    value = formState.previousWaterReading,
                    onValueChange = { value ->
                        onUpdateField { it.copy(previousWaterReading = value) }
                    },
                    label = "Previous",
                    leadingIcon = Icons.Default.KeyboardArrowLeft,
                    accent = WaterAccent,
                    isLocked = lockedFields["prevWater"] == true,
                    onLockedTap = { handleLockTap("prevWater") },
                    keyboardType = KeyboardType.Number,
                    forceShakeTick = forceShakeTick,
                    testTag = "prev_water_input",
                    modifier = Modifier.weight(1f)
                )
                LuxeField(
                    value = formState.currentWaterReading,
                    onValueChange = { value ->
                        onUpdateField { it.copy(currentWaterReading = value) }
                    },
                    label = "Current",
                    leadingIcon = Icons.Default.KeyboardArrowRight,
                    accent = WaterAccent,
                    keyboardType = KeyboardType.Number,
                    errorMessage = currentWaterError,
                    forceShakeTick = forceShakeTick,
                    testTag = "current_water_input",
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewRequester(waterMeterRequester)
                )
            }

            Spacer(Modifier.height(10.dp))

            AutoCalcPreview(
                label = "${FormatUtils.formatUnits(formState.waterUnits)} u  ·  " +
                        FormatUtils.formatMoney(formState.waterCost, currencySymbol)
            )
        }

        Spacer(Modifier.height(24.dp))

        LuxeSectionLabel(text = "Charges & Discount")
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LuxeField(
                value = formState.wasteCharge,
                onValueChange = { value -> onUpdateField { it.copy(wasteCharge = value) } },
                label = "Waste / Min",
                leadingIcon = Icons.Default.Delete,
                isLocked = lockedFields["wasteCharge"] == true,
                onLockedTap = { handleLockTap("wasteCharge") },
                keyboardType = KeyboardType.Number,
                errorMessage = wasteChargeError,
                forceShakeTick = forceShakeTick,
                testTag = "waste_charge_input",
                modifier = Modifier
                    .weight(1f)
                    .bringIntoViewRequester(wasteRequester)
            )
            LuxeField(
                value = formState.otherCharges,
                onValueChange = { value -> onUpdateField { it.copy(otherCharges = value) } },
                label = "Other ($currencySymbol)",
                leadingIcon = Icons.Default.Add,
                keyboardType = KeyboardType.Number,
                forceShakeTick = forceShakeTick,
                testTag = "other_charges_input",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        LuxeField(
            value = formState.discount,
            onValueChange = { value -> onUpdateField { it.copy(discount = value) } },
            label = "Discount",
            placeholder = "Flat or 10%",
            leadingIcon = Icons.Default.ShoppingCart,
            accent = StatusPaid,
            keyboardType = KeyboardType.Text,
            errorMessage = discountError,
            helperText = discountPreview,
            forceShakeTick = forceShakeTick,
            testTag = "discount_input",
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(discountRequester)
        )

        Spacer(Modifier.height(24.dp))

        LuxeSectionLabel(text = "Payment")
        Spacer(Modifier.height(12.dp))

        LuxeField(
            value = formState.amountPaid,
            onValueChange = { value ->
                amountIsUserEdited = true
                onUpdateField { it.copy(amountPaid = value) }
            },
            label = "Amount Paid ($currencySymbol) *",
            placeholder = FormatUtils.formatMoney(formState.totalAmount, currencySymbol),
            leadingIcon = Icons.Default.Check,
            accent = StatusPaid,
            keyboardType = KeyboardType.Number,
            errorMessage = amountPaidError,
            forceShakeTick = forceShakeTick,
            trailingContent = {
                if (amountIsUserEdited && formState.totalAmount > 0) {
                    Surface(
                        onClick = {
                            amountIsUserEdited = false
                            onUpdateField {
                                it.copy(amountPaid = formState.totalAmount.toString())
                            }
                        },
                        shape = CircleShape,
                        color = StatusPaid.copy(alpha = 0.14f),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Refresh, "Reset",
                                tint = StatusPaid,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            },
            testTag = "amount_paid_input",
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(amountRequester)
        )

        AnimatedVisibility(visible = amountPaidValue > 0 && liveDue > 0) {
            Spacer(Modifier.height(10.dp))
            PartialPaymentBanner(due = liveDue, currencySymbol = currencySymbol)
        }

        Spacer(Modifier.height(16.dp))

        LuxeField(
            value = formState.notes,
            onValueChange = { value -> onUpdateField { it.copy(notes = value) } },
            label = "Remarks *",
            placeholder = "e.g. Rent of Bhadra",
            leadingIcon = Icons.Default.Create,
            singleLine = false,
            minLines = 2,
            errorMessage = remarksError,
            forceShakeTick = forceShakeTick,
            testTag = "notes_input",
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(remarksRequester)
        )

        Spacer(Modifier.height(28.dp))

        LuxePrimaryButton(
            text = if (formState.editingRecordId != null)
                "Review & Update" else "Review & Save",
            icon = Icons.Default.ReceiptLong,
            enabled = true,
            onClick = {
                saveAttempted = true
                forceShakeTick++

                val monthMissing = formState.billingMonth.isBlank()
                val rentNum = FormatUtils.parseDoubleOrZero(formState.roomRent)
                val rentMissing = formState.roomRent.isBlank() || rentNum <= 0.0
                val elecCurrMissing = formState.currentElectricityReading.isBlank()
                val prevE = FormatUtils.parseDoubleOrZero(formState.previousElectricityReading)
                val currE = FormatUtils.parseDoubleOrZero(formState.currentElectricityReading)
                val elecBelowPrev = prevE > 0 && currE < prevE
                val waterCurrMissing = !isDirectWaterMode &&
                        formState.currentWaterReading.isBlank()
                val prevW = FormatUtils.parseDoubleOrZero(formState.previousWaterReading)
                val currW = FormatUtils.parseDoubleOrZero(formState.currentWaterReading)
                val waterBelowPrev = !isDirectWaterMode && prevW > 0 && currW < prevW
                val amountMissing = formState.amountPaid.isBlank()
                val remarksMissing = formState.notes.isBlank()

                val firstErrorRequester = when {
                    monthMissing -> monthRequester
                    rentMissing -> rentRequester
                    elecCurrMissing || elecBelowPrev -> elecMeterRequester
                    waterCurrMissing || waterBelowPrev -> waterMeterRequester
                    wasteChargeError != null -> wasteRequester
                    discountError != null -> discountRequester
                    amountMissing -> amountRequester
                    remarksMissing -> remarksRequester
                    else -> null
                }

                if (firstErrorRequester != null) {
                    scope.launch { firstErrorRequester.bringIntoView() }
                    return@LuxePrimaryButton
                }

                showReceiptPreview = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_record_button")
        )
    }

    if (showReceiptPreview) {
        ReceiptPreviewDialog(
            formState = formState,
            activeRoom = activeRoom,
            currencySymbol = currencySymbol,
            isUpdate = formState.editingRecordId != null,
            onEdit = { showReceiptPreview = false },
            onConfirm = {
                val isUpdate = formState.editingRecordId != null
                onSave { saved ->
                    savedEntity = saved
                    savedIsUpdate = isUpdate
                    showReceiptPreview = false
                    showSuccessDialog = true
                }
            }
        )
    }

    if (showSuccessDialog && savedEntity != null) {
        SuccessReceiptDialog(
            isUpdate = savedIsUpdate,
            record = savedEntity!!,
            roomName = activeRoom?.name ?: "Room",
            tenantName = activeRoom?.tenantName.orEmpty(),
            currencySymbol = currencySymbol,
            onDownloadPdf = { savedEntity?.let { onDownloadPdf(it) } },
            onShare = { savedEntity?.let { onShare(it) } },
            onDone = {
                showSuccessDialog = false
                savedEntity = null
                onCancel()
            }
        )
    }

    if (showMonthPicker) {
        val initialMillis = remember {
            try {
                SimpleDateFormat("MMMM yyyy", Locale.US)
                    .parse(formState.billingMonth.ifBlank { "January 2000" })?.time
            } catch (_: Exception) { null }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showMonthPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatted = SimpleDateFormat("MMMM yyyy", Locale.US)
                                .format(Date(millis))
                            onUpdateField { it.copy(billingMonth = formatted) }
                        }
                        showMonthPicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showMonthPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

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

private fun formatBillingMonth(
    value: String
): String {
    if (value.isBlank()) return ""

    return try {
        val parser =
            SimpleDateFormat(
                "yyyy-MM",
                Locale.US
            )

        parser.isLenient = false

        val formatter =
            SimpleDateFormat(
                "MMMM yyyy",
                Locale.US
            )

        formatter.format(
            parser.parse(value)!!
        )
    } catch (_: Exception) {
        value
    }
}

/* ═══════════════════════════════════════════════════════════════
   BILL DATA — normalizes form or record into a display model
   ═══════════════════════════════════════════════════════════════ */
private data class BillData(
    val roomName: String,
    val tenantName: String,
    val billingMonth: String,
    val prevElec: Double,
    val currElec: Double,
    val elecUnits: Double,
    val elecRate: Double,
    val elecCost: Double,
    val prevWater: Double,
    val currWater: Double,
    val waterUnits: Double,
    val waterRate: Double,
    val waterCost: Double,
    val isDirectWater: Boolean,
    val directWaterAmount: Double,
    val rent: Double,
    val waste: Double,
    val other: Double,
    val discount: Double,
    val total: Double,
    val amountPaid: Double,
    val remaining: Double,
    val paymentStatus: String,
    val notes: String
)

private fun billDataFromForm(
    form: RecordFormState,
    roomName: String,
    tenantName: String
): BillData {
    val subtotal = FormatUtils.parseDoubleOrZero(form.roomRent) +
            form.electricityCost + form.waterCost +
            FormatUtils.parseDoubleOrZero(form.wasteCharge) +
            FormatUtils.parseDoubleOrZero(form.otherCharges)
    val raw = form.discount.trim()
    val discountAmount = when {
        raw.isEmpty() -> 0.0
        raw.endsWith("%") -> subtotal * ((raw.dropLast(1).toDoubleOrNull() ?: 0.0) / 100.0)
        else -> raw.toDoubleOrNull() ?: 0.0
    }
    return BillData(
        roomName = roomName,
        tenantName = tenantName,
        billingMonth = form.billingMonth,
        prevElec = FormatUtils.parseDoubleOrZero(form.previousElectricityReading),
        currElec = FormatUtils.parseDoubleOrZero(form.currentElectricityReading),
        elecUnits = form.electricityUnits,
        elecRate = FormatUtils.parseDoubleOrZero(form.electricityRate),
        elecCost = form.electricityCost,
        prevWater = FormatUtils.parseDoubleOrZero(form.previousWaterReading),
        currWater = FormatUtils.parseDoubleOrZero(form.currentWaterReading),
        waterUnits = form.waterUnits,
        waterRate = FormatUtils.parseDoubleOrZero(form.waterRate),
        waterCost = form.waterCost,
        isDirectWater = form.waterBillingMode == "DIRECT",
        directWaterAmount = FormatUtils.parseDoubleOrZero(form.directWaterAmount),
        rent = FormatUtils.parseDoubleOrZero(form.roomRent),
        waste = FormatUtils.parseDoubleOrZero(form.wasteCharge),
        other = FormatUtils.parseDoubleOrZero(form.otherCharges),
        discount = discountAmount,
        total = form.totalAmount,
        amountPaid = FormatUtils.parseDoubleOrZero(form.amountPaid),
        remaining = form.remainingAmount,
        paymentStatus = form.paymentStatus,
        notes = form.notes
    )
}

private fun billDataFromRecord(
    record: MonthlyRecordEntity,
    roomName: String,
    tenantName: String
): BillData {
    val isDirect = record.waterUnits == 0.0 && record.waterCost > 0
    return BillData(
        roomName = roomName,
        tenantName = tenantName,
        billingMonth = record.billingMonth,
        prevElec = record.previousElectricityReading,
        currElec = record.currentElectricityReading,
        elecUnits = record.electricityUnits,
        elecRate = record.electricityRate,
        elecCost = record.electricityCost,
        prevWater = record.previousWaterReading,
        currWater = record.currentWaterReading,
        waterUnits = record.waterUnits,
        waterRate = record.waterRate,
        waterCost = record.waterCost,
        isDirectWater = isDirect,
        directWaterAmount = if (isDirect) record.waterCost else 0.0,
        rent = record.roomRent,
        waste = record.wasteCharge,
        other = record.otherCharges,
        discount = record.discount,
        total = record.totalAmount,
        amountPaid = record.amountPaid,
        remaining = record.remainingAmount,
        paymentStatus = record.paymentStatus,
        notes = record.notes
    )
}

/* ═══════════════════════════════════════════════════════════════
   RECEIPT PREVIEW — before save
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun ReceiptPreviewDialog(
    formState: RecordFormState,
    activeRoom: RoomEntity?,
    currencySymbol: String,
    isUpdate: Boolean,
    onEdit: () -> Unit,
    onConfirm: () -> Unit
) {
    val bill = remember(formState, activeRoom) {
        billDataFromForm(
            formState,
            activeRoom?.name ?: "—",
            activeRoom?.tenantName.orEmpty()
        )
    }

    Dialog(
        onDismissRequest = onEdit,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            BeautifulCard(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(22.dp),
                elevation = 12.dp
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUpdate) "Review Update" else "Review Bill",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            onClick = onEdit,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close, "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            TraditionalBill(bill = bill, currencySymbol = currencySymbol)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LuxeSecondaryButton(
                            text = "Edit",
                            icon = Icons.Default.Edit,
                            onClick = onEdit,
                            modifier = Modifier.weight(1f)
                        )
                        LuxePrimaryButton(
                            text = if (isUpdate) "Confirm Update" else "Confirm & Save",
                            icon = Icons.Default.Check,
                            onClick = onConfirm,
                            modifier = Modifier.weight(1.4f)
                        )
                    }
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   SUCCESS DIALOG — redesigned
   - Big success tick at top
   - Traditional bill layout
   - Electricity + Water side-by-side
   - PDF + Share on one line, Done below
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun SuccessReceiptDialog(
    isUpdate: Boolean,
    record: MonthlyRecordEntity,
    roomName: String,
    tenantName: String,
    currencySymbol: String,
    onDownloadPdf: () -> Unit,
    onShare: () -> Unit,
    onDone: () -> Unit
) {
    val bill = remember(record, roomName, tenantName) {
        billDataFromRecord(record, roomName, tenantName)
    }

    val scale = remember { Animatable(0.9f) }
    val dialogAlpha = remember { Animatable(0f) }
    val tickScale = remember { Animatable(0.3f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        dialogAlpha.animateTo(1f, tween(200))
        scale.animateTo(1f, spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ))
        tickScale.animateTo(1f, spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ))
        contentAlpha.animateTo(1f, tween(350))
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .scale(scale.value)
                .alpha(dialogAlpha.value),
            contentAlignment = Alignment.Center
        ) {
            BeautifulCard(
                modifier = Modifier.fillMaxSize(),
                accent = StatusPaid,
                shape = RoundedCornerShape(22.dp),
                elevation = 14.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .alpha(contentAlpha.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ═══ Big success tick at top ═══
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(tickScale.value),
                        contentAlignment = Alignment.Center
                    ) {
                        // outer glow ring
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(StatusPaid.copy(alpha = 0.15f))
                        )
                        // inner filled circle
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            StatusPaid,
                                            StatusPaid.copy(alpha = 0.78f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (isUpdate) "Bill Updated" else "Payment Success",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$roomName · ${record.billingMonth}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(14.dp))

                    // ═══ Bill area ═══
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(14.dp)
                        ) {
                            TraditionalBill(bill = bill, currencySymbol = currencySymbol)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ═══ PDF + Share on one line ═══
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SuccessActionButton(
                            icon = Icons.Default.PictureAsPdf,
                            label = "Download PDF",
                            accent = Color(0xFFF43F5E),
                            onClick = onDownloadPdf,
                            modifier = Modifier.weight(1f)
                        )
                        SuccessActionButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            accent = Color(0xFF10B981),
                            onClick = onShare,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // ═══ Done full width ═══
                    LuxePrimaryButton(
                        text = "Done",
                        icon = Icons.Default.Check,
                        accent = StatusPaid,
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessActionButton(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier.height(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   TRADITIONAL BILL — bill-style layout with side-by-side meters
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun TraditionalBill(
    bill: BillData,
    currencySymbol: String
) {
    fun money(v: Double) = FormatUtils.formatMoney(v, currencySymbol)

    val subtotal = bill.rent + bill.elecCost + bill.waterCost + bill.waste + bill.other

    Column(modifier = Modifier.fillMaxWidth()) {

        // ═══ Header ═══
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bill.roomName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (bill.tenantName.isNotBlank()) {
                    Text(
                        bill.tenantName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    bill.billingMonth,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PaymentStatusBadge(status = bill.paymentStatus)
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
        Spacer(Modifier.height(14.dp))

        // ═══ Electricity + Water side-by-side ═══
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MeterCard(
                title = "Electricity",
                icon = Icons.Default.Bolt,
                accent = ElectricityAccent,
                prevReading = bill.prevElec,
                currReading = bill.currElec,
                units = bill.elecUnits,
                rate = bill.elecRate,
                amount = bill.elecCost,
                currencySymbol = currencySymbol,
                isFlat = false,
                modifier = Modifier.weight(1f)
            )
            MeterCard(
                title = "Water",
                icon = Icons.Default.WaterDrop,
                accent = WaterAccent,
                prevReading = bill.prevWater,
                currReading = bill.currWater,
                units = bill.waterUnits,
                rate = bill.waterRate,
                amount = if (bill.isDirectWater) bill.directWaterAmount else bill.waterCost,
                currencySymbol = currencySymbol,
                isFlat = bill.isDirectWater,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
        Spacer(Modifier.height(12.dp))

        // ═══ Charges ═══
        SectionLabel("CHARGES")
        Spacer(Modifier.height(8.dp))

        BillRow("Room Rent", money(bill.rent))
        if (bill.elecCost > 0) BillRow("Electricity", money(bill.elecCost))
        if (bill.waterCost > 0) BillRow("Water", money(bill.waterCost))
        if (bill.waste > 0) BillRow("Waste", money(bill.waste))
        if (bill.other > 0) BillRow("Other", money(bill.other))

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        Spacer(Modifier.height(8.dp))

        BillRow("Subtotal", money(subtotal), bold = true)

        if (bill.discount > 0) {
            BillRow(
                "Discount",
                "− ${money(bill.discount)}",
                valueColor = StatusPaid,
                bold = true
            )
        }

        Spacer(Modifier.height(10.dp))

        // ═══ Total banner ═══
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "TOTAL",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                money(bill.total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(14.dp))

        // ═══ Payment ═══
        SectionLabel("PAYMENT")
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PaymentBox(
                label = "Paid",
                value = money(bill.amountPaid),
                color = StatusPaid,
                modifier = Modifier.weight(1f)
            )
            PaymentBox(
                label = "Due",
                value = money(bill.remaining),
                color = if (bill.remaining > 0) StatusUnpaid else StatusPaid,
                modifier = Modifier.weight(1f)
            )
        }

        // ═══ Remarks ═══
        if (bill.notes.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(Modifier.height(12.dp))
            SectionLabel("REMARKS")
            Spacer(Modifier.height(6.dp))
            Text(
                bill.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        fontSize = 10.sp
    )
}

@Composable
private fun MeterCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    prevReading: Double,
    currReading: Double,
    units: Double,
    rate: Double,
    amount: Double,
    currencySymbol: String,
    isFlat: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon, null,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    letterSpacing = 0.5.sp,
                    fontSize = 10.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            if (isFlat) {
                Text(
                    "Flat charge",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    FormatUtils.formatMoney(amount, currencySymbol),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    "Meter",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
                Text(
                    "${fmt(prevReading)} → ${fmt(currReading)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Units × Rate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
                Text(
                    "${fmt(units)} × $currencySymbol${fmt(rate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    FormatUtils.formatMoney(amount, currencySymbol),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BillRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1
        )
    }
}

@Composable
private fun PaymentBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.30f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun fmt(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()

/* ═══════════════════════════════════════════════════════════════
   SHAKE BOX
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun ShakeBox(
    trigger: Any?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger != null) {
            val sequence = listOf(
                -14f, 14f, -12f, 12f, -8f, 8f, -5f, 5f, -2f, 2f, 0f
            )
            for (value in sequence) {
                offsetX.animateTo(value, tween(35, easing = LinearEasing))
            }
        }
    }

    Box(modifier = modifier.offset(x = offsetX.value.dp)) {
        content()
    }
}

/* ═══════════════════════════════════════════════════════════════
   LUXE FIELD
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun LuxeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    isLocked: Boolean = false,
    onLockedTap: (() -> Unit)? = null,
    errorMessage: String? = null,
    helperText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    testTag: String? = null,
    forceShakeTick: Int = 0,
    readOnlyAsPicker: Boolean = false,
    onPickerClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)
    val hasError = errorMessage != null

    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            hasError -> MaterialTheme.colorScheme.error
            isLocked -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            else -> accent
        },
        animationSpec = tween(280),
        label = "borderColor"
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = when {
            hasError -> 2.5.dp
            isLocked -> 1.dp
            else -> 2.dp
        },
        animationSpec = tween(280),
        label = "borderWidth"
    )
    val animatedFill by animateColorAsState(
        targetValue = when {
            hasError -> lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.error, 0.08f)
            isLocked -> lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outline, 0.04f)
            else -> lerp(MaterialTheme.colorScheme.surface, accent, 0.06f)
        },
        animationSpec = tween(280),
        label = "fill"
    )
    val animatedLabelColor by animateColorAsState(
        targetValue = when {
            hasError -> MaterialTheme.colorScheme.error
            isLocked -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> accent
        },
        animationSpec = tween(280),
        label = "labelColor"
    )
    val animatedElevation by animateDpAsState(
        targetValue = if (hasError) 6.dp else 0.dp,
        animationSpec = tween(280),
        label = "elev"
    )

    val valueColor = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.onSurface

    val shakeTrigger = remember(errorMessage, forceShakeTick) {
        if (hasError) "$errorMessage|$forceShakeTick" else null
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = animatedLabelColor,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        ShakeBox(trigger = shakeTrigger) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = animatedElevation,
                        shape = shape,
                        ambientColor = if (hasError) MaterialTheme.colorScheme.error.copy(alpha = 0.40f)
                        else Color.Transparent,
                        spotColor = if (hasError) MaterialTheme.colorScheme.error.copy(alpha = 0.40f)
                        else Color.Transparent
                    )
                    .clip(shape)
                    .background(animatedFill)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(animatedBorderWidth, animatedBorderColor, shape)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            leadingIcon, null,
                            tint = animatedLabelColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (readOnlyAsPicker) {
                            Text(
                                text = value.ifBlank { placeholder.orEmpty() },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (value.isNotBlank()) FontWeight.SemiBold
                                else FontWeight.Normal,
                                color = if (value.isNotBlank()) valueColor
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            OutlinedTextField(
                                value = value,
                                onValueChange = onValueChange,
                                placeholder = placeholder?.let {
                                    {
                                        Text(
                                            it,
                                            color = MaterialTheme.colorScheme
                                                .onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                readOnly = isLocked,
                                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                                singleLine = singleLine,
                                minLines = minLines,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = valueColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    errorBorderColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    cursorColor = accent
                                )
                            )
                        }
                    }

                    trailingContent?.invoke()

                    Spacer(Modifier.width(6.dp))
                    when {
                        hasError -> Icon(
                            Icons.Default.Warning, "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        isLocked -> Icon(
                            Icons.Default.Lock, "Locked",
                            tint = animatedLabelColor,
                            modifier = Modifier.size(16.dp)
                        )
                        readOnlyAsPicker -> Icon(
                            Icons.Default.ArrowDropDown, "Pick",
                            tint = accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                when {
                    isLocked && !hasError && onLockedTap != null -> Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(shape)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onLockedTap() }
                    )
                    readOnlyAsPicker && !isLocked && onPickerClick != null -> Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(shape)
                            .clickable { onPickerClick() }
                    )
                }
            }
        }

        AnimatedVisibility(visible = hasError || helperText != null) {
            Spacer(Modifier.height(6.dp))
            if (hasError) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (helperText != null) {
                Text(
                    text = helperText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   LIVE BILL PREVIEW
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun LiveBillPreview(
    formState: RecordFormState,
    currencySymbol: String,
    isDirectWaterMode: Boolean
) {
    BeautifulCard(
        modifier = Modifier.fillMaxWidth(),
        accent = MaterialTheme.colorScheme.primary
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL BILL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = FormatUtils.formatMoney(formState.totalAmount, currencySymbol),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                PaymentStatusBadge(status = formState.paymentStatus)
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniStat(
                    label = "Electricity",
                    value = "${FormatUtils.formatUnits(formState.electricityUnits)} u",
                    color = ElectricityAccent
                )
                MiniStat(
                    label = "Water",
                    value = if (isDirectWaterMode) "Flat" else
                        "${FormatUtils.formatUnits(formState.waterUnits)} u",
                    color = WaterAccent
                )
                MiniStat(
                    label = "Due",
                    value = FormatUtils.formatMoney(formState.remainingAmount, currencySymbol),
                    color = if (formState.remainingAmount > 0) StatusUnpaid else StatusPaid
                )
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            letterSpacing = 0.6.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun FormAlert(text: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.30f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                color = accent,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InlineError(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Warning, null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MeterSectionHeader(
    title: String,
    icon: ImageVector,
    accent: Color,
    rateLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GradientIconBadge(
            icon = icon,
            accent = accent,
            size = 32.dp,
            iconSize = 16.dp,
            corner = 10.dp
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = accent.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = rateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun FlatWaterCard(amount: Double, currencySymbol: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = WaterAccent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, WaterAccent.copy(alpha = 0.30f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GradientIconBadge(
                icon = Icons.Default.Payments,
                accent = WaterAccent,
                size = 40.dp,
                iconSize = 20.dp,
                corner = 12.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Fixed monthly water charge",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = FormatUtils.formatMoney(amount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WaterAccent
                )
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = WaterAccent.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "FLAT",
                    style = MaterialTheme.typography.labelSmall,
                    color = WaterAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun AutoCalcPreview(label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info, "Info",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Auto-calculate",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Spacer(Modifier.weight(1f))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PartialPaymentBanner(due: Double, currencySymbol: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = StatusUnpaid.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, StatusUnpaid.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning, null,
                tint = StatusUnpaid,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Remaining due",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = FormatUtils.formatMoney(due, currencySymbol),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = StatusUnpaid
            )
        }
    }
}

@Composable
private fun RoomSwitcherCard(
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity>,
    currencySymbol: String,
    onSwitchClick: () -> Unit
) {
    val hasRoom = activeRoom != null
    val accent = if (hasRoom) MaterialTheme.colorScheme.primary else StatusUnpaid
    val hasTenant = !activeRoom?.tenantName.isNullOrBlank()
    val hasAddress = !activeRoom?.address.isNullOrBlank()
    val hasRent = (activeRoom?.defaultRent ?: 0.0) > 0.0

    BeautifulCard(modifier = Modifier.fillMaxWidth(), accent = accent) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                accent,
                                accent.copy(alpha = 0.55f),
                                accent.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(accent, accent.copy(alpha = 0.70f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = propertyTypeIcon(activeRoom?.propertyType),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeRoom?.name ?: "No Room Selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (hasRoom) {
                        if (hasTenant) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = activeRoom?.tenantName ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (hasAddress) {
                            Text(
                                text = activeRoom?.address ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (hasRent) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = FormatUtils.formatMoney(
                                    activeRoom?.defaultRent ?: 0.0,
                                    currencySymbol
                                ) + " / month",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Add a room to get started",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (allRooms.size > 1) {
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        onClick = onSwitchClick,
                        shape = RoundedCornerShape(14.dp),
                        color = accent,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Change room",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
