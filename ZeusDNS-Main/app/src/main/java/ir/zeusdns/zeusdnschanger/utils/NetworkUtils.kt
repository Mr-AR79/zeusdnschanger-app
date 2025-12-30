package ir.zeusdns.zeusdnschanger.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

suspend fun getIpAddress(): String {
    return try {
        withContext(Dispatchers.IO) {
            URL("http://37.32.5.34:81/").readText()
        }
    } catch (e: Exception) {
        "Error: ${e.message?.take(20)}..."
    }
}

fun extractJsonValue(json: String, key: String): String? {
    val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
    val result = pattern.find(json)
    return result?.groupValues?.getOrNull(1)
}

fun extractJsonValueInt(json: String, key: String): Int? {
    val pattern = "\"$key\"\\s*:\\s*(\\d+)".toRegex()
    val result = pattern.find(json)
    return result?.groupValues?.getOrNull(1)?.toIntOrNull()
}

fun extractJsonValueBoolean(json: String, key: String): Boolean? {
    val pattern = "\"$key\"\\s*:\\s*(true|false)".toRegex()
    val result = pattern.find(json)
    return result?.groupValues?.getOrNull(1)?.toBooleanStrictOrNull()
}

suspend fun sendTokenRequest(token: String, ip: String): String {
    return try {
        withContext(Dispatchers.IO) {
            val url = URL("http://37.32.5.34:82/tap-in?token=$token&ip=$ip")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = connection.responseCode
            val response = if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP Error: $responseCode"
            }

            connection.disconnect()

            if (responseCode == 200) {
                "Success: $response"
            } else {
                "Error ($responseCode): $response"
            }
        }
    } catch (e: Exception) {
        "Error: ${e.message ?: "Unknown error"}"
    }
}