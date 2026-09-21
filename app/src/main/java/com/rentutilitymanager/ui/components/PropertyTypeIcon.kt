package com.rentutilitymanager.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Villa
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


fun propertyTypeIcon(propertyType: String?): ImageVector =
    when (propertyType?.lowercase()?.trim()) {
        "room" -> Icons.Default.MeetingRoom
        "apartment", "flat" -> Icons.Default.Apartment
        "house", "home" -> Icons.Default.Home
        "villa" -> Icons.Default.Villa
        "cottage" -> Icons.Default.Cottage
        "commercial", "office", "shop" -> Icons.Default.Business
        "building" -> Icons.Default.House
        else -> Icons.Default.Home
    } as ImageVector


/**
 * Distinct accent color per property type.
 * Used for chips, badges, and icon tile backgrounds.
 */
fun propertyTypeAccent(propertyType: String?): Color =
    when (propertyType?.lowercase()?.trim()) {
        "room" -> Color(0xFFFF1708)
        "apartment", "flat" -> Color(0xFF3B82F6)   // blue
        "house", "home" -> Color(0xFF10B981)       // emerald
        "villa" -> Color(0xFF8B5CF6)               // violet
        "cottage" -> Color(0xFFF59E0B)             // amber
        "building" -> Color(0xFF6366F1)            // indigo
        "commercial", "office", "shop" -> Color(0xFFEF4444) // red
        else -> Color(0xFF64748B)                  // slate
    }


/**
 * Human-readable label for the property type pill.
 */
fun propertyTypeLabel(propertyType: String?): String =
    when (propertyType?.lowercase()?.trim()) {
        "apartment" -> "Apartment"
        "flat" -> "Flat"
        "house" -> "House"
        "home" -> "Home"
        "villa" -> "Villa"
        "cottage" -> "Cottage"
        "building" -> "Building"
        "commercial" -> "Commercial"
        "office" -> "Office"
        "shop" -> "Shop"
        else -> "Room"
    }

/**
 * All property types shown in the Add Room / Edit Room picker.
 */
data class PropertyTypeOption(
    val label: String,
    val value: String
)

val ALL_PROPERTY_TYPES: List<PropertyTypeOption> = listOf(
    PropertyTypeOption("Apartment", "apartment"),
    PropertyTypeOption("House", "house"),
    PropertyTypeOption("Villa", "villa"),
    PropertyTypeOption("Cottage", "cottage"),
    PropertyTypeOption("Building", "building"),
    PropertyTypeOption("Commercial", "commercial")
)