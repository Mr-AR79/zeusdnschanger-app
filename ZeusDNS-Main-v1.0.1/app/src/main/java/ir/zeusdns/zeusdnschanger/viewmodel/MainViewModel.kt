package ir.zeusdns.zeusdnschanger.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.zeusdns.zeusdnschanger.MyVpnService
import ir.zeusdns.zeusdnschanger.VpnStatusManager
import ir.zeusdns.zeusdnschanger.data.AppSettings
import ir.zeusdns.zeusdnschanger.data.DnsServerGroup
import ir.zeusdns.zeusdnschanger.data.PingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("ZeusDNS_Prefs", Context.MODE_PRIVATE)

    var isConnected by mutableStateOf(false)
        private set
    var timerSeconds by mutableLongStateOf(0L)
        private set
    private var timerJob: Job? = null

    var totalUploadBytes by mutableLongStateOf(0L)
        private set
    var totalDownloadBytes by mutableLongStateOf(0L)
        private set

    private var initialTxBytes: Long = 0L
    private var initialRxBytes: Long = 0L

    private val _customDnsGroups = mutableStateListOf<DnsServerGroup>()
    val customDnsGroups: List<DnsServerGroup> get() = _customDnsGroups

    var activeServerId by mutableStateOf(prefs.getString("active_server_id", "free") ?: "free")
        private set

    var appSettings by mutableStateOf(
        AppSettings(
            themeMode = prefs.getString("theme_mode", "system") ?: "system",
            showNotifications = prefs.getBoolean("show_notifications", true),
            language = prefs.getString("language", "system") ?: "system"
        )
    )
        private set

    var cacheSize by mutableStateOf("0 KB")
        private set

    private val _pingUpdateTrigger = MutableStateFlow(0)
    val pingUpdateTrigger: StateFlow<Int> = _pingUpdateTrigger

    init {
        loadCustomDns()
        updateCacheSize()
    }

    fun isDarkThemeEnabled(): Boolean {
        val context = getApplication<Application>().applicationContext
        return when (appSettings.themeMode) {
            "dark" -> true
            "light" -> false
            else -> isSystemInDarkTheme(context)
        }
    }

    private fun isSystemInDarkTheme(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    fun updateCacheSize() {
        val context = getApplication<Application>().applicationContext
        cacheSize = calculateCacheSize(context)
    }

    @SuppressLint("DefaultLocale")
    private fun calculateCacheSize(context: Context): String {
        try {
            val cacheDir = context.cacheDir
            var size = 0L

            if (cacheDir.exists() && cacheDir.isDirectory) {
                cacheDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        size += file.length()
                    }
                }
            }

            return when {
                size >= 1024 * 1024 -> String.format("%.2f MB", size / (1024.0 * 1024.0))
                size >= 1024 -> String.format("%.2f KB", size / 1024.0)
                else -> "$size B"
            }
        } catch (_: Exception) {
            return "0 KB"
        }
    }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isConnected) {
                delay(1000)
                timerSeconds++
                updateTrafficStats()
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    fun setActiveServer(id: String) {
        activeServerId = id
        prefs.edit { putString("active_server_id", id) }
    }

    fun addCustomDns(group: DnsServerGroup) {
        _customDnsGroups.add(group)
        saveCustomDns()
    }

    fun removeCustomDns(id: String) {
        _customDnsGroups.removeAll { it.id == id }
        if (activeServerId == id) {
            setActiveServer("free")
        }
        saveCustomDns()
    }

    fun updateTheme(themeMode: String) {
        appSettings = appSettings.copy(themeMode = themeMode)
        saveSettings()
    }

    fun updateLanguage(language: String) {
        appSettings = appSettings.copy(language = language)
        saveSettings()
    }

    fun toggleNotifications() {
        val current = appSettings
        val newSettings = current.copy(showNotifications = !current.showNotifications)
        appSettings = newSettings
        saveSettings()
        updateVpnServiceNotificationSettings()
    }

    private fun updateVpnServiceNotificationSettings() {
        val context = getApplication<Application>().applicationContext

        if (isConnected) {
            val intent = Intent(context, MyVpnService::class.java)
            intent.action = MyVpnService.ACTION_UPDATE_SETTINGS
            intent.putExtra(MyVpnService.EXTRA_SHOW_NOTIFICATIONS, appSettings.showNotifications)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun clearAppCache(): Boolean {
        val context = getApplication<Application>().applicationContext
        return try {
            val cacheDir = context.cacheDir
            if (cacheDir.exists() && cacheDir.isDirectory) {
                cacheDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        file.delete()
                    }
                }
            }

            context.cacheDir?.let { dir ->
                dir.deleteRecursively()
                dir.mkdirs()
            }

            updateCacheSize()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resetAllSettings(): Boolean {
        return try {
            appSettings = AppSettings()
            saveSettings()

            _customDnsGroups.clear()
            prefs.edit { remove("custom_dns_count") }

            prefs.edit { putString("active_server_id", "free") }
            activeServerId = "free"

            clearAppCache()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun pingAllServers() {
        viewModelScope.launch {
            pingDnsServer("free")
            delay(500)
            pingDnsServer("pro")

            _customDnsGroups.forEachIndexed { index, server ->
                delay(index * 300L)
                pingDnsServer(server.id)
            }
        }
    }

    fun pingDnsServer(serverId: String, pingSecondary: Boolean = true) {
        viewModelScope.launch {
            val server = when (serverId) {
                "free" -> DnsServerGroup(
                    id = "free",
                    name = "Free DNS",
                    primaryAddress = "37.32.5.60",
                    secondaryAddress = "37.32.5.61",
                    type = "free"
                )
                "pro" -> DnsServerGroup(
                    id = "pro",
                    name = "Pro DNS",
                    primaryAddress = "37.32.5.34",
                    secondaryAddress = "37.32.5.35",
                    type = "pro"
                )
                else -> _customDnsGroups.find { it.id == serverId }
            } ?: return@launch

            when (serverId) {
                "free" -> {
                    _freePrimaryStatus.value = "pinging"
                    _freePrimaryTime.value = null
                    _freePrimaryError.value = null
                }
                "pro" -> {
                    _proPrimaryStatus.value = "pinging"
                    _proPrimaryTime.value = null
                    _proPrimaryError.value = null
                }
                else -> {
                    server.primaryPingStatus = "pinging"
                    server.primaryPingTime = null
                    server.primaryPingError = null
                }
            }

            val primaryResult = withContext(Dispatchers.IO) {
                executePingCommand(server.primaryAddress)
            }

            when (serverId) {
                "free" -> {
                    _freePrimaryStatus.value = if (primaryResult.success) "success" else "failed"
                    _freePrimaryTime.value = primaryResult.time
                    _freePrimaryError.value = primaryResult.error
                }
                "pro" -> {
                    _proPrimaryStatus.value = if (primaryResult.success) "success" else "failed"
                    _proPrimaryTime.value = primaryResult.time
                    _proPrimaryError.value = primaryResult.error
                }
                else -> {
                    server.primaryPingStatus = if (primaryResult.success) "success" else "failed"
                    server.primaryPingTime = primaryResult.time
                    server.primaryPingError = primaryResult.error
                }
            }

            if (pingSecondary && server.secondaryAddress != null) {
                delay(500)

                when (serverId) {
                    "free" -> {
                        _freeSecondaryStatus.value = "pinging"
                        _freeSecondaryTime.value = null
                        _freeSecondaryError.value = null
                    }
                    "pro" -> {
                        _proSecondaryStatus.value = "pinging"
                        _proSecondaryTime.value = null
                        _proSecondaryError.value = null
                    }
                    else -> {
                        server.secondaryPingStatus = "pinging"
                        server.secondaryPingTime = null
                        server.secondaryPingError = null
                    }
                }

                val secondaryResult = withContext(Dispatchers.IO) {
                    executePingCommand(server.secondaryAddress)
                }

                when (serverId) {
                    "free" -> {
                        _freeSecondaryStatus.value = if (secondaryResult.success) "success" else "failed"
                        _freeSecondaryTime.value = secondaryResult.time
                        _freeSecondaryError.value = secondaryResult.error
                    }
                    "pro" -> {
                        _proSecondaryStatus.value = if (secondaryResult.success) "success" else "failed"
                        _proSecondaryTime.value = secondaryResult.time
                        _proSecondaryError.value = secondaryResult.error
                    }
                    else -> {
                        server.secondaryPingStatus = if (secondaryResult.success) "success" else "failed"
                        server.secondaryPingTime = secondaryResult.time
                        server.secondaryPingError = secondaryResult.error
                    }
                }
            }
            _pingUpdateTrigger.value++
        }
    }

    fun pingPrimaryOnly(serverId: String) {
        pingDnsServer(serverId, pingSecondary = false)
    }

    fun pingSecondaryOnly(serverId: String) {
        viewModelScope.launch {
            val server = when (serverId) {
                "free" -> DnsServerGroup(
                    id = "free",
                    name = "Free DNS",
                    primaryAddress = "37.32.5.60",
                    secondaryAddress = "37.32.5.61",
                    type = "free"
                )
                "pro" -> DnsServerGroup(
                    id = "pro",
                    name = "Pro DNS",
                    primaryAddress = "37.32.5.34",
                    secondaryAddress = "37.32.5.35",
                    type = "pro"
                )
                else -> _customDnsGroups.find { it.id == serverId }
            } ?: return@launch

            if (server.secondaryAddress != null) {
                when (serverId) {
                    "free" -> {
                        _freeSecondaryStatus.value = "pinging"
                        _freeSecondaryTime.value = null
                        _freeSecondaryError.value = null
                    }
                    "pro" -> {
                        _proSecondaryStatus.value = "pinging"
                        _proSecondaryTime.value = null
                        _proSecondaryError.value = null
                    }
                    else -> {
                        server.secondaryPingStatus = "pinging"
                        server.secondaryPingTime = null
                        server.secondaryPingError = null
                    }
                }

                val result = withContext(Dispatchers.IO) {
                    executePingCommand(server.secondaryAddress)
                }

                when (serverId) {
                    "free" -> {
                        _freeSecondaryStatus.value = if (result.success) "success" else "failed"
                        _freeSecondaryTime.value = result.time
                        _freeSecondaryError.value = result.error
                    }
                    "pro" -> {
                        _proSecondaryStatus.value = if (result.success) "success" else "failed"
                        _proSecondaryTime.value = result.time
                        _proSecondaryError.value = result.error
                    }
                    else -> {
                        server.secondaryPingStatus = if (result.success) "success" else "failed"
                        server.secondaryPingTime = result.time
                        server.secondaryPingError = result.error
                    }
                }
                _pingUpdateTrigger.value++
            }
        }
    }

    private suspend fun executePingCommand(address: String): PingResult {
        return withContext(Dispatchers.IO) {
            try {
                val inetAddress = java.net.InetAddress.getByName(address)
                val startTime = System.currentTimeMillis()
                val isReachable = inetAddress.isReachable(2000)
                val endTime = System.currentTimeMillis()

                if (isReachable) {
                    val timeMs = endTime - startTime
                    PingResult(success = true, time = "${timeMs}ms", error = null)
                } else {
                    try {
                        val socket = java.net.Socket()
                        val port = 53

                        val connectStart = System.currentTimeMillis()
                        socket.connect(java.net.InetSocketAddress(address, port), 3000)
                        val connectEnd = System.currentTimeMillis()
                        socket.close()

                        val timeMs = connectEnd - connectStart
                        PingResult(success = true, time = "${timeMs}ms", error = "TCP Connected")
                    } catch (_: Exception) {
                        PingResult(success = false, time = null, error = "-1")
                    }
                }
            } catch (e: Exception) {
                PingResult(success = false, time = null, error = "Error: ${e.message}")
            }
        }
    }


    private val _freePrimaryStatus = MutableStateFlow("idle")
    private val _freePrimaryTime = MutableStateFlow<String?>(null)
    private val _freePrimaryError = MutableStateFlow<String?>(null)
    private val _freeSecondaryStatus = MutableStateFlow("idle")
    private val _freeSecondaryTime = MutableStateFlow<String?>(null)
    private val _freeSecondaryError = MutableStateFlow<String?>(null)

    private val _proPrimaryStatus = MutableStateFlow("idle")
    private val _proPrimaryTime = MutableStateFlow<String?>(null)
    private val _proPrimaryError = MutableStateFlow<String?>(null)
    private val _proSecondaryStatus = MutableStateFlow("idle")
    private val _proSecondaryTime = MutableStateFlow<String?>(null)
    private val _proSecondaryError = MutableStateFlow<String?>(null)

    fun getPrimaryPingStatus(serverId: String): String {
        return when (serverId) {
            "free" -> _freePrimaryStatus.value
            "pro" -> _proPrimaryStatus.value
            else -> _customDnsGroups.find { it.id == serverId }?.primaryPingStatus ?: "idle"
        }
    }

    fun getPrimaryPingTime(serverId: String): String? {
        return when (serverId) {
            "free" -> _freePrimaryTime.value
            "pro" -> _proPrimaryTime.value
            else -> _customDnsGroups.find { it.id == serverId }?.primaryPingTime
        }
    }

    fun getPrimaryPingError(serverId: String): String? {
        return when (serverId) {
            "free" -> _freePrimaryError.value
            "pro" -> _proPrimaryError.value
            else -> _customDnsGroups.find { it.id == serverId }?.primaryPingError
        }
    }

    fun getSecondaryPingStatus(serverId: String): String {
        return when (serverId) {
            "free" -> _freeSecondaryStatus.value
            "pro" -> _proSecondaryStatus.value
            else -> _customDnsGroups.find { it.id == serverId }?.secondaryPingStatus ?: "idle"
        }
    }

    fun getSecondaryPingTime(serverId: String): String? {
        return when (serverId) {
            "free" -> _freeSecondaryTime.value
            "pro" -> _proSecondaryTime.value
            else -> _customDnsGroups.find { it.id == serverId }?.secondaryPingTime
        }
    }

    fun getSecondaryPingError(serverId: String): String? {
        return when (serverId) {
            "free" -> _freeSecondaryError.value
            "pro" -> _proSecondaryError.value
            else -> _customDnsGroups.find { it.id == serverId }?.secondaryPingError
        }
    }

    private fun saveSettings() {
        prefs.edit {
            putString("theme_mode", appSettings.themeMode)
            putBoolean("show_notifications", appSettings.showNotifications)
            putString("language", appSettings.language)
        }
    }

    private fun saveCustomDns() {
        prefs.edit {
            putInt("custom_dns_count", _customDnsGroups.size)
            _customDnsGroups.forEachIndexed { index, group ->
                putString("dns_${index}_id", group.id)
                putString("dns_${index}_name", group.name)
                putString("dns_${index}_primary", group.primaryAddress)
                putString("dns_${index}_secondary", group.secondaryAddress)
            }
        }
    }

    private fun loadCustomDns() {
        val count = prefs.getInt("custom_dns_count", 0)
        _customDnsGroups.clear()
        for (i in 0 until count) {
            val id = prefs.getString("dns_${i}_id", "") ?: continue
            val name = prefs.getString("dns_${i}_name", "") ?: ""
            val primary = prefs.getString("dns_${i}_primary", "") ?: ""
            val secondary = prefs.getString("dns_${i}_secondary", null)

            if (id.isNotEmpty()) {
                _customDnsGroups.add(
                    DnsServerGroup(id, name, primary, secondary, "custom")
                )
            }
        }
    }

    private val _vpnStatus = MutableStateFlow(false)
    val vpnStatus: StateFlow<Boolean> = _vpnStatus

    init {
        viewModelScope.launch {
            VpnStatusManager.vpnStatus.collect { isConnected ->
                _vpnStatus.value = isConnected
                updateConnectionStatus(isConnected)
            }
        }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        if (isConnected) connect() else disconnect()
    }

    fun connect() {
        isConnected = true
        timerSeconds = 0L
        initialTxBytes = android.net.TrafficStats.getTotalTxBytes()
        initialRxBytes = android.net.TrafficStats.getTotalRxBytes()
        totalUploadBytes = 0L
        totalDownloadBytes = 0L
        startTimer()
    }

    fun disconnect() {
        isConnected = false
        stopTimer()
        initialTxBytes = 0L
        initialRxBytes = 0L
    }

    fun updateTrafficStats() {
        if (!isConnected) return

        val currentTx = android.net.TrafficStats.getTotalTxBytes()
        val currentRx = android.net.TrafficStats.getTotalRxBytes()

        totalUploadBytes = if (currentTx.toInt() != android.net.TrafficStats.UNSUPPORTED &&
            initialTxBytes.toInt() != android.net.TrafficStats.UNSUPPORTED)
            currentTx - initialTxBytes
        else 0L

        totalDownloadBytes = if (currentRx.toInt() != android.net.TrafficStats.UNSUPPORTED &&
            initialRxBytes.toInt() != android.net.TrafficStats.UNSUPPORTED)
            currentRx - initialRxBytes
        else 0L
    }
    @SuppressLint("DefaultLocale")
    fun formatTraffic(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> {
                String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            }
            bytes >= 1024 * 1024 -> {
                String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            }
            bytes >= 1024 -> {
                String.format("%.2f KB", bytes / 1024.0)
            }
            else -> {
                "$bytes B"
            }
        }
    }
}