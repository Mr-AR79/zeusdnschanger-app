package ir.zeusdns.zeusdnschanger.data

import androidx.compose.ui.graphics.vector.ImageVector

enum class ToastType {
    SUCCESS, ERROR, INFO, WARNING
}

data class ToastData(
    val message: String,
    val type: ToastType = ToastType.SUCCESS
)

data class PingResult(
    val success: Boolean,
    val time: String?,
    val error: String?
)

data class NavItem(
    val id: Int,
    val icon: ImageVector,
    val key: String
)

data class DnsServerGroup(
    val id: String,
    val name: String,
    val primaryAddress: String,
    val secondaryAddress: String? = null,
    val type: String, // "free", "pro", "custom"
    var primaryPingStatus: String = "idle",
    var primaryPingTime: String? = null,
    var primaryPingError: String? = null,
    var secondaryPingStatus: String = "idle",
    var secondaryPingTime: String? = null,
    var secondaryPingError: String? = null
)

data class AppSettings(
    val themeMode: String = "system", // "dark", "light", "system"
    val showNotifications: Boolean = true,
    val language: String = "system" // "system", "en", "fa"
)

data class DnsInfo(
    val name: String,
    val primaryAddress: String,
    val secondaryAddress: String? = null,
    val type: String // "free", "pro", "custom"
)