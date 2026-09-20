package com.example.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/* ★ Single state machine — only one dialog open at a time */
private enum class ChangeCredentialFlow { NONE, VERIFY, SETUP }

@Composable
fun AppLockSettingsSection(
    config: AppLockConfig,
    onConfigChanged: (AppLockConfig) -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    var showSetupForFirstTime by remember { mutableStateOf(false) }
    var showDisable by remember { mutableStateOf(false) }
    var changeFlow by remember { mutableStateOf(ChangeCredentialFlow.NONE) }

    BeautifulCard(
        modifier = Modifier.fillMaxWidth(),
        accent = cs.primary,
        shape = RoundedCornerShape(LuxeTokens.CardRadius)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            /* ── Header row with toggle ── */
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientIconBadge(
                    icon = Icons.Default.Security,
                    accent = cs.primary,
                    size = 42.dp,
                    iconSize = 20.dp,
                    corner = 12.dp
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "App lock",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (config.enabled)
                            "${if (config.lockType == LockType.PIN) "PIN" else "Password"} protected"
                        else "Protect the app with a PIN or password",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { on ->
                        if (on) showSetupForFirstTime = true
                        else showDisable = true
                    }
                )
            }

            if (config.enabled && config.setupCompleted) {
                Spacer(Modifier.height(12.dp))
                LuxeDivider()
                Spacer(Modifier.height(12.dp))

                /* ── Biometric toggle — persisted immediately ── */
                LuxeToggleRow(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric unlock",
                    subtitle = if (BiometricHelper.canUse(context)) "Available"
                    else "Not available on this device",
                    checked = config.biometricEnabled,
                    onCheckedChange = { enabled ->
                        val updated = config.copy(biometricEnabled = enabled)
                        AppLockPrefs.save(context, updated)
                        onConfigChanged(updated)
                    },
                    accent = cs.primary
                )

                Spacer(Modifier.height(8.dp))
                LuxeDivider()
                Spacer(Modifier.height(8.dp))

                /* ── Change credential — gated behind verification ── */
                LuxeListRow(
                    icon = Icons.Default.Edit,
                    title = "Change ${if (config.lockType == LockType.PIN) "PIN" else "password"}",
                    subtitle = "Verify with current ${if (config.lockType == LockType.PIN) "PIN" else "password"} or biometric",
                    accent = cs.primary,
                    onClick = { changeFlow = ChangeCredentialFlow.VERIFY }
                )
            }
        }
    }

    /* ══════════════════════════════════════════════════════════
       FIRST-TIME SETUP
       ══════════════════════════════════════════════════════════ */
    if (showSetupForFirstTime) {
        AppLockSetupFlow(
            existingConfig = config,
            isVerified = false,
            onDismiss = { showSetupForFirstTime = false },
            onComplete = { newConfig ->
                AppLockPrefs.save(context, newConfig)
                onConfigChanged(newConfig)
                showSetupForFirstTime = false
            }
        )
    }

    /* ══════════════════════════════════════════════════════════
       ★ CHANGE FLOW — single dialog, state-driven swap
       ══════════════════════════════════════════════════════════ */
    when (changeFlow) {

        ChangeCredentialFlow.VERIFY -> AppLockVerifyFlow(
            config = config,
            title = "Verify it's you",
            subtitle = "Enter your current ${if (config.lockType == LockType.PIN) "PIN" else "password"}, or use biometric",
            onDismiss = { changeFlow = ChangeCredentialFlow.NONE },
            onVerified = { changeFlow = ChangeCredentialFlow.SETUP }
        )

        ChangeCredentialFlow.SETUP -> AppLockSetupFlow(
            existingConfig = config,
            isVerified = true,
            onDismiss = { changeFlow = ChangeCredentialFlow.NONE },
            onComplete = { newConfig ->
                AppLockPrefs.save(context, newConfig)
                onConfigChanged(newConfig)
                changeFlow = ChangeCredentialFlow.NONE
            }
        )

        ChangeCredentialFlow.NONE -> Unit
    }

    /* ══════════════════════════════════════════════════════════
       DISABLE FLOW
       ══════════════════════════════════════════════════════════ */
    if (showDisable) {
        AppLockDisableFlow(
            config = config,
            onDismiss = { showDisable = false },
            onConfirmDisable = {
                AppLockPrefs.clear(context)
                onConfigChanged(AppLockConfig())
                showDisable = false
            }
        )
    }
}