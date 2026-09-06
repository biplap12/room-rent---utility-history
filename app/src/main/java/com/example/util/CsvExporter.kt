package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import java.io.File
import java.io.FileWriter

object CsvExporter {

    fun generateCsvContent(room: RoomEntity?, records: List<MonthlyRecordEntity>, currencySymbol: String): String {
        val sb = StringBuilder()
        sb.append("Room,Billing Month,Previous Elec Meter,Current Elec Meter,Elec Units,Elec Rate,Elec Cost,Previous Water Meter,Current Water Meter,Water Units,Water Rate,Water Cost,Room Rent,Waste Charge,Other Charges,Discount,Total Amount,Amount Paid,Remaining Amount,Payment Status,Payment Date,Notes\n")

        for (rec in records) {
            val roomName = (room?.name ?: "Room ${rec.roomId}").replace("\"", "\"\"")
            val notes = rec.notes.replace("\"", "\"\"").replace("\n", " ")
            sb.append("\"$roomName\",")
            sb.append("\"${rec.billingMonth}\",")
            sb.append("${rec.previousElectricityReading},")
            sb.append("${rec.currentElectricityReading},")
            sb.append("${rec.electricityUnits},")
            sb.append("${rec.electricityRate},")
            sb.append("${rec.electricityCost},")
            sb.append("${rec.previousWaterReading},")
            sb.append("${rec.currentWaterReading},")
            sb.append("${rec.waterUnits},")
            sb.append("${rec.waterRate},")
            sb.append("${rec.waterCost},")
            sb.append("${rec.roomRent},")
            sb.append("${rec.wasteCharge},")
            sb.append("${rec.otherCharges},")
            sb.append("${rec.discount},")
            sb.append("${rec.totalAmount},")
            sb.append("${rec.amountPaid},")
            sb.append("${rec.remainingAmount},")
            sb.append("\"${rec.paymentStatus}\",")
            sb.append("\"${rec.paymentDate}\",")
            sb.append("\"$notes\"\n")
        }
        return sb.toString()
    }

    fun shareCsv(context: Context, csvContent: String, filename: String = "Rent_Utility_Records.csv") {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csvContent)
                putExtra(Intent.EXTRA_SUBJECT, "Rent & Utility Export")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Export Rent & Utility CSV")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
