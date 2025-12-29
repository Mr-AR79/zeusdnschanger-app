package ir.zeusdns.zeusdnschanger.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.zeusdns.zeusdnschanger.ui.theme.*
import ir.zeusdns.zeusdnschanger.utils.Localization
import ir.zeusdns.zeusdnschanger.utils.getIpAddress
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel

@Composable
fun NetworkStatsRow(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    var ipAddress by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val uploadTraffic = remember { derivedStateOf {
        viewModel.formatTraffic(viewModel.totalUploadBytes)
    } }
    val downloadTraffic = remember { derivedStateOf {
        viewModel.formatTraffic(viewModel.totalDownloadBytes)
    } }

    val isDarkTheme = viewModel.isDarkThemeEnabled()

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger >= 0) {
            isLoading = true
            ipAddress = getIpAddress()
            isLoading = false
        }
    }

    LaunchedEffect(viewModel.isConnected) {
        if (viewModel.isConnected) {
            viewModel.updateTrafficStats()
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) CardBackground.copy(alpha = 0.9f)
            else Color.White
        ),
        border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = Localization.getString("upload", viewModel),
                        tint = UploadColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.getString("upload", viewModel),
                        color = UploadColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uploadTraffic.value,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .clickable {
                        refreshTrigger++
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isLoading) Icons.Default.Refresh else Icons.Default.Public,
                        contentDescription = if (isLoading)
                            Localization.getString("getting_ip", viewModel)
                        else
                            Localization.getString("address", viewModel),
                        tint = AddressColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.getString("address", viewModel),
                        color = AddressColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLoading)
                        Localization.getString("getting_ip", viewModel)
                    else (ipAddress ?: Localization.getString("tap_to_get_ip", viewModel)),
                    color = if (isLoading) Color.Yellow.copy(alpha = 0.8f)
                    else if (isDarkTheme) Color.White else Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier.width(100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = Localization.getString("download", viewModel),
                        color = DownloadColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = Localization.getString("download", viewModel),
                        tint = DownloadColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = downloadTraffic.value,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}