package ir.zeusdns.zeusdnschanger.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.zeusdns.zeusdnschanger.data.DnsServerGroup
import ir.zeusdns.zeusdnschanger.ui.theme.*
import ir.zeusdns.zeusdnschanger.utils.Localization
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel

@Composable
fun DnsGroupItem(
    server: DnsServerGroup,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onCopyAddress: (String) -> Unit,
    onPingServer: () -> Unit,
    onDelete: (() -> Unit)? = null,
    isCustom: Boolean = false,
    viewModel: MainViewModel
) {
    val isDarkTheme = viewModel.isDarkThemeEnabled()
    val pingUpdateTrigger by viewModel.pingUpdateTrigger.collectAsState()
    val primaryPingStatus by remember(server.id, pingUpdateTrigger) {
        derivedStateOf { viewModel.getPrimaryPingStatus(server.id) }
    }
    val primaryPingTime by remember(server.id, pingUpdateTrigger) {
        derivedStateOf { viewModel.getPrimaryPingTime(server.id) }
    }
    val primaryPingError by remember(server.id, pingUpdateTrigger) {
        derivedStateOf { viewModel.getPrimaryPingError(server.id) }
    }

    val secondaryPingStatus by remember(server.id, pingUpdateTrigger) {
        derivedStateOf { viewModel.getSecondaryPingStatus(server.id) }
    }
    val secondaryPingTime by remember(server.id, pingUpdateTrigger) {
        derivedStateOf { viewModel.getSecondaryPingTime(server.id) }
    }
    val secondaryPingError by remember(server.id, pingUpdateTrigger) {
        derivedStateOf { viewModel.getSecondaryPingError(server.id) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) CardBackgroundVariant.copy(alpha = 0.9f)
            else Color.White
        ),
        border = BorderStroke(
            2.dp,
            if (isActive) GoldColor else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = when (server.type) {
                            "free" -> Icons.Default.LockOpen
                            "pro" -> Icons.Default.Security
                            else -> Icons.Default.Edit
                        },
                        contentDescription = null,
                        tint = when (server.type) {
                            "free" -> DownloadColor
                            "pro" -> GoldColor
                            else -> AddressColor
                        },
                        modifier = Modifier.size(28.dp)
                    )

                    Column {
                        Text(
                            text = server.name,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onSetActive,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isActive) GoldColor else Color.Gray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = Localization.getString("active", viewModel),
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonUnchecked,
                                    contentDescription = Localization.getString("set_as_active", viewModel),
                                    tint = if (isDarkTheme) Color.White else Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (isCustom && onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = Localization.getString("delete", viewModel),
                                tint = UploadColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DnsAddressRowWithPing(
                    label = Localization.getString("primary_dns", viewModel),
                    address = server.primaryAddress,
                    pingStatus = primaryPingStatus,
                    pingTime = primaryPingTime,
                    pingError = primaryPingError,
                    onCopy = { onCopyAddress(server.primaryAddress) },
                    onPing = { viewModel.pingPrimaryOnly(server.id) },
                    viewModel = viewModel
                )

                server.secondaryAddress?.let { secondaryAddress ->
                    DnsAddressRowWithPing(
                        label = Localization.getString("secondary_dns", viewModel),
                        address = secondaryAddress,
                        pingStatus = secondaryPingStatus,
                        pingTime = secondaryPingTime,
                        pingError = secondaryPingError,
                        onCopy = { onCopyAddress(secondaryAddress) },
                        onPing = { viewModel.pingSecondaryOnly(server.id) },
                        viewModel = viewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(
                    containerColor = when (server.type) {
                        "free" -> DownloadColor.copy(alpha = 0.1f)
                        "pro" -> GoldColor.copy(alpha = 0.1f)
                        else -> AddressColor.copy(alpha = 0.1f)
                    },
                    contentColor = when (server.type) {
                        "free" -> DownloadColor
                        "pro" -> GoldColor
                        else -> AddressColor
                    }
                ) {
                    Text(
                        text = when (server.type) {
                            "free" -> Localization.getString("free_dns_servers", viewModel)
                            "pro" -> Localization.getString("pro_dns_servers", viewModel)
                            else -> Localization.getString("custom_dns_servers", viewModel)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onPingServer() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldColor.copy(alpha = 0.15f),
                        contentColor = GoldColor
                    ),
                    border = BorderStroke(1.dp, GoldColor),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        Localization.getString("ping_all", viewModel),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DnsAddressRowWithPing(
    label: String,
    address: String,
    pingStatus: String,
    pingTime: String?,
    pingError: String?,
    onCopy: () -> Unit,
    onPing: () -> Unit,
    viewModel: MainViewModel
) {
    val isDarkTheme = viewModel.isDarkThemeEnabled()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopy() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) CardBackground.copy(alpha = 0.7f)
            else Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.7f)
                        else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = address,
                        color = if (isDarkTheme) Color.White else Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row {
                    IconButton(
                        onClick = onPing,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = Localization.getString("ping_test", viewModel),
                            tint = when (pingStatus) {
                                "success" -> DownloadColor
                                "failed" -> UploadColor
                                "pinging" -> OrangeWarning
                                else -> GoldColor
                            }
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = Localization.getString("copy_address", viewModel),
                            tint = GoldColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (pingStatus) {
                    "idle" -> Localization.getString("ping_idle", viewModel)
                    "pinging" -> Localization.getString("pinging", viewModel)
                    "success" -> "${Localization.getString("latency", viewModel)} ${pingTime ?: "N/A"}"
                    "failed" -> pingError ?: Localization.getString("ping_failed", viewModel)
                    else -> "وضعیت نامشخص"
                },
                color = when (pingStatus) {
                    "success" -> DownloadColor
                    "failed" -> UploadColor
                    "pinging" -> OrangeWarning
                    else -> Color.Gray
                },
                fontSize = 12.sp
            )
        }
    }
}