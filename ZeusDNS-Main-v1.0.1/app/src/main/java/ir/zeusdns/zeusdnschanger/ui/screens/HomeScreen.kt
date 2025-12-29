package ir.zeusdns.zeusdnschanger.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ir.zeusdns.zeusdnschanger.MyVpnService
import ir.zeusdns.zeusdnschanger.data.DnsInfo
import ir.zeusdns.zeusdnschanger.ui.components.NetworkStatsRow
import ir.zeusdns.zeusdnschanger.ui.theme.*
import ir.zeusdns.zeusdnschanger.utils.Localization
import ir.zeusdns.zeusdnschanger.utils.findActivity
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel

@SuppressLint("DefaultLocale")
@Composable
fun HomeContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val activity = context.findActivity()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (activity == null) {
        Text("Error: No Activity context available")
        return
    }
    val vpnStatus by viewModel.vpnStatus.collectAsState()
    val isConnected = vpnStatus
    val timerSeconds = viewModel.timerSeconds
    val isDarkTheme = viewModel.isDarkThemeEnabled()

    val activeDnsInfo = remember(viewModel.activeServerId, viewModel.customDnsGroups) {
        when (viewModel.activeServerId) {
            "free" -> DnsInfo(
                name = Localization.getString("dns_free", viewModel),
                primaryAddress = "37.32.5.60",
                secondaryAddress = "37.32.5.61",
                type = "free"
            )
            "pro" -> DnsInfo(
                name = Localization.getString("dns_pro", viewModel),
                primaryAddress = "37.32.5.34",
                secondaryAddress = "37.32.5.35",
                type = "pro"
            )
            else -> {
                val customGroup = viewModel.customDnsGroups.find { it.id == viewModel.activeServerId }
                if (customGroup != null) {
                    DnsInfo(
                        name = customGroup.name,
                        primaryAddress = customGroup.primaryAddress,
                        secondaryAddress = customGroup.secondaryAddress,
                        type = "custom"
                    )
                } else {
                    DnsInfo(
                        name = Localization.getString("dns_free", viewModel),
                        primaryAddress = "37.32.5.60",
                        secondaryAddress = "37.32.5.61",
                        type = "free"
                    )
                }
            }
        }
    }

    val formattedTime = remember(timerSeconds) {
        val hours = timerSeconds / 3600
        val minutes = (timerSeconds % 3600) / 60
        val seconds = timerSeconds % 60
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    val buttonColor by animateColorAsState(
        targetValue = if (isConnected) Color(0xFFFF3B30) else GoldColor,
        label = "ButtonColor"
    )

    val vpnLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnService(context, activeDnsInfo)
            viewModel.connect()
        }
    }

    fun toggleVpnConnection() {
        if (isConnected) {
            val intent = Intent(context, MyVpnService::class.java).apply {
                action = "STOP"
            }
            context.startService(intent)
        } else {
            val prepareIntent = VpnService.prepare(context)
            if (prepareIntent != null) {
                vpnLauncher.launch(prepareIntent)
            } else {
                startVpnService(context, activeDnsInfo)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 62.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(
                        PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 26.dp
                        )
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) CardBackgroundVariant
                    else Color.White
                ),
                border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ){
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (activeDnsInfo.type) {
                                        "free" -> DownloadColor.copy(alpha = 0.15f)
                                        "pro" -> GoldColor.copy(alpha = 0.15f)
                                        else -> AddressColor.copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (activeDnsInfo.type) {
                                    "free" -> Icons.Default.LockOpen
                                    "pro" -> Icons.Default.Security
                                    else -> Icons.Default.Edit
                                },
                                contentDescription = "DNS Type",
                                tint = when (activeDnsInfo.type) {
                                    "free" -> DownloadColor
                                    "pro" -> GoldColor
                                    else -> AddressColor
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = Localization.getString("active_dns", viewModel),
                                color = GoldColor.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = activeDnsInfo.name,
                                color = if (isDarkTheme) Color.White else Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Badge(
                            containerColor = when (activeDnsInfo.type) {
                                "free" -> DownloadColor.copy(alpha = 0.2f)
                                "pro" -> GoldColor.copy(alpha = 0.2f)
                                else -> AddressColor.copy(alpha = 0.2f)
                            },
                            contentColor = when (activeDnsInfo.type) {
                                "free" -> DownloadColor
                                "pro" -> GoldColor
                                else -> AddressColor
                            }
                        ) {
                            Text(
                                text = activeDnsInfo.type.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Localization.getString("primary_main", viewModel),
                                color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.width(70.dp)
                            )
                            Text(
                                text = activeDnsInfo.primaryAddress,
                                color = if (isDarkTheme) Color.White else Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (activeDnsInfo.secondaryAddress != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Localization.getString("secondary_main", viewModel),
                                    color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(70.dp)
                                )
                                Text(
                                    text = activeDnsInfo.secondaryAddress,
                                    color = if (isDarkTheme) Color.White else Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) CardBackground
                    else Color.White
                ),
                border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color.Black,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isConnected)
                            Localization.getString("connected", viewModel)
                        else
                            Localization.getString("disconnected", viewModel),
                        color = if (isConnected) GoldColor else Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { toggleVpnConnection() },
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(
                                elevation = 15.dp,
                                shape = RoundedCornerShape(50),
                                spotColor = buttonColor.copy(alpha = 0.5f),
                                ambientColor = buttonColor.copy(alpha = 0.3f)
                            ),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = if (isConnected)
                                Localization.getString("connected", viewModel)
                            else
                                Localization.getString("disconnected", viewModel),
                            modifier = Modifier.size(40.dp),
                            tint = if (isConnected) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        NetworkStatsRow(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            viewModel = viewModel
        )
    }
}

private fun startVpnService(context: Context, dnsInfo: DnsInfo) {
    val intent = Intent(context, MyVpnService::class.java)
    intent.putExtra("primary_dns", dnsInfo.primaryAddress)
    dnsInfo.secondaryAddress?.let { intent.putExtra("secondary_dns", it) }
    context.startService(intent)
}