package ir.zeusdns.zeusdnschanger.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.zeusdns.zeusdnschanger.ui.theme.*
import ir.zeusdns.zeusdnschanger.utils.Localization
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalContext
import ir.zeusdns.zeusdnschanger.data.ToastData
import ir.zeusdns.zeusdnschanger.ui.components.UniversalToast
import kotlinx.coroutines.flow.collectLatest
import android.provider.Settings
import androidx.core.net.toUri

@SuppressLint("BatteryLife")
@Composable
fun ProContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isDarkTheme = viewModel.isDarkThemeEnabled()
    val scrollState = rememberScrollState()

    var toastData by remember { mutableStateOf<ToastData?>(null) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { data ->
            toastData = data
        }
    }
    LaunchedEffect(Unit) {
        if (viewModel.liveIpAddress.value == null) {
            viewModel.refreshIpManually()
        }
    }

    fun checkAndRequestBatteryOptimization() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = context.packageName
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = "package:$packageName".toUri()
            }
            context.startActivity(intent)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = Localization.getString("pro_registration", viewModel),
                    color = GoldColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) CardBackground else Color.White
                    ),
                    border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.userToken.value,
                            onValueChange = { viewModel.userToken.value = it },
                            label = {
                                Text(
                                    Localization.getString("enter_your_token", viewModel),
                                    color = GoldColor.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                focusedBorderColor = GoldColor,
                                unfocusedBorderColor = GoldColor.copy(alpha = 0.5f),
                                cursorColor = GoldColor
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                viewModel.submitTokenManually()
                            }),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            enabled = !viewModel.isAutoUpdateActive.value
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) CardBackgroundVariant else Color(0xFFF5F5F5)
                            ),
                            border = BorderStroke(1.dp, AddressColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Public,
                                        null,
                                        tint = AddressColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        Localization.getString("your_ip", viewModel),
                                        color = if (isDarkTheme) Color.White else Color.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = viewModel.liveIpAddress.value ?: Localization.getString("getting_ip_ellipsis", viewModel),
                                    color = if (viewModel.liveIpAddress.value != null) AddressColor else Color.Yellow,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Localization.getString("auto_update_title", viewModel),
                                    color = if (isDarkTheme) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = Localization.getString("auto_update_desc", viewModel),
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            Switch(
                                checked = viewModel.isAutoUpdateEnabledToggle.value,
                                onCheckedChange = { isChecked ->
                                    viewModel.isAutoUpdateEnabledToggle.value = isChecked
                                    if (isChecked) {
                                        checkAndRequestBatteryOptimization()
                                    } else if (viewModel.isAutoUpdateActive.value) {
                                        viewModel.toggleAutoUpdateService()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GoldColor,
                                    checkedTrackColor = GoldColor.copy(alpha = 0.5f),
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color.Transparent,
                                    uncheckedBorderColor = Color.LightGray
                                ),
                                enabled = !viewModel.isAutoUpdateActive.value
                            )
                        }

                        if (viewModel.isAutoUpdateEnabledToggle.value) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFEEEEEE)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = GoldColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = Localization.getString("update_interval", viewModel),
                                                color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Text(
                                            text = "${viewModel.updateIntervalMinutes.intValue} min",
                                            color = GoldColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Slider(
                                        value = viewModel.updateIntervalMinutes.intValue.toFloat(),
                                        onValueChange = { newValue ->
                                            viewModel.setUpdateInterval(newValue.roundToInt())
                                        },
                                        valueRange = 1f..60f,
                                        steps = 0,
                                        colors = SliderDefaults.colors(
                                            thumbColor = GoldColor,
                                            activeTrackColor = GoldColor,
                                            inactiveTrackColor = GoldColor.copy(alpha = 0.2f),
                                            activeTickColor = Color.Transparent,
                                            inactiveTickColor = Color.Transparent
                                        ),
                                        modifier = Modifier.height(20.dp),
                                        enabled = !viewModel.isAutoUpdateActive.value
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("1 min", fontSize = 10.sp, color = Color.Gray)
                                        Text("60 min", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        if (viewModel.executionResult.value != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (viewModel.isSuccessResult.value) Color(
                                        0xFF1B5E20
                                    ) else Color(0xFFB71C1C)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (viewModel.isSuccessResult.value) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = if (viewModel.isSuccessResult.value)
                                                Localization.getString("request_successful", viewModel)
                                            else
                                                Localization.getString("request_failed", viewModel),
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = if (viewModel.isSuccessResult.value)
                                                Localization.getString("request_successful", viewModel)
                                            else
                                                Localization.getString("request_failed", viewModel),
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = viewModel.executionResult.value ?: "",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()

                                if (viewModel.isAutoUpdateActive.value) {
                                    viewModel.toggleAutoUpdateService()
                                } else {
                                    viewModel.submitTokenManually()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    viewModel.isAutoUpdateActive.value -> Color(0xFFB71C1C)
                                    viewModel.userToken.value.isNotBlank() -> GoldColor
                                    else -> GoldColor.copy(alpha = 0.5f)
                                }
                            ),
                            enabled = viewModel.userToken.value.isNotBlank() || viewModel.isAutoUpdateActive.value
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isAutoUpdateActive.value) Icons.Default.Close else Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = if (viewModel.isAutoUpdateActive.value) Color.White else Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (viewModel.isAutoUpdateActive.value)
                                        Localization.getString("stop_auto_update", viewModel)
                                    else
                                        Localization.getString("send_token", viewModel),
                                    color = if (viewModel.isAutoUpdateActive.value) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (viewModel.isAutoUpdateActive.value) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = GoldColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    Localization.getString("monitoring_ip", viewModel),
                                    color = GoldColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
            UniversalToast(
                toastData = toastData,
                onDismiss = { toastData = null },
                viewModel = viewModel
            )
        }
    }
}