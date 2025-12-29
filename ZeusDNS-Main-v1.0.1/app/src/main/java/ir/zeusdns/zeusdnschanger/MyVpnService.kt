package ir.zeusdns.zeusdnschanger

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.nio.ByteBuffer

@SuppressLint("VpnServicePolicy")
class MyVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private var updateThread: Thread? = null

    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "ZeusVpnChannel"

    private var lastUpdateTime: Long = 0
    private var lastTxBytes: Long = 0
    private var lastRxBytes: Long = 0
    private var currentUploadSpeed: Double = 0.0
    private var currentDownloadSpeed: Double = 0.0

    private var currentDnsName: String = "Free DNS"
    private var currentDnsPrimary: String = "37.32.5.60"
    private var currentDnsSecondary: String = "37.32.5.61"

    private lateinit var notificationManager: NotificationManager
    private var notificationUpdateRunnable: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    companion object {
        const val ACTION_UPDATE_SETTINGS = "ir.zeusdns.ACTION_UPDATE_SETTINGS"
        const val EXTRA_SHOW_NOTIFICATIONS = "show_notifications"
        const val ACTION_REQUEST_STATUS = "ir.zeusdns.ACTION_REQUEST_STATUS"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            "STOP" -> {
                stopVpn()
                START_NOT_STICKY
            }
            ACTION_UPDATE_SETTINGS -> {
                updateNotificationSettings(
                    intent.getBooleanExtra(EXTRA_SHOW_NOTIFICATIONS, true)
                )
                START_NOT_STICKY
            }
            ACTION_REQUEST_STATUS -> {
                sendVpnStatusBroadcast(isRunning)
                START_NOT_STICKY
            }
            else -> {
                notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                createNotificationChannel()

                val primaryDns = intent?.getStringExtra("primary_dns") ?: getActiveDnsFromPrefs(true)
                val secondaryDns = intent?.getStringExtra("secondary_dns") ?: getActiveDnsFromPrefs(false)

                currentDnsPrimary = primaryDns
                currentDnsSecondary = secondaryDns
                currentDnsName = getActiveDnsNameFromPrefs()

                saveCurrentDnsToPrefs()
                startVpn(primaryDns, secondaryDns)
                startNotificationUpdates()

                START_STICKY
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val showNotifications = getNotificationSettings()

            val importance = if (showNotifications) {
                NotificationManager.IMPORTANCE_LOW
            } else {
                NotificationManager.IMPORTANCE_MIN
            }

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Zeus DNS Connection",
                importance
            ).apply {
                description = "Shows DNS connection status and speed"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            try {
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getNotificationSettings(): Boolean {
        val prefs = getSharedPreferences("ZeusDNS_Prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("show_notifications", true)
    }

    private fun updateNotificationSettings(showNotifications: Boolean) {
        notificationUpdateRunnable?.let { handler.removeCallbacks(it) }

        if (showNotifications) {
            startNotificationUpdates()
        } else {
            val simpleNotification = buildSimpleNotification()
            notificationManager.notify(NOTIFICATION_ID, simpleNotification)
        }
    }

    private fun getActiveDnsFromPrefs(isPrimary: Boolean): String {
        val prefs = getSharedPreferences("ZeusDNS_Prefs", Context.MODE_PRIVATE)
        val activeServerId = prefs.getString("active_server_id", "free") ?: "free"

        return when (activeServerId) {
            "free" -> if (isPrimary) "37.32.5.60" else "37.32.5.61"
            "pro" -> if (isPrimary) "37.32.5.34" else "37.32.5.35"
            else -> {
                val customDnsCount = prefs.getInt("custom_dns_count", 0)
                for (i in 0 until customDnsCount) {
                    val id = prefs.getString("dns_${i}_id", "")
                    if (id == activeServerId) {
                        return if (isPrimary) {
                            prefs.getString("dns_${i}_primary", "")?.takeIf { it.isNotBlank() } ?: ""
                        } else {
                            prefs.getString("dns_${i}_secondary", "")?.takeIf { it.isNotBlank() } ?: ""
                        }
                    }
                }
                if (isPrimary) "8.8.8.8" else "8.8.4.4"
            }
        }
    }

    private fun getActiveDnsNameFromPrefs(): String {
        val prefs = getSharedPreferences("ZeusDNS_Prefs", Context.MODE_PRIVATE)
        val activeServerId = prefs.getString("active_server_id", "free") ?: "free"

        return when (activeServerId) {
            "free" -> "Free DNS"
            "pro" -> "Pro DNS"
            else -> {
                val customDnsCount = prefs.getInt("custom_dns_count", 0)
                for (i in 0 until customDnsCount) {
                    val id = prefs.getString("dns_${i}_id", "")
                    if (id == activeServerId) {
                        return prefs.getString("dns_${i}_name", "Custom DNS") ?: "Custom DNS"
                    }
                }
                "Custom DNS"
            }
        }
    }

    private fun saveCurrentDnsToPrefs() {
        val prefs = getSharedPreferences("ZeusDNS_Prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("current_vpn_dns_name", currentDnsName)
            putString("current_vpn_dns_primary", currentDnsPrimary)
            putString("current_vpn_dns_secondary", currentDnsSecondary)
            apply()
        }
    }

    private fun startNotificationUpdates() {
        notificationUpdateRunnable?.let { handler.removeCallbacks(it) }

        lastUpdateTime = SystemClock.elapsedRealtime()
        lastTxBytes = android.net.TrafficStats.getTotalTxBytes()
        lastRxBytes = android.net.TrafficStats.getTotalRxBytes()

        notificationUpdateRunnable = object : Runnable {
            override fun run() {
                updateNotification()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(notificationUpdateRunnable!!)
    }

    private fun updateNotification() {
        val showNotifications = getNotificationSettings()

        if (!showNotifications) {
            val simpleNotification = buildSimpleNotification()
            notificationManager.notify(NOTIFICATION_ID, simpleNotification)
            return
        }

        val currentTime = SystemClock.elapsedRealtime()
        val currentTxBytes = android.net.TrafficStats.getTotalTxBytes()
        val currentRxBytes = android.net.TrafficStats.getTotalRxBytes()

        val timeDiff = (currentTime - lastUpdateTime) / 1000.0
        if (timeDiff > 0) {
            val txDiff = currentTxBytes - lastTxBytes
            val rxDiff = currentRxBytes - lastRxBytes

            currentUploadSpeed = txDiff / timeDiff
            currentDownloadSpeed = rxDiff / timeDiff
        }

        lastUpdateTime = currentTime
        lastTxBytes = currentTxBytes
        lastRxBytes = currentRxBytes

        val notification = buildFullNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildFullNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, MyVpnService::class.java).apply {
            action = "STOP"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val uploadSpeedText = formatSpeed(currentUploadSpeed)
        val downloadSpeedText = formatSpeed(currentDownloadSpeed)

        val notificationContent = "↑ $uploadSpeedText   ↓ $downloadSpeedText"
        val notificationSubText = "$currentDnsName • $currentDnsPrimary"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Zeus Connected")
            .setContentText(notificationContent)
            .setSubText(notificationSubText)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Disconnect",
                    stopPendingIntent
                ).build()
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$notificationContent\n\nDNS Server: $currentDnsName\nPrimary: $currentDnsPrimary\nSecondary: $currentDnsSecondary")
            )
            .build()
    }

    private fun buildSimpleNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, MyVpnService::class.java).apply {
            action = "STOP"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Zeus Connected")
            .setContentText("DNS is active and running...")
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Disconnect",
                    stopPendingIntent
                ).build()
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }

    @SuppressLint("DefaultLocale")
    private fun formatSpeed(bytesPerSecond: Double): String {
        return when {
            bytesPerSecond >= 1024 * 1024 -> String.format("%.1f MB/s", bytesPerSecond / (1024 * 1024))
            bytesPerSecond >= 1024 -> String.format("%.1f KB/s", bytesPerSecond / 1024)
            else -> String.format("%.0f B/s", bytesPerSecond)
        }
    }

    private fun sendVpnStatusBroadcast(isConnected: Boolean) {
        VpnStatusManager.trySendStatus(isConnected)
    }
    private fun startVpn(primaryDns: String, secondaryDns: String) {
        if (isRunning) return

        val builder = Builder()
        builder.setMtu(1280)
        builder.addAddress("10.0.0.2", 32)
        builder.addAddress("fd00:1::2", 128)

        try {
            if (primaryDns.isNotBlank()) {
                builder.addDnsServer(primaryDns)
            }
            if (secondaryDns.isNotBlank()) {
                builder.addDnsServer(secondaryDns)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        builder.allowBypass()
        builder.setSession("Zeus DNS")
        builder.setBlocking(true)

        try {
            vpnInterface = builder.establish()
            if (vpnInterface != null) {
                isRunning = true
                sendVpnStatusBroadcast(true)
                startLoop()

                updateNotification()
                startForeground(NOTIFICATION_ID, buildFullNotification())
            } else {
                stopVpn()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopVpn()
        }
    }

    private fun startLoop() {
        updateThread = Thread {
            val inputStream = FileInputStream(vpnInterface?.fileDescriptor)
            val buffer = ByteBuffer.allocate(32767)

            try {
                while (isRunning && vpnInterface != null) {
                    val length = inputStream.read(buffer.array())
                    if (length > 0) {
                        buffer.clear()
                    }
                }
            } catch (_: Exception) {
            } finally {
                stopVpn()
            }
        }
        updateThread?.start()
    }

    private fun stopVpn() {
        if (!isRunning) return

        isRunning = false

        notificationUpdateRunnable?.let { handler.removeCallbacks(it) }
        notificationUpdateRunnable = null

        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        updateThread?.interrupt()
        updateThread = null

        sendVpnStatusBroadcast(false)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        notificationManager.cancel(NOTIFICATION_ID)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}