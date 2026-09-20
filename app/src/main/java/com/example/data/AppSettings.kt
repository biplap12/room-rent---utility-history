////package com.example.data
////
////import android.content.Context
////import android.content.SharedPreferences
////import kotlinx.coroutines.CoroutineScope
////import kotlinx.coroutines.Dispatchers
////import kotlinx.coroutines.SupervisorJob
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.flow.StateFlow
////import kotlinx.coroutines.flow.asStateFlow
////import kotlinx.coroutines.flow.SharingStarted
////import kotlinx.coroutines.flow.stateIn
////import kotlinx.coroutines.flow.asStateFlow
////class AppSettings(context: Context) {
////    private val prefs: SharedPreferences =
////        context.getSharedPreferences("room_rent_utility_prefs", Context.MODE_PRIVATE)
////
////    private val _currencySymbol = MutableStateFlow(prefs.getString(KEY_CURRENCY, "Rs.") ?: "Rs.")
////    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()
////
////    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
////    private val REMINDERS_KEY = "payment_reminders_enabled"
////
////    private val _paymentRemindersEnabled = MutableStateFlow(
////        prefs.getBoolean(REMINDERS_KEY, true)
////    )
////    val paymentRemindersEnabled: StateFlow<Boolean> = _paymentRemindersEnabled.asStateFlow()
////
////    fun setPaymentRemindersEnabled(enabled: Boolean) {
////        prefs.edit().putBoolean(REMINDERS_KEY, enabled).apply()
////        _paymentRemindersEnabled.value = enabled
////    }
////    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM")
////    val themeMode: StateFlow<String> = _themeMode.asStateFlow()
////
////    private val _selectedRoomId = MutableStateFlow(prefs.getLong(KEY_ROOM_ID, 0L))
////    val selectedRoomId: StateFlow<Long> = _selectedRoomId.asStateFlow()
////
////    fun setCurrency(symbol: String) {
////        prefs.edit().putString(KEY_CURRENCY, symbol).apply()
////        _currencySymbol.value = symbol
////    }
////
////    fun setThemeMode(mode: String) {
////        prefs.edit().putString(KEY_THEME, mode).apply()
////        _themeMode.value = mode
////    }
////
////    fun setSelectedRoomId(roomId: Long) {
////        prefs.edit().putLong(KEY_ROOM_ID, roomId).apply()
////        _selectedRoomId.value = roomId
////    }
////
////    // ═══════════════════════════════════════════════════════════════
////// DELETE PROTECTION — PIN stored as salted SHA-256 hash
////// ═══════════════════════════════════════════════════════════════
////    private val securePrefs = context.getSharedPreferences(
////        "app_secure_settings", android.content.Context.MODE_PRIVATE
////    )
////
////    private val _deleteProtectionEnabled = MutableStateFlow(
////        securePrefs.getBoolean("delete_protection_enabled", false)
////    )
////    val deleteProtectionEnabled: StateFlow<Boolean> = _deleteProtectionEnabled.asStateFlow()
////
////    fun setDeleteProtectionEnabled(enabled: Boolean) {
////        securePrefs.edit().putBoolean("delete_protection_enabled", enabled).apply()
////        _deleteProtectionEnabled.value = enabled
////    }
////
////    fun hasDeletePin(): Boolean = securePrefs.contains("delete_pin_hash")
////
////    fun setDeletePin(pin: String) {
////        val salt = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
////        val hash = hashPin(pin, salt)
////        securePrefs.edit()
////            .putString(
////                "delete_pin_salt",
////                android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP)
////            )
////            .putString(
////                "delete_pin_hash",
////                android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
////            )
////            .apply()
////    }
////
////    fun verifyDeletePin(pin: String): Boolean {
////        val saltStr = securePrefs.getString("delete_pin_salt", null) ?: return false
////        val hashStr = securePrefs.getString("delete_pin_hash", null) ?: return false
////        val salt = android.util.Base64.decode(saltStr, android.util.Base64.NO_WRAP)
////        val expected = android.util.Base64.decode(hashStr, android.util.Base64.NO_WRAP)
////        val actual = hashPin(pin, salt)
////        return java.security.MessageDigest.isEqual(expected, actual)
////    }
////
////    fun clearDeletePin() {
////        securePrefs.edit()
////            .remove("delete_pin_salt")
////            .remove("delete_pin_hash")
////            .apply()
////    }
////
////    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
////        val md = java.security.MessageDigest.getInstance("SHA-256")
////        md.update(salt)
////        var hash = md.digest(pin.toByteArray(Charsets.UTF_8))
////        // 10,000 iterations → ~50 ms of GPU-resistant hashing
////        repeat(10_000) {
////            md.reset()
////            hash = md.digest(hash)
////        }
////        return hash
////    }
////
////    companion object {
////        private const val KEY_CURRENCY = "key_currency_symbol"
////        private const val KEY_THEME = "key_theme_mode"
////        private const val KEY_ROOM_ID = "key_selected_room_id"
////    }
////}
//
//
//
//package com.example.data
//
//import android.content.Context
//import android.content.SharedPreferences
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//
//class AppSettings(context: Context) {
//    private val prefs: SharedPreferences =
//        context.getSharedPreferences("room_rent_utility_prefs", Context.MODE_PRIVATE)
//
//    private val _currencySymbol = MutableStateFlow(prefs.getString(KEY_CURRENCY, "Rs.") ?: "Rs.")
//    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()
//
//    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//    private val REMINDERS_KEY = "payment_reminders_enabled"
//
//    private val _paymentRemindersEnabled = MutableStateFlow(
//        prefs.getBoolean(REMINDERS_KEY, true)
//    )
//    val paymentRemindersEnabled: StateFlow<Boolean> = _paymentRemindersEnabled.asStateFlow()
//
//    fun setPaymentRemindersEnabled(enabled: Boolean) {
//        prefs.edit().putBoolean(REMINDERS_KEY, enabled).apply()
//        _paymentRemindersEnabled.value = enabled
//    }
//
//    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM")
//    val themeMode: StateFlow<String> = _themeMode.asStateFlow()
//
//    private val _selectedRoomId = MutableStateFlow(prefs.getLong(KEY_ROOM_ID, 0L))
//    val selectedRoomId: StateFlow<Long> = _selectedRoomId.asStateFlow()
//
//    fun setCurrency(symbol: String) {
//        prefs.edit().putString(KEY_CURRENCY, symbol).apply()
//        _currencySymbol.value = symbol
//    }
//
//    fun setThemeMode(mode: String) {
//        prefs.edit().putString(KEY_THEME, mode).apply()
//        _themeMode.value = mode
//    }
//
//    fun setSelectedRoomId(roomId: Long) {
//        prefs.edit().putLong(KEY_ROOM_ID, roomId).apply()
//        _selectedRoomId.value = roomId
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // DELETE PROTECTION — PIN stored as salted SHA-256 hash
//    // ═══════════════════════════════════════════════════════════════
//    private val securePrefs = context.getSharedPreferences(
//        "app_secure_settings", Context.MODE_PRIVATE
//    )
//
//    private val _deleteProtectionEnabled = MutableStateFlow(
//        securePrefs.getBoolean("delete_protection_enabled", false)
//    )
//    val deleteProtectionEnabled: StateFlow<Boolean> = _deleteProtectionEnabled.asStateFlow()
//
//    // ★ NEW — reactive hasDeletePin
//    private val _hasDeletePin = MutableStateFlow(
//        securePrefs.contains("delete_pin_hash")
//    )
//    val hasDeletePin: StateFlow<Boolean> = _hasDeletePin.asStateFlow()
//
//    fun setDeleteProtectionEnabled(enabled: Boolean) {
//        securePrefs.edit().putBoolean("delete_protection_enabled", enabled).apply()
//        _deleteProtectionEnabled.value = enabled
//    }
//
//    fun setDeletePin(pin: String) {
//        val salt = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
//        val hash = hashPin(pin, salt)
//        securePrefs.edit()
//            .putString(
//                "delete_pin_salt",
//                android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP)
//            )
//            .putString(
//                "delete_pin_hash",
//                android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
//            )
//            .apply()
//        // ★ notify observers
//        _hasDeletePin.value = true
//    }
//
//    fun verifyDeletePin(pin: String): Boolean {
//        val saltStr = securePrefs.getString("delete_pin_salt", null) ?: return false
//        val hashStr = securePrefs.getString("delete_pin_hash", null) ?: return false
//        val salt = android.util.Base64.decode(saltStr, android.util.Base64.NO_WRAP)
//        val expected = android.util.Base64.decode(hashStr, android.util.Base64.NO_WRAP)
//        val actual = hashPin(pin, salt)
//        return java.security.MessageDigest.isEqual(expected, actual)
//    }
//
//    fun clearDeletePin() {
//        securePrefs.edit()
//            .remove("delete_pin_salt")
//            .remove("delete_pin_hash")
//            .apply()
//        // ★ notify observers
//        _hasDeletePin.value = false
//    }
//
//    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
//        val md = java.security.MessageDigest.getInstance("SHA-256")
//        md.update(salt)
//        var hash = md.digest(pin.toByteArray(Charsets.UTF_8))
//        repeat(10_000) {
//            md.reset()
//            hash = md.digest(hash)
//        }
//        return hash
//    }
//
//    companion object {
//        private const val KEY_CURRENCY = "key_currency_symbol"
//        private const val KEY_THEME = "key_theme_mode"
//        private const val KEY_ROOM_ID = "key_selected_room_id"
//    }
//}


