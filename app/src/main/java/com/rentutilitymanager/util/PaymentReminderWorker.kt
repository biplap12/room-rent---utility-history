package com.rentutilitymanager.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rentutilitymanager.data.AppDatabase
import com.rentutilitymanager.data.AppSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PaymentReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val db = AppDatabase.getDatabase(ctx)
        val settings = AppSettings(ctx)

        val allRooms = db.roomDao().getAllRoomsSync()
        val allRecords = db.monthlyRecordDao().getAllRecordsSync()

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val today = Calendar.getInstance()
        val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)
        val lastDay = today.getActualMaximum(Calendar.DAY_OF_MONTH)

        val thisMonth = monthFormat.format(Date())
        val prevMonth = Calendar.getInstance()
            .apply { add(Calendar.MONTH, -1) }
            .let { monthFormat.format(it.time) }

        val symbol = settings.currencySymbol.value

        // 1. This month's bill hasn't been created, 5 days before month end
        if (dayOfMonth == lastDay - 5) {
            val roomsWithoutBill = allRooms.filter { room ->
                allRecords.none { it.roomId == room.id && it.billingMonth == thisMonth }
            }
            if (roomsWithoutBill.isNotEmpty()) {
                NotificationHelper.show(
                    ctx,
                    id = 4001,
                    channel = NotificationHelper.CHANNEL_REMINDER,
                    title = "Rent bill due soon — $thisMonth",
                    body = "${roomsWithoutBill.size} room(s) still need a bill",
                    bigText = "Bills for $thisMonth haven't been created for: " +
                            roomsWithoutBill.joinToString { it.name } + ". Tap to add them now."
                )
            }
        }

        // 2. Last month's bill still unpaid
        val prevUnpaid = allRecords.filter {
            it.billingMonth.equals(prevMonth, ignoreCase = true) && it.remainingAmount > 0
        }
        if (prevUnpaid.isNotEmpty() && dayOfMonth in listOf(1, 5, 10, 15, 20, 25)) {
            val total = prevUnpaid.sumOf { it.remainingAmount }
            val names = prevUnpaid.mapNotNull { rec ->
                allRooms.firstOrNull { it.id == rec.roomId }?.name
            }.joinToString()
            NotificationHelper.show(
                ctx,
                id = 4002,
                channel = NotificationHelper.CHANNEL_REMINDER,
                title = "Rent due — $prevMonth",
                body = "${FormatUtils.formatMoney(total, symbol)} pending from $names",
                bigText = "You have unpaid rent from $prevMonth totalling " +
                        "${FormatUtils.formatMoney(total, symbol)}."
            )
        }

        // 3. Payment date approaching within next 3 days
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val sdfAlt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayMs = today.timeInMillis

        allRecords.filter { it.remainingAmount > 0 && it.paymentDate.isNotBlank() }
            .forEach { rec ->
                val parsed = try {
                    sdf.parse(rec.paymentDate) ?: sdfAlt.parse(rec.paymentDate)
                } catch (_: Exception) { null }

                if (parsed != null) {
                    val daysUntil = ((parsed.time - todayMs) / (1000 * 60 * 60 * 24)).toInt()
                    if (daysUntil in 0..3) {
                        val room = allRooms.firstOrNull { it.id == rec.roomId }
                        NotificationHelper.show(
                            ctx,
                            id = 5000 + rec.id.toInt(),
                            channel = NotificationHelper.CHANNEL_REMINDER,
                            title = "Payment approaching — ${room?.name ?: "Room"}",
                            body = "${rec.paymentDate} · ${FormatUtils.formatMoney(rec.remainingAmount, symbol)}",
                            bigText = "Payment for ${rec.billingMonth} is due on ${rec.paymentDate}. " +
                                    "Outstanding: ${FormatUtils.formatMoney(rec.remainingAmount, symbol)}."
                        )
                    }
                }
            }

        return Result.success()
    }
}