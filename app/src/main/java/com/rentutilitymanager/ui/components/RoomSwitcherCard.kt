package com.rentutilitymanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rentutilitymanager.data.RoomEntity
import com.rentutilitymanager.ui.theme.StatusUnpaid
import com.rentutilitymanager.util.FormatUtils

@Composable
fun RoomSwitcherCard(
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity>,
    currencySymbol: String,
    onSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasRoom = activeRoom != null
    val accent = if (hasRoom) MaterialTheme.colorScheme.primary else StatusUnpaid
    val hasTenant = !activeRoom?.tenantName.isNullOrBlank()
    val hasAddress = !activeRoom?.address.isNullOrBlank()
    val hasRent = (activeRoom?.defaultRent ?: 0.0) > 0.0

    BeautifulCard(modifier = modifier.fillMaxWidth(), accent = accent) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                accent,
                                accent.copy(alpha = 0.55f),
                                accent.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(accent, accent.copy(alpha = 0.70f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = propertyTypeIcon(activeRoom?.propertyType),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeRoom?.name ?: "No Room Selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (hasRoom) {
                        if (hasTenant) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = activeRoom?.tenantName ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (hasAddress) {
                            Text(
                                text = activeRoom?.address ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (hasRent) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = FormatUtils.formatMoney(
                                    activeRoom?.defaultRent ?: 0.0,
                                    currencySymbol
                                ) + " / month",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Add a room to get started",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (allRooms.size > 1) {
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        onClick = onSwitchClick,
                        shape = RoundedCornerShape(14.dp),
                        color = accent,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Change room",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}