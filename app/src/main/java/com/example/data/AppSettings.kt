package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("room_rent_utility_prefs", Context.MODE_PRIVATE)

    private val _currencySymbol = MutableStateFlow(prefs.getString(KEY_CURRENCY, "Rs.") ?: "Rs.")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _selectedRoomId = MutableStateFlow(prefs.getLong(KEY_ROOM_ID, 0L))
    val selectedRoomId: StateFlow<Long> = _selectedRoomId.asStateFlow()

    fun setCurrency(symbol: String) {
        prefs.edit().putString(KEY_CURRENCY, symbol).apply()
        _currencySymbol.value = symbol
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME, mode).apply()
        _themeMode.value = mode
    }

    fun setSelectedRoomId(roomId: Long) {
        prefs.edit().putLong(KEY_ROOM_ID, roomId).apply()
        _selectedRoomId.value = roomId
    }

    companion object {
        private const val KEY_CURRENCY = "key_currency_symbol"
        private const val KEY_THEME = "key_theme_mode"
        private const val KEY_ROOM_ID = "key_selected_room_id"
    }
}
