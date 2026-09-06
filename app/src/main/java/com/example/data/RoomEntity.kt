package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String = "",
    val defaultRent: Double = 10000.0,
    val electricityRate: Double = 15.0,
    val waterRate: Double = 0.0,
    val wasteCharge: Double = 100.0,
    val createdAt: Long = System.currentTimeMillis()
)
