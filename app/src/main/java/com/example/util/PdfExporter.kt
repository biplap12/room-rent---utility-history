package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import java.io.File
import java.io.FileOutputStream
import com.example.R

object PdfExporter {

    fun generateAndShareBillPdf(
        context: Context,
        room: RoomEntity?,
        record: MonthlyRecordEntity,
        currencySymbol: String
    ): Boolean {
        return try {
            val pdfDoc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDoc.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val boldPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            // Background
            paint.color = Color.WHITE
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            // Header Banner
            paint.color = Color.parseColor("#0F172A") // Deep slate
            canvas.drawRect(0f, 0f, 595f, 90f, paint)

            // Header Text
            boldPaint.color = Color.WHITE
            boldPaint.textSize = 20f
            canvas.drawText("ROOM RENT & UTILITY STATEMENT", 36f, 45f, boldPaint)

            paint.color = Color.parseColor("#94A3B8")
            paint.textSize = 12f
            canvas.drawText("Monthly Billing & Usage Summary", 36f, 68f, paint)

            var y = 120f

            // Property Details
            boldPaint.color = Color.parseColor("#1E293B")
            boldPaint.textSize = 14f
            canvas.drawText("PROPERTY / ROOM:", 36f, y, boldPaint)
            paint.color = Color.parseColor("#334155")
            paint.textSize = 14f
            canvas.drawText(room?.name ?: "Room ${record.roomId}", 180f, y, paint)

            y += 22f
            if (!room?.address.isNullOrBlank()) {
                boldPaint.textSize = 11f
                canvas.drawText("Address:", 36f, y, boldPaint)
                paint.textSize = 11f
                canvas.drawText(room?.address ?: "", 180f, y, paint)
                y += 20f
            }

            boldPaint.textSize = 11f
            canvas.drawText("Billing Period:", 36f, y, boldPaint)
            paint.textSize = 11f
            canvas.drawText(record.billingMonth, 180f, y, paint)

            val statusColor = when (record.paymentStatus) {
                "PAID" -> Color.parseColor("#16A34A")
                "PARTIALLY_PAID" -> Color.parseColor("#D97706")
                else -> Color.parseColor("#DC2626")
            }
            boldPaint.color = statusColor
            canvas.drawText("Status: ${record.paymentStatus}", 420f, y, boldPaint)

            y += 30f
            // Divider
            paint.color = Color.parseColor("#E2E8F0")
            paint.strokeWidth = 1.5f
            canvas.drawLine(36f, y, 559f, y, paint)
            y += 25f

            // Table Header
            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRect(36f, y - 16f, 559f, y + 10f, paint)

            boldPaint.color = Color.parseColor("#475569")
            boldPaint.textSize = 11f
            canvas.drawText("DESCRIPTION", 46f, y, boldPaint)
            canvas.drawText("READING / UNITS", 260f, y, boldPaint)
            canvas.drawText("RATE", 400f, y, boldPaint)
            canvas.drawText("AMOUNT", 490f, y, boldPaint)

            y += 24f

            fun drawRow(desc: String, unitsInfo: String, rateInfo: String, amount: String) {
                paint.color = Color.parseColor("#1E293B")
                paint.textSize = 11f
                canvas.drawText(desc, 46f, y, paint)
                paint.color = Color.parseColor("#64748B")
                canvas.drawText(unitsInfo, 260f, y, paint)
                canvas.drawText(rateInfo, 400f, y, paint)
                boldPaint.color = Color.parseColor("#0F172A")
                boldPaint.textSize = 11f
                canvas.drawText(amount, 490f, y, boldPaint)
                y += 22f
            }

            drawRow("Room Rent", "-", "-", FormatUtils.formatMoney(record.roomRent, currencySymbol))
            drawRow(
                "Electricity",
                "${FormatUtils.formatUnits(record.previousElectricityReading)} → ${FormatUtils.formatUnits(record.currentElectricityReading)} (${FormatUtils.formatUnits(record.electricityUnits)} u)",
                FormatUtils.formatMoney(record.electricityRate, currencySymbol),
                FormatUtils.formatMoney(record.electricityCost, currencySymbol)
            )
            if (record.waterUnits > 0 || record.waterCost > 0) {
                drawRow(
                    "Water",
                    "${FormatUtils.formatUnits(record.previousWaterReading)} → ${FormatUtils.formatUnits(record.currentWaterReading)} (${FormatUtils.formatUnits(record.waterUnits)} u)",
                    FormatUtils.formatMoney(record.waterRate, currencySymbol),
                    FormatUtils.formatMoney(record.waterCost, currencySymbol)
                )
            }
            if (record.wasteCharge > 0) {
                drawRow("Waste / Minimum Charge", "-", "-", FormatUtils.formatMoney(record.wasteCharge, currencySymbol))
            }
            if (record.otherCharges > 0) {
                drawRow("Other Charges", "-", "-", FormatUtils.formatMoney(record.otherCharges, currencySymbol))
            }
            if (record.discount > 0) {
                drawRow("Discount", "-", "-", "- " + FormatUtils.formatMoney(record.discount, currencySymbol))
            }

            y += 10f
            // Divider
            paint.color = Color.parseColor("#CBD5E1")
            canvas.drawLine(36f, y, 559f, y, paint)
            y += 25f

            // Totals
            boldPaint.textSize = 14f
            boldPaint.color = Color.parseColor("#0F172A")
            canvas.drawText("TOTAL BILL:", 340f, y, boldPaint)
            canvas.drawText(FormatUtils.formatMoney(record.totalAmount, currencySymbol), 470f, y, boldPaint)

            y += 22f
            paint.textSize = 12f
            paint.color = Color.parseColor("#16A34A")
            canvas.drawText("Amount Paid:", 340f, y, paint)
            canvas.drawText(FormatUtils.formatMoney(record.amountPaid, currencySymbol), 470f, y, paint)

            y += 20f
            boldPaint.textSize = 13f
            boldPaint.color = if (record.remainingAmount > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
            canvas.drawText("Remaining Balance:", 340f, y, boldPaint)
            canvas.drawText(FormatUtils.formatMoney(record.remainingAmount, currencySymbol), 470f, y, boldPaint)

            if (record.notes.isNotBlank()) {
                y += 40f
                boldPaint.textSize = 11f
                boldPaint.color = Color.parseColor("#334155")
                canvas.drawText("Notes / Remarks:", 46f, y, boldPaint)
                y += 16f
                paint.textSize = 10f
                paint.color = Color.parseColor("#64748B")
                canvas.drawText(record.notes, 46f, y, paint)
            }

            // Footer
            y = 800f
            paint.color = Color.parseColor("#94A3B8")
            paint.textSize = 9f
            canvas.drawText(
                "Generated by Room Rent & Utility History • Bill By ${context.getString(R.string.author_name)}.",
                150f,
                y,
                paint
            )
            pdfDoc.finishPage(page)

            // Save PDF to cache
            val outputDir = File(context.cacheDir, "reports")
            if (!outputDir.exists()) outputDir.mkdirs()
            val pdfFile = File(outputDir, "Bill_${record.billingMonth.replace(" ", "_")}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()

            // Share via FileProvider or simple share intent
            val authority = "${context.packageName}.fileprovider"
            val fileUri = try {
                FileProvider.getUriForFile(context, authority, pdfFile)
            } catch (e: Exception) {
                null
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                if (fileUri != null) {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Bill for ${record.billingMonth}: Total ${FormatUtils.formatMoney(record.totalAmount, currencySymbol)}, Paid: ${FormatUtils.formatMoney(record.amountPaid, currencySymbol)}")
                }
                putExtra(Intent.EXTRA_SUBJECT, "Rent & Utility Bill - ${record.billingMonth}")
            }
            val chooser = Intent.createChooser(shareIntent, "Share Bill PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
