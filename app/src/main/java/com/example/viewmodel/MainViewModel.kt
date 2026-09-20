package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.AppSettings
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import com.example.util.BackupRestoreHelper
import com.example.util.FormatUtils
import com.example.util.PdfExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import androidx.room.util.copy
import com.example.util.NotificationHelper

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class RecordFormState(
    val editingRecordId: Long? = null,
    val roomId: Long = 0L,
    val billingMonth: String = "",
    val previousElectricityReading: String = "",
    val currentElectricityReading: String = "",
    val electricityRate: String = "",
    val previousWaterReading: String = "",
    val currentWaterReading: String = "",
    val waterRate: String = "",
    val waterBillingMode: String = "UNIT",
    val directWaterAmount: String = "0",
    val roomRent: String = "",
    val wasteCharge: String = "",
    val otherCharges: String = "",
    val discount: String = "",
    val amountPaid: String = "",
    val paymentDate: String = "",
    val notes: String = "",
    val electricityUnits: Double = 0.0,
    val electricityCost: Double = 0.0,
    val waterUnits: Double = 0.0,
    val waterCost: Double = 0.0,
    val totalAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentStatus: String = "UNPAID",
    val validationError: String? = null,
    val highUsageWarning: String? = null,
    val previousMonthElecUnits: Double? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val settings = AppSettings(application)
    val repository = AppRepository(database, settings)

    val currencySymbol: StateFlow<String> = settings.currencySymbol
    val themeMode: StateFlow<String> = settings.themeMode

    val paymentRemindersEnabled: StateFlow<Boolean> = settings.paymentRemindersEnabled

    fun setPaymentRemindersEnabled(enabled: Boolean) {
        settings.setPaymentRemindersEnabled(enabled)
    }

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    val allRooms: StateFlow<List<RoomEntity>> = repository.allRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedRoomId: StateFlow<Long> = settings.selectedRoomId

    val activeRoom: StateFlow<RoomEntity?> = combine(allRooms, selectedRoomId) { rooms, currentId ->
        if (rooms.isEmpty()) null
        else rooms.find { it.id == currentId } ?: rooms.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentRoomRecords: StateFlow<List<MonthlyRecordEntity>> =
        activeRoom.flatMapLatest { room ->
            if (room != null) repository.getRecordsForRoom(room.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<MonthlyRecordEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestRecord: StateFlow<MonthlyRecordEntity?> =
        currentRoomRecords.combine(flowOf(Unit)) { records, _ ->
            records.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Form state
    private val _formState = MutableStateFlow(RecordFormState())
    val formState: StateFlow<RecordFormState> = _formState.asStateFlow()

    // Room Dialog State
    private val _editingRoom = MutableStateFlow<RoomEntity?>(null)
    val editingRoom: StateFlow<RoomEntity?> = _editingRoom.asStateFlow()

    private val _showRoomDialog = MutableStateFlow(false)
    val showRoomDialog: StateFlow<Boolean> = _showRoomDialog.asStateFlow()


    // Selected record for detail view
    private val _selectedRecordForDetail = MutableStateFlow<MonthlyRecordEntity?>(null)
    val selectedRecordForDetail: StateFlow<MonthlyRecordEntity?> =
        _selectedRecordForDetail.asStateFlow()

    // ═══════════════════════════════════════════════════════════
    //  ROOM SWITCHING
    // ═══════════════════════════════════════════════════════════

    fun selectRoom(room: RoomEntity) {
        settings.setSelectedRoomId(room.id)
        viewModelScope.launch {
            val form = buildFormForRoom(room.id) ?: return@launch
            _formState.value = recalculate(form)
        }
    }

    /**
     * Alias kept for backward compatibility — same as [selectRoom].
     */
    fun switchRoomForRecord(room: RoomEntity) = selectRoom(room)

    fun setCurrency(symbol: String) {
        settings.setCurrency(symbol)
    }

    fun setThemeMode(mode: String) {
        settings.setThemeMode(mode)
    }

    fun openRecordDetail(record: MonthlyRecordEntity) {
        _selectedRecordForDetail.value = record
    }

    fun closeRecordDetail() {
        _selectedRecordForDetail.value = null
    }

    // ── Room dialog helpers ───────────────────────────────────
    fun openNewRoomDialog() {
        _editingRoom.value = null
        _showRoomDialog.value = true
    }

    fun openEditRoomDialog(room: RoomEntity) {
        _editingRoom.value = room
        _showRoomDialog.value = true
    }

    fun closeRoomDialog() {
        _showRoomDialog.value = false
        _editingRoom.value = null
    }

    fun saveRoom(room: RoomEntity) {
        viewModelScope.launch {
            if (room.id.toInt() == 0) {
                repository.insertRoom(room)
            } else {
                repository.updateRoom(room)
            }
            closeRoomDialog()
        }
    }

    fun deleteRoom(room: RoomEntity) {
        viewModelScope.launch {
            repository.deleteRoom(room)
            val remaining = allRooms.value.filter { it.id != room.id }
            if (remaining.isNotEmpty()) {
                settings.setSelectedRoomId(remaining.first().id)
            } else {
                settings.setSelectedRoomId(0L)
            }
        }
    }

    val deleteProtectionEnabled: StateFlow<Boolean> = settings.deleteProtectionEnabled
    val hasDeletePin: StateFlow<Boolean> = settings.hasDeletePin

    fun setDeleteProtectionEnabled(enabled: Boolean) = settings.setDeleteProtectionEnabled(enabled)
    fun setDeletePin(pin: String) = settings.setDeletePin(pin)
    fun verifyDeletePin(pin: String): Boolean = settings.verifyDeletePin(pin)
    fun clearDeletePin() = settings.clearDeletePin()

    // ═══════════════════════════════════════════════════════════
    //  FORM LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Build a fresh Add-Record form for [roomId], using the room's
     * defaults plus the previous record's meter readings.
     * Returns null if the room doesn't exist.
     */
    private suspend fun buildFormForRoom(roomId: Long): RecordFormState? {
        val room = repository.getRoomByIdSync(roomId) ?: return null
        val latestRec = repository.getLatestRecordForRoomSync(roomId)
        val nextMonthStr = getNextMonthName(latestRec?.billingMonth)

//        val prevElec = latestRec?.currentElectricityReading ?: 0.0
//        val prevWater = latestRec?.currentWaterReading ?: 0.0

        // ★ Prefer the last bill's reading. If there's no bill yet, use the
//   room's move-in start unit.
        val prevElec = latestRec?.currentElectricityReading
            ?: room.startElectricityUnit
        val prevWater = latestRec?.currentWaterReading
            ?: room.startWaterUnit

        return RecordFormState(
            editingRecordId = null,
            roomId = room.id,
            billingMonth = nextMonthStr,
            previousElectricityReading = if (prevElec > 0) prevElec.toString() else "0",
            currentElectricityReading = "",
            electricityRate = room.electricityRate.toString(),
            previousWaterReading = if (prevWater > 0) prevWater.toString() else "0",
            currentWaterReading = "",
            waterRate = room.waterRate.toString(),
            waterBillingMode = room.waterBillingMode,
            directWaterAmount = room.directWaterAmount.toString(),
            roomRent = room.defaultRent.toString(),
            wasteCharge = room.wasteCharge.toString(),
            otherCharges = "0",
            discount = "0",
            amountPaid = "0",
            paymentDate = "",
            notes = "",
            previousMonthElecUnits = latestRec?.electricityUnits
        )
    }

    /**
     * Prepare the Add-Record screen for the given room (or the active
     * room if [targetRoomId] is null). Switches to tab 1.
     */
    fun prepareAddRecord(targetRoomId: Long? = null) {
        viewModelScope.launch {
            val room = if (targetRoomId != null) {
                repository.getRoomByIdSync(targetRoomId)
            } else {
                activeRoom.value ?: allRooms.value.firstOrNull()
            } ?: return@launch

            val form = buildFormForRoom(room.id) ?: return@launch
            _formState.value = recalculate(form)
            _selectedTab.value = 1
        }
    }

    fun prepareEditRecord(record: MonthlyRecordEntity) {
        val room = allRooms.value.firstOrNull { it.id == record.roomId }
        val mode = room?.waterBillingMode ?: "UNIT"

        _formState.value = recalculate(RecordFormState(
            editingRecordId = record.id,
            roomId = record.roomId,
            billingMonth = record.billingMonth,
            previousElectricityReading = record.previousElectricityReading.toString(),
            currentElectricityReading = record.currentElectricityReading.toString(),
            electricityRate = record.electricityRate.toString(),
            previousWaterReading = record.previousWaterReading.toString(),
            currentWaterReading = record.currentWaterReading.toString(),
            waterRate = record.waterRate.toString(),
            waterBillingMode = mode,
            directWaterAmount = if (mode == "DIRECT")
                record.waterCost.toString()
            else
                (room?.directWaterAmount ?: 0.0).toString(),
            roomRent = record.roomRent.toString(),
            wasteCharge = record.wasteCharge.toString(),
            otherCharges = record.otherCharges.toString(),
            discount = record.discount.toString(),
            amountPaid = record.amountPaid.toString(),
            paymentDate = record.paymentDate,
            notes = record.notes
        ))
        _selectedRecordForDetail.value = null
        _selectedTab.value = 1
    }

    fun duplicateRecord(record: MonthlyRecordEntity) {
        viewModelScope.launch {
            val room = allRooms.value.firstOrNull { it.id == record.roomId }
            val mode = room?.waterBillingMode ?: "UNIT"
            val nextMonthStr = getNextMonthName(record.billingMonth)

            val form = RecordFormState(
                editingRecordId = null,
                roomId = record.roomId,
                billingMonth = nextMonthStr,
                previousElectricityReading = record.currentElectricityReading.toString(),
                currentElectricityReading = "",
                electricityRate = record.electricityRate.toString(),
                previousWaterReading = record.currentWaterReading.toString(),
                currentWaterReading = "",
                waterRate = record.waterRate.toString(),
                waterBillingMode = mode,
                directWaterAmount = if (mode == "DIRECT")
                    record.waterCost.toString()
                else
                    (room?.directWaterAmount ?: 0.0).toString(),
                roomRent = record.roomRent.toString(),
                wasteCharge = record.wasteCharge.toString(),
                otherCharges = "0",
                discount = "0",
                amountPaid = "0",
                paymentDate = "",
                notes = "",
                previousMonthElecUnits = record.electricityUnits
            )
            _formState.value = recalculate(form)
            _selectedTab.value = 1
        }
    }

    fun updateFormField(transform: (RecordFormState) -> RecordFormState) {
        _formState.value = recalculate(transform(_formState.value))
    }

    // ═══════════════════════════════════════════════════════════
    //  CALCULATIONS
    // ═══════════════════════════════════════════════════════════

    private fun parseDiscount(raw: String, subtotal: Double): Double {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return 0.0
        return if (trimmed.endsWith("%")) {
            val pct = trimmed.dropLast(1).toDoubleOrNull() ?: 0.0
            subtotal * (pct / 100.0)
        } else {
            trimmed.toDoubleOrNull() ?: 0.0
        }
    }

    private fun recalculate(state: RecordFormState): RecordFormState {
        val prevElec = FormatUtils.parseDoubleOrZero(state.previousElectricityReading)
        val currElec = FormatUtils.parseDoubleOrZero(state.currentElectricityReading)
        val elecRate = FormatUtils.parseDoubleOrZero(state.electricityRate)

        val prevWater = FormatUtils.parseDoubleOrZero(state.previousWaterReading)
        val currWater = FormatUtils.parseDoubleOrZero(state.currentWaterReading)
        val waterRate = FormatUtils.parseDoubleOrZero(state.waterRate)
        val directWater = FormatUtils.parseDoubleOrZero(state.directWaterAmount)

        val rent = FormatUtils.parseDoubleOrZero(state.roomRent)
        val waste = FormatUtils.parseDoubleOrZero(state.wasteCharge)
        val other = FormatUtils.parseDoubleOrZero(state.otherCharges)
        val paid = FormatUtils.parseDoubleOrZero(state.amountPaid)

        val elecUnits = if (currElec >= prevElec) currElec - prevElec else 0.0
        val elecCost = elecUnits * elecRate

        val isDirect = state.waterBillingMode == "DIRECT"
        val rawWaterUnits = if (currWater >= prevWater) currWater - prevWater else 0.0
        val waterUnits = if (isDirect) 0.0 else rawWaterUnits
        val waterCost = if (isDirect) directWater else rawWaterUnits * waterRate

        val subtotal = rent + elecCost + waterCost + waste + other
        val discountAmount = parseDiscount(state.discount, subtotal)
        val total = subtotal - discountAmount
        val safeTotal = if (total > 0) total else 0.0
        val remaining = if (safeTotal > paid) safeTotal - paid else 0.0

        val status = when {
            paid >= safeTotal && safeTotal > 0 -> "PAID"
            paid > 0 -> "PARTIALLY_PAID"
            else -> "UNPAID"
        }

        var error: String? = null
        if (state.currentElectricityReading.isNotBlank() && currElec < prevElec) {
            error = "Current electricity meter (${currElec}) cannot be lower than previous (${prevElec})."
        } else if (!isDirect && state.currentWaterReading.isNotBlank() && currWater < prevWater) {
            error = "Current water meter (${currWater}) cannot be lower than previous (${prevWater})."
        } else if (elecRate < 0 || rent < 0 || waste < 0 || other < 0 || discountAmount < 0 || paid < 0) {
            error = "Negative values are not allowed."
        } else if (!isDirect && waterRate < 0) {
            error = "Water rate cannot be negative."
        } else if (isDirect && directWater < 0) {
            error = "Direct water amount cannot be negative."
        } else if (state.billingMonth.isBlank()) {
            error = "Billing month cannot be empty."
        } else if (discountAmount > subtotal && subtotal > 0) {
            error = "Discount cannot be greater than the subtotal."
        }

        var warning: String? = null
        val prevUsage = state.previousMonthElecUnits
        if (prevUsage != null && prevUsage > 0 && elecUnits > (prevUsage * 2.0)) {
            warning = "Electricity usage (${FormatUtils.formatUnits(elecUnits)} units) is more than 2x previous month (" +
                    "${FormatUtils.formatUnits(prevUsage)} units). Please verify meter reading."
        }

        return state.copy(
            electricityUnits = elecUnits,
            electricityCost = elecCost,
            waterUnits = waterUnits,
            waterCost = waterCost,
            totalAmount = safeTotal,
            remainingAmount = remaining,
            paymentStatus = status,
            validationError = error,
            highUsageWarning = warning
        )
    }

    // ═══════════════════════════════════════════════════════════
    //  SAVE / DELETE RECORD
    // ═══════════════════════════════════════════════════════════

    fun saveCurrentRecord(
        onSaved: ((MonthlyRecordEntity) -> Unit)? = null   // ★ NEW
    ): Boolean {
        val form = _formState.value
        if (form.validationError != null) return false
        if (form.billingMonth.isBlank()) return false

        viewModelScope.launch {
            val subtotalForDiscount = FormatUtils.parseDoubleOrZero(form.roomRent) +
                    form.electricityCost + form.waterCost +
                    FormatUtils.parseDoubleOrZero(form.wasteCharge) +
                    FormatUtils.parseDoubleOrZero(form.otherCharges)
            val discountAmount = parseDiscount(form.discount, subtotalForDiscount)

            val isDirect = form.waterBillingMode == "DIRECT"
            val storedWaterRate = if (isDirect)
                FormatUtils.parseDoubleOrZero(form.directWaterAmount)
            else
                FormatUtils.parseDoubleOrZero(form.waterRate)

            val entity = MonthlyRecordEntity(
                id = form.editingRecordId ?: 0L,
                roomId = form.roomId,
                billingMonth = form.billingMonth.trim(),
                previousElectricityReading = FormatUtils.parseDoubleOrZero(form.previousElectricityReading),
                currentElectricityReading = FormatUtils.parseDoubleOrZero(form.currentElectricityReading),
                electricityUnits = form.electricityUnits,
                electricityRate = FormatUtils.parseDoubleOrZero(form.electricityRate),
                electricityCost = form.electricityCost,
                previousWaterReading = FormatUtils.parseDoubleOrZero(form.previousWaterReading),
                currentWaterReading = FormatUtils.parseDoubleOrZero(form.currentWaterReading),
                waterUnits = form.waterUnits,
                waterRate = storedWaterRate,
                waterCost = form.waterCost,
                roomRent = FormatUtils.parseDoubleOrZero(form.roomRent),
                wasteCharge = FormatUtils.parseDoubleOrZero(form.wasteCharge),
                otherCharges = FormatUtils.parseDoubleOrZero(form.otherCharges),
                discount = discountAmount,
                totalAmount = form.totalAmount,
                amountPaid = FormatUtils.parseDoubleOrZero(form.amountPaid),
                remainingAmount = form.remainingAmount,
                paymentDate = form.paymentDate.trim(),
                paymentStatus = form.paymentStatus,
                notes = form.notes.trim(),
                updatedAt = System.currentTimeMillis()
            )

            // ★ Capture the DB id
            val savedId: Long = if (form.editingRecordId != null && form.editingRecordId > 0) {
                repository.updateRecord(entity)
                entity.id
            } else {
                repository.insertRecord(entity)
            }

            val saved = entity.copy(id = savedId)
            notifyBillSaved(saved)
            _selectedRecordForDetail.value = null

            // ★★ THE CRITICAL LINE — hand the record back to the screen
            onSaved?.invoke(saved)
        }
        return true
    }
    private fun notifyBillSaved(entity: MonthlyRecordEntity) {
        val ctx = getApplication<Application>()
        val room = allRooms.value.firstOrNull { it.id == entity.roomId }
        val symbol = currencySymbol.value
        val paid = entity.amountPaid
        val total = entity.totalAmount
        val due = entity.remainingAmount

        when {
            paid <= 0.0 -> NotificationHelper.show(
                ctx, 3001, NotificationHelper.CHANNEL_SUCCESS,
                title = "Bill saved — ${room?.name ?: "Room"}",
                body = "${entity.billingMonth} · Total ${FormatUtils.formatMoney(total, symbol)}",
                bigText = "Bill for ${entity.billingMonth} saved. Amount is still unpaid."
            )
            due <= 0.0 -> NotificationHelper.show(
                ctx, 3002, NotificationHelper.CHANNEL_SUCCESS,
                title = "Payment received ✅",
                body = "Paid in full — ${FormatUtils.formatMoney(paid, symbol)}",
                bigText = "${room?.name ?: "Room"} · ${entity.billingMonth}\n" +
                        "Paid in full: ${FormatUtils.formatMoney(paid, symbol)}"
            )
            else -> NotificationHelper.show(
                ctx, 3003, NotificationHelper.CHANNEL_SUCCESS,
                title = "Partial payment recorded",
                body = "Paid ${FormatUtils.formatMoney(paid, symbol)} · Due ${FormatUtils.formatMoney(due, symbol)}",
                bigText = "${room?.name ?: "Room"} · ${entity.billingMonth}\n" +
                        "Paid: ${FormatUtils.formatMoney(paid, symbol)}\n" +
                        "Remaining due: ${FormatUtils.formatMoney(due, symbol)}"
            )
        }
    }

    fun deleteRecord(record: MonthlyRecordEntity) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            if (_selectedRecordForDetail.value?.id == record.id) {
                _selectedRecordForDetail.value = null
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  EXPORT / BACKUP
    // ═══════════════════════════════════════════════════════════

    fun exportPdf(context: Context, record: MonthlyRecordEntity) {
        val room = allRooms.value.find { it.id == record.roomId } ?: activeRoom.value
        PdfExporter.generateAndShareBillPdf(context, room, record, currencySymbol.value)
    }

    fun exportAllJson(): String {
        return BackupRestoreHelper.exportToJson(allRooms.value, allRecords.value)
    }

    fun restoreFromJson(jsonString: String): Boolean {
        return try {
            val backup = BackupRestoreHelper.importFromJson(jsonString)
            viewModelScope.launch {
                for (room in backup.rooms) {
                    repository.insertRoom(room.copy(id = 0))
                }
                for (rec in backup.records) {
                    repository.insertRecord(rec.copy(id = 0))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getNextMonthName(lastBillingMonth: String?): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        if (lastBillingMonth.isNullOrBlank()) {
            return sdf.format(Date())
        }
        return try {
            val date = sdf.parse(lastBillingMonth)
            if (date != null) {
                val cal = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.MONTH, 1)
                }
                sdf.format(cal.time)
            } else {
                sdf.format(Date())
            }
        } catch (e: Exception) {
            sdf.format(Date())
        }
    }

    fun exportPdf(context: Context, records: List<MonthlyRecordEntity>) {
        if (records.isEmpty()) return

        val room = activeRoom.value
        val symbol = currencySymbol.value
        val generatedAt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())

        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val tableWidth = pageWidth - 2 * margin

        // ── Paints ───────────────────────────────────────────────
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f; isFakeBoldText = true; color = Color.rgb(17, 24, 39)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f; color = Color.rgb(107, 114, 128)
        }
        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(37, 99, 235)
        }
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; isFakeBoldText = true; color = Color.WHITE
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; color = Color.rgb(31, 41, 55)
        }
        val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = Color.rgb(17, 24, 39)
        }
        val rowAltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(248, 250, 252)
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240); strokeWidth = 0.5f
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; color = Color.rgb(148, 163, 184)
        }
        val paidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = Color.rgb(22, 163, 74)
        }
        val partialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = Color.rgb(217, 119, 6)
        }
        val unpaidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = Color.rgb(220, 38, 38)
        }
        val discountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = Color.rgb(217, 119, 6)
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; color = Color.rgb(148, 163, 184)
        }
        val receiptTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; isFakeBoldText = true; color = Color.WHITE
        }
        val receiptBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(249, 250, 251)
        }
        val receiptHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(37, 99, 235)
        }
        val receiptLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; color = Color.rgb(71, 85, 105)
        }
        val receiptValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; isFakeBoldText = true; color = Color.rgb(17, 24, 39)
        }
        val receiptTotalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10.5f; isFakeBoldText = true; color = Color.rgb(17, 24, 39)
        }

        // ── Column percentages ───────────────────────────────────
        val columns = listOf(
            "Month" to 0.095f,
            "Status" to 0.075f,
            "Rent" to 0.090f,
            "Elec" to 0.085f,
            "Water" to 0.085f,
            "Waste" to 0.075f,
            "Disc." to 0.085f,
            "Total" to 0.100f,
            "Paid" to 0.095f,
            "Due" to 0.095f,
            "Description" to 0.120f
        )
        val colWidths = columns.map { it.second * tableWidth }

        // ★ Integer-aligned boundaries — no fractional drift
        val colLeft = IntArray(columns.size)
        val colRight = IntArray(columns.size)
        var acc = margin.toInt()
        for (i in columns.indices) {
            colLeft[i] = acc
            val next = if (i == columns.lastIndex) (pageWidth - margin).toInt()
            else acc + colWidths[i].toInt()
            colRight[i] = next
            acc = next
        }
        val rightAligned = setOf(2, 3, 4, 5, 6, 7, 8, 9)

        // ── Amount formatting ────────────────────────────────────
        fun fmtAmount(v: Double): String {
            val rounded = Math.round(v * 100.0) / 100.0
            return if (rounded == rounded.toLong().toDouble()) {
                String.format(Locale.US, "%,.0f", rounded)
            } else {
                String.format(Locale.US, "%,.2f", rounded)
            }
        }
        fun num(v: Double) = fmtAmount(v)
        fun money(v: Double) = "$symbol ${fmtAmount(v)}"

        // ── Month formatting ─────────────────────────────────────
        val inMonthFmt = SimpleDateFormat("MMMM yyyy", Locale.US)
        val outMonthFmt = SimpleDateFormat("MM-yyyy", Locale.US)
        fun shortMonth(raw: String): String =
            try { inMonthFmt.parse(raw)?.let { outMonthFmt.format(it) } ?: raw }
            catch (_: Exception) { raw }

        val totalBilled = records.sumOf { it.totalAmount }
        val totalPaid = records.sumOf { it.amountPaid }
        val totalDue = records.sumOf { it.remainingAmount }
        val totalDiscount = records.sumOf { it.discount }
        val grossBilled = totalBilled + totalDiscount

        var pageNumber = 1

        fun newPage(): PdfDocument.Page = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )

        var page = newPage()
        var canvas = page.canvas
        var y = margin.toInt().toFloat()

        // ── Header ───────────────────────────────────────────────
        canvas.drawText("Rent History Report", margin, y + 16f, titlePaint)
        y += 22f

        val roomLine = buildString {
            append(room?.name ?: "All Rooms")
            if (!room?.address.isNullOrBlank()) append("  •  ${room?.address}")
        }
        canvas.drawText(roomLine, margin, y + 10f, subtitlePaint)
        y += 15f
        canvas.drawText(
            "Generated: $generatedAt  •  ${records.size} record(s)  •  Amounts in $symbol",
            margin, y + 10f, subtitlePaint
        )
        y += 20f
        canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
        y += 16f

        // ── Summary cards ────────────────────────────────────────
        val gap = 8f
        val boxH = 44f

        if (totalDiscount > 0) {
            val boxW = (tableWidth - 3 * gap) / 4f
            drawSummaryCard(canvas, margin, y, boxW, boxH,
                "Total Billed", money(totalBilled), Color.rgb(37, 99, 235))
            drawSummaryCard(canvas, margin + boxW + gap, y, boxW, boxH,
                "Discount", "- ${money(totalDiscount)}", Color.rgb(217, 119, 6))
            drawSummaryCard(canvas, margin + 2 * (boxW + gap), y, boxW, boxH,
                "Total Paid", money(totalPaid), Color.rgb(22, 163, 74))
            drawSummaryCard(canvas, margin + 3 * (boxW + gap), y, boxW, boxH,
                "Total Due", money(totalDue), Color.rgb(220, 38, 38))
        } else {
            val boxW = (tableWidth - 2 * gap) / 3f
            drawSummaryCard(canvas, margin, y, boxW, boxH,
                "Total Billed", money(totalBilled), Color.rgb(37, 99, 235))
            drawSummaryCard(canvas, margin + boxW + gap, y, boxW, boxH,
                "Total Paid", money(totalPaid), Color.rgb(22, 163, 74))
            drawSummaryCard(canvas, margin + 2 * (boxW + gap), y, boxW, boxH,
                "Total Due", money(totalDue), Color.rgb(220, 38, 38))
        }
        y += boxH + 16f

        val headerH = 22f
        val rowH = 20f

        fun drawTableHeader() {
            val top = Math.round(y).toFloat()
            val bottom = Math.round(y + headerH).toFloat()
            canvas.drawRect(margin, top, pageWidth - margin, bottom, headerBgPaint)
            columns.forEachIndexed { i, (label, _) ->
                val x = if (i in rightAligned)
                    Math.round(colRight[i] - 5f - headerTextPaint.measureText(label)).toFloat()
                else (colLeft[i] + 5).toFloat()
                canvas.drawText(label, x, top + 15f, headerTextPaint)
            }
            y = bottom
        }
        drawTableHeader()

        fun drawFooter() {
            val fy = pageHeight - 20f
            canvas.drawText("Room Rent & Utility  ‣  By Biplap Neupane", margin, fy, footerPaint)
            val label = "Page $pageNumber"
            canvas.drawText(
                label, pageWidth - margin - footerPaint.measureText(label), fy, footerPaint
            )
        }

        // ── Data rows ────────────────────────────────────────────
        var rowIndex = 0
        for (r in records) {
            if (y + rowH > pageHeight - 30f) {
                drawFooter()
                document.finishPage(page)
                pageNumber++
                page = newPage()
                canvas = page.canvas
                y = margin.toInt().toFloat()
                drawTableHeader()
                rowIndex = 0
            }
            val rowTop = Math.round(y).toFloat()
            val rowBottom = Math.round(y + rowH).toFloat()

            if (rowIndex % 2 == 1) {
                canvas.drawRect(margin, rowTop, pageWidth - margin, rowBottom, rowAltPaint)
            }
            canvas.drawLine(margin, rowBottom, pageWidth - margin, rowBottom, borderPaint)

            val textY = rowTop + 14f
            columns.forEachIndexed { i, _ ->
                when (i) {
                    0 -> canvas.drawText(
                        shortMonth(r.billingMonth),
                        (colLeft[i] + 5).toFloat(), textY, bodyBoldPaint
                    )
                    1 -> {
                        val (label, paint) = when (r.paymentStatus.uppercase()) {
                            "PAID" -> "Paid" to paidPaint
                            "PARTIALLY_PAID" -> "Partial" to partialPaint
                            else -> "Unpaid" to unpaidPaint
                        }
                        canvas.drawText(label, (colLeft[i] + 5).toFloat(), textY, paint)
                    }
                    2 -> rightText(canvas, num(r.roomRent), i, colRight, textY, bodyPaint)
                    3 -> rightText(canvas, num(r.electricityCost), i, colRight, textY, bodyPaint)
                    4 -> rightText(canvas, num(r.waterCost), i, colRight, textY, bodyPaint)
                    5 -> rightText(canvas, num(r.wasteCharge), i, colRight, textY, bodyPaint)
                    6 -> if (r.discount > 0) {
                        rightText(canvas, "-${num(r.discount)}", i, colRight, textY, discountPaint)
                    } else {
                        rightText(canvas, "—", i, colRight, textY, mutedPaint)
                    }
                    7 -> rightText(canvas, num(r.totalAmount), i, colRight, textY, bodyBoldPaint)
                    8 -> rightText(canvas, num(r.amountPaid), i, colRight, textY, bodyPaint)
                    9 -> {
                        val paint = if (r.remainingAmount > 0) unpaidPaint else paidPaint
                        rightText(canvas, num(r.remainingAmount), i, colRight, textY, paint)
                    }
                    10 -> canvas.drawText(
                        r.notes.ifBlank { "—" }.take(15),
                        (colLeft[i] + 5).toFloat(), textY, bodyPaint
                    )
                }
            }
            y = rowBottom
            rowIndex++
        }

        if (y + 24f > pageHeight - 30f) {
            drawFooter()
            document.finishPage(page)
            pageNumber++
            page = newPage()
            canvas = page.canvas
            y = margin.toInt().toFloat()
            drawTableHeader()
        }

        // ── Totals row ───────────────────────────────────────────
        canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
        val tY = Math.round(y).toFloat() + 15f
        canvas.drawText("TOTALS", (colLeft[0] + 5).toFloat(), tY, bodyBoldPaint)
        if (totalDiscount > 0) {
            rightText(canvas, "-${num(totalDiscount)}", 6, colRight, tY, discountPaint)
        }
        rightText(canvas, num(totalBilled), 7, colRight, tY, bodyBoldPaint)
        rightText(canvas, num(totalPaid), 8, colRight, tY, bodyBoldPaint)
        rightText(canvas, num(totalDue), 9, colRight, tY,
            if (totalDue > 0) unpaidPaint else paidPaint)
        y += 22f

        // ── Receipt summary block ────────────────────────────────
        y += 22f

        val blockW = 260
        val blockX = (pageWidth - margin).toInt() - blockW
        val lineH = 20f
        val headerH2 = 28f
        val padTop = 12f
        val padBottom = 14f

        val rows = mutableListOf<Triple<String, String, Paint>>()
        rows.add(Triple("Gross Billed", money(grossBilled), receiptValuePaint))
        if (totalDiscount > 0) {
            rows.add(Triple("Discount", "- ${money(totalDiscount)}", discountPaint))
        }
        rows.add(Triple("Net Billed", money(totalBilled), receiptValuePaint))
        rows.add(Triple("Amount Paid", money(totalPaid), paidPaint))
        rows.add(Triple("Balance Due", money(totalDue),
            if (totalDue > 0) unpaidPaint else paidPaint))

        val blockH = headerH2 + padTop + rows.size * lineH + padBottom
        val blockTop = Math.round(y).toFloat()

        canvas.drawRoundRect(
            blockX.toFloat(), blockTop,
            (blockX + blockW).toFloat(), blockTop + blockH,
            6f, 6f, receiptBgPaint
        )
        canvas.drawRoundRect(
            blockX.toFloat(), blockTop,
            (blockX + blockW).toFloat(), blockTop + headerH2,
            6f, 6f, receiptHeaderPaint
        )
        canvas.drawRect(
            blockX.toFloat(), blockTop + headerH2 - 6f,
            (blockX + blockW).toFloat(), blockTop + headerH2,
            receiptHeaderPaint
        )

        canvas.drawText("PAYMENT SUMMARY", (blockX + 14).toFloat(), blockTop + 18f, receiptTitlePaint)

        var ry = blockTop + headerH2 + padTop + 12f
        rows.forEach { (label, value, valuePaint) ->
            if (label == "Balance Due") {
                canvas.drawLine(
                    (blockX + 12).toFloat(), ry - 14f,
                    (blockX + blockW - 12).toFloat(), ry - 14f,
                    borderPaint
                )
            }
            val isBalance = label == "Balance Due"
            val useLabelPaint = if (isBalance) receiptTotalLabelPaint else receiptLabelPaint
            val useValuePaint = if (isBalance) Paint(valuePaint).apply { textSize = 11f } else valuePaint

            canvas.drawText(label, (blockX + 14).toFloat(), ry, useLabelPaint)
            val vw = useValuePaint.measureText(value)
            canvas.drawText(
                value,
                Math.round(blockX + blockW - 14 - vw).toFloat(),
                ry, useValuePaint
            )
            ry += lineH
        }

        y = blockTop + blockH + 6f

        drawFooter()
        document.finishPage(page)

        // ── Share ────────────────────────────────────────────────
        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(cacheDir, "rent_history_$timestamp.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export PDF"))
    }
    private fun rightText(
        canvas: Canvas,
        text: String,
        idx: Int,
        colRight: IntArray,
        y: Float,
        paint: Paint
    ) {
        val x = Math.round(colRight[idx] - 5f - paint.measureText(text)).toFloat()
        canvas.drawText(text, x, Math.round(y).toFloat(), paint)
    }

    private fun drawSummaryCard(
        canvas: Canvas, x: Float, y: Float, w: Float, h: Float,
        label: String, value: String, accent: Int
    ) {
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(248, 250, 252) }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; color = Color.rgb(100, 116, 139)
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f; isFakeBoldText = true; color = Color.rgb(17, 24, 39)
        }
        canvas.drawRect(x, y, x + w, y + h, bg)
        canvas.drawRect(x, y, x + 3f, y + h, accentPaint)
        canvas.drawText(label, x + 10f, y + 16f, labelPaint)
        canvas.drawText(value, x + 10f, y + 34f, valuePaint)
    }

    // ═══════════════════════════════════════════════════════════
    //  EXCEL — single-room
    // ═══════════════════════════════════════════════════════════
    fun exportExcel(context: Context, records: List<MonthlyRecordEntity>) {
        if (records.isEmpty()) return

        val room = activeRoom.value
        val symbol = currencySymbol.value
        val generatedAt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())

        val headers = listOf(
            "Billing Month", "Payment Date", "Status",
            "Room Rent", "Elec. Units", "Elec. Cost",
            "Water Units", "Water Cost", "Waste Charge",
            "Discount",                     // ★ NEW
            "Total Amount", "Amount Paid", "Remaining Due",
            "Description"
        )

        val sheet = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
            append("<cols>")
            append("""<col min="1" max="1" width="16" customWidth="1"/>""")   // Month
            append("""<col min="2" max="2" width="14" customWidth="1"/>""")   // Payment Date
            append("""<col min="3" max="3" width="14" customWidth="1"/>""")   // Status
            for (i in 4..13) append("""<col min="$i" max="$i" width="13" customWidth="1"/>""")
            append("""<col min="14" max="14" width="32" customWidth="1"/>""") // Description
            append("</cols><sheetData>")

            // Row 1 — title
            append(
                """<row r="1" ht="26" customHeight="1">""" +
                        """<c r="A1" t="inlineStr" s="1"><is><t>Rent History Report — ${
                            xml(room?.name ?: "All Rooms")
                        }</t></is></c></row>"""
            )

            // Row 2 — subtitle
            append(
                """<row r="2" ht="16" customHeight="1">""" +
                        """<c r="A2" t="inlineStr" s="2"><is><t>Generated: $generatedAt  •  ${records.size} record(s)  •  Amounts in $symbol</t></is></c></row>"""
            )

            // Row 3 — spacer
            append("""<row r="3"/>""")

            // Row 4 — headers
            append("""<row r="4" ht="22" customHeight="1">""")
            headers.forEachIndexed { i, h ->
                append("""<c r="${col(i + 1)}4" t="inlineStr" s="3"><is><t>${xml(h)}</t></is></c>""")
            }
            append("</row>")

            // Data rows
            var rn = 5
            for (r in records) {
                append("""<row r="$rn" ht="18" customHeight="1">""")
                val cells: List<Any> = listOf(
                    r.billingMonth,
                    r.paymentDate.ifBlank { "—" },
                    r.paymentStatus.replace("_", " "),
                    r.roomRent,
                    r.electricityUnits,
                    r.electricityCost,
                    r.waterUnits,
                    r.waterCost,
                    r.wasteCharge,
                    r.discount,              // ★ NEW
                    r.totalAmount,
                    r.amountPaid,
                    r.remainingAmount,
                    r.notes.ifBlank { "" }
                )
                cells.forEachIndexed { i, v ->
                    val c = col(i + 1)
                    if (v is Number) {
                        // Discount cell (column J = index 9) uses amber style
                        val style = if (i == 9) "8" else "5"
                        append("""<c r="$c$rn" s="$style"><v>${v.toDouble()}</v></c>""")
                    } else {
                        append("""<c r="$c$rn" t="inlineStr" s="4"><is><t>${xml(v.toString())}</t></is></c>""")
                    }
                }
                append("</row>")
                rn++
            }

            // ── TOTALS row ───────────────────────────────────────
            append("""<row r="$rn" ht="24" customHeight="1">""")
            append("""<c r="A$rn" t="inlineStr" s="6"><is><t>TOTALS</t></is></c>""")
            append("""<c r="B$rn" s="6"/><c r="C$rn" s="6"/>""")

            listOf(
                4 to records.sumOf { it.roomRent },
                5 to records.sumOf { it.electricityUnits },
                6 to records.sumOf { it.electricityCost },
                7 to records.sumOf { it.waterUnits },
                8 to records.sumOf { it.waterCost },
                9 to records.sumOf { it.wasteCharge },
                10 to records.sumOf { it.discount },       // ★ NEW
                11 to records.sumOf { it.totalAmount },
                12 to records.sumOf { it.amountPaid },
                13 to records.sumOf { it.remainingAmount }
            ).forEach { (idx, v) ->
                // Column J (10) is discount → amber style
                val style = if (idx == 10) "8" else "6"
                append("""<c r="${col(idx)}$rn" s="$style"><v>$v</v></c>""")
            }
            append("""<c r="N$rn" s="6"/>""")
            append("</row>")

            // ── PAYMENT SUMMARY block ────────────────────────────
            val totalBilled = records.sumOf { it.totalAmount }
            val totalPaid = records.sumOf { it.amountPaid }
            val totalDue = records.sumOf { it.remainingAmount }
            val totalDiscount = records.sumOf { it.discount }
            val grossBilled = totalBilled + totalDiscount

            rn += 2
            append("""<row r="$rn"/>""")   // spacer

            // Header
            rn++
            append("""<row r="$rn" ht="22" customHeight="1">""")
            append("""<c r="I$rn" t="inlineStr" s="9"><is><t>PAYMENT SUMMARY</t></is></c>""")
            append("""<c r="J$rn" t="inlineStr" s="9"><is><t>Amount ($symbol)</t></is></c>""")
            append("</row>")

            // Rows
            fun summaryRow(label: String, value: Double, labelStyle: String, valueStyle: String) {
                rn++
                append("""<row r="$rn" ht="20" customHeight="1">""")
                append("""<c r="I$rn" t="inlineStr" s="$labelStyle"><is><t>${xml(label)}</t></is></c>""")
                append("""<c r="J$rn" s="$valueStyle"><v>${value}</v></c>""")
                append("</row>")
            }

            summaryRow("Subtotal", grossBilled, "11", "12")
            if (totalDiscount > 0) {
                summaryRow("Discount", -totalDiscount, "13", "14")
            }
            summaryRow("Net Billed", totalBilled, "11", "15")
            summaryRow("Amount Paid", totalPaid, "11", "16")

            // Balance due — larger, bold
            rn++
            append("""<row r="$rn" ht="26" customHeight="1">""")
            append("""<c r="I$rn" t="inlineStr" s="17"><is><t>Balance Due</t></is></c>""")
            append("""<c r="J$rn" s="18"><v>${totalDue}</v></c>""")
            append("</row>")

            append("</sheetData></worksheet>")
        }

        // ═══════════════════════════════════════════════════════════
        // STYLES — 19 xf entries now (7 original + 12 new for summary)
        // ═══════════════════════════════════════════════════════════
        val styles = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<numFmts count="1"><numFmt numFmtId="164" formatCode="#,##0.00"/></numFmts>
