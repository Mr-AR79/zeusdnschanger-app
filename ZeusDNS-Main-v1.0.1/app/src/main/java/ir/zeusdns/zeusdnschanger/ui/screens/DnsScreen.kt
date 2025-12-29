package ir.zeusdns.zeusdnschanger.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.zeusdns.zeusdnschanger.data.DnsServerGroup
import ir.zeusdns.zeusdnschanger.data.ToastData
import ir.zeusdns.zeusdnschanger.data.ToastType
import ir.zeusdns.zeusdnschanger.ui.components.DnsGroupItem
import ir.zeusdns.zeusdnschanger.ui.components.UniversalToast
import ir.zeusdns.zeusdnschanger.ui.theme.CardBackground
import ir.zeusdns.zeusdnschanger.ui.theme.GoldColor
import ir.zeusdns.zeusdnschanger.utils.Localization
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun DnsContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    val isDarkTheme = viewModel.isDarkThemeEnabled()

    LaunchedEffect(Unit) {
        delay(500)
        viewModel.pingAllServers()
    }

    var toastData by remember { mutableStateOf<ToastData?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newDnsName by remember { mutableStateOf("") }
    var newPrimaryDns by remember { mutableStateOf("") }
    var newSecondaryDns by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var primaryDnsError by remember { mutableStateOf<String?>(null) }
    var secondaryDnsError by remember { mutableStateOf<String?>(null) }

    val activeServerId = viewModel.activeServerId
    val customDnsGroups = viewModel.customDnsGroups

    val defaultFreeGroup = DnsServerGroup(
        id = "free",
        name = Localization.getString("dns_free", viewModel),
        primaryAddress = "37.32.5.60",
        secondaryAddress = "37.32.5.61",
        type = "free"
    )

    val defaultProGroup = DnsServerGroup(
        id = "pro",
        name = Localization.getString("dns_pro", viewModel),
        primaryAddress = "37.32.5.34",
        secondaryAddress = "37.32.5.35",
        type = "pro"
    )

    fun copyToClipboard(text: String) {
        val clip = ClipData.newPlainText("DNS Address", text)
        clipboardManager.setPrimaryClip(clip)
        toastData = ToastData(Localization.getString("copied", viewModel), ToastType.SUCCESS)
    }

    fun addCustomDns() {
        nameError = null
        primaryDnsError = null
        secondaryDnsError = null

        val name = newDnsName.trim()
        val primary = newPrimaryDns.trim()

        var hasError = false

        if (name.isBlank()) {
            nameError = Localization.getString("dns_name_required", viewModel)
            hasError = true
        }

        if (primary.isBlank()) {
            primaryDnsError = Localization.getString("primary_dns_required", viewModel)
            hasError = true
        }

        if (hasError) {
            toastData = ToastData(Localization.getString("fill_required_fields", viewModel), ToastType.ERROR)
            return
        }

        val ipPattern = Regex("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")
        if (!ipPattern.matches(primary)) {
            primaryDnsError = Localization.getString("primary_dns_invalid", viewModel)
            toastData = ToastData(Localization.getString("invalid_dns_address", viewModel), ToastType.ERROR)
            return
        }

        val secondary = newSecondaryDns.trim()
        if (secondary.isNotBlank() && !ipPattern.matches(secondary)) {
            secondaryDnsError = Localization.getString("secondary_dns_invalid", viewModel)
            toastData = ToastData(Localization.getString("invalid_dns_address", viewModel), ToastType.ERROR)
            return
        }

        val allDns = listOf(defaultFreeGroup, defaultProGroup) + customDnsGroups
        val exists = allDns.any {
            it.primaryAddress == primary ||
                    (secondary.isNotBlank() && it.secondaryAddress == secondary)
        }
        if (exists) {
            primaryDnsError = Localization.getString("server_already_exists", viewModel)
            if (secondary.isNotBlank()) secondaryDnsError = Localization.getString("server_already_exists", viewModel)
            toastData = ToastData(Localization.getString("server_already_exists", viewModel), ToastType.WARNING)
            return
        }

        val newGroup = DnsServerGroup(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            primaryAddress = primary,
            secondaryAddress = secondary.ifBlank { null },
            type = "custom"
        )

        viewModel.addCustomDns(newGroup)
        toastData = ToastData(Localization.getString("dns_server_added", viewModel), ToastType.SUCCESS)
        newDnsName = ""
        newPrimaryDns = ""
        newSecondaryDns = ""
        showAddDialog = false
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {

            DnsGroupItem(
                server = defaultFreeGroup,
                isActive = activeServerId == "free",
                onSetActive = { viewModel.setActiveServer("free") },
                onCopyAddress = { address -> copyToClipboard(address) },
                onPingServer = { viewModel.pingDnsServer("free") },
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            DnsGroupItem(
                server = defaultProGroup,
                isActive = activeServerId == "pro",
                onSetActive = { viewModel.setActiveServer("pro") },
                onCopyAddress = { address -> copyToClipboard(address) },
                onPingServer = { viewModel.pingDnsServer("pro") },
                viewModel = viewModel
            )

            if (customDnsGroups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = Localization.getString("custom_dns_servers", viewModel),
                    color = GoldColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                customDnsGroups.forEach { group ->
                    DnsGroupItem(
                        server = group,
                        isActive = activeServerId == group.id,
                        onSetActive = { viewModel.setActiveServer(group.id) },
                        onCopyAddress = { address -> copyToClipboard(address) },
                        onPingServer = { viewModel.pingDnsServer(group.id) },
                        onDelete = { viewModel.removeCustomDns(group.id) },
                        isCustom = true,
                        viewModel = viewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = GoldColor,
            contentColor = if (isDarkTheme) Color.Black else Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = Localization.getString("add_custom_dns", viewModel)
            )
        }

        UniversalToast(
            toastData = toastData,
            onDismiss = { toastData = null },
            viewModel = viewModel
        )

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    nameError = null
                    primaryDnsError = null
                    secondaryDnsError = null
                },
                title = {
                    Text(
                        text = Localization.getString("add_dns_server", viewModel),
                        color = if (isDarkTheme) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = Localization.getString("enter_dns_name", viewModel),
                                color = if (isDarkTheme) Color.White.copy(alpha = 0.8f)
                                else Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = newDnsName,
                                onValueChange = {
                                    newDnsName = it
                                    nameError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        Localization.getString("example_dns_name", viewModel),
                                        color = Color.Gray
                                    )
                                },
                                isError = nameError != null,
                                supportingText = {
                                    if (nameError != null) {
                                        Text(
                                            text = nameError ?: "",
                                            color = Color.Red,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                    focusedBorderColor = if (nameError != null) Color.Red else GoldColor,
                                    unfocusedBorderColor = if (nameError != null) Color.Red else GoldColor.copy(alpha = 0.5f),
                                    cursorColor = GoldColor,
                                    errorBorderColor = Color.Red,
                                    errorCursorColor = Color.Red,
                                    errorSupportingTextColor = Color.Red
                                ),
                                singleLine = true
                            )
                        }

                        Column {
                            Text(
                                text = Localization.getString("enter_primary_dns", viewModel),
                                color = if (isDarkTheme) Color.White.copy(alpha = 0.8f)
                                else Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = newPrimaryDns,
                                onValueChange = {
                                    newPrimaryDns = it
                                    primaryDnsError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        Localization.getString("example_primary_dns", viewModel),
                                        color = Color.Gray
                                    )
                                },
                                isError = primaryDnsError != null,
                                supportingText = {
                                    if (primaryDnsError != null) {
                                        Text(
                                            text = primaryDnsError ?: "",
                                            color = Color.Red,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                    focusedBorderColor = if (primaryDnsError != null) Color.Red else GoldColor,
                                    unfocusedBorderColor = if (primaryDnsError != null) Color.Red else GoldColor.copy(alpha = 0.5f),
                                    cursorColor = GoldColor,
                                    errorBorderColor = Color.Red,
                                    errorCursorColor = Color.Red,
                                    errorSupportingTextColor = Color.Red
                                ),
                                singleLine = true
                            )
                        }

                        Column {
                            Text(
                                text = Localization.getString("enter_secondary_dns", viewModel),
                                color = if (isDarkTheme) Color.White.copy(alpha = 0.8f)
                                else Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = newSecondaryDns,
                                onValueChange = {
                                    newSecondaryDns = it
                                    secondaryDnsError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        Localization.getString("example_secondary_dns", viewModel),
                                        color = Color.Gray
                                    )
                                },
                                isError = secondaryDnsError != null,
                                supportingText = {
                                    if (secondaryDnsError != null) {
                                        Text(
                                            text = secondaryDnsError ?: "",
                                            color = Color.Red,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                    focusedBorderColor = if (secondaryDnsError != null) Color.Red else GoldColor.copy(alpha = 0.5f),
                                    unfocusedBorderColor = if (secondaryDnsError != null) Color.Red else GoldColor.copy(alpha = 0.3f),
                                    cursorColor = GoldColor,
                                    errorBorderColor = Color.Red,
                                    errorCursorColor = Color.Red,
                                    errorSupportingTextColor = Color.Red
                                ),
                                singleLine = true
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { addCustomDns() }
                    ) {
                        Text(
                            Localization.getString("add", viewModel),
                            color = GoldColor
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                            nameError = null
                            primaryDnsError = null
                            secondaryDnsError = null
                        }
                    ) {
                        Text(
                            Localization.getString("cancel", viewModel),
                            color = Color.Gray
                        )
                    }
                },
                containerColor = if (isDarkTheme) CardBackground else Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}