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

            // ── palette ──────────────────────────────────────────
            val ink        = Color.parseColor("#0F172A")   // slate-900
            val inkSoft    = Color.parseColor("#475569")   // slate-600
            val inkMuted   = Color.parseColor("#94A3B8")   // slate-400
            val indigo     = Color.parseColor("#4F46E5")   // indigo-600
            val indigoSoft = Color.parseColor("#EEF2FF")   // indigo-50
            val emerald    = Color.parseColor("#10B981")   // emerald-500
            val amber      = Color.parseColor("#F59E0B")   // amber-500
            val rose       = Color.parseColor("#F43F5E")   // rose-500
            val line       = Color.parseColor("#E2E8F0")   // slate-200
            val lineStrong = Color.parseColor("#CBD5E1")   // slate-300

            // Background
            paint.color = Color.WHITE
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            // Header Banner — soft indigo instead of dark slate
            paint.color = indigoSoft
            canvas.drawRect(0f, 0f, 595f, 90f, paint)

            // Accent bar (left edge of header)
            paint.color = indigo
            canvas.drawRect(0f, 0f, 4f, 90f, paint)

            // Header Text
            boldPaint.color = indigo
            boldPaint.textSize = 20f
            canvas.drawText("ROOM RENT & UTILITY STATEMENT", 36f, 45f, boldPaint)

            paint.color = inkSoft
            paint.textSize = 12f
            canvas.drawText("Monthly Billing & Usage Summary", 36f, 68f, paint)

            var y = 120f

// ── Property Details ─────────────────────────────────────────
            boldPaint.color = inkSoft
            boldPaint.textSize = 13f

            paint.color = ink
            paint.textSize = 13f

// Room name
            canvas.drawText("PROPERTY / ROOM:", 36f, y, boldPaint)
            canvas.drawText(room?.name ?: "Room ${record.roomId}", 180f, y, paint)
            y += 20f

// Tenant name (skip if blank)
            if (!room?.tenantName.isNullOrBlank()) {
                canvas.drawText("Tenant Name:", 36f, y, boldPaint)
                canvas.drawText(room.tenantName, 180f, y, paint)
                y += 20f
            }

// Contact (phone + email combined)
            val phone = room?.tenantPhone?.takeIf { it.isNotBlank() }
            val email = room?.tenantEmail?.takeIf { it.isNotBlank() }
            val contact = listOfNotNull(phone, email).joinToString("  •  ")

            if (contact.isNotBlank()) {
                canvas.drawText("Contact:", 36f, y, boldPaint)
                canvas.drawText(contact, 180f, y, paint)
                y += 20f
            }

