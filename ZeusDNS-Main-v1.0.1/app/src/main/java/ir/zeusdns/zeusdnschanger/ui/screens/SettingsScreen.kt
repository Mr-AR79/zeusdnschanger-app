package ir.zeusdns.zeusdnschanger.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import ir.zeusdns.zeusdnschanger.data.ToastData
import ir.zeusdns.zeusdnschanger.data.ToastType
import ir.zeusdns.zeusdnschanger.ui.components.UniversalToast
import ir.zeusdns.zeusdnschanger.ui.theme.*
import ir.zeusdns.zeusdnschanger.utils.Localization
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = GoldColor,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
    viewModel: MainViewModel
) {
    val isDarkTheme = viewModel.isDarkThemeEnabled()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) CardBackgroundVariant else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            action?.invoke() ?: Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isDarkTheme = viewModel.isDarkThemeEnabled()

    val appVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (_: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var toastData by remember { mutableStateOf<ToastData?>(null) }

    fun showToastMessage(message: String, type: ToastType = ToastType.SUCCESS) {
        toastData = ToastData(message, type)
    }

    @SuppressLint("UseKtx")
    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        } catch (_: Exception) {
            showToastMessage("Cannot open link", ToastType.ERROR)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) CardBackground else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = GoldColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = Localization.getString("settings", viewModel),
                        color = if (isDarkTheme) Color.White else Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ZeusDNS",
                        color = GoldColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Text(
            text = Localization.getString("appearance", viewModel),
            color = GoldColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp)
        )

        SettingsItem(
            title = Localization.getString("select_theme", viewModel),
            subtitle = when (viewModel.appSettings.themeMode) {
                "dark" -> Localization.getString("dark", viewModel)
                "light" -> Localization.getString("light", viewModel)
                else -> Localization.getString("system", viewModel)
            },
            icon = when (viewModel.appSettings.themeMode) {
                "dark" -> Icons.Default.DarkMode
                "light" -> Icons.Default.LightMode
                else -> Icons.Default.SettingsSuggest
            },
            iconTint = when (viewModel.appSettings.themeMode) {
                "dark" -> PurpleAccent
                "light" -> GoldColor
                else -> BlueInfo
            },
            onClick = { showThemeDialog = true },
            viewModel = viewModel
        )

        Text(
            text = Localization.getString("language", viewModel),
            color = GoldColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp)
        )

        SettingsItem(
            title = Localization.getString("select_language", viewModel),
            subtitle = when (viewModel.appSettings.language) {
                "en" -> Localization.getString("english", viewModel)
                "fa" -> Localization.getString("persian", viewModel)
                else -> Localization.getString("system", viewModel)
            },
            icon = Icons.Default.GTranslate,
            iconTint = OrangeWarning,
            onClick = { showLanguageDialog = true },
            viewModel = viewModel
        )

        Text(
            text = Localization.getString("notifications", viewModel),
            color = GoldColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp)
        )

        SettingsItem(
            title = Localization.getString("show_notifications", viewModel),
            subtitle = Localization.getString("show_connection_notifications", viewModel),
            icon = Icons.Default.Notifications,
            iconTint = GreenSuccess,
            action = {
                Switch(
                    checked = viewModel.appSettings.showNotifications,
                    onCheckedChange = { viewModel.toggleNotifications() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GoldColor,
                        checkedTrackColor = GoldColor.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            },
            viewModel = viewModel
        )

        Text(
            text = Localization.getString("maintenance", viewModel),
            color = GoldColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp)
        )

        SettingsItem(
            title = Localization.getString("clear_cache", viewModel),
            subtitle = "${Localization.getString("cache_size", viewModel)} ${viewModel.cacheSize}",
            icon = Icons.Default.DeleteSweep,
            iconTint = UploadColor,
            onClick = { showClearCacheDialog = true },
            viewModel = viewModel
        )

        SettingsItem(
            title = Localization.getString("reset_settings", viewModel),
            subtitle = Localization.getString("reset_all_settings_to_default", viewModel),
            icon = Icons.Default.RestartAlt,
            iconTint = UploadColor,
            onClick = { showResetDialog = true },
            viewModel = viewModel
        )

        Text(
            text = Localization.getString("about", viewModel),
            color = GoldColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp)
        )

        SettingsItem(
            title = Localization.getString("app_version", viewModel),
            subtitle = "${Localization.getString("current_version", viewModel)} $appVersion",
            icon = Icons.Default.Upgrade,
            iconTint = BlueInfo,
            viewModel = viewModel
        )

        SettingsItem(
            title = Localization.getString("telegram_support", viewModel),
            subtitle = "@ZeusDNS_Support",
            icon = Icons.AutoMirrored.Filled.ContactSupport,
            iconTint = GreenSuccess,
            onClick = { openUrl("https://t.me/ZeusDNS_Support") },
            viewModel = viewModel
        )

        SettingsItem(
            title = Localization.getString("telegram_channel", viewModel),
            subtitle = "@zeusdns_ir",
            icon = Icons.Default.Language,
            iconTint = GoldColor,
            onClick = { openUrl("https://t.me/zeusdns_ir") },
            viewModel = viewModel
        )

        SettingsItem(
            title = Localization.getString("website", viewModel),
            subtitle = "zeusdns.ir",
            icon = Icons.Default.Public,
            iconTint = AddressColor,
            onClick = { openUrl("https://zeusdns.ir/#about") },
            viewModel = viewModel
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) CardBackgroundVariant else Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ZeusDNS",
                    color = GoldColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "© 2025 ZeusDNS. ${Localization.getString("all_rights_reserved", viewModel)}",
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Gray,
                    fontSize = 10.sp
                )
                Text(
                    text = "${Localization.getString("app_version", viewModel)}: $appVersion",
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                    fontSize = 9.sp
                )
            }
        }
    }

    UniversalToast(
        toastData = toastData,
        onDismiss = { toastData = null },
        viewModel = viewModel
    )

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = Localization.getString("select_theme", viewModel),
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    listOf(
                        "system" to Localization.getString("system", viewModel),
                        "dark" to Localization.getString("dark", viewModel),
                        "light" to Localization.getString("light", viewModel)
                    ).forEach { (themeKey, themeName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateTheme(themeKey)
                                    showThemeDialog = false
                                    showToastMessage(Localization.getString("theme_applied", viewModel), ToastType.SUCCESS)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (themeKey) {
                                    "dark" -> Icons.Default.DarkMode
                                    "light" -> Icons.Default.LightMode
                                    else -> Icons.Default.SettingsSuggest
                                },
                                contentDescription = themeName,
                                tint = when (themeKey) {
                                    "dark" -> PurpleAccent
                                    "light" -> GoldColor
                                    else -> BlueInfo
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = themeName,
                                color = if (isDarkTheme) Color.White else Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            if (viewModel.appSettings.themeMode == themeKey) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GoldColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (themeKey != "light") {
                            HorizontalDivider(
                                color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showThemeDialog = false }
                ) {
                    Text(Localization.getString("confirm", viewModel), color = GoldColor)
                }
            },
            containerColor = if (isDarkTheme) CardBackground else Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = Localization.getString("select_language", viewModel),
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    listOf(
                        "system" to Localization.getString("system", viewModel),
                        "fa" to Localization.getString("persian", viewModel),
                        "en" to Localization.getString("english", viewModel)
                    ).forEach { (langKey, langName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateLanguage(langKey)
                                    showLanguageDialog = false
                                    showToastMessage(
                                        Localization.getString("language_restart_message", viewModel),
                                        ToastType.INFO
                                    )
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GTranslate,
                                contentDescription = langName,
                                tint = OrangeWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = langName,
                                color = if (isDarkTheme) Color.White else Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            if (viewModel.appSettings.language == langKey) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GoldColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (langKey != "en") {
                            HorizontalDivider(
                                color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showLanguageDialog = false }
                ) {
                    Text(Localization.getString("confirm", viewModel), color = GoldColor)
                }
            },
            containerColor = if (isDarkTheme) CardBackground else Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = {
                Text(
                    text = Localization.getString("clear_cache", viewModel),
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = Localization.getString("clear_cache_confirmation", viewModel),
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.8f) else Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.clearAppCache()) {
                            showToastMessage(
                                Localization.getString("cache_cleared", viewModel),
                                ToastType.SUCCESS
                            )
                        } else {
                            showToastMessage("Failed to clear cache", ToastType.ERROR)
                        }
                        showClearCacheDialog = false
                    }
                ) {
                    Text(Localization.getString("clear_cache", viewModel), color = UploadColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCacheDialog = false }
                ) {
                    Text(Localization.getString("cancel", viewModel), color = if (isDarkTheme) Color.White.copy(alpha = 0.8f) else Color.Gray)
                }
            },
            containerColor = if (isDarkTheme) CardBackground else Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = Localization.getString("reset_settings", viewModel),
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = Localization.getString("reset_settings_confirmation", viewModel),
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.8f) else Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.resetAllSettings()) {
                            showToastMessage(
                                Localization.getString("settings_reset", viewModel),
                                ToastType.SUCCESS
                            )
                        } else {
                            showToastMessage("Failed to reset settings", ToastType.ERROR)
                        }
                        showResetDialog = false
                    }
                ) {
                    Text(Localization.getString("reset_settings", viewModel), color = UploadColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false }
                ) {
                    Text(Localization.getString("cancel", viewModel), color = if (isDarkTheme) Color.White.copy(alpha = 0.8f) else Color.Gray)
                }
            },
            containerColor = if (isDarkTheme) CardBackground else Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}