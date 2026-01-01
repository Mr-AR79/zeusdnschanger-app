package ir.zeusdns.zeusdnschanger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ir.zeusdns.zeusdnschanger.utils.sendTokenRequest
import kotlinx.coroutines.*
import java.net.URL
import android.content.pm.ServiceInfo

class IpUpdateService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }

        IpUpdateStatusManager.setServiceStatus(true)

        val token = intent?.getStringExtra(EXTRA_TOKEN) ?: ""
        val interval = intent?.getIntExtra(EXTRA_INTERVAL, 1) ?: 1

        if (!isRunning) {
            isRunning = true
            createNotificationChannel()
            val notification = createNotification("ZeusDNS Auto Update Running...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            startUpdateLoop(token, interval)
        }

        return START_STICKY
    }

    private fun startUpdateLoop(token: String, intervalMinutes: Int) {
        serviceScope.launch {
            var lastRegisteredIp: String? = null
            while (isActive && isRunning) {
                try {
                    val currentIp = fetchRealIp()

                    if (currentIp != null && currentIp != lastRegisteredIp) {
                        val result = sendTokenRequest(token, currentIp)
                        if (result.startsWith("Success:")) {
                            lastRegisteredIp = currentIp
                            IpUpdateStatusManager.updateData(currentIp, result)
                            updateNotification("IP Updated: $currentIp")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(intervalMinutes * 60 * 1000L)
            }
        }
    }

    private fun fetchRealIp(): String? {
        return try {
            URL("http://37.32.5.34:81").readText().trim()
        } catch (_: Exception) {
            null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "IP Auto Update Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): android.app.Notification {
        val stopIntent = Intent(this, IpUpdateService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ZeusDNS Auto Updater")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_stat_name)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceJob.cancel()
        IpUpdateStatusManager.setServiceStatus(false)
        IpUpdateStatusManager.clearData()
    }

    companion object {
        const val CHANNEL_ID = "ip_update_channel"
        const val NOTIFICATION_ID = 999
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        const val EXTRA_TOKEN = "EXTRA_TOKEN"
        const val EXTRA_INTERVAL = "EXTRA_INTERVAL"
    }
}