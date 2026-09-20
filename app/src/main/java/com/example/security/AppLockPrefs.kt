package com.example.security

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import java.security.MessageDigest
import java.security.SecureRandom

enum class LockType { PIN, PASSWORD }

data class AppLockConfig(
    val enabled: Boolean = false,
    val setupCompleted: Boolean = false,
    val lockType: LockType = LockType.PIN,
    val credentialHash: String = "",
    val credentialSalt: String = "",
    val credentialLength: Int = 6,
    val userName: String = "",
    val userEmail: String = "",
    val q1: String = "", val a1Hash: String = "", val a1Salt: String = "",
    val q2: String = "", val a2Hash: String = "", val a2Salt: String = "",
    val q3: String = "", val a3Hash: String = "", val a3Salt: String = "",
    val biometricEnabled: Boolean = false
)

object AppLockPrefs {
    private const val PREFS = "app_lock_prefs"
    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(ctx: Context): AppLockConfig {
        val p = prefs(ctx)
        return AppLockConfig(
            enabled = p.getBoolean("enabled", false),
            setupCompleted = p.getBoolean("setup", false),
            lockType = runCatching { LockType.valueOf(p.getString("type", "PIN")!!) }
                .getOrDefault(LockType.PIN),
            credentialHash = p.getString("cred_hash", "") ?: "",
            credentialSalt = p.getString("cred_salt", "") ?: "",
            userName = p.getString("name", "") ?: "",
            userEmail = p.getString("email", "") ?: "",
            q1 = p.getString("q1", "") ?: "",
            a1Hash = p.getString("a1_hash", "") ?: "",
            a1Salt = p.getString("a1_salt", "") ?: "",
            q2 = p.getString("q2", "") ?: "",
            a2Hash = p.getString("a2_hash", "") ?: "",
            a2Salt = p.getString("a2_salt", "") ?: "",
            q3 = p.getString("q3", "") ?: "",
            a3Hash = p.getString("a3_hash", "") ?: "",
            a3Salt = p.getString("a3_salt", "") ?: "",
            biometricEnabled = p.getBoolean("bio", false)
        )
    }

    fun save(ctx: Context, c: AppLockConfig) {
        prefs(ctx).edit {
            putBoolean("enabled", c.enabled)
            putBoolean("setup", c.setupCompleted)
            putString("type", c.lockType.name)
            putString("cred_hash", c.credentialHash)
            putString("cred_salt", c.credentialSalt)
            putString("name", c.userName)
            putString("email", c.userEmail)
            putString("q1", c.q1); putString("a1_hash", c.a1Hash); putString("a1_salt", c.a1Salt)
            putString("q2", c.q2); putString("a2_hash", c.a2Hash); putString("a2_salt", c.a2Salt)
            putString("q3", c.q3); putString("a3_hash", c.a3Hash); putString("a3_salt", c.a3Salt)
            putBoolean("bio", c.biometricEnabled)
        }
    }

    fun clear(ctx: Context) = prefs(ctx).edit { clear() }

    fun newSalt(): String {
        val bytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(value: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(Base64.decode(salt, Base64.NO_WRAP))
        md.update(value.trim().lowercase().toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
    }

    fun verify(value: String, salt: String, hash: String): Boolean {
        if (hash.isEmpty() || salt.isEmpty()) return false
        return hash(value, salt) == hash
    }
}