package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("room_rent_utility_prefs", Context.MODE_PRIVATE)

    private val _currencySymbol = MutableStateFlow(prefs.getString(KEY_CURRENCY, "Rs.") ?: "Rs.")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val REMINDERS_KEY = "payment_reminders_enabled"

    private val _paymentRemindersEnabled = MutableStateFlow(
        prefs.getBoolean(REMINDERS_KEY, true)
    )
    val paymentRemindersEnabled: StateFlow<Boolean> = _paymentRemindersEnabled.asStateFlow()

    fun setPaymentRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(REMINDERS_KEY, enabled).apply()
        _paymentRemindersEnabled.value = enabled
    }

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

    // ═══════════════════════════════════════════════════════════════
    // DELETE PROTECTION — PIN stored as salted SHA-256 hash
    // ═══════════════════════════════════════════════════════════════
    private val securePrefs = context.getSharedPreferences(
        "app_secure_settings", Context.MODE_PRIVATE
    )

    init {
        android.util.Log.d("DeletePin", "AppSettings created (instance #${System.identityHashCode(this)})")
        android.util.Log.d("DeletePin", "  read on init: enabled=${securePrefs.getBoolean("delete_protection_enabled", false)} hasPin=${securePrefs.contains("delete_pin_hash")}")
    }

    private val _deleteProtectionEnabled = MutableStateFlow(
        securePrefs.getBoolean("delete_protection_enabled", false)
    )
    val deleteProtectionEnabled: StateFlow<Boolean> = _deleteProtectionEnabled.asStateFlow()

    // ★ NEW — reactive hasDeletePin
    private val _hasDeletePin = MutableStateFlow(
        securePrefs.contains("delete_pin_hash")
    )
    val hasDeletePin: StateFlow<Boolean> = _hasDeletePin.asStateFlow()

    fun setDeleteProtectionEnabled(enabled: Boolean) {
        android.util.Log.d("DeletePin", "setDeleteProtectionEnabled($enabled) called")
        securePrefs.edit().putBoolean("delete_protection_enabled", enabled).apply()
        _deleteProtectionEnabled.value = enabled
    }

    fun setDeletePin(pin: String) {
        val salt = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        android.util.Log.d("DeletePin", "setDeletePin called, len=${pin.length}")

        val hash = hashPin(pin, salt)
        securePrefs.edit()
            .putString(
                "delete_pin_salt",
                android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP)
            )
            .putString(
                "delete_pin_hash",
                android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
            )
            .apply()
        // ★ notify observers
        _hasDeletePin.value = true
        android.util.Log.d("DeletePin", "  wrote hash, hasPin now = ${securePrefs.contains("delete_pin_hash")}")
    }

    fun verifyDeletePin(pin: String): Boolean {
        val saltStr = securePrefs.getString("delete_pin_salt", null) ?: return false
        val hashStr = securePrefs.getString("delete_pin_hash", null) ?: return false
        val salt = android.util.Base64.decode(saltStr, android.util.Base64.NO_WRAP)
        val expected = android.util.Base64.decode(hashStr, android.util.Base64.NO_WRAP)
        val actual = hashPin(pin, salt)
        return java.security.MessageDigest.isEqual(expected, actual)
    }

    fun clearDeletePin() {
        securePrefs.edit()
            .remove("delete_pin_salt")
            .remove("delete_pin_hash")
            .apply()
        // ★ notify observers
        _hasDeletePin.value = false
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        md.update(salt)
        var hash = md.digest(pin.toByteArray(Charsets.UTF_8))
        repeat(10_000) {
            md.reset()
            hash = md.digest(hash)
        }
        return hash
    }

    companion object {
        private const val KEY_CURRENCY = "key_currency_symbol"
        private const val KEY_THEME = "key_theme_mode"
        private const val KEY_ROOM_ID = "key_selected_room_id"
    }
}