<fonts count="11">
<font><sz val="11"/><name val="Calibri"/></font>
<font><b/><sz val="16"/><color rgb="FF111827"/><name val="Calibri"/></font>
<font><sz val="10"/><color rgb="FF6B7280"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
<font><sz val="11"/><color rgb="FF1F2937"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FF111827"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FF92400E"/><name val="Calibri"/></font>
<font><b/><sz val="10"/><color rgb="FF6B7280"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FF16A34A"/><name val="Calibri"/></font>
<font><b/><sz val="12"/><color rgb="FFDC2626"/><name val="Calibri"/></font>
<font><b/><sz val="12"/><color rgb="FF111827"/><name val="Calibri"/></font>
</fonts>
<fills count="10">
<fill><patternFill patternType="none"/></fill>
<fill><patternFill patternType="gray125"/></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF2563EB"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFF1F5F9"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFE2E8F0"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFFFF7ED"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFF0FDF4"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFFEF2F2"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFF8FAFC"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFEEF2FF"/><bgColor indexed="64"/></patternFill></fill>
</fills>
<borders count="2">
<border><left/><right/><top/><bottom/><diagonal/></border>
<border><left style="thin"><color rgb="FFCBD5E1"/></left><right style="thin"><color rgb="FFCBD5E1"/></right><top style="thin"><color rgb="FFCBD5E1"/></top><bottom style="thin"><color rgb="FFCBD5E1"/></bottom><diagonal/></border>
</borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="19">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="0" fontId="3" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
<xf numFmtId="0" fontId="4" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1"><alignment vertical="center"/></xf>
<xf numFmtId="164" fontId="4" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="164" fontId="5" fillId="4" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="164" fontId="6" fillId="5" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="0" fontId="3" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="left" vertical="center"/></xf>
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="4" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="164" fontId="4" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" applyFont="1" applyAlignment="1"><alignment horizontal="right"/></xf>
<xf numFmtId="0" fontId="6" fillId="5" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
<xf numFmtId="164" fontId="6" fillId="5" borderId="0" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="right"/></xf>
<xf numFmtId="164" fontId="5" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" applyFont="1" applyAlignment="1"><alignment horizontal="right"/></xf>
<xf numFmtId="164" fontId="8" fillId="6" borderId="0" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="right"/></xf>
<xf numFmtId="0" fontId="5" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="164" fontId="9" fillId="7" borderId="0" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="right"/></xf>
</cellXfs>
</styleSheet>"""

        val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

        val rootRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

        val workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Rent History" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

        val workbookRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(cacheDir, "rent_history_$timestamp.xlsx")

        ZipOutputStream(FileOutputStream(file)).use { zip ->
            fun entry(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            entry("[Content_Types].xml", contentTypes)
            entry("_rels/.rels", rootRels)
            entry("xl/workbook.xml", workbook)
            entry("xl/_rels/workbook.xml.rels", workbookRels)
            entry("xl/styles.xml", styles)
            entry("xl/worksheets/sheet1.xml", sheet)
        }

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Excel"))
    }
    // ═══════════════════════════════════════════════════════════
    //  helpers
    // ═══════════════════════════════════════════════════════════
    private fun col(index: Int): String {
        var i = index
        val sb = StringBuilder()
        while (i > 0) {
            val rem = (i - 1) % 26
            sb.insert(0, ('A' + rem))
            i = (i - 1) / 26
        }
        return sb.toString()
    }

    private fun xml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    fun exportExcelAll(context: Context) {
        val rooms = allRooms.value
        if (rooms.isEmpty()) return

        val recordsByRoom = allRecords.value.groupBy { it.roomId }
        val generatedAt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())

        // ★ Discount column added between Waste Charge and Total Amount.
        val headers = listOf(
            "Billing Month", "Payment Date", "Status",
            "Room Rent", "Elec. Units", "Elec. Cost",
            "Water Units", "Water Cost", "Waste Charge",
            "Discount",
            "Total Amount", "Amount Paid", "Remaining Due",
            "Description"
        )

        val usedNames = mutableSetOf<String>()
        val sheetNames = rooms.map { room ->
            val base = sanitizeSheetName(room.name)
            var candidate = base
            var suffix = 2
            while (candidate.lowercase() in usedNames) {
                candidate = base.take(28) + " " + suffix
                suffix++
            }
            usedNames.add(candidate.lowercase())
            candidate
        }

        val sheetsXml = rooms.mapIndexed { _, room ->
            val roomRecords = recordsByRoom[room.id].orEmpty()
                .sortedByDescending { it.billingMonth }
            buildRoomSheet(room, roomRecords, generatedAt, headers)
        }

        val contentTypes = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
            append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
            append("""<Default Extension="xml" ContentType="application/xml"/>""")
            append("""<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
            for (i in 1..rooms.size) {
                append("""<Override PartName="/xl/worksheets/sheet$i.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>""")
            }
            append("""<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>""")
            append("</Types>")
        }

        val workbook = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
            append("<sheets>")
            rooms.forEachIndexed { idx, _ ->
                append("""<sheet name="${xml(sheetNames[idx])}" sheetId="${idx + 1}" r:id="rId${idx + 1}"/>""")
            }
            append("</sheets></workbook>")
        }

        val workbookRels = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
            for (i in 1..rooms.size) {
                append("""<Relationship Id="rId$i" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet$i.xml"/>""")
            }
            append("""<Relationship Id="rId${rooms.size + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>""")
            append("</Relationships>")
        }

        val rootRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

        // ★ Expanded styles: 10 fonts, 6 fills, 14 cellXfs.
        //   Adds amber / emerald / rose / indigo colours used by the
        //   summary block, plus a medium-gray fill for the subtotal row.
        val styles = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<numFmts count="1"><numFmt numFmtId="164" formatCode="#,##0.00"/></numFmts>