// Address
            if (!room?.address.isNullOrBlank()) {
                canvas.drawText("Address:", 36f, y, boldPaint)
                canvas.drawText(room.address, 180f, y, paint)
                y += 20f
            }

            boldPaint.textSize = 11f
            canvas.drawText("Billing Period:", 36f, y, boldPaint)
            paint.textSize = 11f
            canvas.drawText(record.billingMonth, 180f, y, paint)

            val statusColor = when (record.paymentStatus) {
                "PAID" -> emerald
                "PARTIALLY_PAID" -> amber
                else -> rose
            }
            boldPaint.color = statusColor
            canvas.drawText("Status: ${record.paymentStatus}", 420f, y, boldPaint)

            y += 30f
            // Divider
            paint.color = line
            paint.strokeWidth = 1.5f
            canvas.drawLine(36f, y, 559f, y, paint)
            y += 25f

            // Table Header — soft indigo strip
            paint.color = indigoSoft
            canvas.drawRect(36f, y - 16f, 559f, y + 10f, paint)

            boldPaint.color = indigo
            boldPaint.textSize = 11f
            canvas.drawText("DESCRIPTION", 46f, y, boldPaint)
            canvas.drawText("READING / UNITS", 260f, y, boldPaint)
            canvas.drawText("RATE", 400f, y, boldPaint)
            canvas.drawText("AMOUNT", 490f, y, boldPaint)

            y += 24f

            fun drawRow(desc: String, unitsInfo: String, rateInfo: String, amount: String) {
                paint.color = ink
                paint.textSize = 11f
                canvas.drawText(desc, 46f, y, paint)
                paint.color = inkSoft
                canvas.drawText(unitsInfo, 260f, y, paint)
                canvas.drawText(rateInfo, 400f, y, paint)
                boldPaint.color = ink
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
            paint.color = lineStrong
            canvas.drawLine(36f, y, 559f, y, paint)
            y += 25f

            // Totals
            boldPaint.textSize = 14f
            boldPaint.color = ink
            canvas.drawText("TOTAL BILL:", 340f, y, boldPaint)
            canvas.drawText(FormatUtils.formatMoney(record.totalAmount, currencySymbol), 470f, y, boldPaint)

            y += 22f
            paint.textSize = 12f
            paint.color = emerald
            canvas.drawText("Amount Paid:", 340f, y, paint)
            canvas.drawText(FormatUtils.formatMoney(record.amountPaid, currencySymbol), 470f, y, paint)

            y += 20f
            boldPaint.textSize = 13f
            boldPaint.color = if (record.remainingAmount > 0) rose else emerald
            canvas.drawText("Remaining Balance:", 340f, y, boldPaint)
            canvas.drawText(FormatUtils.formatMoney(record.remainingAmount, currencySymbol), 470f, y, boldPaint)

//            if (record.notes.isNotBlank()) {
//                y += 40f
//                boldPaint.textSize = 11f
//                boldPaint.color = inkSoft
//                canvas.drawText("Notes / Remarks:", 46f, y, boldPaint)
//                y += 16f
//                paint.textSize = 16f
//                paint.color = inkSoft
//                canvas.drawText(record.notes, 46f, y, paint)
//            }

            // ── Notes (left) + Signature (right) ─────────────────────────
            val notesStartY = y + 40f

// LEFT — Notes
            if (record.notes.isNotBlank()) {
                boldPaint.textSize = 11f
                boldPaint.color = inkSoft
                canvas.drawText("Notes / Remarks:", 46f, notesStartY, boldPaint)

                // Wrap long notes into multiple lines (max width ~ 280px on left half)
                val notesPaint = Paint().apply {
                    textSize = 11f
                    color = inkSoft
                    isAntiAlias = true
                }
                val maxNotesWidth = 280f
                val words = record.notes.split(" ")
                val lines = mutableListOf<String>()
                var currentLine = ""
                for (word in words) {
                    val test = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (notesPaint.measureText(test) <= maxNotesWidth) {
                        currentLine = test
                    } else {
                        if (currentLine.isNotEmpty()) lines.add(currentLine)
                        currentLine = word
                    }
                }
                if (currentLine.isNotEmpty()) lines.add(currentLine)

                var noteY = notesStartY + 16f
                for (line in lines.take(4)) {          // cap at 4 lines to avoid overflow
                    canvas.drawText(line, 46f, noteY, notesPaint)
                    noteY += 14f
                }
            }

// RIGHT — Signature block
            val sigRightX = 380f
            val sigLineWidth = 175f

// Signature line (underline)
            paint.color = lineStrong
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(sigRightX, notesStartY + 45f, sigRightX + sigLineWidth, notesStartY + 45f, paint)
            paint.style = Paint.Style.FILL   // reset for later text draws

// "Signature" label
            boldPaint.textSize = 10f
            boldPaint.color = inkSoft
            canvas.drawText("Signature", sigRightX + 60f, notesStartY + 60f, boldPaint)

            // Footer
            y = 800f
            paint.color = inkMuted
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
            val cleanRoomName = listOfNotNull(room?.name, room?.tenantName)
                .map { it.replace(Regex("[^A-Za-z0-9]+"), "") }   // clean each part
                .filter { it.isNotBlank() }                        // drop blanks
                .joinToString("_")                                 // join with _
                .ifBlank { "Room${record.roomId}" }                // fallback
            val pdfFile = File(
                outputDir,
                "Bill_${cleanRoomName}_${record.billingMonth.replace(" ", "_")}.pdf"
            )
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