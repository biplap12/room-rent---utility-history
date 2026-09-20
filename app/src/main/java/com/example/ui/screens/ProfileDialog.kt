package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import com.example.ui.theme.*

@Composable
fun ProfileDialog(
    allRooms: List<RoomEntity>,
    allRecords: List<MonthlyRecordEntity>,
    currencySymbol: String,
    themeMode: String,
    appLockEnabled: Boolean,
    appLockType: String,
    biometricEnabled: Boolean,
    userName: String,
    userEmail: String,
    onClose: () -> Unit,
    onManageRooms: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            BeautifulCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                accent = cs.primary,
                shape = RoundedCornerShape(LuxeTokens.DialogRadius),
                elevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    /* ── Header ── */
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GradientIconBadge(
                            icon = Icons.Default.Person,
                            accent = cs.primary,
                            size = 56.dp,
                            iconSize = 26.dp,
                            corner = 18.dp,
                            topAmount = 0.26f,
                            bottomAmount = 0.10f
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Room Rent Manager",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Offline Meter & Rent Notebook",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(
                                Icons.Default.Close, "Close",
                                tint = cs.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    /* ── Scrollable body ── */
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {

                        /* ── Account ── */
                        if (appLockEnabled && (userName.isNotBlank() || userEmail.isNotBlank())) {
                            LuxeSectionLabel(
                                text = "Account",
                                caption = "Your profile details"
                            )
                            Spacer(Modifier.height(10.dp))
                            LuxeListGroup {
                                ProfileStatRow(
                                    icon = Icons.Default.Person,
                                    label = "Name",
                                    value = userName.ifBlank { "—" },
                                    accent = cs.primary
                                )
                                LuxeDivider()
                                ProfileStatRow(
                                    icon = Icons.Default.Email,
                                    label = "Email",
                                    value = userEmail.ifBlank { "—" },
                                    accent = cs.primary
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                        }

                        /* ── Overview ── */
                        LuxeSectionLabel(
                            text = "Overview",
                            caption = "At-a-glance stats"
                        )
                        Spacer(Modifier.height(10.dp))
                        LuxeListGroup {
                            ProfileStatRow(
                                icon = Icons.Default.Apartment,
                                label = "Rooms",
                                value = allRooms.size.toString(),
                                accent = cs.primary
                            )
                            LuxeDivider()
                            ProfileStatRow(
                                icon = Icons.Default.ReceiptLong,
                                label = "Total Bills",
                                value = allRecords.size.toString(),
                                accent = cs.primary
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        /* ── Preferences ── */
                        LuxeSectionLabel(
                            text = "Preferences",
                            caption = "Appearance and formatting"
                        )
                        Spacer(Modifier.height(10.dp))
                        LuxeListGroup {
                            ProfileStatRow(
                                icon = Icons.Default.CurrencyExchange,
                                label = "Currency",
                                value = currencySymbol,
                                accent = cs.primary
                            )
                            LuxeDivider()
                            ProfileStatRow(
                                icon = Icons.Default.DarkMode,
                                label = "Theme",
                                value = themeMode.replaceFirstChar { it.uppercase() },
                                accent = cs.primary
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        /* ── Security ── */
                        LuxeSectionLabel(
                            text = "Security",
                            caption = "App protection status"
                        )
                        Spacer(Modifier.height(10.dp))
                        LuxeListGroup {
                            ProfileStatRow(
                                icon = if (appLockEnabled) Icons.Default.Lock
                                else Icons.Default.LockOpen,
                                label = "App Lock",
                                value = when {
                                    !appLockEnabled -> "Off"
                                    appLockType.isBlank() -> "Enabled"
                                    else -> "$appLockType protected"
                                },
                                accent = if (appLockEnabled) cs.primary else cs.onSurfaceVariant
                            )
                            if (appLockEnabled) {
                                LuxeDivider()
                                ProfileStatRow(
                                    icon = Icons.Default.Fingerprint,
                                    label = "Biometric",
                                    value = if (biometricEnabled) "Enabled" else "Disabled",
                                    accent = if (biometricEnabled) cs.primary
                                    else cs.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    /* ── Actions ── */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LuxeSecondaryButton(
                            text = "Rooms",
                            icon = Icons.Default.Apartment,
                            onClick = onManageRooms,
                            modifier = Modifier.weight(1f)
                        )
                        LuxePrimaryButton(
                            text = "Settings",
                            icon = Icons.Default.Settings,
                            onClick = onOpenSettings,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }
    }
}

/* ── The ONE and only ProfileStatRow ── */
@Composable
private fun ProfileStatRow(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color
) {
    val cs = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GradientIconBadge(
            icon = icon,
            accent = accent,
            size = 32.dp,
            iconSize = 16.dp,
            corner = 10.dp,
            topAmount = 0.20f,
            bottomAmount = 0.06f
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = cs.onSurface,
            maxLines = 1,
            textAlign = TextAlign.End
        )
    }
}