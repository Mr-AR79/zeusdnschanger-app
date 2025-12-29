package ir.zeusdns.zeusdnschanger.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
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
import ir.zeusdns.zeusdnschanger.data.ToastData
import ir.zeusdns.zeusdnschanger.data.ToastType
import ir.zeusdns.zeusdnschanger.ui.components.UniversalToast
import ir.zeusdns.zeusdnschanger.ui.theme.*
import ir.zeusdns.zeusdnschanger.utils.*
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun ProContent(viewModel: MainViewModel) {
    var token by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var ipAddress by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var toastData by remember { mutableStateOf<ToastData?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isDarkTheme = viewModel.isDarkThemeEnabled()

    LaunchedEffect(Unit) {
        ipAddress = getIpAddress()
    }

    fun submitToken() {
        keyboardController?.hide()
        focusManager.clearFocus()

        if (token.isBlank()) {
            showError = true
            toastData = ToastData(Localization.getString("please_enter_a_token", viewModel), ToastType.ERROR)
            return
        }

        isLoading = true
        showError = false
        resultMessage = null
        showSuccess = false

        coroutineScope.launch {
            try {
                val ip = ipAddress ?: getIpAddress()
                ipAddress = ip

                val result = sendTokenRequest(token, ip)

                if (result.startsWith("Success:")) {
                    val jsonPart = result.substringAfter("Success: ").trim()

                    val message = extractJsonValue(jsonPart, "message") ?: "No message"
                    val username = extractJsonValue(jsonPart, "username") ?: "Unknown"
                    val userIp = extractJsonValue(jsonPart, "ip") ?: ip
                    val lastIp = extractJsonValue(jsonPart, "last_ip") ?: "N/A"
                    val activeIps = extractJsonValueInt(jsonPart, "active_ips") ?: 0
                    val limitation = extractJsonValueInt(jsonPart, "limitation") ?: 0
                    val added = extractJsonValueBoolean(jsonPart, "added") ?: false
                    val alreadyAdded = extractJsonValueBoolean(jsonPart, "already_added") ?: false

                    resultMessage = """
                        ✅ ${Localization.getString("request_successful", viewModel)}
                        
                        👤 Username: $username
                        🌐 IP Address: $userIp
                        📍 Last IP: $lastIp
                        📊 Status: $message
                        
                        🔢 Active IPs: $activeIps / $limitation
                        ✅ Added: ${if (added) "Yes" else "No"}
                        🔄 Already Added: ${if (alreadyAdded) "Yes" else "No"}
                    """.trimIndent()
                    showSuccess = true
                    toastData = ToastData(Localization.getString("request_successful", viewModel), ToastType.SUCCESS)
                } else {
                    resultMessage = "❌ ${Localization.getString("request_failed", viewModel)}: ${result.substringAfter("Error: ")}"
                    showSuccess = false
                    toastData = ToastData(Localization.getString("request_failed", viewModel), ToastType.ERROR)
                }
            } catch (e: Exception) {
                resultMessage = "❌ Error: ${e.message ?: "Unknown error"}"
                showSuccess = false
                toastData = ToastData("Error: ${e.message ?: "Unknown error"}", ToastType.ERROR)
            } finally {
                isLoading = false
            }
        }
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = Localization.getString("pro_registration", viewModel),
                    color = GoldColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) CardBackground else Color.White
                    ),
                    border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        OutlinedTextField(
                            value = token,
                            onValueChange = {
                                token = it
                                showError = false
                                resultMessage = null
                            },
                            label = {
                                Text(
                                    Localization.getString("enter_your_token", viewModel),
                                    color = GoldColor.copy(alpha = 0.7f)
                                )
                            },
                            placeholder = {
                                Text(
                                    Localization.getString("your_token", viewModel),
                                    color = Color.Gray.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                focusedBorderColor = GoldColor,
                                unfocusedBorderColor = GoldColor.copy(alpha = 0.5f),
                                focusedLabelColor = GoldColor,
                                unfocusedLabelColor = GoldColor.copy(alpha = 0.7f),
                                cursorColor = GoldColor
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            isError = showError,
                            supportingText = {
                                if (showError) {
                                    Text(
                                        text = Localization.getString("please_enter_a_token", viewModel),
                                        color = Color.Red,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    submitToken()
                                }
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            )
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
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = Localization.getString("address", viewModel),
                                        tint = AddressColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Localization.getString("your_ip", viewModel),
                                        color = if (isDarkTheme) Color.White else Color.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = ipAddress ?: Localization.getString("getting_ip_ellipsis", viewModel),
                                    color = if (ipAddress != null) AddressColor else Color.Yellow,
                                    fontSize = 14.sp,
                                    fontWeight = if (ipAddress != null) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }

                        if (resultMessage != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (showSuccess) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (showSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = if (showSuccess)
                                                Localization.getString("request_successful", viewModel)
                                            else
                                                Localization.getString("request_failed", viewModel),
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = if (showSuccess)
                                                Localization.getString("request_successful", viewModel)
                                            else
                                                Localization.getString("request_failed", viewModel),
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = resultMessage ?: "",
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )

                                    if (showSuccess) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(
                                            color = Color.White.copy(alpha = 0.3f),
                                            thickness = 1.dp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Info",
                                                tint = Color.White.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = Localization.getString("request_successful", viewModel),
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 12.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { submitToken() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (token.isNotBlank()) GoldColor else GoldColor.copy(alpha = 0.5f)
                            ),
                            enabled = token.isNotBlank() && !isLoading
                        ) {
                            if (isLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        Localization.getString("sending", viewModel),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = Localization.getString("send_token", viewModel),
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        Localization.getString("send_token", viewModel),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            UniversalToast(
                toastData = toastData,
                onDismiss = { toastData = null },
                viewModel = viewModel
            )
        }
    }
}