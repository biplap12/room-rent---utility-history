package com.example.util

import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val rooms: List<RoomEntity>,
    val records: List<MonthlyRecordEntity>
)

object BackupRestoreHelper {

    fun exportToJson(rooms: List<RoomEntity>, records: List<MonthlyRecordEntity>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val roomsArray = JSONArray()
        for (room in rooms) {
            val roomObj = JSONObject().apply {
                put("id", room.id)
                put("name", room.name)
                put("address", room.address)
                put("defaultRent", room.defaultRent)
                put("electricityRate", room.electricityRate)
                put("waterRate", room.waterRate)
                put("wasteCharge", room.wasteCharge)
                put("createdAt", room.createdAt)
            }
            roomsArray.put(roomObj)
        }
        root.put("rooms", roomsArray)

        val recordsArray = JSONArray()
        for (rec in records) {
            val recObj = JSONObject().apply {
                put("id", rec.id)
                put("roomId", rec.roomId)
                put("billingMonth", rec.billingMonth)
                put("previousElectricityReading", rec.previousElectricityReading)
                put("currentElectricityReading", rec.currentElectricityReading)
                put("electricityUnits", rec.electricityUnits)
                put("electricityRate", rec.electricityRate)
                put("electricityCost", rec.electricityCost)
                put("previousWaterReading", rec.previousWaterReading)
                put("currentWaterReading", rec.currentWaterReading)
                put("waterUnits", rec.waterUnits)
                put("waterRate", rec.waterRate)
                put("waterCost", rec.waterCost)
                put("roomRent", rec.roomRent)
                put("wasteCharge", rec.wasteCharge)
                put("otherCharges", rec.otherCharges)
                put("discount", rec.discount)
                put("totalAmount", rec.totalAmount)
                put("amountPaid", rec.amountPaid)
                put("remainingAmount", rec.remainingAmount)
                put("paymentDate", rec.paymentDate)
                put("paymentStatus", rec.paymentStatus)
                put("notes", rec.notes)
                put("createdAt", rec.createdAt)
                put("updatedAt", rec.updatedAt)
            }
            recordsArray.put(recObj)
        }
        root.put("records", recordsArray)

        return root.toString(2)
    }

    fun importFromJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)
        val roomsList = mutableListOf<RoomEntity>()
        val recordsList = mutableListOf<MonthlyRecordEntity>()

        if (root.has("rooms")) {
            val roomsArray = root.getJSONArray("rooms")
            for (i in 0 until roomsArray.length()) {
                val obj = roomsArray.getJSONObject(i)
                roomsList.add(
                    RoomEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", "Imported Room"),
                        address = obj.optString("address", ""),
                        defaultRent = obj.optDouble("defaultRent", 0.0),
                        electricityRate = obj.optDouble("electricityRate", 15.0),
                        waterRate = obj.optDouble("waterRate", 0.0),
                        wasteCharge = obj.optDouble("wasteCharge", 0.0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        if (root.has("records")) {
            val recordsArray = root.getJSONArray("records")
            for (i in 0 until recordsArray.length()) {
                val obj = recordsArray.getJSONObject(i)
                recordsList.add(
                    MonthlyRecordEntity(
                        id = obj.optLong("id", 0L),
                        roomId = obj.optLong("roomId", 1L),
                        billingMonth = obj.optString("billingMonth", ""),
                        previousElectricityReading = obj.optDouble("previousElectricityReading", 0.0),
                        currentElectricityReading = obj.optDouble("currentElectricityReading", 0.0),
                        electricityUnits = obj.optDouble("electricityUnits", 0.0),
                        electricityRate = obj.optDouble("electricityRate", 15.0),
                        electricityCost = obj.optDouble("electricityCost", 0.0),
                        previousWaterReading = obj.optDouble("previousWaterReading", 0.0),
                        currentWaterReading = obj.optDouble("currentWaterReading", 0.0),
                        waterUnits = obj.optDouble("waterUnits", 0.0),
                        waterRate = obj.optDouble("waterRate", 0.0),
                        waterCost = obj.optDouble("waterCost", 0.0),
                        roomRent = obj.optDouble("roomRent", 0.0),
                        wasteCharge = obj.optDouble("wasteCharge", 0.0),
                        otherCharges = obj.optDouble("otherCharges", 0.0),
                        discount = obj.optDouble("discount", 0.0),
                        totalAmount = obj.optDouble("totalAmount", 0.0),
                        amountPaid = obj.optDouble("amountPaid", 0.0),
                        remainingAmount = obj.optDouble("remainingAmount", 0.0),
                        paymentDate = obj.optString("paymentDate", ""),
                        paymentStatus = obj.optString("paymentStatus", "UNPAID"),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        }

        return BackupData(rooms = roomsList, records = recordsList)
    }
}