<fonts count="10">
<font><sz val="11"/><name val="Calibri"/></font>
<font><b/><sz val="16"/><color rgb="FF111827"/><name val="Calibri"/></font>
<font><sz val="10"/><color rgb="FF6B7280"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
<font><sz val="11"/><color rgb="FF1F2937"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FF111827"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FFF59E0B"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FF10B981"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FFF43F5E"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FF4F46E5"/><name val="Calibri"/></font>
</fonts>
<fills count="6">
<fill><patternFill patternType="none"/></fill>
<fill><patternFill patternType="gray125"/></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF2563EB"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFF1F5F9"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFE2E8F0"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFEEF2FF"/><bgColor indexed="64"/></patternFill></fill>
</fills>
<borders count="2">
<border><left/><right/><top/><bottom/><diagonal/></border>
<border><left style="thin"><color rgb="FFCBD5E1"/></left><right style="thin"><color rgb="FFCBD5E1"/></right><top style="thin"><color rgb="FFCBD5E1"/></top><bottom style="thin"><color rgb="FFCBD5E1"/></bottom><diagonal/></border>
</borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="14">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="0" fontId="3" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
<xf numFmtId="0" fontId="4" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1"><alignment vertical="center"/></xf>
<xf numFmtId="164" fontId="4" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="164" fontId="5" fillId="4" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="0" fontId="5" fillId="4" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center"/></xf>
<xf numFmtId="0" fontId="4" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1"><alignment vertical="center"/></xf>
<xf numFmtId="164" fontId="5" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="164" fontId="6" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="164" fontId="7" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="164" fontId="8" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="0" fontId="9" fillId="5" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment vertical="center"/></xf>
</cellXfs>
</styleSheet>"""

        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(cacheDir, "rent_history_all_rooms_$timestamp.xlsx")

        ZipOutputStream(FileOutputStream(file)).use { zip ->
            fun entry(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            entry("[Content_Types].xml", contentTypes)
            entry("_rels/.rels", rootRels)
            entry("xl/workbook.xml", workbook)
            entry("xl/_rels/workbook.xml.rels", workbookRels)
            entry("xl/styles.xml", styles)
            sheetsXml.forEachIndexed { idx, sheet ->
                entry("xl/worksheets/sheet${idx + 1}.xml", sheet)
            }
        }

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Excel"))
    }


    private fun buildRoomSheet(
        room: RoomEntity,
        records: List<MonthlyRecordEntity>,
        generatedAt: String,
        headers: List<String>
    ): String {
        val colLetters = listOf("A","B","C","D","E","F","G","H","I","J","K","L","M","N")

        fun col(i: Int): String = colLetters.getOrElse(i) { "Z" }

        fun esc(s: String): String = s
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

        fun cText(ref: String, styleId: Int, text: String): String =
            """<c r="$ref" s="$styleId" t="inlineStr"><is><t xml:space="preserve">${esc(text)}</t></is></c>"""

        fun cNum(ref: String, styleId: Int, value: Double): String =
            """<c r="$ref" s="$styleId"><v>$value</v></c>"""

        // Column widths
        val colsXml = buildString {
            append("<cols>")
            val widths = listOf(16, 14, 10, 12, 11, 12, 11, 12, 12, 11, 12, 12, 12, 26)
            widths.forEachIndexed { i, w ->
                append("""<col min="${i + 1}" max="${i + 1}" width="$w" customWidth="1"/>""")
            }
            append("</cols>")
        }

        val sb = StringBuilder()

        // ── Row 1: title ──
        sb.append("""<row r="1" ht="26" customHeight="1">""")
        sb.append(cText("A1", 1, room.name))
        sb.append("</row>")

        // ── Row 2: subtitle ──
        val subtitle = buildString {
            if (room.address.isNotBlank()) append(room.address).append("  ·  ")
            append("Generated $generatedAt")
            append("  ·  ")
            append("${records.size} record(s)")
        }
        sb.append("""<row r="2" ht="18" customHeight="1">""")
        sb.append(cText("A2", 2, subtitle))
        sb.append("</row>")

        // ── Row 3: blank ──
        sb.append("""<row r="3"/>""")

        // ── Row 4: headers ──
        sb.append("""<row r="4" ht="22" customHeight="1">""")
        headers.forEachIndexed { i, h -> sb.append(cText("${col(i)}4", 3, h)) }
        sb.append("</row>")

        // ── Data rows ──
        var row = 5
        records.forEach { r ->
            sb.append("""<row r="$row">""")
            sb.append(cText("${col(0)}$row", 4, formatMonthShort(r.billingMonth)))
            sb.append(cText("${col(1)}$row", 4, r.paymentDate))
            sb.append(cText("${col(2)}$row", 4, r.paymentStatus))
            sb.append(cNum("${col(3)}$row", 5, r.roomRent))
            sb.append(cNum("${col(4)}$row", 5, r.electricityUnits))
            sb.append(cNum("${col(5)}$row", 5, r.electricityCost))
            sb.append(cNum("${col(6)}$row", 5, r.waterUnits))
            sb.append(cNum("${col(7)}$row", 5, r.waterCost))
            sb.append(cNum("${col(8)}$row", 5, r.wasteCharge))
            sb.append(cNum("${col(9)}$row", 10, r.discount))          // amber
            sb.append(cNum("${col(10)}$row", 6, r.totalAmount))       // bold
            sb.append(cNum("${col(11)}$row", 5, r.amountPaid))
            sb.append(cNum("${col(12)}$row", 5, r.remainingAmount))
            sb.append(cText("${col(13)}$row", 4, r.notes.ifBlank { "—" }))
            sb.append("</row>")
            row++
        }

        // ── Compute room totals ──
        val tRent    = records.sumOf { it.roomRent }
        val tElec    = records.sumOf { it.electricityCost }
        val tWater   = records.sumOf { it.waterCost }
        val tWaste   = records.sumOf { it.wasteCharge }
        val tDisc    = records.sumOf { it.discount }
        val tTotal   = records.sumOf { it.totalAmount }
        val tPaid    = records.sumOf { it.amountPaid }
        val tDue     = records.sumOf { it.remainingAmount }

        // ── Blank spacer ──
        sb.append("""<row r="$row"/>""")
        row++

        // ── SUBTOTAL row (aligned to the table columns, filled gray) ──
        sb.append("""<row r="$row" ht="20" customHeight="1">""")
        sb.append(cText("${col(0)}$row", 7, "SUBTOTAL"))
        sb.append(cText("${col(1)}$row", 7, ""))
        sb.append(cText("${col(2)}$row", 7, ""))
        sb.append(cNum("${col(3)}$row", 6, tRent))
        sb.append(cText("${col(4)}$row", 7, ""))
        sb.append(cNum("${col(5)}$row", 6, tElec))
        sb.append(cText("${col(6)}$row", 7, ""))
        sb.append(cNum("${col(7)}$row", 6, tWater))
        sb.append(cNum("${col(8)}$row", 6, tWaste))
        sb.append(cNum("${col(9)}$row", 10, tDisc))
        sb.append(cNum("${col(10)}$row", 6, tTotal))
        sb.append(cNum("${col(11)}$row", 6, tPaid))
        sb.append(cNum("${col(12)}$row", 6, tDue))
        sb.append(cText("${col(13)}$row", 7, ""))
        sb.append("</row>")
        row++

        // ── Blank spacer ──
        sb.append("""<row r="$row"/>""")
        row++

        // ── SUMMARY section header ──
        sb.append("""<row r="$row" ht="22" customHeight="1">""")
        sb.append(cText("${col(0)}$row", 13, "SUMMARY"))
        sb.append("</row>")
        row++

        // ── Summary items: label in col A, value in col D ──
        //    styleId: 9 = bold value, 10 = amber, 11 = emerald, 12 = rose
        val items = listOf(
            Triple("Room Rent",      tRent,  9),
            Triple("Electricity",    tElec,  9),
            Triple("Water",          tWater, 9),
            Triple("Waste",          tWaste, 9),
            Triple("Discount",       tDisc,  10),
            Triple("Total Billed",   tTotal, 9),
            Triple("Amount Paid",    tPaid,  11),
            Triple("Remaining Due",  tDue,   if (tDue > 0) 12 else 11)
        )
        items.forEach { (label, value, styleId) ->
            sb.append("""<row r="$row">""")
            sb.append(cText("${col(0)}$row", 8, label))
            sb.append(cNum("${col(3)}$row", styleId, value))
            sb.append("</row>")
            row++
        }

        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
            append(colsXml)
            append("<sheetData>")
            append(sb.toString())
            append("</sheetData>")
            append("</worksheet>")
        }
    }


    private fun sanitizeSheetName(name: String): String {
        val cleaned = name.replace(Regex("""[\\/*?\[\]:]"""), "_").trim()
        return cleaned.ifBlank { "Room" }.take(31)
    }


    private fun formatMonthShort(raw: String): String {
        val t = raw.trim()
        if (t.isEmpty()) return t

        val months = mapOf(
            "january" to "01", "february" to "02", "march" to "03", "april" to "04",
            "may" to "05", "june" to "06", "july" to "07", "august" to "08",
            "september" to "09", "october" to "10", "november" to "11", "december" to "12",
            "jan" to "01", "feb" to "02", "mar" to "03", "apr" to "04",
            "may" to "05", "jun" to "06", "jul" to "07", "aug" to "08",
            "sep" to "09", "sept" to "09", "oct" to "10", "nov" to "11", "dec" to "12"
        )

        val parts = t.split(' ', '-', '/', ',').filter { it.isNotBlank() }
        if (parts.size < 2) return t

        val monthPart = parts[0]
        val yearPart  = parts[1]

        if (monthPart.length <= 2 && monthPart.all { it.isDigit() }
            && yearPart.length == 4 && yearPart.all { it.isDigit() }
        ) {
            return "${monthPart.padStart(2, '0')}-$yearPart"
        }

        val mm = months[monthPart.lowercase(Locale.US)]
        if (mm != null && yearPart.length == 4 && yearPart.all { it.isDigit() }) {
            return "$mm-$yearPart"
        }

        return t
    }

    /* ─────────────────────────────────────────────────────────────
       PDF text helpers. No shrinking — ellipsize on overflow.
       ───────────────────────────────────────────────────────────── */
    private fun drawRightAligned(
        canvas: Canvas, text: String, x: Float, width: Float,
        baseline: Float, paint: Paint, padRight: Float = 6f
    ) {
        if (text.isEmpty()) return
        val maxW = width - padRight - 4f
        val display = ellipsize(paint, text, maxW)
        canvas.drawText(display, x + width - padRight - paint.measureText(display), baseline, paint)
    }

    private fun drawLeftAligned(
        canvas: Canvas, text: String, x: Float, width: Float,
        baseline: Float, paint: Paint, padLeft: Float = 6f
    ) {
        if (text.isEmpty()) return
        val maxW = width - padLeft - 4f
        val display = ellipsize(paint, text, maxW)
        canvas.drawText(display, x + padLeft, baseline, paint)
    }

    private fun drawCentered(
        canvas: Canvas, text: String, centerX: Float,
        baseline: Float, paint: Paint, maxW: Float = Float.MAX_VALUE
    ) {
        if (text.isEmpty()) return
        val display = ellipsize(paint, text, maxW)
        val w = paint.measureText(display)
        canvas.drawText(display, centerX - w / 2f, baseline, paint)
    }

    private fun ellipsize(paint: Paint, text: String, maxW: Float): String {
        if (maxW <= 0f) return ""
        if (paint.measureText(text) <= maxW) return text
        var t = text
        while (t.isNotEmpty() && paint.measureText("$t…") > maxW) {
            t = t.dropLast(1)
        }
        return if (t.isEmpty()) "…" else "$t…"
    }

    fun exportPdfAll(context: Context) {
        val rooms = allRooms.value
        if (rooms.isEmpty()) return

        val recordsByRoom = allRecords.value.groupBy { it.roomId }
        val symbol = currencySymbol.value
        val generatedAt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())

        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val tableWidth = pageWidth - 2 * margin

        /* ── Palette ─────────────────────────────────────────────── */
        val ink        = Color.rgb(17, 24, 39)
        val inkSoft    = Color.rgb(75, 85, 99)
        val inkMuted   = Color.rgb(156, 163, 175)
        val indigo     = Color.rgb(79, 70, 229)
        val indigoSoft = Color.rgb(238, 242, 255)
        val indigoEdge = Color.rgb(199, 210, 254)
        val emerald    = Color.rgb(5, 150, 105)
        val emeraldSoft= Color.rgb(236, 253, 245)
        val amber      = Color.rgb(217, 119, 6)
        val amberSoft  = Color.rgb(255, 251, 235)
        val rose       = Color.rgb(225, 29, 72)
        val roseSoft   = Color.rgb(255, 241, 242)
        val line       = Color.rgb(229, 231, 235)
        val lineStrong = Color.rgb(209, 213, 219)
        val rowAlt     = Color.rgb(249, 250, 251)
        val white      = Color.rgb(255, 255, 255)

        /* ── Text paints (fixed sizes, no letterSpacing) ────────── */
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 22f; isFakeBoldText = true; color = ink
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; color = inkSoft
        }
        val kickerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 7.5f; isFakeBoldText = true; color = inkMuted
        }
        val roomIndexPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; isFakeBoldText = true; color = indigo
        }
        val roomNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f; isFakeBoldText = true; color = ink
        }
        val roomSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; color = inkSoft
        }
        val tenantChipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = indigo
        }
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 7.5f; isFakeBoldText = true; color = inkSoft
        }
        val tableBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; color = ink
        }
        val tableBodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = ink
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 7.5f; color = inkMuted
        }

        val emeraldText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = emerald
        }
        val amberText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = amber
        }
        val roseText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = rose
        }

        /* ── Borders ─────────────────────────────────────────────── */
        val hairline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = line; strokeWidth = 0.5f
        }
        val hairlineStrong = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineStrong; strokeWidth = 0.7f
        }
        val cardBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 0.7f; color = lineStrong
        }
        val indigoCardBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 0.7f; color = indigoEdge
        }
        val indigoEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = indigoEdge; strokeWidth = 0.7f
        }

        /* ── Fills ───────────────────────────────────────────────── */
        val fillWhite       = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = white }
        val fillRowAlt      = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = rowAlt }
        val fillIndigo      = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = indigo }
        val fillIndigoSoft  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = indigoSoft }
        val fillEmeraldSoft = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = emeraldSoft }
        val fillAmberSoft   = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = amberSoft }
        val fillRoseSoft    = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = roseSoft }

        /* ── Summary card paints ─────────────────────────────────── */
        val sumKickerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 7.5f; isFakeBoldText = true; color = inkMuted
        }
        val sumTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f; isFakeBoldText = true; color = ink
        }
        val sumMetaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; color = inkSoft
        }
        val sumLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; color = inkSoft
        }
        val sumValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f; isFakeBoldText = true; color = ink
        }
        val sumValueAmber   = Paint(sumValuePaint).apply { color = amber }
        val sumValueEmerald = Paint(sumValuePaint).apply { color = emerald }
        val sumValueRose    = Paint(sumValuePaint).apply { color = rose }
        val sumTotalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f; isFakeBoldText = true; color = ink
        }
        val sumTotalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f; isFakeBoldText = true; color = ink
        }
        val sumDashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineStrong; strokeWidth = 0.4f
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(1f, 2.5f), 0f)
        }

        /* ── Grand-total paints ──────────────────────────────────── */
        val grandStatValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f; isFakeBoldText = true; color = ink
        }
        val grandStatValueEmerald = Paint(grandStatValuePaint).apply { color = emerald }
        val grandStatValueAmber   = Paint(grandStatValuePaint).apply { color = amber }
        val grandStatValueRose    = Paint(grandStatValuePaint).apply { color = rose }

        val grandBigValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 24f; isFakeBoldText = true; color = indigo
        }

        /* ── Table columns (fractions sum to 1.00) ───────────────── */
        val columns = listOf(
            "MONTH" to 0.09f, "STATUS" to 0.08f,
            "RENT" to 0.09f, "ELEC" to 0.09f, "WATER" to 0.09f, "WASTE" to 0.08f,
            "DISC" to 0.08f,
            "TOTAL" to 0.09f, "PAID" to 0.09f, "DUE" to 0.09f,
            "DESCRIPTION" to 0.13f
        )
        val colWidths = columns.map { it.second * tableWidth }
        val colX = FloatArray(columns.size)
        run {
            var acc = margin
            for (i in columns.indices) { colX[i] = acc; acc += colWidths[i] }
        }
        val rightAligned = setOf(2, 3, 4, 5, 6, 7, 8, 9)

        fun money(v: Double) = FormatUtils.formatMoney(v, symbol)
        fun moneyPlain(v: Double) =
            FormatUtils.formatMoney(v, "").trim().removePrefix(symbol).trim()

        var pageNumber = 1
        fun newPage(): PdfDocument.Page = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )

        var page = newPage()
        var canvas = page.canvas
        var y = margin

        fun drawFooter() {
            val fy = pageHeight - 24f
            canvas.drawLine(margin, fy - 10f, pageWidth - margin, fy - 10f, hairline)
            canvas.drawText("Room Rent & Utility ‣ By Biplap Neupane.", margin, fy, footerPaint)
            val mid = "All Rooms Report"
            canvas.drawText(mid, pageWidth / 2f - footerPaint.measureText(mid) / 2f, fy, footerPaint)
            val label = "Page $pageNumber"
            canvas.drawText(label, pageWidth - margin - footerPaint.measureText(label), fy, footerPaint)
        }

        fun ensureSpace(needed: Float) {
            if (y + needed > pageHeight - 40f) {
                drawFooter(); document.finishPage(page)
                pageNumber++; page = newPage(); canvas = page.canvas; y = margin
            }
        }

        fun forceNewPage() {
            drawFooter(); document.finishPage(page)
            pageNumber++; page = newPage(); canvas = page.canvas; y = margin
        }

        fun roundedRect(x: Float, top: Float, w: Float, h: Float, r: Float, paint: Paint) {
            canvas.drawRoundRect(RectF(x, top, x + w, top + h), r, r, paint)
        }

        fun roundedBorder(x: Float, top: Float, w: Float, h: Float, r: Float, paint: Paint) {
            canvas.drawRoundRect(RectF(x, top, x + w, top + h), r, r, paint)
        }

        /* ── Data prep ───────────────────────────────────────────── */
        data class RoomSummary(
            val room: RoomEntity,
            val records: List<MonthlyRecordEntity>,
            val billed: Double, val paid: Double, val due: Double, val count: Int,
            val discount: Double,
            val rentSum: Double, val elecSum: Double, val waterSum: Double, val wasteSum: Double
        )
        val summaries = rooms.map { room ->
            val recs = recordsByRoom[room.id].orEmpty().sortedByDescending { it.billingMonth }
            RoomSummary(
                room = room,
                records = recs,
                billed = recs.sumOf { it.totalAmount },
                paid = recs.sumOf { it.amountPaid },
                due = recs.sumOf { it.remainingAmount },
                count = recs.size,
                discount = recs.sumOf { it.discount },
                rentSum = recs.sumOf { it.roomRent },
                elecSum = recs.sumOf { it.electricityCost },
                waterSum = recs.sumOf { it.waterCost },
                wasteSum = recs.sumOf { it.wasteCharge }
            )
        }

        val grandBilled    = summaries.sumOf { it.billed }
        val grandPaid      = summaries.sumOf { it.paid }
        val grandDue       = summaries.sumOf { it.due }
        val grandCount     = summaries.sumOf { it.count }
        val grandDiscount  = summaries.sumOf { it.discount }
        val grandRentSum   = summaries.sumOf { it.rentSum }
        val grandElecSum   = summaries.sumOf { it.elecSum }
        val grandWaterSum  = summaries.sumOf { it.waterSum }
        val grandWasteSum  = summaries.sumOf { it.wasteSum }

        /* ═══════════════════════════════════════════════════════════
           Summary card — reused by per-room and grand total.
           ═══════════════════════════════════════════════════════════ */
        fun drawSummaryCard(
            cardX: Float, cardW: Float, cardTop: Float,
            kicker: String, title: String, subtitle: String,
            items: List<Triple<String, String, Int>>,
            emphasisLabels: Set<String>
        ): Float {
            val padX        = 16f
            val headerTop   = 14f
            val kickerH     = 12f
            val titleH      = 20f
            val subH        = 14f
            val headerBot   = 12f
            val lineSpacing = 17f
            val dividerGap  = 8f
            val bottomPad   = 14f

            val preItems  = items.filter { it.first !in emphasisLabels }
            val postItems = items.filter { it.first in emphasisLabels }

            val headerH = headerTop + kickerH + titleH + subH + headerBot
            val preH    = preItems.size * lineSpacing
            val postH   = postItems.size * lineSpacing
            val totalH  = headerH + preH + dividerGap + postH + dividerGap + bottomPad

            roundedRect(cardX, cardTop, cardW, totalH, 8f, fillWhite)
            canvas.drawRect(cardX, cardTop, cardX + 3f, cardTop + totalH, fillIndigo)
            roundedBorder(cardX, cardTop, cardW, totalH, 8f, cardBorder)

            val labelX      = cardX + padX
            val valueRightX = cardX + cardW - padX
            val lineStartX  = labelX
            val lineEndX    = valueRightX

            var cy = cardTop + headerTop

            drawCentered(canvas, kicker,
                cardX + cardW / 2f, cy + kickerH - 3f,
                sumKickerPaint, cardW - padX * 2)
            cy += kickerH

            drawCentered(canvas, title,
                cardX + cardW / 2f, cy + titleH - 5f,
                sumTitlePaint, cardW - padX * 2)
            cy += titleH

            drawCentered(canvas, subtitle,
                cardX + cardW / 2f, cy + subH - 3f,
                sumMetaPaint, cardW - padX * 2)
            cy += subH + headerBot

            canvas.drawLine(lineStartX, cy, lineEndX, cy, hairlineStrong)
            cy += dividerGap

            preItems.forEach { (label, value, colourIdx) ->
                canvas.drawText(label, labelX, cy + 11f, sumLabelPaint)

                val vp = when (colourIdx) {
                    1 -> sumValueAmber
                    2 -> sumValueEmerald
                    3 -> sumValueRose
                    else -> sumValuePaint
                }
                val valueMaxW = valueRightX - (labelX + sumLabelPaint.measureText(label) + 20f)
                val display = ellipsize(vp, value, valueMaxW)
                val vx = valueRightX - vp.measureText(display)
                canvas.drawText(display, vx, cy + 11f, vp)

                val labelW = sumLabelPaint.measureText(label)
                val leaderStart = labelX + labelW + 8f
                val leaderEnd = vx - 8f
                if (leaderEnd > leaderStart) {
                    canvas.drawLine(leaderStart, cy + 8f, leaderEnd, cy + 8f, sumDashPaint)
                }

                cy += lineSpacing
            }

            cy += 2f
            canvas.drawLine(lineStartX, cy, lineEndX, cy, hairlineStrong)
            cy += dividerGap

            postItems.forEach { (label, value, colourIdx) ->
                val isTotal = label.equals("Total Billed", ignoreCase = true)
                val lp = if (isTotal) sumTotalLabelPaint else sumLabelPaint
                val vp = when {
                    isTotal -> sumTotalValuePaint
                    colourIdx == 1 -> sumValueAmber
                    colourIdx == 2 -> sumValueEmerald
                    colourIdx == 3 -> sumValueRose
                    else -> sumValuePaint
                }

                canvas.drawText(label, labelX, cy + 11f, lp)

                val valueMaxW = valueRightX - (labelX + lp.measureText(label) + 20f)
                val display = ellipsize(vp, value, valueMaxW)
                val vx = valueRightX - vp.measureText(display)
                canvas.drawText(display, vx, cy + 11f, vp)

                val labelW = lp.measureText(label)
                val leaderStart = labelX + labelW + 8f
                val leaderEnd = vx - 8f
                if (leaderEnd > leaderStart) {
                    canvas.drawLine(leaderStart, cy + 8f, leaderEnd, cy + 8f, sumDashPaint)
                }

                cy += lineSpacing
            }

            return cardTop + totalH
        }

        /* ═══════════════════════════════════════════════════════════
           Cover header
           ═══════════════════════════════════════════════════════════ */
        canvas.drawText("RENT & UTILITY REPORT", margin, y + 10f, kickerPaint)
        y += 22f
        canvas.drawText("All Rooms Report", margin, y + 18f, titlePaint)
        y += 28f
        canvas.drawText(
            "Generated $generatedAt   ·   ${rooms.size} rooms   ·   $grandCount records",
            margin, y + 10f, subtitlePaint
        )
        y += 20f
        canvas.drawLine(margin, y, pageWidth - margin, y, hairlineStrong)
        y += 22f

        /* ═══════════════════════════════════════════════════════════
           Table header drawer
           ═══════════════════════════════════════════════════════════ */
        val rowH = 24f

        fun drawDetailHeader() {
            roundedRect(margin, y, tableWidth, 22f, 4f, fillIndigoSoft)
            canvas.drawLine(
                margin, y + 22f,
                pageWidth - margin, y + 22f,
                indigoEdgePaint
            )
            columns.forEachIndexed { i, (label, _) ->
                val baseline = y + 15f
                if (i in rightAligned) {
                    drawRightAligned(canvas, label, colX[i], colWidths[i], baseline, tableHeaderPaint)
                } else {
                    drawLeftAligned(canvas, label, colX[i], colWidths[i], baseline, tableHeaderPaint)
                }
            }
            y += 22f
        }

        /* ═══════════════════════════════════════════════════════════
           Per-room pages
           ═══════════════════════════════════════════════════════════ */
        for ((idx, s) in summaries.withIndex()) {
            if (idx > 0) forceNewPage() else y += 4f

            canvas.drawText(
                "ROOM ${idx + 1} OF ${rooms.size}",
                margin, y + 10f, roomIndexPaint
            )
            y += 18f

            drawLeftAligned(
                canvas, s.room.name,
                margin, tableWidth, y + 16f, roomNamePaint, padLeft = 0f
            )
            y += 22f

            val sub = buildString {
                if (s.room.address.isNotBlank()) append(s.room.address).append("  ·  ")
                append("${s.count} record(s)")
            }
            drawLeftAligned(
                canvas, sub,
                margin, tableWidth, y + 10f, roomSubPaint, padLeft = 0f
            )
            y += 18f

            val tenantName = s.room.tenantName.trim()
            if (tenantName.isNotEmpty()) {
                val textW = tenantChipTextPaint.measureText(tenantName)
                val chipW = (textW + 22f).coerceAtMost(tableWidth)
                val chipH = 20f
                roundedRect(margin, y, chipW, chipH, 10f, fillIndigoSoft)
                roundedBorder(margin, y, chipW, chipH, 10f, indigoCardBorder)
                canvas.drawText(
                    ellipsize(tenantChipTextPaint, tenantName, chipW - 20f),
                    margin + 11f, y + 13.5f, tenantChipTextPaint
                )
                y += chipH + 14f
            } else {
                y += 6f
            }

            canvas.drawLine(margin, y, pageWidth - margin, y, hairline)
            y += 16f

            // Mini stats
            val mGap = 8f
            val mW = (tableWidth - 3 * mGap) / 4f
            val mH = 44f
            drawMiniStat(canvas, margin,                     y, mW, mH, "BILLED",   money(s.billed),   indigo,  fillIndigoSoft,  ink)
            drawMiniStat(canvas, margin + (mW + mGap),       y, mW, mH, "DISCOUNT", money(s.discount), amber,   fillAmberSoft,   ink)
            drawMiniStat(canvas, margin + 2 * (mW + mGap),   y, mW, mH, "PAID",     money(s.paid),     emerald, fillEmeraldSoft, ink)
            drawMiniStat(canvas, margin + 3 * (mW + mGap),   y, mW, mH, "DUE",      money(s.due),      rose,    fillRoseSoft,    ink)
            y += mH + 22f

            // Records table
            if (s.records.isEmpty()) {
                canvas.drawText("No records for this room yet.", margin + 8f, y + 12f, tableBodyPaint)
                y += 30f
            } else {
                drawDetailHeader()

                var rowIndex = 0
                for (r in s.records) {
                    if (y + rowH > pageHeight - 40f) {
                        forceNewPage()
                        canvas.drawText("${s.room.name} (continued)", margin, y + 14f, roomNamePaint)
                        y += 26f
                        drawDetailHeader()
                        rowIndex = 0
                    }
                    if (rowIndex % 2 == 1) {
                        canvas.drawRect(margin, y, pageWidth - margin, y + rowH, fillRowAlt)
                    }
                    canvas.drawLine(margin, y + rowH, pageWidth - margin, y + rowH, hairline)

                    val textY = y + 16f

                    drawLeftAligned(
                        canvas, formatMonthShort(r.billingMonth),
                        colX[0], colWidths[0], textY, tableBodyBoldPaint
                    )

                    drawStatusPill(canvas, r.paymentStatus, colX[1] + 4f, y + 6f,
                        fillEmeraldSoft, fillAmberSoft, fillRoseSoft,
                        emeraldText, amberText, roseText)

                    val values = listOf(
                        moneyPlain(r.roomRent), moneyPlain(r.electricityCost),
                        moneyPlain(r.waterCost), moneyPlain(r.wasteCharge),
                        moneyPlain(r.discount),
                        moneyPlain(r.totalAmount), moneyPlain(r.amountPaid),
                        moneyPlain(r.remainingAmount)
                    )
                    values.forEachIndexed { j, text ->
                        val colIdx = j + 2
                        val paint = when (colIdx) {
                            6 -> amberText
                            7 -> tableBodyBoldPaint
                            9 -> if (r.remainingAmount > 0) roseText else emeraldText
                            else -> tableBodyPaint
                        }
                        drawRightAligned(canvas, text, colX[colIdx], colWidths[colIdx], textY, paint)
                    }

                    drawLeftAligned(
                        canvas, r.notes.ifBlank { "—" },
                        colX[10], colWidths[10], textY, tableBodyPaint
                    )

                    y += rowH
                    rowIndex++
                }

                // SUBTOTAL row
                ensureSpace(40f)
                y += 8f
                canvas.drawLine(margin, y, pageWidth - margin, y, hairlineStrong)
                roundedRect(margin, y, tableWidth, 28f, 0f, fillIndigoSoft)
                val subY = y + 18f
                canvas.drawText(
                    "SUBTOTAL — ${s.room.name.take(24)}",
                    margin + 12f, subY, tableBodyBoldPaint
                )
                listOf(6 to s.discount, 7 to s.billed, 8 to s.paid, 9 to s.due)
                    .forEach { (i, v) ->
                        val text = moneyPlain(v)
                        val paint = when (i) {
                            6 -> amberText
                            8 -> emeraldText
                            9 -> if (s.due > 0) roseText else emeraldText
                            else -> tableBodyBoldPaint
                        }
                        drawRightAligned(canvas, text, colX[i], colWidths[i], subY, paint)
                    }
                y += 28f
            }

            // Per-room SUMMARY
            ensureSpace(280f)
            y += 22f

            val cardW = 330f
            val cardX = pageWidth - margin - cardW

            canvas.drawText("ROOM SUMMARY", cardX, y, kickerPaint)
            y += 12f

            val cardBottom = drawSummaryCard(
                cardX = cardX, cardW = cardW, cardTop = y,
                kicker = "SUMMARY",
                title = "Room Summary",
                subtitle = "${s.room.name}  ·  ${s.count} record(s)",
                items = listOf(
                    Triple("Room Rent",     moneyPlain(s.rentSum),   0),
                    Triple("Electricity",   moneyPlain(s.elecSum),   0),
                    Triple("Water",         moneyPlain(s.waterSum),  0),
                    Triple("Waste",         moneyPlain(s.wasteSum),  0),
                    Triple("Discount",      moneyPlain(s.discount),  1),
                    Triple("Total Billed",  moneyPlain(s.billed),    0),
                    Triple("Amount Paid",   moneyPlain(s.paid),      2),
                    Triple("Remaining Due", moneyPlain(s.due),       if (s.due > 0) 3 else 2)
                ),
                emphasisLabels = setOf("Total Billed", "Amount Paid", "Remaining Due")
            )

            y = cardBottom + 6f
        }

        /* ═══════════════════════════════════════════════════════════
           GRAND TOTAL — summary + 4-segment band on its own page.
           ═══════════════════════════════════════════════════════════ */
        forceNewPage()

        // ── Section title ──
        canvas.drawText("Grand Total", margin, y + 18f, titlePaint)
        y += 26f

        canvas.drawText(
            "All rooms  ·  ${rooms.size} room(s)  ·  $grandCount record(s)",
            margin, y + 10f, subtitlePaint
        )
        y += 20f

        canvas.drawLine(margin, y, pageWidth - margin, y, hairlineStrong)
        y += 22f

        /* ── Summary row: one line per component, values right-aligned ── */
        fun grandRow(label: String, value: String, tone: Int) {
            val baseline = y + 14f
            canvas.drawText(label, margin, baseline, sumLabelPaint)

            val vp = when (tone) {
                1 -> sumValueAmber
                2 -> sumValueEmerald
                3 -> sumValueRose
                else -> sumValuePaint
            }
            val display = ellipsize(vp, value, tableWidth - 200f)
            val vx = pageWidth - margin - vp.measureText(display)
            canvas.drawText(display, vx, baseline, vp)

            val labelW = sumLabelPaint.measureText(label)
            val leaderStart = margin + labelW + 10f
            val leaderEnd = vx - 10f
            if (leaderEnd > leaderStart) {
                canvas.drawLine(leaderStart, y + 10f, leaderEnd, y + 10f, sumDashPaint)
            }
            y += 22f
        }

        grandRow("Room Rent",     moneyPlain(grandRentSum),   0)
        grandRow("Electricity",   moneyPlain(grandElecSum),   0)
        grandRow("Water",         moneyPlain(grandWaterSum),  0)
        grandRow("Waste",         moneyPlain(grandWasteSum),  0)
        grandRow("Discount",      moneyPlain(grandDiscount),  1)
        grandRow("Amount Paid",   moneyPlain(grandPaid),      2)
        grandRow("Remaining Due", moneyPlain(grandDue),       if (grandDue > 0) 3 else 2)

        y += 4f
        canvas.drawLine(margin, y, pageWidth - margin, y, hairlineStrong)
        y += 24f

        /* ═══════════════════════════════════════════════════════════
           4-segment band: BILLED · DISCOUNT · PAID · DUE
           ═══════════════════════════════════════════════════════════ */
        val bandH = 92f
        roundedRect(margin, y, tableWidth, bandH, 12f, fillIndigoSoft)
        canvas.drawRect(margin, y, margin + 4f, y + bandH, fillIndigo)
        roundedBorder(margin, y, tableWidth, bandH, 12f, indigoCardBorder)

        // Section label inside the band
        canvas.drawText(
            "GRAND TOTAL ACROSS ALL ROOMS",
            margin + 20f, y + 22f, sumKickerPaint
        )

        val segW = (tableWidth - 32f) / 4f
        val segBaseY = y + 60f

        val segValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f; isFakeBoldText = true; color = ink
        }
        val segLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 7.5f; isFakeBoldText = true; color = inkMuted
        }

        val segments = listOf(
            Triple("BILLED",   money(grandBilled),   indigo),
            Triple("DISCOUNT", money(grandDiscount), amber),
            Triple("PAID",     money(grandPaid),     emerald),
            Triple("DUE",      money(grandDue),      if (grandDue > 0) rose else emerald)
        )

        segments.forEachIndexed { i, (label, value, color) ->
            val sx = margin + 16f + i * segW

            canvas.drawText(label, sx, segBaseY - 14f, segLabelPaint)

            val vp = Paint(segValuePaint).apply { this.color = color }
            val display = ellipsize(vp, value, segW - 8f)
            canvas.drawText(display, sx, segBaseY + 4f, vp)

            if (i < segments.size - 1) {
                canvas.drawLine(
                    sx + segW - 6f, y + 42f,
                    sx + segW - 6f, y + bandH - 14f,
                    hairline
                )
            }
        }

        y += bandH + 22f

        /* ── Closing footnote ── */
        canvas.drawText(
            "This statement covers ${rooms.size} room(s) and $grandCount record(s).",
            margin, y + 8f, subtitlePaint
        )

        drawFooter()
        document.finishPage(page)
        /* ── Write & share ───────────────────────────────────────── */
        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(cacheDir, "rent_history_all_rooms_$timestamp.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export All Rooms PDF"))
    }
    private fun drawMiniStat(
        canvas: Canvas, x: Float, y: Float, w: Float, h: Float,
        label: String, value: String,
        accent: Int, fillColor: Paint, valueColor: Int
    ) {
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 8f, 8f, fillColor)
        canvas.drawRect(x, y, x + 3f, y + h, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent })

        val cap = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 7.5f; isFakeBoldText = true; color = accent; letterSpacing = 0.08f
        }
        val valP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f; isFakeBoldText = true; color = valueColor
        }
        canvas.drawText(label, x + 12f, y + 12f, cap)
        canvas.drawText(value, x + 12f, y + 25f, valP)
    }

    private fun drawStatusPill(
        canvas: Canvas, status: String, x: Float, y: Float,
        fillPaid: Paint, fillPartial: Paint, fillUnpaid: Paint,
        textPaid: Paint, textPartial: Paint, textUnpaid: Paint
    ) {
        val (label, fill, text) = when (status.uppercase()) {
            "PAID" -> Triple("Paid", fillPaid, textPaid)
            "PARTIALLY_PAID" -> Triple("Partial", fillPartial, textPartial)
            else -> Triple("Unpaid", fillUnpaid, textUnpaid)
        }
        val tw = text.measureText(label)
        val pillW = tw + 16f
        val pillH = 14f
        val rect = RectF(x, y, x + pillW, y + pillH)
        canvas.drawRoundRect(rect, 7f, 7f, fill)
        canvas.drawText(label, x + 8f, y + 10f, text)
    }
}